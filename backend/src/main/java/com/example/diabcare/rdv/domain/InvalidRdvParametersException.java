package com.example.diabcare.rdv.domain;

//TODO plusieurs types d'erreur possibles pour la creation (planification) de rdv:
// une pour date passée/présente,
// une pour patientRef null
// une pour incohérence statut VS dateHeure
// et corriger les impacts + ajouter les tests et les exceptionHandler nécessaires; voir commentaire de claude:
/*
Une seule InvalidRdvParametersException peut porter le champ en cause : un attribut (le nom du champ, ou une énumération
des champs possibles) fourni au constructeur, et un accesseur que le gestionnaire lit pour remplir le ProblemDetail.
Un seul gestionnaire suffit, sans classe supplémentaire.

Les classes séparées apportent autre chose : un type distinct que les tests peuvent vérifier sans comparer du texte, et
surtout la possibilité de répondre différemment selon la cause. C’est le cas de l’incohérence statut / date, qui ne
doit pas répondre 400 pour une erreur venue de la base.

Un point à vérifier avant de choisir la forme : comment ton API signale déjà les erreurs de validation de la requête
(roadbook 0004, @Valid). Si le client Angular affichera les erreurs par champ, le plus simple est de rester cohérent :
le ProblemDetail d’une erreur du domaine devrait porter le champ avec le même nom de propriété que celui d’une erreur
de validation. Sinon le front gérera deux formats.

Je ferais donc : une exception avec un champ, une seule classe de plus pour la cause qui change de traitement, et la
comparaison avec le format de validation avant de coder.
 */
public class InvalidRdvParametersException extends RuntimeException {

//    private final String field;
//    private final String value;

    public InvalidRdvParametersException(String message/*, String field, String value*/) {
        super(message);
//        this.field = field;
//        this.value = value;
    }

//    public String getField() {
//        return field;
//    }
//    public String getValue() {
//        return value;
//    }

}
