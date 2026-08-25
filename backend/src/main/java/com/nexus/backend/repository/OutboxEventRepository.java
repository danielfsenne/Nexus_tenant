package com.nexus.backend.repository;

import com.nexus.backend.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop50ByPublishedAtIsNullOrderByIdAsc();
}
