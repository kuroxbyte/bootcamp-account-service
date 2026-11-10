package dev.bootcamp.accountservice.infrastructure.persistence;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Cuenta bancaria tal como se guarda en la colección {@code accounts}. Una sola colección para los
 * tres tipos; {@code type} indica cuál es y los campos propios de los demás tipos quedan vacíos.
 */
@Getter
@Builder
@Document(collection = "accounts")
public class BankAccountDocument {
    @Id private final String id;
    @Indexed private final String customerId;

    /**
     * Tipo del cliente; se guarda para reconstruir las reglas de la cuenta sin consultar
     * customer-service.
     */
    private final String customerType;

    /**
     * Perfil del cliente (STANDARD, VIP o PYME).
     */
    private final String customerProfile;

    /**
     * SAVINGS, CHECKING o FIXED_TERM.
     */
    private final String type;

    private final Integer maxMonthlyMovements;

    /**
     * Solo en la cuenta de ahorro VIP.
     */
    @Field(targetType = FieldType.DECIMAL128)
    private final BigDecimal minimumDailyAverage;

    @Field(targetType = FieldType.DECIMAL128)
    private final BigDecimal maintenanceFee;

    private final List<String> holderIds;
    private final List<String> authorizedSignerIds;
    private final Integer movementDayOfMonth;

    @Field(targetType = FieldType.DECIMAL128)
    private final BigDecimal minimumOpeningAmount;

    @Field(targetType = FieldType.DECIMAL128)
    private final BigDecimal openingAmount;

    private final LocalDate openedAt;
}
