package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;

import java.math.BigDecimal;

/**
 * Monto con el que se abre una cuenta y el mínimo de apertura exigido para su tipo.
 *
 * <p>El mínimo puede ser 0. El monto de apertura es el saldo inicial de la cuenta.
 *
 * @param minimum mínimo de apertura del tipo de cuenta
 * @param amount  monto con el que se abre la cuenta
 */
public record OpeningAmount(BigDecimal minimum, BigDecimal amount) {

    /**
     * Valida el mínimo y el monto de apertura.
     *
     * @throws BusinessRuleException si alguno es negativo o falta, o si el monto es menor que el
     *     mínimo
     */
    public OpeningAmount {
        if (minimum == null || minimum.signum() < 0) {
            throw new BusinessRuleException("El monto mínimo de apertura no puede ser negativo");
        }
        if (amount == null || amount.signum() < 0) {
            throw new BusinessRuleException("El monto de apertura no puede ser negativo");
        }
        if (amount.compareTo(minimum) < 0) {
            throw new BusinessRuleException(
                    "El monto de apertura debe ser al menos " + minimum.toPlainString());
        }
    }
}
