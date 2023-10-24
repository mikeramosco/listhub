package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
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
import kotlinx.android.synthetic.main.activity_bulleted_list.*

@SuppressLint("InflateParams")
class BulletedListView(private val activity: BulletedListActivity) {
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
        if(animate) beginTransition(activity.bulletedListParent)
        activity.noteIcon.visibility =
            if(activity.getList().hasNote())
                View.VISIBLE else View.GONE

        updateListDetailsLayoutVisibilities()
    }

    fun displayLabels(labelsString: String, animate: Boolean = true) {
        if(animate) beginTransition(activity.bulletedListParent)
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
    }

    fun reloadBulletedList(reloadAll: Boolean = true) {
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
        activity.tasksScrollViewContents.visibility = View.INVISIBLE
        for(taskId in activity.getList().getCurrentTasks()) {
            val task = activity.getList().getTask(taskId)
            if(task != null) addTask(task, animate = false, taskReloaded = true)
        }
        updateNoItemsMessageVisibility()
        refreshListOrderIfSorted()
        if(reloadAll) changeTheme(list.getColorThemeIndex())
        activity.getManager().checkForTaskToOpen()
        activity.tasksScrollView.post {
            activity.tasksScrollView.scrollY = activity.getList().getScrollState()
            activity.tasksScrollViewContents.visibility = View.VISIBLE
        }
    }

    private fun refreshListOrderIfSorted() {
        if(activity.getList().isSorted())
            sortList(activity.getList().getSortIndex(), false)
    }

    fun sortList(sortIndex: Int, animate: Boolean = true) {
        if(animate) beginTransition(activity.bulletedListParent)
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
                activity.getString(R.string.sortedBySetDateString)
            sortNewestFirstIndex, sortOldestFirstIndex ->
                activity.getString(R.string.sortedByCreationDateString)
            else -> activity.getString(R.string.sortedAlphabeticallyString)
        }

        moveTaskViewsToSortedOrder(sortIndex)
    }

    private fun moveTaskViewsToSortedOrder(sortIndex: Int) {
        val sortedTaskOrder =
            activity.getManager().getSortedTaskOrder(sortIndex)
        activity.taskContainer.removeAllViews()
        for(task in sortedTaskOrder)
            activity.taskContainer.addView(taskViewMap[task.getTaskId()])
    }

    private fun setToTaskSelectState(selectedView: View? = null, task: Task? = null) {
        if(activity.getManager().inSelectState()) return
        activity.getManager().setSelectState(true)

        beginTransition(activity.bulletedListParent)
        activity.addButtonLayout.visibility = View.GONE
        activity.listActionBar.visibility = View.GONE
        activity.taskSelectActionBar.visibility = View.VISIBLE

        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())
        for(currentTaskView in activity.taskContainer) {
            val checkbox = currentTaskView.findViewById<ImageView>(R.id.checkbox)
            val bulletpoint = currentTaskView.findViewById<ImageView>(R.id.bulletpoint)
            checkbox.visibility = View.VISIBLE
            bulletpoint.visibility = View.GONE
            checkbox.setColoredImageResource(R.drawable.ic_radio_button_unchecked_custom, colorTheme)
        }

        if(selectedView != null && task != null) selectTask(selectedView, task)
    }

    fun setToDefaultState() {
        if(!activity.getManager().inSelectState()) return
        activity.getManager().setSelectState(false)

        beginTransition(activity.bulletedListParent)
        activity.addButtonLayout.visibility = View.VISIBLE
        activity.listActionBar.visibility = View.VISIBLE
        activity.taskSelectActionBar.visibility = View.GONE

        fun resetTaskView(view: View) {
            val taskLayout = view.findViewById<LinearLayout>(R.id.taskLayout)
            taskLayout.background = ContextCompat.getDrawable(
                activity, R.drawable.transition_layout_list_item)
            val checkbox = view.findViewById<ImageView>(R.id.checkbox)
            val bulletpoint = view.findViewById<ImageView>(R.id.bulletpoint)
            checkbox.visibility = View.GONE
            bulletpoint.visibility = View.VISIBLE
        }

        for(currentTaskView in activity.taskContainer.iterator())
            resetTaskView(currentTaskView)
    }

    fun selectAll() {
        if(!activity.getManager().inSelectState())
            setToTaskSelectState()

        for(taskId in activity.getList().getCurrentTasks()) {
            val taskView = taskViewMap[taskId]
            val task = activity.getList().getTask(taskId)
            if(task != null) selectTask(taskView, task, true)
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

    fun editTask(task: Task, view: View) {
        activity.getManager().updateTasksIfDateOutdated()
        val taskText = view.findViewById<TextView>(R.id.taskText)
        val taskDetailsLayout = view.findViewById<LinearLayout>(R.id.taskDetailsLayout)
        val dateIcon = view.findViewById<ImageView>(R.id.dateIcon)
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val taskDetailsDivider1 = view.findViewById<TextView>(R.id.taskDetailsDivider1)
        val noteIcon = view.findViewById<ImageView>(R.id.noteIcon)
        val taskDetailsDivider2 = view.findViewById<TextView>(R.id.taskDetailsDivider2)
        val linkIcon = view.findViewById<ImageView>(R.id.linkIcon)

        taskText.text = task.getTask()

        beginTransition(activity.bulletedListParent)
        taskText.text = task.getTask()
        val showDate = task.hasDueDate()
        if(showDate) activity.displayDate(dateIcon, dateText, task) else {
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
            showDate, task.hasNote(), showLink)

        refreshListOrderIfSorted()
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
        activity.noItemsText.visibility =
            if(activity.taskContainer.isEmpty())
                View.VISIBLE else View.GONE
    }

    fun addTask(task: Task, initialIndex: Int = SENTINEL,
                animate: Boolean = true, taskReloaded: Boolean = false) {
        activity.getManager().updateTasksIfDateOutdated()
        val view = activity.layoutInflater.inflate(R.layout.view_to_do_list_task, null)

        val taskLayout = view.findViewById<LinearLayout>(R.id.taskLayout)
        val checkbox = view.findViewById<ImageView>(R.id.checkbox)
        val bulletpoint = view.findViewById<ImageView>(R.id.bulletpoint)
        val taskText = view.findViewById<TextView>(R.id.taskText)
        val taskDetailsLayout = view.findViewById<LinearLayout>(R.id.taskDetailsLayout)
        val dateIcon = view.findViewById<ImageView>(R.id.dateIcon)
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val taskDetailsDivider1 = view.findViewById<TextView>(R.id.taskDetailsDivider1)
        val noteIcon = view.findViewById<ImageView>(R.id.noteIcon)
        val taskDetailsDivider2 = view.findViewById<TextView>(R.id.taskDetailsDivider2)
        val linkIcon = view.findViewById<ImageView>(R.id.linkIcon)
        val star = view.findViewById<ImageView>(R.id.star)

        checkbox.visibility = View.GONE
        bulletpoint.visibility = View.VISIBLE

        if(task.isLinkedToList()) activity.getManager().removeLinkIfListDoesntExist(task)
        val showDate = task.hasDueDate()
        if(!showDate && task.hasTime()) {
            task.removeTime()
            activity.updateBulletedList(false)
        }

        taskText.text = task.getTask()
        if(showDate) activity.displayDate(dateIcon, dateText, task)
        if(task.hasNote()) noteIcon.visibility = View.VISIBLE
        val showLink = task.isLinkedToList() || task.hasWebsiteLink()
        if(showLink) linkIcon.visibility = View.VISIBLE

        updateLayoutVisibilities(taskDetailsLayout,
            taskDetailsDivider1, taskDetailsDivider2,
            showDate, task.hasNote(), showLink)

        fun toggleTaskStar(index: Int = 0) {
            if(!activity.getList().isSorted()) {
                val currentIndex = activity.taskContainer.indexOfChild(view)
                if (index != currentIndex) {
                    beginTransition(activity.bulletedListParent)
                    activity.taskContainer.removeView(view)
                    activity.taskContainer.addView(view, index)
                }
            }

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
                    activity.updateBulletedList()
                }
                else -> {
                    activity.getList().addTaskToStarred(task)
                    toggleTaskStar()
                    activity.updateBulletedList()
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
            beginTransition(activity.bulletedListParent)
        when {
            initialIndex != SENTINEL -> activity.taskContainer.addView(view, initialIndex)
            else -> activity.taskContainer.addView(view)
        }

        taskViewMap[task.getTaskId()] = view
        if(!taskReloaded) refreshListOrderIfSorted()
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

        // Change Existing Starred Icons
        for(currentTaskId in activity.getList().getCurrentTasks()) {
            val currentTask = activity.getList().getTask(currentTaskId)
            if(currentTask != null && currentTask.isStarred()) {
                val view = taskViewMap[currentTask.getTaskId()]
                val star = view.findViewById<ImageView>(R.id.star)
                star.setColoredImageResource(R.drawable.ic_star_custom, color)
            }
        }
    }
}