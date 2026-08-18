package com.nexus.backend.security;

/**
 * Contexto por-requisição com o tenant do usuário autenticado, extraído do JWT.
 * Toda consulta a dados de negócio deve passar por aqui em vez de confiar em
 * qualquer tenant informado pelo cliente.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Long get() {
        Long tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new IllegalStateException("Nenhum tenant no contexto da requisição atual");
        }
        return tenantId;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
