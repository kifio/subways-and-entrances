package kifio.model

import kifio.DataManager
import kifio.subway.data.Entrance
import kifio.subway.data.Station

/**
 * DataManager for getting data parsed from .pbf files.
 * Android App does not actually works with .pbf files, required data must be ejected with parser module.
 */
interface SEModel: DataManager {

    override fun getStationsJson(): String

    override fun getEntrancesJson(): String

    fun getStations(): Collection<Station>

    fun updateStation(station: Pair<Station, Collection<Entrance>>)
}