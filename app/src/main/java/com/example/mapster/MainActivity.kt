package com.example.mapster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mapster.ui.theme.MapsterTheme
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.Plugin

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
        .zoom(9.0)
        .bearing(120.0)
        .build()

    val mapInitOptions =
        MapInitOptions(context, resourceOptions, mapOptions, plugins, initialCameraOptions, true)
    // create view programmatically
    return MapView(context, mapInitOptions)
}

class MainActivity : ComponentActivity() {
    // lateinit var customMapView : MapView

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
                    val map = rememberMap(-122.4194, 37.7749)
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
                    zoom(6.0)
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