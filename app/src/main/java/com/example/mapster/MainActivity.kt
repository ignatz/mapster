package com.example.mapster

import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.content.res.XmlResourceParser
import android.os.Bundle
import android.util.Xml
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mapster.R.xml.example_track
import com.example.mapster.ui.theme.MapsterTheme
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

@Composable
private fun initMapView(token: String): MapView {
    val context = LocalContext.current

    val mapOptions = MapOptions.Builder().applyDefaultParams(context)
        .constrainMode(ConstrainMode.HEIGHT_ONLY)
        .glyphsRasterizationOptions(
            GlyphsRasterizationOptions.Builder()
                .rasterizationMode(GlyphsRasterizationMode.IDEOGRAPHS_RASTERIZED_LOCALLY)
                // Font family is required when the GlyphsRasterizationMode is set to IDEOGRAPHS_RASTERIZED_LOCALLY or ALL_GLYPHS_RASTERIZED_LOCALLY
                .fontFamily("sans-serif")
                .build()
        )
        .build()

    // plugins that will be loaded as part of MapView initialisation
    val plugins = listOf<Plugin>(
        // PLUGIN_LOGO_CLASS_NAME,
        // PLUGIN_ATTRIBUTION_CLASS_NAME,
    )

    // set token and tile store usage mode for this particular map view, these settings will overwrite the default value.
    val resourceOptions = ResourceOptions.Builder().applyDefaultParams(context)
        .accessToken(token)
        //.accessToken(private)
        .tileStoreUsageMode(TileStoreUsageMode.DISABLED)
        .build()

    // set initial camera position
    val initialCameraOptions = CameraOptions.Builder()
        .center(Point.fromLngLat(-122.4194, 37.7749))
        .zoom(12.0)
        .bearing(120.0)
        .build()

    val mapInitOptions =
        MapInitOptions(context, resourceOptions, mapOptions, plugins, initialCameraOptions, true)
    // create view programmatically
    return MapView(context, mapInitOptions)
}

class MainActivity : ComponentActivity() {
    // lateinit var customMapView : MapView
    private lateinit var polylineAnnotationManager: PolylineAnnotationManager

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
        } while (parser.eventType != XmlResourceParser.START_TAG);

        return getTrack(parser)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MapsterTheme {
                // A surface container using the 'background' color from the theme
                // Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                // Greeting("Android")
                // }
                Box {
                    initMapView(getString(R.string.mapbox_access_token))
                    val map = rememberMap(24.613272, 41.49277)
                    // Create an instance of the Annotation API and get the polyline manager.
                    val annotationApi = map?.annotations
                    polylineAnnotationManager = annotationApi.createPolylineAnnotationManager()
                    // Define a list of geographic coordinates to be connected.
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
                        polylineAnnotationManager?.create(polylineAnnotationOptions)
                    }

                    MapViewContainer(map)
                }
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
    // val accessToken = stringResource(R.string.mapbox_access_token)
    val context = LocalContext.current
    val isDarkTheme = false // LocalDarkMode.current

    val mapView = remember {
        // Plugin.Mapbox.getInstance(context, stringResource(R.string.mapbox_access_token))
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

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MapsterTheme {
        Greeting("Android")
    }
}