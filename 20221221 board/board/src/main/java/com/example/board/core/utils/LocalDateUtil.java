package com.example.board.core.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class LocalDateUtil {

    public static LocalDateTime strToLocalDateTime(String date) {
        if (date == null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date tempDate = new Date();
            date = simpleDateFormat.format(tempDate);
        } else {
            // yyyy-MM-dd HH:mm:ss.SSS -> [yyyy-MM-dd HH:mm:ss, SSS]
            date = date.split("\\.")[0];
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return LocalDateTime.parse(date, formatter);
    }
}
