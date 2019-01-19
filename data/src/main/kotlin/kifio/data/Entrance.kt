package kifio.data

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import kifio.DataConstants

/**
 * Entrance of subway.
 * ref - is order number of entrance.
 */
class Entrance(val ref: Int?,
               val color: String,
               private val lat: Double,
               private val lon: Double) : MapData {

    override fun getLat() = lat

    override fun getLon() = lon

    override fun toString() = "$ref,$color,$lat,$lon"

    override fun toFeature(): Feature {
        val feature = Feature.fromGeometry(Point.fromLngLat(lon, lat))
        feature.addNumberProperty(DataConstants.REF, ref)
        feature.addStringProperty(DataConstants.COLOR, color)
        feature.addStringProperty(DataConstants.CLASS, Entrance::class.java.simpleName)
        feature.addStringProperty(DataConstants.ICON, getIconId())
        return feature
    }

    override fun getIconId() = ref.toString()
}
