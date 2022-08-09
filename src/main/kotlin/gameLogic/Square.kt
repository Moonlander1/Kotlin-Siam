package gameLogic

import rendering.GUI

class Square(private var x: Double,private var y: Double, var row: Int, var col:Int) {
    /* VÁLTOZÓK */
    private var w: Double = GUI.tileS
    private var h: Double = GUI.tileS
    var busy:     Boolean = false
    var selected: Boolean = false


    fun insideSquare(a: Double, b: Double): Boolean = x < a && a < x + w && y < b && b < y + h
    fun reSize() {
        x = GUI.baseX + (GUI.baseW / 6) + (col * (GUI.tileS + GUI.gridS))
        y = GUI.baseY + (GUI.baseH / 6) + (row * (GUI.tileS + GUI.gridS))
        w = GUI.tileS
        h = GUI.tileS
    }
}