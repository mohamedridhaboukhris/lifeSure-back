package com.assurance.Service;

import com.assurance.Entity.Contrat;
import java.time.format.DateTimeFormatter;

public class ContratEmailTemplates {

    // ========================================
    // 📧 EMAIL 1 : Contrat soumis (au client)
    // ========================================
//    public static String contratSoumis(Contrat contrat) {
//        String nomComplet = contrat.getClient().getPrenom() + " " + contrat.getClient().getNom();
//        return """
//            <!DOCTYPE html>
//            <html>
//            <head><meta charset="UTF-8"></head>
//            <body style="font-family: Arial, sans-serif; background: #f4f6f8; padding: 20px;">
//                <div style="max-width: 600px; margin: auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
//
//                    <!-- Header -->
//                    <div style="background: linear-gradient(135deg, #1e3a8a 0%%, #3b82f6 100%%); padding: 30px; text-align: center;">
//                        <h1 style="color: white; margin: 0;">🛡️ LifeSure</h1>
//                        <p style="color: white; margin: 8px 0 0;">Votre demande est en cours d'examen</p>
//                    </div>
//
//                    <!-- Body -->
//                    <div style="padding: 30px;">
//                        <h2 style="color: #1e3a8a;">⏳ Demande reçue</h2>
//                        <p>Bonjour <strong>%s</strong>,</p>
//                        <p>Nous avons bien reçu votre demande de contrat. Notre équipe d'agents va examiner votre dossier dans les plus brefs délais.</p>
//
//                        <!-- Détails contrat -->
//                        <div style="background: #f0f9ff; border-left: 4px solid #3b82f6; padding: 16px; margin: 20px 0; border-radius: 4px;">
//                            <p style="margin: 4px 0;"><strong>📋 N° de contrat :</strong> %s</p>
//                            <p style="margin: 4px 0;"><strong>🛡️ Type :</strong> %s</p>
//                            <p style="margin: 4px 0;"><strong>💰 Prime mensuelle :</strong> %s DT</p>
//                            <p style="margin: 4px 0;"><strong>📊 Statut :</strong> <span style="color: #f59e0b; font-weight: bold;">EN ATTENTE DE VALIDATION</span></p>
//                        </div>
//
//                        <div style="background: #fef3c7; padding: 16px; border-radius: 8px; margin: 20px 0;">
//                            <p style="margin: 0; color: #92400e;">⏱️ <strong>Délai de traitement :</strong> sous 48h ouvrables</p>
//                        </div>
//
//                        <p>Vous recevrez un email dès qu'une décision sera prise.</p>
//
//                        <div style="text-align: center; margin: 30px 0;">
//                            <a href="http://localhost:4200/mes-contrats" style="background: #3b82f6; color: white; padding: 12px 32px; text-decoration: none; border-radius: 8px; font-weight: bold;">📋 Voir mes contrats</a>
//                        </div>
//                    </div>
//
//                    <!-- Footer -->
//                    <div style="background: #f8fafc; padding: 20px; text-align: center; color: #64748b; font-size: 12px;">
//                        <p style="margin: 0;">© 2026 LifeSure - Tous droits réservés</p>
//                        <p style="margin: 4px 0 0;">Cet email est généré automatiquement.</p>
//                    </div>
//                </div>
//            </body>
//            </html>
//            """.formatted(
//                nomComplet,
//                contrat.getNumeroContrat(),
//                contrat.getTypeContrat(),
//                contrat.getPrimeMensuelle()
//        );
//    }




    public static String contratSoumis(Contrat contrat) {
        String nomComplet = contrat.getClient().getPrenom() + " " + contrat.getClient().getNom();
        return """
        <!DOCTYPE html>
        <html>
        <head><meta charset="UTF-8"></head>
        <body style="font-family: Arial, sans-serif; background: #f4f6f8; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                
                <!-- Header -->
                <div style="background: linear-gradient(135deg, #b8860b 0%%, #f0c14b 100%%); padding: 30px; text-align: center;">
                    <h1 style="color: white; margin: 0;">🛡️ LifeSure</h1>
                    <p style="color: white; margin: 8px 0 0;">Votre demande est en cours d'examen</p>
                </div>
                
                <!-- Body -->
                <div style="padding: 30px;">
                    <h2 style="color: #b8860b;">⏳ Demande reçue</h2>
                    <p>Bonjour <strong>%s</strong>,</p>
                    <p>Nous avons bien reçu votre demande de contrat. Notre équipe d'agents va examiner votre dossier dans les plus brefs délais.</p>
                    
                    <!-- Détails contrat -->
                    <div style="background: #fdf6e3; border-left: 4px solid #f0c14b; padding: 16px; margin: 20px 0; border-radius: 4px;">
                        <p style="margin: 4px 0;"><strong>📋 N° de contrat :</strong> %s</p>
                        <p style="margin: 4px 0;"><strong>🛡️ Type :</strong> %s</p>
                        <p style="margin: 4px 0;"><strong>💰 Prime mensuelle :</strong> %s DT</p>
                        <p style="margin: 4px 0;"><strong>📊 Statut :</strong> <span style="color: #f59e0b; font-weight: bold;">EN ATTENTE DE VALIDATION</span></p>
                    </div>
                    
                    <div style="background: #fef3c7; padding: 16px; border-radius: 8px; margin: 20px 0;">
                        <p style="margin: 0; color: #92400e;">⏱️ <strong>Délai de traitement :</strong> sous 48h ouvrables</p>
                    </div>
                    
                    <p>Vous recevrez un email dès qu'une décision sera prise.</p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="http://localhost:4200/mes-contrats" style="background: #f0c14b; color: white; padding: 12px 32px; text-decoration: none; border-radius: 8px; font-weight: bold;">📋 Voir mes contrats</a>
                    </div>
                </div>
                
                <!-- Footer -->
                <div style="background: #f8fafc; padding: 20px; text-align: center; color: #64748b; font-size: 12px;">
                    <p style="margin: 0;">© 2026 LifeSure - Tous droits réservés</p>
                    <p style="margin: 4px 0 0;">Cet email est généré automatiquement.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                nomComplet,
                contrat.getNumeroContrat(),
                contrat.getTypeContrat(),
                contrat.getPrimeMensuelle()
        );
    }




















































































//    // ========================================
//    // 📧 EMAIL 2 : Contrat ACCEPTÉ
//    // ========================================
//    public static String contratAccepte(Contrat contrat) {
//        String nomComplet = contrat.getClient().getPrenom() + " " + contrat.getClient().getNom();
//        String agentNom = contrat.getAgentValidateur() != null
//                ? contrat.getAgentValidateur().getPrenom() + " " + contrat.getAgentValidateur().getNom()
//                : "L'équipe";
//
//        return """
//            <!DOCTYPE html>
//            <html>
//            <head><meta charset="UTF-8"></head>
//            <body style="font-family: Arial, sans-serif; background: #f4f6f8; padding: 20px;">
//                <div style="max-width: 600px; margin: auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
//
//                    <!-- Header VERT -->
//                    <div style="background: linear-gradient(135deg, #059669 0%%, #10b981 100%%); padding: 30px; text-align: center;">
//                        <h1 style="color: white; margin: 0; font-size: 32px;">🎉 Contrat Accepté !</h1>
//                        <p style="color: white; margin: 8px 0 0; font-size: 16px;">Félicitations, votre demande a été approuvée</p>
//                    </div>
//
//                    <!-- Body -->
//                    <div style="padding: 30px;">
//                        <h2 style="color: #059669;">✅ Excellente nouvelle</h2>
//                        <p>Bonjour <strong>%s</strong>,</p>
//                        <p>Nous sommes ravis de vous informer que votre contrat a été <strong style="color: #059669;">ACCEPTÉ</strong> par notre équipe d'agents !</p>
//
//                        <!-- Détails -->
//                        <div style="background: #ecfdf5; border-left: 4px solid #10b981; padding: 16px; margin: 20px 0; border-radius: 4px;">
//                            <p style="margin: 4px 0;"><strong>📋 N° de contrat :</strong> %s</p>
//                            <p style="margin: 4px 0;"><strong>🛡️ Type :</strong> %s</p>
//                            <p style="margin: 4px 0;"><strong>💰 Prime mensuelle :</strong> %s DT</p>
//                            <p style="margin: 4px 0;"><strong>👨‍💼 Validé par :</strong> %s</p>
//                            <p style="margin: 4px 0;"><strong>📊 Statut :</strong> <span style="color: #059669; font-weight: bold;">EN ATTENTE PAIEMENT</span></p>
//                        </div>
//
//                        <!-- Prochaines étapes -->
//                        <div style="background: #f0f9ff; padding: 20px; border-radius: 8px; margin: 20px 0;">
//                            <h3 style="margin-top: 0; color: #1e3a8a;">📝 Prochaines étapes :</h3>
//                            <ol style="margin: 0; padding-left: 20px;">
//                                <li>Effectuer le premier paiement</li>
//                                <li>Signer électroniquement votre contrat</li>
//                                <li>Recevoir votre contrat officiel en PDF</li>
//                            </ol>
//                        </div>
//
//                        <div style="text-align: center; margin: 30px 0;">
//                            <a href="http://localhost:4200/mes-contrats" style="background: #10b981; color: white; padding: 14px 36px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px;">💳 Procéder au paiement</a>
//                        </div>
//
//                        <p style="color: #64748b; font-size: 14px;">Merci de votre confiance ! 🙏</p>
//                    </div>
//
//                    <!-- Footer -->
//                    <div style="background: #f8fafc; padding: 20px; text-align: center; color: #64748b; font-size: 12px;">
//                        <p style="margin: 0;">© 2026 LifeSure - Tous droits réservés</p>
//                    </div>
//                </div>
//            </body>
//            </html>
//            """.formatted(
//                nomComplet,
//                contrat.getNumeroContrat(),
//                contrat.getTypeContrat(),
//                contrat.getPrimeMensuelle(),
//                agentNom
//        );
//    }
//
//    // ========================================
//    // 📧 EMAIL 3 : Contrat REFUSÉ
//    // ========================================
//    public static String contratRefuse(Contrat contrat) {
//        String nomComplet = contrat.getClient().getPrenom() + " " + contrat.getClient().getNom();
//        String agentNom = contrat.getAgentValidateur() != null
//                ? contrat.getAgentValidateur().getPrenom() + " " + contrat.getAgentValidateur().getNom()
//                : "L'équipe";
//
//        return """
//            <!DOCTYPE html>
//            <html>
//            <head><meta charset="UTF-8"></head>
//            <body style="font-family: Arial, sans-serif; background: #f4f6f8; padding: 20px;">
//                <div style="max-width: 600px; margin: auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
//
//                    <!-- Header ROUGE -->
//                    <div style="background: linear-gradient(135deg, #dc2626 0%%, #ef4444 100%%); padding: 30px; text-align: center;">
//                        <h1 style="color: white; margin: 0;">❌ Demande non aboutie</h1>
//                        <p style="color: white; margin: 8px 0 0;">Décision concernant votre contrat</p>
//                    </div>
//
//                    <!-- Body -->
//                    <div style="padding: 30px;">
//                        <p>Bonjour <strong>%s</strong>,</p>
//                        <p>Après examen attentif de votre dossier, nous regrettons de vous informer que votre demande de contrat n'a pas pu être acceptée.</p>
//
//                        <!-- Détails -->
//                        <div style="background: #fef2f2; border-left: 4px solid #ef4444; padding: 16px; margin: 20px 0; border-radius: 4px;">
//                            <p style="margin: 4px 0;"><strong>📋 N° de contrat :</strong> %s</p>
//                            <p style="margin: 4px 0;"><strong>🛡️ Type :</strong> %s</p>
//                            <p style="margin: 4px 0;"><strong>👨‍💼 Examiné par :</strong> %s</p>
//                            <p style="margin: 4px 0;"><strong>📊 Statut :</strong> <span style="color: #dc2626; font-weight: bold;">REFUSÉ</span></p>
//                        </div>
//
//                        <!-- Motif -->
//                        <div style="background: #fef3c7; padding: 20px; border-radius: 8px; margin: 20px 0;">
//                            <h3 style="margin-top: 0; color: #92400e;">📝 Motif détaillé du refus :</h3>
//                            <p style="margin: 0; font-style: italic; color: #78350f;">"%s"</p>
//                        </div>
//
//                        <!-- Que faire -->
//                        <div style="background: #f0f9ff; padding: 20px; border-radius: 8px; margin: 20px 0;">
//                            <h3 style="margin-top: 0; color: #1e3a8a;">💡 Que pouvez-vous faire ?</h3>
//                            <ul style="margin: 0; padding-left: 20px;">
//                                <li>Soumettre une nouvelle demande en corrigeant les éléments mentionnés</li>
//                                <li>Nous contacter pour plus d'informations</li>
//                                <li>Consulter nos autres offres adaptées à votre profil</li>
//                            </ul>
//                        </div>
//
//                        <div style="text-align: center; margin: 30px 0;">
//                            <a href="http://localhost:4200/contrats/nouveau" style="background: #3b82f6; color: white; padding: 12px 32px; text-decoration: none; border-radius: 8px; font-weight: bold; margin-right: 8px;">📝 Nouvelle demande</a>
//                            <a href="mailto:contact@lifesure.tn" style="background: #6b7280; color: white; padding: 12px 32px; text-decoration: none; border-radius: 8px; font-weight: bold;">📧 Nous contacter</a>
//                        </div>
//
//                        <p style="color: #64748b; font-size: 14px;">Merci de votre compréhension.</p>
//                    </div>
//
//                    <!-- Footer -->
//                    <div style="background: #f8fafc; padding: 20px; text-align: center; color: #64748b; font-size: 12px;">
//                        <p style="margin: 0;">© 2026 LifeSure - Tous droits réservés</p>
//                    </div>
//                </div>
//            </body>
//            </html>
//            """.formatted(
//                nomComplet,
//                contrat.getNumeroContrat(),
//                contrat.getTypeContrat(),
//                agentNom,
//                contrat.getMotifRefus()
//        );
//    }


    // EMAIL 2 : ACCEPTÉ
    public static String contratAccepte(Contrat contrat) {
        String nomComplet = contrat.getClient().getPrenom() + " " + contrat.getClient().getNom();
        String agentNom = contrat.getAgentValidateur() != null
                ? contrat.getAgentValidateur().getPrenom() + " " + contrat.getAgentValidateur().getNom()
                : "L'équipe";

        return """
        <!DOCTYPE html>
        <html>
        <head><meta charset="UTF-8"></head>
        <body style="font-family: Arial, sans-serif; background: #f4f6f8; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                
                <!-- Header OR -->
                <div style="background: linear-gradient(135deg, #b8860b 0%%, #f0c14b 100%%); padding: 30px; text-align: center;">
                    <h1 style="color: white; margin: 0; font-size: 32px;">🎉 Contrat Accepté !</h1>
                    <p style="color: white; margin: 8px 0 0; font-size: 16px;">Félicitations, votre demande a été approuvée</p>
                </div>
                
                <!-- Body -->
                <div style="padding: 30px;">
                    <h2 style="color: #b8860b;">✅ Excellente nouvelle</h2>
                    <p>Bonjour <strong>%s</strong>,</p>
                    <p>Nous sommes ravis de vous informer que votre contrat a été <strong style="color: #b8860b;">ACCEPTÉ</strong> par notre équipe d'agents !</p>
                    
                    <!-- Détails -->
                    <div style="background: #fdf6e3; border-left: 4px solid #f0c14b; padding: 16px; margin: 20px 0; border-radius: 4px;">
                        <p style="margin: 4px 0;"><strong>📋 N° de contrat :</strong> %s</p>
                        <p style="margin: 4px 0;"><strong>🛡️ Type :</strong> %s</p>
                        <p style="margin: 4px 0;"><strong>💰 Prime mensuelle :</strong> %s DT</p>
                        <p style="margin: 4px 0;"><strong>👨‍💼 Validé par :</strong> %s</p>
                        <p style="margin: 4px 0;"><strong>📊 Statut :</strong> <span style="color: #b8860b; font-weight: bold;">EN ATTENTE PAIEMENT</span></p>
                    </div>
                    
                    <!-- Prochaines étapes -->
                    <div style="background: #fdf6e3; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #b8860b;">📝 Prochaines étapes :</h3>
                        <ol style="margin: 0; padding-left: 20px;">
                            <li>Effectuer le premier paiement</li>
                            <li>Signer électroniquement votre contrat</li>
                            <li>Recevoir votre contrat officiel en PDF</li>
                        </ol>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="http://localhost:4200/mes-contrats" style="background: #f0c14b; color: white; padding: 14px 36px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px;">💳 Procéder au paiement</a>
                    </div>
                    
                    <p style="color: #64748b; font-size: 14px;">Merci de votre confiance ! 🙏</p>
                </div>
                
                <!-- Footer -->
                <div style="background: #f8fafc; padding: 20px; text-align: center; color: #64748b; font-size: 12px;">
                    <p style="margin: 0;">© 2026 LifeSure - Tous droits réservés</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                nomComplet,
                contrat.getNumeroContrat(),
                contrat.getTypeContrat(),
                contrat.getPrimeMensuelle(),
                agentNom
        );
    }

    // EMAIL 3 : REFUSÉ
    public static String contratRefuse(Contrat contrat) {
        String nomComplet = contrat.getClient().getPrenom() + " " + contrat.getClient().getNom();
        String agentNom = contrat.getAgentValidateur() != null
                ? contrat.getAgentValidateur().getPrenom() + " " + contrat.getAgentValidateur().getNom()
                : "L'équipe";

        return """
        <!DOCTYPE html>
        <html>
        <head><meta charset="UTF-8"></head>
        <body style="font-family: Arial, sans-serif; background: #f4f6f8; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                
                <!-- Header OR -->
                <div style="background: linear-gradient(135deg, #b8860b 0%%, #f0c14b 100%%); padding: 30px; text-align: center;">
                    <h1 style="color: white; margin: 0;">❌ Demande non aboutie</h1>
                    <p style="color: white; margin: 8px 0 0;">Décision concernant votre contrat</p>
                </div>
                
                <!-- Body -->
                <div style="padding: 30px;">
                    <p>Bonjour <strong>%s</strong>,</p>
                    <p>Après examen attentif de votre dossier, nous regrettons de vous informer que votre demande de contrat n'a pas pu être acceptée.</p>
                    
                    <!-- Détails -->
                    <div style="background: #fdf6e3; border-left: 4px solid #f0c14b; padding: 16px; margin: 20px 0; border-radius: 4px;">
                        <p style="margin: 4px 0;"><strong>📋 N° de contrat :</strong> %s</p>
                        <p style="margin: 4px 0;"><strong>🛡️ Type :</strong> %s</p>
                        <p style="margin: 4px 0;"><strong>👨‍💼 Examiné par :</strong> %s</p>
                        <p style="margin: 4px 0;"><strong>📊 Statut :</strong> <span style="color: #b8860b; font-weight: bold;">REFUSÉ</span></p>
                    </div>
                    
                    <!-- Motif -->
                    <div style="background: #fef3c7; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #92400e;">📝 Motif détaillé du refus :</h3>
                        <p style="margin: 0; font-style: italic; color: #78350f;">"%s"</p>
                    </div>
                    
                    <!-- Que faire -->
                    <div style="background: #fdf6e3; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #b8860b;">💡 Que pouvez-vous faire ?</h3>
                        <ul style="margin: 0; padding-left: 20px;">
                            <li>Soumettre une nouvelle demande en corrigeant les éléments mentionnés</li>
                            <li>Nous contacter pour plus d'informations</li>
                            <li>Consulter nos autres offres adaptées à votre profil</li>
                        </ul>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="http://localhost:4200/contrats/nouveau" style="background: #f0c14b; color: white; padding: 12px 32px; text-decoration: none; border-radius: 8px; font-weight: bold; margin-right: 8px;">📝 Nouvelle demande</a>
                        <a href="mailto:contact@lifesure.tn" style="background: #6b7280; color: white; padding: 12px 32px; text-decoration: none; border-radius: 8px; font-weight: bold;">📧 Nous contacter</a>
                    </div>
                    
                    <p style="color: #64748b; font-size: 14px;">Merci de votre compréhension.</p>
                </div>
                
                <!-- Footer -->
                <div style="background: #f8fafc; padding: 20px; text-align: center; color: #64748b; font-size: 12px;">
                    <p style="margin: 0;">© 2026 LifeSure - Tous droits réservés</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                nomComplet,
                contrat.getNumeroContrat(),
                contrat.getTypeContrat(),
                agentNom,
                contrat.getMotifRefus()
        );
    }




























































































































































}