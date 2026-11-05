package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
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
        if (customer.type() == CustomerType.BUSINESS) {
            throw new BusinessRuleException("Un cliente empresarial no puede tener una cuenta de ahorro");
        }
        if (maxMonthlyMovements <= 0) {
            throw new BusinessRuleException("El límite de movimientos mensuales debe ser mayor a 0");
        }
        this.maxMonthlyMovements = maxMonthlyMovements;
    }
}
