package com.example.storygenerator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import android.widget.LinearLayout
import android.graphics.drawable.GradientDrawable
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout

// ===================== Story Generator Class =====================
class StoryGenerator {
    
    private val storyTemplates = listOf(
        "روزی {0} به شهری رفت که نام آن {1} بود. در آن شهر {2} زندگی می‌کرد.",
        "یک روز {0} تصمیم گرفت که به جستجوی {1} برود. او {2} را با خود برد.",
        "{0} داستانی عجیب داشت. در ابتدا {1} بود اما سپس {2} شد.",
        "در {1} شهر، {0} می‌زیست. یک روز او {2} را دید و تعجب کرد.",
        "{0} و {1} دوستان بودند. آنها هر روز {2} را انجام می‌دادند.",
        "یک بار {0} در کنار {1} نشست و درباره {2} فکر کرد.",
        "{0} به {1} گفت: من {2} را دوست دارم.",
        "در یک شب تاریک، {0} صدای {1} را شنید و {2} شروع کرد.",
        "{0} شهری را نقاشی کرد که شامل {1} و {2} بود.",
        "آخرین بار که {0} {1} را دید، او {2} می‌کرد.",
        "یکی از بزرگترین اسرار دنیا در {1} نهفته بود. {0} به تنهایی {2} را کشف کرد.",
        "همه فکر می‌کردند {0} مجنون است، اما او {1} را فهمید و {2} را انجام داد.",
        "{1} جایی بود که {0} هرگز فراموش نکرد. آنجا {2} اتفاق افتاد.",
        "صدای {0} تنها چیزی بود که {1} را بیدار کرد تا {2} شود.",
        "در میان تاریکی، {0} نور {1} را یافت و {2} کرد."
    )
    
    private val storyEndings = listOf(
        "و سرانجام همه چیز خوب شد.",
        "و این داستان تا ابد در تاریخ ماند.",
        "و او یاد کرد که این تنها یک خواب بود.",
        "و زندگی ادامه یافت...",
        "و دوستان برای همیشه پیوند خوردند.",
        "و این درس مهمی برای همه بود.",
        "و خوشبختی برای همیشه به تاریخ تعلق گرفت.",
        "و این داستان هرگز فراموش نشد.",
        "و فصل جدیدی شروع شد.",
        "و آنها تا آخر عمر خوشبخت ماندند.",
        "و دنیا هرگز مثل قبل نبود.",
        "و حقیقت بالاخره فاش شد.",
        "و همه درس‌های لازم را یاد گرفتند.",
        "و این فقط ابتدای داستان بود.",
        "و پرده از رازها برداشته شد."
    )
    
    fun generateStory(words: List<String>): String {
        if (words.isEmpty()) {
            return "لطفاً حداقل یک کلمه وارد کنید!"
        }
        
        val story = StringBuilder()
        var currentWords = words.toMutableList()
        
        // اگر کلمات کم‌تر از 3 تا باشد، آنها را تکرار می‌کنیم
        while (currentWords.size < 3) {
            currentWords.addAll(words)
        }
        
        // قسمت اول داستان
        val template1 = storyTemplates.random()
        story.append(formatTemplate(template1, currentWords.take(3)))
        story.append("\n\n")
        
        // قسمت دوم داستان
        val template2 = storyTemplates.random()
        story.append(formatTemplate(template2, currentWords.drop(3).take(3).ifEmpty { currentWords.take(3) }))
        story.append("\n\n")
        
        // پایان داستان
        story.append(storyEndings.random())
        
        return story.toString()
    }
    
    private fun formatTemplate(template: String, words: List<String>): String {
        var result = template
        for (i in 0..2) {
            val placeholder = "{$i}"
            val word = if (i < words.size) words[i] else "چیزی"
            result = result.replace(placeholder, word)
        }
        return result
    }
}

// ===================== Main Activity =====================
class MainActivity : AppCompatActivity() {
    
    private lateinit var inputEditText: EditText
    private lateinit var generateButton: Button
    private lateinit var storyTextView: TextView
    private lateinit var copyButton: Button
    private lateinit var clearButton: Button
    private val storyGenerator = StoryGenerator()
    private var currentStory = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ایجاد رابط کاربری برنامه‌نویسی
        val mainLayout = createMainLayout()
        setContentView(mainLayout)
        
        // تنظیم رویدادهای دکمه‌ها
        generateButton.setOnClickListener {
            generateStory()
        }
        
        copyButton.setOnClickListener {
            copyToClipboard()
        }
        
        clearButton.setOnClickListener {
            clearAll()
        }
    }
    
    private fun createMainLayout(): LinearLayout {
        val mainLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
        }
        
        // عنوان
        val titleTextView = TextView(this).apply {
            text = "تولید کننده داستان"
            textSize = 24f
            setTextColor(0xFF000000.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16.dpToPx() }
            gravity = android.view.Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        mainLayout.addView(titleTextView)
        
        // برچسب ورودی
        val labelTextView = TextView(this).apply {
            text = "کلمات را وارد کنید (جدا شده با فاصله یا کاما):"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8.dpToPx() }
        }
        mainLayout.addView(labelTextView)
        
        // فیلد ورودی
        inputEditText = EditText(this).apply {
            hint = "مثال: خرس، کوه، باران"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                100.dpToPx()
            ).apply { bottomMargin = 16.dpToPx() }
            padding = 12.dpToPx()
            setBackgroundColor(0xFFF5F5F5.toInt())
            gravity = android.view.Gravity.TOP or android.view.Gravity.RIGHT
            textDirection = android.view.View.TEXT_DIRECTION_RTL
        }
        mainLayout.addView(inputEditText)
        
        // صفحه دکمه‌ها
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16.dpToPx() }
            spacing = 8.dpToPx()
        }
        
        // دکمه تولید
        generateButton = Button(this).apply {
            text = "تولید داستان"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF4CAF50.toInt())
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply { marginEnd = 4.dpToPx() }
        }
        buttonLayout.addView(generateButton)
        
        // دکمه کپی
        copyButton = Button(this).apply {
            text = "کپی"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF2196F3.toInt())
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply { marginEnd = 4.dpToPx() }
            isEnabled = false
        }
        buttonLayout.addView(copyButton)
        
        // دکمه پاک کردن
        clearButton = Button(this).apply {
            text = "پاک کن"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFFf44336.toInt())
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        buttonLayout.addView(clearButton)
        
        mainLayout.addView(buttonLayout)
        
        // برچسب داستان
        val storyLabelTextView = TextView(this).apply {
            text = "داستان تولید شده:"
            textSize = 16f
            setTextColor(0xFF000000.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8.dpToPx() }
        }
        mainLayout.addView(storyLabelTextView)
        
        // ScrollView برای نمایش داستان
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        
        // نمایش‌گر داستان
        storyTextView = TextView(this).apply {
            text = "داستان شما اینجا نمایش داده خواهد شد..."
            textSize = 16f
            setTextColor(0xFF000000.toInt())
            setPadding(12.dpToPx(), 12.dpToPx(), 12.dpToPx(), 12.dpToPx())
            setBackgroundColor(0xFFF9F9F9.toInt())
            gravity = android.view.Gravity.RIGHT
            textDirection = android.view.View.TEXT_DIRECTION_RTL
            movementMethod = ScrollingMovementMethod()
        }
        scrollView.addView(storyTextView)
        
        mainLayout.addView(scrollView)
        
        return mainLayout
    }
    
    private fun generateStory() {
        val input = inputEditText.text.toString().trim()
        
        if (input.isEmpty()) {
            Toast.makeText(this, "لطفاً حداقل یک کلمه وارد کنید!", Toast.LENGTH_SHORT).show()
            return
        }
        
        val words = input.split(" ", "،", "؛").filter { it.isNotEmpty() }
        
        if (words.isEmpty()) {
            Toast.makeText(this, "لطفاً کلمات معتبر وارد کنید!", Toast.LENGTH_SHORT).show()
            return
        }
        
        currentStory = storyGenerator.generateStory(words)
        storyTextView.text = currentStory
        copyButton.isEnabled = true
    }
    
    private fun copyToClipboard() {
        if (currentStory.isEmpty()) {
            Toast.makeText(this, "ابتدا یک داستان تولید کنید!", Toast.LENGTH_SHORT).show()
            return
        }
        
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = android.content.ClipData.newPlainText("Story", currentStory)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "داستان کپی شد!", Toast.LENGTH_SHORT).show()
    }
    
    private fun clearAll() {
        inputEditText.text.clear()
        storyTextView.text = ""
        currentStory = ""
        copyButton.isEnabled = false
    }
    
    // تابع تبدیل dp به pixel
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
