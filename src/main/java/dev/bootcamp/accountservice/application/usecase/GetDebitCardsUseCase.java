package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Caso de uso para consultar las tarjetas de débito de un cliente.
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe.
 */
public interface GetDebitCardsUseCase {

    /**
     * Lista las tarjetas del cliente.
     *
     * @return las tarjetas; ninguna si no tiene
     */
    Flowable<DebitCard> findByCustomerId(String customerId);

    /**
     * Consulta una tarjeta del cliente.
     *
     * @return la tarjeta; error {@link NotFoundException} si no existe o es de otro cliente
     */
    Single<DebitCard> findById(String customerId, String debitCardId);
}
