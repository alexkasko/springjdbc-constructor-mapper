package com.alexkasko.springjdbc.named;

import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;

import static org.springframework.util.StringUtils.hasText;

/**
 * List of named constructors for single class, converts unordered data map into object instance using {@link NamedConstructor}.
 * Designed to use with immutable classes (with final fields) - only constructor invocation is used without any field access.
 * Constructor arguments must be annotated with JSR330 {@link Named} annotations (there are other ways to access
 * constructor names in runtime, see <a href="http://paranamer.codehaus.org/">paranamer project</a>, but we use
 * {@code @Named} annotations only). Constructors without {@code @Named} annotations on arguments will be ignored.
 * Constructors with {@code @Named} annotations must have all they arguments annotated with not blank values without
 * duplicates. All reflection introspection is done on function instantiation. Input map keys may be in any case.
 *
 * @param <T> object type to instantiate
 * @author alexkasko
 * Date: 7/6/12
 * @see NamedConstructor
 * @see NamedConstructorMapper
 */
class NamedConstructorList<T> {
    /**
     * ordered by arguments count descending
     */
    private final List<NamedConstructor<T>> constructors;

    /**
     * Constructor
     *
     * @param clazz class to introspect and instantiate
     */
    NamedConstructorList(Class<T> clazz) {
        if(null == clazz) throw new IllegalArgumentException("Provided class is null");
        this.constructors = extractNamedConstructors(clazz);
    }

    /**
     * Generic-friendly factory method
     *
     * @param clazz class to introspect and instantiate
     * @param <T> class type
     * @return named constructor function instance
     */
    static <T> NamedConstructorList<T> forClass(Class<T> clazz) {
        return new NamedConstructorList<T>(clazz);
    }

    /**
     * Converts unordered data map into object instance using constructor {@link Named} annotated arguments.
     *
     * @param input unordered data map
     * @return instantiated object
     */
    public T invoke(Map<String, ?> input) {
        if(null == input) throw new IllegalArgumentException("Input data map is null");
        NamedConstructor<T> nc = findConstructor(input.keySet());
        return nc.invoke(input);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("NamedConstructorList");
        sb.append("{constructors=").append(constructors);
        sb.append('}');
        return sb.toString();
    }

    private NamedConstructor<T> findConstructor(Set<String> names) {
        for(NamedConstructor<T> nc : constructors) {
            if(names.size() >= nc.names.size() && names.containsAll(nc.names)) return nc;
        }
        throw new IllegalAccessError("No named constructor found for input: '" + names + "', existed constructors: '" + constructors + "'");
    }

    private List<NamedConstructor<T>> extractNamedConstructors(Class<T> clazz) {
        List<NamedConstructor<T>> list = new ArrayList<NamedConstructor<T>>();
        for(Constructor<?> co : clazz.getDeclaredConstructors()) {
            Annotation[][] anArray = co.getParameterAnnotations();
            LinkedHashSet<String> names = extractNames(anArray, co.toGenericString());
            if(names.size() > 0) list.add(new NamedConstructor<T>(co, names));
        }
        if(0 == list.size()) throw new IllegalArgumentException("No named constructors found for class: '" + clazz + "'");
        // check duplicate name sets
        for(NamedConstructor<T> nc1 : list) {
            for (NamedConstructor<T> nc2 : list) {
                if(nc1.names != nc2.names && nc1.names.containsAll(nc2.names) && nc2.names.containsAll(nc1.names)) {
                    throw new IllegalArgumentException("Named constructors with duplicate names set found, " +
                                        "first: '" + nc1 + "', second: '" + nc2 + "'");
                }
            }
        }
        // sort by arguments in descending order
        Collections.sort(list, ArgumentsListComparator.INSTANCE);
        return list;
    }

    private LinkedHashSet<String> extractNames(Annotation[][] anArray, String coStr) {
        LinkedHashSet<String> res = new LinkedHashSet<String>();
        for(Annotation[] anns : anArray) {
            for(Annotation an : anns) {
                if(Named.class.getName().equals(an.annotationType().getName())) {
                    Named na = (Named) an;
                    if(!hasText(na.value())) throw new IllegalArgumentException("@Named annotation with empty value found, constructor: '"  + coStr + "'");
                    boolean unique = res.add(na.value().toLowerCase(Locale.ENGLISH));
                    if(!unique) throw new IllegalArgumentException("Not unique @Named value: '" + na.value() + "', constructor: '" + coStr + "'");
                }
            }
        }
        if(!(0 == res.size() || anArray.length == res.size())) throw new IllegalArgumentException(
                "Not consistent @Named annotations found for constructor: '" + coStr + "'");
        return res;
    }

    private static class ArgumentsListComparator implements Comparator<NamedConstructor> {
        private static final Comparator<NamedConstructor> INSTANCE = new ArgumentsListComparator();
        @Override
        public int compare(NamedConstructor o1, NamedConstructor o2) {
            return o2.names.size() - o1.names.size();
        }
    }
}
