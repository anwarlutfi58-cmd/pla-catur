package com.pla.catur

class AiPlayer(private val engine: ChessEngine, private val difficulty: String) {
    fun bestMove(): Pair<Pair<Int,Int>, Pair<Int,Int>>? {
        val moves = engine.allMoves(false).toMutableList()
        if (moves.isEmpty()) return null
        when (difficulty) {
            "easy" -> return moves.random()
            "medium" -> return preferCapture(moves)
            else -> return bestOfBest(moves)
        }
    }
    private fun scoreMove(move: Pair<Pair<Int,Int>, Pair<Int,Int>>): Int {
        val target = engine.board[move.second.first][move.second.second]
        return when (target?.type) {
            PieceType.PRABU -> 1000
            PieceType.PATIH -> 90
            PieceType.KUBU -> 50
            PieceType.BANTENG -> 30
            PieceType.RESI -> 30
            PieceType.PRAJURIT -> 10
            null -> 0
        }
    }
    private fun preferCapture(moves: List<Pair<Pair<Int,Int>, Pair<Int,Int>>>): Pair<Pair<Int,Int>, Pair<Int,Int>> {
        val sorted = moves.sortedByDescending { scoreMove(it) }
        val top = sorted.filter { scoreMove(it) == scoreMove(sorted[0]) }
        return top.random()
    }
    private fun bestOfBest(moves: List<Pair<Pair<Int,Int>, Pair<Int,Int>>>): Pair<Pair<Int,Int>, Pair<Int,Int>> {
        return moves.maxByOrNull { scoreMove(it) + (1..5).random() } ?: moves.random()
    }
}