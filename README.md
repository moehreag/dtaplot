# DtaPlot

Plotting/Visualizing software for data of 
Luxtronik heatpump controllers.

### Features

This program fetches data from the `/proclog` endpoint
exposed by the heatpump controller.
Analog Data is then displayed in the plot on the left as a graph
while digital data is displayed on the right. Various
graphs can also be displayed together and combined randomly.
The rendered plot can then also be exported to various image
formats and all data can also be saved or merged with other,
previously saved files.

### Notes

- When exporting images note that the resulting image will 
have the resolution of the currently displayed graph pane.
This means that if you increase the window size, the resolution will get
higher.

### Screenshots

![Screenshot of the Welcome Page](assets/welcome.png)
![Screenshot of displayed graphs](assets/graphs.png)

### Related Projects

These Projects served as a base for developing this one:

- [openDTA](https://sourceforge.net/projects/opendta/)
- [python-luxtronik](https://github.com/Bouni/python-luxtronik/)
- [luxtronik](https://github.com/Bouni/luxtronik)
