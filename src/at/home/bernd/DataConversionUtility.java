package at.home.bernd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A utility class that provides methods for data conversion. Singleton.
 */
public class DataConversionUtility
{
    /**
     * Holds the singleton instance.
     */
    private static class SingletonHolder
    {
        private static final DataConversionUtility INSTANCE = new DataConversionUtility();
    }
    
    /**
     * Use getInstance() instead.
     */
    private DataConversionUtility()
    {
    }
    
    /**
     * Returns the singleton instance of this class.
     * @return
     */
    public static DataConversionUtility getInstance()
    {
        return SingletonHolder.INSTANCE;
    }
    
    /**
     * Parses a timestamp and returns its value as a Date.
     * 
     * @param timeStamp        the timestamp
     * @param simpleDateFormat the date format
     * @return                 the timestamp as Date
     */
    public Date parseDateString(String timeStamp, SimpleDateFormat simpleDateFormat)
    {
        Date date = null;
        try
        {
            date = simpleDateFormat.parse(timeStamp);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return date;
    }
    
    /**
     * Parses a temperature string and returns its value as a number. 
     * 
     * @param temperature the temperature string
     * @return            the temperature
     */
    public double parseTemperatureString(String temperature)
    {
        temperature = temperature.strip();
        int indexOfC = temperature.indexOf('C');
        if (indexOfC > 0)
        {
            temperature = temperature.substring(0, indexOfC);
        }
        return Double.parseDouble(temperature);
    }
    
    /**
     * Returns the time string representation of the given timestamp.
     * 
     * @param timestamp the given timestamp
     * @return          the time representation (hours, minutes)
     */
    public String timeString(Date timestamp)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Vienna"));
        String timeString = sdf.format(timestamp);
        return timeString;
    }
    
    /**
     * Returns the date string representation of the given timestamp.
     * 
     * @param timestamp the given timestamp
     * @return          the date representation (year-month-day)
     */
    public String dateString(Date timestamp)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Vienna"));
        String dateString = sdf.format(timestamp);
        return dateString;
    }
    
    /**
     * Maps the wind direction from textual to degrees (E = 90; S = 180; W = 270; N = 360;)
     * 
     * @param direction the textual representation of the wind direction (German or English)
     * @return the corresponding value in degrees
     */
    public double mapDirection(String direction)
    {
        String d = direction.strip();
        if ("NNE".equals(d) || ("NNO".equals(d))) { return 22.5; }
        if ("NE".equals(d)  || ("NO".equals(d)))  { return 45; }
        if ("ENE".equals(d) || ("ONO".equals(d))) { return 67.5; }
        if ("E".equals(d)   || ("O").equals(d))   { return 90; }
        if ("ESE".equals(d) || ("OSO".equals(d))) { return 112.5; }
        if ("SE".equals(d)  || ("SO".equals(d)))  { return 135; }
        if ("SSE".equals(d) || ("SSO".equals(d))) { return 157.5; }
        if ("S".equals(d))                        { return 180; }
        if ("SSW".equals(d))                      { return 202.5; }
        if ("SW".equals(d))                       { return 225; }
        if ("WSW".equals(d))                      { return 247.5; }
        if ("W".equals(d))                        { return 270; }
        if ("WNW".equals(d))                      { return 292.5; }
        if ("NW".equals(d))                       { return 315; }
        if ("NNW".equals(d))                      { return 337.5; }
        if ("N".equals(d))                        { return 360; }
        return -1;
    }
}
