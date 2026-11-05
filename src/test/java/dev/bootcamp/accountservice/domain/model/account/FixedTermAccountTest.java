package dev.bootcamp.accountservice.domain.model.account;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.Test;

class FixedTermAccountTest {

    private final Customer personal = new Customer(1L, CustomerType.PERSONAL);

    @Test
    void createsWithValidMovementDay() {
        assertThatCode(() -> new FixedTermAccount(personal, 15)).doesNotThrowAnyException();
    }

    @Test
    void rejectsInvalidMovementDay() {
        assertThatThrownBy(() -> new FixedTermAccount(personal, 0))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new FixedTermAccount(personal, 32))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsBusinessCustomer() {
        Customer business = new Customer(2L, CustomerType.BUSINESS);

        assertThatThrownBy(() -> new FixedTermAccount(business, 15))
                .isInstanceOf(BusinessRuleException.class);
    }
}
