package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.model.customer.Customer;
import lombok.Getter;
import lombok.Setter;

/**
 * Cuenta bancaria (pasivo) de un cliente; base de las cuentas de ahorro, corriente y plazo fijo.
 */
@Getter
public abstract class BankAccount {
    @Setter private String id;
    private final Customer customer;

    /**
     * Crea la cuenta para el cliente indicado.
     */
    protected BankAccount(Customer customer) {
        this.customer = customer;
    }
}
