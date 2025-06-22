package com.github.raininforest.stylebuilder

import ai.grazie.utils.dropPostfix
import com.github.raininforest.stylebuilder.data.ComponentInfo
import com.github.raininforest.stylebuilder.data.ComponentProperties
import com.github.raininforest.stylebuilder.generator.StyleGenerator
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import org.jetbrains.kotlin.psi.KtClass
import java.io.File

class StyleGeneratorAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        generateStyle(e)
    }

    private fun generateStyle(e: AnActionEvent) {
        val currentFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val classes = currentFile.children.filterIsInstance<KtClass>()
        val propertiesClass = classes
            .find { clazz ->
                clazz.superTypeListEntries
                    .mapNotNull { it.typeReference?.text }
                    .contains("PropertyOwner")
            } ?: return

        val shapeProps = mutableListOf<String>()
        val shadowProps = mutableListOf<String>()
        val floatValueProps = mutableListOf<String>()
        val typographyProps = mutableListOf<String>()
        val colorProps = mutableListOf<String>()
        val dimensionProps = mutableListOf<String>()

        val properties = propertiesClass.primaryConstructorParameters
        properties.forEach {
            val name = it.name
            val typeText = it.typeReference?.text.orEmpty()
            when {
                typeText.contains("Color") -> name?.let { colorProps.add(name) }
                typeText.contains("Dimension") -> name?.let { dimensionProps.add(name) }
                typeText.contains("Typography") -> name?.let { typographyProps.add(name) }
                typeText.contains("FloatValue") -> name?.let { floatValueProps.add(name) }
                typeText.contains("Shape") -> name?.let { shapeProps.add(name) }
                typeText.contains("Shadow") -> name?.let { shadowProps.add(name) }
                else -> {}
            }
        }
        val componentName = propertiesClass
            .name
            ?.dropPostfix("Properties") ?: return
        val componentInfo = ComponentInfo(
            name = componentName,
            properties = ComponentProperties(
                shapeProps = shapeProps,
                floatValueProps = floatValueProps,
                typographyProps = typographyProps,
                colorProps = colorProps,
                dimensionProps = dimensionProps,
                shadowProps = shadowProps,
            )
        )

        StyleGenerator(
            packageName = StylesPackage,
            outputDir = File("${e.project?.basePath.orEmpty()}/$OutputDirPath")
        ).apply { generate(componentInfo) }
    }

    private companion object {
        const val OutputDirPath = "/sdds-core/uikit-compose/src/main/kotlin"
        const val StylesPackage = "com.sdds.compose.uikit"
    }
}