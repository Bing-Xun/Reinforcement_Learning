package ddpg.v1.graph;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class LineChartExample {
    public static void main(String[] args) {
        // 创建模拟数据，降低单位
        double[] prices = {
            100.001, 99.994, 99.986, 100.011, 100.014, 99.997, 99.986, 99.983, 100.001, 100.008,
            100.017, 100.025, 100.013, 100.011, 100.002, 99.996, 99.986, 99.977, 99.981, 100.007,
            100.016, 100.023, 100.031, 100.027, 100.017, 100.010, 99.996, 99.981, 99.986, 100.001,
            100.016, 100.024, 100.020, 100.013, 99.998, 99.990, 99.979, 99.978, 100.003, 100.016,
            100.017, 100.024, 100.013, 99.997, 99.987, 99.979, 99.976, 99.988, 100.003, 100.015,
            100.018, 100.008, 99.997, 99.987, 99.978, 99.987, 100.003, 100.013, 100.022, 100.011
        };

        double[] volumes = {
            1024.5, 1015.7, 1024.1, 1009.2, 1017.9, 1020.3, 1010.4, 1021.8, 1018.9, 1020.7,
            1012.5, 1011.6, 1022.3, 1014.7, 1013.2, 1017.5, 1018.2, 1023.1, 1012.6, 1010.1,
            1020.4, 1017.1, 1018.7, 1023.5, 1016.3, 1012.4, 1021.2, 1019.3, 1016.5, 1014.8,
            1020.0, 1023.2, 1019.4, 1013.6, 1017.9, 1011.7, 1018.1, 1021.5, 1016.9, 1014.2,
            1019.6, 1011.4, 1022.0, 1020.8, 1019.3, 1021.7, 1023.1, 1022.6, 1019.8, 1018.3,
            1017.4, 1019.0, 1020.2, 1018.0, 1022.4, 1016.8, 1020.1, 1017.6, 1019.5, 1016.7,
            1021.3, 1014.3, 1013.9, 1020.6, 1018.5, 1017.7, 1013.1, 1021.0, 1022.8, 1019.2
        };

        // 缩小价格和成交量的单位，以便显示
        double scaleFactor = 0.1; // 缩放因子

        // 创建 XYSeries
        XYSeries priceSeries = new XYSeries("Price");
        XYSeries volumeSeries = new XYSeries("Volume");

        for (int i = 0; i < prices.length; i++) {
            priceSeries.add(i, prices[i] * scaleFactor);  // 缩小价格
            volumeSeries.add(i, volumes[i] * scaleFactor); // 缩小成交量
        }

        // 将数据集添加到 XYSeriesCollection
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(priceSeries);
        dataset.addSeries(volumeSeries);

        // 创建图表
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Price and Volume Over Time", // 图表标题
            "Time",                       // X轴标签
            "Value",                      // Y轴标签
            dataset,                      // 数据集
            PlotOrientation.VERTICAL,     // 图表方向
            true,                          // 显示图例
            true,                          // 显示工具提示
            false                          // 生成URL链接
        );

        // 设置Y轴的范围为 95 到 105
        ValueAxis yAxis = chart.getXYPlot().getRangeAxis();
        yAxis.setRange(95, 105);


        // 创建面板并显示
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        // 创建 JFrame 展示图表
        JFrame frame = new JFrame("Market Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }
}
