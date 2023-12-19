package com.edisonli.patterns.visitor

import com.edisonli.patterns.anno.BuilderAnno
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid

class BuilderVisitor(private val environment: SymbolProcessorEnvironment) : KSVisitorVoid() {

    @Suppress("NewApi")
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitClassDeclaration(classDeclaration, data)
        val builderClass = classDeclaration.annotations.filter {
            it.shortName.asString() == BuilderAnno::class.simpleName
        }.toList()

        if (builderClass.isNotEmpty()) {
            builderClass.stream().forEach {
                val className = "${classDeclaration.simpleName.asString()}BuilderCreator"
                val packageName = classDeclaration.packageName.asString()
                val constructorParams =
                    classDeclaration.primaryConstructor?.parameters?.joinToString(separator = ", ") {
                        "${it.name?.asString()}: ${it.type.resolve().declaration.qualifiedName?.asString()}?"
                    } ?: ""
                val builderCode = if (constructorParams.isEmpty()) {
                    throw Exception("no any constructor parameters...")
                } else {
                    """
package $packageName

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
            }
        }
    }
}