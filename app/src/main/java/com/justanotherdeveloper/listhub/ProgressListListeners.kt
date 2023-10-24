package com.justanotherdeveloper.listhub

import android.view.MotionEvent
import androidx.core.view.size
import kotlinx.android.synthetic.main.activity_progress_list.*

class ProgressListListeners(private val activity: ProgressListActivity) {

    init {
        initButtonAnimationListeners()
        initOnClickListeners()
        initScrollViewListeners()
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
        initButtonAnimationListener(activity.closeReorderSections)
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

    private fun initOnClickListeners() {
        initListActionBarOnClickListeners()
        initTaskSelectActionBarOnClickListeners()
        initTitleBarOnClickListeners()

        activity.addSectionOption.setOnClickListener {
            activity.getDialogs().showAddSectionDialog()
        }
    }

    private fun initTitleBarOnClickListeners() {
        activity.inProgressTaskTitleBar.setOnClickListener {
            if(activity.inProgressTaskContainer.size > 0
                && !activity.getManager().inReorderSectionState())
                activity.getView().toggleInProgressVisibility()
        }

        activity.completedTitleBar.setOnClickListener {
            if(activity.completedTaskContainer.size > 0
                && !activity.getManager().inReorderSectionState())
                activity.getView().toggleCompletedVisibility()
        }

        activity.sortedByTitleBar.setOnClickListener {
            activity.getView().toggleSortedOrder()
        }

        activity.sortedByExitButton.setOnClickListener {
            activity.getManager().sortList()
        }

        activity.inProgressEditSectionButton.setOnClickListener {
            if(!activity.getManager().inSelectState())
                activity.getDialogs().showEditSectionDialog(
                    activity.getList().getInProgressTitle())
        }

        activity.inProgressSelectAllButton.setOnClickListener {
            if(activity.inProgressTaskContainer.size > 0) {
                if(!activity.getList().inProgressShown())
                    activity.getView().toggleInProgressVisibility()
                if(!activity.getManager().inSelectState())
                    activity.getView().setToTaskSelectState()
                for (taskId in activity.getList().getCurrentTasks()) {
                    val taskView = activity.getView().getTaskView(taskId)
                    val task = activity.getList().getTask(taskId)
                    if(task != null) activity.getView().selectTask(
                        taskView, task, true)
                }
            }
        }

        activity.inProgressAddTaskButton.setOnClickListener {
            if(!activity.getManager().inSelectState())
                activity.getDialogs().showAddTaskDialog(
                    selectedSection = activity.getList().getInProgressTitle())
        }

        activity.completedEditSectionButton.setOnClickListener {
            if(!activity.getManager().inSelectState())
                activity.getDialogs().showEditSectionDialog(
                    activity.getList().getCompletedTitle())
        }

        activity.completedSelectAllButton.setOnClickListener {
            if(activity.completedTaskContainer.size > 0) {
                if(!activity.getList().completedShown())
                    activity.getView().toggleCompletedVisibility()
                if(!activity.getManager().inSelectState())
                    activity.getView().setToTaskSelectState()
                for (taskId in activity.getList().getCompletedTasks()) {
                    val taskView = activity.getView().getTaskView(taskId)
                    val task = activity.getList().getTask(taskId)
                    if(task != null) activity.getView().selectTask(
                        taskView, task, true)
                }
            }
        }

        activity.completedAddTaskButton.setOnClickListener {
            if(!activity.getManager().inSelectState())
                activity.getDialogs().showAddTaskDialog(
                    selectedSection = activity.getList().getCompletedTitle())
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
        activity.closeReorderSections.setOnClickListener {
            if(activity.getManager().inReorderSectionState())
                activity.getView().exitReorderSectionsState()
        }
    }

}