package dev.bootcamp.accountservice.domain.model.debitcard;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Tarjeta de débito de un cliente, asociada a una o más de sus cuentas bancarias. Los pagos con la
 * tarjeta salen de la cuenta principal (se procesan en transaction-service).
 */
@Getter
public class DebitCard {
    @Setter private String id;
    private final String customerId;
    private final List<String> accountIds;
    private String primaryAccountId;

    /**
     * Fecha de emisión.
     */
    @Setter private LocalDate issuedAt;

    /**
     * Fecha de baja; null mientras la tarjeta está activa.
     */
    @Setter private LocalDate closedAt;

    /**
     * Crea la tarjeta con su cuenta principal y otras cuentas asociadas.
     *
     * @param customerId       cliente dueño
     * @param primaryAccountId cuenta de la que salen los pagos
     * @param otherAccountIds  otras cuentas asociadas (puede estar vacío o ser null)
     * @throws BusinessRuleException si falta la cuenta principal o se repite una cuenta
     */
    public DebitCard(String customerId, String primaryAccountId, List<String> otherAccountIds) {
        if (primaryAccountId == null || primaryAccountId.isBlank()) {
            throw new BusinessRuleException("La tarjeta de débito debe tener una cuenta principal");
        }
        List<String> all = new ArrayList<>();
        all.add(primaryAccountId);
        if (otherAccountIds != null) {
            all.addAll(otherAccountIds);
        }
        Set<String> unique = new LinkedHashSet<>(all);
        if (unique.size() != all.size()) {
            throw new BusinessRuleException("Una cuenta no puede repetirse en la tarjeta");
        }
        this.customerId = customerId;
        this.primaryAccountId = primaryAccountId;
        this.accountIds = new ArrayList<>(unique);
    }

    /**
     * Reconstruye una tarjeta guardada.
     */
    public static DebitCard restore(
            String id,
            String customerId,
            String primaryAccountId,
            List<String> accountIds,
            LocalDate issuedAt) {
        List<String> others = new ArrayList<>(accountIds);
        others.remove(primaryAccountId);
        DebitCard card = new DebitCard(customerId, primaryAccountId, others);
        card.setId(id);
        card.setIssuedAt(issuedAt);
        return card;
    }

    /**
     * Cuentas asociadas, empezando por la principal y luego en el orden en que se asociaron.
     */
    public List<String> getAccountIds() {
        return List.copyOf(accountIds);
    }

    /**
     * Indica si la tarjeta fue dada de baja.
     */
    public boolean isClosed() {
        return closedAt != null;
    }

    /**
     * Da de baja la tarjeta: sus cuentas quedan libres para asociarse a otra tarjeta.
     */
    public void close(LocalDate today) {
        this.closedAt = today;
    }

    /**
     * Indica si la tarjeta pertenece al cliente.
     */
    public boolean belongsTo(String customerId) {
        return this.customerId.equals(customerId);
    }

    /**
     * Asocia otra cuenta a la tarjeta.
     *
     * @throws BusinessRuleException si la cuenta ya está asociada a la tarjeta
     */
    public void associateAccount(String accountId) {
        if (accountIds.contains(accountId)) {
            throw new BusinessRuleException("La cuenta ya está asociada a la tarjeta");
        }
        accountIds.add(accountId);
    }

    /**
     * Cambia la cuenta principal por otra cuenta ya asociada.
     *
     * @throws BusinessRuleException si la cuenta no está asociada a la tarjeta
     */
    public void changePrimaryAccount(String accountId) {
        if (!accountIds.contains(accountId)) {
            throw new BusinessRuleException(
                    "La cuenta principal debe estar asociada a la tarjeta");
        }
        primaryAccountId = accountId;
    }
}
