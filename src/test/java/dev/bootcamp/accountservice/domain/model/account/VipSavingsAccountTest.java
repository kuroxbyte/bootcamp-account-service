package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VipSavingsAccountTest {

    private final Customer vip = new Customer("1", CustomerType.PERSONAL, CustomerProfile.VIP);
    private final Customer standard = new Customer(
            "2",
            CustomerType.PERSONAL,
            CustomerProfile.STANDARD);

    @Test
    void keepsMinimumDailyAverageOfVipCustomer() {
        VipSavingsAccount account =
                new VipSavingsAccount(vip, TestOpenings.NONE, 5, new BigDecimal("500"));

        assertThat(account.getMinimumDailyAverage()).isEqualByComparingTo("500");
        assertThat(account.getMaxMonthlyMovements()).isEqualTo(5);
    }

    @Test
    void isOnlyForVipCustomers() {
        assertThatThrownBy(
                        () -> new VipSavingsAccount(
                                standard,
                                TestOpenings.NONE,
                                5,
                                new BigDecimal("500")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("La cuenta de ahorro VIP es solo para clientes VIP");
    }

    @Test
    void rejectsMissingOrNegativeMinimumDailyAverage() {
        assertThatThrownBy(() -> new VipSavingsAccount(vip, TestOpenings.NONE, 5, null))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(
                        () -> new VipSavingsAccount(
                                vip,
                                TestOpenings.NONE,
                                5,
                                new BigDecimal("-1")))
                .isInstanceOf(BusinessRuleException.class);
    }
}
