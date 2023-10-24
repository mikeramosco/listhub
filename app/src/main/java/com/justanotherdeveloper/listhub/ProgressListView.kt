package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.util.set
import androidx.core.view.get
import androidx.core.view.isEmpty
import androidx.core.view.iterator
import androidx.core.view.size
import kotlinx.android.synthetic.main.activity_progress_list.*

@SuppressLint("InflateParams")
class ProgressListView(private val activity: ProgressListActivity) {

    private val taskViewMap = SparseArray<View>()
    private val taskViewIsPressedMap = HashMap<View, Boolean>()
    private val taskViewIsHighlightedMap = HashMap<View, Boolean>()

    fun setTitle(listTitle: String) {
        activity.listTitleText.text = listTitle
    }

    fun getTaskView(taskId: Int): View {
        return taskViewMap[taskId]
    }

    fun toggleInProgressVisibility(animate: Boolean = true) {
        toggleTaskContainer(activity.inProgressTaskContainer,
            activity.inProgressTaskArrowIcon, animate = animate)
        activity.getList().toggleInProgressVisibility()
        activity.updateProgressList()
    }

    fun toggleCompletedVisibility(animate: Boolean = true) {
        toggleTaskContainer(activity.completedTaskContainer,
            activity.completedArrowIcon, animate = animate)
        activity.getList().toggleCompletedVisibility()
        activity.updateProgressList()
    }

    private fun toggleListSectionVisibility(taskContainer: LinearLayout, arrowIcon: ImageView,
                                            index: Int, animate: Boolean = true) {
        toggleTaskContainer(taskContainer, arrowIcon, animate = animate)
        activity.getList().toggleSectionVisibility(index)
        activity.updateProgressList()
    }

    private fun moveSectionUp(index: Int) {
        if(index == 0) return
        val list = activity.getList()
        list.moveSection(index, true)
        val view = activity.listSectionsContainer[index]
        beginTransition(activity.progressListParent)
        activity.listSectionsContainer.removeViewAt(index)
        activity.listSectionsContainer.addView(view, index-1)
        activity.updateProgressList()
    }

    private fun moveSectionDown(index: Int) {
        val list= activity.getList()
        if(index == list.getListSections().lastIndex) return
        list.moveSection(index, false)
        val view = activity.listSectionsContainer[index]
        beginTransition(activity.progressListParent)
        activity.listSectionsContainer.removeViewAt(index)
        activity.listSectionsContainer.addView(view, index+1)
        activity.updateProgressList()
    }

    fun addSectionView(colorTheme: Int, section: String, isNewSection: Boolean = false) {
        val view = activity.layoutInflater.inflate(
            R.layout.view_section_title_bar_and_container, null)

        val titleBar = view.findViewById<LinearLayout>(R.id.titleBar)
        val arrowIcon = view.findViewById<ImageView>(R.id.arrowIcon)
        val sectionTitleText = view.findViewById<TextView>(R.id.sectionTitleText)
        val taskCountText = view.findViewById<TextView>(R.id.taskCountText)
        val moveDownButton = view.findViewById<LinearLayout>(R.id.moveDownButton)
        val moveDownIcon = view.findViewById<ImageView>(R.id.moveDownIcon)
        val moveUpButton = view.findViewById<LinearLayout>(R.id.moveUpButton)
        val moveUpIcon = view.findViewById<ImageView>(R.id.moveUpIcon)
        val editSectionButton = view.findViewById<LinearLayout>(R.id.editSectionButton)
        val editSectionIcon = view.findViewById<ImageView>(R.id.editSectionIcon)
        val selectAllButton = view.findViewById<LinearLayout>(R.id.selectAllButton)
        val selectAllIcon = view.findViewById<ImageView>(R.id.selectAllIcon)
        val addTaskButton = view.findViewById<LinearLayout>(R.id.addTaskButton)
        val addTaskIcon = view.findViewById<ImageView>(R.id.addTaskIcon)
        val taskContainer = view.findViewById<LinearLayout>(R.id.taskContainer)

        val manager = activity.getManager()

        titleBar.setOnClickListener {
            val sectionTitle = sectionTitleText.text.toString()
            val index = activity.getList()
                .getListSectionIndex(sectionTitle)
            if(taskContainer.size > 0
                && !activity.getManager().inReorderSectionState())
                toggleListSectionVisibility(
                    taskContainer, arrowIcon, index)
        }

        sectionTitleText.setTextColor(colorTheme)
        taskCountText.setTextColor(colorTheme)
        arrowIcon.setColoredImageResource(R.drawable.ic_keyboard_arrow_right_custom, colorTheme)
        sectionTitleText.text = section
        taskCountText.text = "0"

        if(manager.inReorderSectionState()) {
            arrowIcon.visibility = View.GONE
            editSectionButton.visibility = View.GONE
            selectAllButton.visibility = View.GONE
            addTaskButton.visibility = View.GONE
            moveDownButton.visibility = View.VISIBLE
            moveUpButton.visibility = View.VISIBLE
        }

        moveDownIcon.setColoredImageResource(R.drawable.ic_arrow_drop_down_custom, colorTheme)
        moveDownButton.setOnClickListener {
            if(manager.inReorderSectionState()) {
                val sectionTitle = sectionTitleText.text.toString()
                val list = activity.getList()
                val index = list.getListSectionIndex(sectionTitle)
                moveSectionDown(index)
            }
        }

        moveUpIcon.setColoredImageResource(R.drawable.ic_arrow_drop_up_custom, colorTheme)
        moveUpButton.setOnClickListener {
            if(manager.inReorderSectionState()) {
                val sectionTitle = sectionTitleText.text.toString()
                val list = activity.getList()
                val index = list.getListSectionIndex(sectionTitle)
                moveSectionUp(index)
            }
        }

        editSectionIcon.setColoredImageResource(R.drawable.ic_edit_custom, colorTheme)
        editSectionButton.setOnClickListener {
            if(!manager.inSelectState())
                activity.getDialogs().showEditSectionDialog(
                    sectionTitleText.text.toString())
        }

        selectAllIcon.setColoredImageResource(R.drawable.ic_done_all_custom, colorTheme)
        selectAllButton.setOnClickListener {
            if (taskContainer.size > 0) {
                val sectionTitle = sectionTitleText.text.toString()
                val list = activity.getList()
                val index = list.getListSectionIndex(sectionTitle)
                if (!list.listSectionShown(index))
                    toggleListSectionVisibility(
                        taskContainer, arrowIcon, index
                    )
                if (!activity.getManager().inSelectState())
                    setToTaskSelectState()
                for (taskId in list.getListSections()[index]) {
                    val taskView = taskViewMap[taskId]
                    val task = list.getTask(taskId)
                    if(task != null) selectTask(taskView, task, true)
                }
            }
        }

        addTaskIcon.setColoredImageResource(R.drawable.ic_add_custom, colorTheme)
        addTaskButton.setOnClickListener {
            val sectionTitle = sectionTitleText.text.toString()
            if(!manager.inSelectState())
                activity.getDialogs().showAddTaskDialog(
                    selectedSection = sectionTitle)
        }

        if(isNewSection) {
            beginTransition(activity.progressListParent)
            activity.listSectionsContainer.addView(view, 0)
            taskContainer.visibility = View.GONE
        } else activity.listSectionsContainer.addView(view)
    }

    fun toggleMoveToSectionOptionsLayoutVisibility() {
        fun toggleLayoutVisibility(taskView: View) {
            val moveToSectionOptionsLayout = taskView
                .findViewById<LinearLayout>(R.id.moveToSectionOptionsLayout)
            moveToSectionOptionsLayout.visibility = if(activity.getManager().sectionsExist())
                View.VISIBLE else View.GONE
        }

        for(sectionView in activity.listSectionsContainer) {
            val taskContainer = sectionView
                .findViewById<LinearLayout>(R.id.taskContainer)
            for(taskView in taskContainer)
                toggleLayoutVisibility(taskView)
        }

        for(currentTaskView in activity.inProgressTaskContainer)
            toggleLayoutVisibility(currentTaskView)
    }

    fun initProgressListSections() {
        activity.listSectionsContainer.removeAllViews()
        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())
        for(section in activity.getList().getListSectionTitles())
            addSectionView(colorTheme, section)
        activity.inProgressTaskText.text = activity.getList().getInProgressTitle()
        activity.completedTaskText.text = activity.getList().getCompletedTitle()
    }

    fun sortList(sortIndex: Int, animate: Boolean = true) {
        if(animate) beginTransition(activity.progressListParent)
        if(sortIndex == 0) activity.sortedByTitleBar.visibility = View.GONE
        else {
            val color = activity.getColorTheme(activity.getList().getColorThemeIndex())
            if(sortIndex % 2 == 0)
                activity.sortedByArrowIcon.setColoredImageResource(
                    R.drawable.ic_keyboard_arrow_up_custom, color)
            else activity.sortedByArrowIcon.setColoredImageResource(
                R.drawable.ic_keyboard_arrow_down_custom, color)
            if(activity.sortedByTitleBar.visibility != View.VISIBLE)
                activity.sortedByTitleBar.visibility = View.VISIBLE
        }

        if(sortIndex > 0) activity.sortedByText.text = when(sortIndex) {
            sortDueDateDescendingIndex, sortDueDateAscendingIndex ->
                activity.getString(R.string.sortedByDueDateString)
            sortNewestFirstIndex, sortOldestFirstIndex ->
                activity.getString(R.string.sortedByCreationDateString)
            else -> activity.getString(R.string.sortedAlphabeticallyString)
        }

        moveTaskViewsToSortedOrder(sortIndex)
    }

    private fun moveTaskViewsToSortedOrder(sortIndex: Int) {
        val list = activity.getList()
        for((i, section) in list.getListSections().withIndex()) {
            val sectionView = activity.listSectionsContainer[i]
            val container = sectionView.findViewById<LinearLayout>(R.id.taskContainer)
            rearrangeContainerViews(sortIndex, section, container)
        }
        rearrangeContainerViews(sortIndex,
            list.getCurrentTasks(), activity.inProgressTaskContainer)
        rearrangeContainerViews(sortIndex,
            list.getCompletedTasks(), activity.completedTaskContainer)
    }

    private fun rearrangeContainerViews(sortIndex: Int,
                                        tasks: ArrayList<Int>,
                                        container: LinearLayout) {
        val sortedTaskOrder =
            activity.getManager().getSortedTaskOrder(sortIndex, tasks)
        container.removeAllViews()
        for(task in sortedTaskOrder)
            container.addView(taskViewMap[task.getTaskId()])
    }

    fun refreshListOrderIfSorted() {
        if(activity.getList().isSorted())
            sortList(activity.getList().getSortIndex(), false)
    }

    @Suppress("DEPRECATION")
    private fun initScrollViewItemAnimationListener(view: View, button: View = view) {
        val handler = Handler()
        var viewBounds = Rect()
        button.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    taskViewIsPressedMap[view] = true
                    viewBounds = Rect(v.left, v.top, v.right, v.bottom)
                    handler.postDelayed({
                        val taskViewIsPressed = taskViewIsPressedMap[view]
                        if(taskViewIsPressed != null && taskViewIsPressed) {
                            taskViewIsHighlightedMap[view] = true
                            animateButton(button, true)
                        }
                    }, TRANSITION_DELAY)
                }
                MotionEvent.ACTION_UP -> {
                    val taskViewIsPressed = taskViewIsPressedMap[view]
                    if(taskViewIsPressed != null && taskViewIsPressed &&
                        viewBounds.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                        taskViewIsHighlightedMap[view] = false
                        taskViewIsPressedMap[view] = false
                        animateButton(button, false)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val taskViewIsPressed = taskViewIsPressedMap[view]
                    if(taskViewIsPressed != null && taskViewIsPressed &&
                        !viewBounds.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                        taskViewIsPressedMap[view] = false

                        val taskViewIsHighlighted = taskViewIsHighlightedMap[view]
                        if(taskViewIsHighlighted != null && taskViewIsHighlighted) {
                            taskViewIsHighlightedMap[view] = false
                            animateButton(button, false)
                        }
                    }
                }
            }
            false
        }
    }

    fun updateTitleBars(container: LinearLayout, showSectionIfHidden: Boolean = true,
                        taskReloaded: Boolean = false, animate: Boolean = true) {
        if (container == activity.completedTaskContainer) {
            val size = activity.completedTaskContainer.size
            activity.completedCountText.text = size.toString()
            if(!taskReloaded) {
                if (size == 0 && activity.getList().completedShown())
                    toggleCompletedVisibility(animate = animate)
                else if (size > 0 && !activity.getList().completedShown()
                    && showSectionIfHidden)
                    toggleCompletedVisibility(animate = animate)
            }
            return
        }
        if (container == activity.inProgressTaskContainer) {
            val size = activity.inProgressTaskContainer.size
            activity.inProgressCountText.text = size.toString()
            if(!taskReloaded) {
                if (size == 0 && activity.getList().inProgressShown())
                    toggleInProgressVisibility(animate = animate)
                else if (size > 0 && !activity.getList().inProgressShown()
                    && showSectionIfHidden)
                    toggleInProgressVisibility(animate = animate)
            }
            return
        }
        for ((i, listSectionView) in activity
            .listSectionsContainer.iterator().withIndex()) {
            val taskContainer =
                listSectionView.findViewById<LinearLayout>(R.id.taskContainer)
            if (container == taskContainer) {
                val taskCountText = listSectionView
                    .findViewById<TextView>(R.id.taskCountText)
                val arrowIcon = listSectionView
                    .findViewById<ImageView>(R.id.arrowIcon)
                val size = taskContainer.size
                taskCountText.text = size.toString()
                if(!taskReloaded) {
                    if (size == 0 && activity.getList().listSectionShown(i))
                        toggleListSectionVisibility(taskContainer,
                            arrowIcon, i, animate = animate)
                    else if (size > 0 && !activity.getList().listSectionShown(i)
                        && showSectionIfHidden)
                        toggleListSectionVisibility(taskContainer,
                            arrowIcon, i, animate = animate)
                }
                break
            }
        }
    }

    fun getTaskContainer(task: ProgressTask): LinearLayout {
        return when {
            task.isInProgress() -> activity.inProgressTaskContainer
            task.isCompleted() -> activity.completedTaskContainer
            else -> {
                val sectionIndex = activity.getList().getListSectionIndex(task.getListSectionOfTask())
                val sectionView = activity.listSectionsContainer[sectionIndex]
                sectionView.findViewById(R.id.taskContainer)
            }
        }
    }

    private fun updateLayoutVisibilities(taskDetailsLayout: LinearLayout,
                                         divider1: TextView, divider2: TextView,
                                         divider3: TextView, dateShown: Boolean,
                                         noteShown: Boolean, rewardShown: Boolean,
                                         linkShown: Boolean) {
        taskDetailsLayout.visibility =
            if(dateShown || noteShown || rewardShown || linkShown)
                View.VISIBLE else View.GONE

        var div1IsVisible = false
        var div2IsVisible = false
        var div3IsVisible = false

        if(noteShown && dateShown)
            div1IsVisible = true
        if(rewardShown) {
            if (noteShown) div2IsVisible = true
            else if (dateShown && !noteShown)
                div2IsVisible = true
        }
        if(linkShown) {
            if (rewardShown) div3IsVisible = true
            else if (noteShown && !rewardShown)
                div3IsVisible = true
            else if (dateShown && !noteShown && !rewardShown)
                div3IsVisible = true
        }

        divider1.visibility = if(div1IsVisible) View.VISIBLE else View.GONE
        divider2.visibility = if(div2IsVisible) View.VISIBLE else View.GONE
        divider3.visibility = if(div3IsVisible) View.VISIBLE else View.GONE
    }

    fun addTask(task: ProgressTask, initialIndex: Int = SENTINEL, animate: Boolean = true,
                taskReloaded: Boolean = false, taskQuickAdded: Boolean = false) {
        activity.getManager().updateTasksIfDateOutdated()
        val view = activity.layoutInflater.inflate(R.layout.view_progress_task, null)

        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())

        val taskLayout = view.findViewById<LinearLayout>(R.id.taskLayout)
        val checkbox = view.findViewById<ImageView>(R.id.checkbox)
        val rewardTaskText = view.findViewById<TextView>(R.id.rewardTaskText)
        val taskText = view.findViewById<TextView>(R.id.taskText)
        val taskDetailsLayout = view.findViewById<LinearLayout>(R.id.taskDetailsLayout)
        val dateIcon = view.findViewById<ImageView>(R.id.dateIcon)
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val taskDetailsDivider1 = view.findViewById<TextView>(R.id.taskDetailsDivider1)
        val noteIcon = view.findViewById<ImageView>(R.id.noteIcon)
        val taskDetailsDivider2 = view.findViewById<TextView>(R.id.taskDetailsDivider2)
        val rewardIcon = view.findViewById<ImageView>(R.id.rewardIcon)
        val taskDetailsDivider3 = view.findViewById<TextView>(R.id.taskDetailsDivider3)
        val linkIcon = view.findViewById<ImageView>(R.id.linkIcon)
        val star = view.findViewById<ImageView>(R.id.star)

        if(!activity.getManager().sectionsExist()) view.findViewById<LinearLayout>(
            R.id.moveToSectionOptionsLayout).visibility = View.GONE

        val moveToNextSectionOption =
            view.findViewById<LinearLayout>(R.id.moveToNextSectionOption)
        val moveToOtherSectionOption =
            view.findViewById<LinearLayout>(R.id.moveToOtherSectionOption)

        if(task.isLinkedToList()) activity.getManager().removeLinkIfListDoesntExist(task)
        val showDate = task.hasDueDate()
        if(!showDate && task.hasTime()) {
            task.removeTime()
            activity.updateProgressList(false)
        }

        if(task.isReward()) {
            rewardTaskText.visibility = View.VISIBLE
            rewardTaskText.setTextColor(colorTheme)
        }

        taskText.text = task.getTask()
        if(showDate) activity.displayDate(dateIcon, dateText, task)
        if(task.hasNote()) noteIcon.visibility = View.VISIBLE
        if(task.hasReward()) rewardIcon.visibility = View.VISIBLE
        val showLink = task.isLinkedToList() || task.hasWebsiteLink()
        if(showLink) linkIcon.visibility = View.VISIBLE

        updateLayoutVisibilities(taskDetailsLayout,
            taskDetailsDivider1, taskDetailsDivider2,
            taskDetailsDivider3, showDate, task.hasNote(),
            task.hasReward(), showLink)

        moveToOtherSectionOption.setOnClickListener {
            when {
                activity.getManager().inSelectState() ->
                    activity.getDialogs().showSelectSectionDialog(moveSelectedTasks = true)
                else ->
                    activity.getDialogs().showSelectSectionDialog(task = task, taskView = view)
            }
        }

        moveToOtherSectionOption.setOnLongClickListener {
            if(!activity.getManager().inSelectState())
                setToTaskSelectState(view, task)
            else activity.getDialogs().showSelectedMoreOptionsDialog()
            true
        }

        moveToNextSectionOption.setOnClickListener {
            val nextSectionTitle = activity
                .getList().getNextListOfTaskTitle(task)
            when {
                activity.getManager().inSelectState() ->
                    activity.getManager().moveTasksToOtherSection(nextSectionTitle)
                else -> {
                    moveTaskToOtherSection(task,
                        task.getListSectionOfTask(), nextSectionTitle, view)
                    refreshListOrderIfSorted()
                    activity.updateProgressList()
                }
            }
        }

        moveToNextSectionOption.setOnLongClickListener {
            if(!activity.getManager().inSelectState())
                setToTaskSelectState(view, task)
            else activity.getDialogs().showSelectedMoreOptionsDialog()
            true
        }

        val taskTextSpacer = view.findViewById<View>(R.id.taskTextSpacer)
        taskTextSpacer.visibility = View.GONE

        checkbox.visibility = View.VISIBLE
        checkbox.setOnClickListener {
            when {
                activity.getManager().inSelectState() -> selectTask(view, task)
                task.isCompleted() -> {
                    val index = activity.getList().setTaskForOtherSection(
                        task, activity.getList().getInProgressTitle())
                    removeFromCompleted(index, view)
                    refreshListOrderIfSorted()
                    activity.updateProgressList()
                }
                else -> {
                    moveTaskToOtherSection(task,
                        task.getListSectionOfTask(),
                        activity.getList().getCompletedTitle(), view)
                    refreshListOrderIfSorted()
                    activity.updateProgressList()
                }
            }
        }

        checkbox.setOnLongClickListener {
            if(!activity.getManager().inSelectState())
                setToTaskSelectState(view, task)
            else activity.getDialogs().showSelectedMoreOptionsDialog()
            true
        }

        fun toggleTaskStar(index: Int = 0) {
            fun updateViewPlacement(taskContainer: LinearLayout) {
                val currentIndex = taskContainer.indexOfChild(view)
                if (index != currentIndex) {
                    beginTransition(activity.progressListParent)
                    taskContainer.removeView(view)
                    taskContainer.addView(view, index)
                }
            }

            if(!activity.getList().isSorted()) {
                when {
                    task.isCompleted() -> updateViewPlacement(activity.completedTaskContainer)
                    task.isInProgress() -> updateViewPlacement(activity.inProgressTaskContainer)
                    else -> {
                        val listSectionOfTask = task.getListSectionOfTask()
                        val indexOfListSection = activity.getList()
                            .getListSectionTitles().indexOf(listSectionOfTask)
                        val listSectionView = activity
                            .listSectionsContainer[indexOfListSection]
                        val taskContainer = listSectionView
                            .findViewById<LinearLayout>(R.id.taskContainer)
                        updateViewPlacement(taskContainer)
                    }
                }
            }

            if(task.isStarred())
                star.setColoredImageResource(R.drawable.ic_star_custom, colorTheme)
            else star.setImageResource(R.drawable.ic_star_border_gray)
        }

        star.setOnClickListener {
            when {
                activity.getManager().inSelectState() -> selectTask(view, task)
                task.isStarred() -> {
                    val index = activity.getList().removeTaskFromStarred(task)
                    toggleTaskStar(index)
                    activity.updateProgressList()
                }
                else -> {
                    activity.getList().addTaskToStarred(task)
                    toggleTaskStar()
                    activity.updateProgressList()
                }
            }
        }

        star.setOnLongClickListener {
            if(!activity.getManager().inSelectState())
                setToTaskSelectState(view, task)
            else activity.getDialogs().showSelectedMoreOptionsDialog()
            true
        }

        if(task.isStarred()) star.setColoredImageResource(R.drawable.ic_star_custom, colorTheme)

        taskViewIsPressedMap[view] = false
        taskViewIsHighlightedMap[view] = false
        initScrollViewItemAnimationListener(view, taskLayout)
        taskLayout.setOnClickListener {
            if(activity.getManager().inSelectState())
                selectTask(view, task)
            else activity.getDialogs().showAddTaskDialog(task, view)
        }

        taskLayout.setOnLongClickListener {
            if(!activity.getManager().inSelectState())
                setToTaskSelectState(view, task)
            else activity.getDialogs().showSelectedMoreOptionsDialog()
            true
        }

        if(!task.isCompleted() && animate)
            beginTransition(activity.progressListParent)
        when {
            task.isCompleted() -> {
                val addedTaskIsRepeating = task.isRepeating() &&
                        !task.alreadyRepeated() && !taskReloaded
                val addedTaskHasReward = task.hasReward() &&
                        !task.rewardAlreadyAdded() && !taskReloaded
                moveToCompleted(initialIndex, task, view,
                    alreadyInList = false, taskReloaded = taskReloaded,
                    taskIsRepeating = addedTaskIsRepeating,
                    taskHasReward = addedTaskHasReward)
            }
            task.isInProgress() -> {
                if(initialIndex != SENTINEL)
                    activity.inProgressTaskContainer.addView(view, initialIndex)
                else activity.inProgressTaskContainer.addView(view)
                updateTitleBars(activity.inProgressTaskContainer,
                    taskReloaded = taskReloaded, animate = !taskQuickAdded)
            }
            else ->{
                val listSectionOfTask = task.getListSectionOfTask()
                val indexOfListSection = activity.getList()
                    .getListSectionTitles().indexOf(listSectionOfTask)
                val listSectionView = activity.listSectionsContainer[indexOfListSection]
                val taskContainer = listSectionView.findViewById<LinearLayout>(R.id.taskContainer)
                if(initialIndex != SENTINEL) taskContainer.addView(view, initialIndex)
                else taskContainer.addView(view)
                updateTitleBars(taskContainer, taskReloaded = taskReloaded)
            }
        }

        taskViewMap[task.getTaskId()] = view
        if(!taskReloaded) refreshListOrderIfSorted()
    }

    private fun addRewardTask(task: ProgressTask) {
        task.setRewardAdded()
        val list = activity.getList()
        val rewardTask = ProgressTask(list.getListId())
        rewardTask.setTaskId(list.addNewTaskId())
        rewardTask.setTask(task.getReward())
        rewardTask.setTaskForReward(task.getTask())
        rewardTask.setNote(activity.getString(
            R.string.taskForRewardNote, task.getTask()))
        activity.getManager().setListSection(
            rewardTask, list.getInProgressTitle())
        val index = list.add(rewardTask)
        addTask(rewardTask, index, animate = false, taskQuickAdded = true)
    }

    fun copyTask(task: ProgressTask, taskIsRepeating: Boolean = true) {
        if(taskIsRepeating) task.setRepeated()
        val list = activity.getList()
        val taskId = list.addNewTaskId()
        val taskCopy = ProgressTask(task, taskId)
        if(taskIsRepeating) taskCopy.setAsRepeat()
        val sectionToCopyTo = if(taskIsRepeating)
            list.getInProgressTitle()
        else task.getListSectionOfTask()
        activity.getManager().setListSection(taskCopy, sectionToCopyTo)
        val taskCopyIndex = list.add(taskCopy)
        addTask(taskCopy, taskCopyIndex, animate = false, taskQuickAdded = true)
    }

    private fun removeFromCompleted(index: Int, view: View,
                                    animate: Boolean = true,
                                    taskContainer: LinearLayout =
                                        activity.inProgressTaskContainer) {
        val taskText = view.findViewById<TextView>(R.id.taskText)
        val checkbox = view.findViewById<ImageView>(R.id.checkbox)
        val moveToSectionOptionsLayout = view
            .findViewById<LinearLayout>(R.id.moveToSectionOptionsLayout)

        checkbox.setImageResource(R.drawable.ic_radio_button_unchecked_gray)
        if(activity.getManager().sectionsExist())
            moveToSectionOptionsLayout.visibility = View.INVISIBLE
        taskText.visibility = View.INVISIBLE

        if(animate) beginTransition(activity.progressListParent)
        if(activity.getManager().sectionsExist())
            moveToSectionOptionsLayout.visibility = View.VISIBLE
        taskText.visibility = View.VISIBLE
        taskText.setTextColor(Color.WHITE)
        taskText.paintFlags = 0
        activity.completedTaskContainer.removeView(view)
        val containerWasEmpty = taskContainer.isEmpty()
        taskContainer.addView(view, index)
        updateTitleBars(activity.completedTaskContainer, animate = animate)
        updateTitleBars(taskContainer, showSectionIfHidden = containerWasEmpty,
            animate = false)
    }

    private fun moveToSection(index: Int, view: View,
                              fromContainer: LinearLayout,
                              toContainer: LinearLayout,
                              animate: Boolean = true) {
        if(animate) beginTransition(activity.progressListParent)
        val containerWasEmpty = toContainer.isEmpty()
        fromContainer.removeView(view)
        toContainer.addView(view, index)
        updateTitleBars(fromContainer, animate = animate)
        updateTitleBars(toContainer, showSectionIfHidden = containerWasEmpty,
            animate = false)
    }

    private fun moveToCompleted(index: Int, task: ProgressTask,
                                view: View, alreadyInList: Boolean = true,
                                taskContainer: LinearLayout? = null,
                                taskReloaded: Boolean = false,
                                taskIsRepeating: Boolean = false,
                                taskHasReward: Boolean = false,
                                animate: Boolean = true) {
        val taskText = view.findViewById<TextView>(R.id.taskText)
        val checkbox = view.findViewById<ImageView>(R.id.checkbox)
        val moveToSectionOptionsLayout = view
            .findViewById<LinearLayout>(R.id.moveToSectionOptionsLayout)

        taskText.visibility = View.INVISIBLE
        moveToSectionOptionsLayout.visibility = View.GONE

        if (!taskReloaded && animate)
            beginTransition(activity.progressListParent)
        taskText.visibility = View.VISIBLE
        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())
        if (activity.completedTitleBar.visibility != View.VISIBLE)
            activity.completedTitleBar.visibility = View.VISIBLE
        taskText.setTextColor(
            ContextCompat.getColor(
                activity, R.color.colorLightGray
            )
        )
        taskText.apply {
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            text = task.getTask()
        }
        checkbox.setColoredImageResource(R.drawable.ic_check_circle_custom, colorTheme)
        if (alreadyInList) taskContainer?.removeView(view)
        val containerWasEmpty = activity.completedTaskContainer.isEmpty()
        if (index != SENTINEL) activity.completedTaskContainer.addView(view, index)
        else activity.completedTaskContainer.addView(view)
        updateTitleBars(activity.completedTaskContainer,
            showSectionIfHidden = !alreadyInList || containerWasEmpty,
            taskReloaded = taskReloaded,
            animate = !taskIsRepeating && animate)
        if(taskContainer != null) updateTitleBars(taskContainer,
            taskReloaded, animate = false)
        checkbox.visibility = View.VISIBLE
        moveToSectionOptionsLayout.visibility = View.GONE
        if(taskIsRepeating) copyTask(task)
        if(taskHasReward) addRewardTask(task)
    }

    fun changeTheme(colorThemeIndex: Int = 0) {
        val color = activity.getColorTheme(colorThemeIndex)
        activity.getList().setColorThemeIndex(colorThemeIndex)

        // Change Activity Icons
        activity.backArrowIcon.setColoredImageResource(
            R.drawable.ic_arrow_back_custom, color)
        activity.moreOptionsIcon.setColoredImageResource(
            R.drawable.ic_more_vert_custom, color)

        // Change Title Bar Colors
        activity.addSectionIcon.setColoredImageResource(R.drawable.ic_add_custom, color)
        activity.addSectionText.setTextColor(color)

        val inProgressArrowIconCode =
            if(activity.inProgressTaskContainer.visibility == View.VISIBLE)
                R.drawable.ic_keyboard_arrow_down_custom else
                R.drawable.ic_keyboard_arrow_right_custom
        activity.inProgressTaskArrowIcon.setColoredImageResource(
            inProgressArrowIconCode, color)
        activity.inProgressCountText.setTextColor(color)
        activity.inProgressTaskText.setTextColor(color)
        activity.inProgressEditSectionIcon.setColoredImageResource(
            R.drawable.ic_edit_custom, color)
        activity.inProgressSelectAllIcon.setColoredImageResource(
            R.drawable.ic_done_all_custom, color)
        activity.inProgressAddTaskIcon.setColoredImageResource(
            R.drawable.ic_add_custom, color)

        val completedArrowIconCode =
            if(activity.completedTaskContainer.visibility == View.VISIBLE)
                R.drawable.ic_keyboard_arrow_down_custom else
                R.drawable.ic_keyboard_arrow_right_custom
        activity.completedArrowIcon.setColoredImageResource(
            completedArrowIconCode, color)
        activity.completedCountText.setTextColor(color)
        activity.completedTaskText.setTextColor(color)
        activity.completedEditSectionIcon.setColoredImageResource(
            R.drawable.ic_edit_custom, color)
        activity.completedSelectAllIcon.setColoredImageResource(
            R.drawable.ic_done_all_custom, color)
        activity.completedAddTaskIcon.setColoredImageResource(
            R.drawable.ic_add_custom, color)

        for(view in activity.listSectionsContainer) {
            val arrowIcon = view.findViewById<ImageView>(R.id.arrowIcon)
            val sectionTitleText = view.findViewById<TextView>(R.id.sectionTitleText)
            val taskCountText = view.findViewById<TextView>(R.id.taskCountText)
            val moveDownIcon = view.findViewById<ImageView>(R.id.moveDownIcon)
            val moveUpIcon = view.findViewById<ImageView>(R.id.moveUpIcon)
            val editSectionIcon = view.findViewById<ImageView>(R.id.editSectionIcon)
            val selectAllIcon = view.findViewById<ImageView>(R.id.selectAllIcon)
            val addTaskIcon = view.findViewById<ImageView>(R.id.addTaskIcon)
            val taskContainer = view.findViewById<LinearLayout>(R.id.taskContainer)

            val arrowIconCode =
                if(taskContainer.visibility == View.VISIBLE)
                    R.drawable.ic_keyboard_arrow_down_custom else
                    R.drawable.ic_keyboard_arrow_right_custom
            arrowIcon.setColoredImageResource(arrowIconCode, color)
            taskCountText.setTextColor(color)
            sectionTitleText.setTextColor(color)
            moveDownIcon.setColoredImageResource(R.drawable.ic_arrow_drop_down_custom, color)
            moveUpIcon.setColoredImageResource(R.drawable.ic_arrow_drop_up_custom, color)
            editSectionIcon.setColoredImageResource(R.drawable.ic_edit_custom, color)
            selectAllIcon.setColoredImageResource(R.drawable.ic_done_all_custom, color)
            addTaskIcon.setColoredImageResource(R.drawable.ic_add_custom, color)
        }

        activity.sortedByExitIcon.setColoredImageResource(
            R.drawable.ic_close_custom, color)
        activity.sortedByText.setTextColor(color)
        if(activity.getList().getSortIndex() % 2 == 0)
            activity.sortedByArrowIcon.setColoredImageResource(
                R.drawable.ic_keyboard_arrow_up_custom, color)
        else activity.sortedByArrowIcon.setColoredImageResource(
            R.drawable.ic_keyboard_arrow_down_custom, color)

        // Change Activity Title
        activity.listTitleText.setTextColor(color)

        // Change Add Button Background
        activity.addButtonBackground.setColoredImageResource(
            R.drawable.ic_circle_filled_custom, color)

        // Change Existing Completed Checkboxes
        for(view in activity.completedTaskContainer) {
            val markedCheckbox = view.findViewById<ImageView>(R.id.checkbox)
            markedCheckbox.setColoredImageResource(R.drawable.ic_check_circle_custom, color)
        }

        // Change Existing Starred Icons
        fun updateViewColors(taskId: Int) {
            val task = activity.getList().getTask(taskId)?: return
            val view = taskViewMap[task.getTaskId()]
            if(task.isStarred()) {
                val star = view.findViewById<ImageView>(R.id.star)
                star.setColoredImageResource(R.drawable.ic_star_custom, color)
            }
            if(task.isReward()) {
                val rewardTaskText = view.findViewById<TextView>(R.id.rewardTaskText)
                rewardTaskText.setTextColor(color)
            }
        }

        for(section in activity.getList().getListSections())
            for(taskId in section) updateViewColors(taskId)

        for(currentTaskId in activity.getList().getCurrentTasks())
            updateViewColors(currentTaskId)

        for(completedTaskId in activity.getList().getCompletedTasks())
            updateViewColors(completedTaskId)
    }

    fun displayNote(animate: Boolean = true) {
        if(animate) beginTransition(activity.progressListParent)
        activity.noteIcon.visibility =
            if(activity.getList().hasNote())
                View.VISIBLE else View.GONE

        updateListDetailsLayoutVisibilities()
    }

    fun displayLabels(labelsString: String, animate: Boolean = true) {
        if(animate) beginTransition(activity.progressListParent)
        if(labelsString.isNotEmpty()) {
            activity.labelIcon.visibility = View.VISIBLE
            activity.labelText.visibility = View.VISIBLE
            activity.labelText.text = labelsString
        } else {
            activity.labelIcon.visibility = View.GONE
            activity.labelText.visibility = View.GONE
        }

        updateListDetailsLayoutVisibilities()
    }

    private fun updateListDetailsLayoutVisibilities() {
        val list = activity.getList()
        val hasLabels = list.hasLabels()
        val hasNote = list.hasNote()

        activity.listDetailsLayout.visibility =
            if(hasLabels || hasNote)
                View.VISIBLE else View.GONE

        activity.listDetailsDivider.visibility =
            if(hasLabels && hasNote)
                View.VISIBLE else View.GONE
    }

    fun toggleListSectionsVisibility() {
        for((i, sectionShown) in activity.getList().getListSectionsVisibility().withIndex()) {
            val view = activity.listSectionsContainer[i]

            val taskContainer = view.findViewById<LinearLayout>(R.id.taskContainer)
            val arrowIcon = view.findViewById<ImageView>(R.id.arrowIcon)
            toggleTaskContainer(taskContainer, arrowIcon, animate = false, showOnly = true)
            if(!sectionShown) toggleTaskContainer(taskContainer, arrowIcon, animate = false)
        }

        toggleTaskContainer(activity.completedTaskContainer,
            activity.completedArrowIcon, animate = false, showOnly = true)
        if(!activity.getList().completedShown())
            toggleTaskContainer(activity.completedTaskContainer,
                activity.completedArrowIcon, animate = false)

        toggleTaskContainer(activity.inProgressTaskContainer,
            activity.inProgressTaskArrowIcon, animate = false, showOnly = true)
        if(!activity.getList().inProgressShown())
            toggleTaskContainer(activity.inProgressTaskContainer,
                activity.inProgressTaskArrowIcon, animate = false)
    }

    fun reloadProgressList(reloadAll: Boolean = true) {
        val list = activity.getList()
        if(reloadAll) {
            activity.listTitleText.text = list.getTitle()
            displayLabels(list.getLabelsString(), animate = false)
            displayNote(animate = false)
            initProgressListSections()
        }
        taskViewMap.clear()
        taskViewIsPressedMap.clear()
        taskViewIsHighlightedMap.clear()
        activity.tasksScrollViewContents.visibility = View.INVISIBLE
        for((i, section) in activity.listSectionsContainer.iterator().withIndex()) {
            val taskContainer =
                section.findViewById<LinearLayout>(R.id.taskContainer)
            taskContainer.removeAllViews()
            val sectionTasks =
                activity.getList().getListSectionTasks(i)
            for(taskId in sectionTasks) {
                val task = list.getTask(taskId)
                if(task != null) addTask(task, animate = false, taskReloaded = true)
            }
        }
        activity.inProgressTaskContainer.removeAllViews()
        activity.completedTaskContainer.removeAllViews()
        for(taskId in activity.getList().getCurrentTasks()) {
            val task = list.getTask(taskId)
            if(task != null) addTask(task, animate = false, taskReloaded = true)
        }
        for(taskId in activity.getList().getCompletedTasks()) {
            val task = list.getTask(taskId)
            if(task != null) addTask(task, animate = false, taskReloaded = true)
        }
        refreshListOrderIfSorted()
        if(reloadAll) {
            changeTheme(list.getColorThemeIndex())
            toggleListSectionsVisibility()
        }
        activity.getManager().checkForTaskToOpen()
        activity.tasksScrollView.post {
            activity.tasksScrollView.scrollY = activity.getList().getScrollState()
            activity.tasksScrollViewContents.visibility = View.VISIBLE
        }
    }

    fun moveTaskToOtherSection(task: ProgressTask, currentSection: String,
                               newSection: String, view: View, animate: Boolean = true) {
        val list = activity.getList()
        val completedTitle = list.getCompletedTitle()
        when {
            newSection == completedTitle -> {
                val currentContainer = getTaskContainer(task)
                val index = list.setTaskForOtherSection(task, completedTitle)
                val taskIsRepeating = task.isRepeating() && !task.alreadyRepeated()
                val taskHasReward = task.hasReward() && !task.rewardAlreadyAdded()
                moveToCompleted(index, task, view, taskContainer = currentContainer,
                    taskIsRepeating = taskIsRepeating, taskHasReward = taskHasReward,
                    animate = animate)
            }
            currentSection == completedTitle -> {
                val index = list.setTaskForOtherSection(task, newSection)
                val nextContainer = getTaskContainer(task)
                removeFromCompleted(index, view, animate = animate,
                    taskContainer = nextContainer)
            }
            else -> {
                val currentContainer = getTaskContainer(task)
                val index = list.setTaskForOtherSection(task, newSection)
                val nextContainer = getTaskContainer(task)
                moveToSection(index, view, currentContainer, nextContainer,
                    animate = animate)
            }
        }
    }

    fun editTask(task: ProgressTask, view: View,
                 newSection: String = task.getListSectionOfTask()) {
        activity.getManager().updateTasksIfDateOutdated()
        val taskText = view.findViewById<TextView>(R.id.taskText)
        val taskDetailsLayout = view.findViewById<LinearLayout>(R.id.taskDetailsLayout)
        val dateIcon = view.findViewById<ImageView>(R.id.dateIcon)
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val taskDetailsDivider1 = view.findViewById<TextView>(R.id.taskDetailsDivider1)
        val noteIcon = view.findViewById<ImageView>(R.id.noteIcon)
        val taskDetailsDivider2 = view.findViewById<TextView>(R.id.taskDetailsDivider2)
        val rewardIcon = view.findViewById<ImageView>(R.id.rewardIcon)
        val taskDetailsDivider3 = view.findViewById<TextView>(R.id.taskDetailsDivider3)
        val linkIcon = view.findViewById<ImageView>(R.id.linkIcon)

        taskText.text = task.getTask()

        val currentSection = task.getListSectionOfTask()
        val moveTask = currentSection != newSection
        if(moveTask) moveTaskToOtherSection(task, currentSection, newSection, view)

        if(!moveTask) beginTransition(activity.progressListParent)
        taskText.text = task.getTask()
        val showDate = task.hasDueDate()
        if(showDate) activity.displayDate(dateIcon, dateText, task) else {
            dateIcon.visibility = View.GONE
            dateText.visibility = View.GONE
        }
        noteIcon.visibility =
            if(task.hasNote())
                View.VISIBLE else View.GONE
        rewardIcon.visibility =
            if(task.hasReward())
                View.VISIBLE else View.GONE
        val showLink = task.isLinkedToList() || task.hasWebsiteLink()
        linkIcon.visibility =
            if(showLink)
                View.VISIBLE else View.GONE

        updateLayoutVisibilities(taskDetailsLayout,
            taskDetailsDivider1, taskDetailsDivider2,
            taskDetailsDivider3, showDate, task.hasNote(),
            task.hasReward(), showLink)

        refreshListOrderIfSorted()
    }

    fun releaseTaskViews() {
        fun releaseTaskView(view: View) {
            val viewIsHighlighted = taskViewIsHighlightedMap[view]
            if(viewIsHighlighted != null && viewIsHighlighted) {
                taskViewIsHighlightedMap[view] = false
                val taskLayout = view.findViewById<LinearLayout>(R.id.taskLayout)
                animateButton(taskLayout, false)
            }

            val viewIsPressed = taskViewIsPressedMap[view]
            if(viewIsPressed != null && viewIsPressed)
                taskViewIsPressedMap[view] = false
        }

        for(sectionView in activity.listSectionsContainer) {
            val taskContainer = sectionView
                .findViewById<LinearLayout>(R.id.taskContainer)
            for(view in taskContainer) releaseTaskView(view)
        }
        for(view in activity.inProgressTaskContainer.iterator()) releaseTaskView(view)
        for(view in activity.completedTaskContainer.iterator()) releaseTaskView(view)
    }

    fun setToTaskSelectState(selectedView: View? = null, task: ProgressTask? = null) {
        if(activity.getManager().inSelectState()) return
        activity.getManager().setSelectState(true)

        beginTransition(activity.progressListParent)
        activity.addButtonLayout.visibility = View.GONE
        activity.listActionBar.visibility = View.GONE
        activity.taskSelectActionBar.visibility = View.VISIBLE
        toggleTitleBarOptionsVisibilityForTaskSelectState(false)

        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())

        fun setTaskViewToSelectState(taskView: View) {
            val checkbox = taskView.findViewById<ImageView>(R.id.checkbox)
//            val taskTextSpacer = taskView.findViewById<View>(R.id.taskTextSpacer)
//            checkbox.visibility = View.VISIBLE
//            taskTextSpacer.visibility = View.GONE
            checkbox.setColoredImageResource(R.drawable.ic_radio_button_unchecked_custom, colorTheme)
        }

        for(sectionView in activity.listSectionsContainer) {
            val taskContainer = sectionView
                .findViewById<LinearLayout>(R.id.taskContainer)
            for(taskView in taskContainer)
                setTaskViewToSelectState(taskView)
        }

        for(inProgressTaskView in activity.inProgressTaskContainer)
            setTaskViewToSelectState(inProgressTaskView)

        for(completedTaskView in activity.completedTaskContainer) {
            val checkbox = completedTaskView.findViewById<ImageView>(R.id.checkbox)
            checkbox.setColoredImageResource(R.drawable.ic_radio_button_unchecked_custom, colorTheme)
        }

        if(selectedView != null && task != null) selectTask(selectedView, task)
    }

    fun selectTask(selectedView: View, task: ProgressTask, selectOnly: Boolean = false) {

        if(selectOnly && activity.getManager().taskIsSelected(task)) return

        val isSelected = activity.getManager().toggleSelectedTask(task)
        val selectedCount = activity.getManager().getSelectedCount()
        activity.selectedCountText.text = selectedCount.toString()

        if(selectedCount == 0) {
            setToDefaultState()
            return
        }

        val checkbox = selectedView.findViewById<ImageView>(R.id.checkbox)
        val checkboxCode = if(isSelected) R.drawable.ic_radio_button_checked_custom
        else R.drawable.ic_radio_button_unchecked_custom
        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())
        checkbox.setColoredImageResource(checkboxCode, colorTheme)
        val taskLayout = selectedView.findViewById<LinearLayout>(R.id.taskLayout)
        val backgroundCode = if(isSelected) R.drawable.transition_layout_list_item_selected
        else R.drawable.transition_layout_list_item
        taskLayout.background = ContextCompat.getDrawable(activity, backgroundCode)
    }

    private fun toggleTaskContainer(container: LinearLayout, arrowIcon: ImageView,
                                    animate: Boolean = true, showOnly: Boolean = false) {
        if(animate) beginTransition(activity.progressListParent)
        val color = activity.getColorTheme(activity.getList().getColorThemeIndex())
        if(container.visibility != View.VISIBLE) {
            container.visibility = View.VISIBLE
            arrowIcon.setColoredImageResource(
                R.drawable.ic_keyboard_arrow_down_custom, color)
        } else if(!showOnly) {
            container.visibility = View.GONE
            arrowIcon.setColoredImageResource(
                R.drawable.ic_keyboard_arrow_right_custom, color)
        }
    }

    fun toggleSortedOrder() {
        val newSortIndex = when(activity.getList().getSortIndex()) {
            sortDueDateDescendingIndex -> sortDueDateAscendingIndex
            sortDueDateAscendingIndex -> sortDueDateDescendingIndex
            sortNewestFirstIndex -> sortOldestFirstIndex
            sortOldestFirstIndex -> sortNewestFirstIndex
            sortAToZIndex -> sortZToAIndex
            else -> sortAToZIndex
        }

        activity.getManager().sortList(newSortIndex)
    }

    private fun toggleTitleBarOptionsVisibilityForReorderSectionsState(showOptions: Boolean = true) {
        val visibility = if(showOptions) View.VISIBLE else View.GONE
        val reversed = if(!showOptions) View.VISIBLE else View.GONE
        for(sectionView in activity.listSectionsContainer) {
            sectionView.findViewById<ImageView>(R.id.arrowIcon).visibility = visibility
            sectionView.findViewById<LinearLayout>(R.id.editSectionButton).visibility = visibility
            sectionView.findViewById<LinearLayout>(R.id.selectAllButton).visibility = visibility
            sectionView.findViewById<LinearLayout>(R.id.addTaskButton).visibility = visibility
            sectionView.findViewById<LinearLayout>(R.id.moveUpButton).visibility = reversed
            sectionView.findViewById<LinearLayout>(R.id.moveDownButton).visibility = reversed
        }
        activity.inProgressTaskArrowIcon.visibility = visibility
        activity.inProgressEditSectionButton.visibility = visibility
        activity.inProgressSelectAllButton.visibility = visibility
        activity.inProgressAddTaskButton.visibility = visibility
        activity.inProgressLockedImage.visibility = reversed

        activity.completedArrowIcon.visibility = visibility
        activity.completedEditSectionButton.visibility = visibility
        activity.completedSelectAllButton.visibility = visibility
        activity.completedAddTaskButton.visibility = visibility
        activity.completedLockedImage.visibility = reversed
    }

    private fun toggleTitleBarOptionsVisibilityForTaskSelectState(showOptions: Boolean = true) {
        val visibility = if(showOptions) View.VISIBLE else View.GONE
        for(sectionView in activity.listSectionsContainer) {
            sectionView.findViewById<LinearLayout>(R.id.editSectionButton).visibility = visibility
            sectionView.findViewById<LinearLayout>(R.id.addTaskButton).visibility = visibility
        }
        activity.inProgressEditSectionButton.visibility = visibility
        activity.inProgressAddTaskButton.visibility = visibility
        activity.completedEditSectionButton.visibility = visibility
        activity.completedAddTaskButton.visibility = visibility
    }

    fun enterReorderSectionsState() {
        if(activity.getManager().inReorderSectionState()) return
        activity.getManager().setReorderSectionsState(true)

        beginTransition(activity.progressListParent)
        activity.addButtonLayout.visibility = View.GONE
        activity.listActionBar.visibility = View.GONE
        activity.reorderSectionsActionBar.visibility = View.VISIBLE
        toggleTitleBarOptionsVisibilityForReorderSectionsState(false)

        for((index, view) in activity.listSectionsContainer.iterator().withIndex()) {
            val arrowIcon = view.findViewById<ImageView>(R.id.arrowIcon)
            val taskContainer = view.findViewById<LinearLayout>(R.id.taskContainer)
            if(taskContainer.visibility == View.VISIBLE)
                toggleListSectionVisibility(taskContainer, arrowIcon, index)
        }

        if(activity.inProgressTaskContainer.visibility == View.VISIBLE)
            toggleInProgressVisibility()
        if(activity.completedTaskContainer.visibility == View.VISIBLE)
            toggleCompletedVisibility()
    }

    fun exitReorderSectionsState() {
        if(!activity.getManager().inReorderSectionState()) return
        activity.getManager().setReorderSectionsState(false)

        beginTransition(activity.progressListParent)
        activity.addButtonLayout.visibility = View.VISIBLE
        activity.listActionBar.visibility = View.VISIBLE
        activity.reorderSectionsActionBar.visibility = View.GONE
        toggleTitleBarOptionsVisibilityForReorderSectionsState(true)
    }

    fun setToDefaultState() {
        if(!activity.getManager().inSelectState()) return
        activity.getManager().setSelectState(false)

        beginTransition(activity.progressListParent)
        activity.addButtonLayout.visibility = View.VISIBLE
        activity.listActionBar.visibility = View.VISIBLE
        activity.taskSelectActionBar.visibility = View.GONE
        toggleTitleBarOptionsVisibilityForTaskSelectState()

        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())
        fun resetTaskView(taskIsCompleted: Boolean, view: View) {
            val taskLayout = view.findViewById<LinearLayout>(R.id.taskLayout)
            taskLayout.background = ContextCompat.getDrawable(
                activity, R.drawable.transition_layout_list_item)
            val checkbox = view.findViewById<ImageView>(R.id.checkbox)
            if(taskIsCompleted)
                checkbox.setColoredImageResource(R.drawable.ic_check_circle_custom, colorTheme)
            else checkbox.setImageResource(R.drawable.ic_radio_button_unchecked_gray)
        }

        for(sectionView in activity.listSectionsContainer) {
            val taskContainer = sectionView
                .findViewById<LinearLayout>(R.id.taskContainer)
            for(taskView in taskContainer)
                resetTaskView(false, taskView)
        }

        for(currentTaskView in activity.inProgressTaskContainer)
            resetTaskView(false, currentTaskView)

        for(completedTaskView in activity.completedTaskContainer)
            resetTaskView(true, completedTaskView)
    }

    fun selectAll() {
        if(!activity.getManager().inSelectState())
            setToTaskSelectState()

        val list = activity.getList()

        for((i, section) in list.getListSections().withIndex())
            if(list.listSectionShown(i)) {
                for (taskId in section) {
                    val taskView = taskViewMap[taskId]
                    val task = list.getTask(taskId)
                    if(task != null) selectTask(taskView, task, true)
                }
            }

        if(list.inProgressShown()) {
            for (taskId in list.getCurrentTasks()) {
                val taskView = taskViewMap[taskId]
                val task = list.getTask(taskId)
                if(task != null) selectTask(taskView, task, true)
            }
        }

        if(list.completedShown()) {
            for (taskId in list.getCompletedTasks()) {
                val taskView = taskViewMap[taskId]
                val task = list.getTask(taskId)
                if(task != null) selectTask(taskView, task, true)
            }
        }
    }
}