package core.engine.ai.evaluator

import core.engine.*
import kotlin.random.Random
import kotlin.random.nextInt

val random = Random(System.currentTimeMillis())

class MaterialEvaluator(
    val weights: List<Int> = arrayListOf(
        3, -2,
        4, -2,
        21, -23,
        5, -4,
        5, -4,
        6, -6,
        2
    )
) : Evaluator {


    fun mutateAndCombine(other: MaterialEvaluator): MaterialEvaluator {
        val newWeights = weights.mapIndexed { index, i ->
            (i + other.weights[index] + random.nextInt(-2..2)) / 2
        }

        return MaterialEvaluator(newWeights)

    }


//    val features = arrayListOf(0, 1)

    val engine = Engine

    override fun evaluateState(node: Node, player: Int): Int {

        val state = node.state
        node.calcMoves()

        val myMoves = if (node.player == player) node.myMoves else node.enemyMoves
//        engine.getPossibleMoves(player, state)
        val enemyMoves = if (node.player == player) node.enemyMoves else node.myMoves

        if (myMoves.isEmpty() || state.isTownDead(player) || state.pawnCount(player) == 0) // total lost
            return -30000

        if (state.isTownDead((player.otherPlayer())) || state.pawnCount(player.otherPlayer()) == 0 || enemyMoves.isEmpty()) // total win
            return 30000

        val shootCnt = myMoves.filter { it.type == Move.Type.Shoot }.size
        var eShootCnt = enemyMoves.filter { it.type == Move.Type.Shoot }.size
//        if (eShootCnt == 0)
//            eShootCnt = 1

        val captureCnt = myMoves.filter { it.type == Move.Type.Capture }.size
        var eCaptureCnt = enemyMoves.filter { it.type == Move.Type.Capture }.size

//        if (eCaptureCnt == 0)
//            eCaptureCnt = 1

        val myCannons = state.numberOfCannons(player)

        var enemyCannons = state.numberOfCannons(player.otherPlayer())

//        if (enemyCannons == 0)
//            enemyCannons = 1

        val possibleShots = state.numberOfPossibleShots(player)
        var enemyPossibleShots = state.numberOfPossibleShots(player.otherPlayer())

//        if (enemyPossibleShots == 0)
//            enemyPossibleShots = 1

        var ePawnCount = state.pawnCount(
            player.otherPlayer()
        )
//        if (ePawnCount == 0)
//            ePawnCount = 1


        val features = arrayListOf(
            myMoves.size, enemyMoves.size,
            myCannons, enemyCannons,
            state.pawnCount(player), ePawnCount,
            shootCnt, eShootCnt,
            captureCnt, eCaptureCnt,
            possibleShots, enemyPossibleShots,
            random.nextInt(5)
        )

//        val weights = arrayListOf(
//            3, -2,
//            4, -2,
//            20, -20,
//            5, -4,
//            5, -4,
//            6, -6,
//            2
//        )

        val res = features.reduceIndexed { index, acc, i -> acc + i * weights[index] }

//        println("evaluation is ${res}")


        return res
    }


//    fun evaluateStateZ(player: Int, state: Board): Int {
//        val myMoves = getPossibleMoves(player, state)
//
//        val enemyMoves = getPossibleMoves(player.otherPlayer(), state)
//
//        if (myMoves.isEmpty() || state.isTownDead(player) || state.pawnCount(player) == 0) // total lost
//            return -300
//
//        if (state.isTownDead((player.otherPlayer())) || state.pawnCount(player.otherPlayer()) == 0 || enemyMoves.isEmpty()) // total win
//            return 300
//
//        val shootCnt = myMoves.filter { it.type == Move.Type.Shoot }.size
//        var eShootCnt = enemyMoves.filter { it.type == Move.Type.Shoot }.size
//        if (eShootCnt == 0)
//            eShootCnt = 1
//
//        val captureCnt = myMoves.filter { it.type == Move.Type.Capture }.size
//        var eCaptureCnt = enemyMoves.filter { it.type == Move.Type.Capture }.size
//
//        if (eCaptureCnt == 0)
//            eCaptureCnt = 1
//
//        val myCannons = state.numberOfCannons(player)
//
//        var enemyCannons = state.numberOfCannons(player.otherPlayer())
//
//        if (enemyCannons == 0)
//            enemyCannons = 1
//
//        val possibleShots = state.numberOfPossibleShots(player)
//        var enemyPossibleShots = state.numberOfPossibleShots(player.otherPlayer())
//
//        if (enemyPossibleShots == 0)
//            enemyPossibleShots = 1
//
//        var result =
//            ((myMoves.size.toFloat() / enemyMoves.size) * 2 + 6 * (myCannons.toFloat() / enemyCannons) +
//                    100 * (state.pawnCount(player).toFloat() / state.pawnCount(player.otherPlayer())) +
//                    (6 * (shootCnt.toFloat() / eShootCnt))
//                    + 2 * (captureCnt.toFloat() / eCaptureCnt)
//                    + 4 * (possibleShots / enemyPossibleShots)
//                    + 1 * (random).nextInt(4)
////                    + 1 * (progressTowardEnemyTown)
//                    )
//
//        return result.roundToInt()
//
//
//    }

}