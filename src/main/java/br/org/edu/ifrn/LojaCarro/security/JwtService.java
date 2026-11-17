
package br.org.edu.ifrn.LojaCarro.security;

import br.org.edu.ifrn.LojaCarro.model.Usuario;

import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String gerarToken(Usuario usuario) {

        if (usuario.getId() == null
                || !Boolean.TRUE.equals(usuario.getAtivo())
                || !("ADMIN".equals(usuario.getCargo())
                || "CLIENTE".equals(usuario.getCargo()))) {

            throw new IllegalArgumentException(
                    "Usuário inválido para autenticação."
            );
        }

        Instant agora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("loja-carros")
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(3600))
                .subject(usuario.getId().toString())
                .claim("nome", usuario.getNome())
                .claim("perfil", usuario.getCargo())
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}
