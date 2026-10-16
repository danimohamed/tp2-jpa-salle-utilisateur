# TP 2 — Gestion des entités Salle et Utilisateur avec JPA et Hibernate

Projet Maven autonome (aucun serveur d'application, aucun EJB) qui met en œuvre la
persistance **JPA 2.2 / Hibernate 5.6** sur une base **H2 en mémoire**.

Le domaine est celui de l'application « Réservation de salles » : une `Salle`
possède un nom et une capacité, un `Utilisateur` est rattaché (ou non) à une `Salle`.

---

## Prérequis

| Outil  | Version testée |
|--------|----------------|
| JDK    | 21 (le code est compilé en `release 11`) |
| Maven  | 3.9+ |

## Démarrage rapide

```bash
mvn test        # compile et exécute les 5 tests JUnit
mvn package     # produit target/gestion-salles-1.0.0.jar
```

Pour lancer la démonstration (`App`) sans IDE :

```bash
mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt
java -cp "target/classes;$(cat cp.txt)" com.example.jpa.App   # Windows : utilisez ; comme séparateur
```

Sortie attendue :

```
--- Salles ---
Salle [id=1, nom=Amphi A, capacite=120]
Salle [id=2, nom=TD 12, capacite=25]
--- Utilisateurs ---
Utilisateur [id=1, nom=Alice, email=alice@example.com, salle=Amphi A]
Utilisateur [id=2, nom=Bob, email=bob@example.com, salle=TD 12]
--- Salles de capacite >= 100 ---
Salle [id=1, nom=Amphi A, capacite=120]
--- Utilisateurs de la salle 'TD 12' ---
Utilisateur [id=2, nom=Bob, email=bob@example.com, salle=TD 12]
```

---

## Structure du projet

```
src/main/java/com/example/jpa/
├── App.java                        # démonstration console
├── entities/
│   ├── Salle.java                  # @Entity : id, nom (unique), capacite
│   └── Utilisateur.java            # @Entity : id, nom, email (unique), @ManyToOne Salle
├── dao/
│   ├── GenericDao.java             # CRUD générique + gestion manuelle des transactions
│   ├── SalleDao.java               # + findByCapaciteMin, findByNomContaining
│   └── UtilisateurDao.java         # + findBySalleNom, findByNomContaining, findSansSalle
├── service/
│   └── ServiceGestion.java         # façade métier (possède l'EntityManagerFactory)
└── util/
    └── JpaUtil.java                # singleton EntityManagerFactory (bonne pratique)

src/main/resources/META-INF/persistence.xml
src/test/java/com/example/jpa/test/ServiceGestionTest.java
```

## Dépendances (`pom.xml`)

| Dépendance | Rôle |
|------------|------|
| `org.hibernate:hibernate-entitymanager:5.6.15.Final` | Implémentation JPA |
| `javax.persistence:javax.persistence-api:2.2` | API standard (`javax.persistence.*`) |
| `com.h2database:h2:2.1.214` (runtime) | Base en mémoire |
| `junit:junit:4.13.2` (test) | Tests unitaires |
| `net.bytebuddy:byte-buddy:1.14.18` | **Correctif JDK 21** — Hibernate 5.6.15 embarque Byte Buddy 1.12.x, incapable de lire le format de classe `major version 65` ; sans cette surcharge, l'instrumentation des entités échoue au démarrage. |

## Configuration JPA (`META-INF/persistence.xml`)

- Unité de persistance `gestionPU`, `transaction-type="RESOURCE_LOCAL"` (transactions
  gérées par l'application, pas par le conteneur).
- Les deux entités sont déclarées explicitement avec `<class>`.
- `javax.persistence.jdbc.url = jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1` → la base survit à
  la fermeture de l'`EntityManagerFactory`, ce qui permet de partager le même schéma
  entre plusieurs instances de service.
- `hibernate.hbm2ddl.auto = update` → génération/mise à jour automatique du schéma.
- `hibernate.show_sql` + `hibernate.format_sql` → journalisation du SQL pour le débogage.

---

## Points clés du TP

### 1. Entités

```java
@Entity
public class Salle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(nullable = false)
    private int capacite;
    // constructeur sans argument obligatoire + getters/setters
}
```

```java
@Entity
public class Utilisateur {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "salle_id")   // clé étrangère explicite
    private Salle salle;
}
```

> Un constructeur **sans argument** (au moins `protected`) est imposé par la spécification
> JPA : c'est celui qu'utilise Hibernate pour instancier les entités lors du chargement.

### 2. Transactions manuelles

Chaque opération d'écriture suit le même schéma :

```java
EntityManager em = JpaUtil.createEntityManager();
EntityTransaction tx = em.getTransaction();
try {
    tx.begin();
    em.persist(entite);
    tx.commit();
} catch (RuntimeException e) {
    if (tx.isActive()) tx.rollback();
    throw e;
} finally {
    em.close();     // libère la connexion : indispensable
}
```

`EntityManager` est **court** : on en crée un par unité de travail, jamais un singleton
partagé entre threads. Seul `EntityManagerFactory` est coûteux et doit être un singleton
(`JpaUtil`).

### 3. JPQL utilisé

| Méthode | Requête |
|---------|---------|
| `GenericDao.findAll` | `SELECT e FROM <Entite> e` |
| `SalleDao.findByCapaciteMin` | `SELECT s FROM Salle s WHERE s.capacite >= :capaciteMin` |
| `SalleDao.findByNomContaining` | `... WHERE LOWER(s.nom) LIKE :pattern` |
| `UtilisateurDao.findBySalleNom` | `SELECT u FROM Utilisateur u WHERE u.salle.nom = :nomSalle` |
| `UtilisateurDao.findSansSalle` | `SELECT u FROM Utilisateur u WHERE u.salle IS NULL` |

En JPQL on navigue par **association** (`u.salle.nom`), pas par clé étrangère
(`u.salle_id`) : Hibernate génère lui-même la jointure.

### 4. Pièges rencontrés et corrigés

- **JDK 21 + Hibernate 5.6.15** → `Unsupported class file major version 65` : surcharge
  de `net.bytebuddy:byte-buddy` (voir tableau ci-dessus).
- **`setId` sur une entité gérée** : un identifiant généré ne doit jamais être modifié
  à la main ; il est produit par la base (`IDENTITY`). Les setters d'`id` ont donc été
  volontairement omis.
- **Tests non déterministes** : comme la base H2 est partagée (`DB_CLOSE_DELAY=-1`), le
  `@Before` des tests vide les tables avant chaque test, dans l'ordre
  `Utilisateur` puis `Salle` pour respecter la contrainte de clé étrangère.
- **`merge` vs `persist`** : `persist` sur une entité détachée lève une exception ;
  `update`/`delete` du DAO utilisent `merge` lorsque l'entité n'est plus dans le
  contexte de persistance.

## Tests

```
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

- `testCreationEtRechercheSalle` — persistance puis relecture d'une `Salle`.
- `testCreationUtilisateurAvecSalle` — association `@ManyToOne` persistée et relue.
- `testRechercheUtilisateursParSalle` — requête JPQL par nom de salle.
- `testMiseAJourEtSuppression` — cycle `merge` / `remove`.
- `testRequetesJpqlAvancees` — capacité minimale, recherche par nom, `IS NULL`.

---

## Auteur

Mohamed Dani — [@danimohamed](https://github.com/danimohamed)
