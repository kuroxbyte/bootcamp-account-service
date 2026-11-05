package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import lombok.Getter;

/**
 * Ahorro: sin comisión de mantenimiento, con límite máximo de movimientos mensuales.
 * El límite se valida en transaction-service.
 */
@Getter
public class SavingsAccount extends BankAccount {
    private final int maxMonthlyMovements;

    public SavingsAccount(Customer customer, int maxMonthlyMovements) {
        super(customer);
        if (maxMonthlyMovements <= 0) {
            throw new BusinessRuleException("El límite de movimientos mensuales debe ser mayor a 0");
        }
        this.maxMonthlyMovements = maxMonthlyMovements;
    }
}
