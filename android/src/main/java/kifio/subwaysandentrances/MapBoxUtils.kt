package kifio.subwaysandentrances

import android.content.Context
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.SupportMapFragment

object MapBoxUtils {

    fun newFragment(ctx: Context): SupportMapFragment {
        return SupportMapFragment.newInstance(getOptions(ctx))
    }

    private fun getOptions(ctx: Context) = MapboxMapOptions()
            .styleUrl(ctx.getString(R.string.mapbox_style_url))
            .camera(getCameraPosition(LatLng(55.7558, 37.6173)))

    private fun getCameraPosition(postion: LatLng) = CameraPosition.Builder()
            .target(postion)
            .zoom(9.0)
            .build()
}