package ru.itone.illya4gurenko.publisher_change_food_card.service;

import java.io.IOException;
import java.nio.file.Path;

public interface FileUnitsCheckVisitor {
    boolean visit(Path path) throws IOException;
}
