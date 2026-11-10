package dev.bootcamp.accountservice.infrastructure.config;

import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.application.port.CustomerClient;
import dev.bootcamp.accountservice.application.usecase.CreateBankAccountUseCase;
import dev.bootcamp.accountservice.application.usecase.CreateBankAccountUseCaseImpl;
import dev.bootcamp.accountservice.application.usecase.GetBankAccountsUseCase;
import dev.bootcamp.accountservice.application.usecase.GetBankAccountsUseCaseImpl;
import dev.bootcamp.accountservice.domain.model.account.MinimumOpeningAmounts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

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
            MinimumOpeningAmounts minimumOpeningAmounts) {
        return new CreateBankAccountUseCaseImpl(
                bankAccountRepository,
                customerClient,
                minimumOpeningAmounts);
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
     * Registra el caso de uso {@link GetBankAccountsUseCase}.
     */
    @Bean
    public GetBankAccountsUseCase getBankAccountsUseCase(
            BankAccountRepository bankAccountRepository) {
        return new GetBankAccountsUseCaseImpl(bankAccountRepository);
    }
}
