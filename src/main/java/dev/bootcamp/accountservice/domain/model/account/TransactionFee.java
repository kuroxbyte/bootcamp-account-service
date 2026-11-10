package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;

import java.math.BigDecimal;

/**
 * Comisión por transacción de una cuenta: cada mes calendario, los primeros depósitos y retiros no
 * pagan comisión; cada uno de los siguientes paga un monto fijo. Se cobra en transaction-service.
 *
 * @param freeMonthlyTransactions cantidad de transacciones al mes sin comisión; puede ser 0
 * @param amount                  comisión fija por cada transacción que supera esa cantidad
 */
public record TransactionFee(int freeMonthlyTransactions, BigDecimal amount) {

    /**
     * Valida la cantidad de transacciones sin comisión y el monto de la comisión.
     *
     * @throws BusinessRuleException si alguno es negativo o falta el monto
     */
    public TransactionFee {
        if (freeMonthlyTransactions < 0) {
            throw new BusinessRuleException(
                    "Las transacciones sin comisión no pueden ser negativas");
        }
        if (amount == null || amount.signum() < 0) {
            throw new BusinessRuleException(
                    "La comisión por transacción no puede ser negativa");
        }
    }
}
