package com.example.ISISCapitalist;

import com.example.world.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.List;

public class Services {

    private World world = new World();
    private String path = "src/main/resources";

    World readWorldFromXml(String pseudo){
        JAXBContext jaxbContext;
        try{
            try {
                jaxbContext = JAXBContext.newInstance(World.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                File f = new File(path+"/" + pseudo +"-world.xml");
                world = (World) jaxbUnmarshaller.unmarshal(f);
                return world;
            } catch (Exception ex) {
                jaxbContext = JAXBContext.newInstance(World.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                // File f = new File(path+"/world.xml");
                InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
                world = (World) jaxbUnmarshaller.unmarshal(input);
                return world;
            }
        }catch (Exception ex){
            System.out.println("Erreur lecture du fichier:"+ex.getMessage());
            ex.printStackTrace();
        }

        return world;
    }

    void saveWorldToXml(World world, String pseudo) {
        try {
            OutputStream output = new FileOutputStream(path + "/" + pseudo+"-world.xml");
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Marshaller m = cont.createMarshaller();
            m.marshal(world, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    World getWorld(String username) {
        World world = readWorldFromXml(username);
        saveWorldToXml(world, username);
        return world;
    }

    public ProductType findProductById(World world, int id) {
        for (ProductType p : world.getProducts().getProduct()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }


    public PallierType findManagerByName(World world, String managerName){
        for (PallierType m : world.getManagers().getPallier()) {
            if (m.getName()==managerName) {
                return m;
            }
        }
        return null;
    }

    double getCostOfProduct(ProductType product, int qte) {
        double costProduct = product.getCout();
        return (costProduct * (1-Math.pow(product.getCroissance(), qte))) / (1-product.getCroissance());
    }

    public Boolean updateProduct(String username, ProductType newproduct) {
        World world = getWorld(username);
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) {
            return false;
        }
        // calculer la variation de quantité. Si elle est positive c'est
        // que le joueur a acheté une certaine quantité de ce produit
        // sinon c’est qu’il s’agit d’un lancement de production.
        int qteChange = newproduct.getQuantite() - product.getQuantite();
        if (qteChange > 0) {
        // soustraire de l'argent du joueur le cout de la quantité achetée et mettre à jour la quantité de product
            world.setMoney(world.getMoney()-(getCostOfProduct(product, qteChange)));
            product.setCout(product.getCout()*Math.pow(product.getCroissance(),qteChange));
            product.setQuantite(newproduct.getQuantite());
        }
        else {
        // initialiser product.timeleft à product.vitesse pour lancer la production
            product.setTimeleft(product.getVitesse());
            world.setMoney(world.getMoney() + (product.getRevenu() * product.getQuantite()));
        }

        List<PallierType> unlocks = (List<PallierType>) product.getPalliers().getPallier();
        for (PallierType u : unlocks) {
            // si l'unlock n'est pas encore déloqué et que la quantité est supérieure au seuil
            if (u.isUnlocked() == false && product.getQuantite() >= u.getSeuil()) {
                addUpgrade(u, product);
            }
        }

        // sauvegarder les changements du monde
        saveWorldToXml(world, username);
        return true;
    }

    // prend en paramètre le pseudo du joueur et le manager acheté.
    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateManager(String username, PallierType newmanager) {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager équivalent à celui passé
        // en paramètre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            return false;
        }
        // débloquer ce manager
        // trouver le produit correspondant au manager
        manager.setUnlocked(true);
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        // débloquer le manager de ce produit
        // soustraire de l'argent du joueur le cout du manager
        // sauvegarder les changements au monde
        product.setManagerUnlocked(true);
        world.setMoney(world.getMoney() - manager.getSeuil());
        saveWorldToXml(world, username);
        return true;
    }

    void updateWorld(String username) {
        World world = getWorld(username);
        long diff = System.currentTimeMillis() - world.getLastupdate();
        int angeBonus = world.getAngelbonus();
        List<ProductType> produits = (List<ProductType>) world.getProducts();
        for (ProductType p : produits) {
            // Le produit n'a pas de manager
            if (!p.isManagerUnlocked()) {
                // Le produit a été créé
                if (p.getTimeleft() != 0 && p.getTimeleft() < diff) {
                    double newScore = world.getScore() + p.getRevenu() * (1 + world.getActiveangels() * angeBonus / 100);
                    world.setScore(newScore);
                    double newMoney = world.getMoney() + p.getRevenu() * (1 + world.getActiveangels() * angeBonus / 100);
                    world.setMoney(newMoney);
                } // Le produit n'a pas été créé
                else {
                    long newTimeLeft = p.getTimeleft() - diff;
                    p.setTimeleft(newTimeLeft);
                }
            } else {
                long vitesse = p.getVitesse();
                long nbProd = (int) diff / vitesse;
                // On met à jour le score et l'argent du monde en fonction du nombre de produit créé
                double newScore = world.getScore() + (p.getRevenu() * nbProd * (1 + world.getActiveangels() * angeBonus / 100));
                world.setScore(newScore);
                double newMoney = world.getMoney() + (p.getRevenu() * nbProd * (1 + world.getActiveangels() * angeBonus / 100));
                world.setMoney(newMoney);

                //On calcule le temps restant
                long timeRestant = vitesse - diff % vitesse;
                p.setTimeleft(timeRestant);
            }
        }
        world.setLastupdate(System.currentTimeMillis());
    }

    // permet d'ajouter un upgrade au produit
    public void addUpgrade(PallierType pallier, ProductType p) {
        pallier.setUnlocked(true);
        if (pallier.getTyperatio() == TyperatioType.VITESSE) {
            double vitesse = p.getVitesse();
            vitesse = (int) (vitesse * pallier.getRatio());
            p.setVitesse((int) vitesse);
        }
        if (pallier.getTyperatio() == TyperatioType.GAIN) {
            double revenu = p.getRevenu();
            revenu = revenu * pallier.getRatio();
            p.setRevenu(revenu);
        }
    }
}
