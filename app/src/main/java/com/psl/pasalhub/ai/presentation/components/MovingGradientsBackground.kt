package com.psl.pasalhub.ai.presentation.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import kotlin.math.cos
import kotlin.math.sin

private const val SHADER_SRC = """
    uniform float2 iResolution;
    uniform float iTime;
    layout(color) uniform half4 iColor1;
    layout(color) uniform half4 iColor2;
    layout(color) uniform half4 iColor3;

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / iResolution.xy;
        float2 p = -1.0 + 2.0 * uv;
        p.x *= iResolution.x / iResolution.y;

        // Swirling animation logic
        for(float n = 1.0; n < 4.0; n++) {
            p.x += 0.7 / n * sin(n * p.y + iTime + 0.3 * n) + 0.8;
            p.y += 0.4 / n * sin(n * p.x + iTime + 0.5 * n) - 0.5;
        }

        float v = sin(p.x + p.y);
        vec3 color = mix(iColor1.rgb, iColor2.rgb, 0.5 + 0.5 * sin(iTime * 0.2));
        color = mix(color, iColor3.rgb, 0.5 + 0.5 * v);
        
        return vec4(color, 1.0);
    }
"""

@Composable
fun MovingGradientsBackground(
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false,
    isDark: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val duration = if (isProcessing) 12000 else 24000

    // Use 2 * PI * N for seamless looping in shaders
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = Math.PI.toFloat() * 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val color1 = MaterialTheme.colorScheme.surface
    val color2 =
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.2f else 0.15f)
    val color3 =
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (isDark) 0.15f else 0.1f)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ShaderBackground(
            timeProvider = { time },
            color1 = color1,
            color2 = color2,
            color3 = color3,
            modifier = modifier
        )
    } else {
        FallbackCanvasBackground(
            timeProvider = { time / 10f }, // Adjust scale for canvas
            color1 = color1,
            color2 = color2,
            color3 = color3,
            modifier = modifier
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun ShaderBackground(
    timeProvider: () -> Float,
    color1: Color,
    color2: Color,
    color3: Color,
    modifier: Modifier = Modifier
) {
    val shader = remember { RuntimeShader(SHADER_SRC) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer() // GPU Layer isolation
            .drawWithCache {
                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setColorUniform("iColor1", color1.toArgb())
                shader.setColorUniform("iColor2", color2.toArgb())
                shader.setColorUniform("iColor3", color3.toArgb())

                val brush = ShaderBrush(shader)
                onDrawBehind {
                    shader.setFloatUniform("iTime", timeProvider())
                    drawRect(brush)
                }
            }
    ) {
        // Empty because drawWithCache handles it
    }
}

@Composable
private fun FallbackCanvasBackground(
    timeProvider: () -> Float,
    color1: Color,
    color2: Color,
    color3: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val time = timeProvider()

        drawRect(color = color1)

        val angle = time * 0.2f * Math.PI.toFloat()

        // Optimized blobs for fallback
        val x1 = width * 0.5f + (width * 0.2f) * cos(angle)
        val y1 = height * 0.4f + (height * 0.15f) * sin(angle * 0.6f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color2, Color.Transparent),
                center = Offset(x1, y1),
                radius = width * 0.9f
            ),
            center = Offset(x1, y1),
            radius = width * 0.9f
        )

        val x2 = width * 0.6f + (width * 0.3f) * sin(angle * 0.7f)
        val y2 = height * 0.7f + (height * 0.2f) * cos(angle * 0.5f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color3, Color.Transparent),
                center = Offset(x2, y2),
                radius = width * 0.7f
            ),
            center = Offset(x2, y2),
            radius = width * 0.7f
        )
    }
}
