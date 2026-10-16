package com.example.diabcare.exercices;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercices — callbacks, interfaces fonctionnelles et {@link CompletableFuture}.
 *
 * <p>Fil rouge métier Diabcare : "récupérer la liste des patients", "récupérer le détail d'un
 * patient", "calculer une statistique glycémique" — trois opérations qu'on imagine lentes (appel
 * réseau, requête base de données), et qu'on va rendre asynchrones de 3 façons différentes pour
 * comparer.
 *
 * <p>Progression : 1) le callback n'est qu'une interface fonctionnelle qu'on implémente avec une
 * lambda ; 2) le rendre vraiment asynchrone "à la main" avec un {@link Thread} révèle les limites
 * (lisibilité, erreurs qui disparaissent) ; 3) {@link CompletableFuture} résout ces deux problèmes
 * — c'est l'équivalent Java des Promises JS ; 4) Spring Boot sait exploiter ça nativement via
 * {@code @Async}.
 *
 * <p>Chaque {@code @Nested} ci-dessous correspond à une étape, chaque {@code @Test} est autonome
 * (pas d'ordre imposé, pas d'état partagé entre étapes).
 */
class CallbacksCompletableFutureExercicesTest {

    // ================================================================================
    // ÉTAPE 1 — Interface fonctionnelle et lambda : le callback, version moderne
    // ================================================================================
    //
    // Un "callback" n'est rien d'autre qu'un bout de comportement qu'on passe en paramètre, pour
    // que CE SOIT LA MÉTHODE APPELÉE qui décide QUAND l'exécuter — pas celui qui l'écrit. Avant
    // Java 8, on faisait ça avec une interface qu'on implémentait par une classe anonyme (verbeux).
    // Depuis Java 8, les "interfaces fonctionnelles" (une seule méthode abstraite) se créent
    // directement avec une lambda. `Consumer<T>` et `Function<T, R>` sont deux interfaces
    // fonctionnelles fournies par le JDK (`java.util.function`) — pas besoin d'en définir une
    // soi-même pour ces deux cas très courants.
    //
    @Nested
    @DisplayName("1 — Interface fonctionnelle et lambda")
    class Etape1InterfaceFonctionnelleEtLambda {

        /**
         * Simule une récupération (ici instantanée, pas encore async) de la liste des patients, et
         * appelle le callback fourni une fois le résultat disponible. Le type du paramètre,
         * {@code Consumer<List<String>>}, EST l'interface fonctionnelle : "une fonction qui reçoit
         * une liste de noms et ne retourne rien".
         */
        void recupererListePatients(Consumer<List<String>> callback) {
            List<String> noms = List.of("Dupont Jean", "Martin Alice", "Durand Noé");
            callback.accept(noms);
        }

        @Test
        @DisplayName("callback via lambda")
        void callbackViaLambda() {
            AtomicReference<List<String>> resultatRecu = new AtomicReference<>();

            // La lambda EST l'implémentation de Consumer<List<String>>#accept(List<String>).
            recupererListePatients(noms -> resultatRecu.set(noms));

            assertThat(resultatRecu.get()).containsExactly("Dupont Jean", "Martin Alice", "Durand Noé");
        }

        @Test
        @DisplayName("même callback, écrit comme avant Java 8 (classe anonyme) — pour comparaison")
        void callbackViaClasseAnonyme() {
            AtomicReference<List<String>> resultatRecu = new AtomicReference<>();

            // Strictement équivalent à la lambda ci-dessus : même interface fonctionnelle, juste
            // une syntaxe plus verbeuse pour l'implémenter. La lambda n'a rien de magique, c'est un
            // raccourci d'écriture pour exactement ça.
            recupererListePatients(new Consumer<List<String>>() {
                @Override
                public void accept(List<String> noms) {
                    resultatRecu.set(noms);
                }
            });

            assertThat(resultatRecu.get()).hasSize(3);
        }

        /**
         * Même principe avec {@code Function<T, R>} : un callback qui, lui, RETOURNE une valeur.
         * Utile pour transformer/calculer plutôt que juste "réagir".
         */
        double calculerStatistiqueGlycemique(List<Double> glycemies, Function<List<Double>, Double> calcul) {
            return calcul.apply(glycemies);
        }

        @Test
        @DisplayName("Function<T, R> pour calculer une statistique")
        void statistiqueViaFunction() {
            List<Double> glycemies = List.of(5.8, 6.1, 5.4, 7.0);

            Function<List<Double>, Double> moyenne = valeurs ->
                valeurs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            double resultat = calculerStatistiqueGlycemique(glycemies, moyenne);

            assertThat(resultat).isCloseTo(6.075, org.assertj.core.data.Offset.offset(0.001));
        }
    }

    // ================================================================================
    // ÉTAPE 2 — Callback asynchrone "à la main" avec Thread/Runnable
    // ================================================================================
    //
    // Pour de vrai, "récupérer le détail d'un patient" est lent (réseau, base de données) : on ne
    // veut pas bloquer l'appelant en attendant. Solution brute : lancer un Thread, et appeler le
    // callback depuis CE thread une fois le résultat prêt. Deux callbacks séparés (succès / erreur)
    // puisqu'il n'y a plus de `try/catch` possible autour de l'appel — l'exception, si elle a lieu,
    // se produit plus tard, sur un autre thread.
    //
    @Nested
    @DisplayName("2 — Callback asynchrone avec Thread")
    class Etape2CallbackAsynchroneAvecThread {

        void recupererDetailPatientAsync(String id, Consumer<String> onSuccess, Consumer<Throwable> onError) {
            new Thread(() -> {
                try {
                    Thread.sleep(50); // simule la latence d'un appel réseau/base
                    if (id.isBlank()) {
                        throw new IllegalArgumentException("id de patient vide");
                    }
                    onSuccess.accept("Patient " + id + " : Dupont Jean, né le 27/11/2014");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException e) {
                    onError.accept(e);
                }
            }).start();
        }

        @Test
        @DisplayName("callback de succès, avec attente explicite du résultat")
        void callbackDeSucces() throws InterruptedException {
            AtomicReference<String> resultat = new AtomicReference<>();
            // Un test sur du code asynchrone ne peut pas juste "lire la variable après l'appel" :
            // l'appel revient immédiatement, bien avant que le Thread ait fini. Il faut un
            // mécanisme de synchronisation explicite — ici un CountDownLatch, débloqué par le
            // callback lui-même.
            CountDownLatch latch = new CountDownLatch(1);

            recupererDetailPatientAsync("42", detail -> {
                resultat.set(detail);
                latch.countDown();
            }, erreur -> latch.countDown());

            boolean arriveAvantTimeout = latch.await(2, TimeUnit.SECONDS);

            assertThat(arriveAvantTimeout).isTrue();
            assertThat(resultat.get()).contains("Patient 42");
        }

        /**
         * Le vrai problème n'est pas la syntaxe, c'est l'ENCHAÎNEMENT : récupérer le patient PUIS,
         * avec son résultat, calculer sa statistique glycémique. Callback dans le callback — ça se
         * lit de l'intérieur vers l'extérieur, et chaque niveau doit re-déclarer sa propre gestion
         * d'erreur.
         */
        void calculerStatistiqueAsync(String contexte, Consumer<Double> onSuccess, Consumer<Throwable> onError) {
            new Thread(() -> {
                try {
                    Thread.sleep(30);
                    onSuccess.accept(6.2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        @Test
        @DisplayName("enchaînement de deux callbacks (callback hell) — et l'erreur qui disparaît")
        void enchainementDeDeuxCallbacks() throws InterruptedException {
            AtomicReference<Double> statistiqueFinale = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            recupererDetailPatientAsync("42",
                detailPatient -> // callback de niveau 1 : on a le patient...
                    calculerStatistiqueAsync(detailPatient,
                        statistique -> { // ...callback de niveau 2 : on a la statistique
                            statistiqueFinale.set(statistique);
                            latch.countDown();
                        },
                        erreurNiveau2 -> latch.countDown()),
                erreurNiveau1 -> latch.countDown()
            );

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(statistiqueFinale.get()).isEqualTo(6.2);

            // À retenir de cette étape, sans l'exécuter ici : si `recupererDetailPatientAsync`
            // levait une exception NON interceptée À L'INTÉRIEUR du Thread (par ex. un bug dans le
            // code du onSuccess lui-même), rien ne la rattraperait jamais — ni ce test, ni
            // l'appelant. Elle finirait sur le `UncaughtExceptionHandler` par défaut (une trace dans
            // les logs) sans jamais déclencher `onError`. Avec deux niveaux de callbacks imbriqués,
            // ce risque existe à CHAQUE niveau. C'est précisément ce que CompletableFuture corrige.
        }
    }

    // ================================================================================
    // ÉTAPE 3 — CompletableFuture : la Promise de Java
    // ================================================================================
    //
    // CompletableFuture gère le thread à votre place (un pool interne par défaut) ET propage les
    // exceptions le long de la chaîne — plus besoin d'un second callback "onError" à chaque étape.
    // Vocabulaire à retenir, en miroir des Promises JS :
    //   - CompletableFuture.supplyAsync(...)   ~ new Promise(...) qui démarre tout de suite
    //   - .thenApply(valeur -> nouvelleValeur)  ~ .then(valeur => nouvelleValeur)   (map)
    //   - .thenCompose(valeur -> autreFuture)   ~ .then(valeur => autrePromise)     (flatMap/chaîne)
    //   - .exceptionally(exception -> secours)  ~ .catch(erreur => secours)
    //   - .get() (bloquant, lève des exceptions checked) ~ await (dans une fonction async)
    //
    @Nested
    @DisplayName("3 — CompletableFuture")
    class Etape3CompletableFuture {

        CompletableFuture<List<String>> recupererListePatientsAsync() {
            return CompletableFuture.supplyAsync(() -> {
                sleepSansExceptionChecked(50);
                return List.of("Dupont Jean", "Martin Alice", "Durand Noé");
            });
        }

        CompletableFuture<Double> calculerStatistiqueGlycemiqueAsync(String nomPatient) {
            return CompletableFuture.supplyAsync(() -> {
                sleepSansExceptionChecked(30);
                if (nomPatient.isBlank()) {
                    throw new IllegalArgumentException("nom de patient vide");
                }
                return 6.2;
            });
        }

        @Test
        @DisplayName("thenApply = map : transformer le résultat une fois prêt")
        void thenApplyPourTransformer() throws ExecutionException, InterruptedException {
            CompletableFuture<Integer> nombreDePatients =
                recupererListePatientsAsync().thenApply(List::size);

            // .get() est la version BLOQUANTE : utile en test, à éviter dans du code applicatif
            // (ça annule tout l'intérêt de l'asynchrone — exactement comme un `await` mal placé).
            assertThat(nombreDePatients.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("thenCompose = flatMap : enchaîner un second appel asynchrone")
        void thenComposePourEnchainer() throws ExecutionException, InterruptedException {
            // thenCompose attend une fonction qui retourne ELLE-MÊME un CompletableFuture — sans
            // ça, on obtiendrait un CompletableFuture<CompletableFuture<Double>>, imbriqué comme en
            // étape 2. C'est la différence entre thenApply (map) et thenCompose (flatMap).
            CompletableFuture<Double> statistiqueDuPremierPatient =
                recupererListePatientsAsync()
                    .thenCompose(patients -> calculerStatistiqueGlycemiqueAsync(patients.get(0)));

            assertThat(statistiqueDuPremierPatient.get()).isEqualTo(6.2);
        }

        @Test
        @DisplayName("exceptionally = catch : récupérer une erreur avec une valeur de secours")
        void exceptionallyPourRecuperer() throws ExecutionException, InterruptedException {
            CompletableFuture<Double> statistiqueAvecSecours =
                calculerStatistiqueGlycemiqueAsync("") // nom vide -> lève IllegalArgumentException
                    .exceptionally(exception -> -1.0); // valeur de repli, jamais levée au test

            assertThat(statistiqueAvecSecours.get()).isEqualTo(-1.0);
        }

        @Test
        @DisplayName("sans exceptionally, .get() relance l'erreur (enveloppée)")
        void sansRecuperationLErreurRemonte() {
            CompletableFuture<Double> statistiqueSansSecours =
                calculerStatistiqueGlycemiqueAsync("");

            // .get() enveloppe toujours l'exception d'origine dans une ExecutionException — c'est
            // elle qu'il faut attraper, puis regarder sa cause (getCause()) pour l'exception réelle.
            org.assertj.core.api.Assertions.assertThatThrownBy(statistiqueSansSecours::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("non-bloquant : thenAccept() reste un callback, mais chaîné et sûr")
        void thenAcceptResteNonBloquant() throws InterruptedException {
            AtomicReference<Integer> resultat = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            // thenAccept EST un Consumer<T>, exactement l'interface fonctionnelle de l'étape 1 — la
            // seule différence avec l'étape 2, c'est que CompletableFuture gère le thread et les
            // erreurs pour nous, le callback lui-même n'a rien de spécial à faire pour ça.
            recupererListePatientsAsync()
                .thenApply(List::size)
                .thenAccept(taille -> {
                    resultat.set(taille);
                    latch.countDown();
                });

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(resultat.get()).isEqualTo(3);
        }

        private void sleepSansExceptionChecked(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ================================================================================
    // ÉTAPE 4 — Lien avec Spring Boot : @Async
    // ================================================================================
    //
    // @Async est un marqueur Spring : posé sur une méthode d'un bean (@Service, @Component...), il
    // dit au conteneur "exécute cette méthode sur un thread du pool de tâches, pas sur le thread
    // appelant". Mais ce comportement n'existe QUE parce que Spring enveloppe le bean dans un proxy
    // AOP au démarrage (et seulement si @EnableAsync est activé sur une classe @Configuration) :
    // sans conteneur Spring derrière, @Async n'est qu'une annotation inerte, lue par personne. Le
    // test ci-dessous le prouve concrètement.
    //
    @Nested
    @DisplayName("4 — @Async et Spring Boot")
    class Etape4AsyncEtSpringBoot {

        /**
         * Un service imaginé comme s'il était {@code @Service} dans l'application réelle — ici
         * instancié directement avec {@code new}, SANS conteneur Spring, exprès.
         */
        static class PatientStatistiqueService {

            @Async
            public CompletableFuture<Double> calculerStatistiqueGlycemiqueAsync(List<Double> glycemies) {
                double moyenne = glycemies.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                return CompletableFuture.completedFuture(moyenne);
            }
        }

        @Test
        @DisplayName("@Async sans conteneur Spring ne change RIEN à l'exécution")
        void asyncSansConteneurResteSynchrone() throws ExecutionException, InterruptedException {
            String threadAppelant = Thread.currentThread().getName();
            PatientStatistiqueService service = new PatientStatistiqueService();

            CompletableFuture<Double> resultat =
                service.calculerStatistiqueGlycemiqueAsync(List.of(5.8, 6.1, 5.4));

            // La preuve : completedFuture() exécute déjà tout de façon synchrone AVANT de retourner
            // — on le vérifie en inspectant depuis quel thread la valeur était disponible. Dans une
            // vraie application Spring Boot avec @EnableAsync, le bean serait un PROXY : l'appel
            // reviendrait immédiatement, et le calcul tournerait réellement sur un thread du pool
            // "task executor" de Spring — PAS celui-ci.
            assertThat(resultat.get()).isCloseTo(5.767, org.assertj.core.data.Offset.offset(0.01));
            assertThat(Thread.currentThread().getName()).isEqualTo(threadAppelant);
        }

        /**
         * Ce que verrait un {@code @RestController} si {@code service} était un vrai bean Spring
         * (avec {@code @EnableAsync} actif) injecté par constructeur : il n'a JAMAIS à bloquer sur
         * {@code .get()}. Spring MVC sait nativement renvoyer un {@code CompletableFuture<ResponseEntity<T>>}
         * — il libère le thread servlet pendant l'attente, et répond au client quand le
         * CompletableFuture se termine. Ici, dans le test, {@code .get()} reste nécessaire : JUnit
         * a besoin du résultat de façon synchrone pour l'assertion.
         */
        @Test
        @DisplayName("ce que le contrôleur ferait avec le CompletableFuture retourné par le service")
        void commeSiCetaitUnControleur() throws ExecutionException, InterruptedException {
            PatientStatistiqueService service = new PatientStatistiqueService();

            CompletableFuture<Object> reponseControleur =
                service.calculerStatistiqueGlycemiqueAsync(List.of(5.8, 6.1, 5.4))
                    .thenApply(moyenne -> java.util.Map.of("statistiqueGlycemiqueMoyenne", moyenne));
            // Dans un vrai contrôleur : `return reponseControleur.thenApply(ResponseEntity::ok);`
            // et la méthode du contrôleur a pour signature `CompletableFuture<ResponseEntity<...>>`.

            assertThat(reponseControleur.get()).isEqualTo(
                java.util.Map.of("statistiqueGlycemiqueMoyenne", 5.766666666666667)
            );
        }
    }

    // ================================================================================
    // À RETENIR
    // ================================================================================
    //
    // - Une interface fonctionnelle (une seule méthode abstraite, ex. Consumer<T>, Function<T,R>)
    //   implémentée par une lambda, EST un callback — rien de plus, rien d'autre syntaxe que celle
    //   d'avant Java 8 (classe anonyme), pour le même contrat.
    //
    // - CompletableFuture est l'équivalent Java des Promises JS : supplyAsync ~ new Promise,
    //   thenApply ~ .then (map), thenCompose ~ .then qui chaîne une autre promise (flatMap),
    //   exceptionally ~ .catch, .get() bloquant ~ await. Contrairement aux callbacks "à la main"
    //   (étape 2), les erreurs se propagent automatiquement le long de la chaîne au lieu de
    //   disparaître dans un thread qui plante silencieusement.
    //
    // - @Async n'a d'effet QUE sur un bean géré par un conteneur Spring avec @EnableAsync actif
    //   (le conteneur enveloppe le bean dans un proxy qui redirige réellement l'appel vers un
    //   thread du pool de tâches). Posé sur un objet créé avec `new`, c'est une annotation que
    //   personne ne lit — le code s'exécute de façon parfaitement synchrone.
    //
    // - Un service Spring dont une méthode retourne CompletableFuture<T> (typiquement annotée
    //   @Async) se consomme dans un @RestController SANS jamais appeler .get() : le contrôleur
    //   retourne lui-même un CompletableFuture<ResponseEntity<T>> (ou le fait transformer via
    //   .thenApply(ResponseEntity::ok)), et Spring MVC se charge de répondre au client une fois le
    //   calcul terminé, sans bloquer un thread pendant l'attente. Note : ce pattern est naturel pour
    //   un SERVICE ; un repository Spring Data JPA, lui, reste synchrone par défaut (JPA/JDBC sont
    //   bloquants) — un repository qui retourne CompletableFuture<T> est plus un idiome Spring Data
    //   MongoDB/réactif que JPA classique, bon à savoir pour ne pas le reproduire par réflexe ici.
    //
    // - Côté Angular (vu dans ce projet) : un Observable RxJS ou un signal (httpResource) jouent un
    //   rôle équivalent à CompletableFuture côté Java — une valeur asynchrone sur laquelle on
    //   enchaîne des transformations (thenApply ~ map/pipe, thenCompose ~ switchMap) plutôt que de
    //   la "déballer" immédiatement. Le point commun aux deux mondes : NE PAS bloquer en attendant
    //   une opération lente, et laisser le framework/l'environnement reprendre la main quand le
    //   résultat arrive.
}
