package com.assurance.Service;

import com.assurance.Entity.Contrat;
import com.assurance.Entity.StatutContrat;
import com.assurance.Entity.TauxAssurance;
import com.assurance.Entity.TypeContrat;
import com.assurance.Repository.ContratRepository;
import com.assurance.Repository.TauxAssuranceRepository;
import com.assurance.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContratServiceImpl implements ContratService {
    private final ContratRepository contratRepository;
    private final UserRepository userRepository;

    private final TauxAssuranceRepository tauxAssuranceRepository;
   /* @Override
    public Contrat creerContrat(Contrat contrat) {
        return contratRepository.save(contrat);
    }*/
   /*@Override
   public Contrat creerContrat(Contrat contrat) {
       // 1️⃣ Récupérer le taux actif selon le type de contrat
       TauxAssurance tauxAssurance = tauxAssuranceRepository
               .findByTypeContratAndActifTrue(contrat.getTypeContrat())
               .orElseThrow(() -> new RuntimeException("Taux Assurance non trouvé pour ce type de contrat"));

       // 2️⃣ Calculer la prime mensuelle
       BigDecimal primeMensuelle = contrat.getMontantGarantie()
               .multiply(tauxAssurance.getTaux())
               .divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);

       contrat.setPrimeMensuelle(primeMensuelle);

       // 3️⃣ Sauvegarder le contrat
       Contrat savedContrat = contratRepository.save(contrat);

       // 4️⃣ Générer le numéro du contrat
       savedContrat.setNumeroContrat("CTR-" + savedContrat.getId());

       return contratRepository.save(savedContrat);
   }
*/

    @Override
    public Contrat creerContrat(Contrat contrat) {
        // 1️⃣ Récupérer le taux actif selon le type de contrat
        TauxAssurance tauxAssurance = tauxAssuranceRepository
                .findByTypeContratAndActifTrue(contrat.getTypeContrat())
                .orElseThrow(() -> new RuntimeException("Taux Assurance non trouvé pour ce type de contrat"));

        BigDecimal prime = BigDecimal.ZERO;

        switch (contrat.getTypeContrat()) {
            case AUTO, HABITATION:
                // Prime basée sur le montantGarantie
                prime = contrat.getMontantGarantie()
                        .multiply(tauxAssurance.getTaux())
                        .divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);
                break;

            case SANTE:
                if (contrat.getPlafondAnnuel() == null) {
                    throw new RuntimeException("Plafond annuel doit être fourni par le client pour les contrats santé");
                }
                // Prime basée sur le plafond choisi par le client et éventuellement l’âge
                BigDecimal ageFactor = BigDecimal.valueOf(1 + (contrat.getAgeAssure() / 100.0)); // simple facteur selon âge
                prime = contrat.getPlafondAnnuel()
                        .multiply(tauxAssurance.getTaux())
                        .multiply(ageFactor)
                        .divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);
                break;

            case VOYAGE:
                if (contrat.getPlafondAssurance() == null) {
                    throw new RuntimeException("Plafond assurance doit être fourni par le client pour les contrats voyage");
                }
                if (contrat.getDureeVoyage() == null) {
                    throw new RuntimeException("Durée du voyage est obligatoire");
                }
                // Prime basée sur le plafond choisi par le client et la durée du voyage
                prime = contrat.getPlafondAssurance()
                        .multiply(tauxAssurance.getTaux())
                        .multiply(BigDecimal.valueOf(contrat.getDureeVoyage()))
                        .setScale(2, RoundingMode.HALF_UP);
                break;

            default:
                throw new RuntimeException("Type de contrat inconnu");
        }

        contrat.setPrimeMensuelle(prime);

        // 2️⃣ Sauvegarder le contrat
        Contrat savedContrat = contratRepository.save(contrat);

        // 3️⃣ Générer le numéro du contrat
        savedContrat.setNumeroContrat("CTR-" + savedContrat.getId());

        return contratRepository.save(savedContrat);
    }
    @Override
    public Contrat getContratById(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));
    }

    @Override
    public Contrat getContratByNumero(String numero) {
        return contratRepository.findByNumeroContrat(numero)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));
    }

    @Override
    public List<Contrat> getAllContrats() {
        return contratRepository.findAll();
    }

    @Override
    public List<Contrat> getContratsByClient(Long clientId) {
        return contratRepository.findByClientId(clientId);
    }

    @Override
    public List<Contrat> getContratsByType(TypeContrat type) {
        return contratRepository.findByTypeContrat(type);
    }

    @Override
    public List<Contrat> getContratsByClientAndStatut(Long clientId, StatutContrat statut) {
        return contratRepository.findByClientIdAndStatut(clientId, statut);
    }

    @Override
    public List<Contrat> getContratsExpirantEntre(LocalDate start, LocalDate end) {
        return contratRepository.findExpiringContracts(start, end);
    }

    @Override
    public Map<TypeContrat, Long> statistiquesNombreParType() {
        Map<TypeContrat, Long> result = new HashMap<>();
        contratRepository.countByTypeContrat().forEach(obj -> result.put((TypeContrat) obj[0], (Long) obj[1]));
        return result;
    }

    @Override
    public Map<TypeContrat, Double> statistiquesRevenusParType() {
        Map<TypeContrat, Double> result = new HashMap<>();
        contratRepository.sumPrimeByType().forEach(obj ->
                result.put((TypeContrat) obj[0], ((Number) obj[1]).doubleValue()));
        return result;
    }

    @Override
    public Contrat activerContrat(Long id) {
        Contrat c = getContratById(id);
        c.activer();
        return contratRepository.save(c);
    }

    @Override
    public Contrat suspendreContrat(Long id) {
        Contrat c = getContratById(id);
        c.suspendre();
        return contratRepository.save(c);
    }

    @Override
    public Contrat resilierContrat(Long id) {
        Contrat c = getContratById(id);
        c.resilier();
        return contratRepository.save(c);
    }

    @Override
    public void supprimerContrat(Long id) {
        contratRepository.deleteById(id);
    }

    @Override
    public Contrat updateContrat(Long id, Contrat contrat) {
        Contrat existing = getContratById(id);


        existing.setTypeContrat(contrat.getTypeContrat());
        existing.setDateDebut(contrat.getDateDebut());
        existing.setDateFin(contrat.getDateFin());
        existing.setPrimeMensuelle(contrat.getPrimeMensuelle());

        existing.setDescription(contrat.getDescription());
        existing.setClient(contrat.getClient());
        existing.setVehiculeMarque(contrat.getVehiculeMarque());
        existing.setVehiculeModele(contrat.getVehiculeModele());
        existing.setVehiculeImmatriculation(contrat.getVehiculeImmatriculation());
        existing.setAdresseBien(contrat.getAdresseBien());
        existing.setSuperficieBien(contrat.getSuperficieBien());
        existing.setTypeBien(contrat.getTypeBien());

        return contratRepository.save(existing);
    }
}
