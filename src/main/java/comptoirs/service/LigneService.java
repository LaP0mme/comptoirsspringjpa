package comptoirs.service;

import comptoirs.dao.CommandeRepository;
import comptoirs.dao.LigneRepository;
import comptoirs.dao.ProduitRepository;
import comptoirs.entity.Ligne;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.util.*;
@Service
@Validated // Les contraintes de validatipn des méthodes sont vérifiées
public class LigneService {
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    private final CommandeRepository commandeDao;
    private final LigneRepository ligneDao;
    private final ProduitRepository produitDao;

    // @Autowired
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    public LigneService(CommandeRepository commandeDao, LigneRepository ligneDao, ProduitRepository produitDao) {
        this.commandeDao = commandeDao;
        this.ligneDao = ligneDao;
        this.produitDao = produitDao;
    }

    /**
     * <pre>
     * Service métier : 
     *     Enregistre une nouvelle ligne de commande pour une commande connue par sa clé,
     *     Incrémente la quantité totale commandée (Produit.unitesCommandees) avec la quantite à commander
     * Règles métier :
     *     - le produit référencé doit exister
     *     - la commande doit exister
     *     - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     *     - la quantité doit être positive
     *     - On doit avoir une quantite en stock du produit suffisante
     * <pre>
     * 
     *  @param commandeNum la clé de la commande
     *  @param produitRef la clé du produit
     *  @param quantite la quantité commandée (positive)
     *  @return la ligne de commande créée
     */
    @Transactional
   public Ligne ajouterLigne(Integer commandeNum, Integer produitRef, @Positive int quantite) {
        Ligne nouvelleLigne = null;
        if (quantite > 0) {
            // verification que la commande existe
            var commande = commandeDao.findById(commandeNum).orElseThrow();
            // verification du produit
            var produit = produitDao.findById(produitRef).orElseThrow();
            //verification que la commande n'est pas déjà partie
            if (commande.getEnvoyeele() != null) {
                throw new UnsupportedOperationException("la commande a déjà été envoyée");
            }
            // calcul du nouveau total des Unités commandées
            produit.setUnitesCommandees(produit.getUnitesCommandees() + quantite);
            // creation de la nouvelle ligne
            nouvelleLigne = new Ligne(commande, produit, quantite);
            // ajout de la nouvelle ligne dans la commande
            List<Ligne> listeLignes = commande.getLignes();
            listeLignes.add(nouvelleLigne);
            commande.setLignes(listeLignes);
        }
        //sauvegrade des Dao
        ligneDao.save(nouvelleLigne);
        return nouvelleLigne;
    }
}
