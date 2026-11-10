package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckingAccountTest {

    private final Customer personal = new Customer("1", CustomerType.PERSONAL);
    private final Customer business = new Customer("2", CustomerType.BUSINESS);

    @Test
    void requiresMaintenanceFee() {
        assertThatThrownBy(
                () -> new CheckingAccount(personal,
                OpeningAmount.ZERO,
                null,
                List.of(),
                List.of()))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(
                        () -> new CheckingAccount(
                                personal,
                                OpeningAmount.ZERO,
                                BigDecimal.ZERO,
                                List.of(),
                                List.of()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void allowsBusinessCustomer() {
        assertThatCode(
                        () ->
                                new CheckingAccount(
                                        business,
                                        OpeningAmount.ZERO,
                                        BigDecimal.TEN,
                                        List.of("10"),
                                        List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void businessAccountRequiresAtLeastOneHolder() {
        assertThatThrownBy(
                        () -> new CheckingAccount(
                                business,
                                OpeningAmount.ZERO,
                                BigDecimal.TEN,
                                List.of(),
                                List.of()))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(
                () -> new CheckingAccount(business,
                OpeningAmount.ZERO,
                BigDecimal.TEN,
                null,
                List.of()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void businessAccountAllowsMultipleHolders() {
        CheckingAccount account =
                new CheckingAccount(
                        business,
                        OpeningAmount.ZERO,
                        BigDecimal.TEN,
                        List.of("10", "11"),
                        List.of());

        assertThat(account.getHolderIds()).containsExactly("10", "11");
    }

    @Test
    void businessAccountAllowsZeroOrMoreAuthorizedSigners() {
        CheckingAccount withoutSigners =
                new CheckingAccount(
                        business,
                        OpeningAmount.ZERO,
                        BigDecimal.TEN,
                        List.of("10"),
                        null);
        CheckingAccount withSigners =
                new CheckingAccount(
                        business,
                        OpeningAmount.ZERO,
                        BigDecimal.TEN,
                        List.of("10"),
                        List.of("20", "21"));

        assertThat(withoutSigners.getAuthorizedSignerIds()).isEmpty();
        assertThat(withSigners.getAuthorizedSignerIds()).containsExactly("20", "21");
    }
}
