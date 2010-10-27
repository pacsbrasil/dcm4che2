package org.weasis.launcher.wado.xml;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtil {

    private static Logger logger = LoggerFactory.getLogger(DateUtil.class);

    public static String DATE_FORMAT = "dd-MM-yyyy";

    public static String TIME_FORMAT = "HH:mm:ss";

    public static SimpleDateFormat WeasisTimeFormat = new SimpleDateFormat(TIME_FORMAT);

    public static SimpleDateFormat WeasisDateFormat = new SimpleDateFormat(DATE_FORMAT);

    public static String getTimestamp(ResultSet resultSet, String field, String targetFormat) {
        String result = null;
        Timestamp timestamp = null;
        try {
            timestamp = resultSet.getTimestamp(field);
        } catch (Exception e) {
            logger.error("Sql Error: cannot get the date of the field [{}]", field);
        }
        if (timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(targetFormat);
            result = sdf.format(timestamp);
            logger.debug("Timestamp [{}] converted to [{}]", timestamp, result);
        }
        return result;
    }

    public static String convertDicomDateFormat(java.util.Date date) {
        String result = null;
        if (date != null) {

            result = WeasisDateFormat.format(date);
            logger.debug("Date [{}] converted to [{}]", date, result);
        }
        return result;
    }

    public static String convertDicomTimeFormat(java.util.Date timeInput) {
        String result = null;
        if (timeInput != null) {
            result = WeasisTimeFormat.format(timeInput);
            logger.debug("Date [{}] converted to [{}]", timeInput, result);
        }
        return result;
    }

    public static String convertDateFormat(String dateInput, String inputFormat) {
        if (dateInput == null) {
            return null;
        }
        String result = null;
        SimpleDateFormat sdf;
        java.util.Date date = null;
        try {
            sdf = new SimpleDateFormat(inputFormat);
            date = sdf.parse(dateInput);
        } catch (Exception e) {
            logger.error("Date Error: cannot convert the date [{}]", dateInput);
        }
        if (date != null) {
            result = WeasisDateFormat.format(date);
            logger.debug("Date [{}] converted to [{}]", date, result);
        }
        return result;
    }

    public static String getDate(ResultSet resultSet, String field, String targetFormat) {
        String result = null;
        Date date = null;
        try {
            date = resultSet.getDate(field);
        } catch (Exception e) {
            logger.error("Sql Error: cannot get the date of the field [{}]", field);
        }
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(targetFormat);
            result = sdf.format(date);
            logger.debug("Date [{}] converted to [{}]", date, result);
        }
        return result;
    }

    public static String getDateFromStr(ResultSet resultSet, String field, String sourceFormat, String targetFormat) {
        String result = null;
        String dateStr = "";
        try {
            dateStr = resultSet.getString(field);
        } catch (Exception e) {
            logger.error("Sql Error: cannot get the date of the field [{}]", field);
        }
        try {
            if ((dateStr != null) && (!dateStr.equalsIgnoreCase(""))) {
                SimpleDateFormat sdfSource = new SimpleDateFormat(sourceFormat);
                java.util.Date date = sdfSource.parse(dateStr);

                SimpleDateFormat sdfTarget = new SimpleDateFormat(targetFormat);
                result = sdfTarget.format(date);

                logger.debug("Date [{}] converted to [{}]", dateStr, result);
            }
        } catch (ParseException e) {
            logger.error("Format Error: error parsing the field [{}] [{}]", field, e.getMessage());
        }
        return result;
    }

    public static java.util.Date getDate(String date) {
        if (date != null) {
            try {
                return WeasisDateFormat.parse(date);
            } catch (Exception e) {
            }
        }
        return null;
    }
}
