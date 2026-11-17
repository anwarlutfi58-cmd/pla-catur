package com.pla.catur

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ChessBoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var engine: ChessEngine? = null
        set(value) { field = value; invalidate() }

    var onCellTapped: ((Int, Int) -> Unit)? = null
    var highlightMoves: List<Pair<Int, Int>> = emptyList()
        set(value) { field = value; invalidate() }

    private var cellSize = 0f
    private var boardLeft = 0f
    private var boardTop = 0f

    private val paintLight = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintDark = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintPiece = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintCoord = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintSel = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintMove = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintLastMove = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paintLight.color = 0xFFF0D9B5.toInt()
        paintDark.color = 0xFFB58863.toInt()
        paintBorder.style = Paint.Style.STROKE
        paintBorder.strokeWidth = 6f
        paintBorder.color = 0xFF3E2723.toInt()
        paintPiece.textAlign = Paint.Align.CENTER
        paintCoord.textAlign = Paint.Align.CENTER
        paintCoord.color = 0xFF3E2723.toInt()
        paintCoord.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paintSel.style = Paint.Style.FILL
        paintSel.color = 0xAAFFD700.toInt()
        paintMove.style = Paint.Style.FILL
        paintMove.color = 0x884CAF50.toInt()
        paintLastMove.style = Paint.Style.FILL
        paintLastMove.color = 0x44FFD700.toInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val side = Math.min(w, h) - 40
        cellSize = side / 8f
        boardLeft = (w - side) / 2f
        boardTop = (h - side) / 2f
        paintPiece.textSize = cellSize * 0.75f
        paintCoord.textSize = cellSize * 0.18f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (cellSize == 0f) return
        val side = cellSize * 8

        // Shadow / border
        val outer = RectF(boardLeft - 6, boardTop - 6, boardLeft + side + 6, boardTop + side + 6)
        val borderGrad = LinearGradient(outer.left, outer.top, outer.right, outer.bottom,
            0xFF5D4037.toInt(), 0xFF3E2723.toInt(), Shader.TileMode.CLAMP)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = borderGrad }
        canvas.drawRoundRect(outer, 12f, 12f, borderPaint)

        // Papan
        for (r in 0..7) for (c in 0..7) {
            val left = boardLeft + c * cellSize
            val top = boardTop + r * cellSize
            val rect = RectF(left, top, left + cellSize, top + cellSize)
            canvas.drawRect(rect, if ((r + c) % 2 == 0) paintLight else paintDark)
        }

        // Highlight last move
        val eng = engine
        if (eng != null) {
            eng.lastMoveFrom?.let { (r, c) -> canvas.drawRect(
                boardLeft + c * cellSize, boardTop + r * cellSize,
                boardLeft + (c+1) * cellSize, boardTop + (r+1) * cellSize,
                paintLastMove) }
            eng.lastMoveTo?.let { (r, c) -> canvas.drawRect(
                boardLeft + c * cellSize, boardTop + r * cellSize,
                boardLeft + (c+1) * cellSize, boardTop + (r+1) * cellSize,
                paintLastMove) }
            if (eng.selectedRow >= 0) {
                val r = eng.selectedRow; val c = eng.selectedCol
                canvas.drawRect(
                    boardLeft + c * cellSize, boardTop + r * cellSize,
                    boardLeft + (c+1) * cellSize, boardTop + (r+1) * cellSize,
                    paintSel)
            }
        }

        // Kotak tujuan yang valid
        for ((r, c) in highlightMoves) {
            val cx = boardLeft + c * cellSize + cellSize / 2
            val cy = boardTop + r * cellSize + cellSize / 2
            canvas.drawCircle(cx, cy, cellSize * 0.18f, paintMove)
        }

        // Bidak
        if (eng != null) {
            for (r in 0..7) for (c in 0..7) {
                val p = eng.board[r][c] ?: continue
                val cx = boardLeft + c * cellSize + cellSize / 2
                val cy = boardTop + r * cellSize + cellSize / 2
                val fm = paintPiece.fontMetrics
                val textY = cy - (fm.ascent + fm.descent) / 2f
                paintPiece.color = if (p.isWhite) Color.WHITE else Color.BLACK
                paintPiece.setShadowLayer(4f, 0f, 2f, 0x88000000.toInt())
                canvas.drawText(p.symbol(), cx, textY, paintPiece)
                paintPiece.clearShadowLayer()
            }
        }

        // Koordinat A-H dan 1-8
        for (i in 0..7) {
            val x = boardLeft + i * cellSize + cellSize / 2
            val y = boardTop + side + cellSize * 0.55f
            canvas.drawText(('A' + i).toString(), x, y, paintCoord)
            val x2 = boardLeft - cellSize * 0.3f
            val y2 = boardTop + (7 - i) * cellSize + cellSize * 0.6f
            canvas.drawText((i + 1).toString(), x2, y2, paintCoord)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val c = ((event.x - boardLeft) / cellSize).toInt()
            val r = ((event.y - boardTop) / cellSize).toInt()
            if (r in 0..7 && c in 0..7) {
                onCellTapped?.invoke(r, c)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}