package com.alexkasko.springjdbc.named;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

import static com.alexkasko.springjdbc.named.LowerColumnMapRowMapper.LOWER_MAPPER;
import static org.springframework.util.StringUtils.hasText;

/**
 * Named constructor implementation for class hierarchy mapping. Converts result set row into case-insensitive map,
 * chooses function by discriminator column value and applies it.
 *
 * @author alexkasko
 * Date: 7/6/12
 * @see NamedConstructorMapper
 * @see NamedConstructorList
 * @see NamedConstructor
 */
class NamedConstructorSubclassesMapper<T> extends NamedConstructorMapper<T> {
    private final String discColumn;
    private final Map<String, NamedConstructorList<? extends T>> ncMap;

    /**
     * Constructor
     *
     * @param ncMap discriminator value -> named constructor function mapping
     * @param discColumn discriminator column
     */
    NamedConstructorSubclassesMapper(Map<String, NamedConstructorList<? extends T>> ncMap, String discColumn) {
        if(0 == ncMap.size()) throw new IllegalArgumentException("Provided functions map is empty");
        if(!hasText(discColumn)) throw new IllegalArgumentException("Provided discriminator column is blank");
        this.ncMap = ncMap;
        this.discColumn = discColumn.toLowerCase(Locale.ENGLISH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, ?> map = LOWER_MAPPER.mapRow(rs, rowNum);
        String discVal = (String) map.get(discColumn);
        if(null == discVal) throw new IllegalArgumentException("Null or absent value of disc column: '" + discColumn + "' " +
                "in row data: '" + map + "'");
        NamedConstructorList<? extends T> nc = ncMap.get(discVal);
        if(null == nc) throw new IllegalArgumentException(
                "Cannot find subclass for discriminator: '" + discVal + "', keys: '" + ncMap.keySet() + "', row data: '" + map + "'");
        return nc.invoke(map);
    }
}
