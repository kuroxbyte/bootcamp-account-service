package dev.bootcamp.accountservice.application.port;

import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import java.util.List;

public interface BankAccountRepository {
    List<BankAccount> findByCustomerId(Long customerId);

    BankAccount save(BankAccount account);
}
