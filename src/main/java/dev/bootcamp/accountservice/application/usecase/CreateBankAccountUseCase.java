package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.FixedTermAccount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import java.math.BigDecimal;

/**
 * Caso de uso para abrir una cuenta bancaria (pasivo) a un cliente.
 *
 * <p>Reglas:
 * <ul>
 *   <li>El cliente debe existir en customer-service.</li>
 * </ul>
 */
public interface CreateBankAccountUseCase {

    /**
     * Abre una cuenta de ahorro.
     *
     * @param customerId          id del cliente en customer-service
     * @param maxMonthlyMovements límite máximo de movimientos mensuales
     * @return la cuenta guardada
     * @throws BusinessRuleException si el cliente no existe
     */
    SavingsAccount openSavingsAccount(Long customerId, int maxMonthlyMovements);

    /**
     * Abre una cuenta corriente.
     *
     * @param customerId     id del cliente en customer-service
     * @param maintenanceFee comisión de mantenimiento
     * @return la cuenta guardada
     * @throws BusinessRuleException si el cliente no existe
     */
    CheckingAccount openCheckingAccount(Long customerId, BigDecimal maintenanceFee);

    /**
     * Abre una cuenta a plazo fijo.
     *
     * @param customerId         id del cliente en customer-service
     * @param movementDayOfMonth día del mes en que se permite el único movimiento
     * @return la cuenta guardada
     * @throws BusinessRuleException si el cliente no existe
     */
    FixedTermAccount openFixedTermAccount(Long customerId, int movementDayOfMonth);
}
