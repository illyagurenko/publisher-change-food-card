package ru.itone.illya4gurenko.publisher_change_food_card.service;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.regex.Pattern;

@Service
public class ValidDataService {
    private static final Pattern FILE_TITLE_PATTERN =
            Pattern.compile("^Z\\d{6}\\.[A-Z0-9]+_ENROLL\\d{3}[A-Z0-9]\\.\\d{3}$");

    private static final Pattern HEADER_PATTERN =
            Pattern.compile("^H \\d{8} \\d{6} (IMMEDIATE|IN-TIME  )\\d{8} \\d{6}$");

    private static final Pattern BODY_PATTERN =
            Pattern.compile("^.{100}\\d{16} {14}(DR|CR|ZR).{20}$");

    private static final Pattern FOOTER_PATTERN =
            Pattern.compile("^T {9}\\d$");

    public boolean isFileEnrollFilter(Path path) {
        String nameFile = path.getFileName().toString();
        return nameFile != null && FILE_TITLE_PATTERN.matcher(nameFile).matches();
    }

    public boolean isValidHeader(String unit) {
        return unit != null && HEADER_PATTERN.matcher(unit).matches();
    }

    public boolean isValidFooter(String unit) {
        return unit != null && FOOTER_PATTERN.matcher(unit).matches();
    }

    public boolean isValidBody(String unit) {
        return unit != null && BODY_PATTERN.matcher(unit).matches();
    }
}
