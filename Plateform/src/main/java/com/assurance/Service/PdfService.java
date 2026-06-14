package com.assurance.Service;

import com.assurance.Entity.Contrat;
import com.assurance.Entity.Paiement;
import com.assurance.Entity.Sinistre;
import com.assurance.Entity.User;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfService {

    private static final DeviceRgb NAVY       = new DeviceRgb(26,  43,  94);
    private static final DeviceRgb GOLD       = new DeviceRgb(212, 170,  30);
    private static final DeviceRgb GOLD_LIGHT = new DeviceRgb(255, 250, 215);
    private static final DeviceRgb WHITE      = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb ANTHRACITE = new DeviceRgb(40,  40,  40);
    private static final DeviceRgb SOFT_GRAY  = new DeviceRgb(130, 130, 130);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(220, 220, 220);
    private static final DeviceRgb EMERALD    = new DeviceRgb(4,  120,  87);
    private static final DeviceRgb BURGUNDY   = new DeviceRgb(153,  27,  27);
    private static final DeviceRgb CYAN       = new DeviceRgb(54,  185, 204);
    private static final DeviceRgb ORANGE     = new DeviceRgb(200, 110,  30);

    private final QrCodeService qrCodeService;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    // =====================================================
    // 📄 1. REÇU DE PAIEMENT
    // =====================================================
    public byte[] genererRecuPaiement(Paiement paiement) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = buildDoc(baos);

        // ✅ FIX : header blanc+or comme attestation (pas bleu marine)
        addHeaderOr(doc, "REÇU DE PAIEMENT", "DOCUMENT OFFICIEL");

        // Date + icône calendrier à droite
        Table dateRow = new Table(UnitValue.createPercentArray(new float[]{1f, 0.4f}))
                .useAllAvailableWidth()
                .setMarginLeft(30).setMarginRight(30).setMarginTop(8).setMarginBottom(12);
        Cell dateCell = new Cell().setBorder(Border.NO_BORDER);
        dateCell.add(new Paragraph()
                .add(new Text("Émis le ").setFontColor(SOFT_GRAY).setFontSize(9))
                .add(new Text(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                        .setBold().setFontColor(ANTHRACITE).setFontSize(9)));
        dateRow.addCell(dateCell);
        Cell calCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        calCell.add(new Paragraph("📅  Émis le\n"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                .setFontSize(8).setFontColor(SOFT_GRAY).setTextAlignment(TextAlignment.RIGHT));
        dateRow.addCell(calCell);
        doc.add(dateRow);

        addSectionTitle(doc, "📋", "INFORMATIONS DU PAIEMENT");
        Table t1 = infoTable();
        addRow(t1, "N° Reçu",         "REC-" + paiement.getId());
        addRow(t1, "Date de paiement", fmt(paiement.getDatePaiement(), "dd/MM/yyyy"));
        // ✅ FIX : remplacer _ par espace dans mode paiement
        addRow(t1, "Mode de paiement",
                paiement.getModePaiement().toString().replace("_", " "));
        addStatusRow(t1, "Statut", paiement.getStatut().toString());
        doc.add(t1);

        Contrat c = paiement.getContrat();
        if (c != null) {
            addSectionTitle(doc, "🛡️", "DÉTAILS DU CONTRAT");
            Table t2 = infoTable();
            addRow(t2, "Numéro contrat",   c.getNumeroContrat());
            addRow(t2, "Type d'assurance", c.getTypeContrat().toString());
            if (c.getClient() != null) {
                addRow(t2, "Assuré",
                        c.getClient().getPrenom() + " " + c.getClient().getNom());
                addRow(t2, "Email", c.getClient().getEmail());
            }
            doc.add(t2);
        }

        // ✅ FIX MONTANT : utiliser String.format pour éviter double .00
        String montantFormate = paiement.getMontant() != null
                ? String.format("%.2f DT", paiement.getMontant())
                : "0.00 DT";
        addMontantBlock(doc, montantFormate);

        doc.add(new Paragraph()
                .add(new Text("❤ Merci pour votre confiance.\n")
                        .setBold().setFontSize(11).setFontColor(NAVY))
                .add(new Text("Ce reçu fait foi de votre paiement.")
                        .setItalic().setFontSize(9).setFontColor(SOFT_GRAY))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10).setMarginBottom(20));

        addFooter(doc);
        doc.close();
        return baos.toByteArray();
    }

    // =====================================================
    // 🛡️ 2. ATTESTATION D'ASSURANCE
    // =====================================================
    public byte[] genererAttestation(Contrat contrat) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = buildDoc(baos);

        addHeaderAttestation(doc);
        doc.add(dateLine("Émise le "));

        User client = contrat.getClient();
        String prenom = client != null ? client.getPrenom() : "—";
        String nom    = client != null ? client.getNom()    : "—";

        doc.add(new Paragraph()
                .add(new Text("Nous, ").setFontSize(10).setFontColor(ANTHRACITE))
                .add(new Text("LifeSure Assurances").setBold().setFontColor(GOLD).setFontSize(10))
                .add(new Text(", attestons par la présente que :").setFontSize(10).setFontColor(ANTHRACITE))
                .setMarginLeft(30).setMarginRight(30).setMarginBottom(8));

        // Bloc client
        Table clientTbl = new Table(1).useAllAvailableWidth()
                .setMarginLeft(30).setMarginRight(30).setMarginBottom(12);
        Cell clientCell = new Cell()
                .setBackgroundColor(GOLD_LIGHT)
                .setBorder(new SolidBorder(GOLD, 1.5f))
                .setBorderRadius(new BorderRadius(6))
                .setPadding(12);

        Table clientInner = new Table(UnitValue.createPercentArray(new float[]{0.12f, 1f}))
                .useAllAvailableWidth().setBorder(Border.NO_BORDER);

        Cell iconCell = new Cell().setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE).setPaddingRight(10);
        Table iconBox = new Table(1).setWidth(38);
        Cell ib = new Cell()
                .setBackgroundColor(ORANGE)
                .setBorder(new SolidBorder(ORANGE, 1))
                .setBorderRadius(new BorderRadius(8))
                .setPadding(6).setTextAlignment(TextAlignment.CENTER);
        ib.add(new Paragraph("👤").setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
        iconBox.addCell(ib);
        iconCell.add(iconBox);
        clientInner.addCell(iconCell);

        Cell nameCell = new Cell().setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        nameCell.add(new Paragraph("M./Mme " + prenom + " " + nom)
                .setBold().setFontSize(14).setFontColor(NAVY).setMarginBottom(5));

        if (client != null) {
            Table contactRow = new Table(UnitValue.createPercentArray(new float[]{1f, 1f}))
                    .useAllAvailableWidth().setBorder(Border.NO_BORDER);
            Cell emailCell = new Cell().setBorder(Border.NO_BORDER);
            emailCell.add(new Paragraph("✉  " + client.getEmail())
                    .setFontSize(9).setFontColor(ANTHRACITE));
            contactRow.addCell(emailCell);
            Cell telCell = new Cell().setBorder(Border.NO_BORDER);
            if (client.getTelephone() != null)
                telCell.add(new Paragraph("📞  " + client.getTelephone())
                        .setFontSize(9).setFontColor(ANTHRACITE));
            contactRow.addCell(telCell);
            nameCell.add(contactRow);
        }
        clientInner.addCell(nameCell);
        clientCell.add(clientInner);
        clientTbl.addCell(clientCell);
        doc.add(clientTbl);

        doc.add(new Paragraph("est assuré(e) auprès de notre compagnie selon les modalités suivantes :")
                .setFontSize(10).setFontColor(ANTHRACITE)
                .setMarginLeft(30).setMarginRight(30).setMarginBottom(10));

        addSectionTitle(doc, "🛡️", "DÉTAILS DU CONTRAT");

        Table layout = new Table(UnitValue.createPercentArray(new float[]{1.65f, 1f}))
                .useAllAvailableWidth().setMarginLeft(30).setMarginRight(30).setMarginBottom(12);

        Cell dataCol = new Cell().setBorder(Border.NO_BORDER).setPaddingRight(8);
        Table infoTbl = infoTableWithIcons().setMarginLeft(0).setMarginRight(0);
        addIconRow(infoTbl, "📄", "N° de contrat",    contrat.getNumeroContrat());
        addIconRow(infoTbl, "🏠", "Type d'assurance", contrat.getTypeContrat().toString());
        addIconRow(infoTbl, "📅", "Date de début",    fmt(contrat.getDateDebut(), "dd/MM/yyyy"));
        addIconRow(infoTbl, "📅", "Date de fin",      fmt(contrat.getDateFin(),   "dd/MM/yyyy"));
        addIconRow(infoTbl, "💰", "Prime mensuelle",
                String.format("%.2f DT", contrat.getPrimeMensuelle()));
        addIconStatusRow(infoTbl, "✅", "Statut", contrat.getStatut().toString());

        switch (contrat.getTypeContrat()) {
            case AUTO:
                if (contrat.getVehiculeMarque() != null)
                    addIconRow(infoTbl, "🚗", "Véhicule",
                            contrat.getVehiculeMarque() + " " + contrat.getVehiculeModele()
                                    + " (" + contrat.getVehiculeImmatriculation() + ")");
                break;
            case HABITATION:
                if (contrat.getAdresseBien() != null) {
                    addIconRow(infoTbl, "📍", "Adresse", contrat.getAdresseBien());
                    addIconRow(infoTbl, "🏠", "Type",
                            contrat.getTypeBien() + " (" + contrat.getSuperficieBien() + " m²)");
                }
                break;
            case VOYAGE:
                if (contrat.getDestination() != null) {
                    addIconRow(infoTbl, "✈", "Destination", contrat.getDestination());
                    addIconRow(infoTbl, "⏱", "Durée",
                            contrat.getDureeVoyage() + " jours");
                }
                break;
            case SANTE:
                if (contrat.getPlafondAnnuel() != null)
                    addIconRow(infoTbl, "💊", "Plafond annuel",
                            String.format("%.2f DT", contrat.getPlafondAnnuel()));
                break;
        }
        dataCol.add(infoTbl);
        layout.addCell(dataCol);

        // QR code
        Cell qrCol = new Cell().setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.TOP);
        try {
            String url = frontendUrl + "/verifier-contrat/" + contrat.getNumeroContrat();
            byte[] qrBytes = qrCodeService.generateQrCode(url, 200);

            Table qrBox = new Table(1).useAllAvailableWidth();
            Cell qrCell = new Cell()
                    .setBorder(new SolidBorder(GOLD, 1.5f))
                    .setBorderRadius(new BorderRadius(6))
                    .setBackgroundColor(GOLD_LIGHT)
                    .setPadding(8).setTextAlignment(TextAlignment.CENTER);

            qrCell.add(new Paragraph("🛡  VÉRIFICATION\nD'AUTHENTICITÉ")
                    .setBold().setFontSize(7).setFontColor(NAVY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));

            qrCell.add(new Image(ImageDataFactory.create(qrBytes))
                    .setWidth(100).setHeight(100)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER));

            qrCell.add(new Paragraph("Scannez ce QR Code pour\nvérifier l'authenticité de\ncette attestation")
                    .setFontSize(7).setItalic().setFontColor(SOFT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(6));

            qrCell.add(new Paragraph(url)
                    .setFontSize(6).setFontColor(GOLD)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(4));

            qrBox.addCell(qrCell);
            qrCol.add(qrBox);
        } catch (Exception e) {
            qrCol.add(new Paragraph("[QR]").setFontColor(SOFT_GRAY).setFontSize(8));
        }
        layout.addCell(qrCol);
        doc.add(layout);

        // Mention légale
        Table mentionTbl = new Table(UnitValue.createPercentArray(new float[]{0.05f, 1f}))
                .useAllAvailableWidth()
                .setMarginLeft(30).setMarginRight(30).setMarginBottom(10);
        mentionTbl.setBorder(new SolidBorder(GOLD, 1));
        mentionTbl.setBorderRadius(new BorderRadius(6));

        Cell balanceCell = new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(GOLD_LIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(10);
        balanceCell.add(new Paragraph("⚖")
                .setFontSize(18).setFontColor(GOLD).setTextAlignment(TextAlignment.CENTER));
        mentionTbl.addCell(balanceCell);

        Cell mentionCell = new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(GOLD_LIGHT).setPadding(12);
        mentionCell.add(new Paragraph(
                "La présente attestation est délivrée pour servir et valoir ce que de droit.")
                .setItalic().setFontSize(10).setFontColor(ANTHRACITE));
        mentionTbl.addCell(mentionCell);
        doc.add(mentionTbl);

        // ✅ FIX : signature compacte pour éviter débordement page 2
        addSignatureSectionCompact(doc, contrat);
        addFooter(doc);
        doc.close();
        return baos.toByteArray();
    }

    // =====================================================
    // 📋 3. RAPPORT DE SINISTRE
    // =====================================================
    public byte[] genererRapportSinistre(Sinistre sinistre) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = buildDoc(baos);

        addHeaderSinistre(doc);

        Table banner = new Table(UnitValue.createPercentArray(new float[]{1f, 1f}))
                .useAllAvailableWidth().setMarginLeft(30).setMarginRight(30).setMarginBottom(14);

        Cell numCell = new Cell()
                .setBackgroundColor(GOLD_LIGHT)
                .setBorder(new SolidBorder(GOLD, 1)).setPadding(10);
        numCell.add(new Paragraph("SINISTRE N°")
                .setFontSize(8).setFontColor(SOFT_GRAY).setBold());
        numCell.add(new Paragraph(sinistre.getNumeroSinistre())
                .setBold().setFontSize(12).setFontColor(NAVY));
        banner.addCell(numCell);

        DeviceRgb sc = getStatutColor(sinistre.getStatut().toString());
        DeviceRgb sb = getStatutBg(sinistre.getStatut().toString());
        Cell statCell = new Cell()
                .setBackgroundColor(GOLD_LIGHT)
                .setBorder(new SolidBorder(GOLD, 1)).setPadding(10)
                .setTextAlignment(TextAlignment.RIGHT);
        statCell.add(new Paragraph("STATUT").setFontSize(8).setFontColor(SOFT_GRAY).setBold());
        Cell badgeInner = new Cell()
                .setBorder(new SolidBorder(sc, 1.5f))
                .setBackgroundColor(sb)
                .setPaddingLeft(8).setPaddingRight(8).setPaddingTop(4).setPaddingBottom(4);
        badgeInner.add(new Paragraph("✔ " + sinistre.getStatut().toString())
                .setBold().setFontSize(10).setFontColor(sc)
                .setTextAlignment(TextAlignment.CENTER));
        Table badgeTbl = new Table(1).setHorizontalAlignment(HorizontalAlignment.RIGHT);
        badgeTbl.addCell(badgeInner);
        statCell.add(badgeTbl);
        banner.addCell(statCell);
        doc.add(banner);

        addSectionTitle(doc, "ℹ", "INFORMATIONS DU SINISTRE");
        Table t = infoTableWithIcons();
        addIconRow(t, "🔥", "Type", sinistre.getTypeSinistre().toString());
        addIconRow(t, "📅", "Date du sinistre",
                fmt(sinistre.getDateSinistre(), "dd/MM/yyyy"));
        if (sinistre.getDateDeclaration() != null)
            addIconRow(t, "📅", "Date de déclaration",
                    sinistre.getDateDeclaration().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        long delai = sinistre.getDelaiDeclaration() != null
                ? sinistre.getDelaiDeclaration() : 0;
        addIconColorRow(t, "⏱", "Délai de déclaration",
                (delai > 0 ? "+" : "") + delai + " jour(s)",
                delai > 30 ? BURGUNDY : EMERALD);

        if (sinistre.getClient() != null) {
            User cl = sinistre.getClient();
            addIconRow(t, "👤", "Client",
                    cl.getPrenom() + " " + cl.getNom() + " (" + cl.getEmail() + ")");
        }
        if (sinistre.getContrat() != null)
            addIconRow(t, "📄", "Contrat",
                    sinistre.getContrat().getNumeroContrat()
                            + " (" + sinistre.getContrat().getTypeContrat() + ")");

        // ✅ FIX : formater correctement les montants
        addIconRow(t, "💰", "Montant estimé",
                String.format("%.2f DT", sinistre.getMontantEstime()));
        if (sinistre.getMontantIndemnisation() != null)
            addIconColorRow(t, "💵", "Indemnisation",
                    String.format("%.2f DT", sinistre.getMontantIndemnisation()),
                    EMERALD);
        doc.add(t);

        addSectionTitle(doc, "📝", "DESCRIPTION DES FAITS");
        Table descTbl = new Table(1).useAllAvailableWidth()
                .setMarginLeft(30).setMarginRight(30).setMarginBottom(10);
        Cell descCell = new Cell()
                .setBackgroundColor(GOLD_LIGHT)
                .setBorderLeft(new SolidBorder(NAVY, 3))
                .setBorderTop(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderBottom(Border.NO_BORDER)
                .setPadding(12);
        descCell.add(new Paragraph(
                sinistre.getDescription() != null ? sinistre.getDescription() : "—")
                .setFontSize(10).setFontColor(ANTHRACITE));
        descTbl.addCell(descCell);
        doc.add(descTbl);

        if (sinistre.getScoreFraude() != null) {
            addSectionTitle(doc, "🤖", "ANALYSE INTELLIGENTE (IA)");
            Table ia = infoTableWithIcons();
            // ✅ FIX : affichage correct du score (0.28 → 28,0%)
            String pct = String.format("%.1f%%",
                    sinistre.getScoreFraude().doubleValue() * 100);
            addIconColorRow(ia, "📊", "Score de fraude", pct,
                    sinistre.getScoreFraude().doubleValue() > 0.6 ? BURGUNDY : EMERALD);
            String verdict = Boolean.TRUE.equals(sinistre.getFraude())
                    ? "Suspicion de fraude" : "Sinistre légitime";
            addIconColorRow(ia, "⚖", "Verdict", verdict,
                    Boolean.TRUE.equals(sinistre.getFraude()) ? BURGUNDY : EMERALD);
            addIconRow(ia, "📈", "Sinistres antérieurs",
                    String.valueOf(sinistre.getNbSinistresClient() != null
                            ? sinistre.getNbSinistresClient() : 0));
            doc.add(ia);
        }

        addSectionTitle(doc, "👥", "INTERVENANTS");
        Table iv = infoTableWithIcons();
        addIconRow(iv, "👷", "Agent",
                sinistre.getAgent() != null
                        ? sinistre.getAgent().getPrenom() + " "
                        + sinistre.getAgent().getNom() : "Non affecté");
        addIconRow(iv, "🔍", "Expert",
                sinistre.getExpert() != null
                        ? sinistre.getExpert().getPrenom() + " "
                        + sinistre.getExpert().getNom() : "Non affecté");
        doc.add(iv);

        addFooter(doc);
        doc.close();
        return baos.toByteArray();
    }

    // =====================================================
    // 🛠️ UTILITAIRES
    // =====================================================

    private Document buildDoc(ByteArrayOutputStream baos) throws IOException {
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdf);
        doc.setMargins(0, 0, 40, 0);
        return doc;
    }

    // ─────────────────────────────────────────────────────
    // HEADER OR+BLANC — commun Reçu et Attestation
    // ─────────────────────────────────────────────────────
    private void addHeaderOr(Document doc, String titre, String sousTitre) {
//        goldBar(doc, 7);
//        goldBarThin(doc, 1.5f);

        Table h = new Table(UnitValue.createPercentArray(new float[]{1f, 2.2f}))
                .useAllAvailableWidth();

        // Logo gauche blanc
        Cell logo = new Cell().setBackgroundColor(WHITE).setBorder(Border.NO_BORDER)
                .setPadding(16).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER);
        Table shield = new Table(1).setWidth(55)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        Cell sc = new Cell().setBackgroundColor(GOLD)
                .setBorder(new SolidBorder(GOLD, 3))
                .setBorderRadius(new BorderRadius(28))
                .setPadding(8).setTextAlignment(TextAlignment.CENTER);
        sc.add(new Paragraph("L").setBold().setFontSize(22).setFontColor(WHITE)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
        shield.addCell(sc);
        logo.add(shield);
        logo.add(new Paragraph("LifeSure").setBold().setFontSize(15).setFontColor(NAVY)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(4).setMarginBottom(0));
        logo.add(new Paragraph("ASSURANCES").setBold().setFontSize(8).setFontColor(GOLD)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
        logo.add(new Paragraph("Plateforme d'assurance digitale")
                .setItalic().setFontSize(7).setFontColor(SOFT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        h.addCell(logo);

        // ✅ FIX : titre droite BLANC+OR (pas bleu marine)
        Cell title = new Cell().setBackgroundColor(WHITE).setBorder(Border.NO_BORDER)
                .setPaddingTop(30).setPaddingBottom(30).setPaddingLeft(20).setPaddingRight(20)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER);
        title.add(new Paragraph(titre).setBold().setFontSize(22).setFontColor(GOLD)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));
        title.add(new Paragraph("——  " + sousTitre + "  ——")
                .setItalic().setFontSize(9).setFontColor(SOFT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        h.addCell(title);

        doc.add(h);
//        goldBarThin(doc, 1.5f);
//        goldBar(doc, 5);
        doc.add(new Paragraph(" ").setFontSize(4));
    }

    // ─────────────────────────────────────────────────────
    // HEADER ATTESTATION
    // ─────────────────────────────────────────────────────
    private void addHeaderAttestation(Document doc) {
        goldBar(doc, 7);
        goldBarThin(doc, 1.5f);

        Table h = new Table(UnitValue.createPercentArray(new float[]{1f, 2.2f}))
                .useAllAvailableWidth();

        Cell logo = new Cell().setBackgroundColor(WHITE).setBorder(Border.NO_BORDER)
                .setPadding(20).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER);
        Table shield = new Table(1).setWidth(64)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        Cell sc = new Cell().setBackgroundColor(GOLD)
                .setBorder(new SolidBorder(GOLD, 3))
                .setBorderRadius(new BorderRadius(32))
                .setPadding(12).setTextAlignment(TextAlignment.CENTER);
        sc.add(new Paragraph("L").setBold().setFontSize(26).setFontColor(WHITE)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
        shield.addCell(sc);
        logo.add(shield);
        logo.add(new Paragraph("LifeSure").setBold().setFontSize(16).setFontColor(NAVY)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(6).setMarginBottom(0));
        logo.add(new Paragraph("ASSURANCES").setBold().setFontSize(8).setFontColor(GOLD)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
        logo.add(new Paragraph("Plateforme d'assurance digitale")
                .setItalic().setFontSize(7).setFontColor(SOFT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        h.addCell(logo);

        Cell title = new Cell().setBackgroundColor(WHITE).setBorder(Border.NO_BORDER)
                .setPaddingTop(30).setPaddingBottom(30).setPaddingLeft(20).setPaddingRight(20)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER);
        title.add(new Paragraph("ATTESTATION D'ASSURANCE")
                .setBold().setFontSize(22).setFontColor(GOLD)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));
        title.add(new Paragraph("———  DOCUMENT OFFICIEL CERTIFIÉ  ———")
                .setItalic().setFontSize(8).setFontColor(SOFT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        h.addCell(title);

        doc.add(h);
        goldBarThin(doc, 1.5f);
        goldBar(doc, 5);
        doc.add(new Paragraph(" ").setFontSize(4));
    }

    // ─────────────────────────────────────────────────────
    // HEADER SINISTRE
    // ─────────────────────────────────────────────────────
    private void addHeaderSinistre(Document doc) {
        goldBar(doc, 6);

        Table h = new Table(UnitValue.createPercentArray(new float[]{1f, 2f}))
                .useAllAvailableWidth();

        Cell logo = new Cell().setBackgroundColor(WHITE).setBorder(Border.NO_BORDER)
                .setPadding(14).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER);
        Table shield = new Table(1).setWidth(50)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        Cell sc = new Cell().setBackgroundColor(GOLD)
                .setBorder(new SolidBorder(GOLD, 3))
                .setBorderRadius(new BorderRadius(25))
                .setPadding(6).setTextAlignment(TextAlignment.CENTER);
        sc.add(new Paragraph("L").setBold().setFontSize(20).setFontColor(WHITE)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
        shield.addCell(sc);
        logo.add(shield);
        logo.add(new Paragraph("LifeSure").setBold().setFontSize(13).setFontColor(NAVY)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(4).setMarginBottom(0));
        logo.add(new Paragraph("ASSURANCES").setBold().setFontSize(8).setFontColor(GOLD)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
        logo.add(new Paragraph("Plateforme d'assurance digitale")
                .setItalic().setFontSize(7).setFontColor(SOFT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        h.addCell(logo);

        // ✅ FIX : titre droite avec fond blanc + bordure gauche or
        Cell title = new Cell().setBackgroundColor(WHITE).setBorder(Border.NO_BORDER)
                .setBorderLeft(new SolidBorder(GOLD, 4))
                .setPadding(22).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.RIGHT);
        title.add(new Paragraph("RAPPORT DE SINISTRE")
                .setBold().setFontSize(20).setFontColor(GOLD)
                .setTextAlignment(TextAlignment.RIGHT).setMarginBottom(6));
        title.add(new Paragraph("DOCUMENT D'EXPERTISE")
                .setItalic().setFontSize(9).setFontColor(SOFT_GRAY)
                .setTextAlignment(TextAlignment.RIGHT));
        h.addCell(title);

        doc.add(h);
        goldBar(doc, 4);
        doc.add(new Paragraph(" ").setFontSize(6));
    }

    // ─────────────────────────────────────────────────────
    // TITRE SECTION
    // ─────────────────────────────────────────────────────
    private void addSectionTitle(Document doc, String icon, String title) {
        Table t = new Table(UnitValue.createPercentArray(new float[]{0.06f, 1f}))
                .useAllAvailableWidth()
                .setMarginLeft(30).setMarginRight(30).setMarginTop(10).setMarginBottom(4);
//        t.setBorder(new SolidBorder(GOLD, 1));
//        t.setBorderRadius(new BorderRadius(6));
//        t.setBackgroundColor(GOLD_LIGHT);

        Cell ic = new Cell().setBorder(Border.NO_BORDER)
//                .setBackgroundColor(GOLD_LIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(8).setTextAlignment(TextAlignment.CENTER);
        ic.add(new Paragraph(icon).setFontSize(13).setFontColor(GOLD)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
        t.addCell(ic);

        Cell tc = new Cell().setBorder(Border.NO_BORDER)
//                .setBackgroundColor(GOLD_LIGHT)
                .setPaddingTop(9).setPaddingBottom(9).setPaddingLeft(10).setPaddingRight(12)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        tc.add(new Paragraph(title).setBold().setFontSize(11).setFontColor(NAVY));
        t.addCell(tc);
        doc.add(t);
    }

    // ─────────────────────────────────────────────────────
    // TABLES INFO
    // ─────────────────────────────────────────────────────
    private Table infoTable() {
        return new Table(UnitValue.createPercentArray(new float[]{1f, 2f}))
                .useAllAvailableWidth()
                .setMarginLeft(30).setMarginRight(30).setMarginBottom(10);
    }

    private Table infoTableWithIcons() {
        return new Table(UnitValue.createPercentArray(new float[]{0.07f, 1f, 1.6f}))
                .useAllAvailableWidth()
                .setMarginLeft(30).setMarginRight(30).setMarginBottom(10);
    }

    private void addIconRow(Table t, String icon, String label, String value) {
        Cell ic = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setPadding(7).setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        ic.add(new Paragraph(icon).setFontSize(10).setFontColor(GOLD).setMarginBottom(0));
        t.addCell(ic);

        Cell lc = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setPadding(7).setPaddingLeft(4);
        lc.add(new Paragraph(label).setBold().setFontSize(9).setFontColor(SOFT_GRAY));
        t.addCell(lc);

        Cell vc = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setPadding(7);
        vc.add(new Paragraph(value != null ? value : "—")
                .setFontSize(10).setFontColor(ANTHRACITE));
        t.addCell(vc);
    }

    private void addIconColorRow(Table t, String icon, String label,
                                 String value, DeviceRgb color) {
        Cell ic = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setPadding(7).setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        ic.add(new Paragraph(icon).setFontSize(10).setFontColor(GOLD).setMarginBottom(0));
        t.addCell(ic);

        Cell lc = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setPadding(7).setPaddingLeft(4);
        lc.add(new Paragraph(label).setBold().setFontSize(9).setFontColor(SOFT_GRAY));
        t.addCell(lc);

        Cell vc = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setPadding(7);
        vc.add(new Paragraph(value != null ? value : "—")
                .setBold().setFontSize(10).setFontColor(color));
        t.addCell(vc);
    }

    private void addIconStatusRow(Table t, String icon, String label, String statut) {
        DeviceRgb color = getStatutColor(statut);
        DeviceRgb bg    = getStatutBg(statut);

        Cell ic = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setPadding(7).setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        ic.add(new Paragraph(icon).setFontSize(10).setFontColor(GOLD).setMarginBottom(0));
        t.addCell(ic);

        Cell lc = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setPadding(7).setPaddingLeft(4);
        lc.add(new Paragraph(label).setBold().setFontSize(9).setFontColor(SOFT_GRAY));
        t.addCell(lc);

        Cell vc = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setPadding(5);
        Table badge = new Table(1).setWidth(UnitValue.createPercentValue(55));
        Cell bc = new Cell()
                .setBackgroundColor(bg)
                .setBorder(new SolidBorder(color, 1.5f))
                .setBorderRadius(new BorderRadius(4))
                .setPaddingTop(3).setPaddingBottom(3).setPaddingLeft(8).setPaddingRight(8)
                .setTextAlignment(TextAlignment.CENTER);
        bc.add(new Paragraph(statut).setBold().setFontSize(9).setFontColor(color)
                .setTextAlignment(TextAlignment.CENTER));
        badge.addCell(bc);
        vc.add(badge);
        t.addCell(vc);
    }

    private void addRow(Table t, String label, String value) {
        Cell lc = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setPadding(7).setPaddingLeft(4);
        lc.add(new Paragraph(label).setBold().setFontSize(9).setFontColor(SOFT_GRAY));
        t.addCell(lc);

        Cell vc = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setPadding(7);
        vc.add(new Paragraph(value != null ? value : "—")
                .setFontSize(10).setFontColor(ANTHRACITE));
        t.addCell(vc);
    }

    private void addStatusRow(Table t, String label, String statut) {
        DeviceRgb color = getStatutColor(statut);
        DeviceRgb bg    = getStatutBg(statut);

        Cell lc = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setPadding(7).setPaddingLeft(4);
        lc.add(new Paragraph(label).setBold().setFontSize(9).setFontColor(SOFT_GRAY));
        t.addCell(lc);

        Cell vc = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setPadding(5);
        Table badge = new Table(1).setWidth(UnitValue.createPercentValue(45));
        Cell bc = new Cell()
                .setBackgroundColor(bg)
                .setBorder(new SolidBorder(color, 1.5f))
                .setBorderRadius(new BorderRadius(4))
                .setPaddingTop(3).setPaddingBottom(3).setPaddingLeft(8).setPaddingRight(8)
                .setTextAlignment(TextAlignment.CENTER);
        bc.add(new Paragraph("✔ " + statut).setBold().setFontSize(9).setFontColor(color)
                .setTextAlignment(TextAlignment.CENTER));
        badge.addCell(bc);
        vc.add(badge);
        t.addCell(vc);
    }

    private void addMontantBlock(Document doc, String montant) {
        Table mt = new Table(UnitValue.createPercentArray(new float[]{1f, 0.18f}))
                .useAllAvailableWidth()
                .setMarginLeft(30).setMarginRight(30).setMarginTop(15).setMarginBottom(15);

        Cell left = new Cell()
//                .setBackgroundColor(GOLD_LIGHT)
//                .setBorder(new SolidBorder(GOLD, 1.5f))
                .setPadding(18);
        left.add(new Paragraph("MONTANT PAYÉ")
                .setBold().setFontSize(9).setFontColor(GOLD).setMarginBottom(4));
        left.add(new Paragraph(montant)
                .setBold().setFontSize(28).setFontColor(NAVY));
        mt.addCell(left);

        Cell right = new Cell()
//                .setBackgroundColor(GOLD)
//                .setBorder(new SolidBorder(GOLD, 1.5f))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER);
        right.add(new Paragraph("✓")
                .setBold().setFontSize(26).setFontColor(WHITE)
                .setTextAlignment(TextAlignment.CENTER));
        mt.addCell(right);
        doc.add(mt);
    }

    // ✅ FIX : signature compacte sur même page
    private void addSignatureSectionCompact(Document doc, Contrat contrat) {
        doc.add(new Paragraph(" ").setFontSize(3));

        Table st = new Table(UnitValue.createPercentArray(new float[]{1f, 1f}))
                .useAllAvailableWidth()
                .setMarginLeft(30).setMarginRight(30).setMarginBottom(6);

        // Gauche : signature
        Cell left = new Cell().setBorder(Border.NO_BORDER).setPaddingRight(10);
        left.add(new Paragraph("Signature du client")
                .setBold().setFontSize(9).setFontColor(ANTHRACITE).setMarginBottom(4));

        if (contrat.getSignature() != null && !contrat.getSignature().isEmpty()) {
            try {
                String b64 = contrat.getSignature();
                if (b64.contains(",")) b64 = b64.split(",")[1];
                byte[] sigBytes = java.util.Base64.getDecoder().decode(b64);
                left.add(new Image(ImageDataFactory.create(sigBytes))
                        .setWidth(120).setHeight(45)
                        .setHorizontalAlignment(HorizontalAlignment.LEFT));
            } catch (Exception e) {
                left.add(new Paragraph("[Signature]")
                        .setItalic().setFontColor(SOFT_GRAY).setFontSize(9));
            }
        }
        left.add(new Paragraph("✅ SIGNÉ ÉLECTRONIQUEMENT")
                .setBold().setFontSize(7).setFontColor(EMERALD).setMarginTop(3));
        st.addCell(left);

        // Droite : cachet compact
        Cell right = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);
        right.add(new Paragraph("Cachet officiel")
                .setBold().setFontSize(9).setFontColor(ANTHRACITE)
                .setTextAlignment(TextAlignment.RIGHT).setMarginBottom(4));

        Table cachet = new Table(1).setWidth(UnitValue.createPercentValue(80))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT);
        Cell cc = new Cell()
                .setBackgroundColor(GOLD_LIGHT)
                .setBorder(new SolidBorder(GOLD, 2.5f))
                .setBorderRadius(new BorderRadius(60))
                .setPaddingTop(10).setPaddingBottom(10)
                .setPaddingLeft(14).setPaddingRight(14)
                .setTextAlignment(TextAlignment.CENTER);

        cc.add(new Paragraph("★ ★ ★")
                .setFontSize(8).setFontColor(GOLD)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
        cc.add(new Paragraph("LIFESURE\nASSURANCES")
                .setBold().setFontSize(10).setFontColor(NAVY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(3));
        cc.add(new Paragraph("Mohamed Boukhris")
                .setItalic().setBold().setFontSize(8).setFontColor(NAVY)
                .setTextAlignment(TextAlignment.CENTER));
        cc.add(new Paragraph("Directeur Général")
                .setItalic().setFontSize(7).setFontColor(SOFT_GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
        cc.add(new Paragraph("TUNIS – TUNISIE")
                .setBold().setFontSize(7).setFontColor(NAVY)
                .setTextAlignment(TextAlignment.CENTER));
        cc.add(new Paragraph("★ ★ ★")
                .setFontSize(8).setFontColor(GOLD)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(2));

        cachet.addCell(cc);
        right.add(cachet);
        st.addCell(right);
        doc.add(st);
    }

    // ─────────────────────────────────────────────────────
    // FOOTER
    // ─────────────────────────────────────────────────────
    private void addFooter(Document doc) {
        doc.add(new Paragraph(" ").setFontSize(3));
        goldBar(doc, 2);

        Table ft = new Table(UnitValue.createPercentArray(new float[]{1f, 1f, 1f}))
                .useAllAvailableWidth().setMarginTop(4);

        Cell c1 = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
        c1.add(new Paragraph()
                .add(new Text("📍  ").setFontSize(9).setFontColor(GOLD))
                .add(new Text("LifeSure Assurances\n").setBold().setFontSize(9).setFontColor(NAVY))
                .add(new Text("    Tunis, Tunisie").setFontSize(8).setFontColor(SOFT_GRAY)));
        ft.addCell(c1);

        Cell c2 = new Cell().setBorder(Border.NO_BORDER).setPadding(6)
                .setTextAlignment(TextAlignment.CENTER);
        c2.add(new Paragraph()
                .add(new Text("✉  ").setFontSize(9).setFontColor(GOLD))
                .add(new Text("contact@lifesure.tn\n").setFontSize(8).setFontColor(SOFT_GRAY))
                .add(new Text("📞  ").setFontSize(9).setFontColor(GOLD))
                .add(new Text("+216 71 123 456").setFontSize(8).setFontColor(SOFT_GRAY))
                .setTextAlignment(TextAlignment.CENTER));
        ft.addCell(c2);

        Cell c3 = new Cell().setBorder(Border.NO_BORDER).setPadding(6)
                .setTextAlignment(TextAlignment.RIGHT);
        c3.add(new Paragraph()
                .add(new Text("🌐  ").setFontSize(9).setFontColor(GOLD))
                .add(new Text("www.lifesure.tn").setFontSize(8).setFontColor(SOFT_GRAY))
                .setTextAlignment(TextAlignment.RIGHT));
        ft.addCell(c3);
        doc.add(ft);

        doc.add(new Paragraph(
                "Document généré le "
                        + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        + " - LifeSure © 2026")
                .setFontSize(7).setItalic().setFontColor(WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(GOLD)
                .setPadding(5));
    }

    // ─────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────
    private void goldBar(Document doc, float h) {
        Table t = new Table(1).useAllAvailableWidth();
        Cell c = new Cell().setBackgroundColor(GOLD).setBorder(Border.NO_BORDER).setHeight(h);
        c.add(new Paragraph(""));
        t.addCell(c);
        doc.add(t);
    }

    private void goldBarThin(Document doc, float h) {
        Table t = new Table(1).useAllAvailableWidth()
                .setMarginLeft(18).setMarginRight(18)
                .setMarginTop(1).setMarginBottom(1);
        Cell c = new Cell().setBackgroundColor(GOLD).setBorder(Border.NO_BORDER).setHeight(h);
        c.add(new Paragraph(""));
        t.addCell(c);
        doc.add(t);
    }

    private Paragraph dateLine(String prefix) {
        return new Paragraph()
                .add(new Text(prefix).setFontColor(SOFT_GRAY).setFontSize(9))
                .add(new Text(LocalDate.now()
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                        .setBold().setFontColor(ANTHRACITE).setFontSize(9))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(8).setMarginBottom(12);
    }

    private String fmt(java.time.LocalDate d, String pattern) {
        return d != null ? d.format(DateTimeFormatter.ofPattern(pattern)) : "—";
    }

    private DeviceRgb getStatutColor(String s) {
        if (s == null) return SOFT_GRAY;
        switch (s.toUpperCase()) {
            case "ACTIF": case "ACCEPTE": case "REUSSI": return EMERALD;
            case "REFUSE": case "EXPIRE":                return BURGUNDY;
            case "EN_COURS":                             return CYAN;
            default:                                     return SOFT_GRAY;
        }
    }

    private DeviceRgb getStatutBg(String s) {
        if (s == null) return new DeviceRgb(245, 245, 245);
        switch (s.toUpperCase()) {
            case "ACTIF": case "ACCEPTE": case "REUSSI":
                return new DeviceRgb(220, 252, 231);
            case "REFUSE": case "EXPIRE":
                return new DeviceRgb(254, 226, 226);
            case "EN_COURS":
                return new DeviceRgb(207, 250, 254);
            default:
                return new DeviceRgb(245, 245, 245);
        }
    }
}



































//
//package com.assurance.Service;
//
//import com.assurance.Entity.Contrat;
//import com.assurance.Entity.Paiement;
//import com.assurance.Entity.Sinistre;
//import com.assurance.Entity.User;
//import com.itextpdf.io.image.ImageDataFactory;
//import com.itextpdf.kernel.colors.DeviceRgb;
//import com.itextpdf.kernel.geom.PageSize;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.layout.Document;
//import com.itextpdf.layout.borders.Border;
//import com.itextpdf.layout.borders.SolidBorder;
//import com.itextpdf.layout.element.*;
//import com.itextpdf.layout.properties.BorderRadius;
//import com.itextpdf.layout.properties.HorizontalAlignment;
//import com.itextpdf.layout.properties.TextAlignment;
//import com.itextpdf.layout.properties.UnitValue;
//import com.itextpdf.layout.properties.VerticalAlignment;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//
//@Service
//@RequiredArgsConstructor
//public class PdfService {
//
//    // ── Palette ──────────────────────────────────────────
//    private static final DeviceRgb NAVY        = new DeviceRgb(26,   43,  94);
//    private static final DeviceRgb GOLD        = new DeviceRgb(212, 170,  30);
//    private static final DeviceRgb GOLD_DARK   = new DeviceRgb(180, 140,  10);
//    private static final DeviceRgb GOLD_LIGHT  = new DeviceRgb(255, 248, 220);
//    private static final DeviceRgb CREAM       = new DeviceRgb(252, 249, 240);
//    private static final DeviceRgb WHITE       = new DeviceRgb(255, 255, 255);
//    private static final DeviceRgb ANTHRACITE  = new DeviceRgb( 40,  40,  40);
//    private static final DeviceRgb SOFT_GRAY   = new DeviceRgb(130, 130, 130);
//    private static final DeviceRgb LIGHT_GRAY  = new DeviceRgb(230, 230, 230);
//    private static final DeviceRgb EMERALD     = new DeviceRgb(  4, 120,  87);
//    private static final DeviceRgb EMERALD_BG  = new DeviceRgb(220, 252, 231);
//    private static final DeviceRgb BURGUNDY    = new DeviceRgb(153,  27,  27);
//    private static final DeviceRgb BURGUNDY_BG = new DeviceRgb(254, 226, 226);
//    private static final DeviceRgb CYAN        = new DeviceRgb( 54, 185, 204);
//    private static final DeviceRgb CYAN_BG     = new DeviceRgb(207, 250, 254);
//
//    private final QrCodeService qrCodeService;
//
//    @Value("${app.frontend.url:http://localhost:4200}")
//    private String frontendUrl;
//
//    // =====================================================
//    // 📄 1. REÇU DE PAIEMENT
//    // =====================================================
//    public byte[] genererRecuPaiement(Paiement paiement) throws IOException {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        Document doc = buildDoc(baos);
//
//        addHeaderRecu(doc);
//
//        // ── Date row ──
//        Table dateRow = new Table(UnitValue.createPercentArray(new float[]{1f, 1f}))
//                .useAllAvailableWidth()
//                .setMarginLeft(30).setMarginRight(30).setMarginTop(10).setMarginBottom(14);
//        Cell dl = new Cell().setBorder(Border.NO_BORDER);
//        dl.add(new Paragraph()
//                .add(new Text("Émis le ").setFontColor(SOFT_GRAY).setFontSize(9))
//                .add(new Text(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
//                        .setBold().setFontColor(ANTHRACITE).setFontSize(9)));
//        dateRow.addCell(dl);
//        Cell dr = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
//        dr.add(new Paragraph()
//                .add(new Text("📅  Émis le\n").setFontSize(8).setFontColor(SOFT_GRAY))
//                .add(new Text(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
//                        .setFontSize(8).setFontColor(SOFT_GRAY))
//                .setTextAlignment(TextAlignment.RIGHT));
//        dateRow.addCell(dr);
//        doc.add(dateRow);
//
//        // ── Section paiement ──
//        addSectionTitle(doc, "📋", "INFORMATIONS DU PAIEMENT");
//        Table t1 = infoTable();
//        addIconRow(t1, "🔖", "N° Reçu",          "REC-" + paiement.getId());
//        addIconRow(t1, "📅", "Date de paiement",  fmt(paiement.getDatePaiement(), "dd/MM/yyyy"));
//        addIconRow(t1, "💳", "Mode de paiement",  paiement.getModePaiement().toString().replace("_", " "));
//        addIconStatusRow(t1, "✅", "Statut",       paiement.getStatut().toString());
//        doc.add(t1);
//
//        // ── Section contrat ──
//        Contrat c = paiement.getContrat();
//        if (c != null) {
//            addSectionTitle(doc, "🛡️", "DÉTAILS DU CONTRAT");
//            Table t2 = infoTable();
//            addIconRow(t2, "📄", "Numéro contrat",   c.getNumeroContrat());
//            addIconRow(t2, "🏷️", "Type d'assurance", c.getTypeContrat().toString());
//            if (c.getClient() != null) {
//                addIconRow(t2, "👤", "Assuré",
//                        c.getClient().getPrenom() + " " + c.getClient().getNom());
//                addIconRow(t2, "✉️", "Email", c.getClient().getEmail());
//            }
//            doc.add(t2);
//        }
//
//        // ── Montant ──
//        String montant = paiement.getMontant() != null
//                ? String.format("%.2f DT", paiement.getMontant()) : "0.00 DT";
//        addMontantBlock(doc, montant);
//
//        // ── Merci ──
//        doc.add(new Paragraph()
//                .add(new Text("♥  Merci pour votre confiance.\n")
//                        .setBold().setFontSize(11).setFontColor(NAVY))
//                .add(new Text("Ce reçu fait foi de votre paiement.")
//                        .setItalic().setFontSize(9).setFontColor(SOFT_GRAY))
//                .setTextAlignment(TextAlignment.CENTER)
//                .setMarginTop(14).setMarginBottom(20));
//
//        addFooter(doc);
//        doc.close();
//        return baos.toByteArray();
//    }
//
//    // =====================================================
//    // 🛡️ 2. ATTESTATION D'ASSURANCE
//    // =====================================================
//    public byte[] genererAttestation(Contrat contrat) throws IOException {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        Document doc = buildDoc(baos);
//
//        addHeaderAttestation(doc);
//
//        doc.add(new Paragraph()
//                .add(new Text("Émise le ").setFontColor(SOFT_GRAY).setFontSize(9))
//                .add(new Text(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
//                        .setBold().setFontColor(ANTHRACITE).setFontSize(9))
//                .setTextAlignment(TextAlignment.CENTER)
//                .setMarginTop(10).setMarginBottom(12));
//
//        User client = contrat.getClient();
//        String prenom = client != null ? client.getPrenom() : "—";
//        String nom    = client != null ? client.getNom()    : "—";
//
//        doc.add(new Paragraph()
//                .add(new Text("Nous, ").setFontSize(10).setFontColor(ANTHRACITE))
//                .add(new Text("LifeSure Assurances").setBold().setFontColor(GOLD).setFontSize(10))
//                .add(new Text(", attestons par la présente que :").setFontSize(10).setFontColor(ANTHRACITE))
//                .setMarginLeft(30).setMarginRight(30).setMarginBottom(10));
//
//        // Bloc client
//        addClientBlock(doc, client, prenom, nom);
//
//        doc.add(new Paragraph("est assuré(e) auprès de notre compagnie selon les modalités suivantes :")
//                .setFontSize(10).setFontColor(ANTHRACITE)
//                .setMarginLeft(30).setMarginRight(30).setMarginBottom(10));
//
//        addSectionTitle(doc, "🛡️", "DÉTAILS DU CONTRAT");
//
//        Table layout = new Table(UnitValue.createPercentArray(new float[]{1.65f, 1f}))
//                .useAllAvailableWidth().setMarginLeft(30).setMarginRight(30).setMarginBottom(12);
//
//        Cell dataCol = new Cell().setBorder(Border.NO_BORDER).setPaddingRight(8);
//        Table infoTbl = infoTable().setMarginLeft(0).setMarginRight(0);
//        addIconRow(infoTbl, "📄", "N° de contrat",    contrat.getNumeroContrat());
//        addIconRow(infoTbl, "🏷️", "Type d'assurance", contrat.getTypeContrat().toString());
//        addIconRow(infoTbl, "📅", "Date de début",    fmt(contrat.getDateDebut(), "dd/MM/yyyy"));
//        addIconRow(infoTbl, "📅", "Date de fin",      fmt(contrat.getDateFin(),   "dd/MM/yyyy"));
//        addIconRow(infoTbl, "💰", "Prime mensuelle",
//                String.format("%.2f DT", contrat.getPrimeMensuelle()));
//        addIconStatusRow(infoTbl, "✅", "Statut", contrat.getStatut().toString());
//
//        switch (contrat.getTypeContrat()) {
//            case AUTO:
//                if (contrat.getVehiculeMarque() != null)
//                    addIconRow(infoTbl, "🚗", "Véhicule",
//                            contrat.getVehiculeMarque() + " " + contrat.getVehiculeModele()
//                                    + " (" + contrat.getVehiculeImmatriculation() + ")");
//                break;
//            case HABITATION:
//                if (contrat.getAdresseBien() != null) {
//                    addIconRow(infoTbl, "📍", "Adresse", contrat.getAdresseBien());
//                    addIconRow(infoTbl, "🏠", "Type",
//                            contrat.getTypeBien() + " (" + contrat.getSuperficieBien() + " m²)");
//                }
//                break;
//            case VOYAGE:
//                if (contrat.getDestination() != null) {
//                    addIconRow(infoTbl, "✈", "Destination", contrat.getDestination());
//                    addIconRow(infoTbl, "⏱", "Durée", contrat.getDureeVoyage() + " jours");
//                }
//                break;
//            case SANTE:
//                if (contrat.getPlafondAnnuel() != null)
//                    addIconRow(infoTbl, "💊", "Plafond annuel",
//                            String.format("%.2f DT", contrat.getPlafondAnnuel()));
//                break;
//        }
//        dataCol.add(infoTbl);
//        layout.addCell(dataCol);
//
//        // QR code
//        Cell qrCol = new Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.TOP);
//        try {
//            String url = frontendUrl + "/verifier-contrat/" + contrat.getNumeroContrat();
//            byte[] qrBytes = qrCodeService.generateQrCode(url, 200);
//            Table qrBox = new Table(1).useAllAvailableWidth();
//            Cell qrCell = new Cell()
//                    .setBackgroundColor(CREAM)
//                    .setBorder(new SolidBorder(GOLD, 1.5f))
//                    .setBorderRadius(new BorderRadius(8))
//                    .setPadding(8).setTextAlignment(TextAlignment.CENTER);
//            qrCell.add(new Paragraph("🛡  VÉRIFICATION\nD'AUTHENTICITÉ")
//                    .setBold().setFontSize(7).setFontColor(NAVY)
//                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));
//            qrCell.add(new Image(ImageDataFactory.create(qrBytes))
//                    .setWidth(100).setHeight(100)
//                    .setHorizontalAlignment(HorizontalAlignment.CENTER));
//            qrCell.add(new Paragraph("Scannez ce QR Code pour\nvérifier l'authenticité")
//                    .setFontSize(7).setItalic().setFontColor(SOFT_GRAY)
//                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(6));
//            qrBox.addCell(qrCell);
//            qrCol.add(qrBox);
//        } catch (Exception e) {
//            qrCol.add(new Paragraph("[QR]").setFontColor(SOFT_GRAY).setFontSize(8));
//        }
//        layout.addCell(qrCol);
//        doc.add(layout);
//
//        // Mention légale
//        addMentionLegale(doc);
//
//        addSignatureSectionCompact(doc, contrat);
//        addFooter(doc);
//        doc.close();
//        return baos.toByteArray();
//    }
//
//    // =====================================================
//    // 📋 3. RAPPORT DE SINISTRE
//    // =====================================================
//    public byte[] genererRapportSinistre(Sinistre sinistre) throws IOException {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        Document doc = buildDoc(baos);
//
//        addHeaderSinistre(doc);
//
//        // Banner sinistre n° + statut
//        Table banner = new Table(UnitValue.createPercentArray(new float[]{1f, 1f}))
//                .useAllAvailableWidth().setMarginLeft(30).setMarginRight(30).setMarginBottom(14);
//
//        Cell numCell = new Cell()
//                .setBackgroundColor(CREAM)
//                .setBorder(new SolidBorder(GOLD, 1))
//                .setBorderRadius(new BorderRadius(6))
//                .setPadding(10);
//        numCell.add(new Paragraph("SINISTRE N°")
//                .setFontSize(8).setFontColor(SOFT_GRAY).setBold());
//        numCell.add(new Paragraph(sinistre.getNumeroSinistre())
//                .setBold().setFontSize(12).setFontColor(NAVY));
//        banner.addCell(numCell);
//
//        DeviceRgb sc = getStatutColor(sinistre.getStatut().toString());
//        DeviceRgb sb = getStatutBg(sinistre.getStatut().toString());
//        Cell statCell = new Cell()
//                .setBackgroundColor(CREAM)
//                .setBorder(new SolidBorder(GOLD, 1))
//                .setBorderRadius(new BorderRadius(6))
//                .setPadding(10).setTextAlignment(TextAlignment.RIGHT);
//        statCell.add(new Paragraph("STATUT").setFontSize(8).setFontColor(SOFT_GRAY).setBold());
//        Table badgeTbl = new Table(1).setHorizontalAlignment(HorizontalAlignment.RIGHT);
//        Cell badgeInner = new Cell()
//                .setBorder(new SolidBorder(sc, 1.5f))
//                .setBorderRadius(new BorderRadius(4))
//                .setBackgroundColor(sb)
//                .setPaddingLeft(8).setPaddingRight(8).setPaddingTop(4).setPaddingBottom(4);
//        badgeInner.add(new Paragraph("✔ " + sinistre.getStatut().toString())
//                .setBold().setFontSize(10).setFontColor(sc)
//                .setTextAlignment(TextAlignment.CENTER));
//        badgeTbl.addCell(badgeInner);
//        statCell.add(badgeTbl);
//        banner.addCell(statCell);
//        doc.add(banner);
//
//        addSectionTitle(doc, "ℹ", "INFORMATIONS DU SINISTRE");
//        Table t = infoTable();
//        addIconRow(t, "🔥", "Type", sinistre.getTypeSinistre().toString());
//        addIconRow(t, "📅", "Date du sinistre", fmt(sinistre.getDateSinistre(), "dd/MM/yyyy"));
//        if (sinistre.getDateDeclaration() != null)
//            addIconRow(t, "📅", "Date de déclaration",
//                    sinistre.getDateDeclaration().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
//
//        long delai = sinistre.getDelaiDeclaration() != null ? sinistre.getDelaiDeclaration() : 0;
//        addIconColorRow(t, "⏱", "Délai de déclaration",
//                (delai > 0 ? "+" : "") + delai + " jour(s)",
//                delai > 30 ? BURGUNDY : EMERALD);
//
//        if (sinistre.getClient() != null) {
//            User cl = sinistre.getClient();
//            addIconRow(t, "👤", "Client",
//                    cl.getPrenom() + " " + cl.getNom() + " (" + cl.getEmail() + ")");
//        }
//        if (sinistre.getContrat() != null)
//            addIconRow(t, "📄", "Contrat",
//                    sinistre.getContrat().getNumeroContrat()
//                            + " (" + sinistre.getContrat().getTypeContrat() + ")");
//
//        addIconRow(t, "💰", "Montant estimé",
//                String.format("%.2f DT", sinistre.getMontantEstime()));
//        if (sinistre.getMontantIndemnisation() != null)
//            addIconColorRow(t, "💵", "Indemnisation",
//                    String.format("%.2f DT", sinistre.getMontantIndemnisation()), EMERALD);
//        doc.add(t);
//
//        addSectionTitle(doc, "📝", "DESCRIPTION DES FAITS");
//        Table descTbl = new Table(1).useAllAvailableWidth()
//                .setMarginLeft(30).setMarginRight(30).setMarginBottom(10);
//        Cell descCell = new Cell()
//                .setBackgroundColor(CREAM)
//                .setBorderLeft(new SolidBorder(GOLD, 3))
//                .setBorderTop(Border.NO_BORDER)
//                .setBorderRight(Border.NO_BORDER)
//                .setBorderBottom(Border.NO_BORDER)
//                .setPadding(12);
//        descCell.add(new Paragraph(sinistre.getDescription() != null ? sinistre.getDescription() : "—")
//                .setFontSize(10).setFontColor(ANTHRACITE));
//        descTbl.addCell(descCell);
//        doc.add(descTbl);
//
//        if (sinistre.getScoreFraude() != null) {
//            addSectionTitle(doc, "🤖", "ANALYSE INTELLIGENTE (IA)");
//            Table ia = infoTable();
//            String pct = String.format("%.1f%%", sinistre.getScoreFraude().doubleValue() * 100);
//            addIconColorRow(ia, "📊", "Score de fraude", pct,
//                    sinistre.getScoreFraude().doubleValue() > 0.6 ? BURGUNDY : EMERALD);
//            String verdict = Boolean.TRUE.equals(sinistre.getFraude())
//                    ? "Suspicion de fraude" : "Sinistre légitime";
//            addIconColorRow(ia, "⚖", "Verdict", verdict,
//                    Boolean.TRUE.equals(sinistre.getFraude()) ? BURGUNDY : EMERALD);
//            addIconRow(ia, "📈", "Sinistres antérieurs",
//                    String.valueOf(sinistre.getNbSinistresClient() != null
//                            ? sinistre.getNbSinistresClient() : 0));
//            doc.add(ia);
//        }
//
//        addSectionTitle(doc, "👥", "INTERVENANTS");
//        Table iv = infoTable();
//        addIconRow(iv, "👷", "Agent",
//                sinistre.getAgent() != null
//                        ? sinistre.getAgent().getPrenom() + " " + sinistre.getAgent().getNom()
//                        : "Non affecté");
//        addIconRow(iv, "🔍", "Expert",
//                sinistre.getExpert() != null
//                        ? sinistre.getExpert().getPrenom() + " " + sinistre.getExpert().getNom()
//                        : "Non affecté");
//        doc.add(iv);
//
//        addFooter(doc);
//        doc.close();
//        return baos.toByteArray();
//    }
//
//    // =====================================================
//    // 🛠️ UTILITAIRES — DOCUMENT
//    // =====================================================
//
//    private Document buildDoc(ByteArrayOutputStream baos) throws IOException {
//        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
//        Document doc = new Document(pdf, PageSize.A4);
//        doc.setMargins(0, 0, 40, 0);
//        return doc;
//    }
//
//    // ─────────────────────────────────────────────────────
//    // HEADER REÇU DE PAIEMENT
//    // Style : fond crème, bouclier doré à gauche, titre centré
//    // ─────────────────────────────────────────────────────
//    private void addHeaderRecu(Document doc) {
//        // Bande dorée fine en haut
//        goldBar(doc, 6);
//
//        // Zone header fond crème
//        Table h = new Table(UnitValue.createPercentArray(new float[]{1f, 2f}))
//                .useAllAvailableWidth()
//                .setBackgroundColor(CREAM);
//
//        // -- Logo + bouclier --
//        Cell logo = new Cell()
//                .setBackgroundColor(CREAM)
//                .setBorder(Border.NO_BORDER)
//                .setPadding(20)
//                .setVerticalAlignment(VerticalAlignment.MIDDLE)
//                .setTextAlignment(TextAlignment.CENTER);
//
//        // Bouclier
//        addShieldToCell(logo, 60, 28);
//
//        logo.add(new Paragraph("LifeSure")
//                .setBold().setFontSize(16).setFontColor(NAVY)
//                .setTextAlignment(TextAlignment.CENTER).setMarginTop(6).setMarginBottom(0));
//        logo.add(new Paragraph("ASSURANCES")
//                .setBold().setFontSize(8).setFontColor(GOLD)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
//        logo.add(new Paragraph("Plateforme d'assurance digitale")
//                .setItalic().setFontSize(7).setFontColor(SOFT_GRAY)
//                .setTextAlignment(TextAlignment.CENTER));
//        h.addCell(logo);
//
//        // -- Titre --
//        Cell title = new Cell()
//                .setBackgroundColor(CREAM)
//                .setBorder(Border.NO_BORDER)
//                .setPaddingTop(28).setPaddingBottom(28).setPaddingLeft(20).setPaddingRight(30)
//                .setVerticalAlignment(VerticalAlignment.MIDDLE)
//                .setTextAlignment(TextAlignment.CENTER);
//        title.add(new Paragraph("REÇU DE PAIEMENT")
//                .setBold().setFontSize(24).setFontColor(GOLD)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));
//        title.add(new Paragraph("——  DOCUMENT OFFICIEL  ——")
//                .setItalic().setFontSize(9).setFontColor(SOFT_GRAY)
//                .setTextAlignment(TextAlignment.CENTER));
//
//        // Petit badge décoratif
//        title.add(new Paragraph("🛡")
//                .setFontSize(14).setFontColor(GOLD)
//                .setTextAlignment(TextAlignment.CENTER).setMarginTop(6));
//        h.addCell(title);
//
//        doc.add(h);
//
//        // Bande dorée fine en bas du header
//        goldBar(doc, 3);
//        doc.add(new Paragraph(" ").setFontSize(3));
//    }
//
//    // ─────────────────────────────────────────────────────
//    // HEADER ATTESTATION
//    // ─────────────────────────────────────────────────────
//    private void addHeaderAttestation(Document doc) {
//        goldBar(doc, 6);
//
//        Table h = new Table(UnitValue.createPercentArray(new float[]{1f, 2f}))
//                .useAllAvailableWidth()
//                .setBackgroundColor(CREAM);
//
//        Cell logo = new Cell()
//                .setBackgroundColor(CREAM)
//                .setBorder(Border.NO_BORDER)
//                .setPadding(22)
//                .setVerticalAlignment(VerticalAlignment.MIDDLE)
//                .setTextAlignment(TextAlignment.CENTER);
//        addShieldToCell(logo, 64, 30);
//        logo.add(new Paragraph("LifeSure")
//                .setBold().setFontSize(16).setFontColor(NAVY)
//                .setTextAlignment(TextAlignment.CENTER).setMarginTop(6).setMarginBottom(0));
//        logo.add(new Paragraph("ASSURANCES")
//                .setBold().setFontSize(8).setFontColor(GOLD)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
//        logo.add(new Paragraph("Plateforme d'assurance digitale")
//                .setItalic().setFontSize(7).setFontColor(SOFT_GRAY)
//                .setTextAlignment(TextAlignment.CENTER));
//        h.addCell(logo);
//
//        Cell title = new Cell()
//                .setBackgroundColor(CREAM)
//                .setBorder(Border.NO_BORDER)
//                .setPaddingTop(28).setPaddingBottom(28).setPaddingLeft(20).setPaddingRight(30)
//                .setVerticalAlignment(VerticalAlignment.MIDDLE)
//                .setTextAlignment(TextAlignment.CENTER);
//        title.add(new Paragraph("ATTESTATION D'ASSURANCE")
//                .setBold().setFontSize(22).setFontColor(GOLD)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));
//        title.add(new Paragraph("———  DOCUMENT OFFICIEL CERTIFIÉ  ———")
//                .setItalic().setFontSize(8).setFontColor(SOFT_GRAY)
//                .setTextAlignment(TextAlignment.CENTER));
//        title.add(new Paragraph("🛡").setFontSize(14).setFontColor(GOLD)
//                .setTextAlignment(TextAlignment.CENTER).setMarginTop(6));
//        h.addCell(title);
//
//        doc.add(h);
//        goldBar(doc, 3);
//        doc.add(new Paragraph(" ").setFontSize(3));
//    }
//
//    // ─────────────────────────────────────────────────────
//    // HEADER SINISTRE
//    // ─────────────────────────────────────────────────────
//    private void addHeaderSinistre(Document doc) {
//        goldBar(doc, 6);
//
//        Table h = new Table(UnitValue.createPercentArray(new float[]{1f, 2f}))
//                .useAllAvailableWidth()
//                .setBackgroundColor(CREAM);
//
//        Cell logo = new Cell()
//                .setBackgroundColor(CREAM)
//                .setBorder(Border.NO_BORDER)
//                .setPadding(18)
//                .setVerticalAlignment(VerticalAlignment.MIDDLE)
//                .setTextAlignment(TextAlignment.CENTER);
//        addShieldToCell(logo, 54, 24);
//        logo.add(new Paragraph("LifeSure")
//                .setBold().setFontSize(14).setFontColor(NAVY)
//                .setTextAlignment(TextAlignment.CENTER).setMarginTop(5).setMarginBottom(0));
//        logo.add(new Paragraph("ASSURANCES")
//                .setBold().setFontSize(8).setFontColor(GOLD)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
//        logo.add(new Paragraph("Plateforme d'assurance digitale")
//                .setItalic().setFontSize(7).setFontColor(SOFT_GRAY)
//                .setTextAlignment(TextAlignment.CENTER));
//        h.addCell(logo);
//
//        Cell title = new Cell()
//                .setBackgroundColor(CREAM)
//                .setBorder(Border.NO_BORDER)
//                .setBorderLeft(new SolidBorder(GOLD, 4))
//                .setPadding(24)
//                .setVerticalAlignment(VerticalAlignment.MIDDLE)
//                .setTextAlignment(TextAlignment.RIGHT);
//        title.add(new Paragraph("RAPPORT DE SINISTRE")
//                .setBold().setFontSize(22).setFontColor(GOLD)
//                .setTextAlignment(TextAlignment.RIGHT).setMarginBottom(4));
//        title.add(new Paragraph("DOCUMENT D'EXPERTISE")
//                .setItalic().setFontSize(9).setFontColor(SOFT_GRAY)
//                .setTextAlignment(TextAlignment.RIGHT));
//        h.addCell(title);
//
//        doc.add(h);
//        goldBar(doc, 3);
//        doc.add(new Paragraph(" ").setFontSize(6));
//    }
//
//    // ─────────────────────────────────────────────────────
//    // BOUCLIER DORÉ (logo LifeSure)
//    // ─────────────────────────────────────────────────────
//    private void addShieldToCell(Cell parent, float size, float fontSize) {
//        // Cercle doré avec "L" blanc — simulé avec une table ronde
//        Table shieldOuter = new Table(1)
//                .setWidth(size)
//                .setHorizontalAlignment(HorizontalAlignment.CENTER);
//        Cell outerCell = new Cell()
//                .setBackgroundColor(GOLD_DARK)
//                .setBorder(new SolidBorder(GOLD_DARK, 2))
//                .setBorderRadius(new BorderRadius(size / 2))
//                .setPadding(4)
//                .setTextAlignment(TextAlignment.CENTER);
//
//        Table shieldInner = new Table(1)
//                .setWidth(size - 12)
//                .setHorizontalAlignment(HorizontalAlignment.CENTER);
//        Cell innerCell = new Cell()
//                .setBackgroundColor(GOLD)
//                .setBorder(new SolidBorder(GOLD, 1))
//                .setBorderRadius(new BorderRadius((size - 12) / 2))
//                .setPadding(6)
//                .setTextAlignment(TextAlignment.CENTER);
//        innerCell.add(new Paragraph("L")
//                .setBold().setFontSize(fontSize).setFontColor(WHITE)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
//        shieldInner.addCell(innerCell);
//        outerCell.add(shieldInner);
//        shieldOuter.addCell(outerCell);
//        parent.add(shieldOuter);
//    }
//
//    // ─────────────────────────────────────────────────────
//    // TITRE DE SECTION — style bande gauche dorée
//    // ─────────────────────────────────────────────────────
//    private void addSectionTitle(Document doc, String icon, String title) {
//        Table t = new Table(UnitValue.createPercentArray(new float[]{0.055f, 1f}))
//                .useAllAvailableWidth()
//                .setMarginLeft(30).setMarginRight(30).setMarginTop(12).setMarginBottom(6)
//                .setBackgroundColor(GOLD)
//                .setBorderRadius(new BorderRadius(5));
//
//        // Icône carrée sur fond or foncé
//        Cell ic = new Cell()
//                .setBackgroundColor(GOLD_DARK)
//                .setBorder(Border.NO_BORDER)
//                .setBorderRadius(new BorderRadius(5))
//                .setPadding(8)
//                .setVerticalAlignment(VerticalAlignment.MIDDLE)
//                .setTextAlignment(TextAlignment.CENTER);
//        ic.add(new Paragraph(icon)
//                .setFontSize(11).setFontColor(WHITE)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
//        t.addCell(ic);
//
//        // Texte blanc sur fond doré
//        Cell tc = new Cell()
//                .setBackgroundColor(GOLD)
//                .setBorder(Border.NO_BORDER)
//                .setPaddingTop(8).setPaddingBottom(8).setPaddingLeft(12).setPaddingRight(12)
//                .setVerticalAlignment(VerticalAlignment.MIDDLE);
//        tc.add(new Paragraph(title)
//                .setBold().setFontSize(11).setFontColor(WHITE));
//        t.addCell(tc);
//
//        doc.add(t);
//    }
//
//    // ─────────────────────────────────────────────────────
//    // TABLES INFO
//    // ─────────────────────────────────────────────────────
//    private Table infoTable() {
//        return new Table(UnitValue.createPercentArray(new float[]{0.06f, 1f, 1.6f}))
//                .useAllAvailableWidth()
//                .setMarginLeft(30).setMarginRight(30).setMarginBottom(6);
//    }
//
//    // Ligne standard avec icône carrée
//    private void addIconRow(Table t, String icon, String label, String value) {
//        // Icône dans carré doré arrondi
//        Cell ic = new Cell()
//                .setBorder(Border.NO_BORDER)
//                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
//                .setPadding(6)
//                .setVerticalAlignment(VerticalAlignment.MIDDLE)
//                .setTextAlignment(TextAlignment.CENTER);
//        Table iconBox = new Table(1).setWidth(22).setHorizontalAlignment(HorizontalAlignment.CENTER);
//        Cell ib = new Cell()
//                .setBackgroundColor(GOLD_LIGHT)
//                .setBorder(new SolidBorder(GOLD, 0.5f))
//                .setBorderRadius(new BorderRadius(4))
//                .setPadding(3).setTextAlignment(TextAlignment.CENTER);
//        ib.add(new Paragraph(icon).setFontSize(9).setFontColor(GOLD)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
//        iconBox.addCell(ib);
//        ic.add(iconBox);
//        t.addCell(ic);
//
//        Cell lc = new Cell()
//                .setBorder(Border.NO_BORDER)
//                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
//                .setPadding(8).setPaddingLeft(6);
//        lc.add(new Paragraph(label).setBold().setFontSize(9).setFontColor(SOFT_GRAY));
//        t.addCell(lc);
//
//        Cell vc = new Cell()
//                .setBorder(Border.NO_BORDER)
//                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
//                .setPadding(8);
//        vc.add(new Paragraph(value != null ? value : "—")
//                .setFontSize(10).setFontColor(ANTHRACITE));
//        t.addCell(vc);
//    }
//
//    private void addIconColorRow(Table t, String icon, String label, String value, DeviceRgb color) {
//        Cell ic = new Cell()
//                .setBorder(Border.NO_BORDER)
//                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
//                .setPadding(6).setVerticalAlignment(VerticalAlignment.MIDDLE)
//                .setTextAlignment(TextAlignment.CENTER);
//        Table iconBox = new Table(1).setWidth(22).setHorizontalAlignment(HorizontalAlignment.CENTER);
//        Cell ib = new Cell()
//                .setBackgroundColor(GOLD_LIGHT)
//                .setBorder(new SolidBorder(GOLD, 0.5f))
//                .setBorderRadius(new BorderRadius(4))
//                .setPadding(3).setTextAlignment(TextAlignment.CENTER);
//        ib.add(new Paragraph(icon).setFontSize(9).setFontColor(GOLD)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
//        iconBox.addCell(ib);
//        ic.add(iconBox);
//        t.addCell(ic);
//
//        Cell lc = new Cell()
//                .setBorder(Border.NO_BORDER)
//                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
//                .setPadding(8).setPaddingLeft(6);
//        lc.add(new Paragraph(label).setBold().setFontSize(9).setFontColor(SOFT_GRAY));
//        t.addCell(lc);
//
//        Cell vc = new Cell()
//                .setBorder(Border.NO_BORDER)
//                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
//                .setPadding(8);
//        vc.add(new Paragraph(value != null ? value : "—")
//                .setBold().setFontSize(10).setFontColor(color));
//        t.addCell(vc);
//    }
//
//    private void addIconStatusRow(Table t, String icon, String label, String statut) {
//        DeviceRgb color = getStatutColor(statut);
//        DeviceRgb bg    = getStatutBg(statut);
//
//        Cell ic = new Cell()
//                .setBorder(Border.NO_BORDER)
//                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
//                .setPadding(6).setVerticalAlignment(VerticalAlignment.MIDDLE)
//                .setTextAlignment(TextAlignment.CENTER);
//        Table iconBox = new Table(1).setWidth(22).setHorizontalAlignment(HorizontalAlignment.CENTER);
//        Cell ib = new Cell()
//                .setBackgroundColor(GOLD_LIGHT)
//                .setBorder(new SolidBorder(GOLD, 0.5f))
//                .setBorderRadius(new BorderRadius(4))
//                .setPadding(3).setTextAlignment(TextAlignment.CENTER);
//        ib.add(new Paragraph(icon).setFontSize(9).setFontColor(GOLD)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
//        iconBox.addCell(ib);
//        ic.add(iconBox);
//        t.addCell(ic);
//
//        Cell lc = new Cell()
//                .setBorder(Border.NO_BORDER)
//                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
//                .setPadding(8).setPaddingLeft(6);
//        lc.add(new Paragraph(label).setBold().setFontSize(9).setFontColor(SOFT_GRAY));
//        t.addCell(lc);
//
//        Cell vc = new Cell()
//                .setBorder(Border.NO_BORDER)
//                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
//                .setPadding(5);
//        Table badge = new Table(1).setWidth(UnitValue.createPercentValue(55));
//        Cell bc = new Cell()
//                .setBackgroundColor(bg)
//                .setBorder(new SolidBorder(color, 1.5f))
//                .setBorderRadius(new BorderRadius(4))
//                .setPaddingTop(3).setPaddingBottom(3).setPaddingLeft(8).setPaddingRight(8)
//                .setTextAlignment(TextAlignment.CENTER);
//        bc.add(new Paragraph("● " + statut).setBold().setFontSize(9).setFontColor(color)
//                .setTextAlignment(TextAlignment.CENTER));
//        badge.addCell(bc);
//        vc.add(badge);
//        t.addCell(vc);
//    }
//
//    // ─────────────────────────────────────────────────────
//    // BLOC MONTANT — style médaille
//    // ─────────────────────────────────────────────────────
//    private void addMontantBlock(Document doc, String montant) {
//        Table mt = new Table(UnitValue.createPercentArray(new float[]{1f, 0.22f}))
//                .useAllAvailableWidth()
//                .setMarginLeft(30).setMarginRight(30).setMarginTop(16).setMarginBottom(16)
//                .setBackgroundColor(CREAM)
//                .setBorder(new SolidBorder(GOLD, 1.5f))
//                .setBorderRadius(new BorderRadius(8));
//
//        Cell left = new Cell()
//                .setBackgroundColor(CREAM)
//                .setBorder(Border.NO_BORDER)
//                .setPadding(18);
//        left.add(new Paragraph("MONTANT PAYÉ")
//                .setBold().setFontSize(9).setFontColor(GOLD).setMarginBottom(4));
//        left.add(new Paragraph(montant)
//                .setBold().setFontSize(30).setFontColor(NAVY));
//        mt.addCell(left);
//
//        // Médaille côté droit
//        Cell right = new Cell()
//                .setBackgroundColor(CREAM)
//                .setBorder(Border.NO_BORDER)
//                .setVerticalAlignment(VerticalAlignment.MIDDLE)
//                .setTextAlignment(TextAlignment.CENTER)
//                .setPadding(10);
//
//        // Cercle doré avec coche
//        Table medal = new Table(1).setWidth(50).setHorizontalAlignment(HorizontalAlignment.CENTER);
//        Cell mc = new Cell()
//                .setBackgroundColor(GOLD)
//                .setBorder(new SolidBorder(GOLD_DARK, 3))
//                .setBorderRadius(new BorderRadius(25))
//                .setPadding(8).setTextAlignment(TextAlignment.CENTER);
//        mc.add(new Paragraph("✓")
//                .setBold().setFontSize(22).setFontColor(WHITE)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
//        medal.addCell(mc);
//        right.add(medal);
//        mt.addCell(right);
//
//        doc.add(mt);
//    }
//
//    // ─────────────────────────────────────────────────────
//    // BLOC CLIENT (attestation)
//    // ─────────────────────────────────────────────────────
//    private void addClientBlock(Document doc, User client, String prenom, String nom) {
//        Table clientTbl = new Table(1).useAllAvailableWidth()
//                .setMarginLeft(30).setMarginRight(30).setMarginBottom(12)
//                .setBackgroundColor(CREAM)
//                .setBorder(new SolidBorder(GOLD, 1.5f))
//                .setBorderRadius(new BorderRadius(8));
//
//        Cell clientCell = new Cell().setBorder(Border.NO_BORDER).setPadding(14);
//
//        Table inner = new Table(UnitValue.createPercentArray(new float[]{0.1f, 1f}))
//                .useAllAvailableWidth().setBorder(Border.NO_BORDER);
//
//        // Icône personne carrée
//        Cell iconCell = new Cell().setBorder(Border.NO_BORDER)
//                .setVerticalAlignment(VerticalAlignment.MIDDLE).setPaddingRight(10);
//        Table iconBox = new Table(1).setWidth(36).setHorizontalAlignment(HorizontalAlignment.CENTER);
//        Cell ib = new Cell()
//                .setBackgroundColor(GOLD)
//                .setBorder(new SolidBorder(GOLD_DARK, 1))
//                .setBorderRadius(new BorderRadius(6))
//                .setPadding(6).setTextAlignment(TextAlignment.CENTER);
//        ib.add(new Paragraph("👤").setFontSize(16)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
//        iconBox.addCell(ib);
//        iconCell.add(iconBox);
//        inner.addCell(iconCell);
//
//        Cell nameCell = new Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
//        nameCell.add(new Paragraph("M./Mme " + prenom + " " + nom)
//                .setBold().setFontSize(14).setFontColor(NAVY).setMarginBottom(5));
//        if (client != null) {
//            Table contactRow = new Table(UnitValue.createPercentArray(new float[]{1f, 1f}))
//                    .useAllAvailableWidth().setBorder(Border.NO_BORDER);
//            Cell emailC = new Cell().setBorder(Border.NO_BORDER);
//            emailC.add(new Paragraph("✉  " + client.getEmail())
//                    .setFontSize(9).setFontColor(ANTHRACITE));
//            contactRow.addCell(emailC);
//            Cell telC = new Cell().setBorder(Border.NO_BORDER);
//            if (client.getTelephone() != null)
//                telC.add(new Paragraph("📞  " + client.getTelephone())
//                        .setFontSize(9).setFontColor(ANTHRACITE));
//            contactRow.addCell(telC);
//            nameCell.add(contactRow);
//        }
//        inner.addCell(nameCell);
//        clientCell.add(inner);
//        clientTbl.addCell(clientCell);
//        doc.add(clientTbl);
//    }
//
//    // ─────────────────────────────────────────────────────
//    // MENTION LÉGALE
//    // ─────────────────────────────────────────────────────
//    private void addMentionLegale(Document doc) {
//        Table mt = new Table(UnitValue.createPercentArray(new float[]{0.05f, 1f}))
//                .useAllAvailableWidth()
//                .setMarginLeft(30).setMarginRight(30).setMarginBottom(10)
//                .setBackgroundColor(CREAM)
//                .setBorder(new SolidBorder(GOLD, 1))
//                .setBorderRadius(new BorderRadius(6));
//
//        Cell iconC = new Cell().setBorder(Border.NO_BORDER)
//                .setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(10);
//        iconC.add(new Paragraph("⚖").setFontSize(18).setFontColor(GOLD)
//                .setTextAlignment(TextAlignment.CENTER));
//        mt.addCell(iconC);
//
//        Cell textC = new Cell().setBorder(Border.NO_BORDER).setPadding(12);
//        textC.add(new Paragraph(
//                "La présente attestation est délivrée pour servir et valoir ce que de droit.")
//                .setItalic().setFontSize(10).setFontColor(ANTHRACITE));
//        mt.addCell(textC);
//        doc.add(mt);
//    }
//
//    // ─────────────────────────────────────────────────────
//    // SIGNATURE COMPACTE
//    // ─────────────────────────────────────────────────────
//    private void addSignatureSectionCompact(Document doc, Contrat contrat) {
//        doc.add(new Paragraph(" ").setFontSize(3));
//
//        Table st = new Table(UnitValue.createPercentArray(new float[]{1f, 1f}))
//                .useAllAvailableWidth()
//                .setMarginLeft(30).setMarginRight(30).setMarginBottom(6);
//
//        Cell left = new Cell().setBorder(Border.NO_BORDER).setPaddingRight(10);
//        left.add(new Paragraph("Signature du client")
//                .setBold().setFontSize(9).setFontColor(ANTHRACITE).setMarginBottom(4));
//
//        if (contrat.getSignature() != null && !contrat.getSignature().isEmpty()) {
//            try {
//                String b64 = contrat.getSignature();
//                if (b64.contains(",")) b64 = b64.split(",")[1];
//                byte[] sigBytes = java.util.Base64.getDecoder().decode(b64);
//                left.add(new Image(ImageDataFactory.create(sigBytes))
//                        .setWidth(120).setHeight(45)
//                        .setHorizontalAlignment(HorizontalAlignment.LEFT));
//            } catch (Exception e) {
//                left.add(new Paragraph("[Signature]")
//                        .setItalic().setFontColor(SOFT_GRAY).setFontSize(9));
//            }
//        }
//        left.add(new Paragraph("✅ SIGNÉ ÉLECTRONIQUEMENT")
//                .setBold().setFontSize(7).setFontColor(EMERALD).setMarginTop(3));
//        st.addCell(left);
//
//        // Cachet rond
//        Cell right = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
//        right.add(new Paragraph("Cachet officiel")
//                .setBold().setFontSize(9).setFontColor(ANTHRACITE)
//                .setTextAlignment(TextAlignment.RIGHT).setMarginBottom(4));
//
//        Table cachet = new Table(1).setWidth(UnitValue.createPercentValue(80))
//                .setHorizontalAlignment(HorizontalAlignment.RIGHT);
//        Cell cc = new Cell()
//                .setBackgroundColor(CREAM)
//                .setBorder(new SolidBorder(GOLD, 2.5f))
//                .setBorderRadius(new BorderRadius(60))
//                .setPaddingTop(10).setPaddingBottom(10).setPaddingLeft(14).setPaddingRight(14)
//                .setTextAlignment(TextAlignment.CENTER);
//        cc.add(new Paragraph("★ ★ ★").setFontSize(8).setFontColor(GOLD)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
//        cc.add(new Paragraph("LIFESURE\nASSURANCES")
//                .setBold().setFontSize(10).setFontColor(NAVY)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(3));
//        cc.add(new Paragraph("Mohamed Boukhris")
//                .setItalic().setBold().setFontSize(8).setFontColor(NAVY)
//                .setTextAlignment(TextAlignment.CENTER));
//        cc.add(new Paragraph("Directeur Général")
//                .setItalic().setFontSize(7).setFontColor(SOFT_GRAY)
//                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
//        cc.add(new Paragraph("TUNIS – TUNISIE")
//                .setBold().setFontSize(7).setFontColor(NAVY)
//                .setTextAlignment(TextAlignment.CENTER));
//        cc.add(new Paragraph("★ ★ ★").setFontSize(8).setFontColor(GOLD)
//                .setTextAlignment(TextAlignment.CENTER).setMarginTop(2));
//        cachet.addCell(cc);
//        right.add(cachet);
//        st.addCell(right);
//        doc.add(st);
//    }
//
//    // ─────────────────────────────────────────────────────
//    // FOOTER
//    // ─────────────────────────────────────────────────────
//    private void addFooter(Document doc) {
//        doc.add(new Paragraph(" ").setFontSize(3));
//        goldBar(doc, 2);
//
//        Table ft = new Table(UnitValue.createPercentArray(new float[]{1f, 1f, 1f}))
//                .useAllAvailableWidth().setMarginTop(4);
//
//        Cell c1 = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
//        c1.add(new Paragraph()
//                .add(new Text("📍  ").setFontSize(9).setFontColor(GOLD))
//                .add(new Text("LifeSure Assurances\n").setBold().setFontSize(9).setFontColor(NAVY))
//                .add(new Text("    Tunis, Tunisie").setFontSize(8).setFontColor(SOFT_GRAY)));
//        ft.addCell(c1);
//
//        Cell c2 = new Cell().setBorder(Border.NO_BORDER).setPadding(6)
//                .setTextAlignment(TextAlignment.CENTER);
//        c2.add(new Paragraph()
//                .add(new Text("✉  ").setFontSize(9).setFontColor(GOLD))
//                .add(new Text("contact@lifesure.tn\n").setFontSize(8).setFontColor(SOFT_GRAY))
//                .add(new Text("📞  ").setFontSize(9).setFontColor(GOLD))
//                .add(new Text("+216 71 123 456").setFontSize(8).setFontColor(SOFT_GRAY))
//                .setTextAlignment(TextAlignment.CENTER));
//        ft.addCell(c2);
//
//        Cell c3 = new Cell().setBorder(Border.NO_BORDER).setPadding(6)
//                .setTextAlignment(TextAlignment.RIGHT);
//        c3.add(new Paragraph()
//                .add(new Text("🌐  ").setFontSize(9).setFontColor(GOLD))
//                .add(new Text("www.lifesure.tn").setFontSize(8).setFontColor(SOFT_GRAY))
//                .setTextAlignment(TextAlignment.RIGHT));
//        ft.addCell(c3);
//        doc.add(ft);
//
//        doc.add(new Paragraph(
//                "Document généré le "
//                        + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
//                        + " - LifeSure © 2026")
//                .setFontSize(7).setItalic().setFontColor(WHITE)
//                .setTextAlignment(TextAlignment.CENTER)
//                .setBackgroundColor(GOLD)
//                .setPadding(5));
//    }
//
//    // ─────────────────────────────────────────────────────
//    // HELPERS
//    // ─────────────────────────────────────────────────────
//    private void goldBar(Document doc, float h) {
//        Table t = new Table(1).useAllAvailableWidth();
//        Cell c = new Cell().setBackgroundColor(GOLD).setBorder(Border.NO_BORDER).setHeight(h);
//        c.add(new Paragraph(""));
//        t.addCell(c);
//        doc.add(t);
//    }
//
//    private String fmt(java.time.LocalDate d, String pattern) {
//        return d != null ? d.format(DateTimeFormatter.ofPattern(pattern)) : "—";
//    }
//
//    private DeviceRgb getStatutColor(String s) {
//        if (s == null) return SOFT_GRAY;
//        switch (s.toUpperCase()) {
//            case "ACTIF": case "ACCEPTE": case "REUSSI": return EMERALD;
//            case "REFUSE": case "EXPIRE":                return BURGUNDY;
//            case "EN_COURS":                             return CYAN;
//            default:                                     return SOFT_GRAY;
//        }
//    }
//
//    private DeviceRgb getStatutBg(String s) {
//        if (s == null) return new DeviceRgb(245, 245, 245);
//        switch (s.toUpperCase()) {
//            case "ACTIF": case "ACCEPTE": case "REUSSI": return EMERALD_BG;
//            case "REFUSE": case "EXPIRE":                return BURGUNDY_BG;
//            case "EN_COURS":                             return CYAN_BG;
//            default:                                     return new DeviceRgb(245, 245, 245);
//        }
//    }
//}