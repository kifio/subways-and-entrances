package kifio

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
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
import java.util.*
import com.google.gson.*
import timber.log.*
import kotlinx.coroutines.experimental.*
import android.widget.*
import com.mapbox.mapboxsdk.style.sources.*
import com.mapbox.mapboxsdk.style.layers.*

class MapsActivity : AppCompatActivity() {

    private lateinit var mapboxMap: MapboxMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        setMapBoxFragment()
    }

    private fun setMapBoxFragment() {
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        val fragment = MapBoxUtils.newFragment(this)
        supportFragmentManager.beginTransaction()
                .add(R.id.content, fragment, MapBoxUtils::class.java.simpleName).commit()
        fragment.getMapAsync {
            mapboxMap = it
            loadData()
        }
    }

    private fun loadData() {
        GlobalScope.launch {
            val token = generateToken(getAssets().open("pkey.json"))
            setStations(token)
        }
    }

    private fun setStations(token: String) {
        Timber.d("${Common.baseUrl}/stations.json?access_token=$token")
        val url = URL("${Common.baseUrl}/stations.json?access_token=$token")
        val response = getData(url, Stations::class.java) ?: return
        mapboxMap.addSource(GeoJsonSource("stations-source",
            FeatureCollection.fromFeatures(response.stations.values.map {
            Feature.fromGeometry(Point.fromLngLat(it.lat, it.lon))
        }.toList())))
        mapboxMap.addImage("stations-image", getBitmap(getDrawable(R.drawable.metro)))
        mapboxMap.addLayer(SymbolLayer("stations-layer", "stations-source")
        .withProperties(PropertyFactory.iconImage("stations-image")))
    }

    @Throws(IOException::class)
    private fun <T> getData(url: URL, cl: Class<T>): T? {
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()
        if (connection.getResponseCode() == 200) {
            val response = connection.getInputStream().bufferedReader().use(BufferedReader::readText)
            return Gson().fromJson(response, cl)
        } else {
            return null
        }
    }

    private fun getBitmap(vectorDrawable: Drawable): Bitmap {
        val width = vectorDrawable.getIntrinsicWidth()
        val height = vectorDrawable.getIntrinsicHeight() 
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        vectorDrawable.draw(canvas)
        return bitmap
    }

}
