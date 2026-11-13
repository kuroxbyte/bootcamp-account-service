package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.application.port.DebitCardEventPublisher;
import dev.bootcamp.accountservice.application.port.DebitCardRepository;
import dev.bootcamp.accountservice.application.rule.DebitCardAccountRule;
import dev.bootcamp.accountservice.application.rule.OverdueDebtRule;
import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementación de {@link IssueDebitCardUseCase}.
 */
@RequiredArgsConstructor
public class IssueDebitCardUseCaseImpl implements IssueDebitCardUseCase {
    private final DebitCardRepository debitCardRepository;
    private final DebitCardAccountRule debitCardAccountRule;
    private final OverdueDebtRule overdueDebtRule;
    private final DebitCardEventPublisher debitCardEventPublisher;
    private final Clock clock;

    @Override
    public Single<DebitCard> execute(
            String customerId,
            String primaryAccountId,
            List<String> otherAccountIds) {
        return Single.fromCallable(
                        () -> new DebitCard(customerId, primaryAccountId, otherAccountIds))
                .flatMap(card -> overdueDebtRule.verify(customerId)
                        .andThen(Flowable.fromIterable(card.getAccountIds())
                                .concatMapCompletable(accountId ->
                                        debitCardAccountRule.verify(customerId, accountId)))
                        .andThen(Single.just(card)))
                .flatMap(card -> {
                    card.setIssuedAt(LocalDate.now(clock));
                    return debitCardRepository.save(card);
                })
                .flatMap(saved -> debitCardEventPublisher.publishIssued(saved)
                        .andThen(Single.just(saved)));
    }
}
