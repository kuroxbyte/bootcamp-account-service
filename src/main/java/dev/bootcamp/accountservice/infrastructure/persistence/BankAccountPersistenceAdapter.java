package dev.bootcamp.accountservice.infrastructure.persistence;

import dev.bootcamp.accountservice.application.port.BankAccountRepository;
import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Implementa el puerto {@link BankAccountRepository} con MongoDB reactivo. */
@Slf4j
@Component
@RequiredArgsConstructor
public class BankAccountPersistenceAdapter implements BankAccountRepository {

    private final BankAccountMongoRepository mongoRepository;

    @Override
    public Maybe<BankAccount> findById(String accountId) {
        return Maybe.fromPublisher(mongoRepository.findById(accountId))
                .map(BankAccountPersistenceMapper::toDomain);
    }

    @Override
    public Flowable<BankAccount> findByCustomerId(String customerId) {
        return Flowable.fromPublisher(mongoRepository.findByCustomerId(customerId))
                .map(BankAccountPersistenceMapper::toDomain);
    }

    @Override
    public <T extends BankAccount> Single<T> save(T account) {
        return Single.fromPublisher(
                        mongoRepository.save(BankAccountPersistenceMapper.toDocument(account)))
                .map(
                        saved -> {
                            account.setId(saved.getId());
                            log.debug("Account saved accountId={}", saved.getId());
                            return account;
                        });
    }
}
