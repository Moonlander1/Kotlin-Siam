package gameLogic


import getResource
import javafx.scene.image.Image

class Piece(type: String, row: Int, col: Int, baseR: Double = 0.0) : GameObj(type,row,col) {
    var rotation: Double = baseR
        set(r: Double) {
            field = r
            if(field > 360)
                field - 360
        }
    val selectedImg: Image = Image(getResource("/selected$type.png"))

    override fun push(o: GameObj): Int {
        var rot = rotation + 180
        if(rot >= 360 )
            rot -= 360
        return if(rot == (o as Piece).rotation)
            -1
        else if(rotation == (o as Piece).rotation && type == o.type)
            1
        else
            0
    }

    override fun toString(): String {
        return type
    }

}
