package ru.itone.illya4gurenko.publisher_change_food_card;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ContextLoadsTest extends BaseIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("up context")
    void contextLoads() {
        assertNotNull(applicationContext, "Spring ApplicationContext success init");
    }
}
