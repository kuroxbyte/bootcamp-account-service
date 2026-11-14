package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import lombok.Getter;

import java.util.Set;

/**
 * Ahorro: sin comisión de mantenimiento, con límite máximo de movimientos mensuales. El límite se
 * valida en transaction-service.
 */
@Getter
public class SavingsAccount extends BankAccount {
    private final int maxMonthlyMovements;

    /**
     * Crea una cuenta de ahorro validando sus reglas.
     *
     * @throws BusinessRuleException si el cliente es empresarial o el límite no es mayor a 0
     */
    public SavingsAccount(
            Customer customer,
            OpeningAmount opening,
            int maxMonthlyMovements) {
        super(customer, opening);
        if (customer.type() == CustomerType.BUSINESS) {
            throw new BusinessRuleException(
                    "Un cliente empresarial no puede tener una cuenta de ahorro");
        }
        if (maxMonthlyMovements <= 0) {
            throw new BusinessRuleException(
                    "El límite de movimientos mensuales debe ser mayor a 0");
        }
        this.maxMonthlyMovements = maxMonthlyMovements;
    }

    @Override
    public SavingsAccount withChanges(AccountChanges changes) {
        changes.allowOnly("de ahorro", Set.of("maxMonthlyMovements"));
        return keepStateIn(new SavingsAccount(
                getCustomer(),
                getOpening(),
                AccountChanges.required(changes.maxMonthlyMovements(), "maxMonthlyMovements")));
    }
}
