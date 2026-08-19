package com.nexus.backend.domain;

/**
 * Limites de uso por plano. Um valor null significa "sem limite".
 */
public record PlanLimits(Integer maxUsers, Integer maxCustomers, Integer maxProducts) {

    public static PlanLimits of(Plan plan) {
        return switch (plan) {
            case FREE -> new PlanLimits(3, 100, 100);
            case PRO -> new PlanLimits(20, 10_000, null);
            case ENTERPRISE -> new PlanLimits(null, null, null);
        };
    }
}
