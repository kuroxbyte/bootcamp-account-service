package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerProfile;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Ahorro VIP: cuenta de ahorro que requiere un monto mínimo de promedio diario cada mes. El
 * promedio se calcula en transaction-service.
 */
@Getter
public class VipSavingsAccount extends SavingsAccount {
    private final BigDecimal minimumDailyAverage;

    /**
     * Crea una cuenta de ahorro VIP validando sus reglas.
     *
     * @throws BusinessRuleException si el cliente no es VIP, si es empresarial, si el límite de
     *     movimientos no es mayor a 0 o si el mínimo de promedio diario falta o es negativo
     */
    public VipSavingsAccount(
            Customer customer,
            OpeningAmount opening,
            int maxMonthlyMovements,
            BigDecimal minimumDailyAverage) {
        super(customer, opening, maxMonthlyMovements);
        if (customer.profile() != CustomerProfile.VIP) {
            throw new BusinessRuleException("La cuenta de ahorro VIP es solo para clientes VIP");
        }
        if (minimumDailyAverage == null || minimumDailyAverage.signum() < 0) {
            throw new BusinessRuleException(
                    "El monto mínimo de promedio diario no puede ser negativo");
        }
        this.minimumDailyAverage = minimumDailyAverage;
    }
}
