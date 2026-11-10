package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;

import java.math.BigDecimal;

/**
 * Monto mínimo de apertura de cada tipo de cuenta; cada uno puede ser 0.
 *
 * @param savings   mínimo de la cuenta de ahorro
 * @param checking  mínimo de la cuenta corriente
 * @param fixedTerm mínimo de la cuenta a plazo fijo
 */
public record MinimumOpeningAmounts(BigDecimal savings, BigDecimal checking, BigDecimal fixedTerm) {

    /**
     * Valida que ningún mínimo sea negativo.
     *
     * @throws BusinessRuleException si algún mínimo falta o es negativo
     */
    public MinimumOpeningAmounts {
        for (BigDecimal minimum : new BigDecimal[] {savings, checking, fixedTerm}) {
            if (minimum == null || minimum.signum() < 0) {
                throw new BusinessRuleException(
                        "El monto mínimo de apertura no puede ser negativo");
            }
        }
    }
}
