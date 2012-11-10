package com.alexkasko.springjdbc.named;

import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.inject.Named;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * User: alexey
 * Date: 7/5/12
 */
public class NamedConstructorMapperTest {
    private static final JdbcTemplate jt;

    static {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        jt = new JdbcTemplate(ds);
    }

    @Test
    public void testSingle() throws Exception {
        jt.update("create table single_table(id int not null, foo varchar(10), bar int)");
        jt.update("insert into single_table(id, foo, bar) values(1, 'fff', 42)");
        jt.update("insert into single_table(id, foo) values(2, 'nnn')");
        jt.update("insert into single_table(id, bar) values(3, 43)");
        NamedConstructorMapper<Single> mapper = NamedConstructorMapper.forClass(Single.class);
        List<Single> list = jt.query("select * from single_table order by id", mapper);
        assertEquals("Size fail", 3, list.size());
        assertNotNull("Creation fail", list.get(0));
        assertNotNull("Creation fail", list.get(1));
        assertNotNull("Creation fail", list.get(2));
        assertEquals("Data fail", "fff", list.get(0).foo);
        assertEquals("Data fail", 42, list.get(0).bar);
        assertEquals("Data fail", "nnn", list.get(1).foo);
        assertEquals("Data fail", -1, list.get(1).bar);
        assertNull("Data fail", list.get(2).foo);
        assertEquals("Data fail", 43, list.get(2).bar);
    }

    private static class Single {
        private final int id;
        private final String foo;
        private final int bar;

        private Single(@Named("id") int id, @Named("foo") String foo, @Named("bar") Integer bar) {
            this.id = id;
            this.foo = foo;
            this.bar = null != bar ? bar: -1;
        }
    }

//    @Test
    public void testSubclasses() throws UnsupportedEncodingException {
        jt.update("create table subclasses_table(" +
                "id int, " +
                "discriminator_col varchar(10), " +
                "common_col varchar(10), " +
                "foo_col int, " +
                "bar_col timestamp" +
                ")");
        jt.update("insert into subclasses_table(id, discriminator_col, common_col, foo_col) " +
                        "values(1, 'Foo', 'fff', 42)");
        jt.update("insert into subclasses_table(id, discriminator_col, common_col, bar_col) " +
                        "values(2, 'Bar', 'bbb', '2012-01-01 00:00:00')");
        jt.update("insert into subclasses_table(id, discriminator_col, common_col) " +
                        "values(3, 'Foo', 'nnn')");
        NamedConstructorMapper<Parent> mapper = NamedConstructorMapper.<Parent>builder("discriminator_col")
                .addSubclass("Foo", Foo.class)
                .addSubclass("Bar", Bar.class)
                .build();
        List<Parent> list = jt.query("select * from subclasses_table order by id", mapper);
        assertEquals("Size fail", 3, list.size());
        assertNotNull("Instantiation fail", list.get(0));
        assertNotNull("Instantiation fail", list.get(1));
        assertNotNull("Instantiation fail", list.get(2));
        assertTrue("Subclass fail", list.get(0) instanceof Foo);
        assertTrue("Subclass fail", list.get(1) instanceof Bar);
        assertTrue("Subclass fail", list.get(3) instanceof Foo);
        assertEquals("Data fail", "fff", list.get(0).common);
        assertEquals("Data fail", 42, ((Foo) list.get(0)).fooColumn);
        assertEquals("Data fail", "bbb", list.get(0).common);
        assertEquals("Data fail", "2012-01-01 00:00:00", ((Bar) list.get(0)).barColumn.toString());
        assertEquals("Data fail", "nnn", list.get(0).common);
        assertEquals("Data fail", -1, ((Foo) list.get(0)).fooColumn);
    }

    private static abstract class Parent {
        protected final int id;
        protected final String common;

        private Parent(int id, String common) {
            this.id = id;
            this.common = common;
        }
    }

    private static class Foo extends Parent {
        private final int fooColumn;

        private Foo(@Named("id") int id, @Named("common_col") String common_col, @Named("foo_col") Integer foo_col) {
            super(id, common_col);
            this.fooColumn = null != foo_col ? foo_col : -1;
        }
    }

    private static class Bar extends Parent {
        private final Date barColumn;

        private Bar(@Named("id") int id, @Named("common_col") String common_col, @Named("bar_col") Timestamp bar_col) {
            super(id, common_col);
            this.barColumn = bar_col;
        }
    }
}