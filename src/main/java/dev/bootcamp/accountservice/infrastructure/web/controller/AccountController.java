package dev.bootcamp.accountservice.infrastructure.web.controller;

import dev.bootcamp.accountservice.application.usecase.CreateBankAccountUseCase;
import dev.bootcamp.accountservice.application.usecase.GetBankAccountsUseCase;
import dev.bootcamp.accountservice.application.usecase.UpdateBankAccountUseCase;
import dev.bootcamp.accountservice.domain.model.account.AccountChanges;
import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import dev.bootcamp.accountservice.infrastructure.web.api.AccountsApi;
import dev.bootcamp.accountservice.infrastructure.web.dto.AccountResponseDto;
import dev.bootcamp.accountservice.infrastructure.web.dto.AccountUpdateRequestDto;
import dev.bootcamp.accountservice.infrastructure.web.dto.CheckingAccountRequestDto;
import dev.bootcamp.accountservice.infrastructure.web.dto.FixedTermAccountRequestDto;
import dev.bootcamp.accountservice.infrastructure.web.dto.SavingsAccountRequestDto;
import dev.bootcamp.accountservice.infrastructure.web.mapper.AccountWebMapper;
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
 * Endpoints de cuentas bancarias definidos en el contrato OpenAPI.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountController implements AccountsApi {

    private final CreateBankAccountUseCase createBankAccountUseCase;
    private final GetBankAccountsUseCase getBankAccountsUseCase;
    private final UpdateBankAccountUseCase updateBankAccountUseCase;

    @Override
    public Mono<ResponseEntity<Flux<AccountResponseDto>>> listAccounts(
            String customerId,
            ServerWebExchange exchange) {
        log.debug("Listing accounts customerId={}", customerId);
        Flux<AccountResponseDto> accounts =
                Flux.from(getBankAccountsUseCase.findByCustomerId(customerId))
                        .map(AccountWebMapper::toResponse);
        return Mono.just(ResponseEntity.ok(accounts));
    }

    @Override
    public Mono<ResponseEntity<Flux<AccountResponseDto>>> listAllAccounts(
            ServerWebExchange exchange) {
        log.debug("Listing all accounts for reports");
        return Mono.just(ResponseEntity.ok(
                Flux.from(getBankAccountsUseCase.findAll()).map(AccountWebMapper::toResponse)));
    }

    @Override
    public Mono<ResponseEntity<AccountResponseDto>> getAccountById(
            String accountId,
            ServerWebExchange exchange) {
        log.debug("Fetching account accountId={}", accountId);
        return Mono.from(getBankAccountsUseCase.findById(accountId).toFlowable())
                .map(account -> ResponseEntity.ok(AccountWebMapper.toResponse(account)));
    }

    @Override
    public Mono<ResponseEntity<AccountResponseDto>> openSavingsAccount(
            String customerId,
            Mono<SavingsAccountRequestDto> savingsAccountRequestDto,
            ServerWebExchange exchange) {
        return savingsAccountRequestDto.flatMap(
                request ->
                        created(
                                createBankAccountUseCase.openSavingsAccount(
                                        customerId,
                                        request.getMaxMonthlyMovements(),
                                        request.getOpeningAmount())));
    }

    @Override
    public Mono<ResponseEntity<AccountResponseDto>> openCheckingAccount(
            String customerId,
            Mono<CheckingAccountRequestDto> checkingAccountRequestDto,
            ServerWebExchange exchange) {
        return checkingAccountRequestDto.flatMap(
                request ->
                        created(
                                createBankAccountUseCase.openCheckingAccount(
                                        customerId,
                                        request.getMaintenanceFee(),
                                        request.getHolderIds(),
                                        request.getAuthorizedSignerIds(),
                                        request.getOpeningAmount())));
    }

    @Override
    public Mono<ResponseEntity<AccountResponseDto>> openFixedTermAccount(
            String customerId,
            Mono<FixedTermAccountRequestDto> fixedTermAccountRequestDto,
            ServerWebExchange exchange) {
        return fixedTermAccountRequestDto.flatMap(
                request ->
                        created(
                                createBankAccountUseCase.openFixedTermAccount(
                                        customerId,
                                        request.getMovementDayOfMonth(),
                                        request.getOpeningAmount())));
    }

    @Override
    public Mono<ResponseEntity<AccountResponseDto>> updateAccount(
            String accountId,
            String adminId,
            Mono<AccountUpdateRequestDto> accountUpdateRequestDto,
            ServerWebExchange exchange) {
        return accountUpdateRequestDto
                .flatMap(request -> Mono.from(updateBankAccountUseCase
                        .update(accountId, new AccountChanges(
                                request.getMaintenanceFee(),
                                request.getHolderIds(),
                                request.getAuthorizedSignerIds(),
                                request.getMaxMonthlyMovements(),
                                request.getMinimumDailyAverage(),
                                request.getMovementDayOfMonth()))
                        .toFlowable()))
                .doOnNext(account -> log.info(
                        "Account updated accountId={} adminId={}", accountId, adminId))
                .map(account -> ResponseEntity.ok(AccountWebMapper.toResponse(account)));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteAccount(
            String accountId,
            String adminId,
            ServerWebExchange exchange) {
        return Mono.from(updateBankAccountUseCase.close(accountId).toFlowable())
                .then(Mono.fromCallable(() -> {
                    log.info("Account closed accountId={} adminId={}", accountId, adminId);
                    return ResponseEntity.noContent().<Void>build();
                }));
    }

    private static Mono<ResponseEntity<AccountResponseDto>> created(
            Single<? extends BankAccount> account) {
        return Mono.from(account.toFlowable())
                .doOnNext(
                        saved ->
                                log.info(
                                        "Account opened accountId={} customerId={} type={}",
                                        saved.getId(),
                                        saved.getCustomer().id(),
                                        saved.getClass().getSimpleName()))
                .map(
                        saved ->
                                ResponseEntity.status(HttpStatus.CREATED)
                                        .body(AccountWebMapper.toResponse(saved)));
    }
}
