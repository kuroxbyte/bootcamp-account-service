package dev.bootcamp.accountservice.domain.model.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CheckingAccountTest {

    private final Customer personal = new Customer(1L, CustomerType.PERSONAL);
    private final Customer business = new Customer(2L, CustomerType.BUSINESS);

    @Test
    void requiresMaintenanceFee() {
        assertThatThrownBy(() -> new CheckingAccount(personal, null, List.of(), List.of()))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new CheckingAccount(personal, BigDecimal.ZERO, List.of(), List.of()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void allowsBusinessCustomer() {
        assertThatCode(() -> new CheckingAccount(business, BigDecimal.TEN, List.of(10L), List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void businessAccountRequiresAtLeastOneHolder() {
        assertThatThrownBy(() -> new CheckingAccount(business, BigDecimal.TEN, List.of(), List.of()))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new CheckingAccount(business, BigDecimal.TEN, null, List.of()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void businessAccountAllowsMultipleHolders() {
        CheckingAccount account = new CheckingAccount(business, BigDecimal.TEN, List.of(10L, 11L), List.of());

        assertThat(account.getHolderIds()).containsExactly(10L, 11L);
    }

    @Test
    void businessAccountAllowsZeroOrMoreAuthorizedSigners() {
        CheckingAccount withoutSigners = new CheckingAccount(business, BigDecimal.TEN, List.of(10L), null);
        CheckingAccount withSigners = new CheckingAccount(business, BigDecimal.TEN, List.of(10L), List.of(20L, 21L));

        assertThat(withoutSigners.getAuthorizedSignerIds()).isEmpty();
        assertThat(withSigners.getAuthorizedSignerIds()).containsExactly(20L, 21L);
    }
}
