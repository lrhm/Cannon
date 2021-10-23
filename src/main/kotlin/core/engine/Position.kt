package core.engine


data class Position(val row: Int, val column: Int) {
    val i = row
    val j = column

    override fun equals(other: Any?): Boolean {

        if (other is Position)
            return other.row == row && other.column == column
        return super.equals(other)
    }

    val iList = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")

    fun printChessNotation() {

        if (i == -1)
            return
        println(
            "${iList[j]}-${10 - i}"
        )

    }
}