package com.assurance.Controller;
import com.assurance.Entity.TauxAssurance;
import com.assurance.Entity.TypeContrat;
import com.assurance.Service.TauxAssuranceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/taux")
@RequiredArgsConstructor
public class TauxAssuranceController {
    private final TauxAssuranceService service;




    // Ajouter un nouveau taux
    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/add")
    public TauxAssurance ajouterTaux(@RequestBody TauxAssurance tauxAssurance) {
        return service.ajouterTaux(tauxAssurance.getTypeContrat(), tauxAssurance.getTaux());
    }

    // Mettre à jour le taux actif
    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/set")
    public TauxAssurance setTaux(@RequestBody TauxAssurance tauxAssurance) {
        return service.setTaux(tauxAssurance.getTypeContrat(), tauxAssurance.getTaux());
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/all")
    public List<TauxAssurance> getAllTaux() {
        return service.getAllTaux();
    }




}
