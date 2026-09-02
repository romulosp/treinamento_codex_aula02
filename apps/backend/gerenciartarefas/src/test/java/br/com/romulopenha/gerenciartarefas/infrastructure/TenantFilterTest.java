package br.com.romulopenha.gerenciartarefas.infrastructure;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Conjunto de testes automatizados para TenantFilterTest.
 */
class TenantFilterTest {
    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void usaPrincipalComoTenantEesvaziaContextoNaResposta() throws Exception {
        var identity = identity("tenant-a", false);
        var request = mock(ContainerRequestContext.class);
        var response = mock(ContainerResponseContext.class);
        when(request.getHeaderString("X-Tenant-Id")).thenReturn("tenant-a");

        var filter = new TenantFilter(identity);
        filter.filter(request);

        assertEquals("tenant-a", TenantContext.getCurrentTenant());
        filter.filter(request, response);
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    void rejeitaHeaderDeOutroTenant() throws Exception {
        var identity = identity("tenant-a", false);
        var request = mock(ContainerRequestContext.class);
        when(request.getHeaderString("X-Tenant-Id")).thenReturn("tenant-b");

        new TenantFilter(identity).filter(request);

        verify(request).abortWith(any());
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    void naoCriaContextoParaIdentidadeAnonima() throws Exception {
        var request = mock(ContainerRequestContext.class);

        new TenantFilter(identity("anonymous", true)).filter(request);

        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    void aceitaTenantDoPrincipalQuandoHeaderNaoFoiInformado() throws Exception {
        var request = mock(ContainerRequestContext.class);
        when(request.getHeaderString("X-Tenant-Id")).thenReturn(null);

        new TenantFilter(identity("tenant-a", false)).filter(request);

        assertEquals("tenant-a", TenantContext.getCurrentTenant());
    }

    @Test
    void rejeitaIdentidadeSemPrincipal() throws Exception {
        var identity = mock(SecurityIdentity.class);
        when(identity.isAnonymous()).thenReturn(false);
        when(identity.getPrincipal()).thenReturn(null);
        var request = mock(ContainerRequestContext.class);

        new TenantFilter(identity).filter(request);

        verify(request).abortWith(any());
        assertNull(TenantContext.getCurrentTenant());
    }

    private SecurityIdentity identity(String name, boolean anonymous) {
        var identity = mock(SecurityIdentity.class);
        when(identity.isAnonymous()).thenReturn(anonymous);
        when(identity.getPrincipal()).thenReturn((Principal) () -> name);
        return identity;
    }
}
