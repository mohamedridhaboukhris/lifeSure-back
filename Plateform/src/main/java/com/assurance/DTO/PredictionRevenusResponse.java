//
//
//package com.assurance.DTO;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.List;
//import java.util.Map;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class PredictionRevenusResponse {
//
//    private boolean success;
//
//    // Historique des derniers mois (pour le graphique)
//    private Map<String, Double> historique;
//
//    // Prédictions futures
//    private double predictionMoisProchain;
//    private double predictionDeuxiemeMois;
//    private String labelMoisProchain;       // "Juin 2026"
//    private String labelDeuxiemeMois;       // "Juillet 2026"
//
//    // Analyse IA
//    private String tendance;                // "+5% par mois"
//    private String typeTendance;            // CROISSANCE, STABLE, DECLIN
//    private double scoreConfiance;          // 0.85
//    private List<String> insights;          // Liste de conseils
//
//    private String message;
//}