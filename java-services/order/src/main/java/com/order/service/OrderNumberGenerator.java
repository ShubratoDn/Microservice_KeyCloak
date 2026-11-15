package com.order.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class OrderNumberGenerator {

    private static final int SUFFIX_LENGTH = 8;

    public String next() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, SUFFIX_LENGTH)
                .toUpperCase();
        return "ORD-%s-%s".formatted(date, suffix);
    }
}
