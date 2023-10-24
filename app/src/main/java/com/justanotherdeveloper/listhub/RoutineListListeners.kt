package com.justanotherdeveloper.listhub

import android.view.MotionEvent
import androidx.core.view.size
import kotlinx.android.synthetic.main.activity_routine_list.*
import kotlinx.android.synthetic.main.activity_routine_list.addButton
import kotlinx.android.synthetic.main.activity_routine_list.backArrow
import kotlinx.android.synthetic.main.activity_routine_list.closeTaskSelect
import kotlinx.android.synthetic.main.activity_routine_list.completedTaskContainer
import kotlinx.android.synthetic.main.activity_routine_list.completedTitleBar
import kotlinx.android.synthetic.main.activity_routine_list.listActionBar
import kotlinx.android.synthetic.main.activity_routine_list.moreOptions
import kotlinx.android.synthetic.main.activity_routine_list.selectAll
import kotlinx.android.synthetic.main.activity_routine_list.selectedMoreOptions
import kotlinx.android.synthetic.main.activity_routine_list.sortedByExitButton
import kotlinx.android.synthetic.main.activity_routine_list.sortedByTitleBar
import kotlinx.android.synthetic.main.activity_routine_list.tasksScrollView
import kotlinx.android.synthetic.main.activity_routine_list.tasksScrollViewContents

class RoutineListListeners(private val activity: RoutineListActivity) {

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
        initButtonAnimationListener(activity.resetList)
        initButtonAnimationListener(activity.moreOptions)
        initButtonAnimationListener(activity.listActionBar)
        initButtonAnimationListener(activity.addButton)
    }

    private fun initTaskSelectActionBarAnimationListeners() {
        initButtonAnimationListener(activity.closeTaskSelect)
        initButtonAnimationListener(activity.selectAll)
        initButtonAnimationListener(activity.selectedMoreOptions)
    }

    private fun initOnClickListeners() {
        initListActionBarOnClickListeners()
        initTaskSelectActionBarOnClickListeners()

        activity.toggleNumbersOption.setOnClickListener {
            val list = activity.getList()
            list.toggleRoutineNumbersVisibility()
            activity.getView().toggleRoutineNumbers(list.routineNumbersShown())
            activity.updateRoutineList()
        }

        activity.completedTitleBar.setOnClickListener {
            activity.getView().toggleCompletedTaskContainer()
            activity.getList().toggleCompletedVisibility()
            activity.updateRoutineList()
        }

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
        activity.resetList.setOnClickListener {
            if(!activity.getManager().inSelectState()
                && activity.completedTaskContainer.size > 0)
                activity.getManager().resetList()
        }
        activity.moreOptions.setOnClickListener {
            if(!activity.getManager().inSelectState())
                activity.getDialogs().showMoreOptionsDialog()
        }
        activity.listActionBar.setOnClickListener {
            if(!activity.getManager().inSelectState())
                activity.getDialogs().showNameRoutineListDialog()
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