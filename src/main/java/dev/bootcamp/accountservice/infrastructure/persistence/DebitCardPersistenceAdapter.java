package dev.bootcamp.accountservice.infrastructure.persistence;

import dev.bootcamp.accountservice.application.port.DebitCardRepository;
import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementa el puerto {@link DebitCardRepository} con MongoDB reactivo.
 */
@Component
@RequiredArgsConstructor
public class DebitCardPersistenceAdapter implements DebitCardRepository {

    private final DebitCardMongoRepository mongoRepository;

    @Override
    public Single<DebitCard> save(DebitCard debitCard) {
        boolean closed = debitCard.isClosed();
        return Single.fromPublisher(mongoRepository.save(new DebitCardDocument(
                        debitCard.getId(),
                        debitCard.getCustomerId(),
                        debitCard.getPrimaryAccountId(),
                        closed ? null : debitCard.getAccountIds(),
                        debitCard.getIssuedAt(),
                        debitCard.getClosedAt(),
                        closed ? debitCard.getAccountIds() : null)))
                .map(DebitCardPersistenceAdapter::toDomain);
    }

    @Override
    public Maybe<DebitCard> findById(String debitCardId) {
        return Maybe.fromPublisher(mongoRepository.findById(debitCardId))
                .map(DebitCardPersistenceAdapter::toDomain);
    }

    @Override
    public Flowable<DebitCard> findByCustomerId(String customerId) {
        return Flowable.fromPublisher(mongoRepository.findByCustomerIdAndClosedAtIsNull(customerId))
                .map(DebitCardPersistenceAdapter::toDomain);
    }

    @Override
    public Single<Boolean> existsByAccountId(String accountId) {
        return Single.fromPublisher(mongoRepository.existsByAccountIds(accountId));
    }

    private static DebitCard toDomain(DebitCardDocument document) {
        DebitCard card = DebitCard.restore(
                document.getId(),
                document.getCustomerId(),
                document.getPrimaryAccountId(),
                document.getClosedAt() == null
                        ? document.getAccountIds()
                        : document.getClosedAccountIds(),
                document.getIssuedAt());
        card.setClosedAt(document.getClosedAt());
        return card;
    }
}
