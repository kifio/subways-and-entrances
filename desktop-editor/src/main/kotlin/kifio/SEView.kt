package kifio

import tornadofx.*
import javafx.scene.web.WebView

class SEView : View() {

    private val presenter = SEPresenter(this)

    override val root = borderpane {
        left {
            webview {
                presenter.showMap(engine)
            }
        }

        right {
            listview(presenter.list) {

            }
        }
    }
}
