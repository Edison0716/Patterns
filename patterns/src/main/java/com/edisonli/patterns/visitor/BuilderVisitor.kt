package com.edisonli.patterns.visitor

import com.edisonli.patterns.anno.BuilderAnno
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Origin

class BuilderVisitor(private val environment: SymbolProcessorEnvironment) : KSVisitorVoid() {

    @Suppress("NewApi")
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitClassDeclaration(classDeclaration, data)
        val builderClass = classDeclaration.annotations.filter {
            it.shortName.asString() == BuilderAnno::class.simpleName
        }.toList()

        if (builderClass.isNotEmpty()) {
            builderClass.stream().forEach {
                val className = "${classDeclaration.simpleName.asString()}${BUILDER_CREATOR_SUFFIX}"
                val packageName = classDeclaration.packageName.asString()
                if (it.origin == Origin.KOTLIN) {
                    val constructorParams =
                        classDeclaration.primaryConstructor?.parameters?.joinToString(separator = ", ") {
                            "${it.name?.asString()}: ${it.type.resolve().declaration.qualifiedName?.asString()}?"
                        } ?: ""
                    val builderCode = if (constructorParams.isEmpty()) {
                        throw Exception("no any constructor parameters...")
                    } else {
                        """
package $packageName

/**
 * You can find the generate builder class by [ClassName]BuilderCreator.
 */
class $className private constructor(
    ${classDeclaration.primaryConstructor?.parameters?.joinToString(separator = ",\n    ") {
                            "${it.name?.asString()}: ${it.type.resolve().declaration.qualifiedName?.asString()}?"
                        }}
) {
    class Builder {
        ${classDeclaration.primaryConstructor?.parameters?.joinToString(separator = "\n        ") {
                            "private var ${it.name?.asString()}: ${it.type.resolve().declaration.qualifiedName?.asString()}? = null"
                        }}

        ${classDeclaration.primaryConstructor?.parameters?.joinToString(separator = "\n        ") {
                            "fun ${it.name?.asString()}(${it.name?.asString()}: ${it.type.resolve().declaration.qualifiedName?.asString()}) = apply { this.${it.name?.asString()} = ${it.name?.asString()} }"
                        }}

        fun build() = $className(
            ${classDeclaration.primaryConstructor?.parameters?.joinToString(separator = ",\n            ") { it.name?.asString() ?: "" }}
        )
    }
}
""".trimIndent()

                    }
                    val outputStream = environment.codeGenerator.createNewFile(
                        Dependencies(false, classDeclaration.containingFile!!),
                        packageName,
                        className,
                    )
                    outputStream.write(builderCode.toByteArray())
                    outputStream.close()
                } else if (it.origin == Origin.JAVA) {
                    val builderCode = generateKotlinBuilderCodeForJavaClass(classDeclaration)
                    val outputStream = environment.codeGenerator.createNewFile(
                        Dependencies(false, classDeclaration.containingFile!!),
                        packageName,
                        className
                    )
                    outputStream.write(builderCode.toByteArray())
                    outputStream.close()
                }
            }
        }
    }

    private fun generateKotlinBuilderCodeForJavaClass(classDeclaration: KSClassDeclaration): String {
        val className = classDeclaration.simpleName.asString()
        val builderClassName = "${className}${BUILDER_CREATOR_SUFFIX}"
        val packageName = classDeclaration.packageName.asString()

        // 获取类的所有属性并为每个属性生成一个变量和设置方法
        val properties = classDeclaration.getAllProperties().joinToString(separator = "\n    ") {
            "var ${it.simpleName.asString()}: ${it.type.resolve().declaration.qualifiedName?.asString()}? = null"
        }

        val builderMethods =
            classDeclaration.getAllProperties().joinToString(separator = "\n    ") {
                val propName = it.simpleName.asString()
                val propType = it.type.resolve().declaration.qualifiedName?.asString()
                "fun $propName($propName: $propType) = apply { this.$propName = $propName }"
            }

        // 构建 build 方法的参数列表
        val buildMethodParameters =
            classDeclaration.getAllProperties().joinToString(separator = ", ") {
                it.simpleName.asString()
            }

        return """
package $packageName

class $builderClassName {
    $properties

    $builderMethods

    fun build(): $className {
        return $className($buildMethodParameters)
    }
}
    """.trimIndent()
    }

    companion object {
        const val BUILDER_CREATOR_SUFFIX = "BuilderCreator"
    }
}