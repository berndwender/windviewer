package at.home.bernd;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * A single wind information data point
 */
public class WindDataPoint
{
    /**
     * The type of data
     */
    public static enum WIND_DATA_TYPE
    {
        timestamp,
        direction,
        windSpeed,
        maxWindSpeed,
        temperature,
        chill
    };
    
    /**
     * The timestamp of the data point
     */
    private Date timestamp;
    
    /**
     * The wind direction
     */
    private String direction;
    
    /**
     * The wind speed in km/h+
     */
    private double windSpeed;
    
    /**
     * The maximum wind speed in km/h
     */
    private double maxWindSpeed;
    
    /**
     * The temperature in degrees centigrade (C)
     */
    private double temperature;
    
    /**
     * The "chill" (= temperature in degrees centigrade (C) * chill factor)
     */
    private double chill;

    /**
     * @return the timestamp
     */
    public Date getTimestamp()
    {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Date timestamp)
    {
        this.timestamp = timestamp;
    }

    /**
     * @return the direction
     */
    public String getDirection()
    {
        return direction;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(String direction)
    {
        this.direction = direction;
    }

    /**
     * @return the windSpeed
     */
    public double getWindSpeed()
    {
        return windSpeed;
    }

    /**
     * @param windSpeed the windSpeed to set
     */
    public void setWindSpeed(double windSpeed)
    {
        this.windSpeed = windSpeed;
    }

    /**
     * @return the maxWindSpeed
     */
    public double getMaxWindSpeed()
    {
        return maxWindSpeed;
    }

    /**
     * @param maxWindSpeed the maxWindSpeed to set
     */
    public void setMaxWindSpeed(double maxWindSpeed)
    {
        this.maxWindSpeed = maxWindSpeed;
    }

    /**
     * @return the temperature
     */
    public double getTemperature()
    {
        return temperature;
    }

    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(double temperature)
    {
        this.temperature = temperature;
    }

    /**
     * @return the chill
     */
    public double getChill()
    {
        return chill;
    }

    /**
     * @param chill the chill to set
     */
    public void setChill(double chill)
    {
        this.chill = chill;
    }
    
    /**
     * Returns vale rounded up to the given number of decimal places
     * 
     * @param value the original value
     * @param n the number of decimal places
     * @return the rounded number
     */
    private double roundUpToNDecimalPlaces(double value, int n)
    {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Returns a simple string representation of the wind data point
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.timestamp);
        sb.append(": ");
        sb.append(roundUpToNDecimalPlaces(this.windSpeed, 1));
        sb.append(" - ");
        sb.append(roundUpToNDecimalPlaces(this.maxWindSpeed, 1));
        sb.append("[");
        sb.append(this.direction);
        sb.append("] ");
        sb.append(roundUpToNDecimalPlaces(this.temperature, 1));
        sb.append(" / ");
        sb.append(roundUpToNDecimalPlaces(this.chill, 1));
        return sb.toString();
    }
}
