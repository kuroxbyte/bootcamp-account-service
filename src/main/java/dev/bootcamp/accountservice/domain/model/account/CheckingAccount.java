package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cuenta corriente: con comisión de mantenimiento, sin límite de movimientos mensuales. Si es
 * empresarial, tiene uno o más titulares y cero o más firmantes autorizados.
 */
@Getter
public class CheckingAccount extends BankAccount {
    private final BigDecimal maintenanceFee;
    private final List<String> holderIds;
    private final List<String> authorizedSignerIds;

    /**
     * Crea una cuenta corriente validando sus reglas.
     *
     * @throws BusinessRuleException si no hay comisión de mantenimiento o la cuenta empresarial no
     *     tiene titulares
     */
    public CheckingAccount(
            Customer customer,
            OpeningAmount opening,
            BigDecimal maintenanceFee,
            List<String> holderIds,
            List<String> authorizedSignerIds) {
        super(customer, opening);
        if (maintenanceFee == null || maintenanceFee.signum() <= 0) {
            throw new BusinessRuleException(
                    "La cuenta corriente debe tener comisión de mantenimiento");
        }
        this.maintenanceFee = maintenanceFee;
        this.holderIds = holderIds == null ? List.of() : List.copyOf(holderIds);
        this.authorizedSignerIds =
                authorizedSignerIds == null ? List.of() : List.copyOf(authorizedSignerIds);
        if (customer.type() == CustomerType.BUSINESS && this.holderIds.isEmpty()) {
            throw new BusinessRuleException(
                    "Una cuenta empresarial debe tener al menos un titular");
        }
    }
}
