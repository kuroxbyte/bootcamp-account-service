package dev.bootcamp.accountservice.application.usecase;

import dev.bootcamp.accountservice.application.port.DebitCardEventPublisher;
import dev.bootcamp.accountservice.application.port.DebitCardRepository;
import dev.bootcamp.accountservice.domain.exception.NotFoundException;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Implementación de {@link CloseDebitCardUseCase}.
 */
@RequiredArgsConstructor
public class CloseDebitCardUseCaseImpl implements CloseDebitCardUseCase {
    private final DebitCardRepository debitCardRepository;
    private final DebitCardEventPublisher debitCardEventPublisher;
    private final Clock clock;

    @Override
    public Completable execute(String debitCardId) {
        return debitCardRepository.findById(debitCardId)
                .filter(card -> !card.isClosed())
                .switchIfEmpty(Single.error(() -> new NotFoundException(
                        "La tarjeta de débito no existe")))
                .flatMap(card -> {
                    card.close(LocalDate.now(clock));
                    return debitCardRepository.save(card);
                })
                .flatMapCompletable(debitCardEventPublisher::publishClosed);
    }
}
