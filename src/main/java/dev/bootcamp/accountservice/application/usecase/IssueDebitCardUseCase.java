package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * Caso de uso para emitir una tarjeta de débito asociada a cuentas bancarias del cliente.
 *
 * <p>Reglas:
 *
 * <ul>
 *   <li>El cliente no puede tener deuda vencida en ningún producto de crédito.
 *   <li>La tarjeta tiene una cuenta principal, de la que salen los pagos, y puede tener otras
 *       cuentas asociadas; ninguna se repite.
 *   <li>Cada cuenta debe existir, ser del cliente y no estar asociada a otra tarjeta (una cuenta,
 *       una tarjeta). Un cliente puede tener varias tarjetas.
 *   <li>La fecha de emisión es la fecha actual.
 *   <li>Al emitirla publica {@code DebitCardIssued} en {@code debit-card-events}.
 * </ul>
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe al {@link Single}. Termina con error {@link
 * BusinessRuleException} si se incumple una regla; en ese caso no se guarda nada.
 */
public interface IssueDebitCardUseCase {

    /**
     * Emite la tarjeta.
     *
     * @param customerId       id del cliente
     * @param primaryAccountId cuenta principal
     * @param otherAccountIds  otras cuentas asociadas (puede estar vacío)
     * @return la tarjeta guardada
     */
    Single<DebitCard> execute(
            String customerId,
            String primaryAccountId,
            List<String> otherAccountIds);
}
