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
package org.seasar.doma;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Statement;

import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.JdbcException;
import org.seasar.doma.jdbc.OptimisticLockException;
import org.seasar.doma.jdbc.SqlFileNotFoundException;
import org.seasar.doma.jdbc.UniqueConstraintException;

/**
 * 更新処理を示します。
 * <p>
 * このアノテーションが指定されるメソッドは、{@link Dao}が注釈されたインタフェースのメンバでなければいけません。
 * <p>
 * 注釈されるメソッドは、次の制約を満たす必要があります。
 * <ul>
 * <li>パラメータを1つだけ受け取る。
 * <li>パラメータは {@link Entity}が注釈された型である。
 * <li>戻り値の型は {@code int}である。
 * </ul>
 * 
 * <pre>
 * &#064;Entity
 * public interface Employee {
 *     ...
 * }
 * 
 * &#064;Dao(config = AppConfig.class)
 * public interface EmployeeDao {
 * 
 *     &#064;Update
 *     int update(Employee employee);
 * }
 * </pre>
 * 
 * 注釈されるメソッドは、次の例外をスローすることがあります。
 * <ul>
 * <li> {@link DomaIllegalArgumentException} パラメータに {@code null}を渡した場合
 * <li> {@link OptimisticLockException} 更新件数が1件でなかった場合
 * <li> {@link UniqueConstraintException} 一意制約違反が発生した場合
 * <li> {@link SqlFileNotFoundException}
 * SQLファイルにマッピングしている場合で、かつSQLファイルが見つからなかった場合
 * <li> {@link JdbcException} 上記以外でJDBCに関する例外が発生した場合
 * </ul>
 * 
 * @author taedium
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Query
public @interface Update {

    /**
     * SQLファイルにマッピングするかどうかを示します。
     */
    boolean sqlFile() default false;

    /**
     * クエリタイムアウト（秒）です。
     * <p>
     * 指定しない場合、{@link Config#queryTimeout()}が使用されます。
     * 
     * @see Statement#setQueryTimeout(int)
     */
    int queryTimeout() default -1;

    /**
     * UPDATE文のSET句で、NULLを除去するかどうかを示します。
     */
    boolean excludeNull() default false;

    /**
     * UPDATE文のSET句にバージョン番号を含めるかどうかを示します。
     */
    boolean includeVersion() default false;

    /**
     * UPDATE文のSET句に変更されていないプロパティを含めるかどうかを示します。
     */
    boolean includeUnchanged() default false;

    /**
     * UPDATE文のSET句に含めるプロパティ名の配列です。
     * <p>
     * ここに指定できるのは、カラム名ではなくプロパティ名です。 プロパティ名とは、カラムにマッピングされたエンティティのメソッド名のことです。
     */
    String[] include() default {};

    /**
     * UPDATE文のSET句から除外するプロパティ名の配列です。
     * <p>
     * ここに指定できるのは、カラム名ではなくプロパティ名です。 プロパティ名とは、カラムにマッピングされたエンティティのメソッド名のことです。
     */
    String[] exclude() default {};

    /**
     * 更新結果が1件でない場合にスローされる {@link OptimisticLockException}を抑制するかどうかを示します。
     */
    boolean suppressOptimisticLockException() default false;

}
