package com.example.ISISCapitalist;

import com.example.world.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Services {

    private World myworld = new World;

    World readWorldFromXml() {
        try {
        InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        myworld = (World) u.unmarshal(input);
        }

        catch (Exception e) {
            e.printStackTrace();
        }
        return myworld;
    }

    void saveWordlToXml(World world) {
        OutputStream output = new FileOutputStream("newWorld.xml");
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller m = cont.createMarshaller();
        m.marshal(world, new File("newWorld.xml"));
    }

    World getWorld() {
        readWorldFromXml();
    }
}
