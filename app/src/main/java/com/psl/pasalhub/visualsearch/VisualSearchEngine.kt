package com.psl.pasalhub.visualsearch

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.scale
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisualSearchEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var interpreter: Interpreter? = null

    sealed class VisualSearchResult {
        data class Success(val embeddings: FloatArray) : VisualSearchResult() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false
                other as Success
                return embeddings.contentEquals(other.embeddings)
            }

            override fun hashCode(): Int {
                return embeddings.contentHashCode()
            }
        }

        data class Error(val message: String, val throwable: Throwable? = null) :
            VisualSearchResult()

        object InterpreterNull : VisualSearchResult()
    }

    companion object {
        private const val TAG = "VisualSearchEngine"
        private const val MODEL_PATH = "mobilenetv3_small_quant.tflite"
        private const val INPUT_SIZE = 224
        private const val EMBEDDING_SIZE = 576
    }

    init {
        try {
            loadModel()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading TFLite model", e)
        }
    }

    @Throws(IOException::class)
    private fun loadModel() {
        val fd = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fd.fileDescriptor)
        val fileChannel = inputStream.channel
        val modelBuffer = fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fd.startOffset,
            fd.declaredLength
        )
        interpreter = Interpreter(modelBuffer)
        Log.d(TAG, "Model loaded successfully")

        // Log tensor metadata for debugging
        val inputTensor = interpreter?.getInputTensor(0)
        val outputTensor = interpreter?.getOutputTensor(0)
        Log.d(TAG, "Input Tensor DataType: ${inputTensor?.dataType()}")
        Log.d(TAG, "Output Tensor DataType: ${outputTensor?.dataType()}")
    }

    fun extractEmbeddings(bitmap: Bitmap): VisualSearchResult {
        val interpreter = this.interpreter ?: run {
            Log.e(TAG, "Interpreter is null")
            return VisualSearchResult.InterpreterNull
        }

        try {
            // 1. Scale Bitmap to expected shape (224 x 224)
            val resizedBitmap = bitmap.scale(INPUT_SIZE, INPUT_SIZE)

            // 2. Query input/output specs
            val inputTensor = interpreter.getInputTensor(0)
            val outputTensor = interpreter.getOutputTensor(0)

            // 3. Preprocess bitmap according to expected input data type
            val inputBuffer = preprocessImage(resizedBitmap, inputTensor.dataType())

            // 4. Run inference according to expected output data type
            val embeddings = when (outputTensor.dataType()) {
                DataType.INT8 -> {
                    val outputArray = Array(1) { ByteArray(EMBEDDING_SIZE) }
                    interpreter.run(inputBuffer, outputArray)

                    // Dequantize INT8 back to Float
                    val quantParams = outputTensor.quantizationParams()
                    val scale = quantParams.scale
                    val zeroPoint = quantParams.zeroPoint

                    FloatArray(EMBEDDING_SIZE) { i ->
                        (outputArray[0][i].toInt() - zeroPoint) * scale
                    }
                }

                DataType.UINT8 -> {
                    val outputArray = Array(1) { ByteArray(EMBEDDING_SIZE) }
                    interpreter.run(inputBuffer, outputArray)

                    // Dequantize UINT8 back to Float
                    val quantParams = outputTensor.quantizationParams()
                    val scale = quantParams.scale
                    val zeroPoint = quantParams.zeroPoint

                    FloatArray(EMBEDDING_SIZE) { i ->
                        ((outputArray[0][i].toInt() and 0xFF) - zeroPoint) * scale
                    }
                }

                DataType.FLOAT32 -> {
                    val outputArray = Array(1) { FloatArray(EMBEDDING_SIZE) }
                    interpreter.run(inputBuffer, outputArray)
                    outputArray[0]
                }

                else -> throw IllegalArgumentException("Unsupported output tensor type: ${outputTensor.dataType()}")
            }

            Log.d(TAG, "Inference successful. Embedding size: ${embeddings.size}")
            Log.d(TAG, "Sample values: ${embeddings.take(5)}")
            return VisualSearchResult.Success(embeddings)

        } catch (e: Exception) {
            Log.e(TAG, "Error during inference", e)
            return VisualSearchResult.Error("Inference failed: ${e.message}", e)
        }
    }

    private fun preprocessImage(bitmap: Bitmap, dataType: DataType): ByteBuffer {
        val bytesPerChannel = if (dataType == DataType.FLOAT32) 4 else 1
        val bufferSize = 1 * INPUT_SIZE * INPUT_SIZE * 3 * bytesPerChannel

        val inputBuffer = ByteBuffer.allocateDirect(bufferSize).apply {
            order(ByteOrder.nativeOrder())
            rewind()
        }

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            when (dataType) {
                DataType.INT8 -> {
                    // Converts 0..255 to -128..127 range
                    inputBuffer.put((r - 128).toByte())
                    inputBuffer.put((g - 128).toByte())
                    inputBuffer.put((b - 128).toByte())
                }

                DataType.UINT8 -> {
                    // Keeps 0..255 byte range
                    inputBuffer.put(r.toByte())
                    inputBuffer.put(g.toByte())
                    inputBuffer.put(b.toByte())
                }

                DataType.FLOAT32 -> {
                    // Normalizes to 0.0f.1.0f range
                    inputBuffer.putFloat(r / 255.0f)
                    inputBuffer.putFloat(g / 255.0f)
                    inputBuffer.putFloat(b / 255.0f)
                }

                else -> throw IllegalArgumentException("Unsupported input tensor type: $dataType")
            }
        }

        inputBuffer.rewind()
        return inputBuffer
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
