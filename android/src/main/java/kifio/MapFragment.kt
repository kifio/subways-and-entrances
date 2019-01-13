package kifio

import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.SupportMapFragment

class MapFragment : SupportMapFragment() {

    override fun onMapReady(mapboxMap: MapboxMap?) {
        super.onMapReady(mapboxMap)
        mapboxMap?.uiSettings?.isRotateGesturesEnabled = false
    }
}