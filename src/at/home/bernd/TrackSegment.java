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
     * Returns the size of the track segment (i.e. the number of track points).
     * 
     * @return the size of the track segment
     */
    public int size()
    {
        return this.trackPoints.size();
    }
    
    /**
     * Extracts a list of track sub segments of this segment where the top speed is greater than the given threshold
     * 
     * @param speedThreshold the speed of all points of a subsegment must be greater than this threshold
     * @param minPoints      the minimum number of points of a subsegment
     * @return               the list of matching track segments
     */
    public List<TrackSegment> extractByTopSpeed(double speedThreshold, int minPoints)
    {
        List<TrackSegment> extractedTrackSegments = new ArrayList<TrackSegment>();
        TrackSegment trackSegment = new TrackSegment();
        for (TrackPoint trackPoint : this.trackPoints)
        {
            double speed = trackPoint.getSpeed();
            if (speed > speedThreshold)
            {
                trackSegment.addTrackPoint(trackPoint);
            }
            else
            {
                if (trackSegment.size() > minPoints)
                {
                    extractedTrackSegments.add(trackSegment);
                    trackSegment = new TrackSegment();
                }
                else if (trackSegment.size() > 0)
                {
                    trackSegment = new TrackSegment();
                }
            }
        }
        // check last segment
        if (trackSegment.size() > minPoints)
        {
            extractedTrackSegments.add(trackSegment);
        }
        return extractedTrackSegments;
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
