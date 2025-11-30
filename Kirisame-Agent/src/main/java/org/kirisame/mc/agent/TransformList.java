package org.kirisame.mc.agent;

import org.kirisame.mc.agent.impl.TransformPlayerList;
import org.kirisame.mc.agent.impl.TransformServerGamePacketListenerImpl;

import java.util.List;

public class TransformList {
    public final static List<Transform> transformers = List.of(
            new TransformPlayerList(),
            new TransformServerGamePacketListenerImpl()
    );
}
