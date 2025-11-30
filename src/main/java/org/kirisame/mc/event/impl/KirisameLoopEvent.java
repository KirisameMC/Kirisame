package org.kirisame.mc.event.impl;

import lombok.Getter;
import org.kirisame.mc.KirisameMC;
import org.kirisame.mc.event.Event;

public class KirisameLoopEvent extends Event {
    @Getter
    final static KirisameLoopEvent instance = new KirisameLoopEvent(KirisameMC.getInstance());

    KirisameLoopEvent(KirisameMC kirisameMC) {
        this.kirisameMC = kirisameMC;
    }

    @Getter
    final KirisameMC kirisameMC;
}
