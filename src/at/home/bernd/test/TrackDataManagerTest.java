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
     * 
     * @param gpxUrl     the URL of the GPX data
     * @param weatherUrl the URL of the weather data
     */
    private void testAnalyzeTrackAndWindData(String gpxUrl, String weatherUrl)
    {
        double speedThreshold = 50.0;
        int minPoints = 150;

        WeatherDataManager weatherDataManager = new WeatherDataManager();
        List<WindDataPoint> windData = weatherDataManager.parseWindData(weatherUrl);
        
        TrackDataManager trackDataManager = new TrackDataManager();
        List<Track> trackList = trackDataManager.parseTracks(gpxUrl);
        trackDataManager.addWindDataToTrackList(trackList, windData);

        List<TrackSegment> extractedTrackSegments = trackDataManager.extractTrackSegments(trackList, speedThreshold, minPoints);
        System.out.println("Found " + extractedTrackSegments.size() + " matching segments for speed threshold = " +
                           speedThreshold + ", min. points = " + minPoints);
        
        for (TrackSegment trackSegment : extractedTrackSegments)
        {
            trackDataManager.makeCharts(trackSegment);
        }
    }
    
    /**
     * Tests combining speed and wind data from given data sets.
     * 
     * @param gpxUrl     the URL of the GPX data
     * @param weatherUrl the URL of the weather data
     */
    private void testCombineTrackAndWindData(String gpxUrl, String weatherUrl)
    {
        WeatherDataManager weatherDataManager = new WeatherDataManager();
        List<WindDataPoint> windData = weatherDataManager.parseWindData(weatherUrl);

        TrackDataManager trackDataManager = new TrackDataManager();
        List<Track> trackList = trackDataManager.parseTracks(gpxUrl);
        trackDataManager.addWindDataToTrackList(trackList, windData);
    }
    
    /**
     * Starts the tests
     * @param args
     */
    public static void main(String[] args)
    {
        TrackDataManagerTest trackDataManagerTest = new TrackDataManagerTest();
        String baseUrl = "file:///H|/bwender/windsurfing/";
        String gpxUrl = baseUrl + "bernd.wender_168605310_20200619_101743.gpx";
        String weatherUrl = baseUrl + "windData_2020-06-18.htm";

        trackDataManagerTest.testAnalyzeTrackAndWindData(gpxUrl, weatherUrl);
    }
}
