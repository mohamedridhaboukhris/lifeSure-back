package com.assurance.Service;


import com.assurance.Entity.TauxAssurance;
import com.assurance.Entity.TypeContrat;
import com.assurance.Repository.TauxAssuranceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TauxAssuranceServiceImpl implements TauxAssuranceService {
    private final TauxAssuranceRepository repository;

  /*  public TauxAssurance setTaux(TypeContrat type, BigDecimal taux) {
        // désactiver les anciens taux pour ce type
        repository.findByTypeContratAndActifTrue(type)
                .ifPresent(old -> { old.setActif(false); repository.save(old); });

        TauxAssurance nouveau = new TauxAssurance();
        nouveau.setTypeContrat(type);
        nouveau.setTaux(taux);
        nouveau.setActif(true);
        return repository.save(nouveau);
    }*/
  @Override
  public TauxAssurance setTaux(TypeContrat type, BigDecimal taux) {

      // désactiver ancien taux actif
      repository.findByTypeContratAndActifTrue(type)
              .ifPresent(old -> {
                  old.setActif(false);
                  repository.save(old);
              });

      // ajouter nouveau taux actif
      TauxAssurance nouveau = new TauxAssurance();
      nouveau.setTypeContrat(type);
      nouveau.setTaux(taux);
      nouveau.setActif(true);

      return repository.save(nouveau);
  }

    @Override
    public List<TauxAssurance> getAllTaux() {
        return repository.findAll();
    }
@Override
    public TauxAssurance ajouterTaux(TypeContrat type, BigDecimal taux) {
        TauxAssurance nouveau = new TauxAssurance();
        nouveau.setTypeContrat(type);
        nouveau.setTaux(taux);
        nouveau.setActif(true); // actif par défaut
        return repository.save(nouveau);
    }

}