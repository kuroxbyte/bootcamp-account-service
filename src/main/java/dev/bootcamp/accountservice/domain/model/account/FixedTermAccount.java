package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import lombok.Getter;

import java.util.Set;

/**
 * Plazo fijo: sin comisión de mantenimiento, un solo movimiento en un día específico del mes. El
 * movimiento se valida en transaction-service.
 */
@Getter
public class FixedTermAccount extends BankAccount {
    private final int movementDayOfMonth;

    /**
     * Crea una cuenta a plazo fijo validando sus reglas.
     *
     * @throws BusinessRuleException si el cliente es empresarial o el día no está entre 1 y 31
     */
    public FixedTermAccount(
            Customer customer,
            OpeningAmount opening,
            int movementDayOfMonth) {
        super(customer, opening);
        if (customer.type() == CustomerType.BUSINESS) {
            throw new BusinessRuleException(
                    "Un cliente empresarial no puede tener una cuenta a plazo fijo");
        }
        if (movementDayOfMonth < 1 || movementDayOfMonth > 31) {
            throw new BusinessRuleException("El día de movimiento debe estar entre 1 y 31");
        }
        this.movementDayOfMonth = movementDayOfMonth;
    }

    @Override
    public FixedTermAccount withChanges(AccountChanges changes) {
        changes.allowOnly("a plazo fijo", Set.of("movementDayOfMonth"));
        return keepStateIn(new FixedTermAccount(
                getCustomer(),
                getOpening(),
                AccountChanges.required(changes.movementDayOfMonth(), "movementDayOfMonth")));
    }
}
