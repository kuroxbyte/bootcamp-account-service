package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import io.reactivex.rxjava3.core.Single;

/**
 * Caso de uso para cambiar las cuentas de una tarjeta de débito del cliente: asociar otra cuenta o
 * cambiar la cuenta principal.
 *
 * <p>Después de cada cambio publica {@code DebitCardAccountsUpdated} con el estado completo de la
 * tarjeta. Es reactivo: no hace nada hasta que se suscribe al {@link Single}. Termina con error
 * {@link NotFoundException} si la tarjeta no existe o es de otro cliente, y con {@link
 * BusinessRuleException} si se incumple una regla; en ambos casos no se guarda nada.
 */
public interface UpdateDebitCardAccountsUseCase {

    /**
     * Asocia otra cuenta a la tarjeta; debe ser del cliente y no estar asociada a ninguna tarjeta.
     *
     * @return la tarjeta actualizada
     */
    Single<DebitCard> associateAccount(String customerId, String debitCardId, String accountId);

    /**
     * Cambia la cuenta principal por otra ya asociada a la tarjeta.
     *
     * @return la tarjeta actualizada
     */
    Single<DebitCard> changePrimaryAccount(
            String customerId,
            String debitCardId,
            String accountId);
}
