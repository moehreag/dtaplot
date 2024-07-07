package io.github.moehreag.dtaplot.android

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.compose.common.rememberVerticalLegend
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import io.github.moehreag.dtaplot.Discovery
import io.github.moehreag.dtaplot.Pair
import io.github.moehreag.dtaplot.Translations
import io.github.moehreag.dtaplot.Value
import io.github.moehreag.dtaplot.dta.DtaParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.URL
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class ActivityViewModel : ViewModel() {

    private val timeFormat: NumberFormat = DecimalFormat("00")
    private var graphData: MutableCollection<Map<String, Value<*>>> = mutableListOf()
    private var validData: MutableCollection<Map<String, Value<*>>> = mutableStateListOf()
    private var displayedData: MutableMap<String, Pair<List<Number>, List<Number>>> = mutableStateMapOf()
    val modelProducer = CartesianChartModelProducer.build()

    fun load(address: InetSocketAddress, options: MutableList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = getDataFile(address)
            graphData.addAll(result)
            validData.clear()
            displayedData.clear()
            addSet("TVL")
            getSetNames(options)
            Log.i("DtaPlot/IO", "Added downloaded values!")
        }
    }

    private fun updateGraph(){
        Log.i("DtaPlot/IO", "Updating graph")
        viewModelScope.launch(Dispatchers.IO) {
            modelProducer.runTransaction {
                lineSeries {
                    displayedData.forEach {
                        series(it.value.left, it.value.right)
                    }
                }
            }
        }
        Log.i("DtaPlot/IO", "Updated graph")
    }

    fun isSetDisplayed(name: String): Boolean {
        return displayedData.containsKey(name)
    }

    fun removeSet(name: String){
        displayedData.remove(name)
        updateGraph()
    }

    fun addSet(name: String){
        displayedData[name] = getGraphData(name)
        updateGraph()
    }

    fun legendNames(): Set<String> {
        Log.i("DtaPlot/IO", displayedData.keys.joinToString())
        return displayedData.keys.ifEmpty { setOf(" ") }
    }

    fun formatValue(value: Long): String {
        val zTime = ZonedDateTime.ofInstant(
            Instant.ofEpochSecond(value),
            ZoneId.systemDefault()
        )
        val label: String = Translations.translate(
            "date.format",
            timeFormat.format(zTime.hour.toLong()),
            timeFormat.format(zTime.minute.toLong()),
            timeFormat.format(zTime.dayOfMonth.toLong()),
            timeFormat.format(zTime.monthValue.toLong()),
            timeFormat.format(zTime.year.toLong())
        )
        return label
    }

    private fun getDataFile(address: InetSocketAddress): Collection<Map<String, Value<*>>> {
        val location = "http://${address.hostString}/NewProc"
        Log.i("DtaPlot/IO", "Fetching data... location: $location")
        val bytes = URL(location).openStream().readBytes()
        Log.i("DtaPlot/IO", "Fetched data!")
        val data = DtaParser.get(bytes).datapoints
        Log.i("DtaPlot/IO", "new size: " + data.size)
        return data
    }

    private fun getGraphData(setName: String): Pair<List<Number>, List<Number>> {
        Log.i("DtaPlot/ViewModel-dbg", "getting graph data for $setName")
        val xs: MutableList<Number> = mutableListOf()
        val ys: MutableList<Number> = mutableListOf()
        getValidData().parallelStream().sorted(
            Comparator.comparingInt {
                (it["time"]?.get() as Number).toInt()
            })
            .forEachOrdered { stringValueMap ->
                val time = (stringValueMap["time"]!!
                    .get() as Number).toInt()
                val value = stringValueMap[setName] ?: return@forEachOrdered
                if (value.get() is Number) {
                    val y = (value.get() as Number).toDouble()
                    xs.add(time)
                    ys.add(y)
                }
            }
        Log.i("DtaPlot/ViewModel-dbg", "getting graph data for $setName - done")
        return Pair.of(xs, ys)
    }

    private fun getSetNames(keys: MutableList<String>) {
        keys.clear()
        Log.i("DtaPlot/ViewModel-dbg", "getting set names")
        getValidData()
            .stream().map { obj -> obj.keys }
            .forEach { k ->
                k.stream().filter { s -> !keys.contains(s) && s != "time" }
                    .forEach { e ->
                        keys.add(
                            e
                        )
                    }
            }
        Log.i("DtaPlot/ViewModel-dbg", "getting set names - done")
    }

    private fun getValidData(): Collection<Map<String, Value<*>>> {
        Log.i("DtaPlot/ViewModel-dbg", "getting valid data")
        synchronized(validData) {
            if (validData.isNotEmpty()) {
                return Collections.unmodifiableCollection(validData)
            }

            val entries: MutableCollection<MutableMap<String, Value<*>>> = mutableListOf()
            for (map in graphData) {
                val clone: MutableMap<String, Value<*>> = HashMap<String, Value<*>>(map)
                entries.add(clone)

            }
            val keys: MutableList<String> = mutableListOf()
            entries.stream().map { obj: Map<String, Value<*>> -> obj.keys }
                .forEach { k: Set<String> ->
                    k.stream().filter { s: String -> !keys.contains(s) }
                        .forEach { e: String ->
                            keys.add(
                                e
                            )
                        }
                }
            for (key in keys) {
                val values: MutableList<Double> = mutableListOf()
                for (map in entries) {
                    if (!map.containsKey(key) || map[key]!!.get() !is Number) {
                        continue
                    }
                    val `val` = (map[key]!!.get() as Number).toDouble()
                    if (!values.contains(`val`)) {
                        values.add(`val`)
                    }
                }
                if (values.size <= 1) {
                    for (map in entries) {
                        map.remove(key)
                    }
                }
            }
            Log.i("DtaPlot/ViewModel-dbg", "getting valid data - done")
            validData.addAll(entries)
            return Collections.unmodifiableCollection(entries)
        }
    }

    fun discover(list: MutableList<InetSocketAddress>) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i("DtaPlot/IO", "Discovering heatpumps...")
            list.addAll(Discovery.getInstance().discover())
            var l = ""
            list.forEach {
                if (l.isNotEmpty()) {
                    l += ", "
                }
                l += it.hostString
            }
            Log.i("DtaPlot/IO", "Discovered: $l (${list.size})")
        }
    }

    fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
        }


        /*TODO*/
    }
}