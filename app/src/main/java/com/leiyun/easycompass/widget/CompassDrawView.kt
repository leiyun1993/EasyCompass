package com.leiyun.easycompass.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.leiyun.easycompass.utils.DensityUtils


/**
 * 类名：CompassDrawView
 * 作者：Yun.Lei
 * 功能：
 * 创建日期：2017-08-09 19:27
 * 修改人：
 * 修改时间：
 * 修改备注：
 */
class CompassDrawView : View {

    //背景色
    private var mBackgroundColor: Int = Color.parseColor("#237EAD")
    /* 亮色，用于分针、秒针、渐变终止色 */
    private val mLightColor: Int = Color.parseColor("#ffffff")
    /* 暗色，圆弧、刻度线、时针、渐变起始色 */
    private val mDarkColor: Int = Color.parseColor("#80ffffff")
    /*指针颜色*/
    private val mNHandColor: Int = Color.parseColor("#ff0000")
    //文字大小
    private var mTextSize: Float = 0f
    /* 测量小时文本宽高的矩形 */
    private lateinit var mTextRect: Rect
    private lateinit var mTextPaint: Paint
    private lateinit var mCirclePaint: Paint
    /*北指针*/
    private lateinit var mNHandPaint: Paint
    /* 小时圆圈的外接矩形 */
    private lateinit var mCircleRectF: RectF
    /*大圆画笔*/
    private lateinit var mBGCirclePaint: Paint
    //画布
    private lateinit var mCanvas: Canvas
    /* 刻度圆弧画笔 */
    private lateinit var mScaleArcPaint: Paint
    /* 刻度线画笔 */
    private lateinit var mScaleLinePaint: Paint
    /* 刻度圆弧的外接矩形 */
    private lateinit var mScaleArcRectF: RectF
    /* 指针路径 */
    private lateinit var mNHandPath: Path

    /* 加一个默认的padding值，为了防止用camera旋转时钟时造成四周超出view大小 */
    private var mDefaultPadding: Float = 0.toFloat()
    private var mPaddingLeft: Float = 0.toFloat()
    private var mPaddingTop: Float = 0.toFloat()
    private var mPaddingRight: Float = 0.toFloat()
    private var mPaddingBottom: Float = 0.toFloat()
    /* 指针的在x轴的位移 */
    private val mCanvasTranslateX: Float = 0.toFloat()
    /* 指针的在y轴的位移 */
    private val mCanvasTranslateY: Float = 0.toFloat()
    /* 刻度线长度 */
    private var mScaleLength: Float = 0.toFloat()
    /* 时钟半径，不包括padding值 */
    private var mRadius: Float = 0.toFloat()
    /* 小时圆圈线条宽度 */
    private val mCircleStrokeWidth = 2f

    /*旋转角度*/
    private var mDirection: Float = 0.0f

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private fun initView() {
        mTextSize = DensityUtils.sp2px(context, 14f).toFloat()

        mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.color = mDarkColor
        mTextPaint.textSize = mTextSize

        mBGCirclePaint = Paint()
        mBGCirclePaint.style = Paint.Style.FILL
        mBGCirclePaint.color = mBackgroundColor

        mScaleLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mScaleLinePaint.style = Paint.Style.STROKE
        mScaleLinePaint.color = mBackgroundColor

        mNHandPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mNHandPaint.style = Paint.Style.FILL
        mNHandPaint.color = mNHandColor

        mCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCirclePaint.style = Paint.Style.STROKE
        mCirclePaint.strokeWidth = mCircleStrokeWidth
        mCirclePaint.color = mDarkColor

        mScaleArcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mScaleArcPaint.style = Paint.Style.STROKE
        mScaleArcPaint.color = mDarkColor

        mTextRect = Rect()
        mCircleRectF = RectF()
        mScaleArcRectF = RectF()
        mNHandPath = Path()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measureDimension(widthMeasureSpec), measureDimension(heightMeasureSpec))
    }

    fun updateDirection(direction: Float) {
        mDirection = direction
        invalidate()
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //宽和高分别去掉padding值，取min的一半即表盘的半径
        mRadius = (Math.min(w - paddingLeft - paddingRight, h - paddingTop - paddingBottom) / 2).toFloat()
        mDefaultPadding = 0.12f * mRadius //根据比例确定默认padding大小
        mPaddingLeft = mDefaultPadding + w / 2 - mRadius + paddingLeft
        mPaddingTop = mDefaultPadding + h / 2 - mRadius + paddingTop
        mPaddingRight = mPaddingLeft
        mPaddingBottom = mPaddingTop
        mScaleLength = 0.12f * mRadius//根据比例确定刻度线长度
        mScaleArcPaint.strokeWidth = mScaleLength
        mScaleLinePaint.strokeWidth = 0.012f * mRadius
    }

    private fun measureDimension(measureSpec: Int): Int {
        var result: Int
        val mode = View.MeasureSpec.getMode(measureSpec)
        val size = View.MeasureSpec.getSize(measureSpec)
        if (mode == View.MeasureSpec.EXACTLY) {
            result = size
        } else {
            result = 800
            if (mode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, size)
            }
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mCanvas = canvas
        canvas.save()           //保存画布，先旋转和方向无关的东西
        canvas.rotate(mDirection, (width / 2).toFloat(), (height / 2).toFloat())
        drawBackGroundCircle()
        drawScaleLine()
        canvas.restore()        //重置画布

        canvas.save()
        drawDirectionText()     //重置画布后通过传感器角度来确定文字的位置，以达到文字问正的情况
        drawAngleText()
        canvas.rotate(mDirection, (width / 2).toFloat(), (height / 2).toFloat())
        canvas.restore()
        drawNHand()             //最后画不需要动的指针
    }

    /**
     * 绘制方位文字
     */
    private fun drawDirectionText() {
        mTextPaint.color = mLightColor
        var timeText = "N"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        val textSmallWidth = mTextRect.width()  //1位数宽度
        val offset = mPaddingTop + textSmallWidth + 0.26f * mRadius
        mCanvas.drawText(timeText, width / 2 + getXx(width / 2 - offset, (0 + mDirection).toInt()).toFloat() - textSmallWidth / 2,
                height / 2 - getYy(width / 2 - offset, (0 + mDirection).toInt()).toFloat() + mTextRect.height() / 2, mTextPaint)

        timeText = "E"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        mCanvas.drawText(timeText, width / 2 + getXx(width / 2 - offset, (90 + mDirection).toInt()).toFloat() - textSmallWidth / 2,
                height / 2 - getYy(width / 2 - offset, (90 + mDirection).toInt()).toFloat() + mTextRect.height() / 2, mTextPaint)


        timeText = "S"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        mCanvas.drawText(timeText, width / 2 + getXx(width / 2 - offset, (180 + mDirection).toInt()).toFloat() - textSmallWidth / 2,
                height / 2 - getYy(width / 2 - offset, (180 + mDirection).toInt()).toFloat() + mTextRect.height() / 2, mTextPaint)

        timeText = "W"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        mCanvas.drawText(timeText, width / 2 + getXx(width / 2 - offset, (270 + mDirection).toInt()).toFloat() - textSmallWidth / 2,
                height / 2 - getYy(width / 2 - offset, (270 + mDirection).toInt()).toFloat() + mTextRect.height() / 2, mTextPaint)
    }

    /**
     * 绘制北的指针
     */
    private fun drawNHand() {
        mCanvas.save()
        mCanvas.rotate(0F, (width / 2).toFloat(), (height / 2).toFloat())
        mNHandPath.reset()
        val offset = mPaddingTop + mTextRect.height() / 2 + mTextRect.height()
        mNHandPath.moveTo((width / 2).toFloat(), offset + 0.27f * mRadius)
        mNHandPath.lineTo(width / 2 - 0.02f * mRadius, offset + 0.38f * mRadius)
        mNHandPath.lineTo(width / 2 + 0.02f * mRadius, offset + 0.38f * mRadius)
        mNHandPath.close()
        mNHandPaint.color = mNHandColor
        mCanvas.drawPath(mNHandPath, mNHandPaint)
        mCanvas.restore()
    }

    /**
     * 画一圈梯度渲染的亮暗色渐变圆弧，重绘时不断旋转，上面盖一圈背景色的刻度线
     */
    private fun drawScaleLine() {
        mCanvas.save()
        mCanvas.translate(mCanvasTranslateX, mCanvasTranslateY)
        mScaleArcRectF.set(mPaddingLeft + 1.5f * mScaleLength + mTextRect.height() / 2,
                mPaddingTop + 1.5f * mScaleLength + mTextRect.height() / 2,
                width - mPaddingRight - mTextRect.height() / 2 - 1.5f * mScaleLength,
                height - mPaddingBottom - mTextRect.height() / 2 - 1.5f * mScaleLength)
        mCanvas.drawArc(mScaleArcRectF, 0F, 360F, false, mScaleArcPaint)
        //画背景色刻度线
        for (i in 0..199) {
            mCanvas.drawLine((width / 2).toFloat(), mPaddingTop + mScaleLength + mTextRect.height() / 2,
                    (width / 2).toFloat(), mPaddingTop + 2 * mScaleLength + mTextRect.height() / 2, mScaleLinePaint)
            mCanvas.rotate(1.8f, (width / 2).toFloat(), (height / 2).toFloat())
        }
        mCanvas.restore()
    }

    /**
     * 画最外圈的时间文本和4个弧线
     */
    private fun drawAngleText() {
        val circleR = width / 2 - mPaddingTop - mCircleStrokeWidth / 2
        var timeText = "0"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        val textSmallWidth = mTextRect.width()  //1位数宽度
        val textHeight = mTextRect.height()
        mCanvas.drawText(timeText, width / 2 + getXx(circleR, (0 + mDirection).toInt()).toFloat() - textSmallWidth / 2,
                height / 2 - getYy(circleR, (0 + mDirection).toInt()).toFloat() + textHeight / 2, mTextPaint)

        timeText = "30"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        val textMiddleWidth = mTextRect.width()  //2位数宽度
        mCanvas.drawText(timeText, width / 2 + getXx(circleR, (30 + mDirection).toInt()).toFloat() - textMiddleWidth / 2,
                height / 2 - getYy(circleR, (30 + mDirection).toInt()).toFloat() + textHeight / 2, mTextPaint)

        timeText = "60"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        mCanvas.drawText(timeText, width / 2 + getXx(circleR, (60 + mDirection).toInt()).toFloat() - textMiddleWidth / 2,
                height / 2 - getYy(circleR, (60 + mDirection).toInt()).toFloat() + textHeight / 2, mTextPaint)

        timeText = "90"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        mCanvas.drawText(timeText, width / 2 + getXx(circleR, (90 + mDirection).toInt()).toFloat() - textMiddleWidth / 2,
                height / 2 - getYy(circleR, (90 + mDirection).toInt()).toFloat() + textHeight / 2, mTextPaint)

        timeText = "120"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        val textLargeWidth = mTextRect.width()//3位数字的宽
        mCanvas.drawText(timeText, width / 2 + getXx(circleR, (120 + mDirection).toInt()).toFloat() - textLargeWidth / 2,
                height / 2 - getYy(circleR, (120 + mDirection).toInt()).toFloat() + textHeight / 2, mTextPaint)

        timeText = "150"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        mCanvas.drawText(timeText, width / 2 + getXx(circleR, (150 + mDirection).toInt()).toFloat() - textLargeWidth / 2,
                height / 2 - getYy(circleR, (150 + mDirection).toInt()).toFloat() + textHeight / 2, mTextPaint)

        timeText = "180"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        mCanvas.drawText(timeText, width / 2 + getXx(circleR, (180 + mDirection).toInt()).toFloat() - textMiddleWidth / 2,
                height / 2 - getYy(circleR, (180 + mDirection).toInt()).toFloat() + textHeight / 2, mTextPaint)

        timeText = "210"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        mCanvas.drawText(timeText, width / 2 + getXx(circleR, (210 + mDirection).toInt()).toFloat() - textLargeWidth / 2,
                height / 2 - getYy(circleR, (210 + mDirection).toInt()).toFloat() + textHeight / 2, mTextPaint)

        timeText = "240"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        mCanvas.drawText(timeText, width / 2 + getXx(circleR, (240 + mDirection).toInt()).toFloat() - textLargeWidth / 2,
                height / 2 - getYy(circleR, (240 + mDirection).toInt()).toFloat() + textHeight / 2, mTextPaint)

        timeText = "270"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        mCanvas.drawText(timeText, width / 2 + getXx(circleR, (270 + mDirection).toInt()).toFloat() - textLargeWidth / 2,
                height / 2 - getYy(circleR, (270 + mDirection).toInt()).toFloat() + textHeight / 2, mTextPaint)

        timeText = "300"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        mCanvas.drawText(timeText, width / 2 + getXx(circleR, (300 + mDirection).toInt()).toFloat() - textLargeWidth / 2,
                height / 2 - getYy(circleR, (300 + mDirection).toInt()).toFloat() + textHeight / 2, mTextPaint)

        timeText = "330"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        mCanvas.drawText(timeText, width / 2 + getXx(circleR, (330 + mDirection).toInt()).toFloat() - textLargeWidth / 2,
                height / 2 - getYy(circleR, (330 + mDirection).toInt()).toFloat() + textHeight / 2, mTextPaint)


        //画4个弧
        mCircleRectF.set(mPaddingLeft + mTextRect.height() / 2 + mCircleStrokeWidth / 2,
                mPaddingTop + mTextRect.height() / 2 + mCircleStrokeWidth / 2,
                width - mPaddingRight - mTextRect.height() / 2 + mCircleStrokeWidth / 2,
                height - mPaddingBottom - mTextRect.height() / 2 + mCircleStrokeWidth / 2)
        for (i in 0..11) {
            mCanvas.drawArc(mCircleRectF, 5 + mDirection + 30 * i, 20F, false, mCirclePaint)
        }
    }

    private fun drawBackGroundCircle() {
        mCanvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (width / 2).toFloat(), mBGCirclePaint)
    }

    fun getXx(r: Float, angle: Int): Double {
        val radians = toRadians(angle)
        return Math.sin(radians) * r
    }

    fun getYy(r: Float, angle: Int): Double {
        val radians = toRadians(angle)
        return Math.cos(radians) * r
    }

    fun toRadians(angel: Int): Double {
        return Math.PI * angel / 180
    }
}


























