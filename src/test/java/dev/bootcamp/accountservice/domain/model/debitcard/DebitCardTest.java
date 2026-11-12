package dev.bootcamp.accountservice.domain.model.debitcard;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebitCardTest {

    @Test
    void listsThePrimaryAccountFirst() {
        DebitCard card = new DebitCard("c1", "a1", List.of("a2", "a3"));

        assertThat(card.getPrimaryAccountId()).isEqualTo("a1");
        assertThat(card.getAccountIds()).containsExactly("a1", "a2", "a3");
        assertThat(card.belongsTo("c1")).isTrue();
        assertThat(card.belongsTo("c2")).isFalse();
    }

    @Test
    void requiresPrimaryAccountAndRejectsRepeatedAccounts() {
        assertThatThrownBy(() -> new DebitCard("c1", null, List.of()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("La tarjeta de débito debe tener una cuenta principal");
        assertThatThrownBy(() -> new DebitCard("c1", "a1", List.of("a2", "a1")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Una cuenta no puede repetirse en la tarjeta");
    }

    @Test
    void associatesNewAccountsOnlyOnce() {
        DebitCard card = new DebitCard("c1", "a1", null);

        card.associateAccount("a2");

        assertThat(card.getAccountIds()).containsExactly("a1", "a2");
        assertThatThrownBy(() -> card.associateAccount("a2"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("La cuenta ya está asociada a la tarjeta");
    }

    @Test
    void changesPrimaryOnlyToAnAssociatedAccount() {
        DebitCard card = new DebitCard("c1", "a1", List.of("a2"));

        card.changePrimaryAccount("a2");

        assertThat(card.getPrimaryAccountId()).isEqualTo("a2");
        assertThatThrownBy(() -> card.changePrimaryAccount("a9"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("La cuenta principal debe estar asociada a la tarjeta");
    }

    @Test
    void restoresSavedCardKeepingItsAccounts() {
        DebitCard card = DebitCard.restore(
                "d1",
                "c1",
                "a2",
                List.of("a1", "a2"),
                LocalDate.of(2026, 11, 15));

        assertThat(card.getId()).isEqualTo("d1");
        assertThat(card.getPrimaryAccountId()).isEqualTo("a2");
        assertThat(card.getAccountIds()).containsExactlyInAnyOrder("a1", "a2");
        assertThat(card.getIssuedAt()).isEqualTo(LocalDate.of(2026, 11, 15));
    }
}
