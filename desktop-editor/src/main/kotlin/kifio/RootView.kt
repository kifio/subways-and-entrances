package kifio

import javafx.scene.control.SelectionMode
import javafx.scene.layout.HBox
import kifio.subway.data.Station
import tornadofx.*

class RootView: View() {

    override val root = HBox()
    val stations = mutableListOf<Station>().observable()

    init {
        stations.addAll(readMetroMap(emptyArray()).keys)
        listview(stations) {
            selectionModel.selectionMode = SelectionMode.MULTIPLE
        }
    }
}
