package com.assurance.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {
    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("Endpoint public accessible à tous");
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<String> adminDashboard(Authentication authentication) {
        return ResponseEntity.ok("Dashboard Admin - Bienvenue " + authentication.getName());
    }

    @GetMapping("/agent/clients")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMINISTRATEUR')")
    public ResponseEntity<String> agentClients(Authentication authentication) {
        return ResponseEntity.ok("Liste des clients - Agent: " + authentication.getName());
    }

    @GetMapping("/expert/sinistres")
    @PreAuthorize("hasAnyRole('EXPERT', 'ADMINISTRATEUR')")
    public ResponseEntity<String> expertSinistres(Authentication authentication) {
        return ResponseEntity.ok("Liste des sinistres - Expert: " + authentication.getName());
    }

    @GetMapping("/client/contrats")
    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMINISTRATEUR')")
    public ResponseEntity<String> clientContrats(Authentication authentication) {
        return ResponseEntity.ok("Mes contrats - Client: " + authentication.getName());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(authentication.getPrincipal());
    }
}
