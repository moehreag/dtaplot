package io.github.moehreag.dtaplot.android

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.compose.common.rememberVerticalLegend
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.Shape
import io.github.moehreag.dtaplot.Discovery
import io.github.moehreag.dtaplot.Pair
import io.github.moehreag.dtaplot.Translations
import io.github.moehreag.dtaplot.Value
import io.github.moehreag.dtaplot.dta.DtaParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetAddress
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
    private var graphData: MutableCollection<Map<String, Value<*>>> = mutableStateListOf()
    private var validData: MutableCollection<Map<String, Value<*>>> = mutableStateListOf()
    val modelProducer = CartesianChartModelProducer.build()

    fun load(address: InetSocketAddress) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = getDataFile(address)
            graphData.addAll(result)
            validData.clear()
            modelProducer.tryRunTransaction {
                lineSeries {
                    val data = getGraphData("TVL")
                    series(data.left, data.right)
                }
            }
            Log.i("DtaPlot/IO", "Added downloaded values!")
        }
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

    fun getDataFile(address: InetSocketAddress): Collection<Map<String, Value<*>>> {
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
        getValidData().stream().sorted(
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

    fun getSetNames(): List<String>{
        Log.i("DtaPlot/ViewModel-dbg", "getting set names")
        val keys: MutableList<String> = mutableListOf()
        getValidData().stream().map { obj: Map<String, Value<*>> -> obj.keys }
            .forEach { k: Set<String> ->
                k.stream().filter { s: String -> !keys.contains(s) && s != "time" }
                    .forEach { e: String ->
                        keys.add(
                            e
                        )
                    }
            }
        if (keys.isEmpty()){
            keys.add("")
        }
        Log.i("DtaPlot/ViewModel-dbg", "getting set names - done")
        return keys
    }

    private fun getValidData(): Collection<Map<String, Value<*>>> {
        Log.i("DtaPlot/ViewModel-dbg", "getting valid data")
        if (validData.isNotEmpty()){
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
}