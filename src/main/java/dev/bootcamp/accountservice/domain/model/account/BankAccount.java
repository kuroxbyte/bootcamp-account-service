package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.model.customer.Customer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Cuenta bancaria (pasivo) de un cliente; base de las cuentas de ahorro, corriente y plazo fijo.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
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
     * Fecha de baja; null mientras la cuenta está activa.
     */
    @Setter private LocalDate closedAt;

    /**
     * Indica si la cuenta fue dada de baja.
     */
    public boolean isClosed() {
        return closedAt != null;
    }

    /**
     * Da de baja la cuenta (baja lógica: conserva sus datos y su historial de movimientos).
     */
    public void close(LocalDate today) {
        this.closedAt = today;
    }

    /**
     * Crea una cuenta del mismo tipo con las nuevas condiciones, validadas con las mismas reglas
     * que al abrirla. Conserva el id, el cliente, la apertura, la comisión por transacción y la
     * fecha de baja.
     *
     * @throws dev.bootcamp.accountservice.domain.exception.BusinessRuleException si falta un campo
     *     obligatorio del tipo, viene uno de otro tipo o el nuevo valor incumple una regla
     */
    public abstract BankAccount withChanges(AccountChanges changes);

    /**
     * Copia a la cuenta modificada el estado que no cambia.
     */
    protected <T extends BankAccount> T keepStateIn(T updated) {
        updated.setId(id);
        updated.setOpenedAt(openedAt);
        updated.setTransactionFee(transactionFee);
        updated.setClosedAt(closedAt);
        return updated;
    }
}
