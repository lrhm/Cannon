package core.engine

import src.main.kotlin.core.engine.Engine
import src.main.kotlin.core.engine.Move
import src.main.kotlin.core.engine.Position
import src.main.kotlin.core.engine.otherPlayer
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

open class Player(val player: Int, val type: Type) {


    open fun getMove(engine: Engine): Move {

        val moves = engine.getPossibleMoves(player)

        return moves.random()

    }


    enum class Type {
        Human, AI
    }


}


class Node(
    val state: Board, val engine: Engine, val player: Int, val depth: Int,
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

    val random: Random = Random(System.currentTimeMillis())

    lateinit var parent: Node

//    fun toStr(): String {
//
//    }

    fun isTerminalState(): Boolean {

        if (engine.getPossibleMoves(player, state).isEmpty())
            return true
        if (state.isTownDead(player) && engine.turnsPassed > 2)
            return true

        return false

    }

    lateinit var myMoves: List<Move>

    lateinit var enemyMoves: List<Move>


    fun calcMoves() {
        if (::myMoves.isInitialized)
            return
        myMoves = engine.getPossibleMoves(player, state)
        enemyMoves = engine.getPossibleMoves(player.otherPlayer(), state)
    }


    fun calcDistance(position: Position, toPosition: Position): Double {

        return sqrt(
            ((position.i - toPosition.i) * (position.i - toPosition.i) +
                    (position.j - toPosition.j) * (position.j - toPosition.j)).toDouble()
        )
    }

    fun calcMovementTowardEnemyTownValue(): Double {

        val enemyTown = state.getTownPosition(player.otherPlayer())

        if (enemyTown != null && move != null && move!!.from.i != -1 && move!!.to.i != -1) {

            val distanceBefore = calcDistance(move!!.from, enemyTown!!)
            val disanceNow = calcDistance(move!!.to, enemyTown)

            return disanceNow - distanceBefore
        }
        return 0.0
    }

    fun evaluateState(): Int {

        calcMoves()



        if (myMoves.isEmpty() || state.isTownDead(player) || state.pawnCount(player) == 0) // total lost
            return -200

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

        val distanceToEnemyTown = state.calcDistanceToEnemyTown(player)
        val distanceToMyTown = state.calcDistanceToMyTown(player)

        val enemyDistanceToMyTown = state.calcDistanceToMyTown(player.otherPlayer())

        val progressTowardEnemyTown = 0//calcMovementTowardEnemyTownValue()


        var result =
            ((myMoves.size.toFloat() / enemyMoves.size) * 2 + 4 * (myCannons.toFloat() / enemyCannons) +
                    30 * (state.pawnCount(player).toFloat() / state.pawnCount(player.otherPlayer())) +
                    (6 * (shootCnt.toFloat() / eShootCnt))
                    + 2 * (captureCnt.toFloat() / eCaptureCnt)
                    + 4 * (possibleShots / enemyPossibleShots)
                    + 1 * (random).nextInt(4)
                    + 1 * (progressTowardEnemyTown)
                    )



        println("Evaluation is $result")
        return result.roundToInt()
    }


    var chNodes: List<Node>? = null

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


}


class MinMaxPlayer(player: Int, val maxDepth: Int = 8) : Player(player, Type.AI) {


    fun doAlphaBeta(node: Node, depth: Int, alpha: Int, beta: Int): Int {


        val children = node.getChildNodes()

        if (depth == 0 || node.isTerminalState())
            return node.evaluateState()


        var score = Int.MIN_VALUE
        var value = Int.MIN_VALUE

        var mAlpha = alpha



        for (child in children) {
            child.parent = node
            value = -1 * doAlphaBeta(child, depth - 1, -beta, -mAlpha)

            if (value > score) {
                score = value
                node.bestMove = child.move!!
            }

            if (value > alpha)
                mAlpha = score
//            if (score >= beta)
//                break
        }
        return score


    }

    override fun getMove(engine: Engine): Move {

        val timeStamp = System.currentTimeMillis()

        val parentNode = Node(
            engine.board, engine, engine.playerTurn, 0, true, Int.MIN_VALUE, Int.MIN_VALUE
        )
        val node = doAlphaBeta(
            parentNode, maxDepth, Int.MIN_VALUE, Int.MAX_VALUE
        )

        println("It took ${System.currentTimeMillis() - timeStamp}")
//        println("node is $parentNode with value $node children ${parentNode.bestMove}")

        if (parentNode.bestMove == null)
            return engine.getPossibleMoves(engine.playerTurn).random()
        return parentNode.bestMove!!

        val moves = engine.getPossibleMoves(player)

        return moves.random()
    }


}


class AlphaBetaIDPlayer(player: Int, val maxDepth: Int = 6) : Player(player, Type.AI) {


    val transpositionTable = LRUCache<Node>(1000)

    fun doAlphaBeta(node: Node, depth: Int, alpha: Int, beta: Int): Int {


        val children = node.getChildNodes()



        if (depth == 0 || node.isTerminalState())
            return node.evaluateState()


        var score = Int.MIN_VALUE
        var value = Int.MIN_VALUE

        var mAlpha = alpha



        children.sortBy {

            val node = transpositionTable.get(it.state.toStr())

            if (node != null) {
                return@sortBy node.score
            }

            0
        }

        for (child in children) {

            val hashedNode = transpositionTable.get(child.state.toStr())

            if (hashedNode != null) {

                value = node.score
            } else
                value = -1 * doAlphaBeta(child, depth - 1, -beta, -mAlpha)

            child.score = value

            if (value > score) {
                score = value
                node.bestMove = child.move!!
                transpositionTable.put(node.state.toStr(), node)
            }

            if (value > alpha)
                mAlpha = score
            if (score >= beta)
                break
        }
        return score


    }

    override fun getMove(engine: Engine): Move {

        val timeStamp = System.currentTimeMillis()

        val parentNode = Node(
            engine.board, engine, engine.playerTurn, 0, true, Int.MIN_VALUE, Int.MIN_VALUE
        )
        val node = doAlphaBeta(
            parentNode, maxDepth, Int.MIN_VALUE, Int.MAX_VALUE
        )

        for (i in 1..maxDepth) {

            println("ID ${i} starts")
            doAlphaBeta(parentNode, i, Int.MIN_VALUE, Int.MIN_VALUE)

        }

        println("It took ${System.currentTimeMillis() - timeStamp}")
//        println("node is $parentNode with value $node children ${parentNode.bestMove}")

        if (parentNode.bestMove == null)
            return engine.getPossibleMoves(engine.playerTurn).random()
        return parentNode.bestMove!!

        val moves = engine.getPossibleMoves(player)

        return moves.random()
    }


}


class AlphaBetaPlayer(player: Int, val maxDepth: Int = 8) : Player(player, Type.AI) {


//    val transpositionTable = LRUCache<String, Node?>(1000)

    fun doAlphaBeta(node: Node, depth: Int, alpha: Int, beta: Int): Int {


        val children = node.getChildNodes()



        if (depth == 0 || node.isTerminalState())
            return node.evaluateState()


        var score = Int.MIN_VALUE
        var value = Int.MIN_VALUE

        var mAlpha = alpha


//        children.sortBy {
//
//            transpositionTable.get(it.state.toStr())
//
//            3
//        }

        for (child in children) {
            value = -1 * doAlphaBeta(child, depth - 1, -beta, -mAlpha)


            if (value > score) {
                score = value
                node.bestMove = child.move!!
//                transpositionTable.put(node.state.toStr(), node)
            }

            if (value > alpha)
                mAlpha = score
            if (score >= beta)
                break
        }
        return score


    }

    override fun getMove(engine: Engine): Move {

        val timeStamp = System.currentTimeMillis()

        val parentNode = Node(
            engine.board, engine, engine.playerTurn, 0, true, Int.MIN_VALUE, Int.MIN_VALUE
        )
        val node = doAlphaBeta(
            parentNode, maxDepth, Int.MIN_VALUE, Int.MAX_VALUE
        )

        println("It took ${System.currentTimeMillis() - timeStamp}")
//        println("node is $parentNode with value $node children ${parentNode.bestMove}")

        if (parentNode.bestMove == null)
            return engine.getPossibleMoves(engine.playerTurn).random()
        return parentNode.bestMove!!

        val moves = engine.getPossibleMoves(player)

        return moves.random()
    }


}

fun String.toPlayerType(): Player.Type {

    if (this == "AI")
        return Player.Type.AI

    return Player.Type.Human
}



