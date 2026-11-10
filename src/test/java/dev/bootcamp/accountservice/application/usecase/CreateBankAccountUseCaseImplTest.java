package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.application.port.CustomerClient;
import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.MinimumOpeningAmounts;
import dev.bootcamp.accountservice.domain.model.account.OpeningAmount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.domain.model.account.TestOpenings;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateBankAccountUseCaseImplTest {

    private static final Customer PERSONAL = new Customer("1", CustomerType.PERSONAL);
    private static final Customer BUSINESS = new Customer("2", CustomerType.BUSINESS);

    @Mock private BankAccountRepository bankAccountRepository;

    @Mock private CustomerClient customerClient;

    private CreateBankAccountUseCaseImpl useCase;

    /**
     * Sin mínimo de apertura, salvo en las pruebas que lo cambian.
     */
    @BeforeEach
    void createUseCase() {
        useCase = useCaseWithMinimums(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private CreateBankAccountUseCaseImpl useCaseWithMinimums(
            BigDecimal savings,
            BigDecimal checking,
            BigDecimal fixedTerm) {
        return new CreateBankAccountUseCaseImpl(
                bankAccountRepository,
                customerClient,
                new MinimumOpeningAmounts(savings, checking, fixedTerm));
    }

    @Test
    void rejectsUnknownCustomer() {
        when(customerClient.findById("99")).thenReturn(Maybe.empty());

        useCase.openCheckingAccount("99", BigDecimal.TEN, List.of(), List.of(), BigDecimal.ZERO)
                .test()
                .assertError(NotFoundException.class);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void personalCustomerCanOpenFirstSavingsAccount() {
        when(customerClient.findById("1")).thenReturn(Maybe.just(PERSONAL));
        when(bankAccountRepository.findByCustomerId("1")).thenReturn(Flowable.empty());
        when(bankAccountRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));

        useCase.openSavingsAccount("1", 5, BigDecimal.ZERO).test().assertComplete();

        verify(bankAccountRepository).save(any(SavingsAccount.class));
    }

    @Test
    void personalCustomerCannotOpenSecondSavingsAccount() {
        when(customerClient.findById("1")).thenReturn(Maybe.just(PERSONAL));
        when(bankAccountRepository.findByCustomerId("1"))
                .thenReturn(Flowable.just(new SavingsAccount(PERSONAL, TestOpenings.NONE, 5)));

        useCase.openSavingsAccount(
                "1",
                5,
                BigDecimal.ZERO).test().assertError(BusinessRuleException.class);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void personalCustomerCannotOpenSecondCheckingAccount() {
        when(customerClient.findById("1")).thenReturn(Maybe.just(PERSONAL));
        when(bankAccountRepository.findByCustomerId("1"))
                .thenReturn(
                        Flowable.just(
                                new CheckingAccount(
                                        PERSONAL,
                                        TestOpenings.NONE,
                                        BigDecimal.TEN,
                                        List.of(),
                                        List.of())));

        useCase.openCheckingAccount("1", BigDecimal.TEN, List.of(), List.of(), BigDecimal.ZERO)
                .test()
                .assertError(BusinessRuleException.class);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void personalCustomerCanHaveOneSavingsAndOneCheckingAccount() {
        when(customerClient.findById("1")).thenReturn(Maybe.just(PERSONAL));
        when(bankAccountRepository.findByCustomerId("1"))
                .thenReturn(Flowable.just(new SavingsAccount(PERSONAL, TestOpenings.NONE, 5)));
        when(bankAccountRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));

        useCase.openCheckingAccount("1", BigDecimal.TEN, List.of(), List.of(), BigDecimal.ZERO)
                .test()
                .assertComplete();

        verify(bankAccountRepository).save(any(CheckingAccount.class));
    }

    @Test
    void personalCustomerCanOpenMultipleFixedTermAccounts() {
        when(customerClient.findById("1")).thenReturn(Maybe.just(PERSONAL));
        when(bankAccountRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));

        useCase.openFixedTermAccount("1", 10, BigDecimal.ZERO).test().assertComplete();
        useCase.openFixedTermAccount("1", 20, BigDecimal.ZERO).test().assertComplete();

        verify(bankAccountRepository, never()).findByCustomerId(any());
        verify(bankAccountRepository, times(2)).save(any());
    }

    @Test
    void businessCustomerCanOpenMultipleCheckingAccounts() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));
        when(bankAccountRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));

        useCase.openCheckingAccount("2", BigDecimal.TEN, List.of("10"), List.of(), BigDecimal.ZERO)
                .test()
                .assertComplete();
        useCase.openCheckingAccount("2", BigDecimal.TEN, List.of("10"), List.of(), BigDecimal.ZERO)
                .test()
                .assertComplete();

        verify(bankAccountRepository, never()).findByCustomerId(any());
        verify(bankAccountRepository, times(2)).save(any(CheckingAccount.class));
    }

    @Test
    void businessCustomerCannotOpenSavingsAccount() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));

        useCase.openSavingsAccount(
                "2",
                5,
                BigDecimal.ZERO).test().assertError(BusinessRuleException.class);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void businessCustomerCannotOpenFixedTermAccount() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));

        useCase.openFixedTermAccount(
                "2",
                15,
                BigDecimal.ZERO).test().assertError(BusinessRuleException.class);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void businessCustomerCannotOpenCheckingAccountWithoutHolders() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));

        useCase.openCheckingAccount("2", BigDecimal.TEN, List.of(), List.of("20"), BigDecimal.ZERO)
                .test()
                .assertError(BusinessRuleException.class);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void businessCustomerOpensCheckingAccountWithHoldersAndSigners() {
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));
        when(bankAccountRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));

        CheckingAccount account =
                useCase.openCheckingAccount(
                        "2",
                        BigDecimal.TEN,
                        List.of("10", "11"),
                        List.of("20"),
                        BigDecimal.ZERO)
                        .blockingGet();

        assertThat(account.getHolderIds()).containsExactly("10", "11");
        assertThat(account.getAuthorizedSignerIds()).containsExactly("20");
    }

    @Test
    void opensAccountWithZeroOpeningAmountWhenMinimumIsZero() {
        when(customerClient.findById("1")).thenReturn(Maybe.just(PERSONAL));
        when(bankAccountRepository.findByCustomerId("1")).thenReturn(Flowable.empty());
        when(bankAccountRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));

        useCase.openSavingsAccount("1", 5, BigDecimal.ZERO).test()
                .assertValue(account -> account.getOpening()
                        .equals(new OpeningAmount(BigDecimal.ZERO, BigDecimal.ZERO)));
    }

    @Test
    void keepsOpeningAmountAndMinimumOfTheAccountType() {
        useCase = useCaseWithMinimums(new BigDecimal("50"), new BigDecimal("500"), BigDecimal.ZERO);
        when(customerClient.findById("2")).thenReturn(Maybe.just(BUSINESS));
        when(bankAccountRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));

        useCase.openCheckingAccount(
                "2",
                BigDecimal.TEN,
                List.of("10"),
                List.of(),
                new BigDecimal("800"))
                .test()
                .assertValue(account -> account.getOpening().equals(
                        new OpeningAmount(new BigDecimal("500"), new BigDecimal("800"))));
    }

    @Test
    void rejectsOpeningAmountBelowMinimumOfTheAccountType() {
        useCase = useCaseWithMinimums(new BigDecimal("50"), BigDecimal.ZERO, BigDecimal.ZERO);
        when(customerClient.findById("1")).thenReturn(Maybe.just(PERSONAL));

        useCase.openSavingsAccount("1", 5, new BigDecimal("49.99")).test()
                .assertError(BusinessRuleException.class);
        verify(bankAccountRepository, never()).save(any());
    }
}
