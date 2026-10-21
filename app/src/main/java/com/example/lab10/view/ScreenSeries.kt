package com.example.lab10.view

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lab10.data.SerieApiService
import com.example.lab10.data.SerieModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ContenidoSeriesListado(navController: NavHostController, servicio: SerieApiService) {
    val context = LocalContext.current
    val listaSeries = remember { mutableStateListOf<SerieModel>() }
    LaunchedEffect(Unit) {
        try {
            val listado = servicio.selectSeries()
            listaSeries.clear()
            listaSeries.addAll(listado)
        } catch (e: Exception) {
            Log.e("SERIES-LISTADO", "Error al cargar series", e)
            Toast.makeText(context, "Error al cargar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    LazyColumn {
        item {
            Row(
                modifier = Modifier.fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.1f)
                )
                Text(
                    text = "SERIE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    text = "Accion",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.2f)
                )
            }
        }
        items(listaSeries) { item ->
            Row(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.id ?: 0}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.1f)
                )
                Text(
                    text = item.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.6f)
                )
                IconButton(
                    onClick = {
                        navController.navigate("serieVer/${item.id ?: 0}")
                        Log.e("SERIE-VER", "ID = ${item.id}")
                    },
                    modifier = Modifier.weight(0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Ver",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                IconButton(
                    onClick = {
                        navController.navigate("serieDel/${item.id ?: 0}")
                        Log.e("SERIE-DEL", "ID = ${item.id}")
                    },
                    modifier = Modifier.weight(0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Ver",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
fun ContenidoSerieEditar(navController: NavHostController, servicio: SerieApiService, pid: Int = 0) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var id by remember { mutableStateOf(pid) }
    var name by remember { mutableStateOf("") }
    var release_date by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    if (id != 0) {
        LaunchedEffect(Unit) {
            try {
                val objSerie = servicio.selectSerie(id.toString())
                delay(100)
                name = objSerie.body()?.name ?: ""
                release_date = objSerie.body()?.release_date ?: ""
                rating = objSerie.body()?.rating?.toString() ?: ""
                category = objSerie.body()?.category ?: ""
            } catch (e: Exception) {
                Log.e("SERIE-EDITAR", "Error al obtener serie", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = id.toString(),
            onValueChange = { },
            label = { Text("ID (solo lectura)") },
            readOnly = true,
            singleLine = true
        )
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name: ") },
            singleLine = true
        )
        TextField(
            value = release_date,
            onValueChange = { release_date = it },
            label = { Text("Release Date (Ej: 2024-01-01):") },
            singleLine = true
        )
        TextField(
            value = rating,
            onValueChange = { rating = it },
            label = { Text("Rating (Ej: 5):") },
            singleLine = true
        )
        TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category (drama, comedy, action, horror):") },
            singleLine = true
        )
        Button(
            onClick = {
                scope.launch {
                    try {
                        val ratingValue = rating.toIntOrNull() ?: 0
                        val categoryValue = category.trim().lowercase()
                        val idParam = if (id == 0) null else id
                        val objSerie = SerieModel(
                            id = idParam,
                            name = name,
                            release_date = release_date,
                            rating = ratingValue,
                            category = categoryValue
                        )
                        val response = if (id == 0) {
                            servicio.insertSerie(objSerie)
                        } else {
                            servicio.updateSerie(id.toString(), objSerie)
                        }

                        if (response.isSuccessful) {
                            Toast.makeText(context, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                            navController.navigate("series") {
                                popUpTo("series") { inclusive = true }
                            }
                        } else {
                            val errBody = response.errorBody()?.string() ?: ""
                            val cleanError = if (errBody.contains("<title>")) {
                                val titleMatch = Regex("<title>(.*?)</title>", RegexOption.DOT_MATCHES_ALL).find(errBody)
                                titleMatch?.groupValues?.get(1)?.replace("\n", " ")?.trim() ?: errBody.take(100)
                            } else {
                                errBody
                            }
                            Log.e("SERIE-GRABAR", "Error ${response.code()}: $cleanError")
                            Toast.makeText(context, "Error ${response.code()}: $cleanError", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e("SERIE-GRABAR", "Excepción al grabar", e)
                        Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        ) {
            Text("Grabar", fontSize = 16.sp)
        }
    }
}

@Composable
fun ContenidoSerieEliminar(navController: NavHostController, servicio: SerieApiService, id: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                navController.navigate("series")
            },
            title = { Text(text = "Confirmar Eliminación") },
            text = { Text("¿Está seguro de eliminar la Serie?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        scope.launch {
                            try {
                                val response = servicio.deleteSerie(id.toString())
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Eliminado con éxito", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Error ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("SERIE-ELIMINAR", "Error al eliminar", e)
                            }
                            navController.navigate("series") {
                                popUpTo("series") { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                        navController.navigate("series")
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
