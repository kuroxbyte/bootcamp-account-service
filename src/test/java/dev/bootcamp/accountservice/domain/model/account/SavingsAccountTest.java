package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SavingsAccountTest {

    private final Customer personal = new Customer(
            "1",
            CustomerType.PERSONAL,
            CustomerProfile.STANDARD);

    @Test
    void createsWithPositiveMovementLimit() {
        assertThatCode(
                () -> new SavingsAccount(personal,
                TestOpenings.NONE,
                5)).doesNotThrowAnyException();
    }

    @Test
    void rejectsNonPositiveMovementLimit() {
        assertThatThrownBy(() -> new SavingsAccount(personal, TestOpenings.NONE, 0))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsBusinessCustomer() {
        Customer business = new Customer("2", CustomerType.BUSINESS, CustomerProfile.STANDARD);

        assertThatThrownBy(() -> new SavingsAccount(business, TestOpenings.NONE, 5))
                .isInstanceOf(BusinessRuleException.class);
    }
}
