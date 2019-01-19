package kifio

import javafx.collections.ObservableList
import kifio.model.SEModelImpl
import tornadofx.*
import java.nio.file.Files
import java.nio.file.Paths

class SEPresenter(): MapPresenter {

    private val model = SEModelImpl()

    fun getStationsList(): ObservableList<String> {
        return model.getStations().map { it.name }.observable()
    }

    fun getMapHtml(): String {
        val html = String.format(String(Files.readAllBytes(Paths.get("desktop-editor/src/main/res/map.html"))),
                String(Files.readAllBytes(Paths.get("desktop-editor/src/main/res/yandex_maps_api_key.txt"))))
        println(html)
        return html
    }

    override fun loadEntrancesOffline() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateMap() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
//
//    fun getStationsPainter(): WaypointPainter<Waypoint> {
//        return  WaypointPainter<Waypoint>().apply {
//            waypoints = metroMap.keys.map { StationWaypoint(it) }.toSet()
//            setRenderer(getSubwayRenderer())
//        }
//    }
//
//    private fun getSubwayRenderer(): WaypointRenderer<Waypoint> {
//        return WaypointRenderer { g, map, waypoint -> drawIcon(g, waypoint) }
//    }
//
//    private fun drawIcon(g: Graphics2D, waypoint: Waypoint) {
//        val subwayWaypoint = waypoint as SubwayWaypoint
//        g.drawImage()
//    }
}