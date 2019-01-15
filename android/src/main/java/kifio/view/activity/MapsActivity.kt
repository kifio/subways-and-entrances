package kifio.view.activity

import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kifio.R
import kifio.view.fragment.GoogleMapsFragment
import kifio.view.fragment.MapboxMapFragment
import kotlinx.android.synthetic.main.activity_maps.*
import timber.log.Timber

/**
 * Host activity for all MapFragments
 */
class MapsActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        navigation.setOnNavigationItemSelectedListener(this)
        if (savedInstanceState != null) {
            navigation.selectedItemId = savedInstanceState.getInt(SELECTED_ITEM)
        } else {
            navigation.selectedItemId = R.id.mapbox
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        outState?.putInt(SELECTED_ITEM, navigation.selectedItemId)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mapbox) {
            supportFragmentManager.beginTransaction().replace(R.id.container,
                    getMapboxFragment(), MapboxMapFragment::class.java.simpleName).commit()
        } else if (item.itemId == R.id.google) {
            supportFragmentManager.beginTransaction().replace(R.id.container,
                    getGoogleMapsFragment(), GoogleMapsFragment::class.java.simpleName).commit()
        }

        return false
    }

    private fun getMapboxFragment(): Fragment {
        Timber.d(MapboxMapFragment::class.java.simpleName)
        return supportFragmentManager.findFragmentByTag(MapboxMapFragment::class.java.simpleName)
                ?: MapboxMapFragment.newInstance()
    }

    private fun getGoogleMapsFragment(): Fragment {
        Timber.d(GoogleMapsFragment::class.java.simpleName)
        return supportFragmentManager.findFragmentByTag(GoogleMapsFragment::class.java.simpleName)
                ?: GoogleMapsFragment.newInstance()
    }

    companion object {
        private const val SELECTED_ITEM = "PAGE"
    }
}
