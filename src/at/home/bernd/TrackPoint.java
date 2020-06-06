package at.home.bernd;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * A track point is a part of a track segment as defined in the GPS Exchange Format (GPX).
 */
public class TrackPoint
{
    /**
     * The type of data
     */
    public static enum TRACK_DATA_TYPE
    {
        latitude,
        longitude,
        elevation,
        course,
        speed
    };

    /**
     * The timestamp of the data point
     */
    private Date timestamp;
    
    /**
     * The latitude
     */
    private double latitude;
    
    /**
     * The longitude
     */
    private double longitude;
    
    /**
     * The elevation (in meters)
     */
    private double elevation;
    
    /**
     * The speed in km/h
     */
    private double speed;
    
    /**
     * The course (in degrees)
     */
    private double course;
    
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
     * @return the latitude
     */
    public double getLatitude()
    {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude()
    {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }
    
    /**
     * @return the elevation
     */
    public double getElevation()
    {
        return elevation;
    }

    /**
     * @param elevation the elevation to set
     */
    public void setElevation(double elevation)
    {
        this.elevation = elevation;
    }

    /**
     * @return the speed
     */
    public double getSpeed()
    {
        return speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(double speed)
    {
        this.speed = speed;
    }

    /**
     * @return the course
     */
    public double getCourse()
    {
        return course;
    }

    /**
     * @param course the course to set
     */
    public void setCourse(double course)
    {
        this.course = course;
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
        bd = bd.setScale(n, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Returns a simple string representation of the wind data point
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("    ");
        sb.append(this.timestamp);
        sb.append(": ");
        sb.append(roundUpToNDecimalPlaces(this.latitude, 6));
        sb.append(" / ");
        sb.append(roundUpToNDecimalPlaces(this.longitude, 6));
        sb.append(" / ");
        sb.append(roundUpToNDecimalPlaces(this.elevation, 1));
        sb.append(" - ");
        sb.append(roundUpToNDecimalPlaces(this.speed, 2));
        sb.append(" [");
        sb.append(roundUpToNDecimalPlaces(this.course, 2));
        sb.append("]\n");
        return sb.toString();
    }
}
