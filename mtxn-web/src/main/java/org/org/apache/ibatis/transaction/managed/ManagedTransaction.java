/**
 * Copyright 2009-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.org.apache.ibatis.transaction.managed;

import com.github.mtxn.application.Application;
import com.github.mtxn.transaction.MultiTransactionManager;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *  重写org.org.apache.ibatis.transaction.managed.ManagedTransaction#getConnection()
 *  非托管事务会在这里获取连接
 *  当前事务处于@MultiTransaction时，从ConnectionProxy获取连接
 */
public class ManagedTransaction implements Transaction {

	private static final Log log = LogFactory.getLog(ManagedTransaction.class);

	private DataSource dataSource;
	private TransactionIsolationLevel level;
	private Connection connection;
	private final boolean closeConnection;

	public ManagedTransaction(Connection connection, boolean closeConnection) {
		this.connection = connection;
		this.closeConnection = closeConnection;
	}

	public ManagedTransaction(DataSource ds, TransactionIsolationLevel level, boolean closeConnection) {
		this.dataSource = ds;
		this.level = level;
		this.closeConnection = closeConnection;
	}

	@Override
	public Connection getConnection() throws SQLException {
		MultiTransactionManager multiTransactionManager = Application.resolve(MultiTransactionManager.class);
		// 当前处于多数据源事务，不走缓存
		if (multiTransactionManager.isTransOpen()) {
			return ((TransactionAwareDataSourceProxy) dataSource).getTargetDataSource().getConnection();
		} else {
			if (this.connection == null) {
				openConnection();
			}
			return this.connection;
		}
	}

	@Override
	public void commit() throws SQLException {
		// Does nothing
	}

	@Override
	public void rollback() throws SQLException {
		// Does nothing
	}

	@Override
	public void close() throws SQLException {
		if (this.closeConnection && this.connection != null) {
			if (log.isDebugEnabled()) {
				log.debug("Closing JDBC Connection [" + this.connection + "]");
			}
			this.connection.close();
		}
	}

	protected void openConnection() throws SQLException {
		if (log.isDebugEnabled()) {
			log.debug("Opening JDBC Connection");
		}
		this.connection = this.dataSource.getConnection();
		if (this.level != null) {
			this.connection.setTransactionIsolation(this.level.getLevel());
		}
	}

	@Override
	public Integer getTimeout() throws SQLException {
		return null;
	}

}
