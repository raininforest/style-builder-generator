package com.github.raininforest.stylebuilder.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName

internal fun styleFileName(componentName: String) = styleInterfaceName(componentName)

internal fun styleInterfaceName(componentName: String): String {
    return "${componentName}Style"
}

internal fun styleInterfaceType(componentName: String): ClassName {
    return ClassName("com.sdds.compose.uikit", styleInterfaceName(componentName))
}

internal fun styleBuilderInterfaceName(componentName: String): String {
    return "${componentName}StyleBuilder"
}

internal fun styleBuilderInterfaceType(componentName: String): ClassName {
    return ClassName("com.sdds.compose.uikit", styleBuilderInterfaceName(componentName))
}

internal fun colorsInterfaceName(componentName: String): String {
    return "${componentName}Colors"
}

internal fun colorsInterfaceType(componentName: String): ClassName {
    return ClassName("com.sdds.compose.uikit", colorsInterfaceName(componentName))
}

internal fun dimensionsInterfaceName(componentName: String): String {
    return "${componentName}Dimensions"
}

internal fun dimensionsInterfaceType(componentName: String): ClassName {
    return ClassName("com.sdds.compose.uikit", dimensionsInterfaceName(componentName))
}

internal fun colorsBuilderName(componentName: String): String {
    return "${componentName}ColorsBuilder"
}

internal fun colorsBuilderType(componentName: String) = ClassName("com.sdds.compose.uikit", colorsBuilderName(componentName))

internal fun dimensionsBuilderName(componentName: String): String {
    return "${componentName}DimensionsBuilder"
}

internal fun dimensionsBuilderType(componentName: String) = ClassName("com.sdds.compose.uikit", dimensionsBuilderName(componentName))

internal fun getLambdaType(annotation: ClassName? = null, receiver: ClassName? = null): TypeName {
    val lambdaType = LambdaTypeName.get(
        receiver = receiver,
        returnType = Unit::class.asClassName(),
    )
    return annotation?.let {
        lambdaType.copy(annotations = listOf(AnnotationSpec.builder(it).build()))
    } ?: lambdaType
}
