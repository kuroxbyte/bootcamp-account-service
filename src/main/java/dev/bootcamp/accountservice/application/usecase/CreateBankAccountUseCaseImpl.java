package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.application.port.CustomerClient;
import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.FixedTermAccount;
import dev.bootcamp.accountservice.domain.model.account.MinimumOpeningAmounts;
import dev.bootcamp.accountservice.domain.model.account.OpeningAmount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementación de {@link CreateBankAccountUseCase}.
 */
public class CreateBankAccountUseCaseImpl implements CreateBankAccountUseCase {
    private static final String PERSONAL_ACCOUNT_LIMIT =
            "Un cliente personal solo puede tener una cuenta de este tipo";

    private final BankAccountRepository bankAccountRepository;
    private final CustomerClient customerClient;
    private final MinimumOpeningAmounts minimumOpeningAmounts;

    /**
     * Crea el caso de uso con los adaptadores de sus puertos y los mínimos de apertura por tipo.
     */
    public CreateBankAccountUseCaseImpl(
            BankAccountRepository bankAccountRepository,
            CustomerClient customerClient,
            MinimumOpeningAmounts minimumOpeningAmounts) {
        this.bankAccountRepository = bankAccountRepository;
        this.customerClient = customerClient;
        this.minimumOpeningAmounts = minimumOpeningAmounts;
    }

    @Override
    public Single<SavingsAccount> openSavingsAccount(
            String customerId,
            int maxMonthlyMovements,
            BigDecimal openingAmount) {
        return findCustomer(customerId)
                .map(customer -> new SavingsAccount(
                        customer,
                        new OpeningAmount(minimumOpeningAmounts.savings(), openingAmount),
                        maxMonthlyMovements))
                .flatMap(this::validatePersonalLimit)
                .flatMap(bankAccountRepository::save);
    }

    @Override
    public Single<CheckingAccount> openCheckingAccount(
            String customerId,
            BigDecimal maintenanceFee,
            List<String> holderIds,
            List<String> authorizedSignerIds,
            BigDecimal openingAmount) {
        return findCustomer(customerId)
                .map(customer -> new CheckingAccount(
                        customer,
                        new OpeningAmount(minimumOpeningAmounts.checking(), openingAmount),
                        maintenanceFee,
                        holderIds,
                        authorizedSignerIds))
                .flatMap(this::validatePersonalLimit)
                .flatMap(bankAccountRepository::save);
    }

    @Override
    public Single<FixedTermAccount> openFixedTermAccount(
            String customerId,
            int movementDayOfMonth,
            BigDecimal openingAmount) {
        return findCustomer(customerId)
                .map(customer -> new FixedTermAccount(
                        customer,
                        new OpeningAmount(minimumOpeningAmounts.fixedTerm(), openingAmount),
                        movementDayOfMonth))
                .flatMap(bankAccountRepository::save);
    }

    private Single<Customer> findCustomer(String customerId) {
        return customerClient
                .findById(customerId)
                .switchIfEmpty(Single.error(() -> new NotFoundException("El cliente no existe")));
    }

    // Personal: máximo una cuenta de ahorro y una cuenta corriente.
    private <T extends BankAccount> Single<T> validatePersonalLimit(T account) {
        if (account.getCustomer().type() != CustomerType.PERSONAL) {
            return Single.just(account);
        }
        return bankAccountRepository
                .findByCustomerId(account.getCustomer().id())
                .any(existing -> existing.getClass() == account.getClass())
                .flatMap(
                        alreadyHasSameType ->
                                alreadyHasSameType
                                        ? Single.error(
                                                new BusinessRuleException(PERSONAL_ACCOUNT_LIMIT))
                                        : Single.just(account));
    }
}
