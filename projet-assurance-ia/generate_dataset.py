import pandas as pd
import numpy as np

np.random.seed(42)

# Génération de 200 sinistres réalistes
n_samples = 200
data = []

print("=" * 60)
print("🎲 GÉNÉRATION DU DATASET DE SINISTRES")
print("=" * 60)

# 60% de sinistres LÉGITIMES
n_legitimes = int(n_samples * 0.6)
print(f"\n📋 Génération de {n_legitimes} sinistres légitimes...")

for _ in range(n_legitimes):
    # Pattern légitime : montants raisonnables, peu d'antécédents, déclaration rapide
    montant = np.random.choice([
        np.random.uniform(200, 1000),
        np.random.uniform(1000, 3000),
        np.random.uniform(3000, 8000)
    ], p=[0.5, 0.3, 0.2])

    nb_sinistres = np.random.choice([0, 1, 2], p=[0.6, 0.3, 0.1])
    delai = np.random.randint(0, 7)
    type_sinistre = np.random.randint(0, 4)

    data.append([round(montant), nb_sinistres, delai, type_sinistre, 0])

# 40% de FRAUDES
n_fraudes = n_samples - n_legitimes
print(f"🚨 Génération de {n_fraudes} sinistres frauduleux...")

for _ in range(n_fraudes):
    # Pattern frauduleux : gros montants, nombreux antécédents, déclaration tardive
    montant = np.random.choice([
        np.random.uniform(5000, 15000),
        np.random.uniform(15000, 30000)
    ], p=[0.6, 0.4])

    nb_sinistres = np.random.choice([3, 4, 5, 6, 7, 8], p=[0.3, 0.25, 0.2, 0.1, 0.1, 0.05])
    delai = np.random.randint(10, 35)
    type_sinistre = np.random.randint(0, 4)

    data.append([round(montant), nb_sinistres, delai, type_sinistre, 1])

# Mélanger les données
np.random.shuffle(data)

# Créer le DataFrame
df = pd.DataFrame(data, columns=['montant', 'nb_sinistres', 'delai', 'type', 'fraude'])

# Sauvegarder
df.to_csv('fraude.csv', index=False)

print(f"\n✅ Dataset généré : {len(df)} lignes")
print(f"   📊 Légitimes : {(df['fraude'] == 0).sum()} ({((df['fraude'] == 0).sum()/len(df)*100):.0f}%)")
print(f"   🚨 Fraudes   : {(df['fraude'] == 1).sum()} ({((df['fraude'] == 1).sum()/len(df)*100):.0f}%)")
print(f"\n📈 Aperçu (10 premières lignes) :")
print(df.head(10).to_string(index=False))

print(f"\n📊 Statistiques :")
print(df.describe().to_string())

print("\n" + "=" * 60)
print("✅ FICHIER fraude.csv CRÉÉ AVEC SUCCÈS")
print("=" * 60)