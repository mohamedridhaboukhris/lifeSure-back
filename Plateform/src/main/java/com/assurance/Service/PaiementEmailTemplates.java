//package com.assurance.Service;
//
//import com.assurance.Entity.Contrat;
//import com.assurance.Entity.Paiement;
//
//import java.time.format.DateTimeFormatter;
//
//public class PaiementEmailTemplates {
//
//    /**
//     * 💳 EMAIL : Confirmation de paiement réussi
//     */
//    public static String emailPaiementConfirme(Paiement paiement, Contrat contrat, String frontendUrl) {
//        String numeroContrat = contrat.getNumeroContrat();
//        String type = contrat.getTypeContrat().toString();
//        String montant = paiement.getMontant() + " DT";
//        String numeroRecu = "REC-" + paiement.getId();
//        String datePaiement = paiement.getDatePaiement()
//                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
//        String modePaiement = paiement.getModePaiement().toString();
//        String prenomClient = contrat.getClient().getPrenom();
//        String nomClient = contrat.getClient().getNom();
//
//        // 🆕 SOLUTION : Utiliser String.replace au lieu de formatted()
//        String template = """
//            <!DOCTYPE html>
//            <html lang="fr">
//            <head>
//              <meta charset="UTF-8">
//              <title>Confirmation de paiement</title>
//            </head>
//            <body style="margin:0; padding:0; background-color:#f5f5f0; font-family:'Segoe UI', Arial, sans-serif;">
//
//              <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#f5f5f0; padding:40px 20px;">
//                <tr>
//                  <td align="center">
//
//                    <table width="650" cellpadding="0" cellspacing="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 4px 20px rgba(0,0,0,0.08);">
//
//                      <!-- BANDEAU HEADER BLEU -->
//                      <tr>
//                        <td style="background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%); padding:40px 30px; text-align:center;">
//                          <h1 style="margin:0; color:#ffffff; font-size:32px; font-weight:600; letter-spacing:0.5px;">
//                            🛡️ LifeSure
//                          </h1>
//                          <p style="margin:8px 0 0; color:#dbeafe; font-size:14px; font-weight:500;">
//                            Confirmation de paiement
//                          </p>
//                        </td>
//                      </tr>
//
//                      <!-- LIGNE DORÉE -->
//                      <tr>
//                        <td style="height:4px; background:#d4af37;"></td>
//                      </tr>
//
//                      <!-- CONTENU -->
//                      <tr>
//                        <td style="padding:40px 35px;">
//
//                          <h2 style="margin:0 0 20px; color:#1e3a8a; font-size:22px; font-weight:600;">
//                            ✅ Paiement confirmé
//                          </h2>
//
//                          <p style="margin:0 0 15px; color:#1f2937; font-size:15px; line-height:1.6;">
//                            Bonjour <strong>{{PRENOM}} {{NOM}}</strong>,
//                          </p>
//
//                          <p style="margin:0 0 25px; color:#1f2937; font-size:15px; line-height:1.6;">
//                            Nous avons bien reçu votre paiement. Votre contrat est maintenant
//                            <strong style="color:#047857;">ACTIF</strong>. Voici le récapitulatif de votre transaction.
//                          </p>
//
//                          <!-- CARTE MONTANT (vert) -->
//                          <table width="100%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #047857 0%, #10b981 100%); border-radius:12px; margin-bottom:25px;">
//                            <tr>
//                              <td style="padding:25px; text-align:center;">
//                                <p style="margin:0 0 8px; color:#d1fae5; font-size:13px; font-weight:500; letter-spacing:1px; text-transform:uppercase;">
//                                  💰 Montant payé
//                                </p>
//                                <p style="margin:0; color:#ffffff; font-size:38px; font-weight:700;">
//                                  {{MONTANT}}
//                                </p>
//                              </td>
//                            </tr>
//                          </table>
//
//                          <!-- CARTE INFOS CONTRAT (bleu clair) -->
//                          <table width="100%" cellpadding="0" cellspacing="0" style="background:#eff6ff; border-left:4px solid #3b82f6; border-radius:8px; margin-bottom:20px;">
//                            <tr>
//                              <td style="padding:20px 25px;">
//                                <p style="margin:0 0 12px; color:#1f2937; font-size:15px;">
//                                  📋 <strong>N° Reçu :</strong> {{NUMERO_RECU}}
//                                </p>
//                                <p style="margin:0 0 12px; color:#1f2937; font-size:15px;">
//                                  📄 <strong>N° Contrat :</strong> {{NUMERO_CONTRAT}}
//                                </p>
//                                <p style="margin:0 0 12px; color:#1f2937; font-size:15px;">
//                                  🛡️ <strong>Type :</strong> {{TYPE}}
//                                </p>
//                                <p style="margin:0 0 12px; color:#1f2937; font-size:15px;">
//                                  📅 <strong>Date de paiement :</strong> {{DATE_PAIEMENT}}
//                                </p>
//                                <p style="margin:0 0 12px; color:#1f2937; font-size:15px;">
//                                  💳 <strong>Mode de paiement :</strong> {{MODE_PAIEMENT}}
//                                </p>
//                                <p style="margin:0; color:#1f2937; font-size:15px;">
//                                  📊 <strong>Statut :</strong>
//                                  <span style="color:#047857; font-weight:600;">✅ RÉUSSI</span>
//                                </p>
//                              </td>
//                            </tr>
//                          </table>
//
//                          <!-- ALERTE INFO (or/jaune) -->
//                          <table width="100%" cellpadding="0" cellspacing="0" style="background:#fef3c7; border-left:4px solid #d4af37; border-radius:8px; margin-bottom:25px;">
//                            <tr>
//                              <td style="padding:15px 20px;">
//                                <p style="margin:0; color:#92400e; font-size:14px;">
//                                  ⏰ <strong>Contrat activé :</strong> Votre couverture d'assurance est désormais effective.
//                                </p>
//                              </td>
//                            </tr>
//                          </table>
//
//                          <p style="margin:0 0 25px; color:#1f2937; font-size:15px; line-height:1.6;">
//                            Vous pouvez télécharger votre <strong>reçu officiel</strong> et votre
//                            <strong>attestation d'assurance</strong> depuis votre espace client.
//                          </p>
//
//                          <!-- BOUTON CTA -->
//                          <table width="100%" cellpadding="0" cellspacing="0">
//                            <tr>
//                              <td align="center" style="padding:10px 0 20px;">
//                                <a href="{{FRONTEND_URL}}/admin/paiements"
//                                   style="display:inline-block; background:linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%); color:#ffffff; padding:14px 36px; border-radius:8px; text-decoration:none; font-weight:600; font-size:15px; box-shadow:0 4px 12px rgba(59,130,246,0.3);">
//                                  📥 Télécharger mon reçu
//                                </a>
//                              </td>
//                            </tr>
//                          </table>
//
//                          <p style="margin:25px 0 0; color:#1e3a8a; font-size:14px; text-align:center; font-weight:500;">
//                            Merci de votre confiance ! 🙏
//                          </p>
//
//                        </td>
//                      </tr>
//
//                      <!-- FOOTER -->
//                      <tr>
//                        <td style="background:#f9fafb; padding:25px 30px; text-align:center; border-top:1px solid #e5e7eb;">
//                          <p style="margin:0 0 6px; color:#6b7280; font-size:13px; font-weight:500;">
//                            © 2026 LifeSure - Tous droits réservés
//                          </p>
//                          <p style="margin:0; color:#9ca3af; font-size:12px;">
//                            📧 contact@lifesure.tn · 📞 +216 71 123 456
//                          </p>
//                          <p style="margin:8px 0 0; color:#9ca3af; font-size:11px; font-style:italic;">
//                            Cet email est généré automatiquement.
//                          </p>
//                        </td>
//                      </tr>
//
//                    </table>
//
//                  </td>
//                </tr>
//              </table>
//
//            </body>
//            </html>
//            """;
//
//        // 🆕 Remplacer les placeholders {{XXX}}
//        return template
//                .replace("{{PRENOM}}", prenomClient != null ? prenomClient : "")
//                .replace("{{NOM}}", nomClient != null ? nomClient : "")
//                .replace("{{MONTANT}}", montant)
//                .replace("{{NUMERO_RECU}}", numeroRecu)
//                .replace("{{NUMERO_CONTRAT}}", numeroContrat)
//                .replace("{{TYPE}}", type)
//                .replace("{{DATE_PAIEMENT}}", datePaiement)
//                .replace("{{MODE_PAIEMENT}}", modePaiement)
//                .replace("{{FRONTEND_URL}}", frontendUrl);
//    }
//}