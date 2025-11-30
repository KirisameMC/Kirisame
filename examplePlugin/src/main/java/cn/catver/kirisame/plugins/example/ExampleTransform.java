package cn.catver.kirisame.plugins.example;

import net.bytebuddy.agent.builder.AgentBuilder;
import org.kirisame.mc.api.plugin.KirisameTransform;

import java.lang.instrument.Instrumentation;

public class ExampleTransform implements KirisameTransform {
    @Override
    public AgentBuilder apply(Instrumentation inst, AgentBuilder builder) {
        return TpsOnGuiImpl.transformMinecraftServerGui(builder);
    }
}
