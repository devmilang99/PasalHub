package com.psl.pasalhub.ai.appfunctions

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@AppFunctionServiceEntryPoint(
    serviceName = "PasalHubAIAppFunctionService",
    appFunctionXmlFileName = "pasalhub_functions"
)
abstract class PasalHubFunctionService : AppFunctionService() {

    @AppFunction(isDescribedByKDoc = true)
    fun exploreLatestTech(context: AppFunctionContext): String {
        return "latest electronics"
    }

    @AppFunction(isDescribedByKDoc = true)
    fun exploreSummerFashion(context: AppFunctionContext): String {
        return "summer fashion clothing"
    }

    @AppFunction(isDescribedByKDoc = true)
    fun exploreGamingGear(context: AppFunctionContext): String {
        return "gaming gear and accessories"
    }

    @AppFunction(isDescribedByKDoc = true)
    fun exploreHomeDecor(context: AppFunctionContext): String {
        return "home decor and appliances"
    }
}
