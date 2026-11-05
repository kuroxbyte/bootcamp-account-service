package dev.bootcamp.accountservice.application.port;

import dev.bootcamp.accountservice.domain.model.customer.Customer;
import java.util.Optional;

/** Consulta de clientes en customer-service. */
public interface CustomerClient {
    Optional<Customer> findById(Long customerId);
}
