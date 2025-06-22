package com.github.raininforest.stylebuilder.generator

import com.squareup.kotlinpoet.ClassName

object Types {
    val TypeShape = ClassName("androidx.compose.ui.graphics", "Shape")
    val TypeTextStyle = ClassName("androidx.compose.ui.text", "TextStyle")
    val TypeAnnotationComposable = ClassName("androidx.compose.runtime", "Composable")
    val TypeAnnotationImmutable = ClassName("androidx.compose.runtime", "Immutable")
    val TypeStyle = ClassName("com.sdds.compose.uikit.style", "Style")
    val TypeStyleBuilder = ClassName("com.sdds.compose.uikit.style", "StyleBuilder")
    val TypeShadowAppearance = ClassName("com.sdds.compose.uikit.shadow", "ShadowAppearance")
    val TypeAny = ClassName("kotlin", "Any")
    val TypeInteractiveColor = ClassName("com.sdds.compose.uikit.interactions", "InteractiveColor")
    val TypeColor = ClassName("androidx.compose.ui.graphics", "Color")
    val TypeDp = ClassName("androidx.compose.ui.unit", "Dp")
    val TypeProvidableCompositionLocal = ClassName("androidx.compose.runtime", "ProvidableCompositionLocal")
}