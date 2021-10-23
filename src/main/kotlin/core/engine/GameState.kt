package core.engine

import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.abs
import kotlin.math.sqrt

class GameState {

    /**
     * the board of the game
     * array of 10 by 10,
     * 0 means an empty spot
     * 1 means a soldier for player 0 (Black)
     * 2 means a soldier for player 1 (Red)
     * 3 means a town for player 0
     * 4 means a town for player 1
     */
    var board = Array(10) {
        IntArray(10) { 0 }
    }


    fun getZobristHash(){

    }

    fun toStr(): String {

        var str = ""
        for (i in 0..9)
            for (j in 0..9)
                str = "${str}${board[i][j]}"

        return str
    }

    fun copy(): GameState {


        val newBoard = GameState()
        for (i in 0..9)
            for (j in 0..9)
                newBoard.board[i][j] = this.board[i][j]


        return newBoard
    }


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

    fun calcMoveDistanceTowardEnemy(player: Int, move: Move): Double {

        if (move.type != Move.Type.Capture && move.type != Move.Type.Slide && move.type != Move.Type.Capture)
            return 0.0
        val enemyTownPostion = getTownPosition(player.otherPlayer())!!
        val i = move.to.i
        val j = move.to.j
        if (abs(
                enemyTownPostion.j - j
            ) > 5 && abs(enemyTownPostion.i - i) > 5
        )
            return 0.0

        return calcDistance(i, j, enemyTownPostion.i, enemyTownPostion.j) - calcDistance(
            move.from.i,
            move.from.j,
            enemyTownPostion.i,
            enemyTownPostion.j
        )
    }

    fun calcDistanceToEnemyTown(player: Int): Double {

        val enemyTownPostion = getTownPosition(player.otherPlayer())!!

        var pawnCount = 0
        var totalDistance = 0.0

        for (i in 0..9)
            for (j in 0..9) {
                if (board[i][j].isFriendlySoldier(player)) {
                    totalDistance += calcDistance(i, j, enemyTownPostion.i, enemyTownPostion.j) * if (abs(
                            enemyTownPostion.j - j
                        ) > 5
                    ) 2 else 1


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
//                            if (l == -1 && k == 0)
//                                continue
//                            if (l == 1 && k == 0)
//                                continue
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

    fun getHowCloseThePlayerIsToEnemyTown(player: Int): Int {
        println("calcing close ness factor")
        val weights = arrayOf(40, 30, 20, 10, 5, 3)
        var totalSum = 0

        for (i in 0..6) {
            totalSum += weights[i] * numberOfMyPawnsInIthStepOfEnemyTown(player, i)
        }

        return totalSum
    }


    fun getHowCloseAreWeDefending(player: Int): Int {
        val weights = arrayOf(20, 30, 20, 10, 5, 3)
        var totalSum = 0

        for (i in 0..4) {
            totalSum += weights[i] * numberOfMyPawnsInIthStepOfMyTown(player, i)
        }

        return totalSum
    }

    fun numberOfMyPawnsInIthStepOfMyTown(player: Int, step: Int): Int {

        val enemyTownPosition = getTownPosition(player)!!


        var count = 0
        if (enemyTownPosition.i == 0) {
            for (i in 0..step) {
                for (j in -1 * (i + 2)..1 * (i + 2)) {
                    try {
                        if (board[i + enemyTownPosition.i][j + enemyTownPosition.j].isFriendlySoldier(player))
                            count++
                    } catch (e: Exception) {

                    }
                }
            }
        }


        if (enemyTownPosition.i == 9) {
            for (i in 0..step) {
                for (j in -1 * (i + 2)..1 * (i + 2)) {
                    try {
                        if (board[enemyTownPosition.i - i][j + enemyTownPosition.j].isFriendlySoldier(player))
                            count++
                    } catch (e: Exception) {

                    }
                }
            }
        }


        return count

    }


    fun getHowCloserIsAMoveIZToZEnemiZ(player: Int, move: Move): Int {

        val enemyTownPosition = getTownPosition(player.otherPlayer())!!
        val weights = arrayOf(40, 30, 20, 10, 5, 3)
        var totalSum = 0

        var count = 0
        var step = 5

        if (abs(enemyTownPosition.i - move.to.i) > 6)
            return 0

        if (abs(enemyTownPosition.j - move.to.j) > 6)
            return 0

        if (enemyTownPosition.i == 0) {
            for (i in 0..step) {
                count = 0
                for (j in max(-1 * (i + 1), 0)..min(1 * (i + 1), 9)) {
                    if (i + enemyTownPosition.i == move.to.i && j + enemyTownPosition.j == move.to.j)
                        return weights[i]
                }
            }
        }


        if (enemyTownPosition.i == 9) {
            for (i in 0..step) {

                for (j in max(-1 * (i + 1), 0)..min(1 * (i + 1), 9)) {

                    if (move.to.j == j + enemyTownPosition.j && move.to.i == enemyTownPosition.i - i)
                        return weights[step]
                }
            }


        }

        return 0

    }


    fun numberOfMyPawnsInIthStepOfEnemyTown(player: Int, step: Int): Int {

        val enemyTownPosition = getTownPosition(player.otherPlayer())!!
        val weights = arrayOf(40, 30, 20, 10, 5, 3)
        var totalSum = 0

        var count = 0
        if (enemyTownPosition.i == 0) {
            for (i in 0..step) {
                count = 0
                for (j in max(-1 * (i + 1), 0)..min(1 * (i + 1), 9)) {

//                    println("trying board position ${i + enemyTownPosition.i} and ${j + enemyTownPosition.j} ")
                    try {
                        if (board[i + enemyTownPosition.i][j + enemyTownPosition.j].isFriendlySoldier(player))
                            count++
                    } catch (e: Exception) {

                    }
                }

                totalSum += count * weights[step]
            }
        }


        if (enemyTownPosition.i == 9) {
            for (i in 0..step) {
                count = 0
                for (j in max(-1 * (i + 1), 0)..min(1 * (i + 1), 9)) {

                    try {
                        if (board[enemyTownPosition.i - i][j + enemyTownPosition.j].isFriendlySoldier(player))
                            count++
                    } catch (e: Exception) {

                    }
                }
            }
            totalSum += count * weights[step]

        }

        return totalSum

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

//        print("  A    B   C   D   E   F   G   H   I   J\n")


        for (i in 0..9) {
//            print("${10 - i}  ")
            for (j in 0..9) {
                val end = if (j != 9) "," else ""
                print("${board[i][j]}  ${end} ")
            }
            println("")

        }
    }

}



