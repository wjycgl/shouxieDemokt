package com.example.shouxieDemokt

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.digitalink.RecognitionCandidate
import java.lang.reflect.Method
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    var isOkshibie: Boolean = false
    private var surfaceView: SecondSurfaceView? = null
    private var tv_xuanze1: TextView? = null
    private var tv_xuanze2: TextView? = null
    private var tv_xuanze3: TextView? = null
    private var tv_xuanze4: TextView? = null
    private var tv_xuanze5: TextView? = null
    private var tv_xuanze6: TextView? = null
    private var tv_xuanze7: TextView? = null
    private var tv_xuanze8: TextView? = null
    private var msgBox: CleanableEditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        surfaceView = findViewById<SecondSurfaceView>(R.id.surfaceview)
        tv_xuanze1 = findViewById<TextView>(R.id.tv_xuanze1)
        tv_xuanze2 = findViewById<TextView>(R.id.tv_xuanze2)
        tv_xuanze3 = findViewById<TextView>(R.id.tv_xuanze3)
        tv_xuanze4 = findViewById<TextView>(R.id.tv_xuanze4)
        tv_xuanze5 = findViewById<TextView>(R.id.tv_xuanze5)
        tv_xuanze6 = findViewById<TextView>(R.id.tv_xuanze6)
        tv_xuanze7 = findViewById<TextView>(R.id.tv_xuanze7)
        tv_xuanze8 = findViewById<TextView>(R.id.tv_xuanze8)
        shouxieArray = arrayOf(
            tv_xuanze1,
            tv_xuanze2,
            tv_xuanze3,
            tv_xuanze4,
            tv_xuanze5,
            tv_xuanze6,
            tv_xuanze7,
            tv_xuanze8
        )
        for (textView in shouxieArray) {
            textView!!.setOnClickListener(textOnClickListener)
        }
        msgBox = findViewById<CleanableEditText>(R.id.msgBox)
        disableSoftInput(msgBox)
        msgBox!!.setCustomDeletedCallback(object : CleanableEditText.CustomDeletedCallback {
            override fun onDeleted(cleanableEditText: CleanableEditText?) {
                cancelShouxie()
            }
        })
    }


    var textOnClickListener: View.OnClickListener = View.OnClickListener { v ->
        val tv = v as TextView
        val index = msgBox!!.selectionStart
        val editable: Editable? = msgBox!!.text
        editable?.insert(index, tv.text.toString() + "")
        cancelShouxie()
    }

    fun cancelShouxie() {
        if (surfaceView != null) {
            surfaceView!!.clean()
        }
        clearBoxWords()
    }


    fun disableSoftInput(editText: CleanableEditText?) {
        if (Build.VERSION.SDK_INT <= 10) {
            editText!!.inputType = InputType.TYPE_NULL
        } else {
            val cls: Class<EditText> = EditText::class.java
            val method: Method
            try {
                getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                )
                method = cls.getMethod("setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(editText, false)
            } catch (e: Exception) {
                // TODO: handle exception
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isOkshibie = true
        cancelShouxie()
    }

    companion object {
        @JvmStatic
        fun clearBoxWords() {
            Handler(getMainLooper()).post(Runnable {
                if (shouxieArray.size > 0) {
                    for (j in shouxieArray.indices) {
                        shouxieArray[j]!!.text = ""
                    }
                }
            })
        }


        fun shibie(resultArr: List<String>) {
            Handler(getMainLooper()).post(Runnable { setTextViewWords(resultArr) })
        }

        private fun setTextViewWords(resultArr: List<String>) {
            resultArr.forEachIndexed {index, text ->
                shouxieArray.getOrNull(index)?.text = text
            }

        }

        var mainActivity: Companion = this
        lateinit var shouxieArray: Array<TextView?>
    }

    override fun onPostResume() {
        super.onPostResume()
        cancelShouxie()
    }
}