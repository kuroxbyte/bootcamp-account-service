package dev.bootcamp.accountservice.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

/**
 * Tarjeta de débito tal como se guarda en la colección {@code debit_cards}. El índice único sobre
 * {@code accountIds} impide asociar una misma cuenta a dos tarjetas aunque lleguen dos pedidos a la
 * vez.
 */
@Getter
@AllArgsConstructor
@Document(collection = "debit_cards")
public class DebitCardDocument {
    @Id private final String id;
    @Indexed private final String customerId;
    private final String primaryAccountId;

    /**
     * Cuentas asociadas, empezando por la principal.
     */
    @Indexed(unique = true) private final List<String> accountIds;

    private final LocalDate issuedAt;
}
