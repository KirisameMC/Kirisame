package cn.catver.kirisame.plugins.example;

import lombok.Getter;
import lombok.Setter;
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
import org.kirisame.mc.event.EventBus;
import org.kirisame.mc.event.EventHandler;
import org.kirisame.mc.event.impl.reflect.AgentMessageEvent;
import org.kirisame.mc.event.impl.reflect.TickEvent;

import javax.swing.*;
import java.security.ProtectionDomain;

public class TpsOnGuiImpl {
    @Setter @Getter
    private static JFrame jFrame;

    public static AgentBuilder transformMinecraftServerGui(AgentBuilder builder) {
        EventBus.register(TpsOnGuiImpl.class);

        return builder.type(ElementMatchers.named("net.minecraft.server.gui.MinecraftServerGui"))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
                        return builder.visit(new AsmVisitorWrapper.ForDeclaredMethods().method(
                                ElementMatchers.named("showFrameFor").and(
                                        ElementMatchers.isStatic()
                                ),
                                new AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper() {
                                    @Override
                                    public MethodVisitor wrap(TypeDescription instrumentedType, MethodDescription instrumentedMethod, MethodVisitor methodVisitor, Implementation.Context implementationContext, TypePool typePool, int writerFlags, int readerFlags) {
                                        return new MethodVisitor(Opcodes.ASM9, methodVisitor) {
                                            boolean foundAStoreJFrame = false;
                                            boolean shutdown = false;

                                            @Override
                                            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                                                // 捕获 invokespecial JFrame.<init>()
                                                if (!shutdown && !foundAStoreJFrame
                                                        && opcode == Opcodes.INVOKESPECIAL
                                                        && owner.equals("javax/swing/JFrame")
                                                        && name.equals("<init>")) {
                                                    foundAStoreJFrame = true; // 下一条应该是 astore frame
                                                }
                                                super.visitMethodInsn(opcode, owner, name, desc, itf);
                                            }

                                            @Override
                                            public void visitVarInsn(int opcode, int varIndex) {
                                                if (!shutdown && foundAStoreJFrame && opcode == Opcodes.ASTORE && varIndex != -1){
                                                    shutdown = true;
                                                    super.visitVarInsn(opcode, varIndex);

                                                    super.visitTypeInsn(Opcodes.NEW, "org/kirisame/mc/event/impl/reflect/AgentMessageEvent");
                                                    super.visitInsn(Opcodes.DUP);

                                                    super.visitLdcInsn("E-GetJFrame");
                                                    super.visitVarInsn(Opcodes.ALOAD, 1);

                                                    super.visitMethodInsn(Opcodes.INVOKESPECIAL,
                                                            "org/kirisame/mc/event/impl/reflect/AgentMessageEvent",
                                                            "<init>",
                                                            "(Ljava/lang/String;Ljava/lang/Object;)V",
                                                            false);

                                                    super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                                            "org/kirisame/mc/event/EventBus",
                                                            "post",
                                                            "(Lorg/kirisame/mc/event/Event;)Lorg/kirisame/mc/event/Event;",
                                                            false);

                                                    super.visitInsn(Opcodes.POP);

                                                    return;
                                                }

                                                super.visitVarInsn(opcode, varIndex);
                                            }
                                        };
                                    }
                                }
                        ));
                    }
                });
    }

    @EventHandler
    public static void getJFrame(AgentMessageEvent event){
        if ("E-GetJFrame".equals(event.getLabel())){
            jFrame = (JFrame) event.getMessage();
        }
    }
}
