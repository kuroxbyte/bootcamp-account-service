package dev.bootcamp.accountservice.infrastructure.web.controller;

import dev.bootcamp.accountservice.application.usecase.CloseDebitCardUseCase;
import dev.bootcamp.accountservice.application.usecase.GetDebitCardsUseCase;
import dev.bootcamp.accountservice.application.usecase.IssueDebitCardUseCase;
import dev.bootcamp.accountservice.application.usecase.UpdateDebitCardAccountsUseCase;
import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(DebitCardController.class)
class DebitCardControllerTest {

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private IssueDebitCardUseCase issueDebitCardUseCase;

    @MockitoBean private GetDebitCardsUseCase getDebitCardsUseCase;

    @MockitoBean private UpdateDebitCardAccountsUseCase updateDebitCardAccountsUseCase;

    @MockitoBean private CloseDebitCardUseCase closeDebitCardUseCase;

    private static DebitCard card(String primary, String... others) {
        DebitCard card = new DebitCard("c1", primary, List.of(others));
        card.setId("d1");
        card.setIssuedAt(LocalDate.of(2026, 11, 15));
        return card;
    }

    private WebTestClient.ResponseSpec send(String method, String path, String body) {
        return webTestClient.method(HttpMethod.valueOf(method))
                .uri(path)
                .header("X-Customer-Id", "c1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }

    @Test
    void issuesCard() {
        when(issueDebitCardUseCase.execute("c1", "a1", List.of("a2")))
                .thenReturn(Single.just(card("a1", "a2")));

        send("POST", "/api/v1/debit-cards", "{\"primaryAccountId\":\"a1\",\"accountIds\":[\"a2\"]}")
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo("d1")
                .jsonPath("$.primaryAccountId")
                .isEqualTo("a1")
                .jsonPath("$.accountIds[1]")
                .isEqualTo("a2")
                .jsonPath("$.issuedAt")
                .isEqualTo("2026-11-15");
    }

    @Test
    void rejectsCardWithoutPrimaryAccount() {
        send("POST", "/api/v1/debit-cards", "{\"accountIds\":[\"a2\"]}")
                .expectStatus()
                .isBadRequest();

        verify(issueDebitCardUseCase, never()).execute(any(), any(), any());
    }

    @Test
    void returnsUnprocessableForBrokenRulesAndDuplicatedAccounts() {
        when(issueDebitCardUseCase.execute(eq("c1"), eq("a1"), any()))
                .thenReturn(Single.error(new BusinessRuleException("deuda vencida")))
                .thenReturn(Single.error(new DuplicateKeyException("E11000")));

        send("POST", "/api/v1/debit-cards", "{\"primaryAccountId\":\"a1\"}")
                .expectStatus()
                .isEqualTo(422);
        send("POST", "/api/v1/debit-cards", "{\"primaryAccountId\":\"a1\"}")
                .expectStatus()
                .isEqualTo(422)
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo("La cuenta ya está asociada a una tarjeta de débito");
    }

    @Test
    void listsAndGetsCardsOfTheCustomer() {
        when(getDebitCardsUseCase.findByCustomerId("c1")).thenReturn(Flowable.just(card("a1")));
        when(getDebitCardsUseCase.findById("c1", "d1")).thenReturn(Single.just(card("a1")));
        when(getDebitCardsUseCase.findById("c1", "d9"))
                .thenReturn(Single.error(new NotFoundException("no existe")));

        webTestClient.get().uri("/api/v1/debit-cards").header("X-Customer-Id", "c1").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$[0].id").isEqualTo("d1");
        webTestClient.get().uri("/api/v1/debit-cards/d1").header("X-Customer-Id", "c1").exchange()
                .expectStatus().isOk();
        webTestClient.get().uri("/api/v1/debit-cards/d9").header("X-Customer-Id", "c1").exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void associatesAccountAndChangesPrimary() {
        when(updateDebitCardAccountsUseCase.associateAccount("c1", "d1", "a2"))
                .thenReturn(Single.just(card("a1", "a2")));
        when(updateDebitCardAccountsUseCase.changePrimaryAccount("c1", "d1", "a2"))
                .thenReturn(Single.just(card("a2", "a1")));

        send("POST", "/api/v1/debit-cards/d1/accounts", "{\"accountId\":\"a2\"}")
                .expectStatus().isOk()
                .expectBody().jsonPath("$.accountIds.length()").isEqualTo(2);
        send("PUT", "/api/v1/debit-cards/d1/primary-account", "{\"accountId\":\"a2\"}")
                .expectStatus().isOk()
                .expectBody().jsonPath("$.primaryAccountId").isEqualTo("a2");
    }

    @Test
    void closesCardForAdmin() {
        when(closeDebitCardUseCase.execute("d1")).thenReturn(Completable.complete());
        when(closeDebitCardUseCase.execute("d9"))
                .thenReturn(Completable.error(new NotFoundException("No existe")));

        webTestClient.delete().uri("/api/v1/debit-cards/d1").header("X-Admin-Id", "admin")
                .exchange().expectStatus().isNoContent();
        webTestClient.delete().uri("/api/v1/debit-cards/d9").header("X-Admin-Id", "admin")
                .exchange().expectStatus().isNotFound();
    }
}
