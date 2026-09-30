package com.orbitcommerce.identity.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitcommerce.identity.dto.EventEnvelope;
import com.orbitcommerce.identity.model.OutboxEvent;
import com.orbitcommerce.identity.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    @Value("${spring.application.name}")
    private String producer;

    @Value("${spring.kafka.event-version}")
    private int eventVersion;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;


    public OutboxPoller(OutboxEventRepository outboxEventRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }


    @Scheduled(fixedRate = 30000)
    @Transactional
    public void publishPendingEvents() {

        log.info("Publishing outbox events");

        List<OutboxEvent> pending = outboxEventRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : pending) {
            try {
                String envelope = buildEnvelope(event);
                String topic = event.getEventType();
                String key = event.getAggregateId().toString();

                kafkaTemplate.send(topic, key, envelope).get();

                event.markAsPublished();
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Error publishing event {} (tipo={})", event.getId(), event.getEventType(), e);
            }
        }
    }


    private String buildEnvelope(OutboxEvent event) throws JsonProcessingException {
        EventEnvelope<JsonNode> envelope = EventEnvelope.create(event, eventVersion, producer);
        return objectMapper.writeValueAsString(envelope);
    }

}
