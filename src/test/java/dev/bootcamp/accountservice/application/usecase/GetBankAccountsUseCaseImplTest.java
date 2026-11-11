package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.account.FixedTermAccount;
import dev.bootcamp.accountservice.domain.model.account.OpeningAmount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.domain.model.account.TestOpenings;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBankAccountsUseCaseImplTest {

    private static final Customer PERSONAL = new Customer(
            "1",
            CustomerType.PERSONAL,
            CustomerProfile.STANDARD);

    @Mock private BankAccountRepository bankAccountRepository;

    @InjectMocks private GetBankAccountsUseCaseImpl useCase;

    @Test
    void listsAccountsOfCustomer() {
        SavingsAccount savings = new SavingsAccount(PERSONAL, TestOpenings.NONE, 5);
        FixedTermAccount fixedTerm = new FixedTermAccount(PERSONAL, TestOpenings.NONE, 15);
        when(bankAccountRepository.findByCustomerId("1"))
                .thenReturn(Flowable.just(savings, fixedTerm));

        useCase.findByCustomerId("1").test().assertValues(savings, fixedTerm);
    }

    @Test
    void returnsExistingAccount() {
        SavingsAccount savings = new SavingsAccount(PERSONAL, TestOpenings.NONE, 5);
        when(bankAccountRepository.findById("10")).thenReturn(Maybe.just(savings));

        useCase.findById("10").test().assertValue(savings);
    }

    @Test
    void failsWhenAccountDoesNotExist() {
        when(bankAccountRepository.findById("99")).thenReturn(Maybe.empty());

        useCase.findById("99").test().assertError(NotFoundException.class);
    }

    @Test
    void listsAllAccountsOfTheBank() {
        SavingsAccount savings = new SavingsAccount(PERSONAL, TestOpenings.NONE, 5);
        when(bankAccountRepository.findAll()).thenReturn(Flowable.just(savings));

        useCase.findAll().test().assertValues(savings);
    }
}
