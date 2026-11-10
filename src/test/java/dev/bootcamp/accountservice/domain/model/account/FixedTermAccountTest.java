package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FixedTermAccountTest {

    private final Customer personal = new Customer("1", CustomerType.PERSONAL);

    @Test
    void createsWithValidMovementDay() {
        assertThatCode(
                () -> new FixedTermAccount(personal,
                OpeningAmount.ZERO,
                15)).doesNotThrowAnyException();
    }

    @Test
    void rejectsInvalidMovementDay() {
        assertThatThrownBy(() -> new FixedTermAccount(personal, OpeningAmount.ZERO, 0))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new FixedTermAccount(personal, OpeningAmount.ZERO, 32))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsBusinessCustomer() {
        Customer business = new Customer("2", CustomerType.BUSINESS);

        assertThatThrownBy(() -> new FixedTermAccount(business, OpeningAmount.ZERO, 15))
                .isInstanceOf(BusinessRuleException.class);
    }
}
