package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.application.port.DebitCardRepository;
import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Implementación de {@link GetDebitCardsUseCase}.
 */
public class GetDebitCardsUseCaseImpl implements GetDebitCardsUseCase {
    private final DebitCardRepository debitCardRepository;

    /**
     * Crea el caso de uso con el adaptador de su puerto.
     */
    public GetDebitCardsUseCaseImpl(DebitCardRepository debitCardRepository) {
        this.debitCardRepository = debitCardRepository;
    }

    @Override
    public Flowable<DebitCard> findByCustomerId(String customerId) {
        return debitCardRepository.findByCustomerId(customerId);
    }

    @Override
    public Single<DebitCard> findById(String customerId, String debitCardId) {
        return findOwnedCard(debitCardRepository, customerId, debitCardId);
    }

    /**
     * Busca la tarjeta y verifica que sea del cliente.
     *
     * @return la tarjeta; error {@link NotFoundException} si no existe o es de otro cliente
     */
    static Single<DebitCard> findOwnedCard(
            DebitCardRepository debitCardRepository,
            String customerId,
            String debitCardId) {
        return debitCardRepository.findById(debitCardId)
                .filter(card -> card.belongsTo(customerId))
                .switchIfEmpty(Single.error(() -> new NotFoundException(
                        "La tarjeta de débito no existe o no pertenece al cliente")));
    }
}
