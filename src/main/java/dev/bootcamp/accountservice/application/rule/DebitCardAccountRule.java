package dev.bootcamp.accountservice.application.rule;

import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.application.port.DebitCardRepository;
import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Regla para asociar una cuenta a una tarjeta de débito: la cuenta debe existir, ser del cliente y
 * no estar asociada a ninguna otra tarjeta (una cuenta, una tarjeta).
 */
public class DebitCardAccountRule {

    private final BankAccountRepository bankAccountRepository;
    private final DebitCardRepository debitCardRepository;

    /**
     * Crea la regla con los adaptadores de cuentas y tarjetas.
     */
    public DebitCardAccountRule(
            BankAccountRepository bankAccountRepository,
            DebitCardRepository debitCardRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.debitCardRepository = debitCardRepository;
    }

    /**
     * Verifica que la cuenta se pueda asociar a una tarjeta del cliente.
     *
     * @return termina sin error si se puede; error {@link BusinessRuleException} si la cuenta no
     *     existe, es de otro cliente o ya está asociada a una tarjeta
     */
    public Completable verify(String customerId, String accountId) {
        return bankAccountRepository.findById(accountId)
                .filter(account -> account.getCustomer().id().equals(customerId))
                .switchIfEmpty(Single.error(() -> new BusinessRuleException(
                        "La cuenta " + accountId + " no existe o no es del cliente")))
                .flatMapCompletable(account -> debitCardRepository.existsByAccountId(accountId)
                        .flatMapCompletable(associated -> associated
                                ? Completable.error(new BusinessRuleException(
                                        "La cuenta " + accountId
                                                + " ya está asociada a una tarjeta de débito"))
                                : Completable.complete()));
    }
}
