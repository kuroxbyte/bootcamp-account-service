package dev.bootcamp.accountservice.infrastructure.persistence;

import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.FixedTermAccount;
import dev.bootcamp.accountservice.domain.model.account.OpeningAmount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.domain.model.account.TransactionFee;
import dev.bootcamp.accountservice.domain.model.account.VipSavingsAccount;
import dev.bootcamp.accountservice.domain.model.customer.Customer;
import dev.bootcamp.accountservice.domain.model.customer.CustomerProfile;
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
                        .customerType(account.getCustomer().type().name())
                        .customerProfile(account.getCustomer().profile().name())
                        .minimumOpeningAmount(account.getOpening().minimum())
                        .openingAmount(account.getOpening().amount())
                        .openedAt(account.getOpenedAt());
        if (account.getTransactionFee() != null) {
            document.freeMonthlyTransactions(account.getTransactionFee().freeMonthlyTransactions())
                    .transactionFee(account.getTransactionFee().amount());
        }
        if (account instanceof VipSavingsAccount vipSavings) {
            return document.type(SAVINGS)
                    .maxMonthlyMovements(vipSavings.getMaxMonthlyMovements())
                    .minimumDailyAverage(vipSavings.getMinimumDailyAverage())
                    .build();
        }
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
        Customer customer = new Customer(
                document.getCustomerId(),
                CustomerType.valueOf(document.getCustomerType()),
                CustomerProfile.valueOf(document.getCustomerProfile()));
        OpeningAmount opening = new OpeningAmount(
                document.getMinimumOpeningAmount(),
                document.getOpeningAmount());
        BankAccount account = switch (document.getType()) {
            case SAVINGS -> document.getMinimumDailyAverage() == null
                    ? new SavingsAccount(customer, opening, document.getMaxMonthlyMovements())
                    : new VipSavingsAccount(
                            customer,
                            opening,
                            document.getMaxMonthlyMovements(),
                            document.getMinimumDailyAverage());
            case CHECKING -> new CheckingAccount(
                    customer,
                    opening,
                    document.getMaintenanceFee(),
                    document.getHolderIds(),
                    document.getAuthorizedSignerIds());
            case FIXED_TERM -> new FixedTermAccount(
                    customer,
                    opening,
                    document.getMovementDayOfMonth());
            default -> throw new IllegalStateException(
                    "Tipo de cuenta desconocido: " + document.getType());
        };
        account.setId(document.getId());
        account.setOpenedAt(document.getOpenedAt());
        if (document.getTransactionFee() != null) {
            account.setTransactionFee(new TransactionFee(
                    document.getFreeMonthlyTransactions(),
                    document.getTransactionFee()));
        }
        return account;
    }
}
