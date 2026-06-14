package com.assurance.Controller;

import com.assurance.Entity.Medecin;
import com.assurance.Repository.MedecinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/medecins")
@RequiredArgsConstructor
public class MedecinController {

    private final MedecinRepository medecinRepository;

    // 🏥 Liste tous les médecins actifs
    @GetMapping
    public List<Medecin> getAllMedecins() {
        return medecinRepository.findByActifTrue();
    }

    // 🔍 Médecin par ID
    @GetMapping("/{id}")
    public Medecin getMedecinById(@PathVariable Long id) {
        return medecinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));
    }
}