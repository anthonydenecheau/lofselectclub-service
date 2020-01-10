# lofselectclub-service
**Objet:**
Construction des éléments statistiques pour les clubs de race

## CI/CD

### Réalisation

A l'ouverture d'une issue, il conviendra de créer une nouvelle branche.
**Règle de nommage :**
- feat[nomIssue] pour le traitement d'une évolution
- bug[nomIssue] pour le traitement d'une correction

Une fois le développement réalisé, un commit sera effectué sur la branche.
Le commentaire du commit devra contenir les id des issues corrigées (par exemple: *fix issue #35*)

Une PullRequest et un merge devront être effectués sur la master.

### Cinématique
Le merge déclenche un build de l'image docker via Travis. Cette image est stockée sur DockerHub.
**Travis** update, en fin de traitement, le fichier** .env** de la branche *re7* du projet *gcp-migration* (Cf. terraform/code) avec le nouveau nom de l'image buildée.

La mise à jour du projet *gcp_migration* déclenche le build **Travis** d'une nouvelle  image *scc-docker-server-image* et son déploiement sous gcp dans le projet *lof-ws-re7* via **Terraform**

Pour une livraison dans l'environnement de production, il conviendra d'effectuer dans le projet *gcp-migration* une PR et un merge sur la branche *master*.

### Modification de la base de données
Une *Cloud Function* a été mise en place pour la détection et 'intégration automatique d'un *dump*.
Selon la nature de l'évolution ou de la correction et si cela impacte la donnée, 2 scenario sont possibles :
Soit :
. Embarquer dans le service, les scripts SQL de création/ mise à jour des objets de la base de données (penser à intialiser les nouvelles colonnes par une default value si une valeur nulle peut impacter le code)
. Livrer le dump, une fois le container démarré (les objets du dump doivent être l'image de ce qui a été livré par le service)
Ou
*Si l'évolution n'impacte pas l'existant *
. Livrer le dump
. Livrer le service, une fois le dump effectué.
