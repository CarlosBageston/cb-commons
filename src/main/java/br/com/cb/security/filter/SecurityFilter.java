package br.com.cb.security.filter;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Provider
@Priority(Priorities.AUTHENTICATION + 1)
public class SecurityFilter implements ContainerRequestFilter {

    @Context
    ResourceInfo resourceInfo;

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;

    @ConfigProperty(name = "cb.security.required-claims", defaultValue = "")
    List<String> requiredClaims;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (isPublic()) {
            return;
        }

        if (identity.isAnonymous()) {
            abort(
                    requestContext,
                    "Usuário não autenticado."
            );
            return;
        }

        String missingClaim = findMissingClaim();
        if (missingClaim != null) {
            abort(
                    requestContext,
                    "Claim obrigatório ausente no token: " + missingClaim
            );
        }
    }

    private boolean isPublic() {
        if (resourceInfo == null) {
            return false;
        }

        if (resourceInfo.getResourceMethod() == null) {
            return false;
        }
        return resourceInfo.getResourceMethod().isAnnotationPresent(PermitAll.class)
                || resourceInfo.getResourceClass().isAnnotationPresent(PermitAll.class);
    }

    private String findMissingClaim() {
        if (requiredClaims == null || requiredClaims.isEmpty()) {
            return null;
        }

        jwt = (JsonWebToken) identity.getPrincipal();

        for (String claim : requiredClaims) {
            String trimmed = claim.trim();
            if (trimmed.isEmpty()) continue;

            Object value = jwt.getClaim(trimmed);
            if (value == null || value.toString().isBlank()) {
                return trimmed;
            }
        }
        return null;
    }

    private void abort(
            ContainerRequestContext requestContext,
            String message
    ) {

        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .type(MediaType.APPLICATION_JSON)
                        .entity("""
                            {
                              "message": "%s"
                            }
                            """.formatted(message))
                        .build()
        );
    }
}