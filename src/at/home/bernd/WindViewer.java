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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.home.bernd.TrackPoint.TRACK_DATA_TYPE;
import at.home.bernd.WindDataPoint.WIND_DATA_TYPE;

/**
 * Shows wind data as a diagram
 */
public class WindViewer
{
    /**
     * Parses the wind data (table in XHTML format) and returns the result as a list of wind data points
     */
    private List<WindDataPoint> parseWindData(String url)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Vienna"));

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
                            wdt.setTimestamp(parseDateString(text, simpleDateFormat));
                        }
                        if (idx == 1)
                        {
                            wdt.setDirection(text);
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
                            wdt.setTemperature(parseTemperatureString(text));
                        }
                        if (idx == 9)
                        {
                            wdt.setChill(parseTemperatureString(text));
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
     * Parses the track data (table in GPX format) and returns the result as a list of track data points
     */
    private List<Track> parseTracks(String url)
    {
        List<Track> trackList = new ArrayList<Track>();
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setExpandEntityReferences(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(url);
            Element documentElement = doc.getDocumentElement();
            NodeList tracks = documentElement.getElementsByTagName("trk");
            int nTracks = tracks.getLength();
            for (int i = 0; i < nTracks; i++)
            {
                Node trackNode = tracks.item(i);
                Track track = parseTrack(trackNode);
                trackList.add(track);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return trackList;
    }

    private Track parseTrack(Node trackNode)
    {
        Track track = new Track();
        NodeList trackInfoItems = trackNode.getChildNodes();
        for (int i = 0; i < trackInfoItems.getLength(); i++)
        {
            Node trackInfoItem = trackInfoItems.item(i);
            String nodeName = trackInfoItem.getNodeName();
            if ("name".equals(nodeName))
            {
                track.setName(trackInfoItem.getTextContent());
            }
            else if ("trkseg".equals(nodeName))
            {
                TrackSegment trackSegment = parseTrackSegment(trackInfoItem);
                track.addTrackSegment(trackSegment);
            }
        }
        return track;
    }

    /**
     * Parses a single track segment.
     * 
     * @param trackInfoItem
     * @return 
     * @return the list of track points
     */
    private TrackSegment parseTrackSegment(Node trackInfoItem)
    {
        TrackSegment trackSegment = new TrackSegment();
        NodeList trackPointNodes = trackInfoItem.getChildNodes();
        int nTrackPoints = trackPointNodes.getLength();
        for (int i = 0; i < nTrackPoints; i++)
        {
            Node trackPointNode = trackPointNodes.item(i);
            if (trackPointNode != null && "trkpt".equals(trackPointNode.getNodeName()))
            {
                trackSegment.addTrackPoint(parseTrackPoint(trackPointNode));
            }
        }
        return trackSegment;
    }

    /**
     * Parses a single track point.
     * 
     * @param trackPointNode the track point node
     * @return the track data point
     */
    private TrackPoint parseTrackPoint(Node trackPointNode)
    {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        TimeZone utc = TimeZone.getTimeZone("UTC");
        sdf1.setTimeZone(utc);
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf2.setTimeZone(utc);
        
        TrackPoint trackDataPoint = new TrackPoint();
        NamedNodeMap attrs = trackPointNode.getAttributes();
        
        Node latNode = attrs.getNamedItem("lat");
        trackDataPoint.setLatitude(Double.parseDouble(latNode.getTextContent()));

        Node lonNode = attrs.getNamedItem("lon");
        trackDataPoint.setLongitude(Double.parseDouble(lonNode.getTextContent()));
        
        NodeList valNodes = trackPointNode.getChildNodes();
        for (int i = 0; i < valNodes.getLength(); i++)
        {
            Node valNode = valNodes.item(i);
            if ("ele".equals(valNode.getNodeName()))
            {
                trackDataPoint.setElevation(Double.parseDouble(valNode.getTextContent()));
            }
            else if ("time".equals(valNode.getNodeName()))
            {
                String dateString = valNode.getTextContent();
                Date timeStamp = null;
                if (dateString.length() <= 20)
                {
                    timeStamp = parseDateString(dateString, sdf1);
                }
                else
                {
                    timeStamp = parseDateString(dateString, sdf2);
                }
                trackDataPoint.setTimestamp(timeStamp);
            }
            else if ("course".equals(valNode.getNodeName()))
            {
                trackDataPoint.setCourse(Double.parseDouble(valNode.getTextContent()));
            }
            else if ("speed".equals(valNode.getNodeName()))
            {
                double speedInMetersPerSeconds = Double.parseDouble(valNode.getTextContent());
                trackDataPoint.setSpeed(speedInMetersPerSeconds * 3.6);
            }
        }
        return trackDataPoint;
    }
    
    /**
     * Prints the wind data.
     * 
     * @param windData the list of wind data points
     */
    private void printTrackSegment(TrackSegment trackSegment)
    {
        System.out.println(trackSegment.toString());
    }

    /**
     * Makes an array of x data for the chart
     * @param windList
     * @return
     */
    public List<Date> makeXData(TrackSegment trackSegment)
    {
        List<Date> xData = new ArrayList<Date>();
        for (TrackPoint tp : trackSegment.getTrackPoints())
        {
            xData.add(tp.getTimestamp());
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
    public List<Number> makeYData(TrackSegment trackSegment, TRACK_DATA_TYPE dataType)
    {
        List<Number> yData = new ArrayList<Number>();
        List<TrackPoint> trackPoints = trackSegment.getTrackPoints();
        for (int i = 0; i < trackPoints.size(); i++)
        {
            if (dataType == TRACK_DATA_TYPE.speed)
            {
                yData.add(trackPoints.get(i).getSpeed());
            }
            else if (dataType == TRACK_DATA_TYPE.course)
            {
                yData.add(trackPoints.get(i).getCourse());
            }
        }
        return yData;
    }
    
    /**
     * Prints the wind data.
     * 
     * @param windData the list of wind data points
     */
    private void printWindData(List<WindDataPoint> windData)
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
        windChartBuilder.title("Wind");
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
     * Makes a temperature chart for the given list of Wind data points.
     * 
     * @param windList the list of Wind data points
     */
    public void makeTemperatureChart(List<WindDataPoint> windList)
    {
        XYChartBuilder temperatureChartBuilder = new XYChartBuilder();
        temperatureChartBuilder.width(1600);
        temperatureChartBuilder.height(400);
        temperatureChartBuilder.title("Temperature");
        temperatureChartBuilder.xAxisTitle("time");
        temperatureChartBuilder.yAxisTitle("Degrees Centigrade");

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
     * Makes a speed chart.
     * 
     * @param windList the wind list
     */
    public void makeCharts(TrackSegment trackSegment)
    {
        XYChartBuilder speedChartBuilder = new XYChartBuilder();
        speedChartBuilder.width(1600);
        speedChartBuilder.height(400);
        speedChartBuilder.title("Speed");
        speedChartBuilder.xAxisTitle("time");
        speedChartBuilder.yAxisTitle("km / h");
        
        XYChart speedChart = speedChartBuilder.build();
        List<Date> xData = makeXData(trackSegment);
        List<Number> speedData = makeYData(trackSegment, TRACK_DATA_TYPE.speed);
        speedChart.addSeries("Speed", xData, speedData);
        XYStyler speedChartStyler = speedChart.getStyler();
        speedChartStyler.setLegendPosition(LegendPosition.OutsideS);
        speedChartStyler.setHasAnnotations(false);
        
        XYChartBuilder courseChartBuilder = new XYChartBuilder();
        courseChartBuilder.width(1600);
        courseChartBuilder.height(400);
        courseChartBuilder.title("Course");
        courseChartBuilder.xAxisTitle("time");
        courseChartBuilder.yAxisTitle("Direction");
        
        XYChart courseChart = courseChartBuilder.build();
        List<Number> courseData = makeYData(trackSegment, TRACK_DATA_TYPE.course);
        courseChart.addSeries("Course", xData, courseData);
        XYStyler courseChartStyler = courseChart.getStyler();
        courseChartStyler.setLegendPosition(LegendPosition.OutsideS);
        courseChartStyler.setHasAnnotations(false);
        
        SwingWrapper<XYChart> swingWrapper = new SwingWrapper<XYChart>(speedChart);
        swingWrapper.displayChart();
        
        swingWrapper = new SwingWrapper<XYChart>(courseChart);
        swingWrapper.displayChart();
    }
    
    /**
     * Parses a timestamp and returns its value as a Date.
     * 
     * @param timeStamp the timestamp
     * @return the timestamp as Date
     */
    private Date parseDateString(String timeStamp, SimpleDateFormat simpleDateFormat)
    {
        Date date = null;
        try
        {
            date = simpleDateFormat.parse(timeStamp);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return date;
    }
    
    private double parseTemperatureString(String temperature)
    {
        temperature = temperature.strip();
        int indexOfC = temperature.indexOf('C');
        temperature = temperature.substring(0, indexOfC);
        return Double.parseDouble(temperature);
    }
    
    /**
     * Returns the time representation of the given date.
     * 
     * @param date the given date
     * @return the time representation (hours, minutes)
     */
    private String timeString(Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Vienna"));
        String timeString = sdf.format(date);
        return timeString;
    }
    
    /**
     * Returns the date representation of the given date.
     * 
     * @param date the given date
     * @return the date representation (year-month-day)
     */
    private String dateString(Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Vienna"));
        String dateString = sdf.format(date);
        return dateString;
    }
    
    /**
     * Returns a sublist of track data from a given timestamp to a given timestamp.
     * 
     * @param trackData the original list
     * @param from the "from" timestamp
     * @param to the "to" timestamp
     * @return the sublist
     */
    List<TrackPoint> getTrackData(List<TrackPoint> trackData, Date from, Date to)
    {
        List<TrackPoint> result = new ArrayList<TrackPoint>();
        for (TrackPoint tp : trackData)
        {
            Date ts = tp.getTimestamp();
            if (from.before(ts) && to.after(ts))
            {
                result.add(tp);
            }
        }
        return result;
    }
    
    /**
     * Displays speed charts based on track data from the given URL.
     * 
     * @param trackList the track list
     */
    private void displaySpeedCharts(List<Track> trackList)
    {
        displaySpeedCharts(trackList, (Date) null, (Date) null);
    }
    
    /**
     * Displays speed charts based on track data from the given URL.
     * 
     * @param trackList the track list
     * @param from      the "from" timestamp in ISO format (GMT)
     * @param to        the "to" timestamp in ISO format (GMT)
     */
    private void displaySpeedCharts(List<Track> trackList, String from, String to)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try
        {
            Date fromTimeStamp = sdf.parse(from);
            Date toTimeStamp = sdf.parse(to);
            displaySpeedCharts(trackList, fromTimeStamp, toTimeStamp);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Displays a speed chart based on track data from the given track list.
     * 
     * @param trackList the track list
     * @param from      the "from" timestamp
     * @param to        the "to" timestamp
     */
    private void displaySpeedCharts(List<Track> trackList, Date from, Date to)
    {
        for (Track track : trackList)
        {
            List<TrackSegment> trackSegments = track.getTrackSegments();
            for (TrackSegment trackSegment : trackSegments)
            {
                List<TrackPoint> trackPoints = trackSegment.getTrackPoints();
                if (from == null || to == null)
                {
                    makeCharts(trackSegment);
                }
                else
                {
                    List<TrackPoint> filteredTrackPoints = getTrackData(trackPoints, from, to);
                    if (filteredTrackPoints.size() > 0)
                    {
                        TrackSegment filteredTrackSegment = new TrackSegment();
                        filteredTrackSegment.setTrackPoints(filteredTrackPoints);
                        makeCharts(filteredTrackSegment);
                    }
                }
            }
        }
    }
    
    /**
     * Extracts a list of track segments from the given track list. Segments are extracted if the speed values of all the points of
     * the segment is grater than the given threshold and the number of points is greater than the given minimum.
     * 
     * @param trackList      the list of tracks
     * @param speedThreshold the speed of all points of a subsegment must be greater than this threshold
     * @param minPoints      the minimum number of points of a subsegment
     * @return               the number of segments to be displayed
     */
    private List<TrackSegment> extractTrackSegments(List<Track> trackList, double speedThreshold, int minPoints)
    {
        List<TrackSegment> result = new ArrayList<TrackSegment>();
        for (Track track : trackList)
        {
            List<TrackSegment> trackSegments = track.getTrackSegments();
            for (TrackSegment trackSegment : trackSegments)
            {
                List<TrackSegment> extractedTrackSegments = trackSegment.extractByTopSpeed(speedThreshold, minPoints);
                for (TrackSegment extractedTrackSegment : extractedTrackSegments)
                {
                    result.add(extractedTrackSegment);
                }
            }
        }
        return result;
    }
    
    /**
     * Returns a sublist of wind data from a given timestamp to a given timestamp.
     * 
     * @param windData the original list
     * @param from the "from" timestamp
     * @param to the "to" timestamp
     * @return the sublist
     */
    private List<WindDataPoint> getWindData(List<WindDataPoint> windData, Date from, Date to)
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
                result.add(firstWdp);
                isFirstWdpAdded = true;
            }
            else
            {
                firstWdp = wdp;
            }
            if (isLastWdpAdded)
            {
                break;
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
    private void displayWeatherCharts(List<WindDataPoint> windData)
    {
        displayWeatherCharts(windData, (Date) null, (Date) null);
    }
    
    /**
     * Displays live weather charts (i.e. from n hours back to now).
     * 
     * @param windData the wind data
     * @param nHoursBack from date is n hours back from now
     */
    private void displayLiveWeatherCharts(List<WindDataPoint> windData, int nHoursBack)
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
    private void displayWeatherCharts(List<WindDataPoint> windData, String from, String to)
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
    private void displayWeatherCharts(List<WindDataPoint> windData, Date from, Date to)
    {
        if (from != null && to != null)
        {
            windData = this.getWindData(windData, from, to);
        }
        makeWindChart(windData);
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
     * @param windDataType         the type of wind data
     * @param spline               the spline function responsible for the interpolation
     */
    private void populateInterpolatedWindData(List<WindDataPoint> interpolatedWindData,
                                              WIND_DATA_TYPE windDataType,
                                              PolynomialSplineFunction spline)
    {
        for (WindDataPoint windDataPoint : interpolatedWindData)
        {
            double timestampAsLong = (double) windDataPoint.getTimestamp().getTime();
            switch (windDataType)
            {
                case windSpeed:
                    windDataPoint.setWindSpeed(spline.value(timestampAsLong));
                    break;
                case maxWindSpeed:
                    windDataPoint.setMaxWindSpeed(spline.value(timestampAsLong));
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
    private List<WindDataPoint> interpolateWindData(List<WindDataPoint> windDataPoints, Date[] timstamps)
    {
        List<WindDataPoint> interpolatedWindData = createEmptyWindDataPoints(timstamps);
        populateInterpolatedWindData(interpolatedWindData,
                                     WIND_DATA_TYPE.windSpeed,
                                     createInterpolationFunction(windDataPoints, WIND_DATA_TYPE.windSpeed));
        populateInterpolatedWindData(interpolatedWindData,
                                     WIND_DATA_TYPE.maxWindSpeed,
                                     createInterpolationFunction(windDataPoints, WIND_DATA_TYPE.maxWindSpeed));
        populateInterpolatedWindData(interpolatedWindData,
                                     WIND_DATA_TYPE.temperature,
                                     createInterpolationFunction(windDataPoints, WIND_DATA_TYPE.temperature));
        populateInterpolatedWindData(interpolatedWindData,
                                     WIND_DATA_TYPE.chill,
                                     createInterpolationFunction(windDataPoints, WIND_DATA_TYPE.chill));
        return interpolatedWindData;
    }
    
    /**
     * Tests analyzing speed and wind data from given data sets.
     */
    private void testAnalyzeTrackAndWindData()
    {
        String gpxUrl = "file:///H|/bwender/windsurfing/bernd.wender_168605310_20200607_224206.gpx";
        double speedThreshold = 32.0;
        int minPoints = 1000;
        List<Track> trackList = parseTracks(gpxUrl);
        List<TrackSegment> extractedTrackSegments = extractTrackSegments(trackList, speedThreshold, minPoints);
        System.out.println("Found " + extractedTrackSegments.size() + " matching segments for speed threshold = " +
                           speedThreshold + ", min. points = " + minPoints);

        String weatherUrl = "file:///H|/bwender/windsurfing/windData_2020-06-08.htm";
        List<WindDataPoint> windData = parseWindData(weatherUrl);
        
        for (TrackSegment trackSegment : extractedTrackSegments)
        {
            // printTrackSegment(trackSegment);
            this.makeCharts(trackSegment);
            
            Date[] timestamps = trackSegment.getTimestamps();
            List<WindDataPoint> extractedWindData = getWindData(windData, timestamps[0], timestamps[timestamps.length - 1]);
            // printWindData(extractedWindData);
            List<WindDataPoint> interpolatedWindData = interpolateWindData(extractedWindData, timestamps);
            displayWeatherCharts(interpolatedWindData);
        }
    }
    
    /**
     * Tests printing live weather data
     */
    private void testPrintLiveWeatherData()
    {
        String url = "http://212.232.26.104/";
        int nHoursBack = 3;
        List<WindDataPoint> windData = parseWindData(url);
        displayLiveWeatherCharts(windData, nHoursBack);
    }
    
    /**
     * Starts the wind viewer
     */
    public static void main(String[] args)
    {
        WindViewer windViewer = new WindViewer();
        // windViewer.testPrintLiveWeatherData();
        
        windViewer.testAnalyzeTrackAndWindData();
        
        /*
        Date timeStamp = windViewer.parseDateString("11:42:00 30.05.2020");
        System.out.println(windViewer.timeString(timeStamp));
        System.out.println(windViewer.dateString(timeStamp));
        System.out.println(windViewer.parseTemperatureString(" 14.3 C"));
        */
    }
}
