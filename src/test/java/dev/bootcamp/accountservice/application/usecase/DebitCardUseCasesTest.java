package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.application.port.CustomerDebtStatusRepository;
import dev.bootcamp.accountservice.application.port.DebitCardEventPublisher;
import dev.bootcamp.accountservice.application.port.DebitCardRepository;
import dev.bootcamp.accountservice.application.rule.DebitCardAccountRule;
import dev.bootcamp.accountservice.application.rule.OverdueDebtRule;
import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Casos de uso de tarjetas de débito: emitir, consultar y cambiar sus cuentas.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DebitCardUseCasesTest {

    @Mock private DebitCardRepository debitCardRepository;

    @Mock private DebitCardAccountRule debitCardAccountRule;

    @Mock private CustomerDebtStatusRepository customerDebtStatusRepository;

    @Mock private DebitCardEventPublisher debitCardEventPublisher;

    private IssueDebitCardUseCaseImpl issue;

    private GetDebitCardsUseCaseImpl get;

    private UpdateDebitCardAccountsUseCaseImpl update;

    @BeforeEach
    void createUseCases() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC);
        issue = new IssueDebitCardUseCaseImpl(
                debitCardRepository,
                debitCardAccountRule,
                new OverdueDebtRule(customerDebtStatusRepository),
                debitCardEventPublisher,
                clock);
        get = new GetDebitCardsUseCaseImpl(debitCardRepository);
        update = new UpdateDebitCardAccountsUseCaseImpl(
                debitCardRepository,
                debitCardAccountRule,
                debitCardEventPublisher);
        when(customerDebtStatusRepository.hasOverdueDebt(any())).thenReturn(Single.just(false));
        when(debitCardAccountRule.verify(anyString(), anyString()))
                .thenReturn(Completable.complete());
        when(debitCardRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));
        when(debitCardEventPublisher.publishIssued(any())).thenReturn(Completable.complete());
        when(debitCardEventPublisher.publishAccountsUpdated(any()))
                .thenReturn(Completable.complete());
    }

    private void givenCard(DebitCard card) {
        card.setId("d1");
        when(debitCardRepository.findById("d1")).thenReturn(Maybe.just(card));
    }

    @Test
    void issuesCardVerifyingEveryAccountAndPublishes() {
        DebitCard card = issue.execute("c1", "a1", List.of("a2")).blockingGet();

        assertThat(card.getAccountIds()).containsExactly("a1", "a2");
        assertThat(card.getIssuedAt()).isEqualTo(LocalDate.of(2026, 11, 15));
        verify(debitCardAccountRule).verify("c1", "a1");
        verify(debitCardAccountRule).verify("c1", "a2");
        verify(debitCardEventPublisher).publishIssued(card);
    }

    @Test
    void doesNotIssueCardToCustomerWithOverdueDebt() {
        when(customerDebtStatusRepository.hasOverdueDebt("c1")).thenReturn(Single.just(true));

        issue.execute("c1", "a1", List.of()).test().assertError(BusinessRuleException.class);

        verify(debitCardRepository, never()).save(any());
    }

    @Test
    void doesNotIssueCardWhenAccountCannotBeAssociated() {
        when(debitCardAccountRule.verify("c1", "a2")).thenReturn(Completable.error(
                new BusinessRuleException(
                        "La cuenta a2 ya está asociada a una tarjeta de débito")));

        issue.execute("c1", "a1", List.of("a2")).test().assertError(BusinessRuleException.class);

        verify(debitCardRepository, never()).save(any());
        verify(debitCardEventPublisher, never()).publishIssued(any());
    }

    @Test
    void findsOnlyCardsOfTheCustomer() {
        givenCard(new DebitCard("c1", "a1", List.of()));
        when(debitCardRepository.findByCustomerId("c1"))
                .thenReturn(Flowable.just(new DebitCard("c1", "a1", List.of())));

        get.findByCustomerId("c1").test().assertValueCount(1);
        get.findById("c1", "d1").test().assertValue(card -> card.getId().equals("d1"));
        get.findById("c2", "d1").test().assertError(NotFoundException.class);
    }

    @Test
    void associatesAccountAndPublishesTheNewState() {
        givenCard(new DebitCard("c1", "a1", List.of()));

        DebitCard card = update.associateAccount("c1", "d1", "a2").blockingGet();

        assertThat(card.getAccountIds()).containsExactly("a1", "a2");
        verify(debitCardAccountRule).verify("c1", "a2");
        verify(debitCardEventPublisher).publishAccountsUpdated(card);
    }

    @Test
    void changesPrimaryAccountAndPublishes() {
        givenCard(new DebitCard("c1", "a1", List.of("a2")));

        DebitCard card = update.changePrimaryAccount("c1", "d1", "a2").blockingGet();

        assertThat(card.getPrimaryAccountId()).isEqualTo("a2");
        verify(debitCardEventPublisher).publishAccountsUpdated(card);
    }

    @Test
    void rejectsChangesOnCardsOfOtherCustomersOrToUnassociatedAccounts() {
        givenCard(new DebitCard("c1", "a1", List.of()));

        update.associateAccount("c2", "d1", "a2").test().assertError(NotFoundException.class);
        update.changePrimaryAccount("c1", "d1", "a9").test()
                .assertError(BusinessRuleException.class);
        verify(debitCardRepository, never()).save(any());
    }
}
