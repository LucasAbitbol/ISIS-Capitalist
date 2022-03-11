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

    @GetMapping(value = "/world", produces = {"application/xml","application/json"})
    public ResponseEntity<World> getWorld(@RequestHeader(value="X-User", required = false) String username) {
        World world = services.getWorld(username);
        return ResponseEntity.ok(world);
    }

    @PutMapping(value = "/product", consumes ={"application/xml","application/json"})
    public ProductType putProduct(@RequestHeader(value = "X-User", required = false) String username,@RequestBody ProductType produit) {
        services.updateProduct(username,produit);
        return produit;

    }

    @PutMapping(value = "/manager", consumes ={"application/xml","application/json"})
    public PallierType putManager(@RequestHeader(value = "X-User", required = false) String username, @RequestBody PallierType manager) {
        services.updateManager(username, manager);
        return manager;
    }

    @PutMapping(value = "/upgrade", consumes = {"application/xml","application/json"})
    public PallierType putUpgrade(@RequestHeader(value = "X-User", required = false) String username, @RequestBody PallierType newUpgrade) {
        services.addUpgrade(username, newUpgrade);
            return newUpgrade;
    }

    @PutMapping(value = "/angelUpgrade", consumes = {"application/xml","application/json"})
    public PallierType putAngelUpgrade(@RequestHeader(value = "X-User", required = false) String username, @RequestBody PallierType newAngel) {
        services.addAngelUpgrade(username, newAngel);
            return newAngel;
    }

    @DeleteMapping(value = "/world", produces = {"application/xml","application/json"})
    public void deleteWorld(@RequestHeader(value="X-User", required = true) String username) throws JAXBException {
        services.deleteWorld(username);
    }
}
