package dev.bootcamp.accountservice.application.port;

import dev.bootcamp.accountservice.domain.model.account.BankAccount;

public interface BankAccountRepository {
    BankAccount save(BankAccount account);
}
