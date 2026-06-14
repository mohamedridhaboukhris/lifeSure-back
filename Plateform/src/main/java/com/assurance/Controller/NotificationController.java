package com.assurance.Controller;

import com.assurance.Entity.Notification;
import com.assurance.Entity.User;
import com.assurance.Repository.NotificationRepository;
import com.assurance.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 📬 Toutes les notifications de l'utilisateur connecté
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT', 'EXPERT', 'ADMINISTRATEUR', 'CLIENT')")
    public List<Notification> mesNotifications(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User introuvable"));
        return notificationRepository.findByDestinataireOrderByDateCreationDesc(user);
    }

    /**
     * 🔔 Compteur de notifications non lues
     */
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('AGENT', 'EXPERT', 'ADMINISTRATEUR', 'CLIENT')")
    public Map<String, Long> count(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User introuvable"));
        long count = notificationRepository.countByDestinataireAndLueFalse(user);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return response;
    }

    /**
     * ✅ Marquer une notification comme lue
     */
    @PutMapping("/{id}/lue")
    @PreAuthorize("hasAnyRole('AGENT', 'EXPERT', 'ADMINISTRATEUR', 'CLIENT')")
    public Notification marquerCommeLue(@PathVariable Long id, Authentication authentication) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification introuvable"));

        if (!notif.getDestinataire().getEmail().equals(authentication.getName())) {
            throw new RuntimeException("Non autorisé");
        }

        notif.setLue(true);
        return notificationRepository.save(notif);
    }

    /**
     * ✅ Marquer TOUTES les notifications comme lues
     */
    @PutMapping("/toutes-lues")
    @PreAuthorize("hasAnyRole('AGENT', 'EXPERT', 'ADMINISTRATEUR', 'CLIENT')")
    public Map<String, String> marquerToutesCommeLues(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User introuvable"));

        List<Notification> nonLues = notificationRepository
                .findByDestinataireAndLueFalseOrderByDateCreationDesc(user);

        for (Notification notif : nonLues) {
            notif.setLue(true);
        }
        notificationRepository.saveAll(nonLues);

        Map<String, String> response = new HashMap<>();
        response.put("message", nonLues.size() + " notification(s) marquée(s) comme lues");
        return response;
    }
}