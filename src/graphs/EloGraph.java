package graphs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.Collections;
import java.util.List;

public class EloGraph {
    private String name;
    private JFreeChart chart;

    public EloGraph(String title, List<Integer> data){
        name = title;
        chart = createChart(createDataset(data), data);
    }

    private CategoryDataset createDataset(List<Integer> data){
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int i = 1;
        for(Integer d: data){
            dataset.addValue(d, "test", Integer.toString(i));
            i++;
        }
        return dataset;
    }

    private JFreeChart createChart(CategoryDataset dataset, List<Integer> data){
        JFreeChart chart = ChartFactory.createLineChart(name, "match nbr", "Elo", dataset);
        CategoryPlot plot = chart.getCategoryPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(Collections.min(data) - 20, Collections.max(data) + 20);
        return chart;
    }
    public JFreeChart getChart(){
        return chart;
    }
}
