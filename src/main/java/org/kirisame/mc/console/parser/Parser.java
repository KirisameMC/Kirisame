package org.kirisame.mc.console.parser;

import org.kirisame.mc.console.message.Message;

import java.util.regex.Pattern;

public abstract class Parser<T extends Message> {
    public abstract String getRule();
    public abstract T parse(String input);

    public Pattern getPattern(){
        return Pattern.compile(getRule());
    }
    public boolean match(String input){
        return input.matches(getRule());
    }
}
