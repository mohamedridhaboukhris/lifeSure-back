package com.assurance.Service;

import com.assurance.Entity.Sinistre;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentSinistreService {

    public void ajouterDocuments(Sinistre sinistre, List<MultipartFile> fichiers);
}
