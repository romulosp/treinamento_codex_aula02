package br.com.romulopenha.gerenciartarefas.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários de {@link TenantContext}.
 */
public class TenantContextTest {

    private static final String TENANT_ID = "tenant-123";

    @BeforeEach
    public void setUp() {
        // Ensure a clean state before each test
        TenantContext.clear();
    }

    @AfterEach
    public void tearDown() {
        // Clean up after each test
        TenantContext.clear();
    }

    @Test
    public void testSetAndGetCurrentTenant() {
        TenantContext.setCurrentTenant(TENANT_ID);
        Assertions.assertEquals(TENANT_ID, TenantContext.getCurrentTenant(), "Tenant ID should be stored and retrievable");
    }

    @Test
    public void testClearRemovesTenant() {
        TenantContext.setCurrentTenant(TENANT_ID);
        TenantContext.clear();
        Assertions.assertNull(TenantContext.getCurrentTenant(), "Tenant ID should be null after clear()");
    }
}
