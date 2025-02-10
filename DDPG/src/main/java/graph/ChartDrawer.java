package graph;

import binace.vo.QuoteVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ddpg.v3.indicators.CalculateKD;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ChartDrawer {

    public static void plotPriceChart(List<BigDecimal> prices) {
        // 创建价格时间序列
        TimeSeries priceSeries = new TimeSeries("Price");

        // 将价格添加到时间序列
        for (int i = 0; i < prices.size(); i++) {
            priceSeries.add(new Second(new java.util.Date(2023, 1, i + 1)), prices.get(i));
        }

        // 创建时间序列数据集
        TimeSeriesCollection dataset = new TimeSeriesCollection(priceSeries);

        // 创建图表
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Price Chart", // 标题
            "Date", // X轴标签
            "Price", // Y轴标签
            dataset, // 数据集
            false, // 是否显示图例
            true, // 是否生成工具提示
            false // 是否生成URL链接
        );

        // 创建图表面板并显示
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame("Price Chart");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    public static void plotKDJChart(List<double[]> kdjValues) {
        // 创建时间序列
        TimeSeries kSeries = new TimeSeries("K");
        TimeSeries dSeries = new TimeSeries("D");
        TimeSeries jSeries = new TimeSeries("J");

        // 将KDJ值添加到时间序列
        for (int i = 0; i < kdjValues.size(); i++) {
            double[] kdj = kdjValues.get(i);
            kSeries.add(new Second(new java.util.Date(2023, 1, i + 1)), kdj[0]);
            dSeries.add(new Second(new java.util.Date(2023, 1, i + 1)), kdj[1]);
            jSeries.add(new Second(new java.util.Date(2023, 1, i + 1)), kdj[2]);
        }

        // 创建时间序列数据集
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(kSeries);
        dataset.addSeries(dSeries);
        dataset.addSeries(jSeries);

        // 创建图表
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "KDJ Chart", // 标题
            "Date", // X轴标签
            "Value", // Y轴标签
            dataset, // 数据集
            true, // 是否显示图例
            true, // 是否生成工具提示
            false // 是否生成URL链接
        );

        // 创建图表面板并显示
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame("KDJ Chart");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    public static void plotPriceAndKDJChart(List<BigDecimal> prices, List<double[]> kdjValues) {
        // 创建价格时间序列
        TimeSeries priceSeries = new TimeSeries("Price");

        // 创建KDJ时间序列
        TimeSeries kSeries = new TimeSeries("K");
        TimeSeries dSeries = new TimeSeries("D");
        TimeSeries jSeries = new TimeSeries("J");

        // 将价格和KDJ值添加到时间序列
        for (int i = 0; i < prices.size(); i++) {
            priceSeries.add(new Second(new java.util.Date(2023, 1, i + 1)), prices.get(i));
            if (i >= 2) {
                double[] kdj = kdjValues.get(i - 2); // KDJ从第2天开始有效
                kSeries.add(new Second(new java.util.Date(2023, 1, i + 1)), kdj[0]);
                dSeries.add(new Second(new java.util.Date(2023, 1, i + 1)), kdj[1]);
                jSeries.add(new Second(new java.util.Date(2023, 1, i + 1)), kdj[2]);
            }
        }

        // 创建时间序列数据集
        TimeSeriesCollection priceDataset = new TimeSeriesCollection(priceSeries);
        TimeSeriesCollection kdjDataset = new TimeSeriesCollection();
        kdjDataset.addSeries(kSeries);
        kdjDataset.addSeries(dSeries);
        kdjDataset.addSeries(jSeries);

        // 创建图表
        JFreeChart priceChart = ChartFactory.createTimeSeriesChart(
            "Price Chart", // 标题
            "Date", // X轴标签
            "Price", // Y轴标签
            priceDataset, // 数据集
            false, // 是否显示图例
            true, // 是否生成工具提示
            false // 是否生成URL链接
        );

        JFreeChart kdjChart = ChartFactory.createTimeSeriesChart(
            "KDJ Chart", // 标题
            "Date", // X轴标签
            "Value", // Y轴标签
            kdjDataset, // 数据集
            true, // 是否显示图例
            true, // 是否生成工具提示
            false // 是否生成URL链接
        );

        // 创建图表面板并显示
        ChartPanel priceChartPanel = new ChartPanel(priceChart);
        ChartPanel kdjChartPanel = new ChartPanel(kdjChart);

        JFrame frame = new JFrame("Price and KDJ Chart");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.getContentPane().add(priceChartPanel);
        frame.getContentPane().add(kdjChartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    public static void plotKdGraph(double[][] states) {
        List<BigDecimal> priceList = new ArrayList<>();
        for(double[] state : states) {
            priceList.add(BigDecimal.valueOf(state[0]));
        }

        List<double[]> kdList = CalculateKD.calculateKDJ(priceList, 3);
        ChartDrawer.plotPriceAndKDJChart(priceList, kdList);
    }

    public static void main(String[] args) throws Exception {
//        String filePath = "DDPG/data.json";
//        ObjectMapper objectMapper = new ObjectMapper();
//        double[][] _states = objectMapper.readValue(new File(filePath), double[][].class);

        String filePath = "DDPG/data2.json";
        ObjectMapper objectMapper = new ObjectMapper();
        List<QuoteVO> list =  objectMapper.readValue(new File(filePath), new TypeReference<>(){});
        List<BigDecimal> _list1 = list.stream().map(QuoteVO::getClose).toList();
        List<BigDecimal> _list2 = list.stream().map(QuoteVO::getVolume).toList();

        plotPriceChart(_list1);
        plotPriceChart(_list2);

        // 打印读取的数据
//        System.out.println("从 JSON 文件读取的二维数组:");
//        for (double[] row : _states) {
//            for (double value : row) {
//                System.out.print(value + " ");
//            }
//            System.out.println();
//        }
    }
}
