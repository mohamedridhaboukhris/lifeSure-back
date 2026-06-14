


from flask import Flask, request, jsonify
import pickle
import numpy as np
from datetime import datetime

app = Flask(__name__)

# Charger le modèle au démarrage
print("🚀 Chargement du modèle de détection de fraude...")
model = pickle.load(open("model.pkl", "rb"))
print("✅ Modèle chargé avec succès")

@app.route('/', methods=['GET'])
def home():
    """Endpoint d'accueil"""
    return jsonify({
        "service": "API Détection de Fraude",
        "modèle": "RandomForestClassifier",
        "version": "2.0",
        "status": "✅ Opérationnel",
        "endpoints": {
            "/predict": "POST - Prédire la fraude",
            "/health": "GET - État du service"
        }
    })

@app.route('/health', methods=['GET'])
def health():
    """Vérification de l'état"""
    return jsonify({
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "model_loaded": model is not None
    })

@app.route('/predict', methods=['POST'])
def predict():
    """Prédiction de fraude"""
    try:
        data = request.json

        # Validation des données
        required_fields = ['montant', 'nb_sinistres', 'delai', 'type']
        for field in required_fields:
            if field not in data:
                return jsonify({
                    "error": f"Champ manquant : {field}",
                    "required": required_fields
                }), 400

        # Logger l'analyse
        print(f"\n📊 Nouvelle analyse :")
        print(f"   💰 Montant      : {data['montant']} DT")
        print(f"   📋 Nb sinistres : {data['nb_sinistres']}")
        print(f"   ⏱️  Délai       : {data['delai']} jours")
        print(f"   🏷️  Type        : {data['type']}")

        # Préparer les features
        features = np.array([[
            float(data['montant']),
            int(data['nb_sinistres']),
            int(data['delai']),
            int(data['type'])
        ]])

        # Prédiction
        proba = model.predict_proba(features)[0][1]
        is_fraude = int(proba > 0.6)

        # Niveau de risque
        if proba < 0.3:
            niveau = "FAIBLE"
            emoji = "🟢"
        elif proba < 0.6:
            niveau = "MOYEN"
            emoji = "🟡"
        elif proba < 0.8:
            niveau = "ÉLEVÉ"
            emoji = "🟠"
        else:
            niveau = "CRITIQUE"
            emoji = "🔴"

        # Recommandation
        if is_fraude:
            recommandation = "Vérification approfondie recommandée"
        else:
            recommandation = "Sinistre légitime, traitement standard"

        result = {
            "score": float(proba),
            "score_pourcentage": round(float(proba) * 100, 2),
            "fraude": is_fraude,
            "niveau_risque": niveau,
            "emoji": emoji,
            "recommandation": recommandation,
            "timestamp": datetime.now().isoformat()
        }

        print(f"   {emoji} Résultat : {niveau} ({proba * 100:.2f}%)")
        print(f"   ⚖️  Verdict : {'⚠️ FRAUDE SUSPECTÉE' if is_fraude else '✅ LÉGITIME'}")

        return jsonify(result)

    except Exception as e:
        print(f"❌ Erreur : {str(e)}")
        return jsonify({
            "error": "Erreur lors de la prédiction",
            "details": str(e)
        }), 500

if __name__ == "__main__":
    print("\n" + "=" * 60)
    print("🚀 API DÉTECTION DE FRAUDE - DÉMARRÉE")
    print("=" * 60)
    print(f"📡 URL : http://localhost:5001")
    print(f"📋 Endpoints :")
    print(f"   GET  / → Info")
    print(f"   GET  /health → Santé")
    print(f"   POST /predict → Prédire")
    print("=" * 60 + "\n")
    app.run(host='0.0.0.0', port=5001)