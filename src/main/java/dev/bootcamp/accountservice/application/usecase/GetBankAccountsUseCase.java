package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Caso de uso para consultar cuentas bancarias.
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe.
 */
public interface GetBankAccountsUseCase {

    /**
     * Lista las cuentas de un cliente.
     *
     * @param customerId id del cliente en customer-service
     * @return las cuentas del cliente; ninguna si no tiene
     */
    Flowable<BankAccount> findByCustomerId(String customerId);

    /**
     * Consulta una cuenta por su id.
     *
     * @param accountId id de la cuenta
     * @return la cuenta; error {@link NotFoundException} si no existe
     */
    Single<BankAccount> findById(String accountId);
}
