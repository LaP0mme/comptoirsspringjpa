package comptoirs.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import comptoirs.entity.Ligne;
import comptoirs.entity.Produit;
import comptoirs.dao.*;
import comptoirs.dto.*;

import jakarta.validation.constraints.AssertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolationException;

import java.util.List;
import java.util.NoSuchElementException;

@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class LigneServiceTest {
    static final int NUMERO_COMMANDE_DEJA_LIVREE = 99999;
    static final int NUMERO_COMMANDE_PAS_LIVREE  = 99998;
    static final int REFERENCE_PRODUIT_DISPONIBLE_1 = 93;
    static final int REFERENCE_PRODUIT_DISPONIBLE_2 = 94;
    static final int REFERENCE_PRODUIT_DISPONIBLE_3 = 95;
    static final int REFERENCE_PRODUIT_DISPONIBLE_4 = 96;
    static final int REFERENCE_PRODUIT_INDISPONIBLE = 97;
    static final int UNITES_COMMANDEES_AVANT = 0;

    @Autowired
    LigneService service;
    @Autowired
    ProduitRepository produitDao;
    @Autowired
    CommandeRepository commandeService;
    @Test
    void onPeutAjouterDesLignesSiPasLivre() {
        var ligne = service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_1, 1);
        assertNotNull(ligne.getId(),
        "La ligne doit être enregistrée, sa clé générée"); 
    }

    @Test
    void laQuantiteEstPositive() {
        assertThrows(ConstraintViolationException.class, 
            () -> service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_1, 0),
            "La quantite d'une ligne doit être positive");
    }
    @Test
    void testAjouterLigneDejaLivree(){
        assertThrows(UnsupportedOperationException.class, () ->
                service.ajouterLigne(NUMERO_COMMANDE_DEJA_LIVREE,98,1));
    }
    @Test
    void testAjouterLigneQuantiteNegative(){
        assertThrows(UnsupportedOperationException.class, () ->
                service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE,98,0));
    }
    @Test
    void testAjouterLignePasAssezDeProduits(){
        assertThrows(UnsupportedOperationException.class, () ->
                service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE,98,900));
    }
    @Test
    void testAjouterLigneProduitInexistant(){
        assertThrows(NoSuchElementException.class, () ->
                service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE,1029,1));
    }
    @Test
    void testAjouterLigneCommandeInexistante(){
        assertThrows(NoSuchElementException.class, () ->
                service.ajouterLigne(-1,98,1));
    }
}
