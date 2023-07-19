package com.suraj.customedittext

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.Layout
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import java.util.regex.Pattern
import kotlin.math.roundToInt

/**
 * author suraj
 */
class CustomEditText(context: Context, attrs: AttributeSet?): AppCompatEditText(context, attrs) {

    // constant attributes
    private val borderStroke: Float by lazy { resources.getDimension(R.dimen.border_stroke) }
    private val paddingBig: Int by lazy { resources.getDimensionPixelSize(R.dimen.padding_big) }
    private val paddingSmall: Int by lazy { resources.getDimensionPixelSize(R.dimen.padding_small) }

    // attributes
    private var borderRadius: Float = resources.getDimension(R.dimen.border_radius)
    private val mainBodyHeight: Int by lazy { resources.getDimensionPixelSize(R.dimen.body_height) }
    private val labelHeight: Int by lazy { resources.getDimensionPixelSize(R.dimen.label_height) }
    private val drawableSize: Int by lazy { resources.getDimensionPixelSize(R.dimen.drawable_size) }

    private var textLayout: StaticLayout? = null
    private var shown: Boolean = false

    // validations attributes
    private var regex: String? = null
    private var inValidMessage: String? = null
    private var isRequired: Boolean = true
    private var isValid = false
    private var setNormal = false
    private var errorText: String? = null

    // colors
    private var normalStrokeColor: Int = 0
    private var normalFillColor: Int = 0
    private var successStrokeColor: Int = 0
    private var successFillColor: Int = 0
    private var errorStrokeColor: Int = 0
    private var errorFillColor: Int = 0
    private var defaultHintColor: Int = 0
    private var disabledStrokeColor: Int = 0
    private var disabledFillColor: Int = 0

    private var textColorEnabled: Int = 0
    private var textColorDisabled: Int = 0

    private var textColorStateList: ColorStateList? = null
    private var textColorHintStateList: ColorStateList? = null

    // padding attributes
    private var extraPaddingTop: Int = 0
    private var extraPaddingBottom: Int = 0
    private var customPaddingTop: Int = 0
    private var customPaddingBottom: Int = 0
    private var customPaddingLeft: Int = 0
    private var customPaddingRight: Int = 0


    // label attributes
    private var labelTextSize: Float = 0f
    private var labelTextColor: Int = 0
    private var labelText: String? = null

    // error text attributes
    private var errorLineCount = 1
    private var errorLines = 0
    private var showErrorText = true
    private var errorTextSize: Float = 0f
    private var errorLabelTextColor: Int = 0

    // paint objects
    private val textPaint by lazy { TextPaint(Paint.ANTI_ALIAS_FLAG) }
    private val labelTextPaint by lazy { TextPaint(Paint.ANTI_ALIAS_FLAG) }
    private val errorLabelTextPaint by lazy { TextPaint(Paint.ANTI_ALIAS_FLAG) }

    //icons attributes
    private var drawableRightId: Int = -1
    private var drawableLeftId: Int = -1
    private var drawableRight: Drawable? = null
    private var drawableLeft: Drawable? = null

    //typeface
    private var labelTypeface: Typeface? = null
    private var errorLabelTypeface: Typeface? = null

    //ripple effect attributes
    private var showRipple: Boolean = false
    private var rippleX: Float ? = null
    private var rippleY: Float ? = null
    private var rippleRadius: Float ? = null
    private val ripplePaint by lazy { Paint() }
    private val maxRippleRadius by lazy { (mainBodyHeight * 0.45).toFloat() }
    private var rippleColor: Int = 0

    var required: Boolean
        get() = isRequired
        set(value) {
            this.isRequired = value
        }

    fun setRegex(value: String?) {
        regex = value
    }

    private val isInternalValid: Boolean
        get() = errorText == null

    private val isRTL: Boolean
        get() {
            return false
        }

    init {
        init(context, attrs)
    }

    @SuppressLint("ResourceType", "CustomViewStyleable")
    private fun init(context: Context, attrs: AttributeSet?) {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomEditText)

        textColorEnabled = typedArray.getColor(R.styleable.CustomEditText_cc_textColor, ContextCompat.getColor(context, R.color.black))
        textColorDisabled = typedArray.getColor(R.styleable.CustomEditText_disabledTextColor, ContextCompat.getColor(context, R.color.light_black))
        textColorHintStateList = typedArray.getColorStateList(R.styleable.CustomEditText_cc_hintTextColor)
        defaultHintColor = typedArray.getColor(R.styleable.CustomEditText_cc_hintTextColor, ContextCompat.getColor(context, R.color.light_black))
        normalStrokeColor = typedArray.getColor(R.styleable.CustomEditText_strokeNormalColor, ContextCompat.getColor(context, R.color.black))
        normalFillColor = typedArray.getColor(R.styleable.CustomEditText_fillNormalColor, ContextCompat.getColor(context, R.color.grey_background))
        successStrokeColor = typedArray.getColor(R.styleable.CustomEditText_strokeSuccessColor, ContextCompat.getColor(context, R.color.green))
        successFillColor = typedArray.getColor(R.styleable.CustomEditText_fillSuccessColor, ContextCompat.getColor(context, R.color.background_green))
        errorStrokeColor = typedArray.getColor(R.styleable.CustomEditText_strokeErrorColor, ContextCompat.getColor(context, R.color.red))
        errorFillColor = typedArray.getColor(R.styleable.CustomEditText_fillErrorColor, ContextCompat.getColor(context, R.color.background_red))
        disabledStrokeColor = typedArray.getColor(R.styleable.CustomEditText_strokeDisabledColor, ContextCompat.getColor(context, R.color.light_black))
        disabledFillColor = typedArray.getColor(R.styleable.CustomEditText_fillDisabledColor, ContextCompat.getColor(context, R.color.disabled_white))

        showErrorText = typedArray.getBoolean(R.styleable.CustomEditText_showErrorText, true)

        regex = typedArray.getString(R.styleable.CustomEditText_regex)
        inValidMessage = typedArray.getString(R.styleable.CustomEditText_inValidMessage)
        isRequired = typedArray.getBoolean(R.styleable.CustomEditText_cc_isRequired, true)

        typedArray.getResourceId(R.styleable.CustomEditText_label_fontFamily, 0).apply {
            if(!isInEditMode && this > 0) {
                labelTypeface = ResourcesCompat.getFont(context, this)
                labelTextPaint.typeface = labelTypeface
                labelTextPaint
            }
        }
        typedArray.getResourceId(R.styleable.CustomEditText_errorLabel_fontFamily, 0).apply {
            if(!isInEditMode && this > 0) {
                errorLabelTypeface = ResourcesCompat.getFont(context, this)
                errorLabelTextPaint.typeface = errorLabelTypeface
            }
        }

        labelText = typedArray.getString(R.styleable.CustomEditText_labelText)
        labelTextSize = typedArray.getDimension(R.styleable.CustomEditText_labelTextSize, resources.getDimension(R.dimen.label_text_size))
        labelTextColor = typedArray.getColor(R.styleable.CustomEditText_labelColor, ContextCompat.getColor(context, R.color.black))

        errorTextSize = typedArray.getDimension(R.styleable.CustomEditText_errorLabelTextSize, resources.getDimension(R.dimen.error_label_text_size))
        errorLabelTextColor = typedArray.getColor(R.styleable.CustomEditText_errorLabelColor, ContextCompat.getColor(context, R.color.red))

        drawableLeftId = typedArray.getResourceId(R.styleable.CustomEditText_drawableLeftCompat, -1)
        drawableRightId = typedArray.getResourceId(R.styleable.CustomEditText_drawableRightCompat, -1)
        if (drawableLeftId != -1) drawableLeft = AppCompatResources.getDrawable(context, drawableLeftId)
        if (drawableRightId != -1) drawableRight = AppCompatResources.getDrawable(context, drawableRightId)

        typedArray.recycle()

        val paddings = intArrayOf(
            android.R.attr.padding, // 0
            android.R.attr.paddingLeft, // 1
            android.R.attr.paddingTop, // 2
            android.R.attr.paddingRight, // 3
            android.R.attr.paddingBottom // 4
        )
        val paddingsTypedArray = context.obtainStyledAttributes(attrs, paddings)
        val padding = paddingsTypedArray.getDimensionPixelSize(0, 0)
        customPaddingLeft = paddingsTypedArray.getDimensionPixelSize(1, padding)
        customPaddingTop = paddingsTypedArray.getDimensionPixelSize(2, padding)
        customPaddingRight = paddingsTypedArray.getDimensionPixelSize(3, padding)
        customPaddingBottom = paddingsTypedArray.getDimensionPixelSize(4, padding)
        paddingsTypedArray.recycle()

        background = null
        initPaint()
        initErrorLabelLines()
        initPadding()
        setViewHeight()
        initText()
        initTextWatcher()

        this.contentDescription = labelText
    }

    private fun initPaint() {
        labelTextPaint.textSize = labelTextSize
        labelTextPaint.color = labelTextColor
        labelTextPaint.letterSpacing = 0.033333335f

        errorLabelTextPaint.textSize = errorTextSize
        errorLabelTextPaint.letterSpacing = 0.033333335f

        paint.letterSpacing = 0.03125f

        val typedValue = TypedValue()
        val colorAttr = intArrayOf(android.R.attr.colorControlHighlight)
        val indexOfAttrColor = 0
        val c: TypedArray = context.obtainStyledAttributes(typedValue.data, colorAttr)
        rippleColor = c.getColor(indexOfAttrColor, -1)
        c.recycle()
    }

    private fun initText() {
        if (!text.isNullOrEmpty()) {
            val text = text
            setText(null)
            resetHintTextColor()
            setText(text)
            setSelection(text().length)
        } else {
            resetHintTextColor()
        }
        resetTextColor()
    }

    private fun initTextWatcher() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                when {
                    lineCount in 0..2 -> correctPaddings()
                    lineCount > 2 -> {
                        this@CustomEditText.filters = arrayOf(InputFilter.LengthFilter(s.length - 1))
                        text = s.delete(s.length - 1, s.length)
                        setSelection(text?.length ?: 0)
                    }
                }
                if (error != null) error = null
                isValid()
            }
        })
    }

    private fun getCustomTypeface(fontPath: String): Typeface? {
        return try {
            Typeface.createFromAsset(context.assets, fontPath)
        } catch (e: Exception) {
            null
        }
    }

    fun setTypeFace(typeface: String) {
        this.typeface = getCustomTypeface(typeface)
        postInvalidate()
    }

    fun setLabelTypeface(typeface: String) {
        this.labelTypeface = getCustomTypeface(typeface)
        this.labelTextPaint.typeface = labelTypeface
        postInvalidate()
    }

    fun setErrorLabelTypeface(typeface: String) {
        this.errorLabelTypeface = getCustomTypeface(typeface)
        this.errorLabelTextPaint.typeface = errorLabelTypeface
        postInvalidate()
    }

    fun setSuccess() {
        isValid = true
        postInvalidate()
    }

    fun setNormal() {
        isValid = false
        setNormal = true
        postInvalidate()
    }

    fun setLabelText(label: String) {
        labelText = label
        postInvalidate()
    }

    fun getLabelText(): String? {
        return labelText
    }

    private fun getPixel(dp: Float): Int {
        val displayMetrics = context.resources.displayMetrics
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)
        return px.roundToInt()
    }

    private fun initPadding() {
        customPaddingLeft = if (customPaddingLeft > 0) customPaddingLeft else if (paddingStart > 0) paddingStart else paddingLeft
        customPaddingRight = if (customPaddingRight > 0) customPaddingRight else if (paddingEnd > 0) paddingEnd else paddingRight
        customPaddingTop = if (customPaddingTop > 0) customPaddingTop else paddingTop
        customPaddingBottom = if (customPaddingBottom > 0) customPaddingBottom else paddingBottom
        extraPaddingTop = labelTextSize.toInt() + paddingBig
        val textMetrics = errorLabelTextPaint.fontMetrics
        extraPaddingBottom = ((textMetrics.descent - textMetrics.ascent) * errorLines).toInt() + paddingBig * 2
        correctPaddings()
    }

    private fun initErrorLabelLines() {
        errorLines = if (errorLineCount > 0) errorLineCount else if (errorText != null ) 1 else 0
    }

    private fun correctPaddings() {
        val left = getDrawableLeft()?.let {
            customPaddingLeft + drawableSize + compoundDrawablePadding
        } ?: run { customPaddingLeft }

        val right = getDrawableRight()?.let {
            customPaddingRight + drawableSize + compoundDrawablePadding
        } ?: run {
            customPaddingRight
        }

        var top = if (errorLineCount > 1) {
            gravity = Gravity.START
            paddingBig + (labelHeight * errorLineCount) + paddingSmall
        } else {
            gravity = Gravity.CENTER_VERTICAL
            paddingBig
        }
        top = if (lineCount > 1) top - paddingSmall else top

        super.setPadding(left, top, right, paddingBig)
    }

    private fun setViewHeight() {
        val errorTextHeight = if(errorLineCount > 1) (labelHeight * errorLineCount) + paddingSmall else labelHeight
        minHeight = scrollX + mainBodyHeight + labelHeight + errorTextHeight + paddingBig * 2
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!shown) {
            shown = true
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            adjustErrorLayout()
        }
    }

    private fun adjustErrorLayout() {
//        tempErrorText = "Oops sorry something went wrong, this field is required. Something error occurred, please try again.. Something error occurred, please try again."

        errorText?.let {
            val alignment = if (gravity and Gravity.END == Gravity.END || isRTL)
                Layout.Alignment.ALIGN_OPPOSITE
            else if (gravity and Gravity.START == Gravity.START)
                Layout.Alignment.ALIGN_NORMAL
            else
                Layout.Alignment.ALIGN_CENTER

            val text: String = if (errorText != null) errorText!! else ""
            textLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(text, 0, text.length, errorLabelTextPaint, width)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0.033333335f, 1.0F)
                    .setIncludePad(false)
                    .setEllipsize(TextUtils.TruncateAt.END)
                    .setMaxLines(2)
                    .setEllipsizedWidth(scrollX + width)
                    .build()
            } else {
                StaticLayout(
                    text, 0, text.length,
                    errorLabelTextPaint, width,
                    alignment,
                    1.0f, 0.033333335f,
                    false,
                    TextUtils.TruncateAt.END, width
                )
            }

            errorLineCount = textLayout?.lineCount ?: 1
            errorLineCount = if(errorLineCount > 2) 2 else errorLineCount
            if(errorLineCount > 1) correctPaddings()
            setViewHeight()
        }
    }

    override fun setTextColor(color: Int) {
        textColorEnabled = color
        setColorState()
        postInvalidate()
    }

    private fun setColorState() {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled)
        )
        val colors = intArrayOf(
            textColorEnabled,
            textColorDisabled
        )
        textColorStateList = ColorStateList(states, colors)
    }

    private fun resetTextColor() {
        if (textColorStateList == null) {
            setColorState()
        }
        setTextColor(textColorStateList)
    }

    private fun setHintColor(color: Int) {
        defaultHintColor = color
        textColorHintStateList = ColorStateList.valueOf(defaultHintColor)
        resetHintTextColor()
    }

    private fun resetHintTextColor() {
        if (textColorHintStateList == null) {
            textColorHintStateList = ColorStateList.valueOf(defaultHintColor)
        }
        setHintTextColor(textColorHintStateList)
    }

    fun text(): String {
        return this.text.toString().trim()
    }

    fun getDrawableRight(): Drawable? {
        return drawableRight
    }

    fun setDrawableRight(drawable: Drawable?) {
        drawable?.let { icon ->
            drawableRight = icon
            invalidate()
        }
    }

    fun getDrawableLeft(): Drawable? {
        return drawableLeft
    }

    fun setDrawableLeft(drawable: Drawable?) {
        drawable?.let { icon ->
            drawableLeft = icon
            invalidate()
        }
    }

    override fun isEnabled(): Boolean {
        return super.isEnabled().apply {
            try {
                if(!this) {
                    errorText = null
                    errorLineCount = 1
                    setViewHeight()
                    labelTextColor = ContextCompat.getColor(context, R.color.light_black)
                    labelTextPaint.color = labelTextColor
                    postInvalidate()
                } else {
                    labelTextColor = ContextCompat.getColor(context, android.R.color.black)
                    labelTextPaint.color = labelTextColor
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }
    }

    override fun setError(errorText: CharSequence?) {
        setNormal = false
        isValid = false
        this.errorText = errorText?.toString()
        postInvalidate()
    }

    private fun validateRequired(showError: Boolean): Boolean {
        val valid = !isRequired && this.text().trim().isEmpty() || this.text().trim().isNotEmpty()
        if (!valid && showError) {
            error = "Required"
        }
        return valid
    }

    private fun validateRegex(showError: Boolean): Boolean {
        val b = text().isEmpty() && !this.isRequired || regex == null || this.text().matches(regex = regex!!.toRegex())
        if (!b && showError) {
            error = if (inValidMessage != null && !inValidMessage!!.equals("", ignoreCase = true))
                inValidMessage
            else
                "Invalid"
        }
        return b
    }

    fun isValid(valid: Boolean, errorMessage: String): Boolean {
        var validField = valid
        if (valid) {
            validField = validate(true)
        } else {
            error = errorMessage
        }
        return validField
    }

    fun isValid(): Boolean {
        val valid = validate(false)
        if (!valid) {
            setNormal()
        }
        return valid
    }

    fun isValid(regex: String?): Boolean {
        if (regex == null) {
            return false
        }
        val pattern = Pattern.compile(regex)
        val mText = text ?: ""
        val matcher = pattern.matcher(mText)
        return matcher.matches()
    }

    fun validate(): Boolean {
        return validate(true)
    }

    fun validate(showError: Boolean): Boolean {
        val valid = validateRequired(showError) && validateRegex(showError)
        if (valid) {
            setSuccess()
        }
        return valid
    }

    fun validate(regex: String, errorText: CharSequence): Boolean {
        val isValid = isValid(regex)
        if (!isValid) {
            error = errorText
        }
        postInvalidate()
        return isValid
    }

    private fun getRectF(left: Float, top: Float, right: Float, bottom: Float) = RectF(left, top, right, bottom)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val startX = scrollX.toFloat()
        val endX = (scrollX + width).toFloat()

        val rectTop = startX + labelHeight + paddingBig
        val rectBottom = rectTop + mainBodyHeight

        /**   Draw rectangle   */

        if (isValid) {
            setPropertiesForRectangle(Status.SUCCESS, Paint.Style.FILL)
            fillRectangle(canvas, startX, rectTop, endX, rectBottom)
            setPropertiesForRectangle(Status.SUCCESS, Paint.Style.STROKE)
//            drawRoundRect(canvas, startX, endX, rectTop, rectBottom)

        } else if (!isInternalValid) { // not valid
            setPropertiesForRectangle(Status.ERROR, Paint.Style.FILL)
            fillRectangle(canvas, startX , rectTop, endX, rectBottom)
            setPropertiesForRectangle(Status.ERROR, Paint.Style.STROKE)

//            drawRoundRect(canvas, startX, endX, rectTop, rectBottom)
        } else {
            setPropertiesForRectangle(Status.NORMAL, Paint.Style.FILL)
            fillRectangle(canvas, startX , rectTop, endX, rectBottom)
            setPropertiesForRectangle(Status.NORMAL, Paint.Style.STROKE)
        }
        drawRoundRect(canvas, startX, endX, rectTop, rectBottom)

        /**   Draw error text   */
        if (showErrorText) {
            textLayout?.let { errorTextLayout ->
                if (errorText != null) {
                    errorLabelTextPaint.color = errorLabelTextColor
                    errorLabelTextPaint.textSize = errorTextSize
                    canvas.save()

                    if (isRTL) {
                        canvas.translate((endX - errorTextLayout.width), rectBottom)
                    } else {
                        canvas.translate(startX, rectBottom + paddingSmall)
                    }
                    errorTextLayout.draw(canvas)
                    canvas.restore()
                }
            }
        }

        /**  Draw label */
        if (!labelText.isNullOrEmpty()) {
            val floatingLabelWidth = labelTextPaint.measureText(labelText!!.toString())
            val floatingLabelStartX = if (gravity and Gravity.END == Gravity.END || isRTL) {
                endX - floatingLabelWidth
            } else if (gravity and Gravity.START == Gravity.START) {
                startX
            } else {
                startX + (customPaddingLeft + (width.toFloat() - customPaddingLeft.toFloat() - customPaddingRight.toFloat() - floatingLabelWidth) / 2).toInt()
            }
            val floatingLabelEndY = (labelHeight + scrollY).toFloat()

            canvas.drawText(labelText!!.toString(), floatingLabelStartX, floatingLabelEndY, labelTextPaint)

        }

        /**  Draw drawable icon  */
        drawableLeft?.let {
            val x = (startX + customPaddingLeft).toInt()
            val y = (rectTop + mainBodyHeight / 2 - drawableSize / 2).toInt()


            val x1 = (x + drawableSize)
            val y1 = (y + drawableSize)

            it.setBounds(x, y, x1, y1)
            it.draw(canvas)
        }

        drawableRight?.let {
            val x = (endX - customPaddingRight - drawableSize).toInt()
            val y = (rectTop + mainBodyHeight / 2 - drawableSize / 2).toInt()

            val x1 = x + drawableSize
            val y1 = (y + drawableSize)

            it.setBounds(x, y, x1, y1)
            it.draw(canvas)
        }

        /**  Draw ripple circle  */
        if (showRipple) {
            val rippleX = this.rippleX ?: return
            val rippleY = this.rippleY ?: return
            val rippleR = rippleRadius ?: return

            canvas.drawCircle(rippleX, rippleY, rippleR, ripplePaint)
        }
    }

    private fun fillRectangle(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        canvas.drawRoundRect(
            getRectF(left, top, right, bottom),
            borderRadius, borderRadius, paint
        )
    }

    private fun drawRoundRect(canvas: Canvas,startX:Float,endX:Float,rectTop:Float,rectBottom:Float){
        canvas.drawRoundRect(
            getRectF(
                (startX + getPixel(paint.strokeWidth) / 2),
                rectTop,
                (endX - getPixel(paint.strokeWidth) / 2),
                rectBottom
            ), borderRadius, borderRadius, paint
        )
    }

    private fun setPropertiesForRectangle(status: Status, style: Paint.Style) {
        if(!isEnabled) {
            paint.style = style
            paint.color = disabledFillColor
            return
        }

        when(status) {
            Status.NORMAL -> {
                paint.style = style
                if(style == Paint.Style.STROKE) {
                    paint.strokeWidth = borderStroke
                    paint.color = normalStrokeColor
                } else {
                    paint.color = normalFillColor
                }
            }
            Status.SUCCESS -> {
                paint.style = style
                if(style == Paint.Style.STROKE) {
                    paint.strokeWidth = borderStroke
                    paint.color = successStrokeColor
                } else {
                    paint.color = successFillColor
                }
            }
            Status.ERROR -> {
                paint.style = style
                if(style == Paint.Style.STROKE) {
                    paint.strokeWidth = borderStroke
                    paint.color = errorStrokeColor
                } else {
                    paint.color = errorFillColor
                }
            }

        }
    }

    fun setMaxLength(length: Int) {
        filters = arrayOf(InputFilter.LengthFilter(length))
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val connection = super.onCreateInputConnection(outAttrs)
        outAttrs.imeOptions = outAttrs.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION.inv()
        return connection
    }

    inner class InputFilterMinMax(private var min: Int, private var max: Int) : InputFilter {

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
            try {
                val input = (dest.toString() + source.toString()).toIntOrNull() ?: 0
                if (isInRange(min, max, input)) return null
            } catch (nfe: NumberFormatException) {
                nfe.printStackTrace()
            }
            return ""
        }

        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }

    private var drawableRightClickListener: OnDrawableRightClickListener? = null

    @SuppressLint("ClickableViewAccessibility")
    fun setDrawableRightClickListener(listener: OnDrawableRightClickListener) {
        this.drawableRightClickListener = listener
        this.setOnTouchListener(onTouchListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { _, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                onDrawableTouch(motionEvent)
            }
            else -> {
                onDrawableRelease()
            }
        }
        false
    }

    private fun onDrawableTouch(motionEvent: MotionEvent) {
        drawableRightClickListener?.let { listener ->
            getDrawableRight()?.let { icon ->
                if (
                    (motionEvent.rawX >= right - icon.bounds.width() - compoundDrawablePadding)
                    && (icon.bounds.contains(motionEvent.x.toInt(), motionEvent.y.toInt()))
                ) {

                    icon.alpha = 50

                    val x = (scrollX + width - customPaddingRight - drawableSize / 2).toFloat()
                    val y = (scrollX + labelHeight + paddingBig + mainBodyHeight / 2).toFloat()

                    val radius = (drawableSize / 2).toFloat()
                    startRipple(radius, x, y)
                    listener.onClick()
                    motionEvent.action = MotionEvent.ACTION_CANCEL
                }
            }
        }
    }

    private fun onDrawableRelease() {
        drawableRightClickListener?.let {
            getDrawableRight()?.alpha = 255
            stopRipple()
        }
    }

    private fun startRipple(radius: Float, x: Float, y: Float) {
        rippleRadius = radius
        rippleX = x
        rippleY = y
        ripplePaint.color = rippleColor
        rippleExpandAnimation.run()
    }

    private fun stopRipple() {
        if (rippleRadius != null) rippleFadeAnimation.run()
    }

    private val rippleExpandAnimation = object : Runnable {
        override fun run() {
            rippleRadius?.let { radius ->
                if (radius < maxRippleRadius) {
                    rippleRadius = radius + maxRippleRadius * 0.1f
                    showRipple = true
                    invalidate()
                    postDelayed(this, 20L)
                }
            }
        }
    }

    private val rippleFadeAnimation = object : Runnable {
        override fun run() {
            ripplePaint.color.let { color ->
                if (color.alpha > 10) {
                    ripplePaint.color = color.adjustAlpha(0.9f)
                    invalidate()
                    postDelayed(this, 10L)
                } else {
                    rippleRadius = null
                    showRipple = false
                    invalidate()
                }
            }
        }
    }

    private fun Int.adjustAlpha(factor: Float): Int =
        (this.ushr(24) * factor).roundToInt() shl 24 or (0x00FFFFFF and this)

    private inline val Int.alpha: Int
        get() = (this shr 24) and 0xFF

    interface OnDrawableRightClickListener {
        fun onClick()
    }

    enum class Status {
        NORMAL, SUCCESS, ERROR
    }

}