package com.edisonli.patterns.visitor

import com.edisonli.patterns.anno.SingletonAnno
import com.edisonli.patterns.base.BaseSingleton
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import java.io.File
import java.io.FileOutputStream

class SingletonVisitor : KSVisitorVoid() {
    @Suppress("NewApi")
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitClassDeclaration(classDeclaration, data)
        val singletonClass = classDeclaration
            .annotations
            .filter { it.shortName.asString() == SingletonAnno::class.simpleName }
            .toList()
        if (singletonClass.isNotEmpty()) {
            singletonClass.stream().map {
                it.arguments.first().value as String
            }.forEach {
                when (it) {
                    SingletonAnno.TYPE_DOUBLE_CHECK -> doubleCheckPattern(classDeclaration)
                    SingletonAnno.TYPE_EAGER -> eagerPattern(classDeclaration)
                    else -> throw Exception("Unknown singleton type: $it")
                }
            }
        }
    }

    private fun eagerPattern(classDeclaration: KSClassDeclaration) {
        val className = classDeclaration.simpleName.asString()
        val packageName = classDeclaration.packageName.asString()
        val constructorParams =
            classDeclaration.primaryConstructor?.parameters?.joinToString(separator = ", ") {
                "${it.name?.asString()}: ${it.type.resolve().declaration.qualifiedName?.asString()}"
            } ?: ""
        // Code to be written to the file
        val singletonCode = if (constructorParams.isEmpty()) {
            """
                   package $packageName
               
                   object $className {
                      
                   }
            """.trimIndent()
        } else {
            throw Exception("Eager pattern does not support constructor parameters...")
        }
        val outputStream = FileOutputStream(File(classDeclaration.containingFile!!.filePath))
        outputStream.write(singletonCode.toByteArray())
        outputStream.close()
    }

    private fun doubleCheckPattern(classDeclaration: KSClassDeclaration) {
        // Code to generate the singleton pattern based on BaseSingleton
        // You will need to use the KSP API to generate this code
        // and add it to the source files.
        val className = classDeclaration.simpleName.asString()
        val packageName = classDeclaration.packageName.asString()
        val constructorParams =
            classDeclaration.primaryConstructor?.parameters?.joinToString(separator = ", ") {
                "${it.name?.asString()}: ${it.type.resolve().declaration.qualifiedName?.asString()}"
            } ?: ""
        val newInstanceParams =
            classDeclaration.primaryConstructor?.parameters?.joinToString(separator = ", ") {
                it.name?.asString() ?: ""
            } ?: ""
        val baseSingletonPackageName = BaseSingleton::class.qualifiedName!!
        // Code to be written to the file
        val singletonCode = """
                   package $packageName
                   
                   import $baseSingletonPackageName
               
                   class $className($constructorParams) {
                       companion object : BaseSingleton<$className>() {
                            fun getInstance($constructorParams): $className = getInstance { $className($newInstanceParams) }
                       }
                   }
                  """.trimIndent()

        val outputStream = FileOutputStream(File(classDeclaration.containingFile!!.filePath))
        outputStream.write(singletonCode.toByteArray())
        outputStream.close()
    }
}