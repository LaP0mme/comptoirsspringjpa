package comptoirs.service;

import comptoirs.entity.Ligne;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import comptoirs.dao.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class CommandeServiceTest {
    private static final String ID_PETIT_CLIENT = "0COM";
    private static final String ID_GROS_CLIENT = "2COM";
    private static final String VILLE_PETIT_CLIENT = "Berlin";
    private static final BigDecimal REMISE_POUR_GROS_CLIENT = new BigDecimal("0.15");
    static final int REFERENCE_PRODUIT_DISPONIBLE_2 = 94;
    private static final int REFERENCE_PRODUIT_DISPONIBLE_1 = 93;

    @Autowired
    private CommandeService service;
    @Autowired
    private ProduitRepository produitDao;
    @Autowired
    private LigneService lignesService;
    @Test
    void testCreerCommandePourGrosClient() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        assertNotNull(commande.getNumero(), "On doit avoir la clé de la commande");
        assertEquals(REMISE_POUR_GROS_CLIENT, commande.getRemise(),
            "Une remise de 15% doit être appliquée pour les gros clients");
    }

    @Test
    void testCreerCommandePourPetitClient() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertNotNull(commande.getNumero());
        assertEquals(BigDecimal.ZERO, commande.getRemise(),
            "Aucune remise ne doit être appliquée pour les petits clients");
    }

    @Test
    void testCreerCommandeInitialiseAdresseLivraison() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertEquals(VILLE_PETIT_CLIENT, commande.getAdresseLivraison().getVille(),
            "On doit recopier l'adresse du client dans l'adresse de livraison");
    }
    @Test
    void testEnregistrerExpedition() {
        // On crée une commande
        var commande = service.creerCommande(ID_GROS_CLIENT);
        // On vérifie que la date d'éxpédition a été renseignée
        commande = service.enregistreExpédition(commande.getNumero());
        assertEquals(LocalDate.now(), commande.getEnvoyeele());
    }
    @Test
    void testEnregistrerExpeditionMiseAJourDesStocks() {
        // Création de la commande
        var commande = service.creerCommande(ID_GROS_CLIENT);
        //On récupère les unités en stocks pour chaque produit de la commande avant la mise à jour
        var lignes = commande.getLignes();
        var unitesEnStockAvant = new ArrayList<Integer>();
        for(int i = 0; i < lignes.size(); i++){
            unitesEnStockAvant.add(lignes.get(i).getProduit().getUnitesEnStock());
        }
        commande = service.enregistreExpédition(commande.getNumero());
        //On récupère les unités en stocks après que la commande soit enregistrée
        lignes = commande.getLignes();
        var unitesEnStockApres = new ArrayList<Integer>();
        var quantites = new ArrayList<Integer>();
        for(int i = 0; i < lignes.size(); i++){
            unitesEnStockApres.add(lignes.get(i).getProduit().getUnitesEnStock());
            quantites.add(lignes.get(i).getQuantite());
        }
        // On teste que la mise à jour des stocks à bien été réalisé
        if(unitesEnStockApres.size() == unitesEnStockAvant.size()){
            for(int i = 0; i < unitesEnStockAvant.size(); i++){
                // vérification que la quantité avant - la quantité commande = la quantité restante
                assertEquals(unitesEnStockAvant.get(i) - quantites.get(i), unitesEnStockApres.get(i));
            }
        }
    }

    @Test
    void testEnregistreExpeditionCommandeDejaEnvoyee() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        service.enregistreExpédition(commande.getNumero());
        // on teste que on ne peux pas enregistrer une commande deja envoyée
        assertThrows(UnsupportedOperationException.class, () -> service.enregistreExpédition(commande.getNumero()));
    }
    @Test
    void testEnregistreExpeditionCommandeNonExistante() {
        // on teste qu'on ne peux pas enregistrer une commande qui n'existe pas
        assertThrows(NoSuchElementException.class, () -> service.enregistreExpédition(-1));
    }
}
