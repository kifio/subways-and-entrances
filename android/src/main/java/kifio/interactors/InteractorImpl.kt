package kifio.interactors

import android.content.Context
import com.google.gson.JsonObject
import kifio.MetroMap
import kifio.data.Entrance
import kifio.data.Station

class InteractorImpl : Interactor {
    override fun getGeoData(ctx: Context): Map<Station, List<Entrance>> =
            MetroMap.buildMap(ctx.assets.open("entrances.osm"), ctx.assets.open("stations.osm"))
}