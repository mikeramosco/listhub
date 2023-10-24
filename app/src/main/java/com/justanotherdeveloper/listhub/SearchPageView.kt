package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import kotlinx.android.synthetic.main.activity_search_page.*

@SuppressLint("InflateParams")
class SearchPageView(private val activity: SearchPageActivity) {

    private val itemViewMap = HashMap<String, View>()
    private val itemViewIsPressedMap = HashMap<View, Boolean>()
    private val itemViewIsHighlightedMap = HashMap<View, Boolean>()

    private val keyListMap = HashMap<String, List>()
    private val keyTaskMap = HashMap<String, Task>()
    private var sortedItemsOrder = ArrayList<String>()

    private var indexOfNextItem = 0

    init {
        activity.searchPageParent.post {
            activity.searchField.requestFocus()
        }
    }

    private fun displayItemsInSortedOrder(tasksToDisplay: ArrayList<Task>,
                                          listsToDisplay: ArrayList<List>) {
        keyListMap.clear()
        keyTaskMap.clear()
        for(list in listsToDisplay)
            keyListMap[list.getListId().toString()] = list
        for(task in tasksToDisplay)
            keyTaskMap[getTaskKey(task)] = task
        sortedItemsOrder = activity.getManager()
            .getSortedItemsOrder(tasksToDisplay, listsToDisplay)
        indexOfNextItem = 0
        for((index, key) in sortedItemsOrder.withIndex()) {
            if(index == ITEM_COUNT_FIRST_LOAD) {
                indexOfNextItem = index
                activity.bottomSpacer.visibility = View.GONE
                activity.scrollViewLoadingCircle.visibility = View.VISIBLE
                break
            } else if(key.contains("\t")) {
                val task = keyTaskMap[key]
                if(task != null) displayTask(task)
            } else {
                val list = keyListMap[key]
                if(list != null) displayList(list)
            }
            if(index == sortedItemsOrder.lastIndex)
                indexOfNextItem = sortedItemsOrder.size
        }
    }

    fun loadMoreItems() {
        if(indexOfNextItem == sortedItemsOrder.size) return
        val indexToStopLoading = indexOfNextItem + ITEM_COUNT_PER_INTERVAL
        while(true) {
            val key = sortedItemsOrder[indexOfNextItem++]
            if(key.contains("\t")) {
                val task = keyTaskMap[key]
                if(task != null) displayTask(task)
            } else {
                val list = keyListMap[key]
                if(list != null) displayList(list)
            }
            if(indexOfNextItem == sortedItemsOrder.size) {
                activity.bottomSpacer.visibility = View.VISIBLE
                activity.scrollViewLoadingCircle.visibility = View.GONE
                break
            }
            if(indexOfNextItem == indexToStopLoading) break
        }
    }

    fun displaySearchedItems(listsToDisplay: ArrayList<List>, tasksToDisplay: ArrayList<Task>) {
        clearDisplayedItems()
        displayItemsInSortedOrder(tasksToDisplay, listsToDisplay)
        activity.searchBarLoadingCircle.visibility = View.GONE
    }

    fun clearDisplayedItems(loadingItems: Boolean = true) {
        for(view in activity.searchedItemsContainer.iterator())
            view.visibility = View.GONE
        activity.scrollViewLoadingCircle.visibility = View.GONE
        activity.bottomSpacer.visibility = View.VISIBLE
        if(!loadingItems) {
            activity.searchBarLoadingCircle.visibility = View.GONE
            activity.noItemsText.visibility = View.GONE
        } else activity.noItemsText.visibility = View.VISIBLE
    }

    private fun displayList(list: List) {
        activity.noItemsText.visibility = View.GONE
        val key = list.getListId().toString()
        if(itemViewMap.containsKey(key)) {
            val view = itemViewMap[key]
            view?.visibility = View.VISIBLE
            activity.searchedItemsContainer.removeView(view)
            activity.searchedItemsContainer.addView(view)
        } else inflateListView(list)
    }

    private fun displayTask(task: Task) {
        activity.noItemsText.visibility = View.GONE
        val key = getTaskKey(task)
        if(itemViewMap.containsKey(key)) {
            val view = itemViewMap[key]
            view?.visibility = View.VISIBLE
            activity.searchedItemsContainer.removeView(view)
            activity.searchedItemsContainer.addView(view)
        } else inflateTaskView(task)
    }

    private fun inflateListView(list: List) {
        val view = activity.layoutInflater.inflate(R.layout.view_list_title, null)

        val colorTheme = activity.getColorTheme()

        val listTitleLayout = view.findViewById<LinearLayout>(R.id.listTitleLayout)
        val listTypeIcon = view.findViewById<ImageView>(R.id.listTypeIcon)
        val listTitleText = view.findViewById<TextView>(R.id.listTitleText)
        val listDetailsLayout = view.findViewById<LinearLayout>(R.id.listDetailsLayout)
        val noteIcon = view.findViewById<ImageView>(R.id.noteIcon)
        val listDetailsDivider1 = view.findViewById<TextView>(R.id.listDetailsDivider1)
        val dateIcon = view.findViewById<ImageView>(R.id.dateIcon)
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val listDetailsDivider2 = view.findViewById<TextView>(R.id.listDetailsDivider2)
        val labelIcon = view.findViewById<ImageView>(R.id.labelIcon)
        val labelText = view.findViewById<TextView>(R.id.labelText)
        val detailsIcon = view.findViewById<ImageView>(R.id.detailsIcon)
        val star = view.findViewById<ImageView>(R.id.star)

        listTitleText.text = list.getTitle()

        if(list.hasLabels()) {
            labelIcon.visibility = View.VISIBLE
            labelText.visibility = View.VISIBLE
            labelText.text = list.getLabelsString()
        }

        var hasDate = false
        if(list.getListType() == routineListRef){
            val routineList = activity.getDatabase()
                .getRoutineList(list.getListId())
            hasDate = routineList.hasDate() || routineList.isRepeating()
            if(hasDate) {
                dateIcon.visibility = View.VISIBLE
                dateText.visibility = View.VISIBLE
                var dateString = ""
                if(routineList.hasDate()) {
                    val date = routineList.getDate()?: getTodaysDate()
                    dateString = activity.getRecencyText(date)
                } else if(routineList.isRepeating()) {
                    dateIcon.setImageResource(R.drawable.ic_autorenew_gray)
                    dateString = routineList.getRepeatingDaysString(activity)
                }
                dateText.text = dateString
            }
        }

        if(list.hasNote()) noteIcon.visibility = View.VISIBLE

        val hasLabels = list.hasLabels()
        val hasNote = list.hasNote()

        listDetailsLayout.visibility =
            if(hasLabels || hasNote || hasDate)
                View.VISIBLE else View.GONE

        var div1IsVisible = false
        val div2IsVisible: Boolean
        if(hasNote) {
            div1IsVisible = hasDate
            div2IsVisible = hasLabels
        } else div2IsVisible = hasDate && hasLabels
        listDetailsDivider1.visibility =
            if(div1IsVisible) View.VISIBLE else View.GONE
        listDetailsDivider2.visibility =
            if(div2IsVisible) View.VISIBLE else View.GONE

        detailsIcon.visibility = View.VISIBLE
        detailsIcon.setOnClickListener {
            when(list.getListType()) {
                toDoListRef -> activity.openToDoListDetailsDialog(
                    activity.getDatabase().getToDoList(list.getListId()))
                progressListRef -> activity.openProgressListDetailsDialog(
                    activity.getDatabase().getProgressList(list.getListId()))
                routineListRef -> activity.openRoutineListDetailsDialog(
                    activity.getDatabase().getRoutineList(list.getListId()))
                bulletedListRef -> activity.openBulletedListDetailsDialog(
                    activity.getDatabase().getBulletedList(list.getListId()))
            }
        }

        val listTypeImageRes = when(list.getListType()) {
            toDoListRef -> R.drawable.ic_list_white
            progressListRef -> R.drawable.ic_view_list_white
            routineListRef -> R.drawable.ic_format_list_numbered_white
            else -> R.drawable.ic_format_list_bulleted_white
        }

        listTypeIcon.setImageResource(listTypeImageRes)

        fun toggleListStar() {
            if(list.isStarred())
                star.setColoredImageResource(R.drawable.ic_star_custom, colorTheme)
            else star.setImageResource(R.drawable.ic_star_border_gray)
        }

        star.setOnClickListener {
            when {
                list.isStarred() -> {
                    list.listUpdated()
                    list.toggleStar()
                    activity.getDatabase().toggleStarOfList(list)
                    activity.getDatabase().removeListFromFavorites(list.getListId())
                    toggleListStar()
                }
                else -> {
                    list.listUpdated()
                    list.toggleStar()
                    activity.getDatabase().toggleStarOfList(list)
                    activity.getDatabase().addListToFavorites(list.getListId())
                    toggleListStar()
                }
            }
        }

        if(list.isStarred()) star.setColoredImageResource(R.drawable.ic_star_custom, colorTheme)

        initListTitleLayoutAnimationListener(listTitleLayout)
        listTitleLayout.setOnClickListener {
            if(!activity.getManager().listAlreadyOpened()) {
                activity.getManager().setListOpened()
                when (list.getListType()) {
                    toDoListRef -> activity.openToDoList(list.getListId())
                    progressListRef -> activity.openProgressList(list.getListId())
                    routineListRef -> activity.openRoutineList(list.getListId())
                    bulletedListRef -> activity.openBulletedList(list.getListId())
                }
            }

        }

        activity.searchedItemsContainer.addView(view)

        itemViewMap[list.getListId().toString()] = view
    }

    private fun inflateTaskView(task: Task) {
        val view = activity.layoutInflater.inflate(R.layout.view_to_do_list_task, null)

        val colorTheme = activity.getColorTheme()

        val taskLayout = view.findViewById<LinearLayout>(R.id.taskLayout)
        val checkbox = view.findViewById<ImageView>(R.id.checkbox)
        val bulletpoint = view.findViewById<ImageView>(R.id.bulletpoint)
        val rewardTaskText = view.findViewById<TextView>(R.id.rewardTaskText)
        val taskText = view.findViewById<TextView>(R.id.taskText)
        val taskDetailsLayout = view.findViewById<LinearLayout>(R.id.taskDetailsLayout)
        val dateIcon = view.findViewById<ImageView>(R.id.dateIcon)
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val taskDetailsDivider = view.findViewById<TextView>(R.id.taskDetailsDivider4)
        val listIcon = view.findViewById<ImageView>(R.id.listIcon)
        val listText = view.findViewById<TextView>(R.id.listText)
        val detailsIcon = view.findViewById<ImageView>(R.id.detailsIcon)
        val star = view.findViewById<ImageView>(R.id.star)

        taskDetailsLayout.visibility = View.VISIBLE
        listIcon.visibility = View.VISIBLE
        listText.visibility = View.VISIBLE

        val list = activity.getDatabase().getList(task.getListId())
        val listIconCode = when(list.getListType()) {
            toDoListRef -> R.drawable.ic_list_gray
            progressListRef -> R.drawable.ic_view_list_gray
            routineListRef -> R.drawable.ic_format_list_numbered_gray
            else -> R.drawable.ic_format_list_bulleted_gray
        }
        listIcon.setImageResource(listIconCode)
        listText.text = list.getTitle()

        if(list.getListType() == bulletedListRef) {
            bulletpoint.visibility = View.VISIBLE
            checkbox.visibility = View.GONE
        }

        val canBeReward = list.getListType() == toDoListRef ||
                list.getListType() == progressListRef
        if(canBeReward && task.isReward()) {
            rewardTaskText.visibility = View.VISIBLE
            rewardTaskText.setTextColor(colorTheme)
        }

        detailsIcon.visibility = View.VISIBLE
        detailsIcon.setOnClickListener {
            when(list.getListType()) {
                toDoListRef -> activity.openToDoListTaskDetailsDialog(
                    task, activity.getDatabase())
                progressListRef -> activity.openProgressListTaskDetailsDialog(
                    task as ProgressTask, activity.getDatabase())
                routineListRef -> activity.openRoutineStepDetailsDialog(
                    task, activity.getDatabase())
                bulletedListRef -> activity.openBulletpointDetailsDialog(
                    task, activity.getDatabase())
            }
        }

        taskText.text = task.getTask()
        if(list.getListType() == routineListRef) {
            if(task.hasTime()) {
                displayTime(dateIcon, dateText, task)
                taskDetailsDivider.visibility = View.VISIBLE
            }
        } else {
            val showDate = task.hasDueDate()
            if (showDate) {
                activity.displayDate(dateIcon, dateText, task)
                taskDetailsDivider.visibility = View.VISIBLE
            }
        }

        fun removeFromCompleted() {
            beginTransition(activity.searchPageParent)
            taskText.setTextColor(Color.WHITE)
            taskText.paintFlags = 0
            checkbox.setImageResource(R.drawable.ic_radio_button_unchecked_gray)
        }

        fun moveToCompleted(animate: Boolean = true) {
            if(list.getListType() == bulletedListRef) return
            if(animate) beginTransition(activity.searchPageParent)
            taskText.setTextColor(ContextCompat.getColor(
                activity, R.color.colorLightGray))
            taskText.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                text = task.getTask()
            }
            checkbox.setColoredImageResource(R.drawable.ic_check_circle_custom, colorTheme)
        }

        checkbox.setOnClickListener {
            when {
                task.isCompleted() -> {
                    activity.getManager().removeTaskFromCompleted(task, list.getListType())
                    removeFromCompleted()
                }
                else -> {
                    activity.getManager().markTaskCompleted(task, list.getListType())
                    moveToCompleted()
                }
            }
        }

        fun toggleTaskStar() {
            if(task.isStarred())
                star.setColoredImageResource(R.drawable.ic_star_custom, colorTheme)
            else star.setImageResource(R.drawable.ic_star_border_gray)
        }

        star.setOnClickListener {
            when {
                task.isStarred() -> {
                    activity.getManager().removeTaskFromStarred(task, list.getListType())
                    toggleTaskStar()
                }
                else -> {
                    activity.getManager().addTaskToStarred(task, list.getListType())
                    toggleTaskStar()
                }
            }
        }

        if(task.isStarred()) star.setColoredImageResource(R.drawable.ic_star_custom, colorTheme)

        initListTitleLayoutAnimationListener(taskLayout)
        taskLayout.setOnClickListener {
            if(!activity.getManager().listAlreadyOpened()) {
                activity.getManager().setListOpened()
                when (list.getListType()) {
                    toDoListRef -> activity.openToDoList(list.getListId(), task.getTaskId())
                    progressListRef -> activity.openProgressList(list.getListId(), task.getTaskId())
                    routineListRef -> activity.openRoutineList(list.getListId(), task.getTaskId())
                    bulletedListRef -> activity.openBulletedList(list.getListId(), task.getTaskId())
                }
            }
        }

        if(task.isCompleted()) moveToCompleted(false)
        activity.searchedItemsContainer.addView(view)

        itemViewMap[getTaskKey(task)] = view
    }

    @Suppress("DEPRECATION")
    private fun initListTitleLayoutAnimationListener(view: View) {
        val handler = Handler()
        var viewBounds = Rect()
        view.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    itemViewIsPressedMap[view] = true
                    viewBounds = Rect(v.left, v.top, v.right, v.bottom)
                    handler.postDelayed({
                        val listTitleViewIsPressed = itemViewIsPressedMap[view]
                        if(listTitleViewIsPressed != null && listTitleViewIsPressed) {
                            itemViewIsHighlightedMap[view] = true
                            animateButton(view, true)
                        }
                    }, TRANSITION_DELAY)
                }
                MotionEvent.ACTION_UP -> {
                    val listTitleViewIsPressed = itemViewIsPressedMap[view]
                    if(listTitleViewIsPressed != null && listTitleViewIsPressed &&
                        viewBounds.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                        itemViewIsHighlightedMap[view] = false
                        itemViewIsPressedMap[view] = false
                        animateButton(view, false)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val listTitleViewIsPressed = itemViewIsPressedMap[view]
                    if(listTitleViewIsPressed != null && listTitleViewIsPressed &&
                        !viewBounds.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                        itemViewIsPressedMap[view] = false

                        val listTitleViewIsHighlighted = itemViewIsHighlightedMap[view]
                        if(listTitleViewIsHighlighted != null && listTitleViewIsHighlighted) {
                            itemViewIsHighlightedMap[view] = false
                            animateButton(view, false)
                        }
                    }
                }
            }
            false
        }
    }

    fun releaseItemViews() {
        fun releaseTaskView(itemLayout: LinearLayout) {
            val viewIsHighlighted = itemViewIsHighlightedMap[itemLayout]
            if(viewIsHighlighted != null && viewIsHighlighted) {
                itemViewIsHighlightedMap[itemLayout] = false
                animateButton(itemLayout, false)
            }

            val viewIsPressed = itemViewIsPressedMap[itemLayout]
            if(viewIsPressed != null && viewIsPressed)
                itemViewIsPressedMap[itemLayout] = false
        }

        for(view in activity.searchedItemsContainer.iterator()) {
            val listTitleLayout: LinearLayout? = view.findViewById(R.id.listTitleLayout)
            if(listTitleLayout != null) releaseTaskView(listTitleLayout)

            val taskLayout: LinearLayout? = view.findViewById(R.id.taskLayout)
            if(taskLayout != null) releaseTaskView(taskLayout)
        }
    }

    fun updateFilterIcon(checkedFilterOptions: ArrayList<Boolean>) {
        var searchFiltered = false
        for(optionChecked in checkedFilterOptions)
            if(optionChecked) {
                searchFiltered = true
                break
            }
        val filterIconCode = if(searchFiltered)
            R.drawable.ic_filter_list_white else R.drawable.ic_filter_list_gray
        activity.filterIcon.setImageResource(filterIconCode)
    }

    fun resetSearchPage(animate: Boolean = false) {
        itemViewMap.clear()
        itemViewIsPressedMap.clear()
        itemViewIsHighlightedMap.clear()
        activity.searchedItemsContainer.removeAllViews()
        activity.getManager().updateCurrentDate()
        activity.getManager().applySearch(animate = animate)
    }
}