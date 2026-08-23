package ru.itone.illya4gurenko.publisher_change_food_card.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ValidDataService {
    private static final Pattern FILE_TITLE_PATTERN =
            Pattern.compile("^Z\\d{6}\\.[A-Z0-9]+_ENROLL\\d{3}[A-Z0-9]\\.\\d{3}$");

    private static final Pattern HEADER_PATTERN =
            Pattern.compile("^H \\d{8} \\d{6} (IMMEDIATE|IN-TIME  )(?:\\d{8}| {8}) (?:\\d{6}| {6})$");

    private static final Pattern BODY_PATTERN =
            Pattern.compile("^.{100}.{30}(DR|CR|ZR).{20}$");

    private static final Pattern FOOTER_PATTERN =
            Pattern.compile("^T {9}[ \\d]{10}$");

    private final ValidDataService validDataService;

    public boolean isFileEnrollFilter(Path path) {
        String nameFile = path.getFileName().toString();
        if (nameFile.endsWith(".in_progress") || nameFile.endsWith(".success") || nameFile.endsWith(".error")) {
            return false;
        }
        return nameFile != null && FILE_TITLE_PATTERN.matcher(nameFile).matches();
    }

    public boolean isValidHeader(String unit) {
        return unit != null && HEADER_PATTERN.matcher(unit).matches();
    }

    public boolean isValidFooter(String unit, Path path) {
        return unit != null && FOOTER_PATTERN.matcher(unit).matches() &&
                unit.replaceAll("\\D", "").equals(String.valueOf(count(path)));
    }

    public boolean isValidBody(String unit) {
        return unit != null && BODY_PATTERN.matcher(unit).matches();
    }

    public long count(Path path){
        try (Stream<String> lines = Files.lines(path)) {
            long count = lines
                    .count();
            return count-2;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
