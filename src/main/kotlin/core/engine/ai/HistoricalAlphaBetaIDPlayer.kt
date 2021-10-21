package core.engine.ai

import core.engine.Node
import core.engine.Engine
import core.engine.Move
import core.engine.ai.evaluator.MaterialEvaluator
import core.engine.util.LruCache
import kotlinx.coroutines.*
import java.lang.Integer.max
import java.lang.Integer.min
import java.lang.Thread.sleep
import kotlin.collections.HashMap


class HistoricalAlphaBetaIDPlayer(player: Int, val maxDepth: Int = 8) : Player(player, Type.AI) {

    data class GameState(
        val value: Int,
        val flag: Flag,
        val depth: Int,
        val bestMaxMove: Move?,
        val bestMinMove: Move?
    ) {
        enum class Flag { Exact, LowerBound, UpperBound }
    }

    data class KillerData(
        var maxMove: Move?,
        var minMove: Move?
    )

    data class GameHistory(
        val minMoves: HashMap<Move, MoveHistory> = HashMap(),
        val maxMoves: HashMap<Move, MoveHistory> = HashMap()
    )


    data class MoveHistory(
        val value: Int,
        val depth: Int
    )

    val historyCache = LruCache<String, GameHistory>(1000000)
    val transpositionTable = LruCache<Int, GameState>(1000)
    val killerTable = LruCache<String, KillerData>(3000)

    var evaluator = MaterialEvaluator()

    override fun evaluateState(node: Node): Int {

        return evaluator.evaluateState(node, player)
    }

    fun doAlphaBeta(node: Node, depth: Int, alpha: Int, beta: Int, isMax: Boolean): Int {

        var mAlpha = alpha
        var mBeta = beta

//        var entry = transpositionTable[node.hashCode()]
//        if (entry != null && entry.depth >= depth) {
//            when (entry.flag) {
////                GameState.Flag.Exact -> {
////                    return entry.value
////                }
//                GameState.Flag.LowerBound -> {
//
//                    mAlpha = max(alpha, entry.value)
//                }
//
//                GameState.Flag.UpperBound -> {
//
//                    mBeta = min(beta, entry.value)
//
//                }
//
//            }
//
////            if (mAlpha > mBeta)
////                return entry.value
//        }

//        node.player = node.player.otherPlayer()
        if (depth == 0 || node.isTerminalState()) {
            return evaluateState(node)
        }


        var value = Int.MIN_VALUE


        var moves = node.getMoves()
        var killerMove = killerTable[node.state.toStr()]
        if (killerMove == null)
            killerMove = KillerData(null, null)

        var history = historyCache[node.state.toStr()]

        if (history == null)
            history = GameHistory()

        if (isMax) {
            value = Int.MIN_VALUE

//            val allHistoricalMoves =
//                history.maxMoves.toSortedMap(Comparator.comparingInt { history.maxMoves[it]!!.value })
//
//            var idx = 0
//
////            moves.removeAll(allHistoricalMoves.keys)
////            moves.addAll(allHistoricalMoves.keys)
//            for (move in allHistoricalMoves.keys) {
//                if (moves.remove(move)) {
//                    moves.add(idx, move)
//                    idx++
//                }
//            }

            moves.sortBy {
                history.maxMoves[it]?.value
            }
            if (killerMove.maxMove != null) {

                if (moves.remove(killerMove.maxMove))
                    moves.add(0, killerMove.maxMove!!)
            }




            for (move in moves) {
                val child = node.getNodeForMove(move)

                val score = if (history.maxMoves[move]?.depth ?: -1 >= depth - 1) {
//                    println("does history happen aggain and again?")
                    history.maxMoves[move]!!.value
                } else doAlphaBeta(child, depth - 1, mAlpha, mBeta, false)

                val historicalMove = history.maxMoves[move]
                if (depth - 1 > (historicalMove?.depth ?: -1))
                    history.maxMoves[move] = MoveHistory(score, depth - 1)

                if (score > value) {
                    node.bestMove = move

                }
                value = max(
                    value,
                    score
                )

                if (score >= mBeta) {
                    killerMove.maxMove = move
                    killerTable.put(node.state.toStr(), killerMove)
                    break
                }
                mAlpha = max(mAlpha, value)

            }

            historyCache.put(node.state.toStr(), history)


            return value

        } else {
            value = Int.MAX_VALUE

//            val allHistoricalMoves =
//                history.minMoves.toSortedMap(Comparator.comparingInt { history.maxMoves[it]!!.value })


//            moves.sortBy { history.minMoves[it]?.value }

            moves = moves.asReversed()

//            var idx = 0
//            for (move in allHistoricalMoves.keys.reversed()) {
//                if (moves.remove(move)) {
//                    moves.add(idx, move)
//                    idx++
//                }
//            }


            if (killerMove.minMove != null) {
                if (moves.remove(killerMove.minMove))
                    moves.add(0, killerMove.minMove!!)
            }

            for (move in moves) {


                val child = node.getNodeForMove(move)

                val historicalMove = history.minMoves[move]

                val score = if (historicalMove?.depth ?: -1 >= depth - 1) {
//                    println("does history happen aggain and again?")
                    history.minMoves[move]!!.value
                } else doAlphaBeta(child, depth - 1, mAlpha, mBeta, true)

                if (depth - 1 > (historicalMove?.depth ?: -1))
                    history.minMoves[move] = MoveHistory(score, depth-1)


                value = min(
                    value,
                    score
                )
                if (value <= mAlpha) {

                    killerMove.minMove = move

                    killerTable.put(node.state.toStr(), killerMove)
                    break
                }

                mBeta = min(mAlpha, value)

            }



            historyCache.put(node.state.toStr(), history)
            return value

        }


    }

    override fun getMove(engine: Engine): Move {

        val timeStamp = System.currentTimeMillis()

        val parentNode = Node(
            engine.board, engine, engine.playerTurn, maxDepth, true, Int.MIN_VALUE, Int.MIN_VALUE
        )


        var depth = 0
        var bestMove: Move? = null
        val thread = Thread(Runnable {
            try {


                while (System.currentTimeMillis() - timeStamp < 2000) {
                    depth++
                    println("Historical Iteration $depth starts")

                    doAlphaBeta(
                        parentNode, depth, Int.MIN_VALUE, Int.MAX_VALUE, true
                    )

                    println("Historical Iteration $depth ends")
                    if (parentNode.bestMove != null)
                        bestMove = parentNode.bestMove


                }
            } catch (e: Exception) {

            }

        })
        thread.start()

        sleep(1500)

        thread.stop()

        transpositionTable.evictAll()

        println("It took ${System.currentTimeMillis() - timeStamp} ${bestMove?.type}")
//        println("node is $parentNode with value $node children ${parentNode.bestMove}")

        if (bestMove == null)
            return engine.getPossibleMoves(engine.playerTurn).random()
        return bestMove!!

        val moves = engine.getPossibleMoves(player)

        return moves.random()
    }


}