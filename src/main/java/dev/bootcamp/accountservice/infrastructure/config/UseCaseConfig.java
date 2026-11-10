package dev.bootcamp.accountservice.infrastructure.config;

import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.application.port.CreditCardClient;
import dev.bootcamp.accountservice.application.port.CustomerClient;
import dev.bootcamp.accountservice.application.usecase.CreateBankAccountUseCase;
import dev.bootcamp.accountservice.application.usecase.CreateBankAccountUseCaseImpl;
import dev.bootcamp.accountservice.application.usecase.GetBankAccountsUseCase;
import dev.bootcamp.accountservice.application.usecase.GetBankAccountsUseCaseImpl;
import dev.bootcamp.accountservice.domain.model.account.MinimumOpeningAmounts;
import dev.bootcamp.accountservice.domain.model.account.TransactionFee;
import dev.bootcamp.accountservice.domain.model.account.TransactionFees;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Clock;

/**
 * Registra los casos de uso como beans. Los casos de uso no dependen de Spring; aquí se les
 * inyectan los adaptadores de sus puertos.
 */
@Configuration
public class UseCaseConfig {

    /**
     * Registra el caso de uso {@link CreateBankAccountUseCase}.
     */
    @Bean
    public CreateBankAccountUseCase createBankAccountUseCase(
            BankAccountRepository bankAccountRepository,
            CustomerClient customerClient,
            CreditCardClient creditCardClient,
            MinimumOpeningAmounts minimumOpeningAmounts,
            TransactionFees transactionFees,
            @Value("${accounts.vip-savings.minimum-daily-average}")
            BigDecimal vipMinimumDailyAverage,
            Clock clock) {
        return new CreateBankAccountUseCaseImpl(
                bankAccountRepository,
                customerClient,
                creditCardClient,
                minimumOpeningAmounts,
                transactionFees,
                vipMinimumDailyAverage,
                clock);
    }

    /**
     * Montos mínimos de apertura por tipo de cuenta, definidos en config-repo (0 si no se indican).
     */
    @Bean
    public MinimumOpeningAmounts minimumOpeningAmounts(
            @Value("${accounts.minimum-opening-amount.savings:0}") BigDecimal savings,
            @Value("${accounts.minimum-opening-amount.checking:0}") BigDecimal checking,
            @Value("${accounts.minimum-opening-amount.fixed-term:0}") BigDecimal fixedTerm) {
        return new MinimumOpeningAmounts(savings, checking, fixedTerm);
    }

    /**
     * Comisión por transacción de cada tipo de cuenta, definida en config-repo.
     */
    @Bean
    public TransactionFees transactionFees(
            @Value("${accounts.transaction-fee.savings.free-monthly-transactions}")
            int savingsFree,
            @Value("${accounts.transaction-fee.savings.amount}") BigDecimal savingsAmount,
            @Value("${accounts.transaction-fee.checking.free-monthly-transactions}")
            int checkingFree,
            @Value("${accounts.transaction-fee.checking.amount}") BigDecimal checkingAmount,
            @Value("${accounts.transaction-fee.fixed-term.free-monthly-transactions}")
            int fixedTermFree,
            @Value("${accounts.transaction-fee.fixed-term.amount}") BigDecimal fixedTermAmount) {
        return new TransactionFees(
                new TransactionFee(savingsFree, savingsAmount),
                new TransactionFee(checkingFree, checkingAmount),
                new TransactionFee(fixedTermFree, fixedTermAmount));
    }

    /**
     * Reloj del sistema; con él se fija la fecha de apertura de las cuentas.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /**
     * Registra el caso de uso {@link GetBankAccountsUseCase}.
     */
    @Bean
    public GetBankAccountsUseCase getBankAccountsUseCase(
            BankAccountRepository bankAccountRepository) {
        return new GetBankAccountsUseCaseImpl(bankAccountRepository);
    }
}
