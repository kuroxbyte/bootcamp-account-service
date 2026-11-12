package dev.bootcamp.accountservice.application.rule;

import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.application.port.DebitCardRepository;
import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.domain.model.account.TestOpenings;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebitCardAccountRuleTest {

    private static final Customer CUSTOMER =
            new Customer("c1", CustomerType.PERSONAL, CustomerProfile.STANDARD);

    @Mock private BankAccountRepository bankAccountRepository;

    @Mock private DebitCardRepository debitCardRepository;

    @InjectMocks private DebitCardAccountRule rule;

    private void givenAccountOf(Customer customer) {
        when(bankAccountRepository.findById("a1"))
                .thenReturn(Maybe.just(new SavingsAccount(customer, TestOpenings.NONE, 5)));
    }

    @Test
    void acceptsOwnAccountWithoutCard() {
        givenAccountOf(CUSTOMER);
        when(debitCardRepository.existsByAccountId("a1")).thenReturn(Single.just(false));

        rule.verify("c1", "a1").test().assertComplete();
    }

    @Test
    void rejectsAccountAlreadyAssociatedToCard() {
        givenAccountOf(CUSTOMER);
        when(debitCardRepository.existsByAccountId("a1")).thenReturn(Single.just(true));

        rule.verify("c1", "a1").test().assertError(error -> error instanceof BusinessRuleException
                && error.getMessage().equals(
                        "La cuenta a1 ya está asociada a una tarjeta de débito"));
    }

    @Test
    void rejectsAccountOfAnotherCustomerOrUnknown() {
        givenAccountOf(new Customer("c2", CustomerType.PERSONAL, CustomerProfile.STANDARD));
        when(bankAccountRepository.findById("a9")).thenReturn(Maybe.empty());

        rule.verify("c1", "a1").test().assertError(BusinessRuleException.class);
        rule.verify("c1", "a9").test().assertError(error -> error instanceof BusinessRuleException
                && error.getMessage().equals("La cuenta a9 no existe o no es del cliente"));
    }
}
