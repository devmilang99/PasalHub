package com.psl.pasalhub.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

data class PasalHubDimens(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val logoSize: Dp = 48.dp,
    val buttonHeight: Dp = 48.dp,
    val cardCorner: Dp = 16.dp,
    val padding: Dp = 16.dp
)

val LocalDimens = compositionLocalOf { PasalHubDimens() }

@Composable
fun ProvideDimens(
    windowSizeClass: WindowSizeClass,
    content: @Composable () -> Unit
) {
    val dimens = when {
        windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded -> {
            PasalHubDimens(
                extraSmall = 8.dp,
                small = 12.dp,
                medium = 24.dp,
                large = 48.dp,
                extraLarge = 64.dp,
                logoSize = 80.dp,
                buttonHeight = 64.dp,
                cardCorner = 24.dp,
                padding = 32.dp
            )
        }

        windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium -> {
            PasalHubDimens(
                extraSmall = 6.dp,
                small = 10.dp,
                medium = 20.dp,
                large = 36.dp,
                extraLarge = 48.dp,
                logoSize = 64.dp,
                buttonHeight = 56.dp,
                cardCorner = 20.dp,
                padding = 24.dp
            )
        }

        else -> {
            PasalHubDimens()
        }
    }

    CompositionLocalProvider(LocalDimens provides dimens) {
        content()
    }
}
