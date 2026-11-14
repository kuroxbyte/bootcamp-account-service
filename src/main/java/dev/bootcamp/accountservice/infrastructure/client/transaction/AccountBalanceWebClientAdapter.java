package dev.bootcamp.accountservice.infrastructure.client.transaction;

import dev.bootcamp.accountservice.application.port.AccountBalanceClient;
import dev.bootcamp.accountservice.infrastructure.client.RemoteLookup;
import dev.bootcamp.accountservice.infrastructure.client.ServiceUnavailableException;
import io.reactivex.rxjava3.core.Single;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

/**
 * Implementa el puerto {@link AccountBalanceClient} con
 * {@code GET /api/v1/accounts/{accountId}/balance} de transaction-service, protegido con un
 * circuit breaker.
 */
@Component
public class AccountBalanceWebClientAdapter implements AccountBalanceClient {

    private static final String SERVICE_NAME = "transaction-service";

    private final WebClient webClient;
    private final RemoteLookup remoteLookup;

    /**
     * Crea el cliente con la URL base configurada.
     */
    public AccountBalanceWebClientAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${clients.transaction-service.base-url}") String baseUrl,
            RemoteLookup remoteLookup) {
        this.webClient = webClientBuilder.clone().baseUrl(baseUrl).build();
        this.remoteLookup = remoteLookup;
    }

    @Override
    public Single<BigDecimal> balance(String accountId, String customerId) {
        return remoteLookup
                .findById(
                        webClient.get()
                                .uri("/api/v1/accounts/{accountId}/balance", accountId)
                                .header("X-Customer-Id", customerId)
                                .retrieve()
                                .bodyToMono(AccountBalanceResponse.class)
                                .map(AccountBalanceResponse::balance),
                        SERVICE_NAME,
                        accountId)
                .switchIfEmpty(Single.error(() -> new ServiceUnavailableException(
                        SERVICE_NAME + " no conoce la cuenta " + accountId, null)));
    }

    /**
     * Respuesta del saldo de transaction-service ({@code AccountBalanceResponse} de su contrato).
     *
     * @param accountId cuenta consultada
     * @param balance   saldo actual
     */
    record AccountBalanceResponse(String accountId, BigDecimal balance) {}
}
