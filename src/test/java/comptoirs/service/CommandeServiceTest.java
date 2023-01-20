package comptoirs.service;

import comptoirs.entity.Ligne;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import comptoirs.dao.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class CommandeServiceTest {
    private static final String ID_PETIT_CLIENT = "0COM";
    private static final String ID_GROS_CLIENT = "2COM";
    private static final String VILLE_PETIT_CLIENT = "Berlin";
    private static final BigDecimal REMISE_POUR_GROS_CLIENT = new BigDecimal("0.15");


    @Autowired
    private CommandeService service;
    @Autowired
    private ProduitRepository produitDao;
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
    void testEnregistreExpedition() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        service.enregistreExpédition(commande.getNumero());
        assertEquals(LocalDate.now(),commande.getEnvoyeele(),
                "La date n'est pas la bonne");
    }

    @Test
    void testEnregistreExpeditionPasAssezDeProduits() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        var prod = produitDao.findById(98).orElseThrow();
        prod.setUnitesEnStock(0);
        var ligneCommande = new Ligne(commande, prod, 900000);
        // ajout de la nouvelle ligne dans la commande
        List<Ligne> listeLignes = commande.getLignes();
        listeLignes.add(ligneCommande);
        commande.setLignes(listeLignes);
        assertThrows(UnsupportedOperationException.class, () -> service.enregistreExpédition(commande.getNumero()));
    }
    @Test
    void actualisationStock(){
        var prod = produitDao.findById(98).orElseThrow();
        int stockBefore = prod.getUnitesEnStock();
        service.enregistreExpédition(99998);
        assertEquals(stockBefore-20, produitDao.findById(98).orElseThrow().getUnitesEnStock(),
                "le stock doit être actualisé avec 20 unités de moins");
    }
    @Test
    void testEnregistreExpeditionCommandeDejaEnvoyee() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        service.enregistreExpédition(commande.getNumero());
        assertThrows(UnsupportedOperationException.class, () -> service.enregistreExpédition(commande.getNumero()));
    }
    @Test
    void testEnregistreExpeditionCommandeNonExistante() {
        assertThrows(NoSuchElementException.class, () -> service.enregistreExpédition(-1));
    }
}
