package dev.bootcamp.accountservice.infrastructure.persistence;

import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.FixedTermAccount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerType;

/**
 * Conversión entre el dominio y el documento de MongoDB.
 */
final class BankAccountPersistenceMapper {
    static final String SAVINGS = "SAVINGS";
    static final String CHECKING = "CHECKING";
    static final String FIXED_TERM = "FIXED_TERM";

    private BankAccountPersistenceMapper() {}

    static BankAccountDocument toDocument(BankAccount account) {
        BankAccountDocument.BankAccountDocumentBuilder document =
                BankAccountDocument.builder()
                        .id(account.getId())
                        .customerId(account.getCustomer().id())
                        .customerType(account.getCustomer().type().name());
        if (account instanceof SavingsAccount savings) {
            return document.type(SAVINGS)
                    .maxMonthlyMovements(savings.getMaxMonthlyMovements())
                    .build();
        }
        if (account instanceof CheckingAccount checking) {
            return document.type(CHECKING)
                    .maintenanceFee(checking.getMaintenanceFee())
                    .holderIds(checking.getHolderIds())
                    .authorizedSignerIds(checking.getAuthorizedSignerIds())
                    .build();
        }
        if (account instanceof FixedTermAccount fixedTerm) {
            return document.type(FIXED_TERM)
                    .movementDayOfMonth(fixedTerm.getMovementDayOfMonth())
                    .build();
        }
        throw new IllegalArgumentException(
                "Tipo de cuenta no soportado: " + account.getClass().getSimpleName());
    }

    static BankAccount toDomain(BankAccountDocument document) {
        Customer customer =
                new Customer(
                        document.getCustomerId(), CustomerType.valueOf(document.getCustomerType()));
        BankAccount account =
                switch (document.getType()) {
                    case SAVINGS -> new SavingsAccount(customer, document.getMaxMonthlyMovements());
                    case CHECKING ->
                            new CheckingAccount(
                                    customer,
                                    document.getMaintenanceFee(),
                                    document.getHolderIds(),
                                    document.getAuthorizedSignerIds());
                    case FIXED_TERM ->
                            new FixedTermAccount(customer, document.getMovementDayOfMonth());
                    default ->
                            throw new IllegalStateException(
                                    "Tipo de cuenta desconocido: " + document.getType());
                };
        account.setId(document.getId());
        return account;
    }
}
