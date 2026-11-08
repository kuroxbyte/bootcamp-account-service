package dev.bootcamp.accountservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/** Repositorio de Spring Data para {@link BankAccountDocument}. */
public interface BankAccountMongoRepository
        extends ReactiveMongoRepository<BankAccountDocument, String> {
    /** Busca los productos del cliente. */
    Flux<BankAccountDocument> findByCustomerId(String customerId);
}
