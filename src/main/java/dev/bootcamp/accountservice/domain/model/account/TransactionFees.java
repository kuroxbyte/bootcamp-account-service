package dev.bootcamp.accountservice.domain.model.account;

/**
 * Comisión por transacción de cada tipo de cuenta; se copia a la cuenta al abrirla.
 *
 * @param savings   comisión de la cuenta de ahorro (también la VIP)
 * @param checking  comisión de la cuenta corriente
 * @param fixedTerm comisión de la cuenta a plazo fijo
 */
public record TransactionFees(
        TransactionFee savings,
        TransactionFee checking,
        TransactionFee fixedTerm) {}
