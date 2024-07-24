package org.dgf.core;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtility {

    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

    public static LocalDateTime parse(String datetime) {
        return LocalDateTime.parse(datetime, dateTimeFormatter);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTimeFormatter.format(dateTime);
    }
}
