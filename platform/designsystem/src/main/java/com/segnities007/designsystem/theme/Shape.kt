package com.segnities007.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/** Material3 [Shapes] と [NofyCornerRadius] を対応づけたアプリ既定形状。 */
val Shapes = Shapes(
    small = RoundedCornerShape(NofyCornerRadius.small),
    medium = RoundedCornerShape(NofyCornerRadius.medium),
    large = RoundedCornerShape(NofyCornerRadius.large),
    extraLarge = RoundedCornerShape(NofyCornerRadius.extraLarge)
)
