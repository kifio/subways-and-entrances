package kifio.interactors

import android.content.Context
import kifio.data.Entrance
import kifio.data.Station

interface Interactor {
    fun getGeoData(ctx: Context): Map<Station, List<Entrance>>
}