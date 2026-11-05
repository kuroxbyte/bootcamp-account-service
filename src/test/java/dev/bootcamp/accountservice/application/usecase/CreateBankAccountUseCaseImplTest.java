package dev.bootcamp.accountservice.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.application.port.CustomerClient;
import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateBankAccountUseCaseImplTest {

    private static final Customer PERSONAL = new Customer(1L, CustomerType.PERSONAL);

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CustomerClient customerClient;

    @InjectMocks
    private CreateBankAccountUseCaseImpl useCase;

    @Test
    void rejectsUnknownCustomer() {
        when(customerClient.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.openCheckingAccount(99L, BigDecimal.TEN))
                .isInstanceOf(BusinessRuleException.class);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void personalCustomerCanOpenSavingsAccount() {
        when(customerClient.findById(1L)).thenReturn(Optional.of(PERSONAL));
        when(bankAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.openSavingsAccount(1L, 5);

        verify(bankAccountRepository).save(any(SavingsAccount.class));
    }

    @Test
    void personalCustomerCanOpenFixedTermAccounts() {
        when(customerClient.findById(1L)).thenReturn(Optional.of(PERSONAL));
        when(bankAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.openFixedTermAccount(1L, 10);
        useCase.openFixedTermAccount(1L, 20);

        verify(bankAccountRepository, times(2)).save(any());
    }
}
