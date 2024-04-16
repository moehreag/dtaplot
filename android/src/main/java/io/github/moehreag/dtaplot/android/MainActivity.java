package io.github.moehreag.dtaplot.android;

import io.github.moehreag.dtaplot.DataLoader;
import io.github.moehreag.dtaplot.Value;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import io.github.moehreag.dtaplot.android.databinding.ActivityMainBinding;
import io.github.moehreag.dtaplot.dta.DtaParser;

public class MainActivity extends AppCompatActivity {

	private ActivityMainBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		BottomNavigationView navView = findViewById(R.id.nav_view);
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
				R.id.navigation_plot, R.id.navigation_tcp, R.id.navigation_ws)
				.build();
		//NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
		//NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
		//NavigationUI.setupWithNavController(binding.navView, navController);

	}



}