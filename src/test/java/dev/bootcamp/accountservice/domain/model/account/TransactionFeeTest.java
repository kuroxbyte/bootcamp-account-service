package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionFeeTest {

    @Test
    void allowsZeroFreeTransactionsAndZeroAmount() {
        assertThatCode(() -> new TransactionFee(0, BigDecimal.ZERO)).doesNotThrowAnyException();
    }

    @Test
    void rejectsNegativeFreeTransactions() {
        assertThatThrownBy(() -> new TransactionFee(-1, BigDecimal.ONE))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsMissingOrNegativeAmount() {
        assertThatThrownBy(() -> new TransactionFee(3, null))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new TransactionFee(3, new BigDecimal("-1")))
                .isInstanceOf(BusinessRuleException.class);
    }
}
