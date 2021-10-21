package core.engine

import src.main.kotlin.core.engine.Engine
import src.main.kotlin.core.engine.Move
import src.main.kotlin.core.engine.Position
import src.main.kotlin.core.engine.otherPlayer
import kotlin.math.roundToInt
import kotlin.random.Random

val random: Random = Random(System.currentTimeMillis())


class Node(
    val state: Board, val engine: Engine, var player: Int,
    val depth: Int,
    val isMax: Boolean,
    var alpha: Int,
    var beta: Int,
    var move: Move? = null,
    var score: Int = -500,

    ) {

    constructor(
        node: Node,
        move: Move,
        state: Board, engine: Engine, player: Int, depth: Int,
        isMax: Boolean,
        alpha: Int,
        beta: Int,
        score: Int = -500,
    ) : this(state, engine, player, depth, isMax, alpha, beta, move, score) {
        parent = node

    }


    lateinit var parent: Node


    fun isTerminalState(): Boolean {


        if (state.isTownDead(player) && engine.turnsPassed > 2)
            return true
        if (state.isTownDead(player.otherPlayer()) && engine.turnsPassed > 2)
            return true


        calcMoves()
        if (myMoves.isEmpty() || enemyMoves.isEmpty())
            return true


        return false

    }

    lateinit var myMoves: List<Move>

    lateinit var enemyMoves: List<Move>


    fun calcMoves() {
        if (::myMoves.isInitialized.not())
            myMoves = engine.getPossibleMoves(player, state)


//        myMoves = engine.getPossibleMoves(player, state)
        if (::enemyMoves.isInitialized.not())
            enemyMoves = engine.getPossibleMoves(player.otherPlayer(), state)
    }


    fun calcDistance(position: Position, toPosition: Position): Double {

        return ((position.i - toPosition.i) * (position.i - toPosition.i) +
                2 * (position.j - toPosition.j) * (position.j - toPosition.j)).toDouble()

    }

    fun calcMovementTowardEnemyTownValue(): Double {

        val enemyTown = state.getTownPosition(player.otherPlayer())

        if (enemyTown != null && move != null && move!!.from.i != -1 && move!!.to.i != -1) {

            val distanceBefore = calcDistance(move!!.from, enemyTown!!)
            val disanceNow = calcDistance(move!!.to, enemyTown)

            return distanceBefore - disanceNow
        }
        return 0.0
    }


    fun evaluateState(player: Int): Int {


        this.player = player

        myMoves = engine.getPossibleMoves(player, state)

        enemyMoves = engine.getPossibleMoves(player.otherPlayer(), state)

        if (myMoves.isEmpty() || state.isTownDead(player) || state.pawnCount(player) == 0) // total lost
            return -300

        if (state.isTownDead((player.otherPlayer())) || state.pawnCount(player.otherPlayer()) == 0 || enemyMoves.isEmpty()) // total win
            return 300

        val shootCnt = myMoves.filter { it.type == Move.Type.Shoot }.size
        var eShootCnt = enemyMoves.filter { it.type == Move.Type.Shoot }.size
        if (eShootCnt == 0)
            eShootCnt = 1

        val captureCnt = myMoves.filter { it.type == Move.Type.Capture }.size
        var eCaptureCnt = enemyMoves.filter { it.type == Move.Type.Capture }.size

        if (eCaptureCnt == 0)
            eCaptureCnt = 1

        val myCannons = state.numberOfCannons(player)

        var enemyCannons = state.numberOfCannons(player.otherPlayer())

        if (enemyCannons == 0)
            enemyCannons = 1

        val possibleShots = state.numberOfPossibleShots(player)
        var enemyPossibleShots = state.numberOfPossibleShots(player.otherPlayer())

        if (enemyPossibleShots == 0)
            enemyPossibleShots = 1

        var result =
            ((myMoves.size.toFloat() / enemyMoves.size) * 2 + 6 * (myCannons.toFloat() / enemyCannons) +
                    100 * (state.pawnCount(player).toFloat() / state.pawnCount(player.otherPlayer())) +
                    (6 * (shootCnt.toFloat() / eShootCnt))
                    + 2 * (captureCnt.toFloat() / eCaptureCnt)
                    + 4 * (possibleShots / enemyPossibleShots)
                    + 1 * (random).nextInt(4)
//                    + 1 * (progressTowardEnemyTown)
                    )

//        println("Evaluation is $result")
        return result.roundToInt()
    }

    fun evaluateState(): Int {

        calcMoves()


        if (myMoves.isEmpty() || state.isTownDead(player) || state.pawnCount(player) == 0) // total lost
            return -300

        if (state.isTownDead((player.otherPlayer())) || state.pawnCount(player.otherPlayer()) == 0 || enemyMoves.isEmpty()) // total win
            return 300

        val shootCnt = myMoves.filter { it.type == Move.Type.Shoot }.size
        var eShootCnt = enemyMoves.filter { it.type == Move.Type.Shoot }.size
        if (eShootCnt == 0)
            eShootCnt = 1

        val captureCnt = myMoves.filter { it.type == Move.Type.Capture }.size
        var eCaptureCnt = enemyMoves.filter { it.type == Move.Type.Capture }.size

        if (eCaptureCnt == 0)
            eCaptureCnt = 1

        val myCannons = state.numberOfCannons(player)

        var enemyCannons = state.numberOfCannons(player.otherPlayer())

        if (enemyCannons == 0)
            enemyCannons = 1

        val possibleShots = state.numberOfPossibleShots(player)
        var enemyPossibleShots = state.numberOfPossibleShots(player.otherPlayer())

        if (enemyPossibleShots == 0)
            enemyPossibleShots = 1

        var result =
            ((myMoves.size.toFloat() / enemyMoves.size) * 2 + 6 * (myCannons.toFloat() / enemyCannons) +
                    45 * (state.pawnCount(player).toFloat() / state.pawnCount(player.otherPlayer())) +
                    (6 * (shootCnt.toFloat() / eShootCnt))
                    + 2 * (captureCnt.toFloat() / eCaptureCnt)
                    + 4 * (possibleShots / enemyPossibleShots)
                    + 1 * (random).nextInt(4)
//                    + 1 * (progressTowardEnemyTown)
                    )


//        println("Evaluation is $result")
        return result.roundToInt()
    }


    var chNodes: List<Node>? = null

    fun getMoves(): MutableList<Move> {

        return engine.getPossibleMoves(player, state).toMutableList()
    }

    fun getChildNodes(): MutableList<Node> {


        if (chNodes != null)
            return (chNodes as MutableList<Node>?)!!

        val nodes = mutableListOf<Node>()

        val moves = engine.getPossibleMoves(player, state)

        for (move in moves) {

            val newBoard = state.copy()

            move.applyMove(newBoard, player)

            val node = Node(
                newBoard, engine, (player + 1).mod(2), depth - 1, isMax.not(), alpha, beta, move
            )
            node.parent = this
            nodes.add(
                node
            )

        }

        chNodes = nodes

        return nodes


    }

    var bestMove: Move? = null
//     var children: List<Node>

    var isExpanded = false
    var isPruned = false


    fun getNodeForMove(move: Move): Node {
        val newBoard = state.copy()

        move.applyMove(newBoard, player)

        val node = Node(
            newBoard, engine, (player + 1).mod(2), depth - 1, isMax.not(), alpha, beta, move
        )
        node.parent = this

        return node

    }


}
