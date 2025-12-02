package com.example.miappmodular.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Variantes de botón estilo shadcn.io
 */
sealed class ButtonVariant(
    val backgroundColor: Color,
    val contentColor: Color,
    val pressedColor: Color
) {
    object Default : ButtonVariant(
        backgroundColor = Primary,
        contentColor = Color.White,
        pressedColor = PrimaryLight
    )

    object Outline : ButtonVariant(
        backgroundColor = Color.Transparent,
        contentColor = Foreground,
        pressedColor = PaleGray
    )

    object Ghost : ButtonVariant(
        backgroundColor = Color.Transparent,
        contentColor = Foreground,
        pressedColor = PaleGray
    )

    object Destructive : ButtonVariant(
        backgroundColor = com.example.miappmodular.ui.theme.Destructive,
        contentColor = Color.White,
        pressedColor = Color(0xFFDC2626) // red-600
    )

    object Secondary : ButtonVariant(
        backgroundColor = Beige,
        contentColor = Foreground,
        pressedColor = LightBrown
    )
}

/**
 * Tamaños de botón
 */
sealed class ButtonSize(
    val height: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp
) {
    object Small : ButtonSize(32.dp, 12.dp, 0.dp)
    object Default : ButtonSize(40.dp, 16.dp, 0.dp)
    object Large : ButtonSize(44.dp, 24.dp, 0.dp)
}

/**
 * Variantes de Badge estilo shadcn.io
 */
sealed class BadgeVariant(
    val backgroundColor: Color,
    val borderColor: Color,
    val textColor: Color
) {
    object Default : BadgeVariant(
        backgroundColor = PaleGray,
        borderColor = Border,
        textColor = Foreground
    )

    object Success : BadgeVariant(
        backgroundColor = SuccessLight,
        borderColor = com.example.miappmodular.ui.theme.Success.copy(alpha = 0.3f),
        textColor = Color(0xFF166534) // green-800
    )

    object Destructive : BadgeVariant(
        backgroundColor = DestructiveLight,
        borderColor = com.example.miappmodular.ui.theme.Destructive.copy(alpha = 0.3f),
        textColor = Color(0xFF991B1B) // red-800
    )

    object Warning : BadgeVariant(
        backgroundColor = WarningLight,
        borderColor = com.example.miappmodular.ui.theme.Warning.copy(alpha = 0.3f),
        textColor = Color(0xFF92400E) // amber-800
    )
}