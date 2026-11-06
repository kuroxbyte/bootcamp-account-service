package dev.bootcamp.accountservice.application.port;

import dev.bootcamp.accountservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Maybe;

/** Consulta de clientes en customer-service. */
public interface CustomerClient {

    /**
     * @return el cliente, o vacío si no existe
     */
    Maybe<Customer> findById(String customerId);
}
