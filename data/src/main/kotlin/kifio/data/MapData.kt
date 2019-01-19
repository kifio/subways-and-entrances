package kifio.data

import com.mapbox.geojson.Feature

interface MapData {
    fun toFeature(): Feature
    fun getIconId(): String
    fun getLat(): Double
    fun getLon(): Double
}