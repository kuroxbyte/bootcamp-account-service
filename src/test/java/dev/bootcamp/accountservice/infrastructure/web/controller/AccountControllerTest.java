package dev.bootcamp.accountservice.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.bootcamp.accountservice.application.usecase.CreateBankAccountUseCase;
import dev.bootcamp.accountservice.application.usecase.GetBankAccountsUseCase;
import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.FixedTermAccount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(AccountController.class)
class AccountControllerTest {

    private static final Customer PERSONAL = new Customer("1", CustomerType.PERSONAL);
    private static final Customer BUSINESS = new Customer("2", CustomerType.BUSINESS);

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreateBankAccountUseCase createBankAccountUseCase;

    @MockitoBean
    private GetBankAccountsUseCase getBankAccountsUseCase;

    private static SavingsAccount savings() {
        SavingsAccount account = new SavingsAccount(PERSONAL, 5);
        account.setId("10");
        return account;
    }

    @Test
    void opensSavingsAccount() {
        when(createBankAccountUseCase.openSavingsAccount("1", 5)).thenReturn(Single.just(savings()));

        webTestClient.post().uri("/api/v1/accounts/savings")
                .header("X-Customer-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"maxMonthlyMovements\":5}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("10")
                .jsonPath("$.customerId").isEqualTo("1")
                .jsonPath("$.type").isEqualTo("SAVINGS")
                .jsonPath("$.maxMonthlyMovements").isEqualTo(5);
    }

    @Test
    void opensCheckingAccountWithHoldersAndSigners() {
        CheckingAccount checking = new CheckingAccount(BUSINESS, BigDecimal.TEN, List.of("h1"), List.of("s1"));
        checking.setId("11");
        when(createBankAccountUseCase.openCheckingAccount("2", new BigDecimal("10.0"), List.of("h1"), List.of("s1")))
                .thenReturn(Single.just(checking));

        webTestClient.post().uri("/api/v1/accounts/checking")
                .header("X-Customer-Id", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"maintenanceFee\":10.0,\"holderIds\":[\"h1\"],\"authorizedSignerIds\":[\"s1\"]}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.type").isEqualTo("CHECKING")
                .jsonPath("$.holderIds[0]").isEqualTo("h1")
                .jsonPath("$.authorizedSignerIds[0]").isEqualTo("s1");
    }

    @Test
    void opensFixedTermAccount() {
        FixedTermAccount fixedTerm = new FixedTermAccount(PERSONAL, 15);
        fixedTerm.setId("12");
        when(createBankAccountUseCase.openFixedTermAccount("1", 15)).thenReturn(Single.just(fixedTerm));

        webTestClient.post().uri("/api/v1/accounts/fixed-term")
                .header("X-Customer-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"movementDayOfMonth\":15}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.type").isEqualTo("FIXED_TERM")
                .jsonPath("$.movementDayOfMonth").isEqualTo(15);
    }

    @Test
    void rejectsRequestWithoutCustomerHeader() {
        webTestClient.post().uri("/api/v1/accounts/savings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"maxMonthlyMovements\":5}")
                .exchange()
                .expectStatus().isBadRequest();

        verify(createBankAccountUseCase, never()).openSavingsAccount(anyString(), anyInt());
    }

    @Test
    void rejectsInvalidRequestBody() {
        webTestClient.post().uri("/api/v1/accounts/fixed-term")
                .header("X-Customer-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"movementDayOfMonth\":40}")
                .exchange()
                .expectStatus().isBadRequest();

        verify(createBankAccountUseCase, never()).openFixedTermAccount(anyString(), anyInt());
    }

    @Test
    void returnsNotFoundWhenCustomerDoesNotExist() {
        when(createBankAccountUseCase.openSavingsAccount("99", 5))
                .thenReturn(Single.error(new NotFoundException("El cliente no existe")));

        webTestClient.post().uri("/api/v1/accounts/savings")
                .header("X-Customer-Id", "99")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"maxMonthlyMovements\":5}")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void returnsUnprocessableWhenBusinessRuleIsViolated() {
        when(createBankAccountUseCase.openSavingsAccount("1", 5)).thenReturn(Single.error(
                new BusinessRuleException("Un cliente personal solo puede tener una cuenta de este tipo")));

        webTestClient.post().uri("/api/v1/accounts/savings")
                .header("X-Customer-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"maxMonthlyMovements\":5}")
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Un cliente personal solo puede tener una cuenta de este tipo");
    }

    @Test
    void listsAccountsOfCustomer() {
        when(getBankAccountsUseCase.findByCustomerId("1")).thenReturn(Flowable.just(savings()));

        webTestClient.get().uri("/api/v1/accounts")
                .header("X-Customer-Id", "1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isEqualTo("10");
    }

    @Test
    void returnsAccountById() {
        when(getBankAccountsUseCase.findById("10")).thenReturn(Single.just(savings()));

        webTestClient.get().uri("/api/v1/accounts/10")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.type").isEqualTo("SAVINGS");
    }

    @Test
    void returnsNotFoundWhenAccountDoesNotExist() {
        when(getBankAccountsUseCase.findById("99"))
                .thenReturn(Single.error(new NotFoundException("La cuenta no existe")));

        webTestClient.get().uri("/api/v1/accounts/99")
                .exchange()
                .expectStatus().isNotFound();
    }
}
