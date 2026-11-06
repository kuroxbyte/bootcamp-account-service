package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.model.customer.Customer;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class BankAccount {
    @Setter
    private String id;
    private final Customer customer;

    protected BankAccount(Customer customer) {
        this.customer = customer;
    }
}
