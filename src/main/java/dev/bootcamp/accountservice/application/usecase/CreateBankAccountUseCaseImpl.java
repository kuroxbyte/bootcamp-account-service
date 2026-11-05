package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.application.port.CustomerClient;
import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.FixedTermAccount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import java.math.BigDecimal;

public class CreateBankAccountUseCaseImpl implements CreateBankAccountUseCase {
    private final BankAccountRepository bankAccountRepository;
    private final CustomerClient customerClient;

    public CreateBankAccountUseCaseImpl(BankAccountRepository bankAccountRepository, CustomerClient customerClient) {
        this.bankAccountRepository = bankAccountRepository;
        this.customerClient = customerClient;
    }

    @Override
    public SavingsAccount openSavingsAccount(Long customerId, int maxMonthlyMovements) {
        SavingsAccount account = new SavingsAccount(findCustomer(customerId), maxMonthlyMovements);
        return (SavingsAccount) bankAccountRepository.save(account);
    }

    @Override
    public CheckingAccount openCheckingAccount(Long customerId, BigDecimal maintenanceFee) {
        CheckingAccount account = new CheckingAccount(findCustomer(customerId), maintenanceFee);
        return (CheckingAccount) bankAccountRepository.save(account);
    }

    @Override
    public FixedTermAccount openFixedTermAccount(Long customerId, int movementDayOfMonth) {
        FixedTermAccount account = new FixedTermAccount(findCustomer(customerId), movementDayOfMonth);
        return (FixedTermAccount) bankAccountRepository.save(account);
    }

    private Customer findCustomer(Long customerId) {
        return customerClient.findById(customerId)
                .orElseThrow(() -> new BusinessRuleException("El cliente no existe"));
    }
}
