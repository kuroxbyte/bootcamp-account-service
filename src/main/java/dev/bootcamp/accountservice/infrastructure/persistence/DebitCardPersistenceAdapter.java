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
        return Single.fromPublisher(mongoRepository.save(new DebitCardDocument(
                        debitCard.getId(),
                        debitCard.getCustomerId(),
                        debitCard.getPrimaryAccountId(),
                        debitCard.getAccountIds(),
                        debitCard.getIssuedAt())))
                .map(DebitCardPersistenceAdapter::toDomain);
    }

    @Override
    public Maybe<DebitCard> findById(String debitCardId) {
        return Maybe.fromPublisher(mongoRepository.findById(debitCardId))
                .map(DebitCardPersistenceAdapter::toDomain);
    }

    @Override
    public Flowable<DebitCard> findByCustomerId(String customerId) {
        return Flowable.fromPublisher(mongoRepository.findByCustomerId(customerId))
                .map(DebitCardPersistenceAdapter::toDomain);
    }

    @Override
    public Single<Boolean> existsByAccountId(String accountId) {
        return Single.fromPublisher(mongoRepository.existsByAccountIds(accountId));
    }

    private static DebitCard toDomain(DebitCardDocument document) {
        return DebitCard.restore(
                document.getId(),
                document.getCustomerId(),
                document.getPrimaryAccountId(),
                document.getAccountIds(),
                document.getIssuedAt());
    }
}
