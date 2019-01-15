package kifio.view

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import kifio.view.fragment.GoogleMapsFragment
import kifio.view.fragment.MapboxMapFragment
import timber.log.Timber

class MapFragmentAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    companion object {
        private const val COUNT = 2
    }

    override fun getCount() = COUNT

    // getItem(i: Int) called only if fragment wasn't added to FragmentManager
    override fun getItem(i: Int): Fragment {
        Timber.d("getItem($i)")
        return if (i == 0) MapboxMapFragment.newInstance()
            else GoogleMapsFragment.newInstance()
    }
}