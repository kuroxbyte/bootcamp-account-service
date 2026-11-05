package dev.bootcamp.accountservice.domain.model.account;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CheckingAccountTest {

    private final Customer personal = new Customer(1L, CustomerType.PERSONAL);

    @Test
    void requiresMaintenanceFee() {
        assertThatThrownBy(() -> new CheckingAccount(personal, null))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new CheckingAccount(personal, BigDecimal.ZERO))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void allowsBusinessCustomer() {
        Customer business = new Customer(2L, CustomerType.BUSINESS);

        assertThatCode(() -> new CheckingAccount(business, BigDecimal.TEN))
                .doesNotThrowAnyException();
    }
}
