package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.application.port.AccountBalanceClient;
import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.application.port.DebitCardEventPublisher;
import dev.bootcamp.accountservice.application.port.DebitCardRepository;
import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.account.AccountChanges;
import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.domain.model.account.TestOpenings;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountAdministrationUseCasesTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-12-01T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2026, 12, 1);
    private static final Customer CUSTOMER =
            new Customer("c1", CustomerType.PERSONAL, CustomerProfile.STANDARD);

    @Mock private BankAccountRepository bankAccountRepository;

    @Mock private DebitCardRepository debitCardRepository;

    @Mock private AccountBalanceClient accountBalanceClient;

    @Mock private DebitCardEventPublisher debitCardEventPublisher;

    private UpdateBankAccountUseCaseImpl accounts() {
        return new UpdateBankAccountUseCaseImpl(
                bankAccountRepository, debitCardRepository, accountBalanceClient, CLOCK);
    }

    private static SavingsAccount savings() {
        SavingsAccount account = new SavingsAccount(CUSTOMER, TestOpenings.NONE, 5);
        account.setId("a1");
        return account;
    }

    private void saves() {
        when(bankAccountRepository.save(any()))
                .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));
    }

    @Test
    void updatesActiveAccount() {
        when(bankAccountRepository.findById("a1")).thenReturn(Maybe.just(savings()));
        saves();

        accounts().update("a1", new AccountChanges(null, null, null, 9, null, null)).test()
                .assertValue(updated -> ((SavingsAccount) updated).getMaxMonthlyMovements() == 9);
    }

    @Test
    void closedOrUnknownAccountIsNotFound() {
        SavingsAccount closed = savings();
        closed.close(LocalDate.of(2026, 1, 1));
        when(bankAccountRepository.findById("a1")).thenReturn(Maybe.just(closed));
        when(bankAccountRepository.findById("a9")).thenReturn(Maybe.empty());

        accounts().update("a1", new AccountChanges(null, null, null, 9, null, null)).test()
                .assertError(NotFoundException.class);
        accounts().close("a9").test().assertError(NotFoundException.class);
        new GetBankAccountsUseCaseImpl(bankAccountRepository).findById("a1").test()
                .assertError(NotFoundException.class);
    }

    @Test
    void closesAccountWithZeroBalanceAndNoCard() {
        SavingsAccount account = savings();
        when(bankAccountRepository.findById("a1")).thenReturn(Maybe.just(account));
        when(debitCardRepository.existsByAccountId("a1")).thenReturn(Single.just(false));
        when(accountBalanceClient.balance("a1", "c1"))
                .thenReturn(Single.just(new BigDecimal("0.00")));
        saves();

        accounts().close("a1").test().assertComplete();

        assertThat(account.getClosedAt()).isEqualTo(TODAY);
    }

    @Test
    void doesNotCloseAccountWithCardOrBalance() {
        when(bankAccountRepository.findById("a1")).thenReturn(Maybe.just(savings()));
        when(debitCardRepository.existsByAccountId("a1"))
                .thenReturn(Single.just(true), Single.just(false));
        when(accountBalanceClient.balance("a1", "c1")).thenReturn(Single.just(BigDecimal.TEN));

        accounts().close("a1").test().assertError(BusinessRuleException.class);
        accounts().close("a1").test().assertError(error -> error.getMessage().contains("saldo"));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    void closesActiveDebitCardAndPublishesIt() {
        DebitCard card = new DebitCard("c1", "a1", List.of());
        card.setId("d1");
        when(debitCardRepository.findById("d1")).thenReturn(Maybe.just(card));
        when(debitCardRepository.save(card)).thenReturn(Single.just(card));
        when(debitCardEventPublisher.publishClosed(card)).thenReturn(Completable.complete());

        new CloseDebitCardUseCaseImpl(debitCardRepository, debitCardEventPublisher, CLOCK)
                .execute("d1").test().assertComplete();

        assertThat(card.getClosedAt()).isEqualTo(TODAY);
    }

    @Test
    void closedOrUnknownDebitCardIsNotFound() {
        DebitCard closed = new DebitCard("c1", "a1", List.of());
        closed.close(TODAY);
        when(debitCardRepository.findById("d1")).thenReturn(Maybe.just(closed));
        when(debitCardRepository.findById("d9")).thenReturn(Maybe.empty());
        CloseDebitCardUseCaseImpl useCase =
                new CloseDebitCardUseCaseImpl(debitCardRepository, debitCardEventPublisher, CLOCK);

        useCase.execute("d1").test().assertError(NotFoundException.class);
        useCase.execute("d9").test().assertError(NotFoundException.class);
        new GetDebitCardsUseCaseImpl(debitCardRepository).findById("c1", "d1").test()
                .assertError(NotFoundException.class);
    }
}
