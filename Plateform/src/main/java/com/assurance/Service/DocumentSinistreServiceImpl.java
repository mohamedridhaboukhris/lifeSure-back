//package com.assurance.Service;
//
//import com.assurance.Entity.DocumentSinistre;
//import com.assurance.Entity.Sinistre;
//import com.assurance.Repository.DocumentRepository;
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class DocumentSinistreServiceImpl  implements DocumentSinistreService {
//
//    private final DocumentRepository documentRepository;
//    @Value("${file.upload-dir:uploads/sinistres}")
//    private String uploadDir;
//
//    @Override
//    public void ajouterDocuments(Sinistre sinistre, List<MultipartFile> fichiers) {
//        // Créer le dossier si n'existe pas
//        try {
//            Files.createDirectories(Paths.get(uploadDir));
//        } catch (IOException e) {
//            throw new RuntimeException("Impossible de créer le répertoire d'upload", e);
//        }
//
//        fichiers.forEach(file -> {
//            try {
//                // Générer un nom unique
//                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
//                Path filePath = Paths.get(uploadDir, fileName);
//
//                // Sauvegarder le fichier
//                Files.copy(file.getInputStream(), filePath);
//
//                // Enregistrer en base
//                DocumentSinistre doc = DocumentSinistre.builder()
//                        .fileName(file.getOriginalFilename())
//                        .fileType(file.getContentType())
//                    //    .content(file.getContentType())
//                        .sinistre(sinistre)
//                        .build();
//                documentRepository.save(doc);
//            } catch (IOException e) {
//                throw new RuntimeException("Erreur lors du stockage du fichier: " + file.getOriginalFilename(), e);
//            }
//        });
//    }
//
//    }




















    package com.assurance.Service;

import com.assurance.Entity.DocumentSinistre;
import com.assurance.Entity.Sinistre;
import com.assurance.Repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentSinistreServiceImpl implements DocumentSinistreService {

    private final DocumentRepository documentRepository;

    @Value("${file.upload-dir:uploads/sinistres}")
    private String uploadDir;

    @Override
    public void ajouterDocuments(Sinistre sinistre, List<MultipartFile> fichiers) {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le répertoire d'upload", e);
        }

        fichiers.forEach(file -> {
            try {
                // Générer un nom unique
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);

                // Sauvegarder le fichier sur disque
                Files.copy(file.getInputStream(), filePath);

                // ✅ Enregistrer en base AVEC le contenu binaire
                DocumentSinistre doc = DocumentSinistre.builder()
                        .fileName(file.getOriginalFilename())
                        .fileType(file.getContentType())
                        .content(file.getBytes())  // ✅ AJOUTÉ : contenu binaire
                        .sinistre(sinistre)
                        .build();

                documentRepository.save(doc);

            } catch (IOException e) {
                throw new RuntimeException("Erreur lors du stockage du fichier: "
                        + file.getOriginalFilename(), e);
            }
        });
    }
}

