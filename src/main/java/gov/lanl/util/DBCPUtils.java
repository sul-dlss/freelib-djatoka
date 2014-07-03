/*
 * Copyright (c) 2009  Los Alamos National Security, LLC.
 *
 * Los Alamos National Laboratory
 * Research Library
 * Digital Library Research & Prototyping Team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */

package gov.lanl.util;

import java.util.Date;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.lanl.adore.djatoka.openurl.ResolverException;

/**
 * DBCP / JDBC Utilities Wrapper
 *
 * @author Ryan Chute
 */
public class DBCPUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(DBCPUtils.class.getName());

    private DBCPUtils() {
    }

    /**
     * Set-up a DBCP DataSource from a properties object. Uses a properties key prefix to identify the properties
     * associated with profile. If a database profile has a prefix of djatoka, the props object would contain the
     * following pairs: djatoka.url=jdbc:mysql://localhost/djatoka djatoka.driver=com.mysql.jdbc.Driver
     * djatoka.login=root djatoka.pwd= djatoka.maxActive=50 djatoka.maxIdle=10
     *
     * @param dbid database profile properties file prefix
     * @param props properties object containing relevant pairs
     */
    public static DataSource setupDataSource(final String dbid, final Properties props) throws Exception {
        final String url = props.getProperty(dbid + ".url");
        final String driver = props.getProperty(dbid + ".driver");
        final String login = props.getProperty(dbid + ".login");
        final String pwd = props.getProperty(dbid + ".pwd");
        int maxActive = 50;
        if (props.containsKey(dbid + ".maxActive")) {
            maxActive = Integer.parseInt(props.getProperty(dbid + ".maxActive"));
        }
        int maxIdle = 10;
        if (props.containsKey(dbid + ".maxIdle")) {
            maxIdle = Integer.parseInt(props.getProperty(dbid + ".maxIdle"));
        }
        LOGGER.debug(url + ";" + driver + ";" + login + ";" + pwd + ";" + maxActive + ";" + maxIdle);
        return setupDataSource(url, driver, login, pwd, maxActive, maxIdle);
    }

    /**
     * Set-up a DBCP DataSource from core connection properties.
     *
     * @param connectURI jdbc connection uri
     * @param jdbcDriverName qualified classpath to jdbc driver for database
     * @param username database user account
     * @param password database password
     * @param aMaxActive max simultaneous db connections (default: 50)
     * @param aMaxIdle max idle db connections (default: 10)
     */
    public static DataSource setupDataSource(final String connectURI, final String jdbcDriverName,
            final String username, final String password, final int aMaxActive, final int aMaxIdle) throws Exception {
        int maxActive;
        int maxIdle;

        try {
            java.lang.Class.forName(jdbcDriverName).newInstance();
        } catch (final Exception e) {
            LOGGER.error("Error when attempting to obtain DB Driver: " + jdbcDriverName + " on " +
                    new Date().toString(), e);
            throw new ResolverException(e.getMessage(), e);
        }

        if (aMaxActive <= 0) {
            maxActive = 50;
        } else {
            maxActive = aMaxActive;
        }

        if (aMaxIdle <= 0) {
            maxIdle = 10;
        } else {
            maxIdle = aMaxIdle;
        }

        final GenericObjectPool connectionPool =
                new GenericObjectPool(null, maxActive, GenericObjectPool.WHEN_EXHAUSTED_BLOCK, 3000, maxIdle, false,
                        false, 60000, 5, 30000, true);

        final ConnectionFactory connectionFactory =
                new DriverManagerConnectionFactory(connectURI, username, password);

        new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);

        final PoolingDataSource dataSource = new PoolingDataSource(connectionPool);

        return dataSource;
    }
}
