package com.parsa.partgenerator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val history = mutableListOf<String>()
    private var currentPartText: String? = null

    private val connectorWords = listOf("و", "با", "در", "از", "به", "برای", "را", "باز", "کنار", "میان")

    private fun applyTypo(word: String): String {
        if (word.length < 2) return word
        val chars = word.toMutableList()
        return when (Random.nextInt(3)) {
            0 -> {
                val i = Random.nextInt(chars.size - 1)
                val tmp = chars[i]; chars[i] = chars[i + 1]; chars[i + 1] = tmp
                chars.joinToString("")
            }
            1 -> {
                val i = Random.nextInt(chars.size)
                chars.removeAt(i)
                chars.joinToString("")
            }
            else -> {
                val i = Random.nextInt(chars.size)
                chars.add(i, chars[i])
                chars.joinToString("")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etWords = findViewById<EditText>(R.id.etWords)
        val tvWordCount = findViewById<TextView>(R.id.tvWordCount)
        val tvCurrentPart = findViewById<TextView>(R.id.tvCurrentPart)
        val btnCopy = findViewById<Button>(R.id.btnCopy)
        val btnNew = findViewById<Button>(R.id.btnNew)
        val btnClearHistory = findViewById<Button>(R.id.btnClearHistory)
        val tvHistoryTitle = findViewById<TextView>(R.id.tvHistoryTitle)
        val historyContainer = findViewById<LinearLayout>(R.id.historyContainer)

        fun toPersianDigits(n: Int): String {
            val fa = "۰۱۲۳۴۵۶۷۸۹"
            return n.toString().map { fa[it - '0'] }.joinToString("")
        }

        fun getWords(): List<String> =
            etWords.text.toString()
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

        fun updateWordCount() {
            tvWordCount.text = "${toPersianDigits(getWords().size)} کلمه"
        }

        etWords.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateWordCount()
            }
        })
        updateWordCount()

        fun renderHistory() {
            tvHistoryTitle.text = "تاریخچه (${toPersianDigits(history.size)})"
            historyContainer.removeAllViews()
            for ((i, item) in history.withIndex()) {
                val tv = TextView(this).apply {
                    text = "${toPersianDigits(history.size - i)}. $item"
                    setTextColor(resources.getColor(R.color.text_muted, theme))
                    textSize = 13f
                    setLineSpacing(0f, 1.4f)
                    setBackgroundResource(R.drawable.bg_field)
                    setPadding(24, 20, 24, 20)
                }
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.bottomMargin = 16
                tv.layoutParams = lp
                historyContainer.addView(tv)
            }
        }

        fun generatePartText(words: List<String>, passes: Int = 20): String {
            val counts = HashMap<String, Int>()
            for (w in words) counts[w] = (counts[w] ?: 0) + passes

            val total = words.size * passes
            val output = mutableListOf<String>()
            var lastWord: String? = null
            var lastWasTypo = false
            var placed = 0

            while (placed < total) {
                var candidates = words.filter { (counts[it] ?: 0) > 0 && it != lastWord }
                if (candidates.isEmpty()) candidates = words.filter { (counts[it] ?: 0) > 0 }
                if (candidates.isEmpty()) break

                val pick = candidates[Random.nextInt(candidates.size)]
                counts[pick] = (counts[pick] ?: 0) - 1
                lastWord = pick
                placed++

                val outWord = if (!lastWasTypo && Random.nextInt(100) < 35) {
                    lastWasTypo = true
                    applyTypo(pick)
                } else {
                    lastWasTypo = false
                    pick
                }
                output.add(outWord)

                if (placed < total && Random.nextInt(100) < 25) {
                    output.add(connectorWords[Random.nextInt(connectorWords.size)])
                }
            }

            return output.joinToString(" ")
        }

        fun generatePart() {
            val words = getWords()
            if (words.isEmpty()) {
                Toast.makeText(this, "اول چند تا کلمه وارد کن", Toast.LENGTH_SHORT).show()
                return
            }

            val partText = generatePartText(words, 20)
            currentPartText = partText
            tvCurrentPart.text = partText
            tvCurrentPart.setTextColor(resources.getColor(R.color.text_main, theme))

            history.add(0, partText)
            renderHistory()
        }

        btnNew.setOnClickListener { generatePart() }

        btnCopy.setOnClickListener {
            val text = currentPartText
            if (text.isNullOrEmpty()) {
                Toast.makeText(this, "اول یه پارت بساز", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("part", text))
            Toast.makeText(this, "کپی شد", Toast.LENGTH_SHORT).show()
        }

        btnClearHistory.setOnClickListener {
            history.clear()
            renderHistory()
            Toast.makeText(this, "تاریخچه پاک شد", Toast.LENGTH_SHORT).show()
        }
    }
}
