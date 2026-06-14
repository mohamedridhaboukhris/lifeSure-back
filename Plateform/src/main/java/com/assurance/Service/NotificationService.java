package com.assurance.Service;

import com.assurance.Entity.Notification;
import com.assurance.Entity.User;
import com.assurance.Repository.NotificationRepository;
import com.assurance.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Créer une notification pour TOUS les agents
     */
    public void notifierTousLesAgents(
            Notification.TypeNotification type,
            String titre,
            String message,
            String icone,
            String couleur,
            String lien) {

        List<User> agents = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null
                        && u.getRole().toString().equals("AGENT")
                        && Boolean.TRUE.equals(u.getActif()))
                .toList();

        for (User agent : agents) {
            Notification notif = Notification.builder()
                    .destinataire(agent)
                    .type(type)
                    .titre(titre)
                    .message(message)
                    .icone(icone)
                    .couleur(couleur)
                    .lien(lien)
                    .lue(false)
                    .build();
            notificationRepository.save(notif);
        }

        System.out.println("🔔 " + agents.size() + " notification(s) envoyée(s) aux agents");
    }

    /**
     * Créer une notification pour un user spécifique
     */
    public void notifierUser(User user,
                             Notification.TypeNotification type,
                             String titre,
                             String message,
                             String icone,
                             String couleur,
                             String lien) {

        Notification notif = Notification.builder()
                .destinataire(user)
                .type(type)
                .titre(titre)
                .message(message)
                .icone(icone)
                .couleur(couleur)
                .lien(lien)
                .lue(false)
                .build();
        notificationRepository.save(notif);
    }
}