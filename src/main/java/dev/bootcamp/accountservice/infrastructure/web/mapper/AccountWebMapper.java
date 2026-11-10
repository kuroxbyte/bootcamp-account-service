package dev.bootcamp.accountservice.infrastructure.web.mapper;

import dev.bootcamp.accountservice.domain.model.account.BankAccount;
import dev.bootcamp.accountservice.domain.model.account.CheckingAccount;
import dev.bootcamp.accountservice.domain.model.account.FixedTermAccount;
import dev.bootcamp.accountservice.domain.model.account.SavingsAccount;
import dev.bootcamp.accountservice.infrastructure.web.dto.AccountResponseDto;
import dev.bootcamp.accountservice.infrastructure.web.dto.AccountTypeDto;

/**
 * Conversión entre el dominio y los DTOs del contrato OpenAPI.
 */
public final class AccountWebMapper {

    private AccountWebMapper() {}

    /**
     * Convierte una cuenta en su respuesta; solo se llenan los campos propios de su tipo.
     */
    public static AccountResponseDto toResponse(BankAccount account) {
        AccountResponseDto response =
                new AccountResponseDto()
                        .id(account.getId())
                        .customerId(account.getCustomer().id())
                        .minimumOpeningAmount(account.getOpening().minimum())
                        .openingAmount(account.getOpening().amount())
                        .holderIds(null)
                        .authorizedSignerIds(null);
        if (account instanceof SavingsAccount savings) {
            return response.type(AccountTypeDto.SAVINGS)
                    .maxMonthlyMovements(savings.getMaxMonthlyMovements());
        }
        if (account instanceof CheckingAccount checking) {
            return response.type(AccountTypeDto.CHECKING)
                    .maintenanceFee(checking.getMaintenanceFee())
                    .holderIds(checking.getHolderIds())
                    .authorizedSignerIds(checking.getAuthorizedSignerIds());
        }
        if (account instanceof FixedTermAccount fixedTerm) {
            return response.type(AccountTypeDto.FIXED_TERM)
                    .movementDayOfMonth(fixedTerm.getMovementDayOfMonth());
        }
        throw new IllegalArgumentException(
                "Tipo de cuenta no soportado: " + account.getClass().getSimpleName());
    }
}
