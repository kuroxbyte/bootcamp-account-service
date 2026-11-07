package dev.bootcamp.accountservice.infrastructure.web.controller;

import dev.bootcamp.accountservice.application.usecase.CreateBankAccountUseCase;
import dev.bootcamp.accountservice.application.usecase.GetBankAccountsUseCase;
import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import dev.bootcamp.accountservice.infrastructure.web.api.AccountsApi;
import dev.bootcamp.accountservice.infrastructure.web.dto.AccountResponseDto;
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

@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountController implements AccountsApi {

    private final CreateBankAccountUseCase createBankAccountUseCase;
    private final GetBankAccountsUseCase getBankAccountsUseCase;

    @Override
    public Mono<ResponseEntity<Flux<AccountResponseDto>>> listAccounts(String xCustomerId, ServerWebExchange exchange) {
        log.debug("Listing accounts customerId={}", xCustomerId);
        Flux<AccountResponseDto> accounts = Flux.from(getBankAccountsUseCase.findByCustomerId(xCustomerId))
                .map(AccountWebMapper::toResponse);
        return Mono.just(ResponseEntity.ok(accounts));
    }

    @Override
    public Mono<ResponseEntity<AccountResponseDto>> getAccountById(String accountId, ServerWebExchange exchange) {
        log.debug("Fetching account accountId={}", accountId);
        return Mono.from(getBankAccountsUseCase.findById(accountId).toFlowable())
                .map(account -> ResponseEntity.ok(AccountWebMapper.toResponse(account)));
    }

    @Override
    public Mono<ResponseEntity<AccountResponseDto>> openSavingsAccount(String xCustomerId,
                                                                       Mono<SavingsAccountRequestDto> savingsAccountRequestDto,
                                                                       ServerWebExchange exchange) {
        return savingsAccountRequestDto
                .flatMap(request -> created(createBankAccountUseCase
                        .openSavingsAccount(xCustomerId, request.getMaxMonthlyMovements())));
    }

    @Override
    public Mono<ResponseEntity<AccountResponseDto>> openCheckingAccount(String xCustomerId,
                                                                        Mono<CheckingAccountRequestDto> checkingAccountRequestDto,
                                                                        ServerWebExchange exchange) {
        return checkingAccountRequestDto
                .flatMap(request -> created(createBankAccountUseCase.openCheckingAccount(
                        xCustomerId, request.getMaintenanceFee(),
                        request.getHolderIds(), request.getAuthorizedSignerIds())));
    }

    @Override
    public Mono<ResponseEntity<AccountResponseDto>> openFixedTermAccount(String xCustomerId,
                                                                         Mono<FixedTermAccountRequestDto> fixedTermAccountRequestDto,
                                                                         ServerWebExchange exchange) {
        return fixedTermAccountRequestDto
                .flatMap(request -> created(createBankAccountUseCase
                        .openFixedTermAccount(xCustomerId, request.getMovementDayOfMonth())));
    }

    private static Mono<ResponseEntity<AccountResponseDto>> created(Single<? extends BankAccount> account) {
        return Mono.from(account.toFlowable())
                .doOnNext(saved -> log.info("Account opened accountId={} customerId={} type={}",
                        saved.getId(), saved.getCustomer().id(), saved.getClass().getSimpleName()))
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(AccountWebMapper.toResponse(saved)));
    }
}
