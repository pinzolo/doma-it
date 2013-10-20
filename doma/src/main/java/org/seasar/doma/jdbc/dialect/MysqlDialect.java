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
package org.seasar.doma.jdbc.dialect;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.seasar.doma.DomaNullPointerException;
import org.seasar.doma.expr.ExpressionFunctions;
import org.seasar.doma.internal.jdbc.dialect.MysqlCountCalculatingTransformer;
import org.seasar.doma.internal.jdbc.dialect.MysqlCountGettingTransformer;
import org.seasar.doma.internal.jdbc.dialect.MysqlForUpdateTransformer;
import org.seasar.doma.internal.jdbc.dialect.MysqlPagingTransformer;
import org.seasar.doma.jdbc.JdbcMappingVisitor;
import org.seasar.doma.jdbc.ScriptBlockContext;
import org.seasar.doma.jdbc.SelectForUpdateType;
import org.seasar.doma.jdbc.SqlLogFormattingVisitor;
import org.seasar.doma.jdbc.SqlNode;
import org.seasar.doma.wrapper.Wrapper;

/**
 * MySQL用の方言です。
 * 
 * @author taedium
 * 
 */
public class MysqlDialect extends StandardDialect {

    /** 一意制約違反を表すエラーコードのセット */
    protected static final Set<Integer> UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES = new HashSet<Integer>(
            Arrays.asList(1022, 1062));

    /** 開始の引用符 */
    protected static final char OPEN_QUOTE = '`';

    /** 終了の引用符 */
    protected static final char CLOSE_QUOTE = '`';

    /**
     * インスタンスを構築します。
     */
    public MysqlDialect() {
        this(new MysqlJdbcMappingVisitor(), new MysqlSqlLogFormattingVisitor(),
                new MysqlExpressionFunctions());
    }

    /**
     * {@link JdbcMappingVisitor} を指定してインスタンスを構築します。
     * 
     * @param jdbcMappingVisitor
     *            {@link Wrapper} をJDBCの型とマッピングするビジター
     */
    public MysqlDialect(JdbcMappingVisitor jdbcMappingVisitor) {
        this(jdbcMappingVisitor, new MysqlSqlLogFormattingVisitor(),
                new MysqlExpressionFunctions());
    }

    /**
     * {@link SqlLogFormattingVisitor} を指定してインスタンスを構築します。
     * 
     * @param sqlLogFormattingVisitor
     *            SQLのバインド変数にマッピングされる {@link Wrapper}
     *            をログ用のフォーマットされた文字列へと変換するビジター
     */
    public MysqlDialect(SqlLogFormattingVisitor sqlLogFormattingVisitor) {
        this(new MysqlJdbcMappingVisitor(), sqlLogFormattingVisitor,
                new MysqlExpressionFunctions());
    }

    /**
     * {@link ExpressionFunctions} を指定してインスタンスを構築します。
     * 
     * @param expressionFunctions
     *            SQLのコメント式で利用可能な関数群
     */
    public MysqlDialect(ExpressionFunctions expressionFunctions) {
        this(new MysqlJdbcMappingVisitor(), new MysqlSqlLogFormattingVisitor(),
                expressionFunctions);
    }

    /**
     * {@link JdbcMappingVisitor} と {@link SqlLogFormattingVisitor}
     * を指定してインスタンスを構築します。
     * 
     * @param jdbcMappingVisitor
     *            {@link Wrapper} をJDBCの型とマッピングするビジター
     * @param sqlLogFormattingVisitor
     *            SQLのバインド変数にマッピングされる {@link Wrapper}
     *            をログ用のフォーマットされた文字列へと変換するビジター
     */
    public MysqlDialect(JdbcMappingVisitor jdbcMappingVisitor,
            SqlLogFormattingVisitor sqlLogFormattingVisitor) {
        this(jdbcMappingVisitor, sqlLogFormattingVisitor,
                new MysqlExpressionFunctions());
    }

    /**
     * {@link JdbcMappingVisitor} と {@link SqlLogFormattingVisitor} と
     * {@link ExpressionFunctions} を指定してインスタンスを構築します。
     * 
     * @param jdbcMappingVisitor
     *            {@link Wrapper} をJDBCの型とマッピングするビジター
     * @param sqlLogFormattingVisitor
     *            SQLのバインド変数にマッピングされる {@link Wrapper}
     *            をログ用のフォーマットされた文字列へと変換するビジター
     * @param expressionFunctions
     *            SQLのコメント式で利用可能な関数群
     */
    public MysqlDialect(JdbcMappingVisitor jdbcMappingVisitor,
            SqlLogFormattingVisitor sqlLogFormattingVisitor,
            ExpressionFunctions expressionFunctions) {
        super(jdbcMappingVisitor, sqlLogFormattingVisitor, expressionFunctions);
    }

    @Override
    public String getName() {
        return "mysql";
    }

    @Override
    public boolean isUniqueConstraintViolated(SQLException sqlException) {
        if (sqlException == null) {
            throw new DomaNullPointerException("sqlException");
        }
        int code = getErrorCode(sqlException);
        return UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES.contains(code);
    }

    @Override
    public boolean supportsAutoGeneratedKeys() {
        return true;
    }

    @Override
    public boolean supportsIdentity() {
        return true;
    }

    @Override
    public boolean supportsSelectForUpdate(SelectForUpdateType type,
            boolean withTargets) {
        return type == SelectForUpdateType.NORMAL && !withTargets;
    }

    @Override
    protected SqlNode toCountCalculatingSqlNode(SqlNode sqlNode) {
        MysqlCountCalculatingTransformer transformer = new MysqlCountCalculatingTransformer();
        return transformer.transform(sqlNode);
    }

    @Override
    protected SqlNode toPagingSqlNode(SqlNode sqlNode, long offset, long limit) {
        MysqlPagingTransformer transformer = new MysqlPagingTransformer(offset,
                limit);
        return transformer.transform(sqlNode);
    }

    @Override
    protected SqlNode toForUpdateSqlNode(SqlNode sqlNode,
            SelectForUpdateType forUpdateType, int waitSeconds,
            String... aliases) {
        MysqlForUpdateTransformer transformer = new MysqlForUpdateTransformer(
                forUpdateType, waitSeconds, aliases);
        return transformer.transform(sqlNode);
    }

    @Override
    protected SqlNode toCountGettingSqlNode(SqlNode sqlNode) {
        MysqlCountGettingTransformer transformer = new MysqlCountGettingTransformer();
        return transformer.transform(sqlNode);
    }

    @Override
    public String getScriptBlockDelimiter() {
        return "/";
    }

    @Override
    public ScriptBlockContext createScriptBlockContext() {
        return new MysqlScriptBlockContext();
    }

    @Override
    public String applyQuote(String name) {
        return OPEN_QUOTE + name + CLOSE_QUOTE;
    }

    /**
     * MySQL用の {@link JdbcMappingVisitor} の実装です。
     * 
     * @author taedium
     * 
     */
    public static class MysqlJdbcMappingVisitor extends
            StandardJdbcMappingVisitor {
    }

    /**
     * MySQL用の {@link SqlLogFormattingVisitor} です。
     * 
     * @author taedium
     * 
     */
    public static class MysqlSqlLogFormattingVisitor extends
            StandardSqlLogFormattingVisitor {
    }

    /**
     * MySQL用の {@link ExpressionFunctions} です。
     * 
     * @author taedium
     * 
     */
    public static class MysqlExpressionFunctions extends
            StandardExpressionFunctions {
    }

    /**
     * MySQL用の {@link ScriptBlockContext} です。
     * 
     * @author taedium
     * @since 1.7.0
     */
    public static class MysqlScriptBlockContext extends
            StandardScriptBlockContext {

        protected MysqlScriptBlockContext() {
            sqlBlockStartKeywordsList.add(Arrays.asList("create", "procedure"));
            sqlBlockStartKeywordsList.add(Arrays.asList("create", "function"));
            sqlBlockStartKeywordsList.add(Arrays.asList("create", "trigger"));
            sqlBlockStartKeywordsList.add(Arrays.asList("alter", "procedure"));
            sqlBlockStartKeywordsList.add(Arrays.asList("alter", "function"));
            sqlBlockStartKeywordsList.add(Arrays.asList("alter", "trigger"));
            sqlBlockStartKeywordsList.add(Arrays.asList("declare"));
            sqlBlockStartKeywordsList.add(Arrays.asList("begin"));
        }
    }

}
