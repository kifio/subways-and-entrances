package kifio

import javafx.collections.FXCollections
import javafx.concurrent.Worker
import javafx.scene.web.WebEngine
import netscape.javascript.JSObject;
import kifio.geojson.GeoJson
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.Executors

class SEPresenter(val view: SEView) {

    private var mapReady = false
    private lateinit var doc: JSObject
    private lateinit var engine: WebEngine
    val list = FXCollections.observableList(mutableListOf<String>())

    private fun getMapHtml(): String {
        val html = String(Files.readAllBytes(Paths.get("src/main/res/map.html")))
        val js = String(Files.readAllBytes(Paths.get("src/main/res/object_manager_geojson.js")))
        val apikey = String(Files.readAllBytes(Paths.get("src/main/res/yandex_maps_api_key.txt")))
        return String.format(html, apikey, js)
    }

    fun showMap(engine: WebEngine) {
        this.engine = engine
        loadMap(getMapHtml())
        initCommunication()
    }

    private fun loadMap(html: String) {
        engine.loadContent(html)
        engine.loadWorker.stateProperty().addListener { _, _, newValue ->
            mapReady = newValue == Worker.State.SUCCEEDED
            println(mapReady)
        }
    }

    private fun initCommunication() {
        engine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (newState == Worker.State.SUCCEEDED) {
                println("initCommunication")
                doc = engine.executeScript("window") as JSObject
                loadEntrancesOffline()
            }
        }
    }

    private fun loadEntrancesOffline() {
        println("loadEntrancesOffline")
        val useCase = GetGeoJsonAndPureData(FileInputStream("../android/src/main/assets/entrances.osm"),
            FileInputStream("../android/src/main/assets/stations.osm"))
        list.setAll(useCase.getStations().map { it.name })
        updateMap(useCase.getStationsGeoJson(), useCase.getEntrancesGeoJson())
        /* val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val useCase = GetGeoJsonAndPureData(FileInputStream("../android/src/main/assets/entrances.osm"),
                FileInputStream("../android/src/main/assets/stations.osm"))
            list.setAll(useCase.getStations().map { it.name })
            updateMap(useCase.getStationsGeoJson(), useCase.getEntrancesGeoJson())
        } */
    }

    fun updateMap(stations: GeoJson, entrances: GeoJson) {

    }

    private fun invokeJS(str: String) {

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
