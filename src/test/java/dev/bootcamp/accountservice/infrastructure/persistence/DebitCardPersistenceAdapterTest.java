package dev.bootcamp.accountservice.infrastructure.persistence;

import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest(properties = "spring.data.mongodb.auto-index-creation=true")
@Import(DebitCardPersistenceAdapter.class)
class DebitCardPersistenceAdapterTest {

    @Container @ServiceConnection static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Autowired private DebitCardPersistenceAdapter adapter;

    @Autowired private DebitCardMongoRepository mongoRepository;

    @Autowired private ReactiveMongoTemplate mongoTemplate;

    /**
     * Vacía la colección y espera a que exista el índice único: la creación automática de índices
     * es asíncrona y podría no haber terminado antes de la primera prueba.
     */
    @BeforeEach
    void cleanCollectionWithUniqueIndex() {
        mongoRepository.deleteAll().block();
        MongoPersistentEntityIndexResolver resolver = new MongoPersistentEntityIndexResolver(
                mongoTemplate.getConverter().getMappingContext());
        resolver.resolveIndexFor(DebitCardDocument.class).forEach(index ->
                mongoTemplate.indexOps(DebitCardDocument.class).createIndex(index).block());
    }

    private static DebitCard card(String customerId, String primary, List<String> others) {
        DebitCard card = new DebitCard(customerId, primary, others);
        card.setIssuedAt(LocalDate.of(2026, 11, 15));
        return card;
    }

    @Test
    void savesAndFindsCardWithItsAccounts() {
        DebitCard saved = adapter.save(card("c1", "a1", List.of("a2"))).blockingGet();

        DebitCard found = adapter.findById(saved.getId()).blockingGet();
        assertThat(found.getCustomerId()).isEqualTo("c1");
        assertThat(found.getPrimaryAccountId()).isEqualTo("a1");
        assertThat(found.getAccountIds()).containsExactly("a1", "a2");
        assertThat(found.getIssuedAt()).isEqualTo(LocalDate.of(2026, 11, 15));
        adapter.findByCustomerId("c1").test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(1);
    }

    @Test
    void tellsIfAccountIsAlreadyAssociated() {
        adapter.save(card("c1", "a1", List.of("a2"))).blockingGet();

        adapter.existsByAccountId("a2").test().awaitDone(5, TimeUnit.SECONDS).assertValue(true);
        adapter.existsByAccountId("a3").test().awaitDone(5, TimeUnit.SECONDS).assertValue(false);
    }

    @Test
    void uniqueIndexRejectsTheSameAccountInTwoCards() {
        adapter.save(card("c1", "a1", List.of())).blockingGet();

        adapter.save(card("c1", "a1", List.of())).test().awaitDone(5, TimeUnit.SECONDS)
                .assertError(DuplicateKeyException.class);
    }

    @Test
    void closedCardKeepsItsAccountsButFreesThemForAnotherCard() {
        DebitCard first = adapter.save(card("c1", "a1", List.of("a2"))).blockingGet();
        first.close(LocalDate.of(2026, 12, 1));
        adapter.save(first).blockingGet();
        DebitCard other = adapter.save(card("c2", "x1", List.of())).blockingGet();
        other.close(LocalDate.of(2026, 12, 1));
        adapter.save(other).blockingGet();

        DebitCard reloaded = adapter.findById(first.getId()).blockingGet();
        assertThat(reloaded.isClosed()).isTrue();
        assertThat(reloaded.getAccountIds()).containsExactly("a1", "a2");
        adapter.existsByAccountId("a1").test().awaitDone(5, TimeUnit.SECONDS).assertValue(false);
        assertThat(adapter.findByCustomerId("c1").toList().blockingGet()).isEmpty();
        // La cuenta liberada se asocia a una tarjeta nueva.
        adapter.save(card("c1", "a1", List.of())).test().awaitDone(5, TimeUnit.SECONDS)
                .assertNoErrors();
    }
}
