package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link GetBankAccountsUseCase}.
 */
@RequiredArgsConstructor
public class GetBankAccountsUseCaseImpl implements GetBankAccountsUseCase {
    private final BankAccountRepository bankAccountRepository;

    @Override
    public Flowable<BankAccount> findByCustomerId(String customerId) {
        return bankAccountRepository.findByCustomerId(customerId);
    }

    @Override
    public Single<BankAccount> findById(String accountId) {
        return findActive(bankAccountRepository, accountId);
    }

    @Override
    public Flowable<BankAccount> findAll() {
        return bankAccountRepository.findAll();
    }

    /**
     * Busca una cuenta activa.
     *
     * @return la cuenta; error {@link NotFoundException} si no existe o fue dada de baja
     */
    static Single<BankAccount> findActive(
            BankAccountRepository bankAccountRepository,
            String accountId) {
        return bankAccountRepository
                .findById(accountId)
                .filter(account -> !account.isClosed())
                .switchIfEmpty(Single.error(() -> new NotFoundException("La cuenta no existe")));
    }
}
