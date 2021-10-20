package core.engine

import src.main.kotlin.core.engine.*
import kotlin.math.sqrt

class Board {

    /**
     * the board of the game
     * array of 10 by 10,
     * 0 means an empty spot
     * 1 means a soldier for player 0 (Black)
     * 2 means a soldier for player 1 (Red)
     * 3 means a town for player 0
     * 4 means a town for player 1
     */
    val board = Array(10) {
        IntArray(10) { 0 }
    }

    fun toStr(): String{

        var str = ""
        for (i in 0..9)
            for (j in 0..9)
                str = "${str}${board[i][j]}"

        return str
    }

    fun copy(): Board {


        val newBoard = Board()
        for (i in 0..9)
            for (j in 0..9)
                newBoard.board[i][j] = this.board[i][j]

//        newBoard.townDeads = arrayOf(null, null)
//        newBoard.cannonCounts = arrayOf(null, null)
//        newBoard.pawnCount = arrayOf(null, null)

        return newBoard
    }

//    var townDeads = arrayOf<Boolean?>(null, null)
//    var cannonCounts = arrayOf<Int?>(null, null)
//    var pawnCount = arrayOf<Int?>(null, null)


    init {


        for (j in arrayOf(1, 3, 5, 7, 9))
            for (i in arrayOf(1, 2, 3))
                board[i][j] = 2

        for (j in arrayOf(0, 2, 4, 6, 8))
            for (i in arrayOf(6, 7, 8))
                board[i][j] = 1


    }

    fun getTownPosition(player: Int): Position? {

        val row = if (player == 0) 9 else 0
        for (i in 1..8) {
            if (board[row][i] == 3 + player)
                return Position(row, i)
        }

        return null
    }


    fun calcDistance(i: Int, j: Int, i1: Int, j1: Int): Double {
        return sqrt(((i - i1).times(i - i1) + (j - j1).times(j - j1)).toDouble())
    }

    fun calcDistanceToEnemyTown(player: Int): Double {

        val enemyTownPostion = getTownPosition(player.otherPlayer())!!

        var pawnCount = 0
        var totalDistance = 0.0

        for (i in 0..9)
            for (j in 0..9) {
                if (board[i][j].isFriendlySoldier(player)) {
                    totalDistance += calcDistance(i, j, enemyTownPostion.i, enemyTownPostion.j)
                    pawnCount++
                }
            }


        return totalDistance / pawnCount
    }

    fun calcDistanceToMyTown(player: Int): Double {

        val enemyTownPostion = getTownPosition(player)!!

        var pawnCount = 0
        var totalDistance = 0.0

        for (i in 0..9)
            for (j in 0..9) {
                if (board[i][j].isFriendlySoldier(player)) {
                    totalDistance += calcDistance(i, j, enemyTownPostion.i, enemyTownPostion.j)
                    pawnCount++
                }
            }


        return totalDistance / pawnCount
    }


    fun isTownDead(playerTurn: Int): Boolean {

//        if (townDeads[playerTurn] != null)
//            return townDeads[playerTurn]!!

        val row = if (playerTurn == 0) 9 else 0

        var isTownDead = true
        for (i in 1..9) {
            if (board[row][i] == 3 + playerTurn)
                isTownDead = false
        }

        if (isTownDead) {
//            townDeads[playerTurn] = true
            return true
        }


//        townDeads[playerTurn] = false
        return false

    }

    fun numberOfPossibleShots(player: Int): Int {
        var counter = 0
        for (i in 0..9)
            for (j in 0..9) {

                if (board[i][j].isFriendlySoldier(player)) {

                    for (k in -1..1)
                        for (l in -1..1) {
                            if (l == -1 && k == 0)
                                continue
                            if (l == 1 && k == 0)
                                continue
                            try {
                                if (board[i + k][j + l].isFriendlySoldier(player) and
                                    board[i + 2 * k][j + 2 * l].isFriendlySoldier(player)

                                ) {


                                    if ((i + 4 * k) in 0..9
                                        && (j + 4 * l) in 0..9
                                    )
                                        counter++


                                    if ((i + 5 * k) in 0..9
                                        && (j + 5 * l) in 0..9
                                    )
                                        counter++
                                }
                            } catch (e: Exception) {
                            }

                        }


                }

            }

        return counter
    }


    fun numberOfCannons(player: Int): Int {

//        if (cannonCounts[player] != null)
//            return cannonCounts[player]!!

        var counter = 0
        for (i in 0..9)
            for (j in 0..9) {

                if (board[i][j].isFriendlySoldier(player)) {

                    for (k in -1..1)
                        for (l in -1..1) {
                            if (l == -1 && k == 0)
                                continue
                            if (l == 1 && k == 0)
                                continue
                            try {
                                if (board[i + k][j + l].isFriendlySoldier(player) and
                                    board[i + 2 * k][j + 2 * l].isFriendlySoldier(player)

                                ) {
                                    counter++
                                }
                            } catch (e: Exception) {
                            }

                        }


                }

            }

//        cannonCounts[player] = counter

        return counter
    }


    fun pawnCount(player: Int): Int {

//        if (pawnCount[player] != null)
//            return pawnCount[player]!!

        var cnt = 0
        for (i in 0..9)
            for (j in 0..9)
                if (board[i][j].isFriendlySoldier(player))
                    cnt++

//        pawnCount[player] = cnt
        return cnt
    }


    fun printBoard() {

        print("     A    B   C   D   E   F   G   H   I   J\n")


        for (i in 0..9) {
            print("${i}  ")
            for (j in 0..9) {
                print("${board[i][j]}   ")
            }
            println("")

        }
    }

}



