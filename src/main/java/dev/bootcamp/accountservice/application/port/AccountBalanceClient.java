package dev.bootcamp.accountservice.application.port;

import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;

/**
 * Consulta del saldo de una cuenta en transaction-service, que guarda sus movimientos.
 */
public interface AccountBalanceClient {

    /**
     * Saldo actual de la cuenta.
     *
     * @param accountId  cuenta consultada
     * @param customerId dueño de la cuenta
     * @return el saldo; error si transaction-service no responde
     */
    Single<BigDecimal> balance(String accountId, String customerId);
}
