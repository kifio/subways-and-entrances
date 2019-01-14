package kifio

import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.SupportMapFragment

class MapFragment : SupportMapFragment() {

    companion object {
        fun newInstance(mapOptions: MapboxMapOptions) = SupportMapFragment.newInstance(mapOptions)
    }

    override fun onMapReady(mapboxMap: MapboxMap?) {
        super.onMapReady(mapboxMap)
        mapboxMap?.uiSettings?.isRotateGesturesEnabled = false
    }
}