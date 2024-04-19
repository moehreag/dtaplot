package io.github.moehreag.dtaplot.android.socket

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketCloseCode
import com.neovisionaries.ws.client.WebSocketFrame
import io.github.moehreag.dtaplot.Value
import io.github.moehreag.dtaplot.socket.ws.Storage
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import java.net.InetSocketAddress
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import kotlin.collections.set

object WebSocket {
    private var timer: Timer? = null
    private var socket: com.neovisionaries.ws.client.WebSocket? = null

    private var consumer: (Collection<Map<String, Value<*>>>) -> Unit? = {  }
    private var passwordSupplier: () -> Optional<String> = {Optional.empty<String>()}
    private var connected by mutableStateOf(false)

    private fun load(address: InetSocketAddress) {
        socket = com.neovisionaries.ws.client.WebSocketFactory().createSocket(URI.create("ws://" + address.hostString + ":" + 8214)).apply {
            addProtocol("Lux_WS")
            addListener(Client(this@WebSocket))
            connect()
        }
        connected = true
    }

    fun disconnect() {
        close()
    }

    fun read(
        address: InetSocketAddress,
        passwordDialog: () -> Optional<String>,
        consumer: (Collection<Map<String, Value<*>>>) -> Unit?
    ) {
        if (this.connected) {
            Log.w("DtaPlot/WebSocket","Websocket already connected!")
            return
        }
        try {
            passwordSupplier = passwordDialog
            this.consumer = consumer
            load(address)
        } catch (e: Exception) {
            Log.e("DtaPlot/WebSocket","Error while reading websocket data", e)
        }
    }

    private class Client(val instance: WebSocket) : WebSocketAdapter() {
        override fun onConnected(
            websocket: com.neovisionaries.ws.client.WebSocket?,
            headers: MutableMap<String, MutableList<String>>?
        ) {
            CompletableFuture.runAsync {
                Log.i("DtaPlot/WebSocket", "Connection opened!")
                val opt: Optional<String> = instance.passwordSupplier.invoke()
                if (opt.isPresent){
                    val pw = opt.get().ifBlank { "0" }
                    Log.i("DtaPlot/WebSocket","Sending login..")
                    instance.send("LOGIN;$pw")
                    Log.i("DtaPlot/WebSocket","Login sent!")
                    instance.timer = Timer()
                    instance.timer!!.schedule(object : TimerTask() {
                        override fun run() {
                            instance.send("REFRESH")
                        }
                    }, 10, 1000)
                } else {
                    instance.close()
                }
            }
        }

        override fun onTextMessage(websocket: com.neovisionaries.ws.client.WebSocket?, text: String?) {
            instance.parseResponse(text!!)
        }

        override fun onCloseFrame(websocket: com.neovisionaries.ws.client.WebSocket?, frame: WebSocketFrame?) {
            Log.i("DtaPlot/WebSocket","Socket closed! ${frame!!.closeCode} ${frame.closeReason}")
            instance.close()
        }
    }

    private fun send(content: String) {
        socket!!.sendText(content, true)
    }


    fun close() {
        Log.i("DtaPlot/WebSocket","Closing!")
        if (socket != null) {
            socket!!.sendClose(WebSocketCloseCode.NORMAL)
            socket = null
        }
        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
        }
        connected = false
    }

    fun parseResponse(message: CharSequence) {
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val source = InputSource(StringReader(message.toString()))
            val doc = builder.parse(source)
            val rootName = doc.documentElement.tagName

            when (rootName) {
                "values" -> {
                    val items = doc.documentElement.childNodes
                    var i = 0
                    while (i < items.length) {
                        val node = items.item(i)
                        val id = node.attributes.getNamedItem("id").nodeValue
                        val value = node.firstChild.textContent
                        Storage.idValueMap[id] = value
                        i++
                    }
                    consumer.invoke(setOf(Storage.getMerged()))
                }

                "Navigation" -> {
                    val nodes = doc.documentElement.getElementsByTagName("item")
                    var i = 0
                    while (i < nodes.length) {
                        val node = nodes.item(i)
                        if (node.firstChild.textContent == "Informationen") {
                            val id = node.attributes.getNamedItem("id").nodeValue
                            send("GET;$id")
                            return
                        }
                        i++
                    }
                }

                "Content" -> {
                    val entries = doc.documentElement.getElementsByTagName("item")
                    var i = 0
                    while (i < entries.length) {
                        val node = entries.item(i)
                        val id = node.attributes.getNamedItem("id").nodeValue
                        val name = node.firstChild.textContent
                        Storage.idNameMap[id] = name
                        i++
                    }
                }

                else -> {
                    /* unhandled case? */
                    Log.i("DtaPlot/WebSocket", doc.documentElement.tagName)
                    val list = doc.documentElement.childNodes
                    var i = 0
                    while (i < list.length) {
                        val node = list.item(i)
                        val name = node.nodeName
                        Log.i("DtaPlot/WebSocket", name)
                        i++
                    }
                }
            }
        } catch (e: ParserConfigurationException) {
            Log.e("DtaPlot/WebSocket","Failed to parse XML", e)
        } catch (e: IOException) {
            Log.e("DtaPlot/WebSocket","Failed to parse XML", e)
        } catch (e: SAXException) {
            Log.e("DtaPlot/WebSocket","Failed to parse XML", e)
        }
    }

    fun isConnected(): Boolean {
        return connected
    }
}
