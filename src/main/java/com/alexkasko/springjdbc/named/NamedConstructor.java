package com.alexkasko.springjdbc.named;

import java.lang.reflect.Constructor;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Contains constructor and list of arguments names for it.
 * Object can be instantiated from unordered data map using arguments names.
 * Input map keys may be in any case.
 * Object instantiated through constructor invocation without any field access so
 * it can be immutable with all fields defined as {@code final}
 *
 * @author alexkasko
 * Date: 7/5/12
 * @see NamedConstructorMapper
 * @see NamedConstructorList
 */
class NamedConstructor<T> {
    /**
     * object constructor to use
     */
    private final Constructor<T> constructor;
    /**
     * list of argument names for constructor
     */
    final LinkedHashSet<String> names;

    /**
     * @param constructor object constructor to use
     * @param names list of argument names for constructor
     */
    @SuppressWarnings("unchecked")
    NamedConstructor(Constructor<?> constructor, LinkedHashSet<String> names) {
        this.constructor = (Constructor<T>) constructor;
        if(!this.constructor.isAccessible()) constructor.setAccessible(true);
        this.names = names;
    }

    /**
     * Instantiates object from unordered data map. Orders map values based on argument names order.
     * Map keys may be in any case.
     *
     * @param input unordered data map
     * @return instantiated object
     */
    T invoke(Map<String, ?> input) {
        try {
            Object[] args = new Object[names.size()];
            int ind = 0;
            for(String na : names) {
                args[ind] = input.get(na);
                ind += 1;
            }
            return constructor.newInstance(args);
        } catch(Exception e) {
            throw new RuntimeException(
                    "Object instantiation error, named constructor: '" + this + "', arguments: '" + input + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("NamedConstructor");
        sb.append("{constructor=").append(constructor);
        sb.append(", names=").append(names);
        sb.append('}');
        return sb.toString();
    }
}
