package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import io.reactivex.rxjava3.core.Completable;

/**
 * Caso de uso del empleado del banco para dar de baja una tarjeta de débito (baja lógica).
 *
 * <p>Sus cuentas quedan libres para otra tarjeta. Publica {@code DebitCardClosed}:
 * transaction-service deja de aceptar pagos con la tarjeta y yanki-service la desasocia del
 * monedero que la use. Es reactivo: no hace nada hasta que se suscribe.
 */
public interface CloseDebitCardUseCase {

    /**
     * Da de baja la tarjeta.
     *
     * @return termina sin error si se dio de baja; error {@link NotFoundException} si no existe o
     *     ya fue dada de baja
     */
    Completable execute(String debitCardId);
}
