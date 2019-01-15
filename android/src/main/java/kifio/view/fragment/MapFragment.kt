package kifio.view.fragment

import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import com.mapbox.mapboxsdk.utils.MapFragmentUtils

class MapFragment : SupportMapFragment() {

    companion object {
        fun newInstance(mapboxMapOptions: MapboxMapOptions): MapFragment {
            val mapFragment = MapFragment();
            mapFragment.arguments = MapFragmentUtils.createFragmentArgs(mapboxMapOptions);
            return mapFragment
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap?) {
        super.onMapReady(mapboxMap)
        mapboxMap?.uiSettings?.isRotateGesturesEnabled = false
    }
}