/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.it.sqlfile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.seasar.doma.it.Container;
import org.seasar.doma.it.Sandbox;
import org.seasar.doma.it.dao.EmployeeDao;
import org.seasar.doma.it.entity.Employee;
import org.seasar.doma.jdbc.SelectOptions;

public class SqlFileSelectStreamTest {

    @ClassRule
    public static Container container = new Container();

    @Rule
    public Sandbox sandbox = new Sandbox(container);

    @Test
    public void testStreamAll() throws Exception {
        EmployeeDao dao = container.get(EmployeeDao::get);
        Long count = dao.streamAll(stream -> stream
                .filter(e -> e.getEmployeeName() != null)
                .filter(e -> e.getEmployeeName().startsWith("S")).count());
        assertEquals(new Long(2), count);
    }

    @Test
    public void testStreamAll_resultStream() throws Exception {
        EmployeeDao dao = container.get(EmployeeDao::get);
        Long count = null;
        try (Stream<Employee> stream = dao.streamAll()) {
            count = stream.filter(e -> e.getEmployeeName() != null)
                    .filter(e -> e.getEmployeeName().startsWith("S")).count();
        }
        assertEquals(new Long(2), count);
    }

    @Test
    public void testStreamBySalary() throws Exception {
        EmployeeDao dao = container.get(EmployeeDao::get);
        Long count = dao.streamBySalary(new BigDecimal(2000),
                stream -> stream.count());
        assertEquals(new Long(6), count);
    }

    @Test
    public void testStreamBySalary_resultStream() throws Exception {
        EmployeeDao dao = container.get(EmployeeDao::get);
        Long count;
        try (Stream<Employee> stream = dao.streamBySalary(new BigDecimal(2000))) {
            count = stream.count();
        }
        assertEquals(new Long(6), count);
    }

    @Test
    public void testEntity() throws Exception {
        EmployeeDao dao = container.get(EmployeeDao::get);
        long count = dao.streamAll(s -> s.count());
        assertEquals(14L, count);
    }

    @Test
    public void testEntity_limitOffset() throws Exception {
        EmployeeDao dao = container.get(EmployeeDao::get);
        long count = dao.streamAll(s -> s.count(), SelectOptions.get().limit(5)
                .offset(3));
        assertEquals(5L, count);
    }

    @Test
    public void testEntity_limitOffset_resultStream() throws Exception {
        EmployeeDao dao = container.get(EmployeeDao::get);
        long count;
        try (Stream<Employee> stream = dao.streamAll(SelectOptions.get()
                .limit(5).offset(3))) {
            count = stream.count();
        }
        assertEquals(5L, count);
    }

    @Test
    public void testDomain() throws Exception {
        EmployeeDao dao = container.get(EmployeeDao::get);
        BigDecimal total = dao.streamAllSalary(s -> s.filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, (x, y) -> x.add(y)));
        assertTrue(new BigDecimal("29025").compareTo(total) == 0);
    }

    @Test
    public void testDomain_limitOffset() throws Exception {
        EmployeeDao dao = container.get(EmployeeDao::get);
        BigDecimal total = dao.streamAllSalary(s -> s.filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, (x, y) -> x.add(y)), SelectOptions
                .get().limit(5).offset(3));
        assertTrue(new BigDecimal("6900").compareTo(total) == 0);
    }

    @Test
    public void testMap() throws Exception {
        EmployeeDao dao = container.get(EmployeeDao::get);
        long count = dao.selectAllAsMapList(s -> s.count());
        assertEquals(14L, count);
    }

    @Test
    public void testMap_limitOffset() throws Exception {
        EmployeeDao dao = container.get(EmployeeDao::get);
        long count = dao.selectAllAsMapList(s -> s.count(), SelectOptions.get()
                .limit(5).offset(3));
        assertEquals(5L, count);
    }
}
