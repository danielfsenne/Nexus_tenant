package com.nexus.backend.security;

/**
 * Contexto por-requisição com o usuário autenticado (id/email), extraído do JWT.
 * Usado principalmente para atribuir eventos de auditoria a quem os realizou.
 */
public final class CurrentUserContext {

    public record CurrentUser(Long id, String email) {
    }

    private static final ThreadLocal<CurrentUser> CURRENT_USER = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(Long id, String email) {
        CURRENT_USER.set(new CurrentUser(id, email));
    }

    public static CurrentUser get() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
