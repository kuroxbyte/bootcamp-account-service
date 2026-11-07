package dev.bootcamp.accountservice.infrastructure.config;

import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.application.port.CustomerClient;
import dev.bootcamp.accountservice.application.usecase.CreateBankAccountUseCase;
import dev.bootcamp.accountservice.application.usecase.CreateBankAccountUseCaseImpl;
import dev.bootcamp.accountservice.application.usecase.GetBankAccountsUseCase;
import dev.bootcamp.accountservice.application.usecase.GetBankAccountsUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los casos de uso como beans. Los casos de uso no dependen de Spring;
 * aquí se les inyectan los adaptadores de sus puertos.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public CreateBankAccountUseCase createBankAccountUseCase(BankAccountRepository bankAccountRepository,
                                                             CustomerClient customerClient) {
        return new CreateBankAccountUseCaseImpl(bankAccountRepository, customerClient);
    }

    @Bean
    public GetBankAccountsUseCase getBankAccountsUseCase(BankAccountRepository bankAccountRepository) {
        return new GetBankAccountsUseCaseImpl(bankAccountRepository);
    }
}
