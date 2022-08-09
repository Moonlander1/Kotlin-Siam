package gameLogic
class Rock(type: String, row: Int, col: Int) : GameObj(type,row,col) {

    override fun push(o: GameObj): Int {
        return -1
    }

}