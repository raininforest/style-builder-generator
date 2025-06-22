package com.github.raininforest.stylebuilder.generator

import com.github.raininforest.stylebuilder.data.ComponentInfo
import com.github.raininforest.stylebuilder.generator.Types.TypeAnnotationComposable
import com.github.raininforest.stylebuilder.generator.Types.TypeAnnotationImmutable
import com.github.raininforest.stylebuilder.generator.Types.TypeAny
import com.github.raininforest.stylebuilder.generator.Types.TypeColor
import com.github.raininforest.stylebuilder.generator.Types.TypeDp
import com.github.raininforest.stylebuilder.generator.Types.TypeInteractiveColor
import com.github.raininforest.stylebuilder.generator.Types.TypeProvidableCompositionLocal
import com.github.raininforest.stylebuilder.generator.Types.TypeShadowAppearance
import com.github.raininforest.stylebuilder.generator.Types.TypeShape
import com.github.raininforest.stylebuilder.generator.Types.TypeStyle
import com.github.raininforest.stylebuilder.generator.Types.TypeStyleBuilder
import com.github.raininforest.stylebuilder.generator.Types.TypeTextStyle
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File

internal class StyleGenerator(
    private val outputDir: File,
    private val packageName: String,
) {

    fun generate(componentInfo: ComponentInfo) {
        val fileSpecBuilder = FileSpec.builder(packageName, styleFileName(componentInfo.name))
        fileSpecBuilder.addImport("androidx.compose.runtime", listOf("compositionLocalOf", "structuralEqualityPolicy"))
        fileSpecBuilder.addImport("com.sdds.compose.uikit.interactions", listOf("asInteractive"))
        fileSpecBuilder.addImport("androidx.compose.ui.graphics", listOf("RectangleShape"))
        fileSpecBuilder.addImport("androidx.compose.ui.unit", listOf("dp"))
        fileSpecBuilder.generateLocal(componentInfo)
        fileSpecBuilder.generateStyle(componentInfo)
        fileSpecBuilder.generateStyleBuilder(componentInfo)
        fileSpecBuilder.generateDefaultStyle(componentInfo)
        fileSpecBuilder.generateColors(componentInfo)
        fileSpecBuilder.generateColorsBuilder(componentInfo)
        fileSpecBuilder.generateDefaultColors(componentInfo)
        fileSpecBuilder.generateDimensions(componentInfo)
        fileSpecBuilder.generateDimensionsBuilder(componentInfo)
        fileSpecBuilder.generateDefaultDimensions(componentInfo)
        fileSpecBuilder
            .build()
            .writeTo(outputDir)
    }

    private fun FileSpec.Builder.generateLocal(componentInfo: ComponentInfo) {
        addProperty(
            PropertySpec
                .builder(
                    name = "Local${styleInterfaceName(componentInfo.name)}",
                    type = TypeProvidableCompositionLocal.parameterizedBy(styleInterfaceType(componentInfo.name)),
                )
                .initializer("compositionLocalOf(structuralEqualityPolicy()) { ${styleInterfaceName(componentInfo.name)}.builder().style() }")
                .build()
        )
    }

    private fun FileSpec.Builder.generateDimensions(componentInfo: ComponentInfo) {
        val dimensionsInterfaceName = dimensionsInterfaceName(componentInfo.name)
        addType(
            TypeSpec.interfaceBuilder(dimensionsInterfaceName)
                .addKdoc("Размеры и отступы компонента")
                .addAnnotation(TypeAnnotationImmutable)
                .addProperties(componentInfo.getDimensionProperties())
                .addType(
                    TypeSpec.companionObjectBuilder()
                        .addFunction(
                            FunSpec.builder("builder")
                                .addKdoc("Возвращает экземпляр [${dimensionsBuilderName(componentInfo.name)}]")
                                .returns(dimensionsBuilderType(componentInfo.name))
                                .addCode("return Default${dimensionsInterfaceName}.Builder()")
                                .build()
                        )
                        .build()
                )
                .build()
        )
    }

    private fun FileSpec.Builder.generateColors(componentInfo: ComponentInfo) {
        val colorsInterfaceName = colorsInterfaceName(componentInfo.name)
        addType(
            TypeSpec.interfaceBuilder(colorsInterfaceName)
                .addKdoc("Цвета компонента")
                .addProperties(componentInfo.getColorProperties())
                .addAnnotation(TypeAnnotationImmutable)
                .addType(
                    TypeSpec.companionObjectBuilder()
                        .addFunction(
                            FunSpec.builder("builder")
                                .addKdoc("Возвращает экземпляр [${colorsBuilderName(componentInfo.name)}]")
                                .returns(colorsBuilderType(componentInfo.name))
                                .addCode("return Default${colorsInterfaceName}.Builder()")
                                .build()
                        )
                        .build()
                )
                .build()
        )
    }

    private fun FileSpec.Builder.generateColorsBuilder(componentInfo: ComponentInfo) {
        val colorsInterfaceName = colorsInterfaceName(componentInfo.name)
        val colorsBuilderInterfaceName = colorsBuilderName(componentInfo.name)
        val buildFun = FunSpec
            .builder("build")
            .addKdoc("Вернёт [${colorsInterfaceName(componentInfo.name)}]")
            .addModifiers(listOf(KModifier.ABSTRACT))
            .returns(colorsInterfaceType(componentInfo.name))
            .build()
        addType(
            TypeSpec.interfaceBuilder(colorsBuilderInterfaceName)
                .addKdoc("Билдер для [$colorsInterfaceName]")
                .addFunctions(componentInfo.getColorFunctions())
                .addFunction(buildFun)
                .build()
        )
    }

    private fun FileSpec.Builder.generateDimensionsBuilder(componentInfo: ComponentInfo) {
        val dimensionsInterfaceName = dimensionsInterfaceName(componentInfo.name)
        val dimensionsBuilderInterfaceName = dimensionsBuilderName(componentInfo.name)
        val buildFun = FunSpec
            .builder("build")
            .addKdoc("Вернёт [${dimensionsInterfaceName(componentInfo.name)}]")
            .addModifiers(listOf(KModifier.ABSTRACT))
            .returns(dimensionsInterfaceType(componentInfo.name))
            .build()
        addType(
            TypeSpec.interfaceBuilder(dimensionsBuilderInterfaceName)
                .addKdoc("Билдер для [$dimensionsInterfaceName]")
                .addFunctions(componentInfo.getDimensionFunctions())
                .addFunction(buildFun)
                .build()
        )
    }

    private fun FileSpec.Builder.generateStyleBuilder(componentInfo: ComponentInfo) {
        val styleBuilderInterfaceName = styleBuilderInterfaceName(componentInfo.name)
        val styleBuilderInterfaceType =
            ClassName("com.sdds.compose.uikit", styleBuilderInterfaceName) //todo move to utils
        val colorsBuilderInterfaceName = colorsBuilderType(componentInfo.name)
        val dimensionsBuilderInterfaceName =
            ClassName("com.sdds.compose.uikit", dimensionsBuilderName(componentInfo.name)) //todo move to utils

        addType(
            TypeSpec.interfaceBuilder(styleBuilderInterfaceName)
                .addKdoc("Билдер стиля компонента")
                .addFunctions(componentInfo.getShapeBuilderFuns(styleBuilderInterfaceType))
                .addFunctions(componentInfo.getShadowBuilderFuns(styleBuilderInterfaceType))
                .addFunctions(componentInfo.getTypographyBuilderFuns(styleBuilderInterfaceType))
                .addFunctions(componentInfo.getColorsBuilderFun(styleBuilderInterfaceType, colorsBuilderInterfaceName))
                .addFunctions(
                    componentInfo.getDimensionsBuilderFun(
                        styleBuilderInterfaceType,
                        dimensionsBuilderInterfaceName
                    )
                )
                .addSuperinterface(
                    TypeStyleBuilder.parameterizedBy(styleInterfaceType(componentInfo.name))
                )
                .build()
        )
    }

    private fun FileSpec.Builder.generateDefaultStyle(componentInfo: ComponentInfo) {
        val interfaceName = styleInterfaceName(componentInfo.name)
        val defaultStyleName = "Default$interfaceName"
        addType(
            TypeSpec.classBuilder(defaultStyleName)
                .addModifiers(KModifier.DATA, KModifier.PRIVATE)
                .addSuperinterface(styleInterfaceType(componentInfo.name))
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameters(
                            componentInfo
                                .getStyleProperties()
                                .map {
                                    ParameterSpec.builder(it.name, it.type)
                                        .addModifiers(listOf(KModifier.OVERRIDE))
                                        .build()
                                })
                        .build()
                )
                .addProperties(componentInfo.getStyleProperties().map {
                    PropertySpec.builder(it.name, it.type)
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer(it.name)
                        .build()
                })
                .addType(
                    TypeSpec.classBuilder("Builder")
                        .addSuperinterface(styleBuilderInterfaceType(componentInfo.name))
                        .addProperties(
                            componentInfo.getStyleProperties().map {
                                if (it.name.contains("color")) {
                                    PropertySpec.builder(
                                        name = "colorsBuilder",
                                        type = colorsBuilderType(componentInfo.name),
                                        modifiers = listOf(KModifier.PRIVATE)
                                    )
                                        .initializer("${colorsInterfaceName(componentInfo.name)}.builder()")
                                        .build()
                                } else if (it.name.contains("dimension")) {
                                    PropertySpec.builder(
                                        name = "dimensionsBuilder",
                                        type = dimensionsBuilderType(componentInfo.name),
                                        modifiers = listOf(KModifier.PRIVATE)
                                    )
                                        .initializer("${dimensionsInterfaceName(componentInfo.name)}.builder()")
                                        .build()

                                } else PropertySpec.builder(
                                    name = it.name,
                                    type = it.type.copy(nullable = true),
                                    modifiers = listOf(KModifier.PRIVATE),
                                )
                                    .mutable(true)
                                    .initializer("null")
                                    .build()
                            }
                        )
                        .addFunctions(
                            componentInfo.getShapeBuilderFuns(styleBuilderInterfaceType(componentInfo.name)).map {
                                FunSpec.builder(it.name)
                                    .returns(it.returnType!!)
                                    .addParameters(it.parameters)
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addCode("return apply { this.${it.name} = ${it.name} }")
                                    .build()
                            }
                        )
                        .addFunctions(
                            componentInfo.getShadowBuilderFuns(styleBuilderInterfaceType(componentInfo.name)).map {
                                FunSpec.builder(it.name)
                                    .returns(it.returnType!!)
                                    .addParameters(it.parameters)
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addCode("return apply { this.${it.name} = ${it.name} }")
                                    .build()
                            }
                        )
                        .addFunctions(
                            componentInfo.getTypographyBuilderFuns(styleBuilderInterfaceType(componentInfo.name)).map {
                                FunSpec.builder(it.name)
                                    .returns(it.returnType!!)
                                    .addParameters(it.parameters)
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addCode("return apply { this.${it.name} = ${it.name} }")
                                    .build()
                            }
                        )
                        .addFunctions(
                            componentInfo.getColorsBuilderFun(
                                styleBuilderInterfaceType(componentInfo.name),
                                colorsBuilderType(componentInfo.name)
                            ).map {
                                FunSpec.builder(it.name)
                                    .addParameters(it.parameters)
                                    .addAnnotations(it.annotations)
                                    .returns(it.returnType!!)
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addCode("return apply { this.colorsBuilder.builder() }")
                                    .build()
                            }
                        )
                        .addFunctions(
                            componentInfo.getDimensionsBuilderFun(
                                styleBuilderInterfaceType(componentInfo.name),
                                dimensionsBuilderType(componentInfo.name)
                            ).map {
                                FunSpec.builder(it.name)
                                    .addParameters(it.parameters)
                                    .addAnnotations(it.annotations)
                                    .returns(it.returnType!!)
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addCode("return apply { this.dimensionsBuilder.builder() }")
                                    .build()
                            }
                        )
                        .addFunction(
                            FunSpec.builder("style")
                                .addModifiers(KModifier.OVERRIDE)
                                .returns(styleInterfaceType(componentInfo.name))
                                .addCode(
                                    "return $defaultStyleName(${getDefaultStyleParameterList(componentInfo).joinToString()})"
                                )
                                .build()
                        )
                        .build()
                )
                .build()
        )
    }

    private fun getDefaultStyleParameterList(componentInfo: ComponentInfo): List<String> {
        val parametersList = mutableListOf<String>()
        componentInfo.getShapeProperties().map {
            "${it.name} = ${it.name} ?: RectangleShape"
        }.also { parametersList.addAll(it) }
        componentInfo.getShadowProperties().map {
            "${it.name} = ${it.name} ?: ShadowAppearance()"
        }.also { parametersList.addAll(it) }
        componentInfo.getTypographyProperties().map {
            "${it.name} = ${it.name} ?: TextStyle.Default"
        }.also { parametersList.addAll(it) }
        componentInfo.getFloatValueProperties().map {
            "${it.name} = ${it.name} ?: 0.5f"
        }.also { parametersList.addAll(it) }
        if (componentInfo.properties.colorProps.isNotEmpty()) parametersList.add("colors = colorsBuilder.build()")
        if (componentInfo.properties.dimensionProps.isNotEmpty()) parametersList.add("dimensions = dimensionsBuilder.build()")
        return parametersList
    }

    private fun FileSpec.Builder.generateDefaultColors(componentInfo: ComponentInfo) {
        val interfaceName = colorsInterfaceName(componentInfo.name)
        val defaultStyleName = "Default$interfaceName"
        addType(
            TypeSpec.classBuilder(defaultStyleName)
                .addModifiers(KModifier.DATA, KModifier.PRIVATE)
                .addSuperinterface(colorsInterfaceType(componentInfo.name))
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameters(
                            componentInfo
                                .getColorProperties()
                                .map {
                                    ParameterSpec.builder(it.name, it.type)
                                        .addModifiers(listOf(KModifier.OVERRIDE))
                                        .build()
                                })
                        .build()
                )
                .addProperties(componentInfo.getColorProperties().map {
                    PropertySpec.builder(it.name, it.type)
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer(it.name)
                        .build()
                })
                .addType(
                    TypeSpec.classBuilder("Builder")
                        .addSuperinterface(colorsBuilderType(componentInfo.name))
                        .addProperties(
                            componentInfo.getColorProperties().map {
                                PropertySpec.builder(
                                    name = it.name,
                                    type = it.type.copy(nullable = true),
                                    modifiers = listOf(KModifier.PRIVATE),
                                )
                                    .mutable(true)
                                    .initializer("null")
                                    .build()
                            }
                        )
                        .addFunctions(
                            componentInfo.getColorFunctions(onlyAbstract = true).map {
                                FunSpec.builder(it.name)
                                    .returns(it.returnType!!)
                                    .addParameters(it.parameters)
                                    .addAnnotations(it.annotations)
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addCode("return apply { this.${it.name} = ${it.name} }")
                                    .build()
                            }
                        )
                        .addFunction(
                            FunSpec.builder("build")
                                .addModifiers(KModifier.OVERRIDE)
                                .returns(colorsInterfaceType(componentInfo.name))
                                .addCode(
                                    "return $defaultStyleName(${
                                        componentInfo.getColorProperties().joinToString {
                                            "${it.name} = ${it.name} ?: Color.Black.asInteractive()"
                                        }
                                    })"
                                )
                                .build()
                        )
                        .build()
                )
                .build()
        )
    }

    private fun FileSpec.Builder.generateDefaultDimensions(componentInfo: ComponentInfo) {
        val interfaceName = dimensionsInterfaceName(componentInfo.name)
        val defaultStyleName = "Default$interfaceName"
        addType(
            TypeSpec.classBuilder(defaultStyleName)
                .addModifiers(KModifier.DATA, KModifier.PRIVATE)
                .addSuperinterface(dimensionsInterfaceType(componentInfo.name))
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameters(
                            componentInfo
                                .getDimensionProperties()
                                .map {
                                    ParameterSpec.builder(it.name, it.type)
                                        .addModifiers(listOf(KModifier.OVERRIDE))
                                        .build()
                                })
                        .build()
                )
                .addProperties(componentInfo.getDimensionProperties().map {
                    PropertySpec.builder(it.name, it.type)
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer(it.name)
                        .build()
                })
                .addType(
                    TypeSpec.classBuilder("Builder")
                        .addSuperinterface(dimensionsBuilderType(componentInfo.name))
                        .addProperties(
                            componentInfo.getDimensionProperties().map {
                                PropertySpec.builder(
                                    name = it.name,
                                    type = it.type.copy(nullable = true),
                                    modifiers = listOf(KModifier.PRIVATE),
                                )
                                    .mutable(true)
                                    .initializer("null")
                                    .build()
                            }
                        )
                        .addFunctions(
                            componentInfo.getDimensionFunctions().map {
                                FunSpec.builder(it.name)
                                    .returns(it.returnType!!)
                                    .addParameters(it.parameters)
                                    .addAnnotations(it.annotations)
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addCode("return apply { this.${it.name} = ${it.name} }")
                                    .build()
                            }
                        )
                        .addFunction(
                            FunSpec.builder("build")
                                .addModifiers(KModifier.OVERRIDE)
                                .returns(dimensionsInterfaceType(componentInfo.name))
                                .addCode(
                                    "return $defaultStyleName(${
                                        componentInfo.getDimensionProperties().joinToString {
                                            "${it.name} = ${it.name} ?: 8.dp"
                                        }
                                    })"
                                )
                                .build()
                        )
                        .build()
                )
                .build()
        )
    }

    private fun ComponentInfo.getDimensionsBuilderFun(
        interfaceType: ClassName,
        dimensionsBuilderInterfaceName: ClassName
    ): List<FunSpec> {
        return listOf(
            FunSpec.builder("dimensions")
                .addAnnotation(TypeAnnotationComposable)
                .addModifiers(listOf(KModifier.ABSTRACT))
                .addKdoc("Устанавливает")
                .addParameter(
                    ParameterSpec
                        .builder(
                            name = "builder",
                            type = getLambdaType(
                                annotation = TypeAnnotationComposable,
                                receiver = dimensionsBuilderInterfaceName
                            )
                        ).build()
                )
                .returns(interfaceType)
                .build()
        )
    }

    private fun ComponentInfo.getColorsBuilderFun(
        interfaceType: ClassName,
        colorsBuilderInterfaceName: ClassName
    ): List<FunSpec> {
        return listOf(
            FunSpec.builder("colors")
                .addAnnotation(TypeAnnotationComposable)
                .addModifiers(listOf(KModifier.ABSTRACT))
                .addKdoc("Устанавливает цвет")
                .addParameter(
                    ParameterSpec
                        .builder(
                            name = "builder",
                            type = getLambdaType(
                                annotation = TypeAnnotationComposable,
                                receiver = colorsBuilderInterfaceName
                            )
                        ).build()
                )
                .returns(interfaceType)
                .build()
        )
    }

    private fun ComponentInfo.getShapeBuilderFuns(interfaceType: ClassName): List<FunSpec> =
        properties.shapeProps.map {
            FunSpec.builder(it)
                .addKdoc("Устанавливает форму")
                .addModifiers(listOf(KModifier.ABSTRACT))
                .addParameter(it, TypeShape)
                .returns(interfaceType)
                .build()
        }

    private fun ComponentInfo.getShadowBuilderFuns(interfaceType: ClassName): List<FunSpec> =
        properties.shadowProps.map {
            FunSpec.builder(it)
                .addKdoc("Устанавливает тень")
                .addModifiers(listOf(KModifier.ABSTRACT))
                .addParameter(it, TypeShadowAppearance)
                .returns(interfaceType)
                .build()
        }

    private fun ComponentInfo.getTypographyBuilderFuns(interfaceType: ClassName): List<FunSpec> =
        properties.typographyProps.map {
            FunSpec.builder(it)
                .addKdoc("Устанавливает стиль текста")
                .addModifiers(listOf(KModifier.ABSTRACT))
                .addParameter(it, TypeTextStyle)
                .returns(interfaceType)
                .build()
        }

    private fun FileSpec.Builder.generateStyle(componentInfo: ComponentInfo) {
        val interfaceName = styleInterfaceName(componentInfo.name)
        addType(
            TypeSpec.interfaceBuilder(interfaceName)
                .addKdoc("Стиль компонента")
                .addProperties(componentInfo.getStyleProperties())
                .addSuperinterface(TypeStyle)
                .addAnnotation(TypeAnnotationImmutable)
                .addType(
                    TypeSpec.companionObjectBuilder()
                        .addFunction(
                            FunSpec.builder("builder")
                                .addKdoc("Возвращает экземпляр [${styleBuilderInterfaceName(componentInfo.name)}]")
                                .addParameter(
                                    ParameterSpec.builder("receiver", TypeAny.copy(nullable = true))
                                        .defaultValue("null")
                                        .build()
                                )
                                .returns(styleBuilderInterfaceType(componentInfo.name))
                                .addCode("return Default${interfaceName}.Builder()")
                                .build()
                        )
                        .build()
                )
                .build()
        )
    }

    private fun ComponentInfo.getStyleProperties(): List<PropertySpec> {
        val typographyProperties = getTypographyProperties()
        val shapeProperties = getShapeProperties()
        val floatProperties = getFloatValueProperties()
        val shadowProperties = getShadowProperties()

        return typographyProperties + shapeProperties + shadowProperties + floatProperties + getColorsProperty() + getDimensionsProperty()
    }

    private fun ComponentInfo.getTypographyProperties(
    ): List<PropertySpec> {
        return properties.typographyProps.map {
            PropertySpec
                .builder(name = it, type = TypeTextStyle)
                .apply {
                    addKdoc("Стиль")
                    addModifiers(modifiers)
                }
                .build()
        }
    }

    private fun ComponentInfo.getColorProperties(vararg modifier: KModifier): List<PropertySpec> {
        return properties.colorProps.map {
            PropertySpec
                .builder(name = it, type = ClassName("com.sdds.compose.uikit.interactions", "InteractiveColor"))
                .addModifiers(modifier.toList())
                .addKdoc("Цвет")
                .build()
        }
    }

    private fun ComponentInfo.getColorFunctions(onlyAbstract: Boolean = false): List<FunSpec> {
        return if (onlyAbstract) {
            properties.colorProps.map {
                FunSpec
                    .builder(name = it)
                    .addKdoc("Устанавливает цвет [$it]")
                    .addModifiers(listOf(KModifier.ABSTRACT))
                    .addParameter(it, TypeInteractiveColor)
                    .returns(colorsBuilderType(name))
                    .build()
            }
        } else {
            properties.colorProps.flatMap {
                listOf(
                    FunSpec
                        .builder(name = it)
                        .addKdoc("Устанавливает цвет [$it]")
                        .addModifiers(listOf(KModifier.ABSTRACT))
                        .addParameter(it, TypeInteractiveColor)
                        .returns(colorsBuilderType(name))
                        .build(),
                    FunSpec
                        .builder(name = it)
                        .addKdoc("Устанавливает цвет [$it]")
                        .returns(colorsBuilderType(name))
                        .addParameter(it, TypeColor)
                        .addCode("return $it($it.asInteractive())")
                        .build(),
                )
            }
        }
    }

    private fun ComponentInfo.getDimensionFunctions(): List<FunSpec> {
        return properties.dimensionProps.map {
            FunSpec
                .builder(name = it)
                .addKdoc("Устанавливает [$it]")
                .addModifiers(listOf(KModifier.ABSTRACT))
                .addParameter(it, TypeDp)
                .returns(dimensionsBuilderType(name))
                .build()
        }
    }

    private fun ComponentInfo.getDimensionProperties(): List<PropertySpec> {
        return properties.dimensionProps.map {
            PropertySpec
                .builder(name = it, type = TypeDp)
                .addKdoc("Отступ")
                .build()
        }
    }

    private fun ComponentInfo.getDimensionsProperty(): PropertySpec {
        return PropertySpec
            .builder(name = "dimensions", type = dimensionsInterfaceType(name))
            .apply {
                addKdoc("Размеры и отступы компонента")
                addModifiers(modifiers)
            }
            .build()

    }

    private fun ComponentInfo.getColorsProperty(): PropertySpec {
        return PropertySpec
            .builder(name = "colors", type = colorsInterfaceType(name))
            .apply {
                addKdoc("Цвета компонента")
                addModifiers(modifiers)
            }
            .build()

    }

    private fun ComponentInfo.getFloatValueProperties(): List<PropertySpec> {
        return properties.floatValueProps.map {
            PropertySpec
                .builder(name = it, type = Float::class.asClassName())
                .apply {
                    addKdoc("Значение")
                    addModifiers(modifiers)
                }
                .build()
        }
    }

    private fun ComponentInfo.getShapeProperties(): List<PropertySpec> {
        return properties.shapeProps.map {
            PropertySpec
                .builder(name = it, type = TypeShape)
                .apply {
                    addKdoc("Форма")
                    addModifiers(modifiers)
                }
                .build()
        }
    }

    private fun ComponentInfo.getShadowProperties(): List<PropertySpec> {
        return properties.shadowProps.map {
            PropertySpec
                .builder(name = it, type = TypeShadowAppearance)
                .apply {
                    addKdoc("Тень")
                    addModifiers(modifiers)
                }
                .build()
        }
    }
}
