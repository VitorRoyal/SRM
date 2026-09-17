package com.srm.creditengine.core.settlement.repository;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;
import com.srm.creditengine.core.settlement.dto.SettlementResponseDTO;
import com.srm.creditengine.core.settlement.dto.SettlementStatementFilter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Repository
public class SettlementStatementRepository {

    private static final String FROM_CLAUSE = """
            FROM settlement s
            JOIN receivable r ON r.id = s.receivable_id
            JOIN assignor a ON a.id = r.assignor_id
            """;

    private static final String SELECT_COLUMNS = """
            SELECT s.id, a.id AS assignor_id, a.name AS assignor_name, r.document_number, r.type,
                   r.face_value, r.due_date, s.term_in_months, s.monthly_base_rate, s.monthly_spread,
                   s.present_value_brl, s.discount_brl, s.payment_currency, s.payment_amount,
                   s.exchange_rate_brl_per_unit, s.settled_at
            """;

    private static final String ORDER_AND_PAGE_CLAUSE = " ORDER BY s.settled_at DESC, s.id DESC LIMIT :limit OFFSET :offset";

    private static final RowMapper<SettlementResponseDTO> SETTLEMENT_ROW_MAPPER = (resultSet, rowNumber) ->
            new SettlementResponseDTO(
                    resultSet.getLong("id"),
                    resultSet.getLong("assignor_id"),
                    resultSet.getString("assignor_name"),
                    resultSet.getString("document_number"),
                    ReceivableType.valueOf(resultSet.getString("type")),
                    resultSet.getBigDecimal("face_value"),
                    resultSet.getObject("due_date", LocalDate.class),
                    resultSet.getInt("term_in_months"),
                    resultSet.getBigDecimal("monthly_base_rate"),
                    resultSet.getBigDecimal("monthly_spread"),
                    resultSet.getBigDecimal("present_value_brl"),
                    resultSet.getBigDecimal("discount_brl"),
                    CurrencyCode.valueOf(resultSet.getString("payment_currency")),
                    resultSet.getBigDecimal("payment_amount"),
                    resultSet.getBigDecimal("exchange_rate_brl_per_unit"),
                    resultSet.getTimestamp("settled_at").toInstant()
            );

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SettlementStatementRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<SettlementResponseDTO> findPage(SettlementStatementFilter settlementStatementFilter) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        String whereClause = buildWhereClause(settlementStatementFilter, parameters);
        parameters.addValue("limit", settlementStatementFilter.size());
        parameters.addValue("offset", (long) settlementStatementFilter.page() * settlementStatementFilter.size());

        String sql = SELECT_COLUMNS + FROM_CLAUSE + whereClause + ORDER_AND_PAGE_CLAUSE;
        return namedParameterJdbcTemplate.query(sql, parameters, SETTLEMENT_ROW_MAPPER);
    }

    public long count(SettlementStatementFilter settlementStatementFilter) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        String whereClause = buildWhereClause(settlementStatementFilter, parameters);
        Long total = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + FROM_CLAUSE + whereClause,
                parameters,
                Long.class
        );
        if (total == null) {
            return 0;
        }
        return total;
    }

    private String buildWhereClause(SettlementStatementFilter settlementStatementFilter, MapSqlParameterSource parameters) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1 = 1");
        if (settlementStatementFilter.settledFrom() != null) {
            whereClause.append(" AND s.settled_at >= :settledFrom");
            parameters.addValue("settledFrom", Timestamp.from(settlementStatementFilter.settledFrom()));
        }
        if (settlementStatementFilter.settledUntil() != null) {
            whereClause.append(" AND s.settled_at < :settledUntil");
            parameters.addValue("settledUntil", Timestamp.from(settlementStatementFilter.settledUntil()));
        }
        if (settlementStatementFilter.assignorId() != null) {
            whereClause.append(" AND r.assignor_id = :assignorId");
            parameters.addValue("assignorId", settlementStatementFilter.assignorId());
        }
        if (settlementStatementFilter.paymentCurrency() != null) {
            whereClause.append(" AND s.payment_currency = :paymentCurrency");
            parameters.addValue("paymentCurrency", settlementStatementFilter.paymentCurrency().name());
        }
        return whereClause.toString();
    }
}
