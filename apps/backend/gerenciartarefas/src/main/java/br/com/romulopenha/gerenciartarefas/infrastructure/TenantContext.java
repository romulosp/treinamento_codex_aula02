package br.com.romulopenha.gerenciartarefas.infrastructure;

/**
 * Armazena temporariamente o identificador do tenant corrente no contexto da thread.
 * O valor é definido por {@link TenantFilter} para cada requisição e removido ao final dela.
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
