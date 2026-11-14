package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.account.AccountChanges;
import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Casos de uso del empleado del banco sobre una cuenta: modificar sus condiciones y darla de baja.
 *
 * <p>Son reactivos: no hacen nada hasta que se suscribe.
 */
public interface UpdateBankAccountUseCase {

    /**
     * Reemplaza las condiciones modificables de la cuenta, según su tipo (ver
     * {@link AccountChanges}). El cliente, el tipo y la apertura no cambian.
     *
     * @return la cuenta modificada; error {@link NotFoundException} si no existe o fue dada de
     *     baja, o {@link BusinessRuleException} si los nuevos valores incumplen una regla
     */
    Single<BankAccount> update(String accountId, AccountChanges changes);

    /**
     * Da de baja la cuenta (baja lógica: conserva su historial de movimientos). Solo si su saldo
     * es 0 y no está asociada a una tarjeta de débito activa.
     *
     * @return termina sin error si se dio de baja; error {@link NotFoundException} si no existe o
     *     ya fue dada de baja, o {@link BusinessRuleException} si tiene saldo o tarjeta
     */
    Completable close(String accountId);
}
