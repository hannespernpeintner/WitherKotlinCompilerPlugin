package de.hanno.kotlin.compiler.plugins

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.WARNING
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.FunctionCodegen
import org.jetbrains.kotlin.codegen.ImplementationBodyCodegen
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class DataClassWitherGenerationExtension(val messageCollector: MessageCollector) : ExpressionCodegenExtension {

    private fun getAnnotations() = listOf("de.hanno.kotlin.compiler.plugins.annotations.Wither")

    override fun generateClassSyntheticParts(codegen: ImplementationBodyCodegen) {
        messageCollector.report(
                WARNING,
                "*** generateClassSyntheticParts ***",
                CompilerMessageLocation.create(""))

        val classDescriptor = codegen.descriptor
        if(!classDescriptor.isData) return


        val hasWitherAnnotation = getAnnotations().any { classDescriptor.annotations.hasAnnotation(FqName(it)) }

        messageCollector.report(
                WARNING,
                "*** class ${classDescriptor.name} is annotated with wither: $hasWitherAnnotation ***",
                CompilerMessageLocation.create(""))

        val constructor = classDescriptor.constructors.first { it.isPrimary }
        val properties: List<PropertyDescriptor> = constructor.valueParameters
                .mapNotNull { codegen.bindingContext.get(BindingContext.VALUE_PARAMETER_AS_PROPERTY, it) }


        properties.forEach  { property ->
            val witherFunctionName = "with" + property.name.toString().substring(1 until property.name.toString().length) + property.name.toString()[0].toUpperCase()

            messageCollector.report(
                    WARNING,
                    "*** Generating witherFunction $witherFunctionName for property $property ***",
                    CompilerMessageLocation.create(""))

            val origin = JvmDeclarationOrigin.NO_ORIGIN//(JvmDeclarationOriginKind.OTHER, null, classDescriptor)
            val propertyType = codegen.state.typeMapper.mapType(property.type)
            val classType = codegen.state.typeMapper.mapType(classDescriptor.defaultType)
            val mv = codegen.v.newMethod(origin, Opcodes.ACC_PUBLIC, witherFunctionName, "($propertyType)$classType", null, emptyArray())

            val instructionAdapter = InstructionAdapter(mv)

            val constructorDescription = classDescriptor.unsubstitutedPrimaryConstructor ?: throw IllegalStateException("No primary constructor found!")

            instructionAdapter.anew(classType)
            instructionAdapter.dup()
            for(parameterDescriptor in constructorDescription.valueParameters) {
                val type = codegen.state.typeMapper.mapType(parameterDescriptor.type)
                val isPassedParameter = parameterDescriptor.name.identifier == property.name.identifier
                if(isPassedParameter) {
                    instructionAdapter.load(1, propertyType)
                } else {
                    instructionAdapter.load(0, classType)
                    instructionAdapter.getfield(classType.internalName, parameterDescriptor.name.identifier, type.descriptor)
                }
            }

            val constructorAsmMethod = codegen.state.typeMapper.mapAsmMethod(constructor)
            instructionAdapter.invokespecial(classType.internalName, "<init>", constructorAsmMethod.descriptor, false)
            instructionAdapter.areturn(classType)

            mv.visitCode()

            FunctionCodegen.endVisit(mv, witherFunctionName, codegen.myClass)
        }
    }
}
