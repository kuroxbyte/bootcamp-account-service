package dev.bootcamp.accountservice.infrastructure.messaging;

import dev.bootcamp.accountservice.application.port.DebitCardEventPublisher;
import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementa el puerto {@link DebitCardEventPublisher} publicando en el tópico
 * {@code debit-card-events}. Cada evento lleva el estado completo de la tarjeta y la clave del
 * mensaje es el id del cliente. El contrato está en {@code docs/events/asyncapi.yaml} del
 * repositorio raíz.
 *
 * <p>Si Kafka no responde ({@code max.block.ms}, 1 s en config-repo), el error se registra y la
 * operación no falla: el evento se pierde (limitación conocida; evitarla requiere el patrón
 * outbox).
 */
@Slf4j
@Component
public class DebitCardKafkaEventPublisher implements DebitCardEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final Clock clock;
    private final String topic;

    /**
     * Crea el publicador con el tópico configurado.
     */
    public DebitCardKafkaEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            JsonMapper jsonMapper,
            Clock clock,
            @Value("${messaging.topics.debit-card-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
        this.topic = topic;
    }

    @Override
    public Completable publishIssued(DebitCard debitCard) {
        return publish("DebitCardIssued", debitCard);
    }

    @Override
    public Completable publishAccountsUpdated(DebitCard debitCard) {
        return publish("DebitCardAccountsUpdated", debitCard);
    }

    private Completable publish(String eventType, DebitCard debitCard) {
        return Completable.defer(() -> {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("eventType", eventType);
            event.put("eventId", UUID.randomUUID().toString());
            event.put("debitCardId", debitCard.getId());
            event.put("customerId", debitCard.getCustomerId());
            event.put("primaryAccountId", debitCard.getPrimaryAccountId());
            event.put("accountIds", debitCard.getAccountIds());
            event.put("occurredAt", Instant.now(clock).toString());
            return Completable.fromCompletionStage(kafkaTemplate.send(
                    topic,
                    debitCard.getCustomerId(),
                    jsonMapper.writeValueAsString(event)));
        })
                // send() puede bloquear mientras busca al broker: fuera del hilo de eventos.
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> log.info(
                        "{} published debitCardId={}",
                        eventType,
                        debitCard.getId()))
                .onErrorComplete(error -> {
                    log.error(
                            "{} could not be published debitCardId={}",
                            eventType,
                            debitCard.getId(),
                            error);
                    return true;
                });
    }
}
