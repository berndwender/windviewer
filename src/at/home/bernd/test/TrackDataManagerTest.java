package at.home.bernd.test;

import java.util.Date;
import java.util.List;

import at.home.bernd.Track;
import at.home.bernd.TrackDataManager;
import at.home.bernd.TrackSegment;
import at.home.bernd.WeatherDataManager;
import at.home.bernd.WindDataPoint;

/**
 * Tests the TrackDataManager
 */
public class TrackDataManagerTest
{
    /**
     * Tests analyzing speed and wind data from given data sets.
     */
    private void testAnalyzeTrackAndWindData()
    {
        TrackDataManager trackDataManager = new TrackDataManager();
        
        String gpxUrl = "file:///H|/bwender/windsurfing/bernd.wender_168605310_20200607_224206.gpx";
        double speedThreshold = 50.0;
        int minPoints = 150;
        List<Track> trackList = trackDataManager.parseTracks(gpxUrl);
        List<TrackSegment> extractedTrackSegments = trackDataManager.extractTrackSegments(trackList, speedThreshold, minPoints);
        System.out.println("Found " + extractedTrackSegments.size() + " matching segments for speed threshold = " +
                           speedThreshold + ", min. points = " + minPoints);

        WeatherDataManager weatherDataManager = new WeatherDataManager();
        String weatherUrl = "file:///H|/bwender/windsurfing/windData_2020-06-08.htm";
        List<WindDataPoint> windData = weatherDataManager.parseWindData(weatherUrl);
        
        for (TrackSegment trackSegment : extractedTrackSegments)
        {
            // printTrackSegment(trackSegment);
            trackDataManager.makeCharts(trackSegment);
            
            Date[] timestamps = trackSegment.getTimestamps();
            List<WindDataPoint> extractedWindData = weatherDataManager.getWindData(windData, timestamps[0], timestamps[timestamps.length - 1]);
            // printWindData(extractedWindData);
            List<WindDataPoint> interpolatedWindData = weatherDataManager.interpolateWindData(extractedWindData, timestamps);
            weatherDataManager.displayWeatherCharts(interpolatedWindData);
        }
    }
    
    /**
     * Starts the tests
     * @param args
     */
    public static void main(String[] args)
    {
        TrackDataManagerTest trackDataManagerTest = new TrackDataManagerTest();
        trackDataManagerTest.testAnalyzeTrackAndWindData();
    }
}
