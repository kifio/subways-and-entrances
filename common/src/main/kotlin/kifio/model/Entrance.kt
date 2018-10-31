package kifio.model

class Entrance(val id: String, val ref: Int?, val color: String?, val lat: Double, val lon: Double) {
    override fun toString(): String {
        return "$ref,$lat,$lon)"
    }
}
