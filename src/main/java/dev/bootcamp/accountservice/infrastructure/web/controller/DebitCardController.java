package dev.bootcamp.accountservice.infrastructure.web.controller;

import dev.bootcamp.accountservice.application.usecase.CloseDebitCardUseCase;
import dev.bootcamp.accountservice.application.usecase.GetDebitCardsUseCase;
import dev.bootcamp.accountservice.application.usecase.IssueDebitCardUseCase;
import dev.bootcamp.accountservice.application.usecase.UpdateDebitCardAccountsUseCase;
import dev.bootcamp.accountservice.domain.model.debitcard.DebitCard;
import dev.bootcamp.accountservice.infrastructure.web.api.DebitCardsApi;
import dev.bootcamp.accountservice.infrastructure.web.dto.DebitCardAccountRequestDto;
import dev.bootcamp.accountservice.infrastructure.web.dto.DebitCardRequestDto;
import dev.bootcamp.accountservice.infrastructure.web.dto.DebitCardResponseDto;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Endpoints de tarjetas de débito definidos en el contrato OpenAPI.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DebitCardController implements DebitCardsApi {

    private final IssueDebitCardUseCase issueDebitCardUseCase;
    private final GetDebitCardsUseCase getDebitCardsUseCase;
    private final UpdateDebitCardAccountsUseCase updateDebitCardAccountsUseCase;
    private final CloseDebitCardUseCase closeDebitCardUseCase;

    @Override
    public Mono<ResponseEntity<DebitCardResponseDto>> issueDebitCard(
            String customerId,
            Mono<DebitCardRequestDto> debitCardRequestDto,
            ServerWebExchange exchange) {
        return debitCardRequestDto
                .flatMap(request -> toMono(issueDebitCardUseCase.execute(
                        customerId,
                        request.getPrimaryAccountId(),
                        request.getAccountIds())))
                .doOnNext(card -> log.info(
                        "Debit card issued debitCardId={} customerId={}",
                        card.getId(),
                        customerId))
                .map(card -> ResponseEntity.status(HttpStatus.CREATED).body(toResponse(card)));
    }

    @Override
    public Mono<ResponseEntity<Flux<DebitCardResponseDto>>> listDebitCards(
            String customerId,
            ServerWebExchange exchange) {
        log.debug("Listing debit cards customerId={}", customerId);
        return Mono.just(ResponseEntity.ok(
                Flux.from(getDebitCardsUseCase.findByCustomerId(customerId))
                        .map(DebitCardController::toResponse)));
    }

    @Override
    public Mono<ResponseEntity<DebitCardResponseDto>> getDebitCardById(
            String customerId,
            String debitCardId,
            ServerWebExchange exchange) {
        return toMono(getDebitCardsUseCase.findById(customerId, debitCardId))
                .map(card -> ResponseEntity.ok(toResponse(card)));
    }

    @Override
    public Mono<ResponseEntity<DebitCardResponseDto>> associateDebitCardAccount(
            String customerId,
            String debitCardId,
            Mono<DebitCardAccountRequestDto> debitCardAccountRequestDto,
            ServerWebExchange exchange) {
        return debitCardAccountRequestDto
                .flatMap(request -> toMono(updateDebitCardAccountsUseCase.associateAccount(
                        customerId,
                        debitCardId,
                        request.getAccountId())))
                .map(card -> ResponseEntity.ok(toResponse(card)));
    }

    @Override
    public Mono<ResponseEntity<DebitCardResponseDto>> changeDebitCardPrimaryAccount(
            String customerId,
            String debitCardId,
            Mono<DebitCardAccountRequestDto> debitCardAccountRequestDto,
            ServerWebExchange exchange) {
        return debitCardAccountRequestDto
                .flatMap(request -> toMono(updateDebitCardAccountsUseCase.changePrimaryAccount(
                        customerId,
                        debitCardId,
                        request.getAccountId())))
                .map(card -> ResponseEntity.ok(toResponse(card)));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteDebitCard(
            String debitCardId,
            String adminId,
            ServerWebExchange exchange) {
        return Mono.from(closeDebitCardUseCase.execute(debitCardId).toFlowable())
                .then(Mono.fromCallable(() -> {
                    log.info("Debit card closed debitCardId={} adminId={}", debitCardId, adminId);
                    return ResponseEntity.noContent().<Void>build();
                }));
    }

    private static Mono<DebitCard> toMono(Single<DebitCard> card) {
        return Mono.from(card.toFlowable());
    }

    private static DebitCardResponseDto toResponse(DebitCard card) {
        return new DebitCardResponseDto(
                card.getId(),
                card.getCustomerId(),
                card.getPrimaryAccountId(),
                card.getAccountIds(),
                card.getIssuedAt());
    }
}
