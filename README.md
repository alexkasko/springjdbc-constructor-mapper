Spring's RowMapper implementation using annotated constructors
==============================================================

Creates `RowMapper`s for classes having their constructor arguments annotated with JSR330 @Named annotation.
Uses constructors for objects instantiation. Supports class hierarchies using discriminator column.
May be used as an alternative for <a href="http://static.springsource.org/spring/docs/2.5.x/api/org/springframework/jdbc/core/BeanPropertyRowMapper.html">BeanPropertyRowMapper</a>

Library depends on [spring-jdbc](http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html).

Library is available in [Maven cental](http://repo1.maven.org/maven2/com/alexkasko/springjdbc/).

Javadocs for the latest release are available [here](http://alexkasko.github.com/springjdbc-constructor-mapper/javadocs).

Library usage
-------------

Maven dependency (available in central repository):

    <dependency>
        <groupId>com.alexkasko.springjdbc</groupId>
        <artifactId>springjdbc-constructor-mapper</artifactId>
        <version>1.0.1</version>
    </dependency>

Single class example:

    // class with annotated constructor
    private static class MyClass {
        private final String foo;
        private final int bar;

        private MyClass(@Named("foo") String foo, @Named("bar") Integer bar) {
            this.id = id;
            this.bar = null != bar ? bar : -1;
        }
    }
    // mapper creation
    RowMapper<MyClass> mapper = NamedConstructorMapper.forClass(MyClass.class);
    // standard row mapper usage
    MyClass obj = jt.queryForObject("select * from my_table where id = 42", mapper);

Subclasses example:

    // builder takes discriminator column name, subclasses are registered with their discriminators
    RowMapper<Parent> mapper = NamedConstructorMapper.<Parent>builder("discriminator_column")
            .addSubclass("Foo", Foo.class)
            .addSubclass("Bar", Bar.class)
            .build();

####annotated constructors

To support constructor invocation with unordered row data constructor arguments must be named.
There are different ways to access constructor names in runtime (see <a href="http://paranamer.codehaus.org/">paranamer</a>).
This project uses JSR330 `javax.inject.Named` annotations, that must be applied to all constructor arguments.
Constructors without `@Named` annotations on arguments will be ignored.
Constructors with `@Named` annotations must have all they arguments annotated with not blank values without duplicates.

####choosing from multiple constrcutors

Constructor will be invoken only if row data contains columns for each constructor argument with the same names.
Additional columns in row data will be ignored. If class has multiple annotated constructors, they will be checked against
row data based on arguments list size in descending order.

####reflection introspection and caching

All reflection introspection is done on mapper instantiation. After that mapper instances hold references to constructors and
their arguments names and invoke constructors for incoming row data. `NamedConstructorMapper` doesn't cache mapper creation:
each `NamedConstructorMapper.forClass` call do reflection introspection. So it's better to create mappers for each class
only once (using `static final` field or application level cache).

####subclasses mapping

`NamedConstructorMapper` supports subclasses mapping using discriminator column. Subclasses mapper chooses constructor
by discriminator column value.

####columns case sensivity

All column names to `@Named` values comparisons are case-insensitive using `Locale.ENGLISH`, so all column names
and `@Named` values must be locale insensitive.

License information
-------------------

This project is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Changelog
---------

**1.0.1** (2013-05-06)

 * static-import-friendly alias for single classes

**1.0** (2012-11-11)

 * initial version