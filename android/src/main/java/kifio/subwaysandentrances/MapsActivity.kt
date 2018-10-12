package kifio.subwaysandentrances

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.mapbox.mapboxsdk.Mapbox

class MapsActivity : AppCompatActivity() {

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

        }
    }
}
