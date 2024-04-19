package io.github.moehreag.dtaplot.android.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.moehreag.dtaplot.Pair
import io.github.moehreag.dtaplot.Value
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

abstract class TableView : View {

    private val content: MutableList<Pair<String, Value<*>>> = mutableStateListOf()
    private val data: MutableCollection<Map<String, Value<*>>> = mutableListOf()

    @Composable
    fun DrawTable(padding: PaddingValues) {


            LazyColumn(modifier = Modifier
                .padding(padding)
                .fillMaxWidth(), content = {
                items(content) {
                    Row(modifier = Modifier.padding(4.dp).fillMaxWidth()) {
                        OutlinedTextField(value = it.right.get().toString(), onValueChange = { newVal ->
                            set(it.right as Value.Mutable<*>, newVal)
                        }, label = {
                            Row {
                                Text(text = it.left)
                                Spacer(modifier = Modifier.padding(2.dp))
                                if (it.right.unit.isNotBlank()) {
                                    Text(text = "(${it.right.unit})")
                                }
                            }
                        }, readOnly = it.right !is Value.Mutable<*>, modifier = Modifier.fillMaxWidth())
                    }
                }
            })
    }

    fun hasContent(): Boolean {
        return content.isNotEmpty()
    }

    fun load(data: Collection<Map<String, Value<*>>>) {
        this.data.clear()
        this.data.addAll(data)
        synchronized(content) {
            content.clear()
            data.forEach { map ->
                map.forEach { (s, value) ->
                    content.add(Pair.of(s, value))
                }
            }
            content.sortBy { it.left }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T, B : Any> set(mut: Value.Mutable<B>, newVal: T) {
        if (newVal is String) {
            val cls: Class<*> = mut.get().javaClass
            try {
                if (mut.get() !is String) {
                    val handle = MethodHandles.lookup().findStatic(
                        cls, "valueOf", MethodType.methodType(
                            cls,
                            String::class.java
                        )
                    )
                    if (newVal.contains(" ")) {
                        mut.set(handle.invoke(newVal.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[0]) as B)
                    } else {
                        mut.set(handle.invoke(newVal) as B)
                    }
                } else {
                    mut.set(newVal as B)
                }
            } catch (e: Throwable) {
                throw RuntimeException(e)
            }
        }
    }
}