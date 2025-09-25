package com.ecommerce.common.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventStoreRepository extends JpaRepository<EventStoreEntry, Long> {

    List<EventStoreEntry> findByAggregateIdOrderByVersionAsc(String aggregateId);

    List<EventStoreEntry> findByAggregateIdAndVersionGreaterThanOrderByVersionAsc(String aggregateId, Long version);

    @Query("SELECT e FROM EventStoreEntry e WHERE e.eventType = :eventType ORDER BY e.createdAt ASC")
    List<EventStoreEntry> findByEventTypeOrderByCreatedAtAsc(@Param("eventType") String eventType);

    List<EventStoreEntry> findAllByOrderByCreatedAtAsc();

    Long countByAggregateId(String aggregateId);
}