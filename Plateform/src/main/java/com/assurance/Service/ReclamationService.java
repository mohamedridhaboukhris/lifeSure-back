package com.assurance.Service;

import com.assurance.Entity.Reclamation;
import com.assurance.Entity.StatutReclamation;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReclamationService {


    Reclamation soumettreReclamation(Reclamation reclamation, Long clientId);
    List<Reclamation> getReclamationsClient(Long clientId);

    List<Reclamation> getReclamationsSoumises();
    public Reclamation traiter(Long reclamationId, StatutReclamation statut, String justification);
    public List<Reclamation> getAll();
}
