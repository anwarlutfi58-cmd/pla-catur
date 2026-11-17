package com.pla.catur

enum class PieceType(val label: String) {
    PRABU("Prabu"), PATIH("Patih"), KUBU("Kubu"),
    BANTENG("Banteng"), RESI("Resi"), PRAJURIT("Prajurit")
}

data class Piece(
    val type: PieceType,
    val isWhite: Boolean,
    var moved: Boolean = false
) {
    fun symbol(): String {
        val white = mapOf(
            PieceType.PRABU to "♔", PieceType.PATIH to "♕",
            PieceType.KUBU to "♖", PieceType.BANTENG to "♘",
            PieceType.RESI to "♗", PieceType.PRAJURIT to "♙"
        )
        val black = mapOf(
            PieceType.PRABU to "♚", PieceType.PATIH to "♛",
            PieceType.KUBU to "♜", PieceType.BANTENG to "♞",
            PieceType.RESI to "♝", PieceType.PRAJURIT to "♟"
        )
        return if (isWhite) white[type]!! else black[type]!!
    }
    fun name(): String = type.label
}