package br.com.romulopenha.gerenciartarefas.infrastructure;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * Resolve o tenant a partir da identidade autenticada, nunca de um valor arbitrario enviado pelo cliente.
 * O header legado X-Tenant-Id, quando informado, so e aceito se coincidir com a identidade autenticada.
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class TenantFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    private final SecurityIdentity securityIdentity;

    @Inject
    public TenantFilter(SecurityIdentity securityIdentity) {
        this.securityIdentity = securityIdentity;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (securityIdentity == null || securityIdentity.isAnonymous()) {
            return;
        }

        String tenantId = securityIdentity.getPrincipal() == null
                ? null : securityIdentity.getPrincipal().getName();
        if (tenantId == null || tenantId.isBlank()) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Tenant ausente na identidade autenticada")
                    .build());
            return;
        }

        String requestedTenant = requestContext.getHeaderString(TENANT_HEADER);
        if (requestedTenant != null && !requestedTenant.isBlank() && !tenantId.equals(requestedTenant)) {
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity("Tenant solicitado nao pertence a identidade autenticada")
                    .build());
            return;
        }

        TenantContext.setCurrentTenant(tenantId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        TenantContext.clear();
    }
}
