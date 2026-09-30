package com.orbitcommerce.identity.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.orbitcommerce.identity.model.OutboxEvent;

import java.util.UUID;

public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        int eventVersion,
      //  @JsonFormat(shape = JsonFormat.Shape.STRING)
        String occurredAt,
        String traceId,
        String producer,
        T payload
) {

    public static EventEnvelope<JsonNode> create(
            OutboxEvent event,
            int eventVersion,
            String producer
    ) {

        String traceId = event.getTraceId() != null ? event.getTraceId() :
                UUID.randomUUID().toString().replace("-", "");

        return new EventEnvelope<>(
                event.getId(),
                event.getEventType(),
                eventVersion,
                event.getCreatedAt().toString(),
                traceId,
                producer,
                event.getPayload()
        );
    }

}
