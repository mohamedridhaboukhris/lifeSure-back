package com.assurance.Repository;

import com.assurance.Entity.DocumentSinistre;
import com.assurance.Entity.Sinistre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<DocumentSinistre, Long> {
    List<DocumentSinistre> findBySinistre(Sinistre sinistre);

    // Supprimer tous les documents liés à un sinistre
    void deleteBySinistre(Sinistre sinistre);
}
