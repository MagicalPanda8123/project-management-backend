package org.magicalpanda.projectmanagementbackend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/protected-resource")
    public Map<String, Object> protectedResource(Authentication authentication) {

        return Map.of(
                "authenticated", true,
                "principal", authentication.getPrincipal(),
                "authorities", authentication.getAuthorities()
        );
    }


}
