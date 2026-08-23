package ru.itone.illya4gurenko.publisher_change_food_card.service;

import java.nio.file.Path;

public class FileUnitsCheckService implements FileUnitsCheckVisitor{
    private final ValidDataService validDataService;

    public FileUnitsCheckService(ValidDataService validDataService) {
        this.validDataService = validDataService;
    }

    @Override
    public void visit(Path path) {
        //заинджектить проверку и стримом кажду строку под проверочку
    }
}
