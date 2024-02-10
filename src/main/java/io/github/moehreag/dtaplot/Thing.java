package io.github.moehreag.dtaplot;

import javax.swing.*;

import java.awt.*;

import ptolemy.plot.Plot;
import ptolemy.plot.PlotFormatter;

public class Thing {

	public static void main(String[] args){

		JFrame frame = new JFrame("aa");

		Plot plot = new Plot();
		PlotFormatter formatter = new PlotFormatter(plot);
		frame.add(plot);
		frame.add(formatter, BorderLayout.EAST);
		frame.setVisible(true);
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
	}
}
