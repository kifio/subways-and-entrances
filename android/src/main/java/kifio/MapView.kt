package kifio

import android.content.Context
import com.google.gson.JsonObject

interface MapView {
    fun addLayer(geoJsonData: String)
    fun getContext(): Context
}