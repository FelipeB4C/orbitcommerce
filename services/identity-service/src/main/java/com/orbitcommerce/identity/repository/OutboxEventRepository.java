package com.orbitcommerce.identity.repository;

import com.orbitcommerce.identity.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByPublishedFalseOrderByCreatedAtAsc();

}
