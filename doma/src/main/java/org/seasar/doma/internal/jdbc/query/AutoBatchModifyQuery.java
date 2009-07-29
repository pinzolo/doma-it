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
package org.seasar.doma.internal.jdbc.query;

import static org.seasar.doma.internal.util.AssertionUtil.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.seasar.doma.DomaMessageCode;
import org.seasar.doma.entity.Entity;
import org.seasar.doma.entity.EntityProperty;
import org.seasar.doma.entity.VersionProperty;
import org.seasar.doma.internal.jdbc.sql.PreparedSql;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.JdbcException;
import org.seasar.doma.jdbc.SqlExecutionSkipCause;

/**
 * @author taedium
 * 
 */
public abstract class AutoBatchModifyQuery<I, E extends Entity<I>> implements
        BatchModifyQuery {

    protected static final String[] EMPTY_STRINGS = new String[] {};

    protected String[] includedPropertyNames = EMPTY_STRINGS;

    protected String[] excludedPropertyNames = EMPTY_STRINGS;

    protected final Class<E> entityClass;

    protected Config config;

    protected List<E> entities;

    protected String callerClassName;

    protected String callerMethodName;

    protected final List<EntityProperty<?>> targetProperties = new ArrayList<EntityProperty<?>>();

    protected final List<EntityProperty<?>> idProperties = new ArrayList<EntityProperty<?>>();

    protected VersionProperty<?> versionProperty;

    protected String tableName;

    protected final Map<String, String> columnNameMap = new HashMap<String, String>();

    protected final List<PreparedSql> sqls = new ArrayList<PreparedSql>();

    protected boolean optimisticLockCheckRequired;

    protected boolean autoGeneratedKeysSupported;

    protected boolean executable;

    protected SqlExecutionSkipCause executionSkipCause = SqlExecutionSkipCause.BATCH_TARGET_NONEXISTENT;

    protected E entity;

    protected int queryTimeout;

    protected int batchSize;

    public AutoBatchModifyQuery(Class<E> entityClass) {
        assertNotNull(entityClass);
        this.entityClass = entityClass;
    }

    protected void prepareTableAndColumnNames() {
        tableName = entity.__getQualifiedTableName(config);
        for (EntityProperty<?> p : entity.__getEntityProperties()) {
            if (p.isTransient()) {
                continue;
            }
            if (!p.isUpdatable()) {
                continue;
            }
            columnNameMap.put(p.getName(), p.getColumnName(config));
        }
    }

    protected void prepareIdAndVersionProperties() {
        for (EntityProperty<?> p : entity.__getEntityProperties()) {
            if (p.isTransient()) {
                continue;
            }
            if (p.isId()) {
                idProperties.add(p);
            }
        }
        versionProperty = entity.__getVersionProperty();
    }

    protected void validateIdExistent() {
        if (idProperties.isEmpty()) {
            throw new JdbcException(DomaMessageCode.DOMA2022, entity
                    .__getName());
        }
    }

    protected void prepareOptions() {
        if (queryTimeout <= 0) {
            queryTimeout = config.queryTimeout();
        }
        if (batchSize <= 0) {
            batchSize = config.batchSize();
        }
    }

    protected boolean isTargetPropertyName(String name) {
        if (includedPropertyNames.length > 0) {
            for (String includedName : includedPropertyNames) {
                if (includedName.equals(name)) {
                    for (String excludedName : excludedPropertyNames) {
                        if (excludedName.equals(name)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        if (excludedPropertyNames.length > 0) {
            for (String excludedName : excludedPropertyNames) {
                if (excludedName.equals(name)) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void setEntities(List<I> entities) {
        assertNotNull(entities);
        this.entities = new ArrayList<E>(entities.size());
        Iterator<I> it = entities.iterator();
        if (it.hasNext()) {
            I entity = it.next();
            if (!entityClass.isInstance(entity)) {
                throw new JdbcException(DomaMessageCode.DOMA2026, entity,
                        entityClass.getName());
            }
            this.entities.add(entityClass.cast(entity));
        }
        while (it.hasNext()) {
            I entity = it.next();
            this.entities.add(entityClass.cast(entity));
        }
    }

    public void setCallerClassName(String callerClassName) {
        this.callerClassName = callerClassName;
    }

    public void setCallerMethodName(String callerMethodName) {
        this.callerMethodName = callerMethodName;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setIncludedPropertyNames(String... includedPropertyNames) {
        this.includedPropertyNames = includedPropertyNames;
    }

    public void setExcludedPropertyNames(String... excludedPropertyNames) {
        this.excludedPropertyNames = excludedPropertyNames;
    }

    @Override
    public PreparedSql getSql() {
        return sqls.get(0);
    }

    @Override
    public String getClassName() {
        return callerClassName;
    }

    @Override
    public String getMethodName() {
        return callerMethodName;
    }

    @Override
    public List<PreparedSql> getSqls() {
        return sqls;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public boolean isOptimisticLockCheckRequired() {
        return optimisticLockCheckRequired;
    }

    @Override
    public boolean isAutoGeneratedKeysSupported() {
        return autoGeneratedKeysSupported;
    }

    @Override
    public boolean isExecutable() {
        return executable;
    }

    @Override
    public SqlExecutionSkipCause getSqlExecutionSkipCause() {
        return executionSkipCause;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    @Override
    public int getBatchSize() {
        return batchSize;
    }

    @Override
    public String toString() {
        return sqls.toString();
    }

}
