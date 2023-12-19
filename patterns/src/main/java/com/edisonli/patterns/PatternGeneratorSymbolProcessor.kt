package com.edisonli.patterns

import com.edisonli.patterns.anno.BuilderAnno
import com.edisonli.patterns.anno.SingletonAnno
import com.edisonli.patterns.visitor.BuilderVisitor
import com.edisonli.patterns.visitor.SingletonVisitor
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate

class PatternGeneratorSymbolProcessor(private val environment: SymbolProcessorEnvironment) :
    SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        environment.logger.info("PatternGenerator is starting...")
        val ret = mutableListOf<KSAnnotated>()
        handleSingleton(resolver, ret)
        return ret
    }

    private fun handleSingleton(resolver: Resolver, ret: MutableList<KSAnnotated>) {
        // singleton
        val singletonSymbols =
            resolver.getSymbolsWithAnnotation(SingletonAnno::class.qualifiedName ?: "")
        ret.addAll(singletonSymbols.filter { !it.validate() })
        handleVisitor(singletonSymbols, SingletonVisitor())
        // builder
        val builderSymbols =
            resolver.getSymbolsWithAnnotation(BuilderAnno::class.qualifiedName ?: "")
        ret.addAll(builderSymbols.filter { !it.validate() })
        handleVisitor(builderSymbols, BuilderVisitor(environment))
    }

    private fun <T : KSVisitorVoid> handleVisitor(symbols: Sequence<KSAnnotated>, t: T) {
        symbols.filter { it.validate() }.forEach { it.accept(t, Unit) }
    }
}