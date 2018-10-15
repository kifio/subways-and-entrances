package kifio

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.*
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import android.graphics.*
import android.graphics.drawable.*
import java.io.*
import java.net.*
import java.lang.*
import com.google.gson.*
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import timber.log.*
import kotlinx.coroutines.experimental.*
import com.mapbox.mapboxsdk.style.sources.*
import com.mapbox.mapboxsdk.style.layers.*
import kifio.Common.generateToken
import kifio.model.Station

class MapsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        setMapBoxFragment()
    }

    private fun setMapBoxFragment() {
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        val fragment = SupportMapFragment.newInstance(MapboxMapOptions()
                .camera(getCameraPosition(LatLng(55.7558, 37.6173))))
        supportFragmentManager.beginTransaction()
                .add(R.id.content, fragment, SupportMapFragment::class.java.simpleName).commit()
        fragment.getMapAsync {
            loadData(it)
        }
    }

    private fun getCameraPosition(postion: LatLng) = CameraPosition.Builder()
            .target(postion)
            .zoom(9.0)
            .build()

    private fun loadData(mapboxMap: MapboxMap) {
        GlobalScope.launch {
            val token = generateToken(getAssets().open("pkey.json"))
            loadEntitites(mapboxMap, token, "stations", ::getSubwayIcon)
            loadEntitites(mapboxMap, token, "entrances", ::getSubwayIcon)
        }
    }

    private fun loadEntitites(mapboxMap: MapboxMap, token: String, layer: String, getIcon: (Int) -> Drawable) {
        Timber.d("${Common.baseUrl}/$layer.json?access_token=$token")
        val url = URL("${Common.baseUrl}/$layer.json?access_token=$token")
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()
        if (connection.responseCode == 200) {
            val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            runOnUiThread{ drawEntities(mapboxMap, response, getIcon, layer) }
        }
    }

    private fun drawEntities(mapboxMap: MapboxMap, responseString: String, getIcon: (Int) -> Drawable, layer: String) {
        val json = JsonParser().parse(responseString).asJsonObject
        val features = FeatureCollection.fromFeatures(
                json.entrySet().asSequence()
                        .map { entry -> buildFeature(entry.value.asJsonObject) }.toList())

        mapboxMap.addSource(GeoJsonSource("$layer-source", features))
        mapboxMap.addImage("$layer-image", getBitmap(getIcon(0)))
        mapboxMap.addLayer(SymbolLayer("$layer-layer", "$layer-source")
                .withProperties(PropertyFactory.iconImage("$layer-image"),
                        PropertyFactory.iconSize(0.5f)))
    }

    private fun buildFeature(jsonElement: JsonObject): Feature {
        Timber.d(jsonElement.toString())
        val lat: Double = jsonElement.get("lat").asDouble
        val lon: Double = jsonElement.get("lon").asDouble
        return Feature.fromGeometry(Point.fromLngLat(lon, lat))
    }

    private fun getSubwayIcon(stub: Int) = getDrawable(R.drawable.metro)

    private fun getEntranceIcon(ref: Int) = resources.getIdentifier("metro_$ref",
            "drawable", packageName)

    private fun getBitmap(vectorDrawable: Drawable): Bitmap {
        val width = vectorDrawable.intrinsicWidth
        val height = vectorDrawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }

}
