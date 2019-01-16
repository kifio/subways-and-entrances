package kifio.subway.view

import kifio.subway.view.fragment.GoogleMapsFragment
import kifio.subway.view.fragment.MapboxMapFragment
import timber.log.Timber

class MapFragmentAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm) {

    companion object {
        private const val COUNT = 2
    }

    override fun getCount() = COUNT

    // getItem(i: Int) called only if fragment wasn't added to FragmentManager
    override fun getItem(i: Int): androidx.fragment.app.Fragment {
        Timber.d("getItem($i)")
        return if (i == 0) MapboxMapFragment.newInstance()
            else GoogleMapsFragment.newInstance()
    }
}