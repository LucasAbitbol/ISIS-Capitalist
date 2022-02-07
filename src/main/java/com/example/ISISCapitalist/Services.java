package com.example.ISISCapitalist;

import com.example.world.World;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Services {
    World readWorldFromXml() {
        InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        World world = (World) u.unmarshal(new File(input));
    }

    void saveWordlToXml(World world) {
        OutputStream output = new FileOutputStream(newWorld);
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller m = cont.createMarshaller();
        m.marshal(films, new File("newWorld.xml"));
    }

    World getWorld() {
        readWorldFromXml();
    }
}
