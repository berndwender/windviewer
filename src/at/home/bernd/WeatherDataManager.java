package at.home.bernd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;

import at.home.bernd.WindDataPoint.WIND_DATA_TYPE;

/**
 * Manages the processing of weather data, especially wind data
 */
public class WeatherDataManager
{
    /**
     * The date format of the weather data provider
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
    
    /**
     * Set the time zone accordingly!
     */
    static
    {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Europe/Vienna"));
    }
    
    /**
     * Parses the wind data (table in XHTML format) and returns the result as a list of wind data points
     */
    public List<WindDataPoint> parseWindData(String url)
    {
        DataConversionUtility dcu = DataConversionUtility.getInstance();

        List<WindDataPoint> windList = new ArrayList<WindDataPoint>();
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setExpandEntityReferences(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(url);
            DOMImplementation domImpl = doc.getImplementation();
            if (! domImpl.hasFeature("ElementTraversal", "1.0"))
            {
                System.err.println("The DOM implementation does not claim support for ElementTraversal.");
            }
            ElementTraversal et = (ElementTraversal) doc.getDocumentElement();
            Element firstElementChild = et.getFirstElementChild();
            ElementTraversal trElement = (ElementTraversal) firstElementChild;
            while (trElement != null)
            {
                // skip title row
                if (firstElementChild != trElement)
                {
                    WindDataPoint wdt = new WindDataPoint();
                    windList.add(wdt);
                    ElementTraversal tdElement = (ElementTraversal) trElement.getFirstElementChild();
                    int idx = 0;
                    while (tdElement != null)
                    {
                        Element td = (Element) tdElement;
                        String text = td.getTextContent();
                        if (idx == 0)
                        {
                            wdt.setTimestamp(dcu.parseDateString(text, DATE_FORMAT));
                        }
                        if (idx == 1)
                        {
                            wdt.setDirection(dcu.mapDirection(text));
                        }
                        if (idx == 2)
                        {
                            wdt.setWindSpeed(Double.parseDouble(text));
                        }
                        if (idx == 5)
                        {
                            wdt.setMaxWindSpeed(Double.parseDouble(text));
                        }
                        if (idx == 8)
                        {
                            wdt.setTemperature(dcu.parseTemperatureString(text));
                        }
                        if (idx == 9)
                        {
                            wdt.setChill(dcu.parseTemperatureString(text));
                        }
                        tdElement = (ElementTraversal) tdElement.getNextElementSibling();
                        idx++;
                    }
                }
                trElement = (ElementTraversal) trElement.getNextElementSibling();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        Collections.reverse(windList);
        return windList;
    }

    /**
     * Prints the wind data.
     * 
     * @param windData the list of wind data points
     */
    public void printWindData(List<WindDataPoint> windData)
    {
        for (WindDataPoint wdp: windData)
        {
            System.out.println(wdp);
        }
    }
    
    /**
     * Makes an array of x data for the chart
     * @param windList
     * @return
     */
    public List<Date> makeXData(List<WindDataPoint> windList)
    {
        List<Date> xData = new ArrayList<Date>();
        for (WindDataPoint wdp : windList)
        {
            xData.add(wdp.getTimestamp());
        }
        return xData;
    }
    
    /**
     * Makes an array of y data.
     * 
     * @param windList
     * @param the type of data
     * @return
     */
    public List<Number> makeYData(List<WindDataPoint> windList, WIND_DATA_TYPE dataType)
    {
        List<Number> yData = new ArrayList<Number>();
        for (int i = 0; i < windList.size(); i++)
        {
            WindDataPoint windDataPoint = windList.get(i);
            if (dataType == WIND_DATA_TYPE.windSpeed)
            {
                double windSpeed = windDataPoint.getWindSpeed();
                yData.add(windSpeed);
            }
            else if (dataType == WIND_DATA_TYPE.maxWindSpeed)
            {
                double maxWindSpeed = windDataPoint.getMaxWindSpeed();
                yData.add(maxWindSpeed);
            }
            else if (dataType == WIND_DATA_TYPE.direction)
            {
                double direction = windDataPoint.getDirection();
                yData.add(direction);
            }
            else if (dataType == WIND_DATA_TYPE.temperature)
            {
                double temperature = windDataPoint.getTemperature();
                yData.add(temperature);
            }
            else if (dataType == WIND_DATA_TYPE.chill)
            {
                double chill = windDataPoint.getChill();
                yData.add(chill);
            }
        }
        return yData;
    }
    
    /**
     * Makes a wind chart for the given list of Wind data points.
     * 
     * @param windList the list of Wind data points
     */
    public void makeWindChart(List<WindDataPoint> windList)
    {
        XYChartBuilder windChartBuilder = new XYChartBuilder();
        windChartBuilder.width(1600);
        windChartBuilder.height(400);
        windChartBuilder.title("Wind Speed");
        windChartBuilder.xAxisTitle("time");
        windChartBuilder.yAxisTitle("km / h");
        
        XYChart windChart = windChartBuilder.build();
        List<Date> xData = makeXData(windList);
        windChart.addSeries("Wind Speed", xData, makeYData(windList, WIND_DATA_TYPE.windSpeed));
        windChart.addSeries("Max Wind Speed", xData, makeYData(windList, WIND_DATA_TYPE.maxWindSpeed));
        XYStyler styler = windChart.getStyler();
        styler.setLegendPosition(LegendPosition.OutsideS);
        styler.setHasAnnotations(false);
        
        SwingWrapper<XYChart> swingWrapper = new SwingWrapper<XYChart>(windChart);
        swingWrapper.displayChart();
    }
    
    /**
     * Makes a wind direction chart for the given list of Wind data points.
     * 
     * @param windList the list of Wind data points
     */
    public void makeWindDirectionChart(List<WindDataPoint> windList)
    {
        XYChartBuilder windChartBuilder = new XYChartBuilder();
        windChartBuilder.width(1600);
        windChartBuilder.height(400);
        windChartBuilder.title("Wind Direction");
        windChartBuilder.xAxisTitle("time");
        windChartBuilder.yAxisTitle("Degrees");
        
        XYChart windDirectionChart = windChartBuilder.build();
        List<Date> xData = makeXData(windList);
        windDirectionChart.addSeries("Wind Direction", xData, makeYData(windList, WIND_DATA_TYPE.direction));
        XYStyler styler = windDirectionChart.getStyler();
        styler.setLegendPosition(LegendPosition.OutsideS);
        styler.setHasAnnotations(false);
        
        SwingWrapper<XYChart> swingWrapper = new SwingWrapper<XYChart>(windDirectionChart);
        swingWrapper.displayChart();
    }
    
    /**
     * Makes a temperature chart for the given list of Wind data points.
     * 
     * @param windList the list of Wind data points
     */
    public void makeTemperatureChart(List<WindDataPoint> windList)
    {
        XYChartBuilder temperatureChartBuilder = new XYChartBuilder();
        temperatureChartBuilder.width(1600);
        temperatureChartBuilder.height(400);
        temperatureChartBuilder.title("Air Temperature");
        temperatureChartBuilder.xAxisTitle("time");
        temperatureChartBuilder.yAxisTitle("C");

        XYChart temperatureChart = temperatureChartBuilder.build();
        List<Date> xData = makeXData(windList);
        temperatureChart.addSeries("Temperature", xData, makeYData(windList, WIND_DATA_TYPE.temperature));
        temperatureChart.addSeries("Chill", xData, makeYData(windList, WIND_DATA_TYPE.chill));
        XYStyler styler = temperatureChart.getStyler();
        styler.setLegendPosition(LegendPosition.OutsideS);
        styler.setHasAnnotations(false);
        
        SwingWrapper<XYChart> swingWrapper = new SwingWrapper<XYChart>(temperatureChart);
        swingWrapper.displayChart();
    }
    
    /**
     * Returns a sublist of wind data from a given timestamp to a given timestamp.
     * 
     * @param windData the original list
     * @param from the "from" timestamp
     * @param to the "to" timestamp
     * @return the sublist
     */
    public List<WindDataPoint> getWindData(List<WindDataPoint> windData, Date from, Date to)
    {
        List<WindDataPoint> result = new ArrayList<WindDataPoint>();
        WindDataPoint firstWdp = null;
        boolean isFirstWdpAdded = false;
        boolean isLastWdpAdded = false;
        for (WindDataPoint wdp : windData)
        {
            Date ts = wdp.getTimestamp();
            if (from.before(ts) && ! isFirstWdpAdded)
            {
                if (firstWdp != null)
                {
                    result.add(firstWdp);
                    isFirstWdpAdded = true;
                }
            }
            else
            {
                firstWdp = wdp;
            }
            if (isLastWdpAdded)
            {
                if (result.size() > 2)
                {
                    break;
                }
            }
            if (from.before(ts) && to.after(ts))
            {
                result.add(wdp);
            }
            else if (result.size() > 0)
            {
                result.add(wdp);
                isLastWdpAdded = true;
            }
        }
        return result;
    }

    /**
     * Displays the weather charts based on the given wind data.
     * 
     * @param windData the wind data
     */
    public void displayWeatherCharts(List<WindDataPoint> windData)
    {
        displayWeatherCharts(windData, (Date) null, (Date) null);
    }
    
    /**
     * Displays live weather charts (i.e. from n hours back to now).
     * 
     * @param windData the wind data
     * @param nHoursBack from date is n hours back from now
     */
    public void displayLiveWeatherCharts(List<WindDataPoint> windData, int nHoursBack)
    {
        GregorianCalendar fromDate = new GregorianCalendar();
        fromDate.add(Calendar.HOUR, -3);
        GregorianCalendar toDate = new GregorianCalendar();
        displayWeatherCharts(windData, fromDate.getTime(), toDate.getTime());
    }
    
    /**
     * Displays the weather charts based on weather data from the given URL.
     * 
     * @param windData the wind data
     * @param from     the "from" timestamp
     * @param to       the "to" timestamp
     */
    public void displayWeatherCharts(List<WindDataPoint> windData, String from, String to)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try
        {
            Date fromTimeStamp = sdf.parse(from);
            Date toTimeStamp = sdf.parse(to);
            displayWeatherCharts(windData, fromTimeStamp, toTimeStamp);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Displays the weather charts based on weather data from the given URL.
     * 
     * @param windData the wind data
     * @param from     the "from" timestamp
     * @param to       the "to" timestamp
     */
    public void displayWeatherCharts(List<WindDataPoint> windData, Date from, Date to)
    {
        if (from != null && to != null)
        {
            windData = this.getWindData(windData, from, to);
        }
        makeWindChart(windData);
        makeWindDirectionChart(windData);
        makeTemperatureChart(windData);
    }

    /**
     * Creates a list of empty wind data points based on the given timestamps
     * 
     * @param timstamps the array of timestamps to be used for the wind data points
     * @return the list of empty wind data points
     */
    private List<WindDataPoint> createEmptyWindDataPoints(Date[] timstamps)
    {
        List<WindDataPoint> emptyWindDataPoints = new ArrayList<WindDataPoint>();
        for (Date timestamp : timstamps)
        {
            WindDataPoint wdpt = new WindDataPoint();
            wdpt.setTimestamp(timestamp);
            emptyWindDataPoints.add(wdpt);
        }
        return emptyWindDataPoints;
    }
    
    
    /**
     * Creates an interpolation function for the given wind data type based on the given wind data points.
     * 
     * @param windDataPoints the wind data points
     * @return the interpolation function
     */
    private PolynomialSplineFunction createInterpolationFunction(List<WindDataPoint> windDataPoints,
                                                                 WIND_DATA_TYPE windDataType)
    {
        int nDataPoints = windDataPoints.size();
        double[] xData = new double[nDataPoints];
        double[] yData = new double[nDataPoints];
        for (int i = 0; i < nDataPoints; i++)
        {
            WindDataPoint windDataPoint = windDataPoints.get(i);
            xData[i] = (double) windDataPoint.getTimestamp().getTime();
            switch (windDataType)
            {
                case windSpeed:
                    yData[i] = windDataPoint.getWindSpeed();
                    break;
                case maxWindSpeed:
                    yData[i] = windDataPoint.getMaxWindSpeed();
                    break;
                case direction:
                    yData[i] = windDataPoint.getDirection();
                    break;
                case temperature:
                    yData[i] = windDataPoint.getTemperature();
                    break;
                case chill:
                    yData[i] = windDataPoint.getChill();
                    break;
                default:
                    break;
            }
        }
        SplineInterpolator splineInterpolator = new SplineInterpolator();
        PolynomialSplineFunction splineFunction = splineInterpolator.interpolate(xData, yData);
        return splineFunction;
    }


    /**
     * Populates the interpolated wind data based on the given spline funtion.
     * 
     * @param interpolatedWindData the wind data to be populated
     * @param minWindTimestamp     the minimum timestamp of available wind data
     * @param maxWindTimestamp     the maximum timestamp of available wind data
     * @param windDataType         the type of wind data
     * @param spline               the spline function responsible for the interpolation
     */
    private void populateInterpolatedWindData(List<WindDataPoint> interpolatedWindData,
                                              Date minWindTimestamp,
                                              Date maxWindTimestamp,
                                              WIND_DATA_TYPE windDataType,
                                              PolynomialSplineFunction spline)
    {
        for (WindDataPoint windDataPoint : interpolatedWindData)
        {
            double minWTst = (double) minWindTimestamp.getTime();
            double maxWTst = (double) maxWindTimestamp.getTime();
            double timestampAsLong = (double) windDataPoint.getTimestamp().getTime();
            if (timestampAsLong < minWTst || timestampAsLong > maxWTst)
            {
                continue;
            }
            switch (windDataType)
            {
                case windSpeed:
                    windDataPoint.setWindSpeed(spline.value(timestampAsLong));
                    break;
                case maxWindSpeed:
                    windDataPoint.setMaxWindSpeed(spline.value(timestampAsLong));
                    break;
                case direction:
                    windDataPoint.setDirection(spline.value(timestampAsLong));
                    break;
                case temperature:
                    windDataPoint.setTemperature(spline.value(timestampAsLong));
                    break;
                case chill:
                    windDataPoint.setChill(spline.value(timestampAsLong));
                    break;
                default:
                    break;
            }
        }
    }
    
    /**
     * Returns a list of interpolated wind data points.
     * 
     * @param windDataPoints the original wind data points
     * @param timstamps      the timestamps to be used for the resulting list
     * @return
     */
    public List<WindDataPoint> interpolateWindData(List<WindDataPoint> windDataPoints, Date[] timstamps)
    {
        Date minWindTimestamp = windDataPoints.get(0).getTimestamp();
        Date maxWindTimestamp = windDataPoints.get(windDataPoints.size() - 1).getTimestamp();
        List<WindDataPoint> interpolatedWindData = createEmptyWindDataPoints(timstamps);
        populateInterpolatedWindData(interpolatedWindData,
                                     minWindTimestamp,
                                     maxWindTimestamp,
                                     WIND_DATA_TYPE.windSpeed,
                                     createInterpolationFunction(windDataPoints, WIND_DATA_TYPE.windSpeed));
        populateInterpolatedWindData(interpolatedWindData,
                                     minWindTimestamp,
                                     maxWindTimestamp,
                                     WIND_DATA_TYPE.maxWindSpeed,
                                     createInterpolationFunction(windDataPoints, WIND_DATA_TYPE.maxWindSpeed));
        populateInterpolatedWindData(interpolatedWindData,
                                     minWindTimestamp,
                                     maxWindTimestamp,
                                     WIND_DATA_TYPE.direction,
                                     createInterpolationFunction(windDataPoints, WIND_DATA_TYPE.direction));
        populateInterpolatedWindData(interpolatedWindData,
                                     minWindTimestamp,
                                     maxWindTimestamp,
                                     WIND_DATA_TYPE.temperature,
                                     createInterpolationFunction(windDataPoints, WIND_DATA_TYPE.temperature));
        populateInterpolatedWindData(interpolatedWindData,
                                     minWindTimestamp,
                                     maxWindTimestamp,
                                     WIND_DATA_TYPE.chill,
                                     createInterpolationFunction(windDataPoints, WIND_DATA_TYPE.chill));
        return interpolatedWindData;
    }
}
