package dev.bootcamp.accountservice.domain.model.account;

import dev.bootcamp.accountservice.domain.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Nuevos valores de las condiciones modificables de una cuenta ({@code PUT}). Cada tipo de cuenta
 * usa solo los suyos y deben venir todos los obligatorios de su tipo; enviar el de otro tipo es un
 * error (una lista vacía cuenta como no enviada).
 *
 * @param maintenanceFee      comisión de mantenimiento (corriente)
 * @param holderIds           titulares (corriente)
 * @param authorizedSignerIds firmantes autorizados (corriente)
 * @param maxMonthlyMovements límite de movimientos mensuales (ahorro)
 * @param minimumDailyAverage mínimo de promedio diario mensual (ahorro VIP)
 * @param movementDayOfMonth  día del único movimiento del mes (plazo fijo)
 */
public record AccountChanges(
        BigDecimal maintenanceFee,
        List<String> holderIds,
        List<String> authorizedSignerIds,
        Integer maxMonthlyMovements,
        BigDecimal minimumDailyAverage,
        Integer movementDayOfMonth) {

    /**
     * Verifica que solo vengan campos del tipo de cuenta.
     *
     * @param accountType nombre del tipo de cuenta, para el mensaje
     * @param allowed     campos que admite el tipo
     * @throws BusinessRuleException si viene un campo de otro tipo de cuenta
     */
    void allowOnly(String accountType, Set<String> allowed) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("maintenanceFee", maintenanceFee);
        fields.put("holderIds", holderIds);
        fields.put("authorizedSignerIds", authorizedSignerIds);
        fields.put("maxMonthlyMovements", maxMonthlyMovements);
        fields.put("minimumDailyAverage", minimumDailyAverage);
        fields.put("movementDayOfMonth", movementDayOfMonth);
        fields.entrySet().stream()
                .filter(field -> isPresent(field.getValue())
                        && !allowed.contains(field.getKey()))
                .findFirst()
                .ifPresent(field -> {
                    throw new BusinessRuleException(
                            "El campo " + field.getKey() + " no aplica a una cuenta "
                                    + accountType);
                });
    }

    // Una lista vacía cuenta como no enviada: el contrato OpenAPI la usa como valor por defecto.
    private static boolean isPresent(Object value) {
        return value instanceof List<?> list ? !list.isEmpty() : value != null;
    }

    /**
     * Valor obligatorio de un campo entero.
     *
     * @throws BusinessRuleException si no viene
     */
    static int required(Integer value, String field) {
        if (value == null) {
            throw new BusinessRuleException("El campo " + field + " es obligatorio");
        }
        return value;
    }
}
