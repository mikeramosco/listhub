package com.justanotherdeveloper.listhub

import android.graphics.Rect
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.iterator
import androidx.core.view.size
import kotlinx.android.synthetic.main.fragment_lists.*

class ListsFragmentListeners(private val fragment: ListsFragment) {

    private val listTitleOptionPressed = ArrayList<Boolean>()
    private val listTitleOptionHighlighted = ArrayList<Boolean>()

    init {
        initListTitleOptionPressedBooleans()
        initButtonAnimationListeners()
        initOnClickListeners()
        initScrollViewListeners()
    }

    private fun initListTitleOptionPressedBooleans() {
        for(view in fragment.listTitlesOptionsContainer) {
            listTitleOptionPressed.add(false)
            listTitleOptionHighlighted.add(false)
        }
    }

    private fun setOptionPressed(option: View, isPressed: Boolean? = null, isHighlighted: Boolean? = null) {
        for((i, view) in fragment.listTitlesOptionsContainer.iterator().withIndex()) {
            if(view == option) {
                if(isPressed != null) listTitleOptionPressed[i] = isPressed
                if(isHighlighted != null) listTitleOptionHighlighted[i] = isHighlighted
                break
            }
        }
    }

    private fun optionPressed(option: View): Boolean {
        for((i, view) in fragment.listTitlesOptionsContainer.iterator().withIndex())
            if(view == option) return listTitleOptionPressed[i]
        return false
    }

    private fun optionHighlighted(option: View): Boolean {
        for((i, view) in fragment.listTitlesOptionsContainer.iterator().withIndex())
            if(view == option) return listTitleOptionHighlighted[i]
        return false
    }

    @Suppress("DEPRECATION")
    private fun initListTitleOptionAnimationListener(option: View) {
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
        initListTitleOptionAnimationListener(fragment.sortByOption)
        initListTitleOptionAnimationListener(fragment.filterListsOption)
        initListTitleOptionAnimationListener(fragment.showDetailsOption)
        initListTitleOptionAnimationListener(fragment.reorderOption)
        initListTitleOptionAnimationListener(fragment.deleteOption)
        initListTitleOptionAnimationListener(fragment.duplicateOption)
        initListTitleOptionAnimationListener(fragment.archiveOption)
        initListTitleOptionAnimationListener(fragment.unarchiveOption)
        initListTitleOptionAnimationListener(fragment.manageLabelsOption)
        initListTitleOptionAnimationListener(fragment.selectAllOption)
        initListTitleOptionAnimationListener(fragment.clearSelectionOption)
    }

    private fun initOnClickListeners() {
        fragment.sortByOption.setOnClickListener {
            fragment.getDialogs().showSortByDialog()
        }
        fragment.filterListsOption.setOnClickListener {
            fragment.getDialogs().showFilterListsDialog()
        }
        fragment.showDetailsOption.setOnClickListener {
            fragment.getDatabase().toggleListsShowDetails()
            fragment.getListsView().toggleShowDetails()
        }
        fragment.reorderOption.setOnClickListener {
            if(fragment.listTitlesContainer.size > 1)
                fragment.getHomeActivity().openReorderPage()
        }
        fragment.deleteOption.setOnClickListener {
            fragment.getManager().deleteOptionClicked()
        }
        fragment.duplicateOption.setOnClickListener {
            fragment.getManager().duplicateOptionClicked()
        }
        fragment.archiveOption.setOnClickListener {
            fragment.getManager().archiveOptionClicked()
        }
        fragment.unarchiveOption.setOnClickListener {
            fragment.getManager().unarchiveOptionClicked()
        }
        fragment.manageLabelsOption.setOnClickListener {
            fragment.getDialogs().showManageLabelsDialog()
        }
        fragment.selectAllOption.setOnClickListener {
            fragment.getListsView().selectAll()
        }
        fragment.clearSelectionOption.setOnClickListener {
            fragment.getListsView().setToDefaultState()
        }
        fragment.createNewListLayout.setOnClickListener {
            fragment.getHomeActivity().getDialogs().showAddListOrItemDialog(true)
        }
    }

    private fun initScrollViewListeners() {
        fragment.listTitlesScrollView.viewTreeObserver.addOnScrollChangedListener {
            fragment.getListsView().releaseTaskViews()
            releaseListTitleOptions()
        }

        fragment.listTitlesScrollView.setOnTouchListener { _, motionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_MOVE -> {
                    calibrateScrollView()
                }
                MotionEvent.ACTION_UP -> {
                    fragment.getListsView().releaseTaskViews()
                    releaseListTitleOptions()
                }
            }
            false
        }

        fragment.listTitlesOptionsScrollView.viewTreeObserver.addOnScrollChangedListener {
            releaseListTitleOptions()
        }

        fragment.listTitlesOptionsScrollView.setOnTouchListener { _, motionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_MOVE -> {
                    calibrateListTitleOptionsScrollView()
                }
                MotionEvent.ACTION_UP -> {
                    releaseListTitleOptions()
                }
            }
            false
        }
    }

    private fun calibrateScrollView() {
        val scrollViewHeight = fragment.listsTitlesScrollViewContents
            .height - fragment.listTitlesScrollView.height
        if(fragment.listTitlesScrollView.scrollY == 0) {
            fragment.listTitlesScrollView.scrollY = 1
        } else if(fragment.listTitlesScrollView.scrollY == scrollViewHeight) {
            fragment.listTitlesScrollView.scrollY = scrollViewHeight - 1
        }
    }

    private fun calibrateListTitleOptionsScrollView() {
        val scrollViewWidth = fragment.listTitlesOptionsContainer.width -
                fragment.listTitlesOptionsScrollView.width
        if(fragment.listTitlesOptionsScrollView.scrollX == 0) {
            fragment.listTitlesOptionsScrollView.scrollX = 1
        } else if(fragment.listTitlesOptionsScrollView.scrollX == scrollViewWidth) {
            fragment.listTitlesOptionsScrollView.scrollX = scrollViewWidth - 1
        }
    }

    private fun releaseListTitleOptions() {
        for((i, view) in fragment.listTitlesOptionsContainer.iterator().withIndex()) {
            if(listTitleOptionHighlighted[i]) {
                animateButton(view as LinearLayout, false)
                listTitleOptionHighlighted[i] = false
            }

            if(listTitleOptionPressed[i])
                listTitleOptionPressed[i] = false
        }
    }
}