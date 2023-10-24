package com.justanotherdeveloper.listhub

import android.view.MotionEvent
import kotlinx.android.synthetic.main.activity_bulleted_list.*

class BulletedListListeners(private val activity: BulletedListActivity) {

    init {
        initButtonAnimationListeners()
        initOnClickListeners()
        initScrollViewListeners()
    }

    private fun initScrollViewListeners() {
        activity.tasksScrollView.viewTreeObserver.addOnScrollChangedListener {
            activity.getView().releaseTaskViews()
        }

        activity.tasksScrollView.setOnTouchListener { _, motionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_MOVE -> {
                    calibrateScrollView()
                }
                MotionEvent.ACTION_UP -> {
                    activity.getView().releaseTaskViews()
                }
            }
            false
        }
    }

    private fun calibrateScrollView() {
        val scrollViewHeight = activity.tasksScrollViewContents.height - activity.tasksScrollView.height
        if(activity.tasksScrollView.scrollY == 0) {
            activity.tasksScrollView.scrollY = 1
        } else if(activity.tasksScrollView.scrollY == scrollViewHeight) {
            activity.tasksScrollView.scrollY = scrollViewHeight - 1
        }
    }

    private fun initButtonAnimationListeners() {
        initListActionBarAnimationListeners()
        initTaskSelectActionBarAnimationListeners()
    }

    private fun initListActionBarAnimationListeners() {
        initButtonAnimationListener(activity.backArrow)
        initButtonAnimationListener(activity.moreOptions)
        initButtonAnimationListener(activity.listActionBar)
        initButtonAnimationListener(activity.addButton)
    }

    private fun initTaskSelectActionBarAnimationListeners() {
        initButtonAnimationListener(activity.closeTaskSelect)
        initButtonAnimationListener(activity.addDate)
        initButtonAnimationListener(activity.selectAll)
        initButtonAnimationListener(activity.selectedMoreOptions)
    }

    private fun initOnClickListeners() {
        initListActionBarOnClickListeners()
        initTaskSelectActionBarOnClickListeners()

        activity.sortedByTitleBar.setOnClickListener {
            activity.getView().toggleSortedOrder()
        }

        activity.sortedByExitButton.setOnClickListener {
            activity.getManager().sortList()
        }
    }

    private fun initListActionBarOnClickListeners() {
        activity.backArrow.setOnClickListener {
            if(!activity.getManager().inSelectState())
                activity.onBackPressed()
        }
        activity.moreOptions.setOnClickListener {
            if(!activity.getManager().inSelectState())
                activity.getDialogs().showMoreOptionsDialog()
        }
        activity.listActionBar.setOnClickListener {
            if(!activity.getManager().inSelectState())
                activity.getDialogs().showNameListDialog()
        }
        activity.addButton.setOnClickListener {
            if(!activity.getManager().inSelectState())
                activity.getDialogs().showAddTaskDialog()
        }
    }

    private fun initTaskSelectActionBarOnClickListeners() {
        activity.closeTaskSelect.setOnClickListener {
            if(activity.getManager().inSelectState())
                activity.getView().setToDefaultState()
        }
        activity.addDate.setOnClickListener {
            if(activity.getManager().inSelectState())
                activity.getDialogs().showChooseCalendarMethodDialog(
                    activity.supportFragmentManager)
        }
        activity.selectAll.setOnClickListener {
            if(activity.getManager().inSelectState())
                activity.getView().selectAll()
        }
        activity.selectedMoreOptions.setOnClickListener {
            if(activity.getManager().inSelectState())
                activity.getDialogs().showSelectedMoreOptionsDialog()
        }
    }

}