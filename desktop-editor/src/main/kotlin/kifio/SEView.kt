package kifio

import tornadofx.*

class SEView : View(), MapView {

    private val presenter = SEPresenter(this)

    override val root = borderpane {
        left {
            webview {
                engine.loadContent(presenter.getMapHtml())
            }
        }

        right {
            listview(presenter.getStationsList()) {

            }
        }
    }

    override fun addStationsLayer(geoJsonData: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addEntrancesLayer(geoJsonData: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}