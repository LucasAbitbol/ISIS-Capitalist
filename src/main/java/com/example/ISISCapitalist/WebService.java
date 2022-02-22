package com.example.ISISCapitalist;

import com.example.world.World;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("adventureisis/generic")
@CrossOrigin
public class WebService {
    Services services;

    public WebService() {
        services = new Services();
    }

    @GetMapping(value = "world", produces = {"application/xml", "application/json"})

    public ResponseEntity<World> getWorld() {
        World world = services.getWorld();
        return ResponseEntity.ok(world);
    }
}
