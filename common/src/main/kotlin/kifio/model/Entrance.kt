package kifio.model

class Entrance(val ref: Int?, val color: String?, val lat: Double, val lon: Double) {
    override fun toString() ="$ref,$color,$lat,$lon)"
}
