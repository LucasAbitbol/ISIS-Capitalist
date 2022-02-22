package com.example.ISISCapitalist;

import com.example.world.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;

public class Services {

    private World myworld = new World();

    World readWorldFromXml(String pseudo) {
        InputStream input;
        try {
            input = new FileInputStream(pseudo+"-world.xml");
        } catch (Exception e) {
            e.printStackTrace();
            input = getClass().getClassLoader().getResourceAsStream("world.xml");
        }
        try {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        myworld = (World) u.unmarshal(input);}
        catch (Exception e) {
            e.printStackTrace();
        }
        return myworld;
    }

    void saveWordlToXml(World world, String pseudo) {
        try {
            OutputStream output = new FileOutputStream(pseudo+"-world.xml");
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Marshaller m = cont.createMarshaller();
            m.marshal(world, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    World getWorld(String username) {
        return readWorldFromXml(username);
    }
}
