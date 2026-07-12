package br.com.cb.security.context;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.*;

@RequestScoped
public class SecurityContext {

    @Inject
    SecurityIdentity identity;

    public LoggedUser getCurrentUser() {

        if (identity == null || identity.isAnonymous()) {
            throw new AuthenticationFailedException();
        }

        JsonWebToken jwt = (JsonWebToken) identity.getPrincipal();

        Map<String, Object> claims = new HashMap<>();

        jwt.getClaimNames()
                .forEach(name -> claims.put(name, jwt.getClaim(name)));

        return new LoggedUser(
                jwt.getSubject(),
                jwt.getClaim("email"),
                Set.copyOf(identity.getRoles()),
                Map.copyOf(claims)
        );
    }
}