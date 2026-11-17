package com.pla.catur

class ChessEngine {
    var board = Array(8) { arrayOfNulls<Piece>(8) }
    var isWhiteTurn = true
    var selectedRow = -1
    var selectedCol = -1
    var lastMoveFrom: Pair<Int, Int>? = null
    var lastMoveTo: Pair<Int, Int>? = null

    fun setup() {
        board = Array(8) { arrayOfNulls<Piece>(8) }
        val back = listOf(
            PieceType.KUBU, PieceType.BANTENG, PieceType.RESI, PieceType.PATIH,
            PieceType.PRABU, PieceType.RESI, PieceType.BANTENG, PieceType.KUBU
        )
        for (c in 0..7) {
            board[0][c] = Piece(back[c], false)
            board[1][c] = Piece(PieceType.PRAJURIT, false)
            board[6][c] = Piece(PieceType.PRAJURIT, true)
            board[7][c] = Piece(back[c], true)
        }
        isWhiteTurn = true
        selectedRow = -1; selectedCol = -1
        lastMoveFrom = null; lastMoveTo = null
    }

    fun validMoves(r: Int, c: Int): List<Pair<Int, Int>> {
        val p = board[r][c] ?: return emptyList()
        val moves = mutableListOf<Pair<Int, Int>>()
        val dir = if (p.isWhite) -1 else 1

        fun addIfOk(nr: Int, nc: Int) {
            if (nr !in 0..7 || nc !in 0..7) return
            val target = board[nr][nc]
            if (target == null || target.isWhite != p.isWhite) moves.add(Pair(nr, nc))
        }

        when (p.type) {
            PieceType.PRAJURIT -> {
                val fr = r + dir
                if (fr in 0..7 && board[fr][c] == null) {
                    moves.add(Pair(fr, c))
                    val startRow = if (p.isWhite) 6 else 1
                    if (r == startRow) {
                        val fr2 = r + dir * 2
                        if (board[fr2][c] == null) moves.add(Pair(fr2, c))
                    }
                }
                for (dc in listOf(-1, 1)) {
                    val nr = r + dir; val nc = c + dc
                    if (nr in 0..7 && nc in 0..7) {
                        val t = board[nr][nc]
                        if (t != null && t.isWhite != p.isWhite) moves.add(Pair(nr, nc))
                    }
                }
            }
            PieceType.KUBU -> {
                for (d in listOf(-1,0,1)) for (e in listOf(-1,0,1)) {
                    if (d == 0 && e == 0) continue
                    var nr = r + d; var nc = c + e
                    while (nr in 0..7 && nc in 0..7) {
                        val t = board[nr][nc]
                        if (t == null) moves.add(Pair(nr, nc))
                        else {
                            if (t.isWhite != p.isWhite) moves.add(Pair(nr, nc))
                            break
                        }
                        nr += d; nc += e
                    }
                }
            }
            PieceType.RESI -> {
                for (d in listOf(-1,1)) for (e in listOf(-1,1)) {
                    var nr = r + d; var nc = c + e
                    while (nr in 0..7 && nc in 0..7) {
                        val t = board[nr][nc]
                        if (t == null) moves.add(Pair(nr, nc))
                        else {
                            if (t.isWhite != p.isWhite) moves.add(Pair(nr, nc))
                            break
                        }
                        nr += d; nc += e
                    }
                }
            }
            PieceType.PATIH -> {
                for (d in listOf(-1,0,1)) for (e in listOf(-1,0,1)) {
                    if (d == 0 && e == 0) continue
                    var nr = r + d; var nc = c + e
                    while (nr in 0..7 && nc in 0..7) {
                        val t = board[nr][nc]
                        if (t == null) moves.add(Pair(nr, nc))
                        else {
                            if (t.isWhite != p.isWhite) moves.add(Pair(nr, nc))
                            break
                        }
                        nr += d; nc += e
                    }
                }
            }
            PieceType.BANTENG -> {
                val jumps = listOf(Pair(-2,-1),Pair(-2,1),Pair(-1,-2),Pair(-1,2),Pair(1,-2),Pair(1,2),Pair(2,-1),Pair(2,1))
                for ((dr,dc) in jumps) addIfOk(r+dr, c+dc)
            }
            PieceType.PRABU -> {
                for (d in -1..1) for (e in -1..1) {
                    if (d == 0 && e == 0) continue
                    addIfOk(r+d, c+e)
                }
            }
        }
        // filter move yang bikin raja skak
        return moves.filter { !wouldBeCheck(r, c, it.first, it.second, p.isWhite) }
    }

    private fun wouldBeCheck(fr: Int, fc: Int, tr: Int, tc: Int, whiteKing: Boolean): Boolean {
        val temp = board[tr][tc]
        val moving = board[fr][fc] ?: return false
        board[tr][tc] = moving
        board[fr][fc] = null
        val inCheck = isInCheck(whiteKing)
        board[fr][fc] = moving
        board[tr][tc] = temp
        return inCheck
    }

    fun isInCheck(whiteKing: Boolean): Boolean {
        var kr = -1; var kc = -1
        for (r in 0..7) for (c in 0..7) {
            val p = board[r][c]
            if (p != null && p.type == PieceType.PRABU && p.isWhite == whiteKing) { kr = r; kc = c }
        }
        if (kr == -1) return false
        for (r in 0..7) for (c in 0..7) {
            val p = board[r][c] ?: continue
            if (p.isWhite == whiteKing) continue
            val raw = rawMoves(r, c)
            if (raw.any { it.first == kr && it.second == kc }) return true
        }
        return false
    }

    private fun rawMoves(r: Int, c: Int): List<Pair<Int, Int>> {
        val p = board[r][c] ?: return emptyList()
        val out = mutableListOf<Pair<Int, Int>>()
        val dir = if (p.isWhite) -1 else 1
        fun ok(nr: Int, nc: Int): Boolean {
            if (nr !in 0..7 || nc !in 0..7) return false
            val t = board[nr][nc]
            return t == null || t.isWhite != p.isWhite
        }
        when (p.type) {
            PieceType.PRAJURIT -> {
                if (ok(r+dir, c)) out.add(Pair(r+dir, c))
                for (dc in listOf(-1,1)) if (ok(r+dir, c+dc)) out.add(Pair(r+dir, c+dc))
            }
            PieceType.BANTENG -> {
                for ((dr,dc) in listOf(Pair(-2,-1),Pair(-2,1),Pair(-1,-2),Pair(-1,2),Pair(1,-2),Pair(1,2),Pair(2,-1),Pair(2,1)))
                    if (ok(r+dr, c+dc)) out.add(Pair(r+dr, c+dc))
            }
            PieceType.PRABU -> {
                for (d in -1..1) for (e in -1..1) {
                    if (d == 0 && e == 0) continue
                    if (ok(r+d, c+e)) out.add(Pair(r+d, c+e))
                }
            }
            PieceType.RESI -> {
                for (d in listOf(-1,1)) for (e in listOf(-1,1)) {
                    var nr = r+d; var nc = c+e
                    while (nr in 0..7 && nc in 0..7) {
                        if (board[nr][nc] == null) out.add(Pair(nr, nc))
                        else { if (board[nr][nc]!!.isWhite != p.isWhite) out.add(Pair(nr, nc)); break }
                        nr += d; nc += e
                    }
                }
            }
            PieceType.KUBU, PieceType.PATIH -> {
                for (d in listOf(-1,0,1)) for (e in listOf(-1,0,1)) {
                    if (d == 0 && e == 0) continue
                    var nr = r+d; var nc = c+e
                    while (nr in 0..7 && nc in 0..7) {
                        if (board[nr][nc] == null) out.add(Pair(nr, nc))
                        else { if (board[nr][nc]!!.isWhite != p.isWhite) out.add(Pair(nr, nc)); break }
                        nr += d; nc += e
                    }
                }
            }
        }
        return out
    }

    fun move(fromR: Int, fromC: Int, toR: Int, toC: Int): Boolean {
        val p = board[fromR][fromC] ?: return false
        board[toR][toC] = p.copy(moved = true)
        board[fromR][fromC] = null
        lastMoveFrom = Pair(fromR, fromC)
        lastMoveTo = Pair(toR, toC)
        isWhiteTurn = !isWhiteTurn
        return true
    }

    fun allMoves(forWhite: Boolean): List<Pair<Pair<Int,Int>, Pair<Int,Int>>> {
        val out = mutableListOf<Pair<Pair<Int,Int>, Pair<Int,Int>>>()
        for (r in 0..7) for (c in 0..7) {
            val p = board[r][c] ?: continue
            if (p.isWhite != forWhite) continue
            for (m in validMoves(r, c)) out.add(Pair(Pair(r,c), m))
        }
        return out
    }

    fun isCheckmate(forWhite: Boolean): Boolean =
        isInCheck(forWhite) && allMoves(forWhite).isEmpty()

    fun isStalemate(forWhite: Boolean): Boolean =
        !isInCheck(forWhite) && allMoves(forWhite).isEmpty()
}