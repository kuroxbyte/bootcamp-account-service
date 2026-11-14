package dev.bootcamp.accountservice.infrastructure.client.transaction;

import dev.bootcamp.accountservice.infrastructure.client.ServiceUnavailableException;
import dev.bootcamp.accountservice.infrastructure.client.StubWebClient;
import dev.bootcamp.accountservice.infrastructure.client.TestCircuitBreakers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AccountBalanceWebClientAdapterTest {

    private static AccountBalanceWebClientAdapter adapter(StubWebClient stub) {
        return new AccountBalanceWebClientAdapter(
                stub.builder(),
                "http://transaction-service",
                TestCircuitBreakers.remoteLookup(Duration.ofSeconds(1)));
    }

    @Test
    void readsTheBalanceOfTheAccount() {
        StubWebClient stub = StubWebClient.respondingWith(
                HttpStatus.OK, "{\"accountId\":\"a1\",\"balance\":12.50}");

        BigDecimal balance = adapter(stub).balance("a1", "c1").blockingGet();

        assertThat(balance).isEqualByComparingTo("12.50");
        assertThat(stub.lastPath()).isEqualTo("/api/v1/accounts/a1/balance");
    }

    @Test
    void failsAsUnavailableWhenTransactionServiceDoesNotKnowTheAccountOrFails() {
        adapter(StubWebClient.respondingWith(HttpStatus.NOT_FOUND, "{}"))
                .balance("a1", "c1").test().awaitDone(2, TimeUnit.SECONDS)
                .assertError(ServiceUnavailableException.class);
        adapter(StubWebClient.respondingWith(HttpStatus.INTERNAL_SERVER_ERROR, "{}"))
                .balance("a1", "c1").test().awaitDone(2, TimeUnit.SECONDS)
                .assertError(ServiceUnavailableException.class);
    }
}
