package com.alexkasko.springjdbc.named;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static com.alexkasko.springjdbc.named.LowerColumnMapRowMapper.LOWER_MAPPER;

/**
 * Named constructor mapper implementation for single class.
 * Converts result set row into case-insensitive map and applies {@link NamedConstructorList} to it.
 *
 * @author alexkasko
 * Date: 7/6/12
 * @see NamedConstructorMapper
 * @see NamedConstructorList
 * @see NamedConstructor
 */
class NamedConstructorSingleMapper<T> extends NamedConstructorMapper<T> {
    private final NamedConstructorList<T> list;

    /**
     * Constructor
     *
     * @param list list of constructors
     */
    NamedConstructorSingleMapper(NamedConstructorList<T> list) {
        this.list = list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, ?> map = LOWER_MAPPER.mapRow(rs, rowNum);
        return list.invoke(map);
    }
}
