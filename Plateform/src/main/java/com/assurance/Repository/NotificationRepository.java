package com.assurance.Repository;

import com.assurance.Entity.Notification;
import com.assurance.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Toutes les notifications d'un user, triées par date desc
    List<Notification> findByDestinataireOrderByDateCreationDesc(User destinataire);

    // Compter les non lues
    long countByDestinataireAndLueFalse(User destinataire);

    // Récupérer les non lues
    List<Notification> findByDestinataireAndLueFalseOrderByDateCreationDesc(User destinataire);
}