package com.example.ISISCapitalist;

import com.example.world.PallierType;
import com.example.world.ProductType;
import com.example.world.TyperatioType;
import com.example.world.World;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class Services {

    InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
    private String path = "src/main/resources";

    /*
    Ici on retrouve toutes les fonctions permettant de retrouver les managers, les produits, les anges ou les prix en fonction de certains paramètres
    Pour avoir un code plus lisible dans les fonctions servant à l'API REST appelées dans WebService
    */
    public ProductType findProductById(World world, int id) {
        for (ProductType p : world.getProducts().getProduct()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    double costOfProduct(ProductType product, int qte) {
        double costProduct = product.getCout();
        return (costProduct * (1 - Math.pow(product.getCroissance(), qte))) / (1 - product.getCroissance());
    }

    public PallierType findAngel(World world, String name) {
        for (PallierType ange : world.getAngelupgrades().getPallier()) {
            if (name.equals(ange.getName())) {
                return ange;
            }
        }
        return null;
    }

    // permet d'ajouter un upgrade au produit en fonction de son type
    public void addUnlock(PallierType pallier, ProductType product) {
        pallier.setUnlocked(true);
        if (pallier.getTyperatio() == TyperatioType.VITESSE) {
            double vitesse = product.getVitesse();
            vitesse = vitesse * pallier.getRatio();
            product.setVitesse((int) vitesse);
        }
        if (pallier.getTyperatio() == TyperatioType.GAIN) {
            double revenu = product.getRevenu();
            revenu = revenu * pallier.getRatio();
            product.setRevenu(revenu);
        }
    }

    /*
    Ici on retrouve toutes les fonctions appelées dans WebService qui servent à fabriquer notre API REST
    */

    World readWorldFromXml(String pseudo) {
        JAXBContext jaxbContext;
        World world = new World();
        try {
            try {
                jaxbContext = JAXBContext.newInstance(World.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                File f = new File(path + "/" + pseudo + "-world.xml");
                world = (World) jaxbUnmarshaller.unmarshal(f);
                return world;
            } catch (Exception ex) {
                jaxbContext = JAXBContext.newInstance(World.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                world = (World) jaxbUnmarshaller.unmarshal(input);
                return world;
            }
        } catch (Exception ex) {
            System.out.println("Erreur lecture du fichier : " + ex.getMessage());
            ex.printStackTrace();
        }

        return world;
    }

    void saveWorldToXml(World world, String pseudo) {
        try {
            OutputStream output = new FileOutputStream(path + "/" + pseudo + "-world.xml");
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Marshaller m = cont.createMarshaller();
            m.marshal(world, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    World getWorld(String username) {
        World world = readWorldFromXml(username);
        updateWorld(world);
        saveWorldToXml(world, username);
        return world;
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
            world.setMoney(world.getMoney() - costOfProduct(product, qteChange));
            product.setCout(product.getCout() * Math.pow(product.getCroissance(), qteChange));
            product.setQuantite(newproduct.getQuantite());
        } else {
            // initialiser product.timeleft à product.vitesse pour lancer la production
            product.setTimeleft(product.getVitesse());
            world.setMoney(world.getMoney() + product.getRevenu() * product.getQuantite());
        }

        for (PallierType u : product.getPalliers().getPallier()) {
            // si l'unlock n'est pas encore déloqué et que la quantité est supérieure au seuil
            if (!u.isUnlocked() && product.getQuantite() < u.getSeuil() && newproduct.getQuantite() >= u.getSeuil()) {
                addUnlock(u, product);
            }
        }
        // sauvegarder les changements du monde
        saveWorldToXml(world, username);
        return true;
    }

    // prend en paramètre le pseudo du joueur et le manager acheté.
    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateManager(String username, PallierType newmanager) {
        System.out.println("Début de l'ajout du manager");
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager équivalent à celui passé en paramètre
        for (PallierType manager : world.getManagers().getPallier()) {
            System.out.println("Manager : " + manager.getName());
            if (manager.getName().equals(newmanager.getName())) {
                // débloquer ce manager
                // trouver le produit correspondant au manager
                System.out.println("Manager correspondant");
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
            } else {
                System.out.println("Manager non correspondant");
            }
        }
        world.setLastupdate(System.currentTimeMillis());
        this.saveWorldToXml(world, username);
        return true;
    }

    void updateWorld(World world) {
        System.out.println("DEBUT DE L'UPDATE");
        long diffTime = System.currentTimeMillis() - world.getLastupdate();
        int angeBonus = world.getAngelbonus();
        double moneyToAdd = 0;
        List<ProductType> produits = world.getProducts().getProduct();
        for (ProductType p : produits) {
            //System.out.println("Produit : " + p.getName());
            long vitesse = p.getVitesse();
            long nbProd = diffTime / vitesse;
            if (!p.isManagerUnlocked()) {
                // Le produit n'a pas de manager
                //System.out.println("Manager non débloqué");
                if (p.getTimeleft() != 0 && p.getTimeleft() <= diffTime) {
                    // le produit a été crée
                    double anges = 1 + world.getActiveangels() * angeBonus / 100;
                    moneyToAdd = p.getRevenu() * p.getQuantite() *angeBonus;
                    p.setTimeleft(0);
                } else {
                    // le produit n'a pas été crée
                    long newTimeLeft = p.getTimeleft() - diffTime;
                    p.setTimeleft(newTimeLeft);
                }
            } else {
                //System.out.println("Manager débloqué");
                // le produit a un manager
                if (diffTime/p.getVitesse() > 0) {
                    // des produits ont été créés
                    double anges = 1 + world.getActiveangels()*angeBonus / 100;
                    moneyToAdd += (diffTime/p.getVitesse() + p.getQuantite()) * p.getRevenu() * anges;
                }
                else {
                    // le produit n'a pas eu le temps d'être créé
                    long tempsRestant = p.getTimeleft() - diffTime;
                    p.setTimeleft(tempsRestant);
                }
            }
        }
        world.setScore((world.getScore() + moneyToAdd));
        world.setMoney(world.getMoney()+moneyToAdd);
        world.setLastupdate(System.currentTimeMillis());
    }

    public Boolean addUpgrade(String username, PallierType newUpgrade) {
        System.out.println("Début de l'ajout de l'upgrade");
        World world = getWorld(username);
        // trouver dans ce monde l'upgrade
        for (PallierType p : world.getUpgrades().getPallier()) {
            System.out.println("Upgrade : " + p.getName());
            if (newUpgrade.getName().equals(p.getName())) {
                System.out.println("Correspondant");
                p.setUnlocked(true);
                System.out.println("L'upgrade est unlocké");
                // trouver le produit correspondant a l'upgrade
                ProductType product = findProductById(world, p.getIdcible());
                if (product == null) {
                    return false;
                }
                // soustraire de l'argent du joueur le cout du cash upgrade
                double money = world.getMoney();
                double seuil = p.getSeuil();

                double newMoney = money - seuil;
                world.setMoney(newMoney);

                // modifier le produit en fonction de l'upgrade
                addUnlock(p, product);
            } else {
                System.out.println("Non correspondant");
            }
        }
        // sauvegarder les changements au monde
        saveWorldToXml(world, username);
        return true;
    }

    public Boolean addAngelUpgrade(String username, PallierType angel) {
        World world = getWorld(username);
        PallierType ange = findAngel(world, angel.getName());
        if (ange == null) {
            return false;
        }
        // on débloque cet ange
        ange.setUnlocked(true);
        int angels = ange.getSeuil();
        double totalAngels = world.getTotalangels();
        double newtotalangel = totalAngels - angels;
        if (ange.getTyperatio() == TyperatioType.ANGE) {
            int angeBonus = world.getAngelbonus();
            angeBonus += ange.getRatio();
            world.setAngelbonus(angeBonus);
        } else {
            addUpgrade(username, ange);
        }
        world.setActiveangels(newtotalangel);
        saveWorldToXml(world, username);
        return true;
    }

    public void deleteWorld(String username) throws JAXBException {
        // on recalcule les anges actifs et totaux, le score
        try {
            World world = readWorldFromXml(username);
            double anges = Math.round(150 * Math.sqrt((world.getScore()) / Math.pow(10, 15))) - world.getTotalangels();
            double angesActifs = world.getActiveangels() + anges;
            double angesTotaux = world.getTotalangels() + anges;
            double score = world.getScore();

            //on recrée une instance du monde ou l'on applique les résultats
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            world = (World) u.unmarshal(input);
            world.setActiveangels(angesActifs);
            world.setTotalangels(angesTotaux);
            world.setScore(score);
            world.setMoney(0);
            saveWorldToXml(world, username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
