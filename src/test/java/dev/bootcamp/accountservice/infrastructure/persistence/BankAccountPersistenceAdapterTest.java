package dev.bootcamp.accountservice.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.FixedTermAccount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

@Testcontainers
@DataMongoTest
@Import(BankAccountPersistenceAdapter.class)
class BankAccountPersistenceAdapterTest {

    @Container @ServiceConnection static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    private static final Customer PERSONAL = new Customer("c1", CustomerType.PERSONAL);
    private static final Customer BUSINESS = new Customer("c2", CustomerType.BUSINESS);

    @Autowired private BankAccountPersistenceAdapter adapter;

    @Autowired private BankAccountMongoRepository mongoRepository;

    @BeforeEach
    void cleanCollection() {
        mongoRepository.deleteAll().block();
    }

    @Test
    void savesSavingsAccountAndAssignsId() {
        SavingsAccount saved = adapter.save(new SavingsAccount(PERSONAL, 5)).blockingGet();

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
                                        BUSINESS,
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
        FixedTermAccount saved = adapter.save(new FixedTermAccount(PERSONAL, 15)).blockingGet();

        FixedTermAccount found = (FixedTermAccount) adapter.findById(saved.getId()).blockingGet();
        assertThat(found.getMovementDayOfMonth()).isEqualTo(15);
    }

    @Test
    void findsAccountsByCustomer() {
        adapter.save(new SavingsAccount(PERSONAL, 5)).blockingGet();
        adapter.save(new FixedTermAccount(PERSONAL, 10)).blockingGet();
        adapter.save(new CheckingAccount(BUSINESS, BigDecimal.TEN, List.of("h1"), List.of()))
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
}
