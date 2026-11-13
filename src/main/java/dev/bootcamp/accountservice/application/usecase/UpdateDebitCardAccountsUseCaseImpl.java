package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.application.port.DebitCardEventPublisher;
import dev.bootcamp.accountservice.application.port.DebitCardRepository;
import dev.bootcamp.accountservice.application.rule.DebitCardAccountRule;
import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link UpdateDebitCardAccountsUseCase}.
 */
@RequiredArgsConstructor
public class UpdateDebitCardAccountsUseCaseImpl implements UpdateDebitCardAccountsUseCase {
    private final DebitCardRepository debitCardRepository;
    private final DebitCardAccountRule debitCardAccountRule;
    private final DebitCardEventPublisher debitCardEventPublisher;

    @Override
    public Single<DebitCard> associateAccount(
            String customerId,
            String debitCardId,
            String accountId) {
        return GetDebitCardsUseCaseImpl
                .findOwnedCard(debitCardRepository, customerId, debitCardId)
                .flatMap(card -> debitCardAccountRule.verify(customerId, accountId)
                        .andThen(Single.fromCallable(() -> {
                            card.associateAccount(accountId);
                            return card;
                        })))
                .flatMap(this::saveAndPublish);
    }

    @Override
    public Single<DebitCard> changePrimaryAccount(
            String customerId,
            String debitCardId,
            String accountId) {
        return GetDebitCardsUseCaseImpl
                .findOwnedCard(debitCardRepository, customerId, debitCardId)
                .map(card -> {
                    card.changePrimaryAccount(accountId);
                    return card;
                })
                .flatMap(this::saveAndPublish);
    }

    private Single<DebitCard> saveAndPublish(DebitCard card) {
        return debitCardRepository.save(card)
                .flatMap(saved -> debitCardEventPublisher.publishAccountsUpdated(saved)
                        .andThen(Single.just(saved)));
    }
}
