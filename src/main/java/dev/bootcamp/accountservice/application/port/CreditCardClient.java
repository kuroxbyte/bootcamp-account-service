package dev.bootcamp.accountservice.application.port;

import io.reactivex.rxjava3.core.Single;

/**
 * Consulta de tarjetas de crédito en credit-service.
 */
public interface CreditCardClient {

    /**
     * Indica si el cliente tiene al menos una tarjeta de crédito con el banco.
     *
     * @return true si tiene alguna tarjeta, false si no tiene ninguna
     */
    Single<Boolean> hasCreditCard(String customerId);
}
