package com.assurance.Repository;

import com.assurance.Entity.Rappel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RappelRepository extends JpaRepository<Rappel, Long> {

    List<Rappel> findByContratIdAndTypeRappel(Long contratId, Rappel.TypeRappel type);

    List<Rappel> findByDateEnvoiAfter(LocalDateTime date);
}