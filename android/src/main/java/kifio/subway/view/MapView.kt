package kifio.subway.view

interface MapView {
    fun addStationsLayer(geoJsonData: String?)
    fun addEntrancesLayer(geoJsonData: String?)
}