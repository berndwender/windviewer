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

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;

import at.home.bernd.WindDataPoint.DATATYPE;

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
                            wdt.setTimestamp(parseDateString(text));
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
    public List<Number> makeYData(List<WindDataPoint> windList, DATATYPE dataType)
    {
        List<Number> yData = new ArrayList<Number>();
        for (int i = 0; i < windList.size(); i++)
        {
            if (dataType == DATATYPE.windSpeed)
            {
                yData.add(windList.get(i).getWindSpeed());
            }
            else if (dataType == DATATYPE.maxWindSpeed)
            {
                yData.add(windList.get(i).getMaxWindSpeed());
            }
            else if (dataType == DATATYPE.temperature)
            {
                yData.add(windList.get(i).getTemperature());
            }
            else if (dataType == DATATYPE.chill)
            {
                yData.add(windList.get(i).getChill());
            }
        }
        return yData;
    }
    
    /**
     * Makes wind and temperature charts.
     * 
     * @param windList the wind list
     */
    public void makeCharts(List<WindDataPoint> windList)
    {
        XYChartBuilder windChartBuilder = new XYChartBuilder();
        windChartBuilder.width(1600);
        windChartBuilder.height(400);
        windChartBuilder.title("Wind");
        windChartBuilder.xAxisTitle("time");
        windChartBuilder.yAxisTitle("km / h");
        
        XYChart windChart = windChartBuilder.build();
        List<Date> xData = makeXData(windList);
        windChart.addSeries("Wind Speed", xData, makeYData(windList, DATATYPE.windSpeed));
        windChart.addSeries("Max Wind Speed", xData, makeYData(windList, DATATYPE.maxWindSpeed));
        XYStyler styler = windChart.getStyler();
        styler.setLegendPosition(LegendPosition.OutsideS);
        styler.setHasAnnotations(false);

        XYChartBuilder temperatureChartBuilder = new XYChartBuilder();
        temperatureChartBuilder.width(1600);
        temperatureChartBuilder.height(400);
        temperatureChartBuilder.title("Temperature");
        temperatureChartBuilder.xAxisTitle("time");
        temperatureChartBuilder.yAxisTitle("Degrees Centigrade");

        
        XYChart temperatureChart = temperatureChartBuilder.build();
        temperatureChart.addSeries("Temperature", xData, makeYData(windList, DATATYPE.temperature));
        temperatureChart.addSeries("Chill", xData, makeYData(windList, DATATYPE.chill));
        styler = temperatureChart.getStyler();
        styler.setLegendPosition(LegendPosition.OutsideS);
        styler.setHasAnnotations(false);
        
        SwingWrapper<XYChart> swingWrapper = new SwingWrapper<XYChart>(windChart);
        swingWrapper.displayChart();
        
        swingWrapper = new SwingWrapper<XYChart>(temperatureChart);
        swingWrapper.displayChart();
    }
    
    /**
     * Parses a timestamp and returns its value as a Date.
     * 
     * @param timeStamp the timestamp
     * @return the timestamp as Date
     */
    private Date parseDateString(String timeStamp)
    {
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Vienna"));
        try
        {
            date = sdf.parse(timeStamp);
        } catch (ParseException e)
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
     * Returns a sublist of wind data from a given timestamp to a given timestamp.
     * 
     * @param windData the original list
     * @param from the "from" timestamp
     * @param to the "to" timestamp
     * @return the sublist
     */
    List<WindDataPoint> getWindData(List<WindDataPoint> windData, Date from, Date to)
    {
        List<WindDataPoint> result = new ArrayList<WindDataPoint>();
        for (WindDataPoint wdp : windData)
        {
            Date ts = wdp.getTimestamp();
            if (from.before(ts) && to.after(ts))
            {
                result.add(wdp);
            }
        }
        return result;
    }
    
    /**
     * Starts the wind viewer
     */
    public static void main(String[] args)
    {
        WindViewer windViewer = new WindViewer();
        // String url = "file:///d|/bernd/projects/WindViewer/data/wind.xml";
        String url = "http://212.232.26.104/";
        List<WindDataPoint> windData = windViewer.parseWindData(url);
        
        // windViewer.printWindData(windData);
        
        GregorianCalendar fromDate = new GregorianCalendar();
        fromDate.add(Calendar.HOUR, -3);
        
        GregorianCalendar toDate = new GregorianCalendar();
        windData = windViewer.getWindData(windData, fromDate.getTime(), toDate.getTime());
        windViewer.makeCharts(windData);
        
        /*
        Date timeStamp = windViewer.parseDateString("11:42:00 30.05.2020");
        System.out.println(windViewer.timeString(timeStamp));
        System.out.println(windViewer.dateString(timeStamp));
        System.out.println(windViewer.parseTemperatureString(" 14.3 C"));
        */
    }
}
