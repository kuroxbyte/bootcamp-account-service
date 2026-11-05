package dev.bootcamp.accountservice.application.port;

import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import java.util.List;

public interface BankAccountRepository {
    List<BankAccount> findByCustomerId(Long customerId);

    /**
     * Guarda la cuenta y la devuelve con el mismo tipo concreto que recibió.
     */
    <T extends BankAccount> T save(T account);
}
