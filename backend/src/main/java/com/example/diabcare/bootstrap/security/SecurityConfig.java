package com.example.diabcare.bootstrap.security;

/**
 * Backend = resource server pur : il ne fait jamais de login, il valide des tokens
 * émis par Keycloak (realm "diabcare"). Stateless : pas de session HTTP côté back,
 * le front (Next.js + keycloak-js) porte le JWT à chaque appel.
 */
//@Configuration
//@EnableMethodSecurity // active @PreAuthorize sur les controllers
public class SecurityConfig {

//    @Bean
//    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // API stateless consommée par un client JS, pas de formulaire serveur
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/actuator/health").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
//        return http.build();
//    }
//
//    private JwtAuthenticationConverter jwtAuthenticationConverter() {
//        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
//        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
//        return converter;
//    }
}
