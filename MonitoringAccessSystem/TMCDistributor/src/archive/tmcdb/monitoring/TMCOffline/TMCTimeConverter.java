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

import java.text.SimpleDateFormat;
import java.text.Format;

import java.util.Date;
import java.util.SimpleTimeZone;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Util for convert times
 *
 * @author pmerino@alma.cl
 * @version 1.1
 */
public class TMCTimeConverter {
    /** The logger */
    private static final Logger log = Logger.getLogger(TMCTimeConverter.class);

    /**
     * Transforms date in format 01/01/1970 01:00:00 to long format
     *
     * @param dateWithFormat Date in String format
     * @return Date in long format
     */
    public static long toAcstime(String dateWithFormat) {
        long epoch;
        try {
            SimpleDateFormat df = new SimpleDateFormat(TMCConstants.YYYY_MM_DD_T_HH_MM_SS);
            df.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, TMCConstants.UTC));
            Date date = df.parse(dateWithFormat);
            epoch = (date.getTime())/1000;
            return (epoch * 10000000 + 122192928000000000L);
        }
        catch (java.text.ParseException e) {
            log.error("Error trying to parse the date");
            log.error(e);
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * Transforms date in format long to format 01/01/1970T01:00:00
     *
     * @param acstime ACS time
     * @return Date in format 2014-07-01T15:05:00
     */
    public static String toDateReadableFormat(long acstime) {
        long epoch=(acstime-122192928000000000L)/10000000;
        SimpleDateFormat df = new SimpleDateFormat(TMCConstants.YYYY_MM_DD_T_HH_MM_SS);
        df.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, TMCConstants.UTC));
        return df.format(new java.util.Date (epoch*1000));
    }

    /**
     * Transforms date in format long to format 01/01/1970T01:00:00.0000
     *
     * @param acstime ACS time
     * @return Date in format 2014-07-01T15:05:00.282118
     */
    public static String toDateReadableFormatMillisecond(long acstime) {
        String dateReadable = null;
        try {
          Process p = Runtime.getRuntime().exec("python acstime2humanmilliseconds.py " + acstime);
          BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
          dateReadable = in.readLine();
        }
        catch (Exception e) {
          log.error("Problem trying to convert the acstime=" + acstime + " to date readable format with millisecond.");
          log.error(e);
        }
        return dateReadable;
    }

    /**
     * Transforms date in format long to format 01/01/1970T01:00:00.000
     *
     * @param acstime ACS time
     * @return Date in format 2014-07-01T15:05:00.282
     */
    public static String toDateReadableFormatMillisecondShort(long acstime) {
        long epoch=(acstime-122192928000000000L)/10000L;
        SimpleDateFormat df = new SimpleDateFormat(TMCConstants.YYYY_MM_DD_T_HH_MM_SS_SSS);
        df.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, TMCConstants.UTC));
        return df.format(new java.util.Date (epoch));
    }

    /**
     * Transforms a java.util.Date to String format
     *
     * @param date The java util date
     * @return The String format date
     */
    public static String toDDMMYYYY(Date date) {
        Format formatter = new SimpleDateFormat(TMCConstants.DD_MM_YYYY);
        return formatter.format(date);
    }

    /**
     * Transforms an ACS time to YYYYMMDD
     *
     * @param acstime The ACS time
     * @return The String format date
     */
    public static String toYYYYMMDD(long acstime) {
        String yearMonthDay = TMCTimeConverter.toDateReadableFormat((new Long(acstime)).longValue());
        int quoteIndex = yearMonthDay.indexOf("T");
        yearMonthDay = yearMonthDay.substring(0, quoteIndex);
        return yearMonthDay;
    }

    /**
     * Transforms a java.util.Date to String format
     *
     * @param acstime The ACS time
     * @return The String format date
     */
    public static String toDDMMYYYY(long acstime) {
        String dateYYYYMMDD = toYYYYMMDD(acstime);
        String dd = dateYYYYMMDD.substring(8, 10);
        String mm = dateYYYYMMDD.substring(5, 7);
        String yyyy = dateYYYYMMDD.substring(0, 4);
        return dd + "-" + mm + "-" + yyyy;
    }

    /**
     * Transforms a datatime like as "2013-04-02 17:08:16" to ""2013-04-02"
     *
     * @param datetime The datetime
     * @return The date in YYYY-MM-DD format
     */
    public static String toYYYYMMDD(String datetime) {
        return datetime != null && datetime.length() > 0 ? datetime.substring(0, 10):null;
    }

    /**
     * See if the start time is very old or not
     *
     * @param startTime The startime
     * @return  True if the time is less than time limit, false in other wise
     */
    public static boolean isTimeVeryOld(long startTime, long timeLimit) {
        boolean isTimeVeryOld = startTime <= timeLimit;
        if (isTimeVeryOld && log.isInfoEnabled())
            log.info("The time is very old (acstime=" + startTime + ")");
        return isTimeVeryOld; 
    }

    /**
     * Transforms to date the acstime
     *
     * @param acstime The acstime
     * @return The date
     */
    public static Date toDate(long acstime) {
        return toDate(toDateReadableFormat(acstime), null);
    }

    /**
     * Transforms to Date format.
     *
     * @param dateString The date string. Like "2013-04-02T17:08:16"
     * @return The Date
     */
    public static Date toDate(String dateString, String format) {
        Date date = null;
        if (dateString != null) {
            dateString = dateString.replace("T", " ");
            try {
                SimpleDateFormat formatter;
                if (format == null)
                    formatter = new SimpleDateFormat(TMCConstants.YYYY_MM_DD_HH_MM_SS);
                else
                    formatter = new SimpleDateFormat(format);
                date = formatter.parse(dateString);
            }
            catch (java.text.ParseException pe) {
                log.error("Parse Exception");
                log.error(pe);
                pe.printStackTrace();
            }
        }
        return date;
    }

    /**
     * Transforms a acstime to hh, mm, ss values contained in an array.
     *
     * @param acstime The acstime
     * @return the time in an array 0:hh 1:mm 2:ss
     */
    public static String[] getTime(long acstime) {
        String[] time = null;
        String dateTime = toDateReadableFormat(acstime);
        if (dateTime != null && dateTime.length() > 0) {
            int index = dateTime.indexOf("T");
            if (index != -1) {
                String hhmmss = dateTime.substring(index + 1);
                time = hhmmss.split("\\:");
            }
        }
        return time;
    }

    /**
     * If currentDate is other day tahn startupDate, then return true.
     * In othercase, return false
     * 
     * @param currentDate The current date
     * @param startupDate The startup date of the application
     * @return True if is other day. El other case, is false
     */
    public static boolean isOtherDay(Date currentDate, Date startupDate) {
        return (currentDate.getDay() != startupDate.getDay()) || (currentDate.getMonth() != startupDate.getMonth()) || (currentDate.getYear() != startupDate.getYear());
    }
}
