package at.home.bernd;

import java.util.ArrayList;
import java.util.List;

/**
 * A track segment is a collection of track points as defined in the GPS Exchange Format (GPX).
 */
public class TrackSegment
{
    /**
     * The list of track points
     */
    private List<TrackPoint> trackPoints = new ArrayList<TrackPoint>();

    public void addTrackPoint(TrackPoint trackPoint)
    {
        this.trackPoints.add(trackPoint);
    }
    
    /**
     * @return the trackPoints
     */
    public List<TrackPoint> getTrackPoints()
    {
        return trackPoints;
    }

    /**
     * @param trackPoints the trackPoints to set
     */
    public void setTrackPoints(List<TrackPoint> trackPoints)
    {
        this.trackPoints = trackPoints;
    }
    
    /**
     * Returns a simple string representation of this track
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("  Track Segment:\n");
        for (TrackPoint trackPoint : trackPoints)
        {
            sb.append(trackPoint.toString());
        }
        return sb.toString();
    }
}
