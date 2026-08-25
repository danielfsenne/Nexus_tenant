package com.nexus.backend.repository;

import com.nexus.backend.domain.Tenant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    /**
     * Trava a linha do tenant (SELECT ... FOR UPDATE) até o fim da transação
     * chamadora. Usado para serializar criações concorrentes de
     * clientes/produtos/usuários do mesmo tenant, evitando que duas
     * requisições simultâneas passem na checagem de limite do plano ao
     * mesmo tempo e ambas insiram, estourando o limite.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tenant t where t.id = :id")
    Optional<Tenant> lockById(@Param("id") Long id);
}
