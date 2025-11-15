package org.kirisame.mc.event;

import lombok.Getter;
import lombok.Setter;

public class CancelableEvent extends Event{
    @Getter @Setter
    boolean cancel = false;
}
