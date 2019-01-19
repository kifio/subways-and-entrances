package kifio.geojson

import com.google.gson.*
import com.mapbox.geojson.Feature
import java.lang.reflect.Type

/**
 * Simple implementation of GeoJson object for subway objects collection.
 */
class GeoJson {

    val type = "FeatureCollection"

    val features = mutableListOf<Feature>()

    fun setFeatures(features: List<Feature>) {
        this.features.addAll(features)
    }

    fun toJson(): String {
        return GsonBuilder()
                .registerTypeAdapter(GeoJson::class.java, CustomSerializer())
                .create().toJson(this)
    }

    private class CustomSerializer : JsonSerializer<GeoJson> {
        override fun serialize(src: GeoJson, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val json = JsonObject()
            json.add("type", context.serialize(src.type))
            json.add("features", context.serialize(src.features))
            return json
        }
    }
}