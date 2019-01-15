package kifio.data.geojson

import com.google.gson.*
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.GeoJson
import java.lang.reflect.Type

/**
 * Simple implementation of GeoJson object for subway objects collection.
 */
class GeoJsonImpl : GeoJson {

    private val type = "FeatureCollection"

    private lateinit var features: List<Feature>

    fun setFeatures(features: List<Feature>) {
        this.features = features
    }

    override fun toJson(): String {
        return GsonBuilder()
                .registerTypeAdapter(GeoJsonImpl::class.java, CustomSerializer())
                .create().toJson(this)
    }

    override fun bbox(): BoundingBox? = null

    override fun type() = type

    private class CustomSerializer : JsonSerializer<GeoJsonImpl> {

        override fun serialize(src: GeoJsonImpl?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            val json = JsonObject()
            json.add("type", context?.serialize(src?.type))
            json.add("features", context?.serialize(src?.features))
            return json
        }

    }
}