package dev.bootcamp.accountservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Repositorio de Spring Data para {@link BankAccountDocument}.
 */
public interface BankAccountMongoRepository
        extends ReactiveMongoRepository<BankAccountDocument, String> {
    /**
     * Busca las cuentas activas (sin fecha de baja) del cliente.
     */
    Flux<BankAccountDocument> findByCustomerIdAndClosedAtIsNull(String customerId);
}
