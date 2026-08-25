package ru.itone.illya4gurenko.publisher_change_food_card.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public class FileUnitsCheckVisitorService implements FileUnitsCheckVisitor {
    private final ValidDataService validDataService;

    public FileUnitsCheckVisitorService(ValidDataService validDataService) {
        this.validDataService = validDataService;
    }

    @Override
    public boolean visit(Path path) throws IOException {
        String firstLine;
        String lastLine;
        long validBodyCount;

        try {
            try (Stream<String> lines = Files.lines(path)) {
                firstLine = lines.findFirst().orElse(null);
            }
            if (firstLine == null) return false;

            lastLine = readLastLine(path);
            if (lastLine == null) return false;

            try (Stream<String> lines = Files.lines(path)) {
                validBodyCount = lines.filter(validDataService::isValidBody).count();
            }

        } catch (MalformedInputException | UncheckedIOException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        return validDataService.isValidHeader(firstLine) &&
                validDataService.isValidFooter(lastLine, path) &&
                validDataService.count(path) == validBodyCount;
    }

    private String readLastLine(Path path) throws IOException {
        java.io.File file = path.toFile();
        if (!file.exists() || file.length() == 0) {
            return null;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long length = raf.length();
            long pos = length - 1;


            while (pos >= 0) {
                raf.seek(pos);
                byte b = raf.readByte();
                if (b != '\n' && b != '\r') {
                    break;
                }
                pos--;
            }

            if (pos < 0) {
                return "";
            }

            long endPos = pos;

            while (pos >= 0) {
                raf.seek(pos);
                byte b = raf.readByte();
                if (b == '\n' || b == '\r') {
                    pos++;
                    break;
                }
                pos--;
            }

            long startPos = Math.max(0, pos);
            int lineLength = (int) (endPos - startPos + 1);

            byte[] bytes = new byte[lineLength];
            raf.seek(startPos);
            raf.readFully(bytes);

            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}