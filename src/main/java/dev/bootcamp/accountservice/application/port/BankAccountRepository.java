package dev.bootcamp.accountservice.application.port;

import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de persistencia de las cuentas bancarias.
 */
public interface BankAccountRepository {

    /**
     * Busca la cuenta por su id, aunque esté dada de baja.
     *
     * @return la cuenta, o vacío si no existe
     */
    Maybe<BankAccount> findById(String accountId);

    /**
     * Busca los productos del cliente.
     *
     * @return las cuentas activas del cliente (ninguna si no tiene)
     */
    Flowable<BankAccount> findByCustomerId(String customerId);

    /**
     * Todas las cuentas del banco, también las dadas de baja.
     */
    Flowable<BankAccount> findAll();

    /**
     * Guarda la cuenta y la emite con el mismo tipo concreto que recibió.
     */
    <T extends BankAccount> Single<T> save(T account);
}
