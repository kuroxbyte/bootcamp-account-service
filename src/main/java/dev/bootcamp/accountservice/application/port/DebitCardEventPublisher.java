package dev.bootcamp.accountservice.application.port;

import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import io.reactivex.rxjava3.core.Completable;

/**
 * Publicación de eventos de tarjetas de débito para otros microservicios (transaction-service los
 * usa para procesar los pagos con tarjeta).
 */
public interface DebitCardEventPublisher {

    /**
     * Publica que se emitió una tarjeta, con su estado completo.
     *
     * @return termina cuando se publicó, o sin error si no se pudo (el adaptador lo registra)
     */
    Completable publishIssued(DebitCard debitCard);

    /**
     * Publica que cambiaron las cuentas de una tarjeta (cuenta asociada o nueva principal), con su
     * estado completo.
     *
     * @return termina cuando se publicó, o sin error si no se pudo (el adaptador lo registra)
     */
    Completable publishAccountsUpdated(DebitCard debitCard);

    /**
     * Publica que se dio de baja una tarjeta, con su último estado.
     *
     * @return termina cuando se publicó, o sin error si no se pudo (el adaptador lo registra)
     */
    Completable publishClosed(DebitCard debitCard);
}
