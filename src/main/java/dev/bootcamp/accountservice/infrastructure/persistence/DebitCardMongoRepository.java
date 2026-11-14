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
     * Tarjetas activas (sin fecha de baja) del cliente.
     */
    Flux<DebitCardDocument> findByCustomerIdAndClosedAtIsNull(String customerId);

    /**
     * Indica si alguna tarjeta activa tiene asociada la cuenta; MongoDB busca el valor dentro de la
     * lista {@code accountIds}, que las tarjetas dadas de baja no tienen.
     */
    Mono<Boolean> existsByAccountIds(String accountId);
}
