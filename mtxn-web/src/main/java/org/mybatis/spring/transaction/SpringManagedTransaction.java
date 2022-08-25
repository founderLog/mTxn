/**
 * Copyright 2010-2016 the original author or authors.
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
package org.mybatis.spring.transaction;

import com.github.mtxn.application.Application;
import com.github.mtxn.transaction.MultiTransactionManager;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.transaction.Transaction;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.springframework.util.Assert.notNull;

/**
 *  重写org.mybatis.spring.transaction.SpringManagedTransaction#getConnection()
 *  Spring托管事物从这里取连接
 *  当前事务处于@MultiTransaction时，从ConnectionProxy获取连接
 */
public class SpringManagedTransaction implements Transaction {

	private static final Log LOGGER = LogFactory.getLog(SpringManagedTransaction.class);

	private final DataSource dataSource;

	private Connection connection;

	private boolean isConnectionTransactional;

	private boolean autoCommit;

	public SpringManagedTransaction(DataSource dataSource) {
		notNull(dataSource, "No DataSource specified");
		this.dataSource = dataSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Connection getConnection() throws SQLException {
		MultiTransactionManager multiTransactionManager = Application.resolve(MultiTransactionManager.class);
		// 当前处于多数据源事务，不走缓存
		if(multiTransactionManager.isTransOpen()){
			return this.dataSource.getConnection();
		}else{
			if (this.connection == null) {
				openConnection();
			}
			return this.connection;
		}
	}

	/**
	 * Gets a connection from Spring transaction manager and discovers if this
	 * {@code Transaction} should manage connection or let it to Spring.
	 * <p>
	 * It also reads autocommit setting because when using Spring Transaction MyBatis
	 * thinks that autocommit is always false and will always call commit/rollback
	 * so we need to no-op that calls.
	 */
	private void openConnection() throws SQLException {
		this.connection = DataSourceUtils.getConnection(this.dataSource);
		this.autoCommit = this.connection.getAutoCommit();
		this.isConnectionTransactional = DataSourceUtils.isConnectionTransactional(this.connection, this.dataSource);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(
					"JDBC Connection ["
							+ this.connection
							+ "] will"
							+ (this.isConnectionTransactional ? " " : " not ")
							+ "be managed by Spring");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit() throws SQLException {
		if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Committing JDBC Connection [" + this.connection + "]");
			}
			this.connection.commit();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rollback() throws SQLException {
		if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Rolling back JDBC Connection [" + this.connection + "]");
			}
			this.connection.rollback();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws SQLException {
		DataSourceUtils.releaseConnection(this.connection, this.dataSource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getTimeout() throws SQLException {
		ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		if (holder != null && holder.hasTimeout()) {
			return holder.getTimeToLiveInSeconds();
		}
		return null;
	}

}
