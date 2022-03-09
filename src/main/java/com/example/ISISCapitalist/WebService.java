package com.example.ISISCapitalist;

import com.example.world.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.lang.invoke.WrongMethodTypeException;

@RestController
@RequestMapping("adventureisis/generic")
@CrossOrigin
public class WebService {
    Services services;

    public WebService() {
        services = new Services();
    }

    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorld(@Context HttpServletRequest request) throws JAXBException {
        String username = request.getHeader("X-user");
        return Response.ok(services.getWorld(username)).build();
}

    @PUT
    @Path("world")
    public void putWorld(@Context HttpServletRequest request, World world) throws JAXBException {
        String username = request.getHeader("X-user");
        services.saveWorldToXml(world, username);
    }

    @PUT
    @Path("product")
    public void putProduct(@Context HttpServletRequest request, ProductType product) throws JAXBException {
        String username = request.getHeader("X-user");
        services.updateProduct(username, product);
    }

    @PUT
    @Path("manager")
    public void putManager(@Context HttpServletRequest request, PallierType manager) throws JAXBException {
        String username = request.getHeader("X-user");
        services.updateManager(username, manager);
    }

    @PUT
    @Path("upgrade")
    public void putUpgrade(@Context HttpServletRequest request, PallierType upgrade) throws JAXBException {
        String username = request.getHeader("X-user");
        services.addUpgrade(username, upgrade);
    }

    @PUT
    @Path("angel")
    public void putAngel(@Context HttpServletRequest request, PallierType angel) throws JAXBException {
        String username = request.getHeader("X-user");
        services.addAngelUpgrade(username, angel);
    }

    @DELETE
    @Path("world")
    public void deleteWorld(@Context HttpServletRequest request) throws JAXBException {
        String username = request.getHeader("X-user");
        services.deleteWorld(username);
    }
}
