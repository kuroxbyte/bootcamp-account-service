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
     * Busca el recurso por su id.
     *
     * @return la cuenta, o vacío si no existe
     */
    Maybe<BankAccount> findById(String accountId);

    /**
     * Busca los productos del cliente.
     *
     * @return las cuentas del cliente (ninguna si no tiene)
     */
    Flowable<BankAccount> findByCustomerId(String customerId);

    /**
     * Guarda la cuenta y la emite con el mismo tipo concreto que recibió.
     */
    <T extends BankAccount> Single<T> save(T account);
}
