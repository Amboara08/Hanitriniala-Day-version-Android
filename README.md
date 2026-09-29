# Hantriniala Day — version 8

Application Android locale de suivi du cycle menstruel.

- Nom visible et package harmonisés avec « Hantriniala Day ».
- Accueil moderne avec en-tête rouge, séparation courbée et aperçu du cycle.
- Icônes vectorielles dessinées directement par l’application.
- Calendrier, rapports, historique, rappels et protection par code PIN.

## Générer l’APK

Avec Java 8 et Gradle 2.2.1 :

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-8.0.504.1-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& "C:\Gradle\gradle-2.2.1\bin\gradle.bat" clean assembleDebug
```

L’APK est créé dans `app\build\outputs\apk\app-debug.apk`.

Application Android native en Java, compatible avec l'environnement déjà utilisé : Java 8, Gradle 2.2.1 et Android API 22.

## Nouveautés

- Interface inspirée de la référence : en-têtes rouges en dégradé, cartes blanches arrondies et navigation plus lisible.
- Icônes de navigation plus grandes et onglet actif mis en évidence.
- Fiche moderne lors du toucher d'une date.
- Risque de grossesse estimé selon le calendrier : faible, moyen, élevé ou très élevé.
- Le niveau faible ne signifie jamais un risque nul, car l'ovulation réelle peut se décaler.
- Correction du premier jour réel d'une période depuis l'historique.
- Recalcul automatique des règles prévues, de l'ovulation estimée et des niveaux de risque après une correction.
- Possibilité de laisser le dernier jour inconnu, y compris pour une période déjà marquée en cours.
- Signature dans Paramètres : BY Aboalakely / pour toi / Tiffakeliko.
- Rappel Android local configurable de 0 à 7 jours avant les règles estimées.
- Choix de l'heure et bouton de test de notification.
- Reprogrammation du rappel après une correction du cycle et après le redémarrage du téléphone.
- Légende du calendrier avec les mêmes couleurs que les journées correspondantes.

## Création de l'APK

Depuis PowerShell, dans le dossier du projet :

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-8.0.504.1-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& "C:\Gradle\gradle-2.2.1\bin\gradle.bat" assembleDebug
```

L'APK sera créé dans `app\build\outputs\apk\app-debug.apk`.
