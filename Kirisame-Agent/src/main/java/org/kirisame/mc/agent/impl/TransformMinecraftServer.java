package org.kirisame.mc.agent.impl;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.JavaModule;
import org.kirisame.mc.agent.Transform;
import org.kirisame.mc.api.agent.AgentMessageBus;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.function.BooleanSupplier;

public class TransformMinecraftServer extends Transform {
    @Override
    public AgentBuilder apply(Instrumentation inst, AgentBuilder builder) {
        return builder.type(ElementMatchers.named("net.minecraft.server.MinecraftServer"))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
                        return builder.visit(new AsmVisitorWrapper.ForDeclaredMethods()
                                .method(
                                        ElementMatchers.named("tickServer").and(
                                                ElementMatchers.isProtected()
                                        ).and(ElementMatchers.takesArgument(
                                                0, BooleanSupplier.class
                                        )),
                                        new AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper() {
                                            @Override
                                            public MethodVisitor wrap(TypeDescription instrumentedType, MethodDescription instrumentedMethod, MethodVisitor methodVisitor, Implementation.Context implementationContext, TypePool typePool, int writerFlags, int readerFlags) {
                                                return new MethodVisitor(Opcodes.ASM9, methodVisitor) {
                                                    @Override
                                                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                                                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                                                        if (opcode == Opcodes.INVOKEVIRTUAL &&
                                                            owner.equals("net/minecraft/server/MinecraftServer") &&
                                                            name.equals("tickChildren") &&
                                                            descriptor.equals("(Ljava/util/function/BooleanSupplier;)V")){

                                                            super.visitVarInsn(Opcodes.ALOAD,0);
                                                            super.visitMethodInsn(
                                                                    Opcodes.INVOKESTATIC,
                                                                    "org/kirisame/mc/agent/impl/TransformMinecraftServer",
                                                                    "afterTickChildren",
                                                                    "(Ljava/lang/Object;)V",
                                                                    false
                                                            );

                                                        }
                                                    }
                                                };
                                            }
                                        }
                                )
                        );
                    }
                });
    }

    public static void afterTickChildren(Object minecraftServer){
        AgentMessageBus.post("Tick",minecraftServer);
    }
}
