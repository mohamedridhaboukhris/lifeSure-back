package com.assurance.Controller;

import com.assurance.Entity.Contrat;
import com.assurance.Entity.Paiement;
import com.assurance.Entity.Sinistre;
import com.assurance.Repository.ContratRepository;
import com.assurance.Repository.PaiementRepository;
import com.assurance.Repository.SinistreRepository;
import com.assurance.Service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;
    private final PaiementRepository paiementRepository;
    private final ContratRepository contratRepository;
    private final SinistreRepository sinistreRepository;

    // 📄 Reçu de paiement
    @GetMapping("/recu/{paiementId}")
    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMINISTRATEUR')")
    public ResponseEntity<byte[]> recuPaiement(@PathVariable Long paiementId) throws IOException {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new RuntimeException("Paiement introuvable"));

        byte[] pdf = pdfService.genererRecuPaiement(paiement);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "Recu_Paiement_" + paiement.getId() + ".pdf");

        return new ResponseEntity<>(pdf, headers, 200);
    }

    // 🛡️ Attestation d'assurance
    @GetMapping("/attestation/{contratId}")
    @PreAuthorize("hasAnyRole('CLIENT','AGENT','ADMINISTRATEUR')")
    public ResponseEntity<byte[]> attestation(@PathVariable Long contratId) throws IOException {
        Contrat contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));

        byte[] pdf = pdfService.genererAttestation(contrat);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "Attestation_" + contrat.getNumeroContrat() + ".pdf");

        return new ResponseEntity<>(pdf, headers, 200);
    }

    // 📋 Rapport de sinistre
    @GetMapping("/sinistre/{sinistreId}")
    @PreAuthorize("hasAnyRole('CLIENT','AGENT','EXPERT','ADMINISTRATEUR')")
    public ResponseEntity<byte[]> rapportSinistre(@PathVariable Long sinistreId) throws IOException {
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre introuvable"));

        byte[] pdf = pdfService.genererRapportSinistre(sinistre);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "Rapport_" + sinistre.getNumeroSinistre() + ".pdf");

        return new ResponseEntity<>(pdf, headers, 200);
    }
}