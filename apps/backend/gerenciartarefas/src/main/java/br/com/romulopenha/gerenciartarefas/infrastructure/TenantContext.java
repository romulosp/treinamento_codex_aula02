package br.com.romulopenha.gerenciartarefas.infrastructure;

/**
 * Simple ThreadLocal storage for the current tenant identifier.
 * The value is set by {@link TenantFilter} for each incoming request and cleared afterwards.
 */
public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
