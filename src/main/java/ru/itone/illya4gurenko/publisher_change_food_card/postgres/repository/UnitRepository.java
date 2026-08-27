package ru.itone.illya4gurenko.publisher_change_food_card.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itone.illya4gurenko.publisher_change_food_card.postgres.entity.Unit;
@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
}

