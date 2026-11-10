package dev.bootcamp.accountservice.infrastructure.client.credit;

import dev.bootcamp.accountservice.infrastructure.client.ServiceUnavailableException;
import dev.bootcamp.accountservice.infrastructure.client.StubWebClient;
import dev.bootcamp.accountservice.infrastructure.client.TestCircuitBreakers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class CreditCardWebClientAdapterTest {

    private static CreditCardWebClientAdapter adapter(StubWebClient stub, Duration timeout) {
        return new CreditCardWebClientAdapter(
                stub.builder(),
                "http://credit-service",
                TestCircuitBreakers.remoteLookup(timeout));
    }

    @Test
    void customerWithCreditCardHasCreditCard() {
        StubWebClient stub = StubWebClient.respondingWith(
                HttpStatus.OK,
                "[{\"id\":\"c1\",\"customerId\":\"1\",\"creditLimit\":1000}]");

        adapter(stub, Duration.ofSeconds(1))
                .hasCreditCard("1")
                .test()
                .awaitDone(2, TimeUnit.SECONDS)
                .assertValue(true);
        assertThat(stub.lastPath()).isEqualTo("/api/v1/credit-cards");
        assertThat(stub.lastHeader("X-Customer-Id")).isEqualTo("1");
    }

    @Test
    void customerWithoutCreditCardsHasNoCreditCard() {
        StubWebClient stub = StubWebClient.respondingWith(HttpStatus.OK, "[]");

        adapter(stub, Duration.ofSeconds(1))
                .hasCreditCard("1")
                .test()
                .awaitDone(2, TimeUnit.SECONDS)
                .assertValue(false);
    }

    @Test
    void failsAsUnavailableOnServerError() {
        StubWebClient stub =
                StubWebClient.respondingWith(HttpStatus.INTERNAL_SERVER_ERROR, "{\"status\":500}");

        adapter(stub, Duration.ofSeconds(1))
                .hasCreditCard("1")
                .test()
                .awaitDone(2, TimeUnit.SECONDS)
                .assertError(ServiceUnavailableException.class);
    }

    @Test
    void failsAsUnavailableOnTimeout() {
        adapter(StubWebClient.neverResponding(), Duration.ofMillis(100))
                .hasCreditCard("1")
                .test()
                .awaitDone(2, TimeUnit.SECONDS)
                .assertError(ServiceUnavailableException.class);
    }
}
