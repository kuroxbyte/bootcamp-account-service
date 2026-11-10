package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckingAccountTest {

    private final Customer personal = new Customer(
            "1",
            CustomerType.PERSONAL,
            CustomerProfile.STANDARD);
    private final Customer business = new Customer(
            "2",
            CustomerType.BUSINESS,
            CustomerProfile.STANDARD);
    private final Customer pyme = new Customer(
            "3",
            CustomerType.BUSINESS,
            CustomerProfile.PYME);

    @Test
    void pymeAccountHasNoMaintenanceFee() {
        CheckingAccount withoutFee = new CheckingAccount(
                pyme,
                TestOpenings.NONE,
                null,
                List.of("10"),
                List.of());
        CheckingAccount withZeroFee = new CheckingAccount(
                pyme,
                TestOpenings.NONE,
                BigDecimal.ZERO,
                List.of("10"),
                List.of());

        assertThat(withoutFee.getMaintenanceFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(withZeroFee.getMaintenanceFee()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void pymeAccountRejectsMaintenanceFee() {
        assertThatThrownBy(
                        () -> new CheckingAccount(
                                pyme,
                                TestOpenings.NONE,
                                BigDecimal.TEN,
                                List.of("10"),
                                List.of()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("La cuenta corriente PYME no tiene comisión de mantenimiento");
    }

    @Test
    void requiresMaintenanceFee() {
        assertThatThrownBy(
                () -> new CheckingAccount(personal,
                TestOpenings.NONE,
                null,
                List.of(),
                List.of()))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(
                        () -> new CheckingAccount(
                                personal,
                                TestOpenings.NONE,
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
                                        TestOpenings.NONE,
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
                                TestOpenings.NONE,
                                BigDecimal.TEN,
                                List.of(),
                                List.of()))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(
                () -> new CheckingAccount(business,
                TestOpenings.NONE,
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
                        TestOpenings.NONE,
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
                        TestOpenings.NONE,
                        BigDecimal.TEN,
                        List.of("10"),
                        null);
        CheckingAccount withSigners =
                new CheckingAccount(
                        business,
                        TestOpenings.NONE,
                        BigDecimal.TEN,
                        List.of("10"),
                        List.of("20", "21"));

        assertThat(withoutSigners.getAuthorizedSignerIds()).isEmpty();
        assertThat(withSigners.getAuthorizedSignerIds()).containsExactly("20", "21");
    }
}
