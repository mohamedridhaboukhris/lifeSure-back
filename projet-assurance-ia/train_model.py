import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix
import pickle

print("=" * 60)
print("🤖 ENTRAÎNEMENT DU MODÈLE DE DÉTECTION DE FRAUDE")
print("=" * 60)

# 1️⃣ Charger le dataset
print("\n📂 Chargement du dataset...")
data = pd.read_csv("fraude.csv")
print(f"   ✅ {len(data)} lignes chargées")
print(f"   📊 Distribution :")
print(f"      - Légitimes : {(data['fraude'] == 0).sum()}")
print(f"      - Fraudes   : {(data['fraude'] == 1).sum()}")

# 2️⃣ Préparer les features
print("\n🔧 Préparation des features...")
X = data[['montant', 'nb_sinistres', 'delai', 'type']]
y = data['fraude']

# 3️⃣ Séparation train/test (80/20)
print("\n✂️  Séparation train/test (80/20)...")
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42, stratify=y
)
print(f"   📚 Train : {len(X_train)} exemples")
print(f"   🧪 Test  : {len(X_test)} exemples")

# 4️⃣ Entraînement
print("\n🎓 Entraînement du modèle Random Forest...")
model = RandomForestClassifier(
    n_estimators=100,
    max_depth=10,
    min_samples_split=5,
    random_state=42,
    class_weight='balanced'
)
model.fit(X_train, y_train)
print("   ✅ Modèle entraîné")

# 5️⃣ Évaluation
print("\n📊 Évaluation du modèle...")
y_pred = model.predict(X_test)
accuracy = accuracy_score(y_test, y_pred)
print(f"\n   🎯 Précision : {accuracy * 100:.2f}%")

print("\n   📈 Rapport de classification :")
print(classification_report(y_test, y_pred,
      target_names=['Légitime', 'Fraude']))

print("\n   📊 Matrice de confusion :")
cm = confusion_matrix(y_test, y_pred)
print(f"      Vrais Négatifs (légitime correct) : {cm[0][0]}")
print(f"      Faux Positifs (fausse alerte)     : {cm[0][1]}")
print(f"      Faux Négatifs (fraude ratée)      : {cm[1][0]}")
print(f"      Vrais Positifs (fraude détectée)  : {cm[1][1]}")

# 6️⃣ Cross-validation
print("\n🔄 Validation croisée (5-fold)...")
scores = cross_val_score(model, X, y, cv=5, scoring='accuracy')
print(f"   📊 Précision moyenne : {scores.mean() * 100:.2f}% (+/- {scores.std() * 100:.2f}%)")

# 7️⃣ Importance des features
print("\n🔍 Importance des features :")
features = ['montant', 'nb_sinistres', 'delai', 'type']
importances = model.feature_importances_
for feature, importance in sorted(zip(features, importances), key=lambda x: -x[1]):
    bar = "█" * int(importance * 50)
    print(f"   {feature:15s} : {bar} {importance * 100:.2f}%")

# 8️⃣ Sauvegarde
print("\n💾 Sauvegarde du modèle...")
pickle.dump(model, open("model.pkl", "wb"))
print("   ✅ Modèle sauvegardé : model.pkl")

print("\n" + "=" * 60)
print("✅ ENTRAÎNEMENT TERMINÉ AVEC SUCCÈS")
print("=" * 60)