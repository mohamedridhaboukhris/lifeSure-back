package com.assurance.Service;

import com.assurance.Entity.Contrat;
import com.assurance.Entity.Sinistre;
import com.assurance.Entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatbotFallback {

    /**
     * Réponses prédéfinies quand Gemini est indisponible.
     * Garantit que le chatbot ne tombe JAMAIS en panne.
     */
    public String getAnswer(String message, User user, List<Contrat> contrats, List<Sinistre> sinistres) {
        String msg = message.toLowerCase();

        // 🔵 SALUTATIONS
        if (containsAny(msg, "bonjour", "salut", "hello", "bonsoir", "coucou", "hi", "hey")) {
            String name = user != null ? " " + user.getPrenom() : "";
            return "Bonjour" + name + " ! 👋 Je suis LifeBot. Comment puis-je vous aider ?";
        }

        // 📋 QUESTIONS SUR LES CONTRATS
        if (containsAny(msg, "mes contrats", "mes contrat", "quels sont mes")) {
            if (user == null) {
                return "🔐 Pour voir vos contrats, vous devez d'abord vous connecter à votre compte.";
            }
            if (contrats == null || contrats.isEmpty()) {
                return "📋 Vous n'avez encore aucun contrat. Cliquez sur 'Nouveau contrat' pour souscrire !";
            }
            StringBuilder sb = new StringBuilder("📋 Vous avez " + contrats.size() + " contrat(s) :\n\n");
            for (Contrat c : contrats) {
                sb.append("• ").append(c.getNumeroContrat())
                        .append(" (").append(c.getTypeContrat()).append(") - ")
                        .append(c.getStatut()).append(" - ")
                        .append(c.getPrimeMensuelle()).append(" DT/mois\n");
            }
            return sb.toString();
        }

        if (containsAny(msg, "contrat", "souscri", "assurance auto", "assurance habitation", "assurance santé", "assurance voyage")) {
            return "📋 LifeSure propose 4 types de contrats :\n" +
                    "• 🚗 AUTO - Pour vos véhicules\n" +
                    "• 🏠 HABITATION - Pour votre logement\n" +
                    "• ❤️ SANTÉ - Pour vous et votre famille\n" +
                    "• ✈️ VOYAGE - Pour vos déplacements\n\n" +
                    "Cliquez sur 'Nouveau contrat' pour en souscrire un.";
        }

        // 🚨 QUESTIONS SUR LES SINISTRES
        if (containsAny(msg, "mes sinistres", "mes sinistre")) {
            if (user == null) {
                return "🔐 Pour voir vos sinistres, vous devez d'abord vous connecter.";
            }
            if (sinistres == null || sinistres.isEmpty()) {
                return "✅ Vous n'avez déclaré aucun sinistre. Bonne nouvelle !";
            }
            StringBuilder sb = new StringBuilder("🚨 Vous avez " + sinistres.size() + " sinistre(s) :\n\n");
            for (Sinistre s : sinistres) {
                sb.append("• ").append(s.getNumeroSinistre())
                        .append(" - ").append(s.getStatut()).append("\n");
            }
            return sb.toString();
        }

        if (containsAny(msg, "sinistre", "déclar", "declar", "accident", "vol")) {
            return "🚨 Pour déclarer un sinistre :\n" +
                    "1. Allez dans 'Mes sinistres'\n" +
                    "2. Cliquez sur 'Déclarer un sinistre'\n" +
                    "3. Remplissez le formulaire et joignez les photos\n" +
                    "4. Un agent traitera votre demande rapidement.";
        }

        // 💳 QUESTIONS SUR LES PAIEMENTS
        if (containsAny(msg, "paie", "prime", "payer", "mensualité", "facture")) {
            return "💳 Pour payer votre prime :\n" +
                    "1. Allez dans 'Mes contrats'\n" +
                    "2. Cliquez sur 'Payer' à côté du contrat\n" +
                    "3. Le paiement est sécurisé via Stripe (carte bancaire)\n" +
                    "4. Vous recevez un reçu PDF par email.";
        }

        // 💬 QUESTIONS SUR LES RÉCLAMATIONS
        if (containsAny(msg, "réclam", "reclam", "plain", "plainte")) {
            return "💬 Pour soumettre une réclamation :\n" +
                    "1. Allez dans 'Mes réclamations'\n" +
                    "2. Cliquez sur 'Nouvelle réclamation'\n" +
                    "3. Décrivez votre problème\n" +
                    "Un expert traitera votre demande sous 48h.";
        }

        // 💰 QUESTIONS SUR LES TARIFS
        if (containsAny(msg, "tarif", "prix", "coût", "cout", "combien")) {
            return "💰 Les tarifs varient selon le type de contrat et votre profil. " +
                    "Pour obtenir un devis précis, créez un contrat dans 'Nouveau contrat' " +
                    "et le prix sera calculé automatiquement.";
        }

        // 📞 CONTACT
        if (containsAny(msg, "contact", "téléphone", "telephone", "numéro", "numero", "appeler")) {
            return "📞 Service client LifeSure :\n" +
                    "• Téléphone : +216 71 123 456\n" +
                    "• Email : contact@lifesure.tn\n" +
                    "• Horaires : Lundi - Vendredi, 9h - 18h";
        }

        // 🙏 REMERCIEMENTS
        if (containsAny(msg, "merci", "thank")) {
            return "🙏 Avec plaisir ! N'hésitez pas si vous avez d'autres questions.";
        }

        // 👋 AU REVOIR
        if (containsAny(msg, "au revoir", "bye", "adieu", "à bientôt")) {
            return "👋 À bientôt ! Bonne journée.";
        }

        // ❓ QUESTION INCONNUE
        return "Je n'ai pas bien compris votre question. 🤔\n\n" +
                "Vous pouvez me parler de :\n" +
                "• 📋 Contrats (vos contrats, types disponibles)\n" +
                "• 🚨 Sinistres (déclaration, suivi)\n" +
                "• 💳 Paiements (payer une prime)\n" +
                "• 💬 Réclamations\n" +
                "• 📞 Contact";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}