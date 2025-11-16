package org.kirisame.mc.api.command;

import java.util.List;

public record KirisameCommandLabel(String label, String... aliases) {
    public List<String> lookup(){
        List<String> list = new java.util.ArrayList<>();
        list.add(label);
        list.addAll(List.of(aliases));
        return list;
    }
}
