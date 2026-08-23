package ru.itone.illya4gurenko.publisher_change_food_card.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileUnitsCheckVisitorService implements FileUnitsCheckVisitor {
    private final ValidDataService validDataService;

    public FileUnitsCheckVisitorService(ValidDataService validDataService) {
        this.validDataService = validDataService;
    }

    @Override
    public boolean visit(Path path) throws IOException {
        try {
            String firstLine;
            try (Stream<String> lines = Files.lines(path)) {
                firstLine = lines.findFirst().orElse(null);
            }

            String lastLine;
            try (Stream<String> lines = Files.lines(path)) {
                lastLine = lines.reduce((first, second) -> second).orElse(null);
            }
            long count;
            try (Stream<String> lines = Files.lines(path)) {
                count = lines
                        .filter(validDataService::isValidBody)
                        .count();
            }
            return validDataService.isValidFooter(lastLine, path) &&
                    validDataService.isValidHeader(firstLine) &&
                    validDataService.count(path) == count;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}