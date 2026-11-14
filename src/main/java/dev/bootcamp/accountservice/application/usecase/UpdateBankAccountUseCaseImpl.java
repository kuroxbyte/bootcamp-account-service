package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.application.port.AccountBalanceClient;
import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.application.port.DebitCardRepository;
import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.account.AccountChanges;
import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Implementación de {@link UpdateBankAccountUseCase}.
 */
@RequiredArgsConstructor
public class UpdateBankAccountUseCaseImpl implements UpdateBankAccountUseCase {
    private final BankAccountRepository bankAccountRepository;
    private final DebitCardRepository debitCardRepository;
    private final AccountBalanceClient accountBalanceClient;
    private final Clock clock;

    @Override
    public Single<BankAccount> update(String accountId, AccountChanges changes) {
        return GetBankAccountsUseCaseImpl.findActive(bankAccountRepository, accountId)
                .map(account -> account.withChanges(changes))
                .flatMap(bankAccountRepository::save);
    }

    @Override
    public Completable close(String accountId) {
        return GetBankAccountsUseCaseImpl.findActive(bankAccountRepository, accountId)
                .flatMap(account -> debitCardRepository.existsByAccountId(accountId)
                        .flatMap(onCard -> onCard
                                ? Single.<BankAccount>error(new BusinessRuleException(
                                        "La cuenta está asociada a una tarjeta de débito; primero"
                                                + " debe darse de baja la tarjeta"))
                                : Single.just(account)))
                .flatMap(account -> accountBalanceClient
                        .balance(accountId, account.getCustomer().id())
                        .flatMap(balance -> balance.signum() != 0
                                ? Single.<BankAccount>error(new BusinessRuleException(
                                        "La cuenta tiene saldo " + balance.toPlainString()
                                                + "; debe quedar en 0 para darla de baja"))
                                : Single.just(account)))
                .flatMap(account -> {
                    account.close(LocalDate.now(clock));
                    return bankAccountRepository.save(account);
                })
                .ignoreElement();
    }
}
