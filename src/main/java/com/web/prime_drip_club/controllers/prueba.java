package com.web.prime_drip_club.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/prueba")
public class prueba {

    @GetMapping
    public String getMethodName() {
        return "Hola Mundo desde el controlador prueba";
    }

}
