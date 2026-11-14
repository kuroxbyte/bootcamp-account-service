package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountChangesTest {

    private static final Customer PERSONAL =
            new Customer("c1", CustomerType.PERSONAL, CustomerProfile.STANDARD);
    private static final Customer VIP =
            new Customer("c2", CustomerType.PERSONAL, CustomerProfile.VIP);
    private static final Customer BUSINESS =
            new Customer("c3", CustomerType.BUSINESS, CustomerProfile.STANDARD);
    private static final TransactionFee FEE = new TransactionFee(3, BigDecimal.ONE);

    private static <T extends BankAccount> T opened(T account) {
        account.setId("a1");
        account.setOpenedAt(LocalDate.of(2026, 1, 10));
        account.setTransactionFee(FEE);
        return account;
    }

    private static AccountChanges savings(Integer max) {
        return new AccountChanges(null, null, null, max, null, null);
    }

    @Test
    void savingsChangesItsLimitAndKeepsItsState() {
        SavingsAccount account = opened(new SavingsAccount(PERSONAL, TestOpenings.NONE, 5));

        SavingsAccount updated = account.withChanges(savings(8));

        assertThat(updated.getMaxMonthlyMovements()).isEqualTo(8);
        assertThat(updated.getId()).isEqualTo("a1");
        assertThat(updated.getOpenedAt()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(updated.getTransactionFee()).isEqualTo(FEE);
        assertThat(updated.getCustomer()).isEqualTo(PERSONAL);
    }

    @Test
    void savingsRequiresItsLimitAndRejectsFieldsOfOtherTypes() {
        SavingsAccount account = opened(new SavingsAccount(PERSONAL, TestOpenings.NONE, 5));

        assertThatThrownBy(() -> account.withChanges(savings(null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("maxMonthlyMovements");
        assertThatThrownBy(() -> account.withChanges(
                new AccountChanges(BigDecimal.TEN, null, null, 5, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("maintenanceFee");
        AccountChanges emptyLists = new AccountChanges(null, List.of(), List.of(), 7, null, null);
        assertThat(account.withChanges(emptyLists).getMaxMonthlyMovements()).isEqualTo(7);
        assertThatThrownBy(() -> account.withChanges(savings(0)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void vipSavingsChangesItsMinimumDailyAverage() {
        VipSavingsAccount account = opened(new VipSavingsAccount(
                VIP, TestOpenings.NONE, 5, new BigDecimal("500")));

        VipSavingsAccount updated = account.withChanges(
                new AccountChanges(null, null, null, 6, new BigDecimal("700"), null));

        assertThat(updated.getMinimumDailyAverage()).isEqualByComparingTo("700");
        assertThatThrownBy(() -> account.withChanges(savings(6)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void checkingChangesFeeHoldersAndSigners() {
        CheckingAccount account = opened(new CheckingAccount(
                BUSINESS, TestOpenings.NONE, BigDecimal.TEN, List.of("h1"), List.of()));

        CheckingAccount updated = account.withChanges(new AccountChanges(
                new BigDecimal("12"), List.of("h1", "h2"), List.of("s1"), null, null, null));

        assertThat(updated.getMaintenanceFee()).isEqualByComparingTo("12");
        assertThat(updated.getHolderIds()).containsExactly("h1", "h2");
        assertThat(updated.getAuthorizedSignerIds()).containsExactly("s1");
        assertThatThrownBy(() -> account.withChanges(new AccountChanges(
                new BigDecimal("12"), List.of(), null, null, null, null)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void fixedTermChangesItsDay() {
        FixedTermAccount account = opened(new FixedTermAccount(PERSONAL, TestOpenings.NONE, 15));

        assertThat(account.withChanges(new AccountChanges(null, null, null, null, null, 20))
                .getMovementDayOfMonth()).isEqualTo(20);
        assertThatThrownBy(() -> account.withChanges(savings(3)))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> account.withChanges(
                new AccountChanges(null, null, null, null, null, null)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void closingKeepsTheDateAndChangesKeepIt() {
        SavingsAccount account = opened(new SavingsAccount(PERSONAL, TestOpenings.NONE, 5));
        assertThat(account.isClosed()).isFalse();

        account.close(LocalDate.of(2026, 12, 1));

        assertThat(account.isClosed()).isTrue();
        assertThat(account.withChanges(savings(5)).getClosedAt())
                .isEqualTo(LocalDate.of(2026, 12, 1));
    }

    @Test
    void pymeCheckingRequiresMaintenanceFeeOfZero() {
        Customer pyme = new Customer("c4", CustomerType.BUSINESS, CustomerProfile.PYME);
        CheckingAccount account = opened(new CheckingAccount(
                pyme, TestOpenings.NONE, null, List.of("h1"), List.of()));

        assertThatThrownBy(() -> account.withChanges(new AccountChanges(
                null, List.of("h1"), List.of(), null, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("maintenanceFee");
        assertThatThrownBy(() -> account.withChanges(new AccountChanges(
                BigDecimal.TEN, List.of("h1"), List.of(), null, null, null)))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(account.withChanges(new AccountChanges(
                BigDecimal.ZERO, List.of("h1", "h2"), List.of(), null, null, null))
                .getHolderIds()).containsExactly("h1", "h2");
    }
}
