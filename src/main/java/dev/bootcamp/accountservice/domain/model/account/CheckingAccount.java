package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import java.math.BigDecimal;
import lombok.Getter;

/** Cuenta corriente: con comisión de mantenimiento, sin límite de movimientos mensuales. */
@Getter
public class CheckingAccount extends BankAccount {
    private final BigDecimal maintenanceFee;

    public CheckingAccount(Customer customer, BigDecimal maintenanceFee) {
        super(customer);
        if (maintenanceFee == null || maintenanceFee.signum() <= 0) {
            throw new BusinessRuleException("La cuenta corriente debe tener comisión de mantenimiento");
        }
        this.maintenanceFee = maintenanceFee;
    }
}
