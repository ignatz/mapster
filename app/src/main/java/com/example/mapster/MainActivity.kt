package com.example.mapster

import android.content.res.Resources.NotFoundException
import android.content.res.XmlResourceParser
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mapster.R.xml.example_track
import com.example.mapster.ui.theme.MapsterTheme
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager

class MainActivity : ComponentActivity() {
    // Returns a list of sample points, or null, if not available.
    private fun getPoints(): List<Point>? {
        lateinit var parser: XmlResourceParser
        try {
            parser = applicationContext.resources.getXml(example_track)
        } catch (e: NotFoundException) {
            return null
        }

        // This loops exists, because parser.nextTag() throws an exception. It seems that after the
        // first .next() we're still at the start of the document, where probably one would expect
        // that we move on, instead.
        do {
            parser.next()
        } while (parser.eventType != XmlResourceParser.START_TAG)

        return getTrack(parser)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MapsterTheme {
                var showTrack by remember { mutableStateOf(true) }

                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showTrack = !showTrack }) {
                            Text("Toggle")
                        }
                    },
                    content= {
                        val map = rememberMap(24.613272, 41.49277)

                        // Create a polyline manager instance and remember it.
                        val polylineAnnotationManager = remember { map.annotations.createPolylineAnnotationManager() }

                        // Define a list of geographic coordinates to be connected.
                        println("showTrack: $showTrack")
                        if (showTrack == true) {
                            val points = getPoints()
                            if (points != null) {
                                // Set options for the resulting line layer.
                                val polylineAnnotationOptions: PolylineAnnotationOptions =
                                    PolylineAnnotationOptions()
                                        .withPoints(points)
                                        // Style the line that will be added to the map.
                                        .withLineColor("#ee4e8b")
                                        .withLineWidth(5.0)
                                // Add the resulting line to the map.
                                polylineAnnotationManager.create(polylineAnnotationOptions)
                            }
                        } else {
                            println("delete all")
                            polylineAnnotationManager.deleteAll()
                        }

                        MapViewContainer(map)
                    }
                )
            }
        }
    }
}

@Composable
fun MapViewContainer(map: MapView) {
    AndroidView({ map })
}

@Composable
private fun rememberMap(longitude: Double, latitude: Double): MapView {
    val context = LocalContext.current

    val mapView = remember {
        val isDarkTheme = false // LocalDarkMode.current

        MapView(context).apply {
            val mapBoxMap = this.getMapboxMap()

            mapBoxMap.loadStyleUri(if (isDarkTheme) Style.DARK else Style.LIGHT)

            mapBoxMap.setCamera(
                cameraOptions = cameraOptions {
                    center(Point.fromLngLat(longitude, latitude))
                    zoom(12.0)
                }
            )
        }
    }

    return mapView
}