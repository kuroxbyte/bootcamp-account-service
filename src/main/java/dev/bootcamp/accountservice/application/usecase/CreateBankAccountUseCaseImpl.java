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
import io.reactivex.rxjava3.core.Single;
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
    public Single<SavingsAccount> openSavingsAccount(String customerId, int maxMonthlyMovements) {
        return findCustomer(customerId)
                .map(customer -> new SavingsAccount(customer, maxMonthlyMovements))
                .flatMap(this::validatePersonalLimit)
                .flatMap(bankAccountRepository::save);
    }

    @Override
    public Single<CheckingAccount> openCheckingAccount(String customerId, BigDecimal maintenanceFee,
                                                       List<String> holderIds, List<String> authorizedSignerIds) {
        return findCustomer(customerId)
                .map(customer -> new CheckingAccount(customer, maintenanceFee, holderIds, authorizedSignerIds))
                .flatMap(this::validatePersonalLimit)
                .flatMap(bankAccountRepository::save);
    }

    @Override
    public Single<FixedTermAccount> openFixedTermAccount(String customerId, int movementDayOfMonth) {
        return findCustomer(customerId)
                .map(customer -> new FixedTermAccount(customer, movementDayOfMonth))
                .flatMap(bankAccountRepository::save);
    }

    private Single<Customer> findCustomer(String customerId) {
        return customerClient.findById(customerId)
                .switchIfEmpty(Single.error(() -> new BusinessRuleException("El cliente no existe")));
    }

    // Personal: máximo una cuenta de ahorro y una cuenta corriente.
    private <T extends BankAccount> Single<T> validatePersonalLimit(T account) {
        if (account.getCustomer().type() != CustomerType.PERSONAL) {
            return Single.just(account);
        }
        return bankAccountRepository.findByCustomerId(account.getCustomer().id())
                .any(existing -> existing.getClass() == account.getClass())
                .flatMap(alreadyHasSameType -> alreadyHasSameType
                        ? Single.error(new BusinessRuleException("Un cliente personal solo puede tener una cuenta de este tipo"))
                        : Single.just(account));
    }
}
