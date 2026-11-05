package dev.bootcamp.accountservice.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateBankAccountUseCaseImplTest {

    private static final Customer PERSONAL = new Customer(1L, CustomerType.PERSONAL);
    private static final Customer BUSINESS = new Customer(2L, CustomerType.BUSINESS);

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CustomerClient customerClient;

    @InjectMocks
    private CreateBankAccountUseCaseImpl useCase;

    @Test
    void rejectsUnknownCustomer() {
        when(customerClient.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.openCheckingAccount(99L, BigDecimal.TEN, List.of(), List.of()))
                .isInstanceOf(BusinessRuleException.class);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void personalCustomerCanOpenFirstSavingsAccount() {
        when(customerClient.findById(1L)).thenReturn(Optional.of(PERSONAL));
        when(bankAccountRepository.findByCustomerId(1L)).thenReturn(List.of());
        when(bankAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.openSavingsAccount(1L, 5);

        verify(bankAccountRepository).save(any(SavingsAccount.class));
    }

    @Test
    void personalCustomerCannotOpenSecondSavingsAccount() {
        when(customerClient.findById(1L)).thenReturn(Optional.of(PERSONAL));
        when(bankAccountRepository.findByCustomerId(1L)).thenReturn(List.of(new SavingsAccount(PERSONAL, 5)));

        assertThatThrownBy(() -> useCase.openSavingsAccount(1L, 5))
                .isInstanceOf(BusinessRuleException.class);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void personalCustomerCannotOpenSecondCheckingAccount() {
        when(customerClient.findById(1L)).thenReturn(Optional.of(PERSONAL));
        when(bankAccountRepository.findByCustomerId(1L))
                .thenReturn(List.of(new CheckingAccount(PERSONAL, BigDecimal.TEN, List.of(), List.of())));

        assertThatThrownBy(() -> useCase.openCheckingAccount(1L, BigDecimal.TEN, List.of(), List.of()))
                .isInstanceOf(BusinessRuleException.class);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void personalCustomerCanHaveOneSavingsAndOneCheckingAccount() {
        when(customerClient.findById(1L)).thenReturn(Optional.of(PERSONAL));
        when(bankAccountRepository.findByCustomerId(1L)).thenReturn(List.of(new SavingsAccount(PERSONAL, 5)));
        when(bankAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.openCheckingAccount(1L, BigDecimal.TEN, List.of(), List.of());

        verify(bankAccountRepository).save(any(CheckingAccount.class));
    }

    @Test
    void personalCustomerCanOpenMultipleFixedTermAccounts() {
        when(customerClient.findById(1L)).thenReturn(Optional.of(PERSONAL));
        when(bankAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.openFixedTermAccount(1L, 10);
        useCase.openFixedTermAccount(1L, 20);

        verify(bankAccountRepository, never()).findByCustomerId(any());
        verify(bankAccountRepository, times(2)).save(any());
    }

    @Test
    void businessCustomerCanOpenMultipleCheckingAccounts() {
        when(customerClient.findById(2L)).thenReturn(Optional.of(BUSINESS));
        when(bankAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.openCheckingAccount(2L, BigDecimal.TEN, List.of(10L), List.of());
        useCase.openCheckingAccount(2L, BigDecimal.TEN, List.of(10L), List.of());

        verify(bankAccountRepository, never()).findByCustomerId(any());
        verify(bankAccountRepository, times(2)).save(any(CheckingAccount.class));
    }

    @Test
    void businessCustomerCannotOpenSavingsAccount() {
        when(customerClient.findById(2L)).thenReturn(Optional.of(BUSINESS));

        assertThatThrownBy(() -> useCase.openSavingsAccount(2L, 5))
                .isInstanceOf(BusinessRuleException.class);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void businessCustomerCannotOpenFixedTermAccount() {
        when(customerClient.findById(2L)).thenReturn(Optional.of(BUSINESS));

        assertThatThrownBy(() -> useCase.openFixedTermAccount(2L, 15))
                .isInstanceOf(BusinessRuleException.class);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void businessCustomerCannotOpenCheckingAccountWithoutHolders() {
        when(customerClient.findById(2L)).thenReturn(Optional.of(BUSINESS));

        assertThatThrownBy(() -> useCase.openCheckingAccount(2L, BigDecimal.TEN, List.of(), List.of(20L)))
                .isInstanceOf(BusinessRuleException.class);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void businessCustomerOpensCheckingAccountWithHoldersAndSigners() {
        when(customerClient.findById(2L)).thenReturn(Optional.of(BUSINESS));
        when(bankAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CheckingAccount account = useCase.openCheckingAccount(2L, BigDecimal.TEN, List.of(10L, 11L), List.of(20L));

        assertThat(account.getHolderIds()).containsExactly(10L, 11L);
        assertThat(account.getAuthorizedSignerIds()).containsExactly(20L);
    }
}
