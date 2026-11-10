package dev.bootcamp.accountservice.domain.model.account;

import java.math.BigDecimal;

/**
 * Aperturas de prueba.
 */
public final class TestOpenings {

    /**
     * Apertura sin mínimo y sin monto.
     */
    public static final OpeningAmount NONE = new OpeningAmount(BigDecimal.ZERO, BigDecimal.ZERO);

    private TestOpenings() {
    }
}
