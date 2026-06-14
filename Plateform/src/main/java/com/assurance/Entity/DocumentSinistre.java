package com.assurance.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents_sinistre")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSinistre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    //@Column(nullable = false)
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] content; // le fichier en binaire

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType; // image/jpeg ou application/pdf

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id", nullable = false)
    @JsonIgnore
    private Sinistre sinistre;
}
