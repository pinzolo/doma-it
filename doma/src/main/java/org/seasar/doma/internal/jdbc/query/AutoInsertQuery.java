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

import java.sql.Statement;

import org.seasar.doma.DomaMessageCode;
import org.seasar.doma.entity.Entity;
import org.seasar.doma.entity.EntityProperty;
import org.seasar.doma.entity.GeneratedIdProperty;
import org.seasar.doma.internal.jdbc.sql.PreparedSqlBuilder;
import org.seasar.doma.jdbc.JdbcException;
import org.seasar.doma.jdbc.id.IdGenerationConfig;

/**
 * @author taedium
 * 
 */
public class AutoInsertQuery<I, E extends Entity<I>> extends
        AutoModifyQuery<I, E> implements InsertQuery {

    protected boolean nullExcluded;

    protected GeneratedIdProperty<?> generatedIdProperty;

    protected IdGenerationConfig idGenerationConfig;

    public AutoInsertQuery(Class<E> entityClass) {
        super(entityClass);
    }

    public void compile() {
        assertNotNull(config, entity, callerClassName, callerMethodName);
        executable = true;
        entity.__preInsert();
        prepareTableAndColumnNames();
        prepareIdAndVersionProperties();
        prepareOptions();
        prepareTargetProperties();
        prepareVersionValue();
        prepareSql();
        assertNotNull(sql);
    }

    @Override
    protected void prepareIdAndVersionProperties() {
        super.prepareIdAndVersionProperties();
        generatedIdProperty = entity.__getGeneratedIdProperty();
        if (generatedIdProperty != null) {
            String idColumnName = columnNameMap.get(generatedIdProperty
                    .getName());
            idGenerationConfig = new IdGenerationConfig(config, entity,
                    tableName, idColumnName);
            generatedIdProperty.validateGenerationStrategy(idGenerationConfig);
            autoGeneratedKeysSupported = generatedIdProperty
                    .isAutoGeneratedKeysSupported(idGenerationConfig);
            generatedIdProperty.preInsert(idGenerationConfig);
        }
        versionProperty = entity.__getVersionProperty();
    }

    protected void prepareTargetProperties() {
        for (EntityProperty<?> p : entity.__getEntityProperties()) {
            if (p.isTransient()) {
                continue;
            }
            if (!p.isInsertable()) {
                continue;
            }
            if (p.isId()) {
                if (p != generatedIdProperty
                        || generatedIdProperty.isIncluded(idGenerationConfig)) {
                    targetProperties.add(p);
                }
                if (generatedIdProperty == null && p.getDomain().isNull()) {
                    throw new JdbcException(DomaMessageCode.DOMA2020, entity
                            .__getName(), p.getName());
                }
                continue;
            }
            if (p.isVersion()) {
                targetProperties.add(p);
                continue;
            }
            if (nullExcluded && p.getDomain().isNull()) {
                continue;
            }
            if (!isTargetPropertyName(p.getName())) {
                continue;
            }
            targetProperties.add(p);
        }
    }

    protected void prepareVersionValue() {
        if (versionProperty != null) {
            versionProperty.setIfNecessary(1);
        }
    }

    protected void prepareSql() {
        PreparedSqlBuilder builder = new PreparedSqlBuilder(config);
        builder.appendSql("insert into ");
        builder.appendSql(tableName);
        builder.appendSql(" (");
        for (EntityProperty<?> p : targetProperties) {
            builder.appendSql(columnNameMap.get(p.getName()));
            builder.appendSql(", ");
        }
        builder.cutBackSql(2);
        builder.appendSql(") values (");
        for (EntityProperty<?> p : targetProperties) {
            builder.appendDomain(p.getDomain());
            builder.appendSql(", ");
        }
        builder.cutBackSql(2);
        builder.appendSql(")");
        sql = builder.build();
    }

    @Override
    public void generateId(Statement statement) {
        if (generatedIdProperty != null) {
            generatedIdProperty.postInsert(idGenerationConfig, statement);
        }
    }

    public void setNullExcluded(boolean nullExcluded) {
        this.nullExcluded = nullExcluded;
    }

}
