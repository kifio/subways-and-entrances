package kifio.subway.data

import com.mapbox.geojson.Feature

interface MapData {
    fun toFeature(): Feature
    fun getIconId(): String
}