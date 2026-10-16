package com.example.diabcare.bootstrap;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/// Classe de config permettant de mettre sous /api toutes les classes annotées RestController, donc tous les endpoint Rest.
/// Permet de ne pas mettre `server.servlet.context-path` dans la conf spring, car s'applique à tout le contenu
/// du "war" (y compris la console h2 et spring actuator, ce qui n'aurait pas de sens).
@Configuration
public class ApiPathConfig implements WebMvcConfigurer {

    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api", HandlerTypePredicate.forAnnotation(RestController.class));
    }

}
