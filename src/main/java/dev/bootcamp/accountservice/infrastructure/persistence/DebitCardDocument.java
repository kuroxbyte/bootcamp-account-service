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
 * {@code accountIds} impide asociar una misma cuenta a dos tarjetas activas aunque lleguen dos
 * pedidos a la vez.
 *
 * <p>Una tarjeta dada de baja guarda sus cuentas en {@code closedAccountIds} y no tiene
 * {@code accountIds}: el índice es disperso ({@code sparse}), así sus cuentas quedan libres.
 */
@Getter
@AllArgsConstructor
@Document(collection = "debit_cards")
public class DebitCardDocument {
    @Id private final String id;
    @Indexed private final String customerId;
    private final String primaryAccountId;

    /**
     * Cuentas asociadas, empezando por la principal; null si la tarjeta fue dada de baja.
     */
    @Indexed(unique = true, sparse = true) private final List<String> accountIds;

    private final LocalDate issuedAt;

    /**
     * Fecha de baja; null mientras la tarjeta está activa.
     */
    private final LocalDate closedAt;

    /**
     * Cuentas que tenía la tarjeta al darse de baja.
     */
    private final List<String> closedAccountIds;
}
