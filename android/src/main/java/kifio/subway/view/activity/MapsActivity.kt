package kifio.subway.view.activity

import android.os.Bundle
import android.os.PersistableBundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import androidx.fragment.app.Fragment
import kifio.subway.R
import kifio.subway.view.fragment.GoogleMapsFragment
import kifio.subway.view.fragment.MapboxMapFragment
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
            hideFragment(GoogleMapsFragment::class.java.simpleName)
            showFragment(MapboxMapFragment::class.java.simpleName, ::newMapboxFragment)
        } else if (item.itemId == R.id.google) {
            hideFragment(MapboxMapFragment::class.java.simpleName)
            showFragment(GoogleMapsFragment::class.java.simpleName, ::newGoogleMapsFragment)
        }
        return false
    }

    private fun findFragment(tag: String) = supportFragmentManager.findFragmentByTag(tag)

    private fun<T: Fragment> showFragment(fragmentName: String, newInstance: () -> T) {
        val fragment = findFragment(fragmentName)
        if (fragment == null) {
            Timber.d("Add $fragmentName")
            supportFragmentManager.beginTransaction().add(R.id.container,
                    newInstance(), fragmentName).commit()
        } else if (fragment.isDetached) {
            Timber.d("Attach $fragmentName")
            supportFragmentManager.beginTransaction().attach(fragment).commit()
        }
    }

    private fun hideFragment(fragmentName: String) {
        val fragment = findFragment(fragmentName)
        if (fragment != null && fragment.isAdded) {
            Timber.d("Detach $fragmentName")
            supportFragmentManager.beginTransaction().detach(fragment).commit()
        }
    }

    private fun newGoogleMapsFragment() = GoogleMapsFragment.newInstance()

    private fun newMapboxFragment() = MapboxMapFragment.newInstance()

    companion object {
        private const val SELECTED_ITEM = "PAGE"
        const val INITIAL_LAT = 55.7558
        const val INITIAL_LON = 37.6173
    }
}
