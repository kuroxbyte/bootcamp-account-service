package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.model.customer.Customer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Cuenta bancaria (pasivo) de un cliente; base de las cuentas de ahorro, corriente y plazo fijo.
 */
@Getter
public abstract class BankAccount {
    @Setter private String id;
    private final Customer customer;
    private final OpeningAmount opening;

    /**
     * Fecha de apertura; desde ese día se calcula el saldo promedio diario.
     */
    @Setter private LocalDate openedAt;

    /**
     * Transacciones al mes sin comisión y comisión por cada transacción adicional, según el tipo
     * de cuenta al momento de abrirla.
     */
    @Setter private TransactionFee transactionFee;

    /**
     * Crea la cuenta para el cliente indicado con su monto de apertura.
     */
    protected BankAccount(Customer customer, OpeningAmount opening) {
        this.customer = customer;
        this.opening = opening;
    }
}
