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
import androidx.core.view.iterator
import androidx.core.view.size
import kotlinx.android.synthetic.main.activity_to_do_list.*

@SuppressLint("InflateParams")
class ToDoListView(private val activity: ToDoListActivity) {

    private val taskViewMap = SparseArray<View>()
    private val taskViewIsPressedMap = HashMap<View, Boolean>()
    private val taskViewIsHighlightedMap = HashMap<View, Boolean>()

    fun setTitle(listTitle: String) {
        activity.listTitleText.text = listTitle
    }

    fun getTaskView(taskId: Int): View {
        return taskViewMap[taskId]
    }

    fun displayNote(animate: Boolean = true) {
        if(animate) beginTransition(activity.toDoListParent)
        activity.noteIcon.visibility =
            if(activity.getList().hasNote())
                View.VISIBLE else View.GONE

        updateListDetailsLayoutVisibilities()
    }

    fun displayLabels(labelsString: String, animate: Boolean = true) {
        if(animate) beginTransition(activity.toDoListParent)
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

        for(view in activity.taskContainer.iterator()) releaseTaskView(view)
        for(view in activity.completedTaskContainer.iterator()) releaseTaskView(view)
    }

    fun reloadToDoList(reloadAll: Boolean = true) {
        val list = activity.getList()
        if(reloadAll) {
            activity.listTitleText.text = list.getTitle()
            displayLabels(list.getLabelsString(), animate = false)
            displayNote(animate = false)
        }
        taskViewMap.clear()
        taskViewIsPressedMap.clear()
        taskViewIsHighlightedMap.clear()
        activity.taskContainer.removeAllViews()
        activity.completedTaskContainer.removeAllViews()
        activity.tasksScrollViewContents.visibility = View.INVISIBLE
        for(taskId in list.getCurrentTasks()) {
            val task = list.getTask(taskId)
            if (task != null) addTask(task, animate = false, taskReloaded = true)
        }
        for(taskId in list.getCompletedTasks()) {
            val task = list.getTask(taskId)
            if (task != null) addTask(task, animate = false, taskReloaded = true)
        }
        updateNoItemsMessageVisibility()
        refreshListOrderIfSorted()
        if(reloadAll) {
            changeTheme(list.getColorThemeIndex())
            showCompletedTaskContainer()
            if(!list.completedShown())
                toggleCompletedTaskContainer(false)
        }
        activity.getManager().checkForTaskToOpen()
        activity.tasksScrollView.post {
            activity.tasksScrollView.scrollY = list.getScrollState()
            activity.tasksScrollViewContents.visibility = View.VISIBLE
        }
    }

    fun refreshListOrderIfSorted() {
        if(activity.getList().isSorted())
            sortList(activity.getList().getSortIndex(), false)
    }

    fun sortList(sortIndex: Int, animate: Boolean = true) {
        if(animate) beginTransition(activity.toDoListParent)
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
        rearrangeContainerViews(sortIndex)
        rearrangeContainerViews(sortIndex, true)
    }

    private fun rearrangeContainerViews(sortIndex: Int, sortCompletedTasks: Boolean = false) {
        val sortedTaskOrder =
            activity.getManager().getSortedTaskOrder(sortIndex, sortCompletedTasks)
        val container = if(sortCompletedTasks)
            activity.completedTaskContainer else activity.taskContainer
        container.removeAllViews()
        for(task in sortedTaskOrder)
            container.addView(taskViewMap[task.getTaskId()])
    }

    private fun setToTaskSelectState(selectedView: View? = null, task: Task? = null) {
        if(activity.getManager().inSelectState()) return
        activity.getManager().setSelectState(true)

        beginTransition(activity.toDoListParent)
        activity.addButtonLayout.visibility = View.GONE
        activity.listActionBar.visibility = View.GONE
        activity.taskSelectActionBar.visibility = View.VISIBLE

        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())
        for(currentTaskView in activity.taskContainer) {
            val checkbox = currentTaskView.findViewById<ImageView>(R.id.checkbox)
            checkbox.setColoredImageResource(R.drawable.ic_radio_button_unchecked_custom, colorTheme)
        }

        for(completedTaskView in activity.completedTaskContainer) {
            val checkbox = completedTaskView.findViewById<ImageView>(R.id.checkbox)
            checkbox.setColoredImageResource(R.drawable.ic_radio_button_unchecked_custom, colorTheme)
        }

        if(selectedView != null && task != null) selectTask(selectedView, task)
    }

    fun setToDefaultState() {
        if(!activity.getManager().inSelectState()) return
        activity.getManager().setSelectState(false)

        beginTransition(activity.toDoListParent)
        activity.addButtonLayout.visibility = View.VISIBLE
        activity.listActionBar.visibility = View.VISIBLE
        activity.taskSelectActionBar.visibility = View.GONE

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

        for(currentTaskView in activity.taskContainer)
            resetTaskView(false, currentTaskView)

        for(completedTaskView in activity.completedTaskContainer)
            resetTaskView(true, completedTaskView)
    }

    fun selectAll() {
        if(!activity.getManager().inSelectState())
            setToTaskSelectState()

        for(taskId in activity.getList().getCurrentTasks()) {
            val taskView = taskViewMap[taskId]
            val task = activity.getList().getTask(taskId)
            if (task != null) selectTask(taskView, task, true)
        }

        if(activity.getList().completedShown()) {
            for (taskId in activity.getList().getCompletedTasks()) {
                val taskView = taskViewMap[taskId]
                val task = activity.getList().getTask(taskId)
                if (task != null) selectTask(taskView, task, true)
            }
        }
    }

    private fun selectTask(selectedView: View, task: Task, selectOnly: Boolean = false) {

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

    private fun removeFromCompleted(index: Int, taskText: TextView,
                                    checkbox: ImageView, view: View) {
        beginTransition(activity.toDoListParent)
        taskText.setTextColor(Color.WHITE)
        taskText.paintFlags = 0
        checkbox.setImageResource(R.drawable.ic_radio_button_unchecked_gray)
        activity.completedTaskContainer.removeView(view)
        activity.taskContainer.addView(view, index)
        updateCompletedTitleBar()
    }

    fun updateCompletedTitleBar() {
        val size = activity.completedTaskContainer.size
        activity.completedCountText.text = size.toString()
        if (size == 0) activity.completedTitleBar.visibility = View.GONE
        updateNoItemsMessageVisibility()
    }

    fun moveToCompleted(index: Int, task: Task, view: View,
                        alreadyInList: Boolean = true, animate: Boolean = true) {
        val taskText = view.findViewById<TextView>(R.id.taskText)
        val checkbox = view.findViewById<ImageView>(R.id.checkbox)

        if(animate) beginTransition(activity.toDoListParent)
        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())
        if(activity.completedTitleBar.visibility != View.VISIBLE)
            activity.completedTitleBar.visibility = View.VISIBLE
        taskText.setTextColor(ContextCompat.getColor(
            activity, R.color.colorLightGray))
        taskText.apply {
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            text = task.getTask()
        }
        checkbox.setColoredImageResource(R.drawable.ic_check_circle_custom, colorTheme)
        if(alreadyInList) activity.taskContainer.removeView(view)
        if(index != SENTINEL) activity.completedTaskContainer.addView(view, index)
        else activity.completedTaskContainer.addView(view)
        activity.completedCountText.text =
            activity.completedTaskContainer.size.toString()
    }

    fun editTask(task: Task, view: View) {
        activity.getManager().updateTasksIfDateOutdated()
        val checkbox = view.findViewById<ImageView>(R.id.checkbox)
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

        var taskMoved = false

        if(task.isCompleted()) {
            if(!activity.getList().taskIsCompleted(task)) {
                val index = activity.getList().markTaskCompleted(task)
                moveToCompleted(index, task, view)
                if(task.isRepeating() && !task.alreadyRepeated()) copyTask(task)
                if(task.hasReward() && !task.rewardAlreadyAdded()) addRewardTask(task)
                taskMoved = true
            }
        } else {
            if(activity.getList().taskIsCompleted(task)) {
                val index = activity.getList().removeTaskFromCompleted(task)
                removeFromCompleted(index, taskText, checkbox, view)
                taskMoved = true
            }
        }

        if(!taskMoved) beginTransition(activity.toDoListParent)
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

    private fun addRewardTask(task: Task) {
        task.setRewardAdded()
        val rewardTask = Task(activity.getList().getListId())
        rewardTask.setTaskId(activity.getList().addNewTaskId())
        rewardTask.setTask(task.getReward())
        rewardTask.setTaskForReward(task.getTask())
        rewardTask.setNote(activity.getString(
            R.string.taskForRewardNote, task.getTask()))
        val index = activity.getList().add(rewardTask)
        addTask(rewardTask, index, animate = false)
    }

    fun copyTask(task: Task, taskIsRepeating: Boolean = true) {
        if(taskIsRepeating) task.setRepeated()
        val taskId = activity.getList().addNewTaskId()
        val taskCopy = Task(task, taskId)
        if(taskIsRepeating) {
            taskCopy.setAsRepeat()
            taskCopy.setCompleted(false)
        }
        else taskCopy.setCompleted(task.isCompleted())
        val taskCopyIndex = activity.getList().add(taskCopy)
        addTask(taskCopy, taskCopyIndex, animate = false)
    }

    fun updateNoItemsMessageVisibility() {
        val list = activity.getList()
        val nItems = list.getCurrentTasks().size + list.getCompletedTasks().size
        activity.noItemsText.visibility = if(nItems == 0) View.VISIBLE else View.GONE
    }

    fun addTask(task: Task, initialIndex: Int = SENTINEL,
                animate: Boolean = true, taskReloaded: Boolean = false) {
        activity.getManager().updateTasksIfDateOutdated()
        val view = activity.layoutInflater.inflate(R.layout.view_to_do_list_task, null)

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

        if(task.isLinkedToList()) activity.getManager().removeLinkIfListDoesntExist(task)
        val showDate = task.hasDueDate()
        if(!showDate && task.hasTime()) {
            task.removeTime()
            activity.updateToDoList(false)
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

        checkbox.setOnClickListener {
            when {
                activity.getManager().inSelectState() -> selectTask(view, task)
                task.isCompleted() -> {
                    val index = activity.getList().removeTaskFromCompleted(task)
                    removeFromCompleted(index, taskText, checkbox, view)
                    refreshListOrderIfSorted()
                    activity.updateToDoList()
                }
                else -> {
                    val index = activity.getList().markTaskCompleted(task)
                    moveToCompleted(index, task, view)
                    if(task.isRepeating() && !task.alreadyRepeated()) copyTask(task)
                    if(task.hasReward() && !task.rewardAlreadyAdded()) addRewardTask(task)
                    refreshListOrderIfSorted()
                    activity.updateToDoList()
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
            if(!activity.getList().isSorted()) {
                if (task.isCompleted()) {
                    val currentIndex = activity.completedTaskContainer.indexOfChild(view)
                    if (index != currentIndex) {
                        beginTransition(activity.toDoListParent)
                        activity.completedTaskContainer.removeView(view)
                        activity.completedTaskContainer.addView(view, index)
                    }
                } else {
                    val currentIndex = activity.taskContainer.indexOfChild(view)
                    if (index != currentIndex) {
                        beginTransition(activity.toDoListParent)
                        activity.taskContainer.removeView(view)
                        activity.taskContainer.addView(view, index)
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
                    activity.updateToDoList()
                }
                else -> {
                    activity.getList().addTaskToStarred(task)
                    toggleTaskStar()
                    activity.updateToDoList()
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

        @Suppress("DEPRECATION")
        fun initTaskLayoutAnimationListener(button: View) {
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

        taskViewIsPressedMap[view] = false
        taskViewIsHighlightedMap[view] = false
        initTaskLayoutAnimationListener(taskLayout)
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
            beginTransition(activity.toDoListParent)
        when {
            task.isCompleted() -> {
                moveToCompleted(initialIndex,
                    task, view, false, !taskReloaded)
                if(!taskReloaded) {
                    if (task.isRepeating() && !task.alreadyRepeated()) copyTask(task)
                    if (task.hasReward() && !task.rewardAlreadyAdded()) addRewardTask(task)
                    if (!activity.getList().completedShown())
                        activity.completedTitleBar.performClick()
                }
            }
            initialIndex != SENTINEL -> activity.taskContainer.addView(view, initialIndex)
            else -> activity.taskContainer.addView(view)
        }

        taskViewMap[task.getTaskId()] = view
        if(!taskReloaded) refreshListOrderIfSorted()
    }

    private fun showCompletedTaskContainer() {
        val color: Int = activity.getColorTheme(
            activity.getList().getColorThemeIndex())
        activity.completedTaskContainer.visibility = View.VISIBLE
        activity.completedArrowIcon.setColoredImageResource(
            R.drawable.ic_keyboard_arrow_down_custom, color)
    }

    fun toggleCompletedTaskContainer(animate: Boolean = true) {
        if(animate) beginTransition(activity.toDoListParent)
        val color = activity.getColorTheme(activity.getList().getColorThemeIndex())
        if(activity.completedTaskContainer.visibility != View.VISIBLE)
            showCompletedTaskContainer()
        else {
            activity.completedTaskContainer.visibility = View.GONE
            activity.completedArrowIcon.setColoredImageResource(
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

    fun changeTheme(colorThemeIndex: Int = 0) {
        val color = activity.getColorTheme(colorThemeIndex)
        activity.getList().setColorThemeIndex(colorThemeIndex)

        // Change Activity Icons
        activity.backArrowIcon.setColoredImageResource(
            R.drawable.ic_arrow_back_custom, color)
        activity.moreOptionsIcon.setColoredImageResource(
            R.drawable.ic_more_vert_custom, color)
        val completedArrowIconCode =
            if(activity.completedTaskContainer.visibility == View.VISIBLE)
                R.drawable.ic_keyboard_arrow_down_custom else
                R.drawable.ic_keyboard_arrow_right_custom
        activity.completedArrowIcon.setColoredImageResource(
            completedArrowIconCode, color)
        activity.completedCountText.setTextColor(color)
        activity.completedTaskText.setTextColor(color)
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
        for(currentTaskId in activity.getList().getCurrentTasks()) {
            val view = taskViewMap[currentTaskId]
            val currentTask = activity.getList().getTask(currentTaskId)
            if(currentTask != null && currentTask.isStarred()) {
                val star = view.findViewById<ImageView>(R.id.star)
                star.setColoredImageResource(R.drawable.ic_star_custom, color)
            }
            if(currentTask != null && currentTask.isReward()) {
                val rewardTaskText = view.findViewById<TextView>(R.id.rewardTaskText)
                rewardTaskText.setTextColor(color)
            }
        }

        for(completedTaskId in activity.getList().getCompletedTasks()) {
            val view = taskViewMap[completedTaskId]
            val completedTask = activity.getList().getTask(completedTaskId)
            if(completedTask != null && completedTask.isStarred()) {
                val star = view.findViewById<ImageView>(R.id.star)
                star.setColoredImageResource(R.drawable.ic_star_custom, color)
            }
            if(completedTask != null && completedTask.isReward()) {
                val rewardTaskText = view.findViewById<TextView>(R.id.rewardTaskText)
                rewardTaskText.setTextColor(color)
            }
        }
    }
}