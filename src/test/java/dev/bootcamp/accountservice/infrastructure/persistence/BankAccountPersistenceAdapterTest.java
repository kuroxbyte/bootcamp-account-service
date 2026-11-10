package dev.bootcamp.accountservice.infrastructure.persistence;

import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.FixedTermAccount;
import dev.bootcamp.accountservice.domain.model.account.OpeningAmount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.domain.model.account.TestOpenings;
import dev.bootcamp.accountservice.domain.model.account.VipSavingsAccount;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
@Import(BankAccountPersistenceAdapter.class)
class BankAccountPersistenceAdapterTest {

    @Container @ServiceConnection static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    private static final Customer PERSONAL = new Customer(
            "c1",
            CustomerType.PERSONAL,
            CustomerProfile.STANDARD);
    private static final Customer BUSINESS = new Customer(
            "c2",
            CustomerType.BUSINESS,
            CustomerProfile.STANDARD);

    @Autowired private BankAccountPersistenceAdapter adapter;

    @Autowired private BankAccountMongoRepository mongoRepository;

    @BeforeEach
    void cleanCollection() {
        mongoRepository.deleteAll().block();
    }

    @Test
    void savesSavingsAccountAndAssignsId() {
        SavingsAccount saved = adapter.save(
                new SavingsAccount(PERSONAL, TestOpenings.NONE, 5)).blockingGet();

        assertThat(saved.getId()).isNotBlank();
        BankAccount found = adapter.findById(saved.getId()).blockingGet();
        assertThat(found).isInstanceOf(SavingsAccount.class);
        assertThat(((SavingsAccount) found).getMaxMonthlyMovements()).isEqualTo(5);
        assertThat(found.getCustomer()).isEqualTo(PERSONAL);
    }

    @Test
    void savesCheckingAccountWithHoldersSignersAndFee() {
        CheckingAccount saved =
                adapter.save(
                                new CheckingAccount(
                                        BUSINESS, TestOpenings.NONE,
                                        new BigDecimal("15.50"),
                                        List.of("h1", "h2"),
                                        List.of("s1")))
                        .blockingGet();

        CheckingAccount found = (CheckingAccount) adapter.findById(saved.getId()).blockingGet();
        assertThat(found.getMaintenanceFee()).isEqualByComparingTo("15.50");
        assertThat(found.getHolderIds()).containsExactly("h1", "h2");
        assertThat(found.getAuthorizedSignerIds()).containsExactly("s1");
    }

    @Test
    void savesFixedTermAccount() {
        FixedTermAccount saved = adapter.save(
                new FixedTermAccount(PERSONAL, TestOpenings.NONE, 15)).blockingGet();

        FixedTermAccount found = (FixedTermAccount) adapter.findById(saved.getId()).blockingGet();
        assertThat(found.getMovementDayOfMonth()).isEqualTo(15);
    }

    @Test
    void findsAccountsByCustomer() {
        adapter.save(new SavingsAccount(PERSONAL, TestOpenings.NONE, 5)).blockingGet();
        adapter.save(new FixedTermAccount(PERSONAL, TestOpenings.NONE, 10)).blockingGet();
        adapter.save(
                new CheckingAccount(
                        BUSINESS,
                        TestOpenings.NONE,
                        BigDecimal.TEN,
                        List.of("h1"),
                        List.of()))
                .blockingGet();

        adapter.findByCustomerId("c1").test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(2);
    }

    @Test
    void returnsEmptyWhenAccountDoesNotExist() {
        adapter.findById("000000000000000000000000")
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertComplete()
                .assertNoValues();
    }

    @Test
    void keepsMinimumAndOpeningAmount() {
        OpeningAmount opening = new OpeningAmount(new BigDecimal("50"), new BigDecimal("120.75"));
        SavingsAccount saved = adapter.save(new SavingsAccount(PERSONAL, opening, 5)).blockingGet();

        BankAccount found = adapter.findById(saved.getId()).blockingGet();
        assertThat(found.getOpening().minimum()).isEqualByComparingTo("50");
        assertThat(found.getOpening().amount()).isEqualByComparingTo("120.75");
    }

    @Test
    void savesVipSavingsAccountWithMinimumDailyAverage() {
        Customer vip = new Customer("c3", CustomerType.PERSONAL, CustomerProfile.VIP);
        VipSavingsAccount saved = adapter.save(
                new VipSavingsAccount(vip, TestOpenings.NONE, 5, new BigDecimal("500")))
                .blockingGet();

        BankAccount found = adapter.findById(saved.getId()).blockingGet();
        assertThat(found).isInstanceOf(VipSavingsAccount.class);
        assertThat(((VipSavingsAccount) found).getMinimumDailyAverage())
                .isEqualByComparingTo("500");
        assertThat(found.getCustomer()).isEqualTo(vip);
    }

    @Test
    void keepsOpeningDate() {
        SavingsAccount account = new SavingsAccount(PERSONAL, TestOpenings.NONE, 5);
        account.setOpenedAt(LocalDate.of(2026, 11, 15));
        SavingsAccount saved = adapter.save(account).blockingGet();

        BankAccount found = adapter.findById(saved.getId()).blockingGet();
        assertThat(found.getOpenedAt()).isEqualTo(LocalDate.of(2026, 11, 15));
    }
}
