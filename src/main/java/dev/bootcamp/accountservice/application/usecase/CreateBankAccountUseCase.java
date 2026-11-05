package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.FixedTermAccount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import java.math.BigDecimal;
import java.util.List;

/**
 * Caso de uso para abrir una cuenta bancaria (pasivo) a un cliente.
 *
 * <p>Reglas:
 * <ul>
 *   <li>El cliente debe existir en customer-service.</li>
 *   <li>Cliente personal: máximo una cuenta de ahorro y una cuenta corriente;
 *       cuentas a plazo fijo sin límite.</li>
 *   <li>Cliente empresarial: no puede tener cuentas de ahorro ni a plazo fijo;
 *       cuentas corrientes sin límite.</li>
 *   <li>Cuenta empresarial: uno o más titulares y cero o más firmantes autorizados.</li>
 * </ul>
 */
public interface CreateBankAccountUseCase {

    /**
     * Abre una cuenta de ahorro.
     *
     * @param customerId          id del cliente en customer-service
     * @param maxMonthlyMovements límite máximo de movimientos mensuales
     * @return la cuenta guardada
     * @throws BusinessRuleException si el cliente no existe, es empresarial,
     *                               o es personal y ya tiene una cuenta de ahorro
     */
    SavingsAccount openSavingsAccount(Long customerId, int maxMonthlyMovements);

    /**
     * Abre una cuenta corriente.
     *
     * @param customerId          id del cliente en customer-service
     * @param maintenanceFee      comisión de mantenimiento
     * @param holderIds           titulares de la cuenta (obligatorio al menos uno si es empresarial)
     * @param authorizedSignerIds firmantes autorizados (puede estar vacío)
     * @return la cuenta guardada
     * @throws BusinessRuleException si el cliente no existe, es personal y ya tiene una
     *                               cuenta corriente, o es empresarial y no tiene titulares
     */
    CheckingAccount openCheckingAccount(Long customerId, BigDecimal maintenanceFee,
                                        List<Long> holderIds, List<Long> authorizedSignerIds);

    /**
     * Abre una cuenta a plazo fijo.
     *
     * @param customerId         id del cliente en customer-service
     * @param movementDayOfMonth día del mes en que se permite el único movimiento
     * @return la cuenta guardada
     * @throws BusinessRuleException si el cliente no existe o es empresarial
     */
    FixedTermAccount openFixedTermAccount(Long customerId, int movementDayOfMonth);
}
