package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpeningAmountTest {

    @Test
    void allowsZeroMinimumAndZeroAmount() {
        assertThatCode(() -> new OpeningAmount(BigDecimal.ZERO, BigDecimal.ZERO))
                .doesNotThrowAnyException();
    }

    @Test
    void allowsAmountEqualOrAboveMinimum() {
        assertThatCode(() -> new OpeningAmount(new BigDecimal("100"), new BigDecimal("100")))
                .doesNotThrowAnyException();
        assertThatCode(() -> new OpeningAmount(new BigDecimal("100"), new BigDecimal("250.50")))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsAmountBelowMinimum() {
        assertThatThrownBy(() -> new OpeningAmount(new BigDecimal("100"), new BigDecimal("99.99")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El monto de apertura debe ser al menos 100");
    }

    @Test
    void rejectsNegativeOrMissingValues() {
        assertThatThrownBy(() -> new OpeningAmount(new BigDecimal("-1"), BigDecimal.ZERO))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new OpeningAmount(BigDecimal.ZERO, new BigDecimal("-1")))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new OpeningAmount(null, BigDecimal.ZERO))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new OpeningAmount(BigDecimal.ZERO, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void minimumsPerAccountTypeCanBeZeroButNotNegative() {
        BigDecimal zero = BigDecimal.ZERO;

        assertThatCode(() -> new MinimumOpeningAmounts(zero, zero, zero))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> new MinimumOpeningAmounts(zero, new BigDecimal("-5"), zero))
                .isInstanceOf(BusinessRuleException.class);
    }
}
