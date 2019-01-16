package kifio.subway.data

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import kifio.DataConstants

class Station(val name: String,
              val color: String,
              val lat: Double,
              val lon: Double) : MapData {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Station

        if (name != other.name && color != other.color) return false

        return true
    }

    override fun getIconId() = color

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + name.hashCode()
        hash = 31 * hash + color.hashCode()
        return hash;
    }

    override fun toString() = "$name,$color,$lat,$lon"

    override fun toFeature(): Feature {
        val feature = Feature.fromGeometry(Point.fromLngLat(lon, lat))
        feature.addStringProperty(DataConstants.NAME, name)
        feature.addStringProperty(DataConstants.COLOR, color)
        feature.addStringProperty(DataConstants.CLASS, Station::class.java.simpleName)
        feature.addStringProperty(DataConstants.ICON, getIconId())
        return feature
    }
}
