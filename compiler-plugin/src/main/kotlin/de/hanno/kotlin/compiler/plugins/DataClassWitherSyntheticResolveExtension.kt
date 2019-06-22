package de.hanno.kotlin.compiler.plugins

import de.hanno.kotlin.compiler.plugins.DataClassWitherGenerationExtension.Companion.annotations
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

class DataClassWitherSyntheticResolveExtension(val messageCollector: MessageCollector): SyntheticResolveExtension {

    val compilerMessageLocation = CompilerMessageLocation.create("")
    private fun MessageCollector.warn(msg: String) = report(CompilerMessageSeverity.WARNING, msg, compilerMessageLocation)


    override fun generateSyntheticMethods(thisDescriptor: ClassDescriptor,
                                          name: Name,
                                          bindingContext: BindingContext,
                                          fromSupertypes: List<SimpleFunctionDescriptor>,
                                          result: MutableCollection<SimpleFunctionDescriptor>) {

        messageCollector.warn("*** XXXXXXXX ***")

        val hasWitherAnnotation = annotations.any { thisDescriptor.annotations.hasAnnotation(FqName(it)) }

        if(!thisDescriptor.isData || !hasWitherAnnotation) return

        val primaryConstructor = thisDescriptor.constructors.first { it.isPrimary }
        val properties = primaryConstructor.valueParameters

        val descriptors = properties.map { property ->
            val witherFunctionName = "with" + property.name.toString().substring(1 until property.name.toString().length) + property.name.toString()[0].toUpperCase()
            val functionDescriptor = SimpleFunctionDescriptorImpl.create(
                    thisDescriptor, Annotations.EMPTY, Name.identifier(witherFunctionName), CallableMemberDescriptor.Kind.SYNTHESIZED, thisDescriptor.source
            )
            functionDescriptor.initialize(
                    null,
                    thisDescriptor.thisAsReceiverParameter,
                    emptyList(),
                    listOf(property.copy(functionDescriptor, name, 0)),
                    thisDescriptor.defaultType,
                    Modality.FINAL,
                    property.visibility
            )
        }

        result.addAll(descriptors)

        messageCollector.report(
                CompilerMessageSeverity.WARNING,
                "*** Generated ${descriptors.size} descriptors ***",
                CompilerMessageLocation.create(""))

//        super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
    }


//    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> {
//
//        val hasWitherAnnotation = getAnnotations().any { thisDescriptor.annotations.hasAnnotation(FqName(it)) }
//
//        if(!thisDescriptor.isData || !hasWitherAnnotation) return emptyList()
//
//
//        val primaryConstructor = thisDescriptor.constructors.first { it.isPrimary }
//        val properties = primaryConstructor.valueParameters
//
//        return properties.map { property ->
//            val witherFunctionName = "with" + property.name.toString().substring(1 until property.name.toString().length) + property.name.toString()[0].toUpperCase()
//            Name.identifier(witherFunctionName)
//        }
//    }
}