package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import lombok.Getter;

/**
 * Plazo fijo: sin comisión de mantenimiento, un solo movimiento en un día específico del mes.
 * El movimiento se valida en transaction-service.
 */
@Getter
public class FixedTermAccount extends BankAccount {
    private final int movementDayOfMonth;

    public FixedTermAccount(Customer customer, int movementDayOfMonth) {
        super(customer);
        if (movementDayOfMonth < 1 || movementDayOfMonth > 31) {
            throw new BusinessRuleException("El día de movimiento debe estar entre 1 y 31");
        }
        this.movementDayOfMonth = movementDayOfMonth;
    }
}
