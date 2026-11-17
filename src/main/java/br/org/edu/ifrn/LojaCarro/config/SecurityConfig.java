
package br.org.edu.ifrn.LojaCarro.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.DispatcherType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.jwt.secret-base64}")
    private String segredo;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey() {

        byte[] chave = Base64.getDecoder().decode(segredo);

        if (chave.length < 32) {
            throw new IllegalStateException(
                    "A chave JWT deve possuir pelo menos 32 bytes."
            );
        }

        return new SecretKeySpec(chave, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey chave) {
        return new NimbusJwtEncoder(
                new ImmutableSecret<>(chave)
        );
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey chave) {

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withSecretKey(chave)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        "loja-carros"
                )
        );

        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter roles =
                new JwtGrantedAuthoritiesConverter();

        roles.setAuthoritiesClaimName("perfil");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(roles);

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .formLogin(AbstractHttpConfigurer::disable)

                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR
                        ).permitAll()

                        // Cadastro e login públicos
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/cadastro",
                                "/auth/login"
                        ).permitAll()

                        // Rotas administrativas
                        .requestMatchers(
                                "/carro/admin",
                                "/carro/admin/**",
                                "/carro/logs",
                                "/auth/clientes"
                        ).hasRole("ADMIN")

                        // Catálogo público
                        .requestMatchers(
                                HttpMethod.GET,
                                "/carro",
                                "/carro/listarCarros"
                        ).permitAll()

                        // Detalhes e fotografias públicas
                        .requestMatchers(
                                new RegexRequestMatcher(
                                        "^/carro/[0-9]+(?:/foto)?(?:\\?.*)?$",
                                        "GET"
                                )
                        ).permitAll()

                        // Cadastro de veículos
                        .requestMatchers(
                                HttpMethod.POST,
                                "/carro",
                                "/carro/salvar"
                        ).hasRole("ADMIN")

                        // Atualização de veículos
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/carro/{id}"
                        ).hasRole("ADMIN")

                        // Exclusão lógica
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/carro/{id}"
                        ).hasRole("ADMIN")

                        // Marcar como vendido
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/carro/{id}/vender"
                        ).hasRole("ADMIN")

                        // Bloqueia endpoints antigos
                        .anyRequest().denyAll()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }
}
