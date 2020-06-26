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
        speed,
        windDirection,
        windSpeed,
        maxWindSpeed,
        relCourse
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
     * The wind direction in degrees: N = 0 (=360); E = 90; S = 180; W = 270
     */
    private double windDirection;
    
    /**
     * The wind speed in km/h+
     */
    private double windSpeed;
    
    /**
     * The maximum wind speed in km/h
     */
    private double maxWindSpeed;
    
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
     * @return the wind direction
     */
    public double getWindDirection()
    {
        return windDirection;
    }

    /**
     * @param windDirection the wind direction to set
     */
    public void setWindDirection(double windDirection)
    {
        this.windDirection = windDirection;
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
     * Returns a simple string representation of the wind data point
     */
    public String toString()
    {
        DataConversionUtility dcu = DataConversionUtility.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("    ");
        sb.append(this.timestamp);
        sb.append(": ");
        sb.append(dcu.roundUpToNDecimalPlaces(this.latitude, 6));
        sb.append(" / ");
        sb.append(dcu.roundUpToNDecimalPlaces(this.longitude, 6));
        sb.append(" / elev.: ");
        sb.append(dcu.roundUpToNDecimalPlaces(this.elevation, 1));
        sb.append(" / speed: ");
        sb.append(dcu.roundUpToNDecimalPlaces(this.speed, 2));
        sb.append(" [");
        sb.append(dcu.roundUpToNDecimalPlaces(this.course, 2));
        sb.append("] / wind speed: ");
        sb.append(dcu.roundUpToNDecimalPlaces(this.windSpeed, 2));
        sb.append(" - ");
        sb.append(dcu.roundUpToNDecimalPlaces(this.maxWindSpeed, 2));
        sb.append(" [");
        sb.append(dcu.roundUpToNDecimalPlaces(this.windDirection, 2));
        sb.append("]\n");
        return sb.toString();
    }
}
