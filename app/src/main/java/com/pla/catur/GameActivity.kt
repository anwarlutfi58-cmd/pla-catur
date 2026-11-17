package com.pla.catur

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var speech: SpeechHelper
    private lateinit var sound: SpatialSoundHelper
    private lateinit var engine: ChessEngine
    private var ai: AiPlayer? = null // Diperbaiki: tidak pakai lateinit
    private lateinit var boardView: ChessBoardView
    private lateinit var txtTurn: TextView
    private lateinit var txtInfo: TextView
    private lateinit var txtStatus: TextView

    private var mode = "ai"
    private var difficulty = "medium"
    private var busy = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        prefs = getSharedPreferences("caturPrefs", MODE_PRIVATE)
        speech = SpeechHelper(this)
        sound = SpatialSoundHelper()

        mode = intent.getStringExtra("mode") ?: "ai"
        difficulty = intent.getStringExtra("difficulty") ?: "medium"

        engine = ChessEngine()
        engine.setup()
        ai = if (mode == "ai") AiPlayer(engine, difficulty) else null

        boardView = findViewById(R.id.boardView)
        txtTurn = findViewById(R.id.txtTurn)
        txtInfo = findViewById(R.id.txtInfo)
        txtStatus = findViewById(R.id.txtStatus)
        boardView.engine = engine
        boardView.onCellTapped = { r, c -> onCellTapped(r, c) }

        findViewById<Button>(R.id.btnHome).setOnClickListener { finish() }

        updateTurnText()
        speech.speak("Permainan catur dimulai. Giliran putih.")
    }

    private fun onCellTapped(r: Int, c: Int) {
        if (busy) return
        if (mode == "ai" && !engine.isWhiteTurn) return

        val p = engine.board[r][c]
        if (engine.selectedRow == -1) {
            if (p != null && p.isWhite == engine.isWhiteTurn) {
                engine.selectedRow = r; engine.selectedCol = c
                boardView.highlightMoves = engine.validMoves(r, c)
                speech.speak("${p.name()} di ${coord(r,c)}. ${boardView.highlightMoves.size} langkah tersedia.")
                sound.playMove(c)
                boardView.invalidate()
            }
        } else {
            val moves = boardView.highlightMoves
            if (moves.contains(Pair(r, c))) {
                val captured = engine.board[r][c]
                engine.move(engine.selectedRow, engine.selectedCol, r, c)
                engine.selectedRow = -1; engine.selectedCol = -1
                boardView.highlightMoves = emptyList()
                if (captured != null) {
                    sound.playCapture(c)
                    speech.speak("Memakan ${captured.name()} di ${coord(r,c)}")
                } else {
                    sound.playMove(c)
                    speech.speak("Melangkah ke ${coord(r,c)}")
                }
                boardView.invalidate()
                checkGameState()
                if (mode == "ai" && !engine.isWhiteTurn) {
                    busy = true
                    handler.postDelayed({ aiMove() }, 600)
                } else {
                    updateTurnText()
                }
            } else {
                engine.selectedRow = -1; engine.selectedCol = -1
                boardView.highlightMoves = emptyList()
                boardView.invalidate()
                speech.speak("Dibatalkan")
            }
        }
    }

    private fun aiMove() {
        val move = ai?.bestMove()
        if (move == null) { busy = false; return }
        val (from, to) = move
        val captured = engine.board[to.first][to.second]
        engine.move(from.first, from.second, to.first, to.second)
        if (captured != null) sound.playCapture(to.second) else sound.playMove(to.second)
        speech.speak("AI melangkah ke ${coord(to.first, to.second)}")
        boardView.invalidate()
        busy = false
        checkGameState()
        if (mode == "ai" && !engine.isWhiteTurn) {
            busy = true
            handler.postDelayed({ aiMove() }, 600)
        } else {
            updateTurnText()
        }
    }

    private fun checkGameState() {
        if (engine.isCheckmate(!engine.isWhiteTurn)) {
            txtStatus.visibility = View.VISIBLE
            txtStatus.text = getString(R.string.checkmate_win)
            speech.speak("Skak mat. ${if (engine.isWhiteTurn) "Hitam" else "Putih"} menang.")
            val win = (mode == "ai" && !engine.isWhiteTurn) || (mode == "local")
            if (win && mode == "ai") {
                prefs.edit().apply {
                    putInt("wins", prefs.getInt("wins", 0) + 1)
                    putInt("coins", prefs.getInt("coins", 0) + 200)
                    val wins = prefs.getInt("wins", 0) + 1
                    putInt("level", Math.floor(wins / 3.0).toInt() + 1)
                    apply()
                }
            }
            showResult(true)
            return
        }
        if (engine.isStalemate(!engine.isWhiteTurn)) {
            txtStatus.visibility = View.VISIBLE
            txtStatus.text = getString(R.string.stalemate)
            speech.speak("Seri. Tidak ada langkah tersisa.")
            showResult(false)
            return
        }
        if (engine.isInCheck(engine.isWhiteTurn)) {
            txtStatus.visibility = View.VISIBLE
            txtStatus.text = getString(R.string.check)
            speech.speak("Skak!")
            sound.playCheck(3)
        } else {
            txtStatus.visibility = View.GONE
        }
    }

    private fun showResult(win: Boolean) {
        val view = layoutInflater.inflate(R.layout.dialog_result, null)
        view.findViewById<TextView>(R.id.resTitle).text =
            if (win) getString(R.string.checkmate_win) else getString(R.string.stalemate)
        view.findViewById<TextView>(R.id.resText).text =
            if (win) "+Rp 200" else "Tidak ada hadiah"
        val dlg = AlertDialog.Builder(this).setView(view).setCancelable(false).create()
        view.findViewById<Button>(R.id.btnRetry).setOnClickListener { dlg.dismiss(); recreate() }
        view.findViewById<Button>(R.id.btnMenu).setOnClickListener { dlg.dismiss(); finish() }
        dlg.show()
    }

    private fun updateTurnText() {
        txtTurn.text = if (engine.isWhiteTurn) getString(R.string.white_turn) else getString(R.string.black_turn)
        txtInfo.text = if (engine.isWhiteTurn) "Ketuk bidak putih untuk memilih"
                       else "Ketuk bidak hitam untuk memilih"
    }

    private fun coord(r: Int, c: Int): String = "${('A' + c)}${8 - r}"

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        speech.shutdown(); sound.release()
        super.onDestroy()
    }
}