package com.ev.approver;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        drawPieChart();
    }

    private void drawPieChart(){
        PieChart pieChart = (PieChart)findViewById(R.id.piechart);
        ArrayList<PieEntry> yvalues = new ArrayList<PieEntry>();
        yvalues.add(new PieEntry(25, "Approved"));
        yvalues.add(new PieEntry(10, "Pending"));

        PieDataSet dataSet = new PieDataSet(yvalues, "Approval Satistics");

        //dataSet.setColors();

        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);

        pieChart.setHoleRadius(7);
        pieChart.setTransparentCircleRadius(10);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(Integer.parseInt(getString(R.string.chart_data_size)));
        pieChart.setData(data);
        pieChart.invalidate();
        pieChart.setEntryLabelTextSize(Integer.parseInt(getString(R.string.chart_data_size)));
        pieChart.setHoleColor(R.color.hole_color);
        pieChart.setEntryLabelColor(R.color.black);

    }
}
