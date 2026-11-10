package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.FixedTermAccount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;
import java.util.List;

/**
 * Caso de uso para abrir una cuenta bancaria (pasivo) a un cliente.
 *
 * <p>Reglas:
 *
 * <ul>
 *   <li>El cliente debe existir en customer-service.
 *   <li>Cliente personal: máximo una cuenta de ahorro y una cuenta corriente; cuentas a plazo fijo
 *       sin límite.
 *   <li>Cliente empresarial: no puede tener cuentas de ahorro ni a plazo fijo; cuentas corrientes
 *       sin límite.
 *   <li>Cuenta empresarial: uno o más titulares y cero o más firmantes autorizados.
 *   <li>El monto de apertura debe ser al menos el mínimo de apertura del tipo de cuenta, que puede
 *       ser 0; es el saldo inicial de la cuenta.
 * </ul>
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe al {@link Single}. Termina con error {@link
 * NotFoundException} si el cliente o el producto no existe (o no pertenece al cliente), y con error
 * {@link BusinessRuleException} si se incumple una regla de negocio; en ambos casos no se guarda
 * nada.
 */
public interface CreateBankAccountUseCase {

    /**
     * Abre una cuenta de ahorro.
     *
     * @param customerId id del cliente en customer-service
     * @param maxMonthlyMovements límite máximo de movimientos mensuales
     * @param openingAmount monto de apertura
     * @return la cuenta guardada; error {@link NotFoundException} si el cliente no existe; {@link
     *     BusinessRuleException} si es empresarial, es personal y ya tiene una cuenta de ahorro, o
     *     el monto de apertura es menor que el mínimo
     */
    Single<SavingsAccount> openSavingsAccount(
            String customerId,
            int maxMonthlyMovements,
            BigDecimal openingAmount);

    /**
     * Abre una cuenta corriente.
     *
     * @param customerId id del cliente en customer-service
     * @param maintenanceFee comisión de mantenimiento
     * @param holderIds titulares de la cuenta (obligatorio al menos uno si es empresarial)
     * @param authorizedSignerIds firmantes autorizados (puede estar vacío)
     * @param openingAmount monto de apertura
     * @return la cuenta guardada; error {@link NotFoundException} si el cliente no existe; {@link
     *     BusinessRuleException} si es personal y ya tiene una cuenta corriente, es empresarial y
     *     no tiene titulares, o el monto de apertura es menor que el mínimo
     */
    Single<CheckingAccount> openCheckingAccount(
            String customerId,
            BigDecimal maintenanceFee,
            List<String> holderIds,
            List<String> authorizedSignerIds,
            BigDecimal openingAmount);

    /**
     * Abre una cuenta a plazo fijo.
     *
     * @param customerId id del cliente en customer-service
     * @param movementDayOfMonth día del mes en que se permite el único movimiento
     * @param openingAmount monto de apertura
     * @return la cuenta guardada; error {@link NotFoundException} si el cliente no existe; {@link
     *     BusinessRuleException} si es empresarial o el monto de apertura es menor que el mínimo
     */
    Single<FixedTermAccount> openFixedTermAccount(
            String customerId,
            int movementDayOfMonth,
            BigDecimal openingAmount);
}
