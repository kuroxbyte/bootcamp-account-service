package dev.bootcamp.accountservice.infrastructure.messaging;

import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebitCardKafkaEventPublisherTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Mock private KafkaTemplate<String, String> kafkaTemplate;

    private DebitCardKafkaEventPublisher publisher;

    private DebitCard card;

    @BeforeEach
    void createPublisher() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC);
        publisher = new DebitCardKafkaEventPublisher(
                kafkaTemplate,
                jsonMapper,
                clock,
                "debit-card-events");
        card = new DebitCard("c1", "a1", List.of("a2"));
        card.setId("d1");
    }

    @Test
    void publishesTheWholeCardWithCustomerAsKey() {
        when(kafkaTemplate.send(eq("debit-card-events"), eq("c1"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        publisher.publishIssued(card).test().awaitDone(2, TimeUnit.SECONDS).assertComplete();

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("debit-card-events"), eq("c1"), json.capture());
        JsonNode event = jsonMapper.readTree(json.getValue());
        assertThat(event.get("eventType").asString()).isEqualTo("DebitCardIssued");
        assertThat(event.get("eventId").asString()).hasSize(36);
        assertThat(event.get("debitCardId").asString()).isEqualTo("d1");
        assertThat(event.get("primaryAccountId").asString()).isEqualTo("a1");
        assertThat(event.get("accountIds").size()).isEqualTo(2);
        assertThat(event.get("occurredAt").asString()).isEqualTo("2026-11-15T10:00:00Z");
    }

    @Test
    void publishesAccountsUpdatedAndDoesNotFailWhenKafkaIsDown() {
        when(kafkaTemplate.send(eq("debit-card-events"), eq("c1"), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("down")));

        publisher.publishAccountsUpdated(card).test().awaitDone(2, TimeUnit.SECONDS)
                .assertComplete();
    }

    @Test
    void publishesDebitCardClosed() {
        when(kafkaTemplate.send(eq("debit-card-events"), eq("c1"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        publisher.publishClosed(card).test().awaitDone(2, TimeUnit.SECONDS).assertComplete();

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("debit-card-events"), eq("c1"), json.capture());
        assertThat(jsonMapper.readTree(json.getValue()).get("eventType").asString())
                .isEqualTo("DebitCardClosed");
    }
}
