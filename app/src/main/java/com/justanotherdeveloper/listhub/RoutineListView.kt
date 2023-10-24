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
import androidx.core.view.isEmpty
import androidx.core.view.iterator
import androidx.core.view.size
import kotlinx.android.synthetic.main.activity_routine_list.*
import kotlinx.android.synthetic.main.activity_routine_list.addButtonBackground
import kotlinx.android.synthetic.main.activity_routine_list.addButtonLayout
import kotlinx.android.synthetic.main.activity_routine_list.backArrowIcon
import kotlinx.android.synthetic.main.activity_routine_list.completedArrowIcon
import kotlinx.android.synthetic.main.activity_routine_list.completedCountText
import kotlinx.android.synthetic.main.activity_routine_list.completedTaskContainer
import kotlinx.android.synthetic.main.activity_routine_list.completedTaskText
import kotlinx.android.synthetic.main.activity_routine_list.completedTitleBar
import kotlinx.android.synthetic.main.activity_routine_list.labelIcon
import kotlinx.android.synthetic.main.activity_routine_list.labelText
import kotlinx.android.synthetic.main.activity_routine_list.listActionBar
import kotlinx.android.synthetic.main.activity_routine_list.listDetailsLayout
import kotlinx.android.synthetic.main.activity_routine_list.listTitleText
import kotlinx.android.synthetic.main.activity_routine_list.moreOptionsIcon
import kotlinx.android.synthetic.main.activity_routine_list.noteIcon
import kotlinx.android.synthetic.main.activity_routine_list.selectedCountText
import kotlinx.android.synthetic.main.activity_routine_list.sortedByArrowIcon
import kotlinx.android.synthetic.main.activity_routine_list.sortedByExitIcon
import kotlinx.android.synthetic.main.activity_routine_list.sortedByText
import kotlinx.android.synthetic.main.activity_routine_list.sortedByTitleBar
import kotlinx.android.synthetic.main.activity_routine_list.taskSelectActionBar
import kotlinx.android.synthetic.main.activity_routine_list.tasksScrollView
import kotlinx.android.synthetic.main.activity_routine_list.tasksScrollViewContents

@SuppressLint("InflateParams")
class RoutineListView(private val activity: RoutineListActivity) {

    private var routineStepCounter = 1

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
        if(animate) beginTransition(activity.routineListParent)
        activity.noteIcon.visibility =
            if(activity.getList().hasNote())
                View.VISIBLE else View.GONE

        updateListDetailsLayoutVisibilities()
    }

    fun displayListDate(animate: Boolean = true) {
        if(animate) beginTransition(activity.routineListParent)
        val list = activity.getList()
        val dateText = (if(list.hasDate()) {
            val date = list.getDate()?: return
            activity.getRecencyText(date)
        } else list.getRepeatingDaysString(activity))
        activity.dateIcon.visibility = View.VISIBLE
        activity.dateText.visibility = View.VISIBLE
        val dateIconCode = if(list.hasDate())
            R.drawable.ic_date_gray else R.drawable.ic_autorenew_gray
        activity.dateIcon.setImageResource(dateIconCode)
        activity.dateText.text = dateText

        updateListDetailsLayoutVisibilities()
    }

    fun removeListDate(animate: Boolean = true) {
        if(activity.dateIcon.visibility == View.GONE) return
        if(animate) beginTransition(activity.routineListParent)
        activity.dateIcon.visibility = View.GONE
        activity.dateText.visibility = View.GONE

        updateListDetailsLayoutVisibilities()
    }

    fun displayLabels(labelsString: String, animate: Boolean = true) {
        if(animate) beginTransition(activity.routineListParent)
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
        val hasDate = list.hasDate() || list.isRepeating()
        val hasLabels = list.hasLabels()
        val hasNote = list.hasNote()

        activity.listDetailsLayout.visibility =
            if(hasLabels || hasNote || hasDate)
                View.VISIBLE else View.GONE

        var div1IsVisible = false
        val div2IsVisible: Boolean
        if(hasNote) {
            div1IsVisible = hasDate
            div2IsVisible = hasLabels
        } else div2IsVisible = hasDate && hasLabels
        activity.listDetailsDivider1.visibility =
            if(div1IsVisible) View.VISIBLE else View.GONE
        activity.listDetailsDivider2.visibility =
            if(div2IsVisible) View.VISIBLE else View.GONE
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

    @SuppressLint("SetTextI18n")
    fun updateRoutineNumbers() {
        routineStepCounter = 1
        for(id in activity.getList().getRoutineTaskIdsOrder()) {
            val view = taskViewMap[id]
            val routineStepText = view.findViewById<TextView>(R.id.routineStepText)
            routineStepText.text = "${routineStepCounter++}. "
        }
    }

    fun reloadRoutineList(reloadAll: Boolean = true) {
        val list = activity.getList()
        if(reloadAll) {
            activity.listTitleText.text = list.getTitle()
            displayLabels(list.getLabelsString(), animate = false)
            displayNote(animate = false)
            if (list.hasDate() || list.isRepeating())
                displayListDate(animate = false)
            else removeListDate(animate = false)
        }
        routineStepCounter = 1
        taskViewMap.clear()
        taskViewIsPressedMap.clear()
        taskViewIsHighlightedMap.clear()
        activity.taskContainer.removeAllViews()
        activity.completedTaskContainer.removeAllViews()
        activity.tasksScrollViewContents.visibility = View.INVISIBLE
        for(id in list.getRoutineTaskIdsOrder()) {
            val task = list.getTask(id)
            if(task != null) addTask(task, animate = false, taskReloaded = true)
        }
        updateNoItemsMessageVisibility()
        refreshListOrderIfSorted()
        if(reloadAll) {
            changeTheme(list.getColorThemeIndex())
            showCompletedTaskContainer()
            if(!list.completedShown())
                toggleCompletedTaskContainer(false)
            updateToggleNumbersViews()
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
        if(animate) beginTransition(activity.routineListParent)
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

        beginTransition(activity.routineListParent)
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

        beginTransition(activity.routineListParent)
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
            if(task != null) selectTask(taskView, task, true)
        }

        if(activity.getList().completedShown()) {
            for (taskId in activity.getList().getCompletedTasks()) {
                val taskView = taskViewMap[taskId]
                val task = activity.getList().getTask(taskId)
                if(task != null) selectTask(taskView, task, true)
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
                                    checkbox: ImageView, view: View,
                                    animate: Boolean = true) {
        if(animate) beginTransition(activity.routineListParent)
        taskText.setTextColor(Color.WHITE)
        taskText.paintFlags = 0
        checkbox.setImageResource(R.drawable.ic_radio_button_unchecked_gray)
        activity.completedTaskContainer.removeView(view)
        activity.taskContainer.addView(view, index)
        if(activity.completedTaskContainer.size == 0)
            activity.resetList.visibility = View.GONE
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

        if(animate) beginTransition(activity.routineListParent)
        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())
        if(activity.completedTitleBar.visibility != View.VISIBLE)
            activity.completedTitleBar.visibility = View.VISIBLE
        if(activity.resetList.visibility != View.VISIBLE)
            activity.resetList.visibility = View.VISIBLE
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
        val checkbox = view.findViewById<ImageView>(R.id.checkbox)
        val taskText = view.findViewById<TextView>(R.id.taskText)
        val taskDetailsLayout = view.findViewById<LinearLayout>(R.id.taskDetailsLayout)
        val dateIcon = view.findViewById<ImageView>(R.id.dateIcon)
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val taskDetailsDivider1 = view.findViewById<TextView>(R.id.taskDetailsDivider1)
        val noteIcon = view.findViewById<ImageView>(R.id.noteIcon)
        val taskDetailsDivider2 = view.findViewById<TextView>(R.id.taskDetailsDivider2)
        val linkIcon = view.findViewById<ImageView>(R.id.linkIcon)

        taskText.text = task.getTask()

        var taskMoved = false

        if(task.isCompleted()) {
            if(!activity.getList().taskIsCompleted(task)) {
                val index = activity.getList().markTaskCompleted(task)
                moveToCompleted(index, task, view)
                if(task.isRepeating() && !task.alreadyRepeated()) copyTask(task)
                taskMoved = true
            }
        } else {
            if(activity.getList().taskIsCompleted(task)) {
                val index = activity.getList().removeTaskFromCompleted(task)
                removeFromCompleted(index, taskText, checkbox, view)
                taskMoved = true
            }
        }

        if(!taskMoved) beginTransition(activity.routineListParent)
        taskText.text = task.getTask()
        if(task.hasTime()) displayTime(dateIcon, dateText, task) else {
            dateIcon.visibility = View.GONE
            dateText.visibility = View.GONE
        }
        noteIcon.visibility =
            if(task.hasNote())
                View.VISIBLE else View.GONE
        val showLink = task.isLinkedToList() || task.hasWebsiteLink()
        linkIcon.visibility =
            if(showLink)
                View.VISIBLE else View.GONE

        updateLayoutVisibilities(taskDetailsLayout,
            taskDetailsDivider1, taskDetailsDivider2,
            task.hasTime(), task.hasNote(), showLink)

        refreshListOrderIfSorted()
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

    fun uncheckTask(task: Task, view: View, animate: Boolean = true, resettingList: Boolean = false) {
        val checkbox = view.findViewById<ImageView>(R.id.checkbox)
        val taskText = view.findViewById<TextView>(R.id.taskText)
        val index = activity.getList().removeTaskFromCompleted(task)
        removeFromCompleted(index, taskText, checkbox, view, animate = animate)
        if(!resettingList) {
            refreshListOrderIfSorted()
            activity.updateRoutineList()
        }
    }

    private fun completeTask(task: Task, view: View) {
        val index = activity.getList().markTaskCompleted(task)
        moveToCompleted(index, task, view)
        if(task.isRepeating() && !task.alreadyRepeated()) copyTask(task)
        refreshListOrderIfSorted()
        activity.updateRoutineList()
    }

    private fun updateLayoutVisibilities(taskDetailsLayout: LinearLayout,
                                         divider1: TextView, divider2: TextView,
                                         dateShown: Boolean, noteShown: Boolean,
                                         linkShown: Boolean) {
        taskDetailsLayout.visibility =
            if(dateShown || noteShown || linkShown)
                View.VISIBLE else View.GONE
        var div1IsVisible = false
        val div2IsVisible: Boolean
        if(dateShown) {
            div1IsVisible = noteShown
            div2IsVisible = linkShown
        } else div2IsVisible = noteShown && linkShown
        divider1.visibility = if(div1IsVisible) View.VISIBLE else View.GONE
        divider2.visibility = if(div2IsVisible) View.VISIBLE else View.GONE
    }

    fun updateNoItemsMessageVisibility() {
        val list = activity.getList()
        val nItems = list.getCurrentTasks().size + list.getCompletedTasks().size
        activity.noItemsText.visibility = if(nItems == 0) View.VISIBLE else View.GONE
    }

    @SuppressLint("SetTextI18n")
    fun addTask(task: Task, initialIndex: Int = SENTINEL,
                animate: Boolean = true, taskReloaded: Boolean = false) {
        val view = activity.layoutInflater.inflate(R.layout.view_to_do_list_task, null)

        val taskLayout = view.findViewById<LinearLayout>(R.id.taskLayout)
        val checkbox = view.findViewById<ImageView>(R.id.checkbox)
        val routineStepText = view.findViewById<TextView>(R.id.routineStepText)
        val taskText = view.findViewById<TextView>(R.id.taskText)
        val taskDetailsLayout = view.findViewById<LinearLayout>(R.id.taskDetailsLayout)
        val dateIcon = view.findViewById<ImageView>(R.id.dateIcon)
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val taskDetailsDivider1 = view.findViewById<TextView>(R.id.taskDetailsDivider1)
        val noteIcon = view.findViewById<ImageView>(R.id.noteIcon)
        val taskDetailsDivider2 = view.findViewById<TextView>(R.id.taskDetailsDivider2)
        val linkIcon = view.findViewById<ImageView>(R.id.linkIcon)
        val star = view.findViewById<ImageView>(R.id.star)

        if(task.isLinkedToList()) activity.getManager().removeLinkIfListDoesntExist(task)

        if(activity.getList().routineNumbersShown())
            routineStepText.visibility = View.VISIBLE
        routineStepText.text = "${routineStepCounter++}. "
        taskText.text = task.getTask()
        if(task.hasTime()) displayTime(dateIcon, dateText, task)
        if(task.hasNote()) noteIcon.visibility = View.VISIBLE
        val showLink = task.isLinkedToList() || task.hasWebsiteLink()
        if(showLink) linkIcon.visibility = View.VISIBLE

        updateLayoutVisibilities(taskDetailsLayout,
            taskDetailsDivider1, taskDetailsDivider2,
            task.hasTime(), task.hasNote(), showLink)

        checkbox.setOnClickListener {
            when {
                activity.getManager().inSelectState() -> selectTask(view, task)
                task.isCompleted() -> uncheckTask(task, view)
                else -> completeTask(task, view)
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
                        beginTransition(activity.routineListParent)
                        activity.completedTaskContainer.removeView(view)
                        activity.completedTaskContainer.addView(view, index)
                    }
                } else {
                    val currentIndex = activity.taskContainer.indexOfChild(view)
                    if (index != currentIndex) {
                        beginTransition(activity.routineListParent)
                        activity.taskContainer.removeView(view)
                        activity.taskContainer.addView(view, index)
                    }
                }
            }
            updateRoutineNumbers()

            val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())
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
                    activity.updateRoutineList()
                }
                else -> {
                    activity.getList().addTaskToStarred(task)
                    toggleTaskStar()
                    activity.updateRoutineList()
                }
            }
        }

        star.setOnLongClickListener {
            if(!activity.getManager().inSelectState())
                setToTaskSelectState(view, task)
            else activity.getDialogs().showSelectedMoreOptionsDialog()
            true
        }

        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())
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
            beginTransition(activity.routineListParent)
        when {
            task.isCompleted() -> {
                moveToCompleted(initialIndex,
                    task, view, false, !taskReloaded)
                if(task.isRepeating() && !task.alreadyRepeated() && !taskReloaded) copyTask(task)
                if(!taskReloaded && !activity.getList().completedShown())
                    activity.completedTitleBar.performClick()
            }
            initialIndex != SENTINEL -> activity.taskContainer.addView(view, initialIndex)
            else -> activity.taskContainer.addView(view)
        }

        taskViewMap[task.getTaskId()] = view
        if(!taskReloaded) refreshListOrderIfSorted()
    }

    private fun updateToggleNumbersViews(showNumbers: Boolean
                                         = activity.getList().routineNumbersShown()) {
        val iconCode = if(showNumbers)
            R.drawable.ic_format_list_numbers_gone_custom
        else R.drawable.ic_format_list_numbered_custom

        val stringCode = if(showNumbers)
            R.string.hideNumbersString else R.string.showNumbersString

        val color = activity.getColorTheme(activity.getList().getColorThemeIndex())
        activity.toggleNumbersIcon.setColoredImageResource(iconCode, color)
        activity.toggleNumbersText.text = activity.getString(stringCode)
    }

    fun toggleRoutineNumbers(showNumbers: Boolean, animate: Boolean = true) {
        if(animate) beginTransition(activity.routineListParent)

        fun toggleNumberVisibility(view: View) {
            val routineStepText = view.findViewById<TextView>(R.id.routineStepText)
            if(showNumbers) routineStepText.visibility = View.VISIBLE
            else routineStepText.visibility = View.GONE
        }

        for(currentTaskView in activity.taskContainer)
            toggleNumberVisibility(currentTaskView)

        for(completedTaskView in activity.completedTaskContainer)
            toggleNumberVisibility(completedTaskView)

        updateToggleNumbersViews(showNumbers)
    }

    private fun showCompletedTaskContainer() {
        val color: Int = activity.getColorTheme(
            activity.getList().getColorThemeIndex())
        activity.completedTaskContainer.visibility = View.VISIBLE
        activity.completedArrowIcon.setColoredImageResource(
            R.drawable.ic_keyboard_arrow_down_custom, color)
    }

    fun toggleCompletedTaskContainer(animate: Boolean = true) {
        if(animate) beginTransition(activity.routineListParent)
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
        activity.resetListIcon.setColoredImageResource(
            R.drawable.ic_replay_custom, color)
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
        val toggleNumbersIconCode =
            if(activity.getList().routineNumbersShown())
                R.drawable.ic_format_list_numbers_gone_custom else
                R.drawable.ic_format_list_numbered_custom
        activity.toggleNumbersIcon.setColoredImageResource(
            toggleNumbersIconCode, color)
        activity.toggleNumbersText.setTextColor(color)
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
        for(view in activity.completedTaskContainer.iterator()) {
            val markedCheckbox = view.findViewById<ImageView>(R.id.checkbox)
            markedCheckbox.setColoredImageResource(R.drawable.ic_check_circle_custom, color)
        }

        // Change Existing Starred Icons
        for(currentTaskId in activity.getList().getCurrentTasks()) {
            val currentTask = activity.getList().getTask(currentTaskId)
            if(currentTask != null && currentTask.isStarred()) {
                val view = taskViewMap[currentTaskId]
                val star = view.findViewById<ImageView>(R.id.star)
                star.setColoredImageResource(R.drawable.ic_star_custom, color)
            }
        }

        for(completedTaskId in activity.getList().getCompletedTasks()) {
            val completedTask = activity.getList().getTask(completedTaskId)
            if(completedTask != null && completedTask.isStarred()) {
                val view = taskViewMap[completedTaskId]
                val star = view.findViewById<ImageView>(R.id.star)
                star.setColoredImageResource(R.drawable.ic_star_custom, color)
            }
        }
    }
}