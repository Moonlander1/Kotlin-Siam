package gameLogic

import getResource
import rendering.GUI
import javafx.scene.image.Image
import java.awt.Point

abstract class GameObj(val type: String, row: Int, col: Int) {
    var drawLocX: Double? = null
    var drawLocY: Double? = null
    var gridLoc: Point? = Point(row,col)
        set(p: Point?)  {
            field = p
            calculateDrawLoc()
        }
    val img: Image = Image(getResource("/$type.png"))
    var benched: Boolean = true

    fun calculateDrawLoc() {
        if(gridLoc?.x!! != -1 && gridLoc?.y!! != -1) {
            drawLocX = (GUI.baseX + GUI.baseW / 6.0) + gridLoc?.x!! * (GUI.tileS + GUI.gridS)
            drawLocY = (GUI.baseY + GUI.baseH / 6.0) + gridLoc?.y!! * (GUI.tileS + GUI.gridS)
        }
    }

    fun update() {
        calculateDrawLoc()
    }
    abstract fun push(o: GameObj): Int;

    override fun toString(): String {
        return type
    }

}