package gameLogic

import java.awt.Point

class Player(private val type: String, val starts: Boolean) {
    val pieces = mutableListOf<Piece>()
    var onBench: Int = 0


    init {
        generateToBench(type)
    }
    private fun generateToBench(type: String) {
        for(i in 0 until 5)
            pieces.add(Piece(type,-1,-1,if(starts) 0.0 else 180.0))
        onBench = 5
    }
    fun benchPiece(p: Piece) {
        p.gridLoc = Point(-1, -1)
        p.rotation = if (type == "Elephant") 0.0 else 180.0
        p.benched = true
        onBench++
    }

    override fun toString(): String {
        return type
    }
}