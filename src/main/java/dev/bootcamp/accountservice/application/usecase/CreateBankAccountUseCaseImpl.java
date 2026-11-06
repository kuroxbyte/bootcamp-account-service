package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.application.port.CustomerClient;
import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.FixedTermAccount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import java.math.BigDecimal;
import java.util.List;

public class CreateBankAccountUseCaseImpl implements CreateBankAccountUseCase {
    private final BankAccountRepository bankAccountRepository;
    private final CustomerClient customerClient;

    public CreateBankAccountUseCaseImpl(BankAccountRepository bankAccountRepository, CustomerClient customerClient) {
        this.bankAccountRepository = bankAccountRepository;
        this.customerClient = customerClient;
    }

    @Override
    public SavingsAccount openSavingsAccount(String customerId, int maxMonthlyMovements) {
        SavingsAccount account = new SavingsAccount(findCustomer(customerId), maxMonthlyMovements);
        validatePersonalLimit(account);
        return bankAccountRepository.save(account);
    }

    @Override
    public CheckingAccount openCheckingAccount(String customerId, BigDecimal maintenanceFee,
                                               List<String> holderIds, List<String> authorizedSignerIds) {
        CheckingAccount account = new CheckingAccount(
                findCustomer(customerId), maintenanceFee, holderIds, authorizedSignerIds);
        validatePersonalLimit(account);
        return bankAccountRepository.save(account);
    }

    @Override
    public FixedTermAccount openFixedTermAccount(String customerId, int movementDayOfMonth) {
        FixedTermAccount account = new FixedTermAccount(findCustomer(customerId), movementDayOfMonth);
        return bankAccountRepository.save(account);
    }

    private Customer findCustomer(String customerId) {
        return customerClient.findById(customerId)
                .orElseThrow(() -> new BusinessRuleException("El cliente no existe"));
    }

    // Personal: máximo una cuenta de ahorro y una cuenta corriente.
    private void validatePersonalLimit(BankAccount account) {
        if (account.getCustomer().type() != CustomerType.PERSONAL) {
            return;
        }
        boolean alreadyHasSameType = bankAccountRepository.findByCustomerId(account.getCustomer().id())
                .stream()
                .anyMatch(existing -> existing.getClass() == account.getClass());
        if (alreadyHasSameType) {
            throw new BusinessRuleException("Un cliente personal solo puede tener una cuenta de este tipo");
        }
    }
}
