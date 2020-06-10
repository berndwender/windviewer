package at.home.bernd.test;

import java.util.Date;

import at.home.bernd.DataConversionUtility;
import at.home.bernd.TrackDataManager;
import at.home.bernd.WeatherDataManager;

/**
 * Tests the DataConversionUtility
 */
public class DataConversionUtilityTest
{
    /**
     * Tests parsing of date strings
     */
    private void testParseDateString()
    {
        DataConversionUtility dcu = DataConversionUtility.getInstance();
        
        String timeStampFromWeatherData = "11:42:00 30.05.2020"; // in CEST
        Date date = dcu.parseDateString(timeStampFromWeatherData, WeatherDataManager.DATE_FORMAT);
        System.out.println(date);
        
        String timeStampFromGpxData = "2020-05-30T09:42:00Z"; // in UTC
        date = dcu.parseDateString(timeStampFromGpxData, TrackDataManager.DATE_FORMAT);
        System.out.println(date);
        
        String timeStampFromGpxDataAlt = "2020-05-30T09:42:00.000Z"; // in UTC
        date = dcu.parseDateString(timeStampFromGpxDataAlt, TrackDataManager.ALT_DATE_FORMAT);
        System.out.println(date);
    }
    
    /**
     * Tests parsing temperature strings
     */
    private void testParseTemperatureString()
    {
        DataConversionUtility dcu = DataConversionUtility.getInstance();
        double temperature = dcu.parseTemperatureString(" 28.3 C");
        System.out.println(temperature);
        
        temperature = dcu.parseTemperatureString("28.4 C");
        System.out.println(temperature);
        
        temperature = dcu.parseTemperatureString("28.5");
        System.out.println(temperature);
    }
    
    /**
     * Tests the mapping of wind directions.
     */
    private void testMapDirection()
    {
        DataConversionUtility dcu = DataConversionUtility.getInstance();
        
        String[] germanTerms =  { "N", "NNO", "NO", "ONO", "O", "OSO", "SO", "SSO", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" };
        String[] englishTerms = { "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" };

        for (String germanTerm: germanTerms)
        {
            System.out.println(germanTerm + " = " + dcu.mapDirection(germanTerm));
        }
        System.out.println();
        for (String englishTerm: englishTerms)
        {
            System.out.println(englishTerm + " = " + dcu.mapDirection(englishTerm));
        }
    }
    
    /**
     * Starts the tests
     */
    public static void main(String[] args)
    {
        DataConversionUtilityTest dataConversionUtilityTest = new DataConversionUtilityTest();
        dataConversionUtilityTest.testParseDateString();
        dataConversionUtilityTest.testParseTemperatureString();
        dataConversionUtilityTest.testMapDirection();
    }
}
