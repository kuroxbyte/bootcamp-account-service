package dev.bootcamp.accountservice.application.port;

import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de persistencia de las tarjetas de débito.
 */
public interface DebitCardRepository {

    /**
     * Guarda la tarjeta y la emite con su id asignado.
     */
    Single<DebitCard> save(DebitCard debitCard);

    /**
     * Busca la tarjeta por su id.
     *
     * @return la tarjeta, o vacío si no existe
     */
    Maybe<DebitCard> findById(String debitCardId);

    /**
     * Tarjetas del cliente (ninguna si no tiene).
     */
    Flowable<DebitCard> findByCustomerId(String customerId);

    /**
     * Indica si la cuenta ya está asociada a alguna tarjeta de débito.
     */
    Single<Boolean> existsByAccountId(String accountId);
}
