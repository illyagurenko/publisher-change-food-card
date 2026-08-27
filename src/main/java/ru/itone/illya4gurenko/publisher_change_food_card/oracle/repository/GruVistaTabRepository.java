package ru.itone.illya4gurenko.publisher_change_food_card.oracle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itone.illya4gurenko.publisher_change_food_card.oracle.entity.GruVistaTab;
@Repository
public interface GruVistaTabRepository extends JpaRepository<GruVistaTab, Long> {

}
