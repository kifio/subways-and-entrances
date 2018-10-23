package kifio.model

class Entrance(val id: String, val ref: Int?, val color: String?, val lat: Double, val lon: Double) {
    var station: String? = null

    override fun toString(): String {
        return "Entrance(ref=$ref, lat=$lat, lon=$lon)"
    }
}