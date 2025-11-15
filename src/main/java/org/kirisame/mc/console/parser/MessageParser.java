package org.kirisame.mc.console.parser;

import org.kirisame.mc.console.message.ConsoleMessage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageParser {
    Pattern pattern = Pattern.compile("\\[(\\d{2}:\\d{2}:\\d{2})\\] \\[([^\\]/]+)/([^\\]]+)\\]: (.*)");

    public ConsoleMessage parse(String input) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()){
            String date = matcher.group(1);
            String thread = matcher.group(2);
            String level = matcher.group(3);
            String message = matcher.group(4);

            LocalTime localTime = LocalTime.parse(date, DateTimeFormatter.ofPattern("HH:mm:ss"));
            ZonedDateTime zdt = localTime.atDate(LocalDate.now()).atZone(ZoneId.systemDefault());
            return new ConsoleMessage(zdt.toInstant().toEpochMilli(), thread, level, message);
        }
        return null;
    }
}
