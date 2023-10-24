package com.justanotherdeveloper.listhub

import android.graphics.Rect
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.iterator
import kotlinx.android.synthetic.main.fragment_home.*



class HomeFragmentListeners(private val fragment: HomeFragment) {

    private val homeSectionOptionPressed = ArrayList<Boolean>()
    private val homeSectionOptionHighlighted = ArrayList<Boolean>()

    init {
        initHomeSectionOptionPressedBooleans()
        initButtonAnimationListeners()
        initOnClickListeners()
        initScrollViewListeners()
    }

    private fun initHomeSectionOptionPressedBooleans() {
        for(view in fragment.homeSectionOptionsContainer) {
            homeSectionOptionPressed.add(false)
            homeSectionOptionHighlighted.add(false)
        }
    }

    private fun setOptionPressed(option: View, isPressed: Boolean? = null, isHighlighted: Boolean? = null) {
        for((i, view) in fragment.homeSectionOptionsContainer.iterator().withIndex()) {
            if(view == option) {
                if(isPressed != null) homeSectionOptionPressed[i] = isPressed
                if(isHighlighted != null) homeSectionOptionHighlighted[i] = isHighlighted
                break
            }
        }
    }

    private fun optionPressed(option: View): Boolean {
        for((i, view) in fragment.homeSectionOptionsContainer.iterator().withIndex())
            if(view == option) return homeSectionOptionPressed[i]
        return false
    }

    private fun optionHighlighted(option: View): Boolean {
        for((i, view) in fragment.homeSectionOptionsContainer.iterator().withIndex())
            if(view == option) return homeSectionOptionHighlighted[i]
        return false
    }

    @Suppress("DEPRECATION")
    private fun initHomeSectionOptionAnimationListener(option: View) {
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

    private fun initButtonAnimationListeners() {
        initHomeSectionOptionAnimationListener(fragment.dateRangeOption)
        initHomeSectionOptionAnimationListener(fragment.filterSectionOption)
        initHomeSectionOptionAnimationListener(fragment.showDetailsOption)
    }

    private fun initOnClickListeners() {
        fragment.dateRangeOption.setOnClickListener {
            fragment.getDialogs().showSetDateRangeDialog()
        }
        fragment.filterSectionOption.setOnClickListener {
            fragment.getDialogs().showFilterSearchDialog()
        }
        fragment.showDetailsOption.setOnClickListener {
            fragment.getDatabase().toggleHomeShowDetails()
            fragment.getHomeView().toggleShowDetails()
        }
        fragment.selectSectionLayout.setOnClickListener {
            fragment.getDialogs().showSelectHomeSectionDialog()
        }
    }

    private fun initScrollViewListeners() {
        fragment.homeScrollView.viewTreeObserver.addOnScrollChangedListener {
            fragment.getHomeView().releaseItemViews()
            releaseHomeSectionOptions()

            val view = fragment.homeScrollView.getChildAt(
                fragment.homeScrollView.childCount - 1)
            val diff = view.bottom - (fragment.homeScrollView.height +
                    fragment.homeScrollView.scrollY)
            if(diff == 0) fragment.getHomeView().loadMoreItems()
        }

        fragment.homeScrollView.setOnTouchListener { _, motionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_MOVE -> {
                    calibrateScrollView()
                }
                MotionEvent.ACTION_UP -> {
                    fragment.getHomeView().releaseItemViews()
                    releaseHomeSectionOptions()
                }
            }
            false
        }

        fragment.homeScrollView.viewTreeObserver.addOnScrollChangedListener {
            releaseHomeSectionOptions()
        }

        fragment.homeSectionOptionsScrollView.setOnTouchListener { _, motionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_MOVE -> {
                    calibrateHomeSectionOptionsScrollView()
                }
                MotionEvent.ACTION_UP -> {
                    releaseHomeSectionOptions()
                }
            }
            false
        }
    }

    private fun calibrateScrollView() {
        val scrollViewHeight = fragment.homeScrollViewContents
            .height - fragment.homeScrollView.height
        if(fragment.homeScrollView.scrollY == 0) {
            fragment.homeScrollView.scrollY = 1
        } else if(fragment.homeScrollView.scrollY == scrollViewHeight) {
            fragment.homeScrollView.scrollY = scrollViewHeight - 1
        }
    }

    private fun calibrateHomeSectionOptionsScrollView() {
        val scrollViewWidth = fragment.homeSectionOptionsContainer.width -
                fragment.homeSectionOptionsScrollView.width
        if(fragment.homeSectionOptionsScrollView.scrollX == 0) {
            fragment.homeSectionOptionsScrollView.scrollX = 1
        } else if(fragment.homeSectionOptionsScrollView.scrollX == scrollViewWidth) {
            fragment.homeSectionOptionsScrollView.scrollX = scrollViewWidth - 1
        }
    }

    private fun releaseHomeSectionOptions() {
        for((i, view) in fragment.homeSectionOptionsContainer.iterator().withIndex()) {
            if(homeSectionOptionHighlighted[i]) {
                animateButton(view as LinearLayout, false)
                homeSectionOptionHighlighted[i] = false
            }

            if(homeSectionOptionPressed[i])
                homeSectionOptionPressed[i] = false
        }
    }

}