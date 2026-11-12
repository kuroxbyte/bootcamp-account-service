package dev.bootcamp.accountservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio de Spring Data para {@link DebitCardDocument}.
 */
public interface DebitCardMongoRepository
        extends ReactiveMongoRepository<DebitCardDocument, String> {

    /**
     * Tarjetas del cliente.
     */
    Flux<DebitCardDocument> findByCustomerId(String customerId);

    /**
     * Indica si alguna tarjeta tiene asociada la cuenta; MongoDB busca el valor dentro de la
     * lista {@code accountIds}.
     */
    Mono<Boolean> existsByAccountIds(String accountId);
}
