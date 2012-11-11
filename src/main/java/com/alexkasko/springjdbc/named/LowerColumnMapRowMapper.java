package com.alexkasko.springjdbc.named;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * {@code ColumnMapRowMapper} implementation using HashMap with lower
 * keys instead of {@code LinkedCaseInsensitiveMap}
 *
 * @author alexkasko
 * Date: 11/11/12
 */
class LowerColumnMapRowMapper extends ColumnMapRowMapper {
    static final RowMapper<Map<String, Object>> LOWER_MAPPER = new LowerColumnMapRowMapper();

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> createColumnMap(int columnCount) {
        return new HashMap<String, Object>(columnCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getColumnKey(String columnName) {
        return columnName.toLowerCase(Locale.ENGLISH);
    }
}
