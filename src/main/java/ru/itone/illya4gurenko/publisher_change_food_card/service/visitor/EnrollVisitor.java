package ru.itone.illya4gurenko.publisher_change_food_card.service.visitor;

import lombok.Getter;
import ru.itone.illya4gurenko.publisher_change_food_card.config.ConstantsUtils;
import ru.itone.illya4gurenko.publisher_change_food_card.dao.GruDao;
import ru.itone.illya4gurenko.publisher_change_food_card.dao.PomDao;
import ru.itone.illya4gurenko.publisher_change_food_card.dao.ValidationError;
import ru.itone.illya4gurenko.publisher_change_food_card.exception.FileValidationException;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.File;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.FileStatus;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.Unit;
import ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.dto.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnrollVisitor implements Visitor {
    // коды POM_TYPE
    private static final String POM_TYPE_HEADER = "101";
    private static final String POM_TYPE_BODY = "106";
    private static final String POM_TYPE_TRAILER = "108";
    // форматы дат и времени
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmmss");
    // паттерны шапки и подвала
    private static final Pattern TITLE_FILE_PATTERN = Pattern.compile("^Z(\\d{3})(\\d{3})\\.[A-Za-z0-9_]+_ENROLL\\d+\\.(\\d{3})$");
    private static final Pattern HEADER_IMMEDIATE = Pattern.compile("^H\\s(\\d{8})\\s(\\d{6})\\s(IMMEDIATE)\\s*$");
    private static final Pattern HEADER_INTIME = Pattern.compile("^H\\s(\\d{8})\\s(\\d{6})\\s(INTIME)\\s(\\d{8})\\s(\\d{6})$");
    private static final Pattern TRAILER_PATTERN = Pattern.compile("^T\\s+(\\d+)\\s*$");
    // DAO
    private final PomDao pomDao;
    private final GruDao gruDao;
    // данные файла
    @Getter
    private File fileEntity;
    private final String lastRow;
    private final String fullPathDir;
    private final String filename;
    // флаги валидности
    private boolean isSaveFile = false;
    private boolean isValidFilename = false;
    private boolean isValidHeader = false;
    private boolean isValidTrailer = false;
    private boolean hasAnyError = false;
    // первая и последняя строка
    private Header header;
    private Trailer trailer;
    // количество строк
    private int countRows = 0;



    public EnrollVisitor(PomDao pomDao, GruDao gruDao, String lastRow, String fullPathDir, String filename) {
        this.pomDao = pomDao;
        this.gruDao = gruDao;
        this.lastRow = lastRow;
        this.fullPathDir = fullPathDir;
        this.filename = filename;
    }

    @Override
    public void visit(Object o) {
        if (!(o instanceof String)) {
            throw new IllegalArgumentException("visitor wait String");
        }
        String str = (String) o;
        if (!isSaveFile) {
            saveFile();
        }
        if (header == null) {
            processHeader(str);
            return;
        }
        if (str.equals(lastRow)) {
            processTrailer(str);
            if (hasAnyError || !isValidHeader || !isValidTrailer || !isValidFilename) {
                throw new FileValidationException("File contains validation errors");
            }
            return;
        }
        processBody(str);

    }

    // save File in db
    private void saveFile() {
        String sender = null;
        String julianDate = null;

        Matcher titleMatcher = TITLE_FILE_PATTERN.matcher(filename);
        if (titleMatcher.matches()) {
            isValidFilename = true;
            sender = titleMatcher.group(1) + " " + titleMatcher.group(2);
            julianDate = titleMatcher.group(3);
        } else {
            isValidFilename = false;
            hasAnyError  = true;
        }

        fileEntity = pomDao.saveFile(filename, fullPathDir, sender, julianDate);

        // чтоб зря боди не парсить
        Trailer preCheckTrailer = parseTrailer(lastRow);
        if (preCheckTrailer == null) {
            isValidTrailer = false;
            hasAnyError  = true;
        } else {
            isValidTrailer = true;
        }
        isSaveFile = true;
    }

    // Header
    private void processHeader(String line) {
        header = parseHeader(line);

        if (header != null) {
            isValidHeader = true;
            pomDao.saveUnit(fileEntity, POM_TYPE_HEADER, FileStatus.SUCCESS, line, null);
        } else {
            isValidHeader = false;
            hasAnyError  = true;
            Unit unit = pomDao.saveUnit(fileEntity, POM_TYPE_HEADER, FileStatus.ERROR, line, ConstantsUtils.MSG_INVALID_HEADER);
            pomDao.saveUnitError(fileEntity, unit, line, List.of(new ValidationError(ConstantsUtils.ERR_CODE_HEADER, ConstantsUtils.MSG_INVALID_HEADER)));
        }
    }

    private Header parseHeader(String line) {
        Matcher immediateMatcher = HEADER_IMMEDIATE.matcher(line);
        if (immediateMatcher.matches()) {
            return new Header(line, ProcType.IMMEDIATE, null);
        }
        Matcher intimeMatcher = HEADER_INTIME.matcher(line);
        if (intimeMatcher.matches()) {
            LocalDate date = LocalDate.parse(intimeMatcher.group(4), DATE_FORMAT);
            LocalTime time = LocalTime.parse(intimeMatcher.group(5), TIME_FORMAT);
            return new Header(line, ProcType.INTIME, LocalDateTime.of(date, time));
        }
        return null;
    }

    // Trailer
    private void processTrailer(String line) {
        trailer = parseTrailer(line);
        boolean isCountEquals = trailer != null && trailer.getDeclaredCount() == countRows;

        if (isCountEquals) {
            isValidTrailer = true;
            pomDao.saveUnit(fileEntity, POM_TYPE_TRAILER, FileStatus.SUCCESS, line, null);
        } else {
            isValidTrailer = false;
            hasAnyError = true;
            String errorMsg = !isCountEquals
                    ? ConstantsUtils.MSG_INVALID_COUNT_ROWS
                    : ConstantsUtils.MSG_INVALID_TRAILER;
            Unit unit = pomDao.saveUnit(fileEntity, POM_TYPE_TRAILER, FileStatus.ERROR, line, errorMsg);
            pomDao.saveUnitError(fileEntity, unit, line, List.of(new ValidationError(ConstantsUtils.ERR_CODE_TRAILER, errorMsg)));
        }
    }

    private Trailer parseTrailer(String line) {
        if (line == null) return null;
        Matcher matcher = TRAILER_PATTERN.matcher(line.trim());
        if (matcher.matches()) {
            try {
                int count = Integer.parseInt(matcher.group(1).trim());
                return new Trailer(line, count);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // Body
    private void processBody(String line) {
        countRows++;

        if (!isValidFilename || !isValidHeader || !isValidTrailer) {
            String errorMsg = !isValidFilename ? ConstantsUtils.MSG_INVALID_FILENAME: ConstantsUtils.MSG_AUTO_INVALID;
            Unit unit = pomDao.saveUnit(fileEntity, POM_TYPE_BODY, FileStatus.ERROR, line, errorMsg);
            pomDao.saveUnitError(fileEntity, unit, line, List.of(new ValidationError(ConstantsUtils.ERR_CODE_AUTO_INVALID, errorMsg)));
            return;
        }

        List<ValidationError> errors = new ArrayList<>();
        Body body = parseBody(line, errors);

        if (!errors.isEmpty() || body == null) {
            hasAnyError = true;
            String firstErrorMsg = errors.isEmpty() ? ConstantsUtils.MSG_BODY_INVALID : errors.getFirst().message();
            Unit unit = pomDao.saveUnit(fileEntity, POM_TYPE_BODY, FileStatus.ERROR, line, firstErrorMsg);
            pomDao.saveUnitError(fileEntity, unit, line, errors);
        } else {
            Unit unit = pomDao.saveUnit(fileEntity, POM_TYPE_BODY, FileStatus.SUCCESS, line, null);
            gruDao.save(
                    body.getAccount(),
                    body.getAmount(),
                    body.getOperationType(),
                    unit.getId(),
                    fileEntity.getId(),
                    header.getProcType(),
                    header.getFocTimestamp()
            );
        }
    }

    private Body parseBody(String line, List<ValidationError> errors) {
        if (line == null || line.length() != 152) {
            errors.add(new ValidationError(ConstantsUtils.ERR_CODE_BODY_LEN, "length != 152 chars"));
            return null;
        }

        String fullName = line.substring(0, 100).trim();
        String account = line.substring(100, 130).trim();
        String typeStr = line.substring(130, 132).trim();
        String amountStr = line.substring(132, 152).trim();

        if (fullName.isEmpty()) {
            errors.add(new ValidationError(ConstantsUtils.ERR_CODE_EMPTY_NAME, "full name mustn`t be empty"));
        }

        if (account.isEmpty()) {
            errors.add(new ValidationError(ConstantsUtils.ERR_CODE_EMPTY_ACCOUNT, "account mustn`t be empty"));
        }

        Type operationType = null;
        try {
            operationType = Type.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            errors.add(new ValidationError(ConstantsUtils.ERR_CODE_INVALID_TYPE, "uncorrected type: '" + typeStr));
        }

        BigDecimal amount = null;
        try {
            amount = new BigDecimal(amountStr.replace(',', '.'));
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                errors.add(new ValidationError(ConstantsUtils.ERR_CODE_NEGATIVE_AMOUNT, "amount mustn`t <0: " + amountStr));
            }
        } catch (Exception e) {
            errors.add(new ValidationError(ConstantsUtils.ERR_CODE_INVALID_AMOUNT, "uncorrected amount: '" + amountStr));
        }

        if (!errors.isEmpty()) {
            return null;
        }

        return new Body(line, fullName, account, operationType, amount);
    }
}
