package ru.itone.illya4gurenko.publisher_change_food_card.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.File;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.FileStatus;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.Unit;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.UnitError;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.repository.FileRepository;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.repository.UnitErrorRepository;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.repository.UnitRepository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PomDao {
    private final FileRepository fileRepository;
    private final UnitRepository unitRepository;
    private final UnitErrorRepository unitErrorRepository;

    // POM.FILE
    // сохранение названия файла в бд и обновление статуса файла
    public File saveFile(String name, String path, String sender, String ulianDate){
        File file = new File();
        file.setFilename(name);
        file.setFullpath(path);
        file.setSender(sender);
        file.setInsTime(OffsetDateTime.now());
        file.setFileStatus(FileStatus.IN_PROCESS);
        file.setUliDate(ulianDate);
        file.setFileComment(null);

        return fileRepository.save(file);
    }
    public void updateFileStatus(File file, FileStatus status, String comment) {
        file.setFileStatus(status);
        file.setFileComment(comment);
        file.setUpdTime(OffsetDateTime.now());
        fileRepository.save(file);
    }

    // POM.UNIT
    // сохранение строки в бд
    public Unit saveUnit(File file, String pomType, FileStatus status, String line, String addValue){
        Unit unit = new Unit();
        unit.setFile(file);
        unit.setInsTime(LocalDateTime.now());
        unit.setPomType(pomType);
        unit.setStatus(status);
        unit.setUnitValue(line);
        unit.setAddValue(addValue);
        return unitRepository.save(unit);
    }

    // POM.UNIT_ERROR
    // сохранение ошибки строки в бд
    public void saveUnitError(File file, Unit unit, String rawLine, List<ValidationError> errors) {
        if (errors == null || errors.isEmpty()) {
            return;
        }

        int seq = 1;
        for (ValidationError err : errors) {
            UnitError unitError = new UnitError();
            unitError.setFile(file);
            unitError.setUnit(unit);
            unitError.setErrorSeq(seq++);
            unitError.setErrorCode(err.code());
            unitError.setErrorField(rawLine);
            unitError.setErrorMsg(err.message());
            unitErrorRepository.save(unitError);
        }
    }
}
