package com.example.shouxieDemokt

import android.R.attr
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.github.houbb.nlp.hanzi.similar.core.HanziSimilar
import com.github.houbb.nlp.hanzi.similar.util.HanziSimilarHelper
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.RecognitionContext
import com.google.mlkit.vision.digitalink.RecognitionResult
import com.google.mlkit.vision.digitalink.WritingArea
import java.util.Timer
import java.util.TimerTask


class SecondSurfaceView : SurfaceView, SurfaceHolder.Callback, Runnable {
    private var surfaceHolder: SurfaceHolder? = null
    private var paint: Paint? = null
    private var path: Path? = null
    private var isRunning = false
    private var isBeginDraw = false
    private var canvas: Canvas? = null

    private var myTimer: Timer? = null
    private var myTimerTask: TimerTask? = null
    private var digitalInkTool: DigitalInkRecognizeTool? = null
    private var inkRecognizer: DigitalInkRecognizer? = null
    private var inkBuilder = Ink.builder()
    private var strokeBuilder: Ink.Stroke.Builder = Ink.Stroke.builder()

    inner class MyTimerTask : TimerTask() {
        override fun run() {
            MainActivity.mainActivity!!.clearBoxWords()
            clean()

        }
    }

    fun startTimerTask() {
        if (myTimer == null && myTimerTask == null) {
            myTimer = Timer()
            myTimerTask = MyTimerTask()
            myTimer!!.schedule(myTimerTask, 3000)
        }
    }

    private fun stopTimerTask() {
        if (myTimerTask != null) {
            myTimerTask!!.cancel()
            myTimerTask = null
        }
        if(myTimer != null){
            myTimer!!.cancel()
            myTimer = null
        }
    }


    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {

        this.holder.setFormat(PixelFormat.TRANSLUCENT)
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {

        this.holder.setFormat(PixelFormat.TRANSLUCENT)
        initView()
    }

    constructor(context: Context?) : super(context) {
        this.holder.setFormat(PixelFormat.TRANSLUCENT)
        initView()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val t = System.currentTimeMillis()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isBeginDraw = true
                path!!.moveTo(x, y)
                strokeBuilder.addPoint(Ink.Point.create(x, y,t))
                stopTimerTask()
            }

            MotionEvent.ACTION_MOVE -> {
                path!!.lineTo(x ,y)
                strokeBuilder.addPoint(Ink.Point.create(x, y,t))
            }
            MotionEvent.ACTION_UP -> {
                isBeginDraw = false
                strokeBuilder.addPoint(Ink.Point.create(x, y,t))
                inkBuilder.addStroke(strokeBuilder.build())
                var ink = inkBuilder.build()
                inkRecognizer?.recognize(ink)
                    ?.addOnSuccessListener { result: RecognitionResult ->
                        val lc = result.candidates.map { it.text }
                        val resList = filterDuplicateStr(lc)
                        Log.d("dddex",resList.toString())
                        if (resList.size == 1){
                            val zi = resList.get(0).first()
                            var similarBigList = HanziSimilarHelper.similarList(zi)
                            var similarList = similarBigList
                            if(similarBigList.size > 6){
                                similarList = similarBigList.subList(0,6)
                            }
                            resList.addAll(similarList)
                        }
                        MainActivity.mainActivity.shibie(resList)
                    }
                    ?.addOnFailureListener { e: Exception ->

                    }
                startTimerTask()

            }

            else -> {}
        }
        return true
    }
    private fun filterDuplicateStr(list: List<String>): MutableList<String> {
        // Step 1: 提取每个字符串的最后一个字符
        val lastChars = list.map { it.last() }

        // Step 2: 统计每个字符出现的次数
        val countMap = mutableMapOf<Char, Int>()
        lastChars.forEach { char ->
            countMap[char] = countMap.getOrDefault(char, 0) + 1
        }

        val stringList: List<String> = lastChars.map { it.toString() }

        // Step 3: 如果有重复字符，返回一个包含重复字符的列表，反之返回所有字符
        val repeatedChars = countMap.filter { it.value > 1 }.keys
        return if (repeatedChars.isNotEmpty()) {
            // 如果有重复字符，保留第一个重复字符
            stringList.filter { it.last() == repeatedChars.first() }.take(1).toMutableList()
        } else {
            // 如果没有重复字符，返回所有字符
            stringList.toMutableList()
        }

    }

    private fun initView() {
        surfaceHolder = holder // 获得SurfaceHolder对象
        setZOrderOnTop(true)
        surfaceHolder!!.addCallback(this) // 为SurfaceView添加状态监听
        paint = Paint() // 创建一个画笔对象
        path = Path()

    }

    override fun run() {
        while (isRunning) {
            if (isBeginDraw) drawView()
        }
    }

    override fun surfaceChanged(arg0: SurfaceHolder, arg1: Int, arg2: Int, arg3: Int) {
    }

    override fun surfaceCreated(arg0: SurfaceHolder) {
        isRunning = true
        //开启一个绘画线程
        Thread(this).start()

        digitalInkTool = DigitalInkRecognizeTool()
        digitalInkTool!!.downloadDigitalInkModel("zh-CN",object : DigitalInkRecognizeTool.DigitalModelDownloadListener{
            override fun onDownloadSuccess(digitalInkRecognizer: DigitalInkRecognizer) {
                inkRecognizer = digitalInkRecognizer
            }

            override fun onDownloadFailure() {
                TODO("Not yet implemented")
            }


        })
    }

    override fun surfaceDestroyed(arg0: SurfaceHolder) {
        isRunning = false
        isBeginDraw = false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isRunning = false
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun drawView() {
        try {
            if (surfaceHolder != null) {
                canvas = surfaceHolder!!.lockCanvas()
                paint!!.color = Color.parseColor("#E5FA6028")
                paint!!.style = Paint.Style.STROKE
                paint!!.strokeWidth = 10f
                canvas!!.drawPath(path!!, paint!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (canvas != null && surfaceHolder != null) {
                surfaceHolder!!.unlockCanvasAndPost(canvas)
            }
        }
    }

    /**
     * 清除内容
     */
    fun clean() {
        for (i in 0..3) {
            try {
                inkBuilder = Ink.builder()
                strokeBuilder = Ink.Stroke.builder()
                isBeginDraw = false
                if (surfaceHolder != null) {
                    canvas = surfaceHolder!!.lockCanvas()
                    canvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    paint!!.reset()
                    path!!.reset()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    surfaceHolder!!.unlockCanvasAndPost(canvas)
                }
            }
        }

    }

}