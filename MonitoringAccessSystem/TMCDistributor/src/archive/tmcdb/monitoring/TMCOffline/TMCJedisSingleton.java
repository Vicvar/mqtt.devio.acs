/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package archive.tmcdb.monitoring.TMCOffline;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import org.apache.log4j.Logger;

/**
 * Singleton Jedis connection
 *
 * @version 1.0
 * @author pmerino@alma.cl
 */
public class TMCJedisSingleton {
    /** The Redis time between eviction runs millis by default */
    private static final int REDIS_TIME_BEETWEEN_EVICTION_RUNS_MILLIS_DEFAULT = -1;

    /** The Redis URL by default */
    private static final String REDIS_URL_DEFAULT = "localhost";

    /** The Redis Port by default */
    private static final int REDIS_PORT_DEFAULT = 6379;

    /** The Redis Timeout by default */
    private static final int REDIS_TIMEOUT_DEFAULT = 0;

    /** The logger */
    private static final Logger log = Logger.getLogger(TMCJedisSingleton.class);

    /** Instance constant */
    private static TMCJedisSingleton INSTANCE = null;

    /** Jedis pool configuration */
    private static JedisPoolConfig jedisPoolConfig;

    /** Jedis pool */
    private static JedisPool jedisPool;

    /** The jedis */
    private static Jedis jedis;

    /** The Redis time between eviction runs millis */
    private int redisTimeBetweenEvictionRunsMillis = REDIS_TIME_BEETWEEN_EVICTION_RUNS_MILLIS_DEFAULT;

    /** The Redis URL */
    private String redisUrl = REDIS_URL_DEFAULT;

    /** The Redis Port */
    private int redisPort = REDIS_PORT_DEFAULT;

    /** The Redis Timeout */
    private int redisTimeout = REDIS_TIMEOUT_DEFAULT;

    /**
     * Constructor
     */
    private TMCJedisSingleton() {
        TMCProperties tmcProperties = new TMCProperties(TMCConstants.CONFIGURATION_FILE);

        try {
            redisTimeBetweenEvictionRunsMillis = (new Integer(tmcProperties.getProperties().getProperty(TMCConstants.REDIS_TIME_BEETWEEN_EVICTION_RUNS_MILLIS))).intValue();
        }
        catch (Exception e) {
            log.error("Error reading redisTimeBetweenEvictionRunsMillis");
            log.error(e);
            e.printStackTrace();
        }

        try {
            redisUrl = tmcProperties.getProperties().getProperty(TMCConstants.REDIS_URL);
        }
        catch (Exception e) {
            log.error("Error reading redisUrl");
            log.error(e);
            e.printStackTrace();
        }

        try {
            redisPort = (new Integer(tmcProperties.getProperties().getProperty(TMCConstants.REDIS_PORT))).intValue();
        }
        catch (Exception e) {
            log.error("Error reading redisPort");
            log.error(e);
            e.printStackTrace();
        }

        try {
            redisTimeout = (new Integer(tmcProperties.getProperties().getProperty(TMCConstants.REDIS_TIMEOUT))).intValue();
        }
        catch (Exception e) {
            log.error("Error reading redisTimeout");
            log.error(e);
            e.printStackTrace();
        }

        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(redisTimeBetweenEvictionRunsMillis);
        jedisPool = new JedisPool(jedisPoolConfig, redisUrl, redisPort, redisTimeout);

        try {
            jedis = jedisPool.getResource();
        }
        catch (JedisConnectionException jce) {
            log.error("Error connecting to Redis");
            log.error(jce);
            jce.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Create the instance
     */
    private synchronized static void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TMCJedisSingleton();
        }
    }

    /**
     * Get the instance
     *
     * @return The instance
     */
    public static TMCJedisSingleton getInstance() {
        createInstance();
        return INSTANCE;
    }

    /**
     * Get the jedis connection
     *
     * @return The jedis connection
     */
    public static Jedis getJedis() {
        return jedis;
    }
}