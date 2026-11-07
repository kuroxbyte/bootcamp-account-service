package dev.bootcamp.accountservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface BankAccountMongoRepository extends ReactiveMongoRepository<BankAccountDocument, String> {
    Flux<BankAccountDocument> findByCustomerId(String customerId);
}
