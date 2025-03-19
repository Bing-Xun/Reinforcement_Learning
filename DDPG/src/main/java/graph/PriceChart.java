package graph;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PriceChart extends ApplicationFrame {

    public PriceChart(String title, List<DataPoint> dataPoints) {
        super(title);

        TimeSeries priceSeries = new TimeSeries("價格");
        for (DataPoint dataPoint : dataPoints) {
            Date date = new Date(dataPoint.getTimestamp());
            Millisecond millisecond = new Millisecond(date);
            priceSeries.addOrUpdate(millisecond, dataPoint.getPrice());
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(priceSeries);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "價格走勢圖",
            "時間 (yyyy-MM-dd HH:mm:ss.SSS)",
            "價格",
            dataset,
            true,
            true,
            false
        );

        XYPlot plot = chart.getXYPlot();
        DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.TAIWAN));

        for (DataPoint dataPoint : dataPoints) {
            if (dataPoint.getTag() != null && !dataPoint.getTag().isEmpty()) {
                Date date = convertTimestampToDate(dataPoint.getTimestamp());
                Hour hour = new Hour(date);
                double x = hour.getFirstMillisecond();
                double y = dataPoint.getPrice().doubleValue();
                XYTextAnnotation annotation = new XYTextAnnotation(dataPoint.getTag(), x, y);
                annotation.setPaint(Color.BLUE);
                plot.addAnnotation(annotation);
            }
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);
    }


    public static Date convertTimestampToDate(long timestamp) {
        return new Date(timestamp);
    }

    public static String formatTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    public static void plot(List<DataPoint> dataPoints) {
        PriceChart chart = new PriceChart("價格圖表", dataPoints);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }

    public static void main(String[] args) {
        List<DataPoint> dataPoints = List.of(
            new DataPoint(new BigDecimal("10.50"), new BigDecimal(1000), 1678886400000L, "事件A"),
            new DataPoint(new BigDecimal("10.75"), new BigDecimal(1200), 1678972800000L, null),
            new DataPoint(new BigDecimal("11.00"), new BigDecimal(1500), 1679059200000L, "事件B"),
            new DataPoint(new BigDecimal("11.25"), new BigDecimal(1300), 1679145600000L, null)
        );

        plot(dataPoints);
    }


}