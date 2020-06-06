package at.home.bernd;

import java.util.ArrayList;
import java.util.List;

/**
 * A track is a collection of track segments as defined in the GPS Exchange Format (GPX).
 */
public class Track
{
    /**
     * The name of the track
     */
    private String name;
    
    /**
     * The track segments
     */
    private List<TrackSegment> trackSegments = new ArrayList<TrackSegment>();
    
    public void addTrackSegment(TrackSegment trackSegment)
    {
        this.trackSegments.add(trackSegment);
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the trackSegments
     */
    public List<TrackSegment> getTrackSegments()
    {
        return trackSegments;
    }

    /**
     * @param trackSegments the trackSegments to set
     */
    public void setTrackSegments(List<TrackSegment> trackSegments)
    {
        this.trackSegments = trackSegments;
    }
    
    /**
     * Returns a simple string representation of this track
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Track: " + getName() + "\n");
        for (TrackSegment trackSegment : trackSegments)
        {
            sb.append(trackSegment.toString());
        }
        return sb.toString();
    }
}
