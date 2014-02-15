package org.seasar.doma.it;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.seasar.doma.it.dao.ScriptDao;
import org.seasar.doma.jdbc.dialect.Db2Dialect;
import org.seasar.doma.jdbc.dialect.H2Dialect;
import org.seasar.doma.jdbc.dialect.HsqldbDialect;
import org.seasar.doma.jdbc.dialect.MssqlDialect;
import org.seasar.doma.jdbc.dialect.MysqlDialect;
import org.seasar.doma.jdbc.dialect.OracleDialect;
import org.seasar.doma.jdbc.dialect.PostgresDialect;
import org.seasar.doma.jdbc.dialect.SqliteDialect;

/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
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

/**
 * @author nakamura-to
 *
 */
public class Container extends TestWatcher {

    private Pattern jdbcUrlPattern = Pattern.compile("^jdbc:([^:]*):.*");

    private AppConfig config;

    public <COMPONENT> COMPONENT get(Function<AppConfig, COMPONENT> mapper) {
        return mapper.apply(config);
    }

    @Override
    protected void starting(Description description) {
        String url = System.getProperty("url",
                "jdbc:h2:mem:it;DB_CLOSE_DELAY=-1");
        String user = System.getProperty("user", "sa");
        String password = System.getProperty("password", "");
        Dbms dbms = determineDbms(url);
        config = createConfig(dbms, url, user, password);
        config.getLocalTransactionManager().required(() -> {
            ScriptDao dao = ScriptDao.get(config);
            dao.create();
        });
    }

    protected Dbms determineDbms(String url) {
        Matcher matcher = jdbcUrlPattern.matcher(url);
        if (matcher.matches()) {
            return Dbms.valueOf(matcher.group(1).toUpperCase());
        }
        throw new IllegalArgumentException("url: " + url);
    }

    protected AppConfig createConfig(Dbms dbms, String url, String user,
            String password) {
        switch (dbms) {
        case H2:
            return new AppConfig(new H2Dialect(), dbms, url, user, password);
        case HSQLDB:
            return new AppConfig(new HsqldbDialect(), dbms, url, user, password);
        case SQLITE:
            return new AppConfig(new SqliteDialect(), dbms, url, user, password);
        case MYSQL:
            return new AppConfig(new MysqlDialect(), dbms, url, user, password);
        case POSTGRESQL:
            return new AppConfig(new PostgresDialect(), dbms, url, user,
                    password);
        case SQLSERVER:
            return new AppConfig(new MssqlDialect(), dbms, url, user, password);
        case ORACLE:
            return new AppConfig(new OracleDialect(), dbms, url, user, password);
        case DB2:
            return new AppConfig(new Db2Dialect(), dbms, url, user, password);
        }
        throw new IllegalArgumentException("unreachable: " + dbms);
    }

    @Override
    protected void finished(Description description) {
        config.getLocalTransactionManager().required(() -> {
            ScriptDao dao = ScriptDao.get(config);
            dao.drop();
        });
    }

}
