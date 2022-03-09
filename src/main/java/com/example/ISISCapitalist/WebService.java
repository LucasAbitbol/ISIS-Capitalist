package com.example.ISISCapitalist;

import com.example.world.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("adventureisis/generic")
@CrossOrigin
public class WebService {
    Services services;

    public WebService() {
        services = new Services();
    }

    @GetMapping(value = "world", produces = {"application/xml", "application/json"})

    public ResponseEntity<World> getWorld(@RequestHeader(value = "X-User", required = false) String username) {
        World world = services.getWorld(username);
        return ResponseEntity.ok(world);
    }

    @PutMapping(value = "/product", consumes = "application/json")
    ResponseEntity<World> newproduct(String username, ProductType product) {
        services.updateProduct(username, product);
        return ResponseEntity.ok(services.getWorld(username));
    }

    @PutMapping(value = "/manager", consumes = "application/json")
    ResponseEntity<World> newmanager(String username, PallierType manager) {
        services.updateManager(username, manager);
        return ResponseEntity.ok(services.getWorld(username));
    }

    @PutMapping(value = "/upgrade", consumes = "application/json")
    ResponseEntity<World> newupgrade(String username, PallierType upgrade) {
        services.addUpgrade(username, upgrade);
        return ResponseEntity.ok(services.getWorld(username));
    }
}
