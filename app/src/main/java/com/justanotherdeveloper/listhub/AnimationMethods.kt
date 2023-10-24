@file:Suppress("DEPRECATION")

package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.drawable.TransitionDrawable
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.HashMap

// Within a scrollview in a dialog,
// method adds animation feature to button views from "dialogOptions"
// to fade between 2 colors when pressed & released
@SuppressLint("ClickableViewAccessibility")
fun initDialogScrollOptions(scrollView: ScrollView,
                            scrollViewContents: LinearLayout,
                            dialogOptions: ArrayList<View>) {

    // Maps to track if a button view is pressed or highlighted
    // (Highlighted: faded to highlighted button color)
    val viewPressedMap = HashMap<View, Boolean>()
    val viewHighlightedMap = HashMap<View, Boolean>()

    // init maps
    for(view in dialogOptions) {
        viewPressedMap[view] = false
        viewHighlightedMap[view] = false
    }

    // sets scrollview one off from top/bottom if detected at top/bottom
    fun calibrateScrollView() {
        val scrollViewHeight = scrollViewContents.height - scrollView.height
        if(scrollView.scrollY == 0) scrollView.scrollY = 1
        else if(scrollView.scrollY == scrollViewHeight)
            scrollView.scrollY = scrollViewHeight - 1
    }

    // iterates through button views and releases them
    // (Release: faded back to original button color)
    fun releaseViews() {
        fun releaseView(view: View) {
            val viewIsHighlighted = viewHighlightedMap[view]
            if(viewIsHighlighted != null && viewIsHighlighted) {
                viewHighlightedMap[view] = false
                animateButton(view, false)
            }

            val viewIsPressed = viewPressedMap[view]
            if(viewIsPressed != null && viewIsPressed)
                viewPressedMap[view] = false
        }

        for(view in dialogOptions) releaseView(view)
    }

    // auto releases all button views if scrollview moved
    // (listeners will detect movement even if at top/bottom
    // because of "calibrateScrollView()")
    scrollView.viewTreeObserver.addOnScrollChangedListener {
        releaseViews()
    }
    scrollView.setOnTouchListener { _, motionEvent ->
        when(motionEvent.action) {
            MotionEvent.ACTION_MOVE -> {
                calibrateScrollView()
            }
            MotionEvent.ACTION_UP -> {
                releaseViews()
            }
        }
        false
    }

    // takes button view and uses Rect object to detect
    // if user's finger is pressed over the button view
    fun initOptionAnimationListener(view: View) {
        val handler = Handler()
        var viewBounds = Rect()
        view.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    viewPressedMap[view] = true
                    viewBounds = Rect(v.left, v.top, v.right, v.bottom)
                    handler.postDelayed({
                        val taskViewIsPressed = viewPressedMap[view]
                        if(taskViewIsPressed != null && taskViewIsPressed) {
                            viewHighlightedMap[view] = true
                            animateButton(view, true)
                        }
                    }, TRANSITION_DELAY)
                }
                MotionEvent.ACTION_UP -> {
                    val taskViewIsPressed = viewPressedMap[view]
                    if(taskViewIsPressed != null && taskViewIsPressed &&
                        viewBounds.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                        viewHighlightedMap[view] = false
                        viewPressedMap[view] = false
                        animateButton(view, false)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val taskViewIsPressed = viewPressedMap[view]
                    if(taskViewIsPressed != null && taskViewIsPressed &&
                        !viewBounds.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                        viewPressedMap[view] = false

                        val taskViewIsHighlighted = viewHighlightedMap[view]
                        if(taskViewIsHighlighted != null && taskViewIsHighlighted) {
                            viewHighlightedMap[view] = false
                            animateButton(view, false)
                        }
                    }
                }
            }
            false
        }
    }

    for(view in dialogOptions)
        initOptionAnimationListener(view)
}

// Within a horizontal scrollview in a dialog,
// method adds animation feature to button views from "dialogOptions"
// to fade between 2 colors when pressed & released
@SuppressLint("ClickableViewAccessibility")
fun initDialogHorizontalScrollOptions(scrollView: HorizontalScrollView,
                                      scrollViewContents: LinearLayout,
                                      dialog: BottomSheetDialog,
                                      dialogOptions: ArrayList<LinearLayout>) {

    // Lists to track if a button view is pressed or highlighted
    // (Highlighted: faded to highlighted button color)
    val optionPressed = ArrayList<Boolean>()
    val optionHighlighted = ArrayList<Boolean>()

    // init lists
    for(view in dialogOptions) {
        optionPressed.add(false)
        optionHighlighted.add(false)
    }

    fun setOptionPressed(option: View, isPressed: Boolean? = null, isHighlighted: Boolean? = null) {
        for((i, view) in dialogOptions.withIndex()) {
            if(view == option) {
                if(isPressed != null) optionPressed[i] = isPressed
                if(isHighlighted != null) optionHighlighted[i] = isHighlighted
                break
            }
        }
    }

    fun optionPressed(option: View): Boolean {
        for((i, view) in dialogOptions.iterator().withIndex())
            if(view == option) return optionPressed[i]
        return false
    }

    fun optionHighlighted(option: View): Boolean {
        for((i, view) in dialogOptions.iterator().withIndex())
            if(view == option) return optionHighlighted[i]
        return false
    }

    // iterates through button views and releases them
    // (Release: faded back to original button color)
    fun releaseOptions() {
        for((i, view) in dialogOptions.withIndex()) {
            if(optionHighlighted[i]) {
                animateButton(view, false)
                optionHighlighted[i] = false
            }

            if(optionPressed[i])
                optionPressed[i] = false
        }
    }

    // releases button views if BottomSheetDialog is being dragged down
    dialog.behavior.setBottomSheetCallback(object :
        BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_DRAGGING)
                releaseOptions()
            if(newState == BottomSheetBehavior.STATE_HIDDEN)
                dialog.cancel()
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) { }
    })

    // sets scrollview one off from top/bottom if detected at top/bottom
    fun calibrateOptionsScrollView() {
        val scrollViewWidth = scrollViewContents.width - scrollView.width
        if(scrollView.scrollX == 0) scrollView.scrollX = 1
        else if(scrollView.scrollX == scrollViewWidth)
            scrollView.scrollX = scrollViewWidth - 1
    }

    // auto releases all button views if scrollview moved
    // (listeners will detect movement even if at top/bottom
    // because of "calibrateScrollView()")
    scrollView.viewTreeObserver.addOnScrollChangedListener {
        releaseOptions()
    }
    scrollView.setOnTouchListener { _, motionEvent ->
        when(motionEvent.action) {
            MotionEvent.ACTION_MOVE -> {
                calibrateOptionsScrollView()
            }
            MotionEvent.ACTION_UP -> {
                releaseOptions()
            }
        }
        false
    }

    // takes button view and uses Rect object to detect
    // if user's finger is pressed over the button view
    fun initScrollViewOptionAnimationListener(option: View) {
        val handler = Handler()
        var viewBounds = Rect()
        option.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    setOptionPressed(option, isPressed = true)
                    viewBounds = Rect(v.left, v.top, v.right, v.bottom)
                    handler.postDelayed({
                        if(optionPressed(option)) {
                            setOptionPressed(option, isHighlighted = true)
                            animateButton(option, true)
                        }
                    }, TRANSITION_DELAY)
                }
                MotionEvent.ACTION_UP -> {
                    if(optionPressed(option) &&
                        viewBounds.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                        setOptionPressed(option, isPressed = false, isHighlighted = false)
                        animateButton(option, false)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if(optionPressed(option) &&
                        !viewBounds.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                        setOptionPressed(option, isPressed = false)

                        if(optionHighlighted(option)) {
                            setOptionPressed(option, isHighlighted = false)
                            animateButton(option, false)
                        }
                    }
                }
            }
            false
        }
    }

    for(view in dialogOptions)
        initScrollViewOptionAnimationListener(view)
}

// method adds animation feature to button views in a dialog from "dialogOptions"
// to fade between 2 colors when pressed & released
fun initDialogOptions(dialog: BottomSheetDialog? = null,
                      dialogOptions: ArrayList<LinearLayout>) {

    // Lists to track if a button view is pressed
    val dialogOptionPressed = ArrayList<Boolean>()
    for(i in 0 until dialogOptions.size)
        dialogOptionPressed.add(false)

    fun setOptionPressed(option: LinearLayout, isPressed: Boolean) {
        dialogOptionPressed[dialogOptions.indexOf(option)] = isPressed
    }

    fun optionIsPressed(option: LinearLayout): Boolean {
        return dialogOptionPressed[dialogOptions.indexOf(option)]
    }

    // takes a button view and releases it
    // (Release: faded back to original button color)
    fun releaseOption(option: LinearLayout, showFadeOut: Boolean = true) {
        animateButton(option, false, showFadeOut)
        setOptionPressed(option, false)
    }

    // releases button views if BottomSheetDialog is being dragged down
    dialog?.behavior?.setBottomSheetCallback(object :
        BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                for(option in dialogOptions)
                    if(optionIsPressed(option))
                        releaseOption(option, false)
            }
            if(newState == BottomSheetBehavior.STATE_HIDDEN) {
                dialog.cancel()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) { }
    })

    // takes button view and uses Rect object to detect
    // if user's finger is pressed over the button view
    fun initAnimatedOption(option: LinearLayout) {
        var viewBounds = Rect()

        fun touchIsInViewBounds(v: View, event: MotionEvent): Boolean {
            return viewBounds.contains(v.left + event.x.toInt(), v.top + event.y.toInt())
        }

        option.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    setOptionPressed(option, true)
                    viewBounds = Rect(v.left, v.top, v.right, v.bottom)
                    animateButton(option, true)
                }
                MotionEvent.ACTION_UP -> {
                    if(optionIsPressed(option) && touchIsInViewBounds(v, event))
                        releaseOption(option)
                }
                MotionEvent.ACTION_MOVE -> {
                    if(optionIsPressed(option) && !touchIsInViewBounds(v, event))
                        releaseOption(option)
                }
            }
            false
        }
    }

    for(option in dialogOptions)
        initAnimatedOption(option)
}

// takes button view and uses Rect object to detect
// if user's finger is pressed over the button view
fun initButtonAnimationListener(button: View) {
    var viewBounds = Rect()
    var buttonPressed = false
    button.setOnTouchListener { v, event ->
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                buttonPressed = true
                viewBounds = Rect(v.left, v.top, v.right, v.bottom)
                animateButton(button, true)
            }
            MotionEvent.ACTION_UP -> {
                if(buttonPressed &&
                    viewBounds.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                    animateButton(button, false)
                    buttonPressed = false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if(buttonPressed &&
                    !viewBounds.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                    animateButton(button, false)
                    buttonPressed = false
                }
            }
        }
        false
    }
}

// takes button view and adds animation feature to it
// to fade between 2 colors when pressed & released
fun animateButton(button: View, isPressed: Boolean, showFadeOut: Boolean = true) {

    val transition = button.background as TransitionDrawable

    if(isPressed)
        transition.startTransition(FADE_IN_DURATION)
    else {
        if(showFadeOut) transition.startTransition(0)
        transition.reverseTransition(FADE_OUT_DURATION)
    }
}