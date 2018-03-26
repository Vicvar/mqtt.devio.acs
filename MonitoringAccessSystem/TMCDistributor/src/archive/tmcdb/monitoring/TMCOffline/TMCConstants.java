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

import java.util.Date;

/**
 * All constants of the application
 * 
 * @version 1.0
 * @author pmerino@alma.cl
 */
public class TMCConstants {
    /** The application config file */
    public static final String APPLICATION_CONFIG_FILE = "classpath:META-INF/applicationContext.xml";

    /** String formatter: yyyy-MM-dd HH:mm:ss */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /** String formatter: yyyy-MM-dd'T'HH:mm:ss */
    public static final String YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";

    /** String formatter: yyyy-MM-dd'T'HH:mm:ss:SSS */
    public static final String YYYY_MM_DD_T_HH_MM_SS_SSS = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    /** String formatter: dd-MM-yyyy */
    public static final String DD_MM_YYYY = "dd-MM-yyyy";

    /** String formatter: yyyy-MM-dd */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    /** String formatter: yyyyMMdd */
    public static final String YYYYMMDD = "yyyyMMdd";

    /** The UTC time */
    public static final String UTC = "UTC";

    /** Clob size field size */
    public static final int CLOB_FIELD_SIZE = 5;

    /** The Redis channel size */
    public static final int CHANNEL_SIZE = 3;

    /** Property name: path dumper */
    public static final String PATH_DUMPER = "path_dumper";

    /** Property name: unset value */
    public static final String UNSET_VALUE = "unset_value";

    /** Property name: unknown monitor point name */
    public static final String UNKNOWN_MONITOR_POINT_NAME = "unknown_monitor_point_name";

    /** Property name: maximun clob size */
    public static final String MAX_CLOB_SIZE = "max_clob_size";

    /** Property name: application name */
    public static final String APPLICATION_NAME = "application_name";

    /** Redis max limit date */
    public static final String REDIS_MAX_LIMIT_DATE = "redis_max_limit_date";

    /** Property name: Redis max length list */
    public static final String REDIS_MAX_LENGTH_LIST = "redis_max_length_list";

    /** Property name: Redis remove from list */
    public static final String REDIS_REMOVE_FROM_LIST = "redis_remove_from_list";

    /** Property name: time beetween eviction run millis */
    public static final String REDIS_TIME_BEETWEEN_EVICTION_RUNS_MILLIS = "redis_time_between_eviction_runs_millis";

    /** Property name: Redis Url */
    public static final String REDIS_URL = "redis_url";

    /** Property name: Redis port */
    public static final String REDIS_PORT = "redis_port";

    /** Property name: Redis time out */
    public static final String REDIS_TIMEOUT = "redis_timeout";

    /** Property name: topic name */
    public static final String TOPIC_NAME = "topic_name";

    /** Property name: bean name */
    public static final String BEAN_NAME = "bean_name";

    /** The extension file */
    public static final String EXTENSION_FILE = "extension_file";

    /** The configuration file system environment */
    public static final String CONFIGURATION_FILE = "CONFIG_ARCHIVER_FILE";

    /** The maximun of total threads */
    public static final String MAX_TOTAL_THREADS = "max_total_threads";

    /** The thread sleep */
    public static final String THREAD_SLEEP = "thread_sleep";

    /** The time limit */
    public static final String TIME_LIMIT = "time_limit";

    /** The time delta */
    public static final String TIME_DELTA = "time_delta";

    /** The startup date by default */
    public static final Date STARTUP_DATE_DEFAULT = new Date();
}
