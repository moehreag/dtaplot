package io.github.moehreag.dtaplot.android

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import io.github.moehreag.dtaplot.*
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
import kotlin.io.path.bufferedReader
import kotlin.io.path.exists
import kotlin.io.path.moveTo
import kotlin.io.path.notExists

class ActivityViewModel : ViewModel() {

    private val timeFormat: NumberFormat = DecimalFormat("00")
    private val graphData: MutableCollection<Map<String, Value<*>>> = mutableListOf()
    private var validData: MutableCollection<Map<String, Value<*>>> = mutableListOf()
    private var displayedData: MutableMap<String, Pair<List<Number>, List<Number>>> = mutableStateMapOf()
    val setNames: MutableList<String> = mutableStateListOf()
    private var minTime = 0
    val modelProducer = CartesianChartModelProducer()

    fun load(address: InetSocketAddress) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = getDataFile(address)
            graphData.addAll(result)
            validData.clear()
            displayedData.clear()
            addSet("TVL")
            getSetNames(setNames)
            Log.i("DtaPlot/IO", "Added downloaded values!")
        }
    }

    private fun updateGraph() {
        Log.i("DtaPlot/IO", "Updating graph")
        viewModelScope.launch(Dispatchers.IO) {
            modelProducer.runTransaction {
                if (displayedData.isNotEmpty()) {
                    lineSeries {
                        displayedData.values.forEach { series(it.left, it.right) }
                    }
                }
            }
        }
        Log.i("DtaPlot/IO", "Updated graph")
    }

    fun isSetDisplayed(name: String): Boolean {
        return displayedData.containsKey(name)
    }

    fun removeSet(name: String) {
        displayedData.remove(name)
        updateGraph()
    }

    fun addSet(name: String) {
        displayedData[name] = getGraphData(name)
        updateGraph()
    }

    fun legendNames(): Set<String> {
        Log.i("DtaPlot/IO", displayedData.keys.joinToString())
        return displayedData.keys.ifEmpty { setOf(" ") }
    }

    fun formatValue(value: Long): String {
        val zTime = ZonedDateTime.ofInstant(
            Instant.ofEpochSecond((value * 120) + minTime),
            ZoneId.systemDefault()
        )
        val label: String = Translations.translate(
            "date.format",
            timeFormat.format(zTime.hour),
            timeFormat.format(zTime.minute),
            timeFormat.format(zTime.dayOfMonth),
            timeFormat.format(zTime.monthValue),
            timeFormat.format(zTime.year)
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
        val xs: MutableList<Int> = mutableListOf()
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
        val setMinTime = xs.minBy { it }
        if (setMinTime < minTime) {
            val newMap = mutableMapOf<String, Pair<List<Number>, List<Number>>>()
            displayedData.forEach { (t, u) ->
                newMap[t] = Pair.of(u.left.map { it.toInt() - (minTime - setMinTime) }, u.right)
            }
            displayedData = newMap
        }
        minTime = setMinTime
        return Pair.of(xs.map { (it - minTime) / 120 }.toMutableList(), ys)
    }

    private fun getSetNames(keys: MutableList<String>) {
        keys.clear()
        Log.i("DtaPlot/ViewModel-dbg", "getting set names")
        getValidData()
            .stream().map { obj -> obj.keys }
            .forEach { k ->
                k.stream().filter { s -> !keys.contains(s) && s != "time" }
                    .forEach { e ->
                        keys.add(e)
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

            val entries: MutableCollection<MutableMap<String, Value<*>>> = mutableSetOf()
            for (map in graphData) {
                val clone: MutableMap<String, Value<*>> = HashMap<String, Value<*>>(map)
                entries.add(clone)

            }
            Log.i("DtaPlot/ViewModel-dbg", "Cloned data!")
            val keys: MutableSet<String> = mutableSetOf()
            entries.map { it.keys }.forEach { k: Set<String> -> keys.addAll(k) }
            val values: MutableSet<Double> = mutableSetOf()
            for (key in keys) {
                values.clear()
                for (map in entries) {
                    if (!map.containsKey(key) || map[key]!!.get() !is Number) {
                        continue
                    }
                    val `val` = (map[key]!!.get() as Number).toDouble()
                    values.add(`val`)
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

    fun discover(list: MutableList<InetSocketAddress?>) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i("DtaPlot/IO", "Discovering heatpumps...")
            list.addAll(Discovery.getInstance().discover())
            var l = ""
            list.forEach {
                if (l.isNotEmpty()) {
                    l += ", "
                }
                l += it?.toString()
            }
            Log.i("DtaPlot/IO", "Discovered: $l (${list.size})")
            if (list.isEmpty()) {
                list.add(null)
            }
        }
    }

    fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
        }


        /*TODO*/
    }

    companion object {
        fun saver(context: Context, lifecycleScope: LifecycleCoroutineScope): Saver<ActivityViewModel, List<Any>> {
            return Saver({ value ->
                if (value.graphData.isEmpty() && value.displayedData.isEmpty()) {
                    return@Saver null
                }
                val valueList = mutableListOf<Any>()
                val dataPath = "saved-graph-data.json"
                lifecycleScope.launch(Dispatchers.IO) {
                    val path = context.cacheDir.resolve(dataPath).toPath()

                    Log.i("ActivityViewModel/Saver", "saving graph data to " + path.toAbsolutePath())
                    val tmp = path.resolveSibling("${path.fileName}~")
                    DataLoader.getInstance().save(value.graphData, tmp)
                    tmp.moveTo(path, true)
                    Log.i("ActivityViewModel/Saver", "saved!")
                }
                valueList.add(0, dataPath)
                valueList.add(1, value.displayedData.keys.toList())
                return@Saver valueList
            }, { value ->
                val model = ActivityViewModel()
                val dataPath = context.cacheDir.toPath().resolve(value[0] as String)
                val tmp = dataPath.resolveSibling("${dataPath.fileName}~")

                if ((value.isEmpty() || dataPath.notExists()) && tmp.notExists()) {
                    return@Saver model
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    while (tmp.exists()) {
                        Thread.sleep(100)
                    }
                    Log.i("ActivityViewModel/Saver", "restoring graph data from " + dataPath.toAbsolutePath())
                    val json = dataPath.bufferedReader().useLines { it.joinToString("\n") }
                    if (json.isNotEmpty()) {
                        model.graphData.addAll(DataLoader.getInstance().load(json))
                        model.getValidData()
                        (value[1] as Collection<*>).filterIsInstance<String>().forEach {
                            model.displayedData[it] = model.getGraphData(it)
                        }
                        model.updateGraph()
                        model.getSetNames(model.setNames)
                    }
                    Log.i("ActivityViewModel/Saver", "restored!")
                }
                return@Saver model
            })
        }
    }
}