package at.home.bernd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.home.bernd.TrackPoint.TRACK_DATA_TYPE;
import at.home.bernd.WindDataPoint.WIND_DATA_TYPE;

/**
 * Manages the processing of track data.
 */
public class TrackDataManager
{
    /**
     * The date format of the GPX data
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * The alternative date format of the GPX data
     */
    public static final SimpleDateFormat ALT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * Set the time zone (UTC).
     */
    static
    {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        DATE_FORMAT.setTimeZone(utc);
        ALT_DATE_FORMAT.setTimeZone(utc);
    }
    
    /**
     * Parses the track data (table in GPX format) and returns the result as a list of track data points
     * 
     * @param url the URL of the track data in GPX format
     * @return    the track data
     */
    public List<Track> parseTracks(String url)
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

    /**
     * Parses the details of a single track.
     * 
     * @param trackNode the corresponding track node
     * @return          the track
     */
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
     * @param trackInfoNode the corresponding node
     * @return the track segment
     */
    private TrackSegment parseTrackSegment(Node trackInfoNode)
    {
        TrackSegment trackSegment = new TrackSegment();
        NodeList trackPointNodes = trackInfoNode.getChildNodes();
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
     * @param trackPointNode the corresponding node
     * @return the track point
     */
    private TrackPoint parseTrackPoint(Node trackPointNode)
    {
        DataConversionUtility dcu = DataConversionUtility.getInstance();
        
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
                    timeStamp = dcu.parseDateString(dateString, DATE_FORMAT);
                }
                else
                {
                    timeStamp = dcu.parseDateString(dateString, ALT_DATE_FORMAT);
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
     * Prints the given track segment.
     * 
     * @param trackSegment the track segment
     */
    public void printTrackSegment(TrackSegment trackSegment)
    {
        System.out.println(trackSegment.toString());
    }

    /**
     * Makes an array of x data (= timestamps) for an XY chart.
     * 
     * @param trackSegment the track segment
     * @return             the X data
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
     * Makes an array of y data (= double values) for an XY chart..
     * 
     * @param trackSegment the track segment
     * @param dataType     the type of data to be displayed in the chart
     * @return             the Y data
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
            else if (dataType == TRACK_DATA_TYPE.windSpeed)
            {
                yData.add(trackPoints.get(i).getWindSpeed());
            }
            else if (dataType == TRACK_DATA_TYPE.maxWindSpeed)
            {
                yData.add(trackPoints.get(i).getMaxWindSpeed());
            }
            else if (dataType == TRACK_DATA_TYPE.windDirection)
            {
                yData.add(trackPoints.get(i).getWindDirection());
            }
        }
        return yData;
    }
    
    /**
     * Makes the charts.
     * 
     * @param trackSegment the track segment
     */
    public void makeCharts(TrackSegment trackSegment)
    {
        makeSpeedChart(trackSegment);
        makeCourseChart(trackSegment);
        makeWindChart(trackSegment);
        // makeWindDirectionChart(trackSegment);
    }
    
    /**
     * Makes a speed chart.
     * 
     * @param trackSegment the track segment
     */
    public void makeSpeedChart(TrackSegment trackSegment)
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
        
        SwingWrapper<XYChart> swingWrapper = new SwingWrapper<XYChart>(speedChart);
        swingWrapper.displayChart();
    }
    
    /**
     * Makes a course chart.
     * 
     * @param trackSegment the track segment
     */
    public void makeCourseChart(TrackSegment trackSegment)
    {
        XYChartBuilder courseChartBuilder = new XYChartBuilder();
        List<Date> xData = makeXData(trackSegment);
        courseChartBuilder.width(1600);
        courseChartBuilder.height(400);
        courseChartBuilder.title("Course");
        courseChartBuilder.xAxisTitle("time");
        courseChartBuilder.yAxisTitle("Direction");
        
        XYChart courseChart = courseChartBuilder.build();
        List<Number> courseData = makeYData(trackSegment, TRACK_DATA_TYPE.course);
        courseChart.addSeries("Course", xData, courseData);
        
        List<Number> windDirectionData = makeYData(trackSegment, TRACK_DATA_TYPE.windDirection);
        courseChart.addSeries("Wind Direction", xData, windDirectionData);
        
        XYStyler courseChartStyler = courseChart.getStyler();
        courseChartStyler.setLegendPosition(LegendPosition.OutsideS);
        courseChartStyler.setHasAnnotations(false);
        
        SwingWrapper<XYChart> swingWrapper = new SwingWrapper<XYChart>(courseChart);
        swingWrapper.displayChart();
    }
    
    /**
     * Makes a wind chart for the given list of Wind data points.
     * 
     * @param windList the list of Wind data points
     */
    public void makeWindChart(TrackSegment trackSegment)
    {
        XYChartBuilder windChartBuilder = new XYChartBuilder();
        List<Date> xData = makeXData(trackSegment);
        windChartBuilder.width(1600);
        windChartBuilder.height(400);
        windChartBuilder.title("Wind Speed");
        windChartBuilder.xAxisTitle("time");
        windChartBuilder.yAxisTitle("km / h");
        
        XYChart windChart = windChartBuilder.build();
        windChart.addSeries("Wind Speed", xData, makeYData(trackSegment, TRACK_DATA_TYPE.windSpeed));
        windChart.addSeries("Max Wind Speed", xData, makeYData(trackSegment, TRACK_DATA_TYPE.maxWindSpeed));
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
    public void makeWindDirectionChart(TrackSegment trackSegment)
    {
        XYChartBuilder windChartBuilder = new XYChartBuilder();
        List<Date> xData = makeXData(trackSegment);
        windChartBuilder.width(1600);
        windChartBuilder.height(400);
        windChartBuilder.title("Wind Direction");
        windChartBuilder.xAxisTitle("time");
        windChartBuilder.yAxisTitle("Degrees");
        
        XYChart windDirectionChart = windChartBuilder.build();
        windDirectionChart.addSeries("Wind Direction", xData, makeYData(trackSegment, TRACK_DATA_TYPE.windDirection));
        XYStyler styler = windDirectionChart.getStyler();
        styler.setLegendPosition(LegendPosition.OutsideS);
        styler.setHasAnnotations(false);
        
        SwingWrapper<XYChart> swingWrapper = new SwingWrapper<XYChart>(windDirectionChart);
        swingWrapper.displayChart();
    }
    
    /**
     * Returns a an extracted list of track data (from timestamp to timestamp).
     * 
     * @param trackData the original list of track points
     * @param from      the "from" timestamp
     * @param to        the "to" timestamp
     * @return          the extracted list of track points
     */
    public List<TrackPoint> extractTrackData(List<TrackPoint> trackData, Date from, Date to)
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
     * Extracts a list of track segments from the given track list. Segments are extracted if the speed values of all the points of
     * the segment is grater than the given threshold and the number of points is greater than the given minimum.
     * 
     * @param trackList      the list of tracks
     * @param speedThreshold the speed of all points of a subsegment must be greater than this threshold
     * @param minPoints      the minimum number of points of a subsegment
     * @return               the number of segments to be displayed
     */
    public List<TrackSegment> extractTrackSegments(List<Track> trackList, double speedThreshold, int minPoints)
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
     * Adds the matching wind data to the given track list.
     * 
     * @param trackList the track list
     * @param windData  the wind data
     */
    public void addWindDataToTrackList(List<Track> trackList, List<WindDataPoint> windData)
    {
        for (Track track : trackList)
        {
            addWindDataToTrack(track, windData);
        }
    }
    
    /**
     * Adds the matching wind data to the given track.
     * 
     * @param track    the track segment
     * @param windData the wind data
     */
    public void addWindDataToTrack(Track track, List<WindDataPoint> windData)
    {
        WeatherDataManager weatherDataManager = new WeatherDataManager();
        List<TrackSegment> trackSegments = track.getTrackSegments();
        for (TrackSegment trackSegment : trackSegments)
        {
            Date[] timestamps = trackSegment.getTimestamps();
            List<WindDataPoint> extractedWindData = weatherDataManager.getWindData(windData, timestamps[0], timestamps[timestamps.length - 1]);
            List<WindDataPoint> interpolatedWindData = weatherDataManager.interpolateWindData(extractedWindData, timestamps);
            addWindDataToTrackSegement(trackSegment, interpolatedWindData);
        }
    }
    
    /**
     * Adds the matching wind data to the given track segment.
     * 
     * @param trackSegment the track segment
     * @param windData     the wind data
     */
    public void addWindDataToTrackSegement(TrackSegment trackSegment, List<WindDataPoint> windData)
    {
        List<TrackPoint> trackPoints = trackSegment.getTrackPoints();
        Iterator<WindDataPoint> it = windData.iterator();
        for (TrackPoint trackPoint : trackPoints)
        {
            WindDataPoint windDataPoint = it.next();
            if (trackPoint.getTimestamp().equals(windDataPoint.getTimestamp()))
            {
                trackPoint.setWindDirection(windDataPoint.getDirection());
                trackPoint.setWindSpeed(windDataPoint.getWindSpeed());
                trackPoint.setMaxWindSpeed(windDataPoint.getMaxWindSpeed());
            }
        }
    }
    
    /**
     * Displays speed charts based on track data from the given URL.
     * 
     * @param trackList the track list
     */
    public void displaySpeedCharts(List<Track> trackList)
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
    public void displaySpeedCharts(List<Track> trackList, String from, String to)
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
    public void displaySpeedCharts(List<Track> trackList, Date from, Date to)
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
                    List<TrackPoint> filteredTrackPoints = extractTrackData(trackPoints, from, to);
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
}
