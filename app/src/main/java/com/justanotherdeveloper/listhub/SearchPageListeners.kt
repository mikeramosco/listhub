package com.justanotherdeveloper.listhub

import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_search_page.*

class SearchPageListeners(private val activity: SearchPageActivity) {

    init {
        initButtonAnimationListeners()
        initOnClickListeners()
        initScrollViewListener()
        initSearchFieldListener()
    }

    private fun initButtonAnimationListeners() {
        initButtonAnimationListener(activity.backArrow)
        initButtonAnimationListener(activity.filterButton)
    }

    private fun initOnClickListeners() {
        activity.backArrow.setOnClickListener {
            activity.finish()
        }

        activity.filterButton.setOnClickListener {
            activity.showFilterSearchDialog()
        }
    }

    private fun initScrollViewListener() {
        activity.itemsScrollView.viewTreeObserver.addOnScrollChangedListener {
            activity.getView().releaseItemViews()

            val view = activity.itemsScrollView.getChildAt(
                activity.itemsScrollView.childCount - 1)
            val diff = view.bottom - (activity.itemsScrollView.height +
                    activity.itemsScrollView.scrollY)
            if(diff == 0) activity.getView().loadMoreItems()
        }

        activity.itemsScrollView.setOnTouchListener { _, motionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_MOVE -> {
                    calibrateScrollView()
                }
                MotionEvent.ACTION_UP -> {
                    activity.getView().releaseItemViews()
                }
            }
            false
        }
    }

    private fun calibrateScrollView() {
        val scrollViewHeight = activity.itemsScrollViewContents.height - activity.itemsScrollView.height
        if(activity.itemsScrollView.scrollY == 0) {
            activity.itemsScrollView.scrollY = 1
        } else if(activity.itemsScrollView.scrollY == scrollViewHeight) {
            activity.itemsScrollView.scrollY = scrollViewHeight - 1
        }
    }

    @Suppress("DEPRECATION")
    private val handler = Handler()

    private fun initSearchFieldListener() {
        activity.searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                activity.searchBarLoadingCircle.visibility = View.VISIBLE
                val searchString = activity.searchField.text.toString()
                handler.postDelayed({
                    val updatedSearchString = activity.searchField.text.toString()
                    if(searchString == updatedSearchString)
                        activity.getManager().applySearch()
                }, SEARCH_DELAY)
            }
        })
    }
}