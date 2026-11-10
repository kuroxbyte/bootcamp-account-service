package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.application.port.CreditCardClient;
import dev.bootcamp.accountservice.application.port.CustomerClient;
import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.FixedTermAccount;
import dev.bootcamp.accountservice.domain.model.account.MinimumOpeningAmounts;
import dev.bootcamp.accountservice.domain.model.account.OpeningAmount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.domain.model.account.TransactionFee;
import dev.bootcamp.accountservice.domain.model.account.TransactionFees;
import dev.bootcamp.accountservice.domain.model.account.VipSavingsAccount;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementación de {@link CreateBankAccountUseCase}.
 */
public class CreateBankAccountUseCaseImpl implements CreateBankAccountUseCase {
    private static final String PERSONAL_ACCOUNT_LIMIT =
            "Un cliente personal solo puede tener una cuenta de este tipo";
    private static final String CREDIT_CARD_REQUIRED =
            "El cliente debe tener una tarjeta de crédito con el banco para abrir esta cuenta";

    private final BankAccountRepository bankAccountRepository;
    private final CustomerClient customerClient;
    private final CreditCardClient creditCardClient;
    private final MinimumOpeningAmounts minimumOpeningAmounts;
    private final TransactionFees transactionFees;
    private final BigDecimal vipMinimumDailyAverage;
    private final Clock clock;

    /**
     * Crea el caso de uso con los adaptadores de sus puertos y la configuración de los productos.
     *
     * @param minimumOpeningAmounts  mínimos de apertura por tipo de cuenta
     * @param transactionFees        comisión por transacción por tipo de cuenta
     * @param vipMinimumDailyAverage mínimo de promedio diario mensual de la cuenta de ahorro VIP
     * @param clock                  reloj con el que se fija la fecha de apertura
     */
    public CreateBankAccountUseCaseImpl(
            BankAccountRepository bankAccountRepository,
            CustomerClient customerClient,
            CreditCardClient creditCardClient,
            MinimumOpeningAmounts minimumOpeningAmounts,
            TransactionFees transactionFees,
            BigDecimal vipMinimumDailyAverage,
            Clock clock) {
        this.bankAccountRepository = bankAccountRepository;
        this.customerClient = customerClient;
        this.creditCardClient = creditCardClient;
        this.minimumOpeningAmounts = minimumOpeningAmounts;
        this.transactionFees = transactionFees;
        this.vipMinimumDailyAverage = vipMinimumDailyAverage;
        this.clock = clock;
    }

    @Override
    public Single<SavingsAccount> openSavingsAccount(
            String customerId,
            int maxMonthlyMovements,
            BigDecimal openingAmount) {
        return findCustomer(customerId)
                .map(customer -> savingsAccount(customer, maxMonthlyMovements, openingAmount))
                .flatMap(account -> validateCreditCard(account, CustomerProfile.VIP))
                .flatMap(account -> validatePersonalLimit(account, SavingsAccount.class))
                .flatMap(account -> save(account, transactionFees.savings()));
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
                .flatMap(account -> validateCreditCard(account, CustomerProfile.PYME))
                .flatMap(account -> validatePersonalLimit(account, CheckingAccount.class))
                .flatMap(account -> save(account, transactionFees.checking()));
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
                .flatMap(account -> save(account, transactionFees.fixedTerm()));
    }

    private Single<Customer> findCustomer(String customerId) {
        return customerClient
                .findById(customerId)
                .switchIfEmpty(Single.error(() -> new NotFoundException("El cliente no existe")));
    }

    // Un cliente VIP siempre abre la cuenta de ahorro VIP.
    private SavingsAccount savingsAccount(
            Customer customer,
            int maxMonthlyMovements,
            BigDecimal openingAmount) {
        OpeningAmount opening = new OpeningAmount(minimumOpeningAmounts.savings(), openingAmount);
        if (customer.profile() == CustomerProfile.VIP) {
            return new VipSavingsAccount(
                    customer,
                    opening,
                    maxMonthlyMovements,
                    vipMinimumDailyAverage);
        }
        return new SavingsAccount(customer, opening, maxMonthlyMovements);
    }

    // VIP (ahorro) y PYME (corriente): el cliente debe tener una tarjeta de crédito al abrirla.
    private <T extends BankAccount> Single<T> validateCreditCard(
            T account,
            CustomerProfile profileRequiringCard) {
        if (account.getCustomer().profile() != profileRequiringCard) {
            return Single.just(account);
        }
        return creditCardClient
                .hasCreditCard(account.getCustomer().id())
                .flatMap(hasCard -> hasCard
                        ? Single.just(account)
                        : Single.error(new BusinessRuleException(CREDIT_CARD_REQUIRED)));
    }

    // Personal: máximo una cuenta de ahorro (estándar o VIP) y una cuenta corriente.
    private <T extends BankAccount> Single<T> validatePersonalLimit(
            T account,
            Class<? extends BankAccount> product) {
        if (account.getCustomer().type() != CustomerType.PERSONAL) {
            return Single.just(account);
        }
        return bankAccountRepository
                .findByCustomerId(account.getCustomer().id())
                .any(product::isInstance)
                .flatMap(alreadyHasProduct -> alreadyHasProduct
                        ? Single.error(new BusinessRuleException(PERSONAL_ACCOUNT_LIMIT))
                        : Single.just(account));
    }

    private <T extends BankAccount> Single<T> save(T account, TransactionFee transactionFee) {
        account.setOpenedAt(LocalDate.now(clock));
        account.setTransactionFee(transactionFee);
        return bankAccountRepository.save(account);
    }
}
