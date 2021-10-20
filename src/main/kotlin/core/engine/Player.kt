package core.engine

import core.engine.util.LruCache
import src.main.kotlin.core.engine.Engine
import src.main.kotlin.core.engine.Move
import src.main.kotlin.core.engine.Position
import src.main.kotlin.core.engine.otherPlayer
import kotlin.math.abs
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

    data class GameState(val score: Int, val finalized: Boolean, var bestMove: Move?, val depth: Int)

    val transpositionTable = LruCache<Int, GameState>(1000)

    fun doAlphaBeta(node: Node, depth: Int, alpha: Int, beta: Int): Int {


        val children = node.getChildNodes()

        if (depth == 0 || node.isTerminalState())
            return node.evaluateState()


        var score = Int.MIN_VALUE
        var value = Int.MIN_VALUE

        var mAlpha = alpha


//        val hit = children.filter { transpositionTable[it.state.toStr()] != null }

//        hit.sortedBy {
//            val stateVal = transpositionTable[it.state.toStr()]!!
//            stateVal.score * (if (stateVal.depth.mod(2) == depth.mod(2)) -1 else 1)
//        }

        children.sortBy {
            if (transpositionTable[it.hashCode()] != null) {
                val stateVal = transpositionTable[it.hashCode()]!!
                stateVal.score * (if (stateVal.depth.mod(2) == depth.mod(2)) 1 else -1)

            }
            0
        }


        for (child in children) {

//            val hashedNode = transpositionTable[child.state.toStr()]

//            if (hashedNode != null && hashedNode.finalized) {
//
//                value = -1 * hashedNode.score
//            } else
//
            value = -1 * doAlphaBeta(child, depth - 1, -beta, -mAlpha)

            child.score = value

            if (value > score) {
                score = value
                node.bestMove = child.move!!
            }

            if (value > alpha)
                mAlpha = score
            if (score >= beta)
                break
        }

//        if (depth > (transpositionTable[node.state.toStr()+"${node.player}"]?.depth ?: -1))
//            transpositionTable.put(
//                node.state.toStr()+"${node.player}",
//                GameState(score, score >= 300 || score <= -300, node.bestMove, depth)
//            )

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

        val startTime = System.currentTimeMillis()
        var d = 0
        while (System.currentTimeMillis() - startTime < 1000) {
            println("ID ${d} starts")
            doAlphaBeta(parentNode, d, Int.MIN_VALUE, Int.MIN_VALUE)
            d++
        }

        transpositionTable.evictAll()

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


        if (depth == 0 || node.isTerminalState())
            return node.evaluateState()

//        val children = node.getChildNodes()

        var score = Int.MIN_VALUE
        var value = Int.MIN_VALUE
        var mAlpha = alpha


        for (move in node.getMoves()) {

//            val newBoard = node.state.copy()

//            move.applyMove(newBoard, player)
//
//            val childNode = Node(
//                newBoard, node.engine, (player + 1).mod(2), depth - 1, node.isMax.not(), alpha, beta, move
//            )
//            childNode.parent = node

            val child = node.getNodeForMove(move)

            value = -1 * doAlphaBeta(child, depth - 1, -beta, -mAlpha)

            if (value > score) {
                score = value
                node.bestMove = move
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
            engine.board, engine, engine.playerTurn, maxDepth, true, Int.MIN_VALUE, Int.MIN_VALUE
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



