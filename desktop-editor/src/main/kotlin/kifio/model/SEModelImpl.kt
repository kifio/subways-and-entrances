package kifio.model

import kifio.readMetroMap
import kifio.subway.data.Entrance
import kifio.subway.data.Station

class SEModelImpl : SEModel {

    private val metroMap = readMetroMap(emptyArray())

    override fun getStations(): Collection<Station> = metroMap.keys

    override fun getStationsJson(): String {

    }

    override fun getEntrancesJson(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateStation(station: Pair<Station, Collection<Entrance>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}