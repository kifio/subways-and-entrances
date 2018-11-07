package kifio.model

class Station(val name: String, val color: String?, val lat: Double, val lon: Double) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Station

        if (name != other.name && color != other.color) return false

        return true
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + (name.hashCode())
        hash = 31 * hash + (if (color == null) 0 else color?.hashCode())
        return hash;
    }

    override fun toString() = "$name,$color,$lat,$lon"
}
