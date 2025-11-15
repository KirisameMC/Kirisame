package org.kirisame.mc.api.command;

public record KirisameCommandLabel(String label,String... aliases) {
    public String lookup(){
        return label+","+String.join(",", aliases)+",";
    }
}
