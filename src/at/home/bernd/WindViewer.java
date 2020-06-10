package at.home.bernd;

import java.util.List;

/**
 * Shows wind data as a diagram
 */
public class WindViewer
{
    /**
     * Displays live weather data.
     *
     * @param url the URL of the live weather data
     */
    private void displayLiveWeatherData(String url, int nHoursBack)
    {
        WeatherDataManager weatherDataManager = new WeatherDataManager();
        List<WindDataPoint> windData = weatherDataManager.parseWindData(url);
        weatherDataManager.displayLiveWeatherCharts(windData, nHoursBack);
    }
    
    /**
     * Starts the wind viewer.
     */
    public static void main(String[] args)
    {
        WindViewer windViewer = new WindViewer();
        String liveUrl = "http://212.232.26.104/";
        int nHoursBack = 5;
        windViewer.displayLiveWeatherData(liveUrl, nHoursBack);
    }
}
