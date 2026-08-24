package ru.itone.illya4gurenko.publisher_change_food_card.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class ValidDataService {
    private static final Pattern FILE_TITLE_PATTERN =
            Pattern.compile("^Z\\d{6}\\.[A-Za-z0-9]+_ENROLL\\d+\\.\\d{3}$");

    private static final Pattern HEADER_PATTERN =
            Pattern.compile("^H \\d{8} \\d{6} (IMMEDIATE|INTIME   (?: \\d{8} \\d{6})?)$");

    private static final Pattern BODY_PATTERN =
            Pattern.compile("^.{100}.{30}(DR|CR|ZR) {0,19}\\d+(\\.\\d{1,2})?$");

    private static final Pattern FOOTER_PATTERN =
            Pattern.compile("^T {9} *\\d+$");

    public boolean isFileEnrollFilter(Path path) {
        String nameFile = path.getFileName().toString();
        if(!FILE_TITLE_PATTERN.matcher(nameFile).matches())
            System.out.println("nonononononoononononon");
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
