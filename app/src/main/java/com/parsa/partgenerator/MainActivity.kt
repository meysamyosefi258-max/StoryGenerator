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
            val total = minOf(words.size * passes, 6000)

            val pool = ArrayList<String>(total)
            while (pool.size < total) {
                pool.addAll(words)
            }
            while (pool.size > total) {
                pool.removeAt(pool.size - 1)
            }
            pool.shuffle()

            for (i in 1 until pool.size) {
                if (pool[i] == pool[i - 1]) {
                    for (j in i + 1 until pool.size) {
                        if (pool[j] != pool[i] && pool[j] != pool[i - 1]) {
                            val tmp = pool[i]
                            pool[i] = pool[j]
                            pool[j] = tmp
                            break
                        }
                    }
                }
            }

            val output = ArrayList<String>(pool.size * 2)
            var lastWasTypo = false
            for (idx in pool.indices) {
                val word = pool[idx]
                val outWord = if (!lastWasTypo && Random.nextInt(100) < 35) {
                    lastWasTypo = true
                    applyTypo(word)
                } else {
                    lastWasTypo = false
                    word
                }
                output.add(outWord)

                if (idx < pool.size - 1 && Random.nextInt(100) < 25) {
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

            btnNew.isEnabled = false
            Thread {
                val partText = generatePartText(words, 20)
                runOnUiThread {
                    currentPartText = partText
                    tvCurrentPart.text = partText
                    tvCurrentPart.setTextColor(resources.getColor(R.color.text_main, theme))

                    history.add(0, partText)
                    renderHistory()
                    btnNew.isEnabled = true
                }
            }.start()
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
