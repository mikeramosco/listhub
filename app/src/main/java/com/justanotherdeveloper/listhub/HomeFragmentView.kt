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
import kotlinx.android.synthetic.main.fragment_home.*

@SuppressLint("InflateParams")
class HomeFragmentView(private val fragment: HomeFragment,
                       private val activity: HomeActivity) {

    private val itemViewMap = HashMap<String, View>()
    private val itemViewIsPressedMap = HashMap<View, Boolean>()
    private val itemViewIsHighlightedMap = HashMap<View, Boolean>()

    private val keyListMap = HashMap<String, List>()
    private val keyTaskMap = HashMap<String, Task>()
    private var sortedItemsOrder = ArrayList<String>()

    private var indexOfNextItem = 0

    fun getTaskOfKey(key: String): Task? {
        return keyTaskMap[key]
    }

    fun toggleShowDetails() {
        beginTransition(fragment.homeFragmentParent)
        val detailsShown = fragment.getDatabase().homeDetailsShown()
        updateShowDetailsOptionState(detailsShown)

        fun toggleDetailsIconVisibility(view: View) {
            val detailsIcon = view.findViewById<ImageView>(R.id.detailsIcon)
            detailsIcon.visibility = if(detailsShown)
                View.VISIBLE else View.GONE
        }

        for(view in fragment.itemsContainer.iterator())
            toggleDetailsIconVisibility(view)
    }

    fun updateShowDetailsOptionState(
        detailsShown: Boolean = fragment.getDatabase().homeDetailsShown()) {
        val showDetailsCode = if(detailsShown)
            R.string.hideDetailsString else R.string.showDetailsString
        fragment.showDetailsText.text = fragment.getString(showDetailsCode)
    }

    private fun clearDisplayedItems() {
        fragment.noItemsText.visibility = View.VISIBLE
        fragment.scrollViewLoadingCircle.visibility = View.GONE
        for(view in activity.itemsContainer.iterator())
            view.visibility = View.GONE
    }

    private fun displayList(list: List, detailsShown: Boolean) {
        fragment.noItemsText.visibility = View.GONE
        val key = list.getListId().toString()
        if(itemViewMap.containsKey(key)) {
            val view = itemViewMap[key]
            view?.visibility = View.VISIBLE
            fragment.itemsContainer.removeView(view)
            fragment.itemsContainer.addView(view)
        } else inflateListView(list, detailsShown)
    }

    fun displayTask(task: Task,
                    detailsShown: Boolean =
                        fragment.getDatabase().homeDetailsShown(),
                    isNew: Boolean = false) {
        if(isNew && fragment.getManager()
                .sectionReloadedIfDateIsOutdated()) return
        fragment.noItemsText.visibility = View.GONE
        val key = getTaskKey(task)
        if(!isNew && itemViewMap.containsKey(key)) {
            val view = itemViewMap[key]
            view?.visibility = View.VISIBLE
            fragment.itemsContainer.removeView(view)
            fragment.itemsContainer.addView(view)
        } else inflateTaskView(task, detailsShown, isNew)
    }

    private fun inflateListView(list: List, detailsShown: Boolean) {
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
            val routineList = fragment.getDatabase()
                .getRoutineList(list.getListId())
            hasDate = routineList.hasDate() || routineList.isRepeating()
            if(hasDate) {
                dateIcon.visibility = View.VISIBLE
                dateText.visibility = View.VISIBLE
                var dateString = ""
                if(routineList.hasDate()) {
                    val date = routineList.getDate()?: getTodaysDate()
                    dateString = fragment.requireActivity().getRecencyText(date)
                } else if(routineList.isRepeating()) {
                    dateIcon.setImageResource(R.drawable.ic_autorenew_gray)
                    dateString = routineList.getRepeatingDaysString(
                        fragment.requireContext())
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

        if(detailsShown) detailsIcon.visibility = View.VISIBLE
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
                    if(fragment.getManager().getCurrentSectionIndex() == importantIndex) {
                        beginTransition(fragment.homeFragmentParent)
                        view.visibility = View.GONE
                    }
                    fragment.getManager().reloadSectionIfCalendarShown()
                    activity.getListFragment().getListsView().reloadLists()
                }
                else -> {
                    list.listUpdated()
                    list.toggleStar()
                    activity.getDatabase().toggleStarOfList(list)
                    activity.getDatabase().addListToFavorites(list.getListId())
                    toggleListStar()
                    fragment.getManager().reloadSectionIfCalendarShown()
                    activity.getListFragment().getListsView().reloadLists()
                }
            }
        }

        if(list.isStarred()) star.setColoredImageResource(R.drawable.ic_star_custom, colorTheme)

        initListTitleLayoutAnimationListener(listTitleLayout)
        listTitleLayout.setOnClickListener {
            if(!activity.listAlreadyOpened()) {
                activity.setListOpened()
                when (list.getListType()) {
                    toDoListRef -> activity.openToDoList(list.getListId())
                    progressListRef -> activity.openProgressList(list.getListId())
                    routineListRef -> activity.openRoutineList(list.getListId())
                    bulletedListRef -> activity.openBulletedList(list.getListId())
                }
            }

        }

        activity.itemsContainer.addView(view)

        itemViewMap[list.getListId().toString()] = view
    }

    private fun inflateTaskView(task: Task, detailsShown: Boolean, isNew: Boolean) {
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

        if(detailsShown) detailsIcon.visibility = View.VISIBLE
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
            beginTransition(fragment.homeFragmentParent)
            taskText.setTextColor(Color.WHITE)
            taskText.paintFlags = 0
            checkbox.setImageResource(R.drawable.ic_radio_button_unchecked_gray)
        }

        fun moveToCompleted(animate: Boolean = true) {
            if(list.getListType() == bulletedListRef) return
            if(animate) beginTransition(fragment.homeFragmentParent)
            taskText.setTextColor(
                ContextCompat.getColor(
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
                    fragment.getManager().removeTaskFromCompleted(task, list.getListType())
                    removeFromCompleted()
                    beginTransition(fragment.homeFragmentParent)
                    view.visibility = View.GONE
                }
                else -> {
                    fragment.getManager().markTaskCompleted(task, list.getListType())
                    moveToCompleted()
                    beginTransition(fragment.homeFragmentParent)
                    if(task.hasReward() || task.isRepeating())
                        fragment.getManager().loadSection()
                    else view.visibility = View.GONE
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
                    fragment.getManager().removeTaskFromStarred(task, list.getListType())
                    toggleTaskStar()
                    if(fragment.getManager().getCurrentSectionIndex() == importantIndex) {
                        beginTransition(fragment.homeFragmentParent)
                        view.visibility = View.GONE
                    }
                    fragment.getManager().reloadSectionIfCalendarShown()
                }
                else -> {
                    fragment.getManager().addTaskToStarred(task, list.getListType())
                    toggleTaskStar()
                    fragment.getManager().reloadSectionIfCalendarShown()
                }
            }
        }

        if(task.isStarred()) star.setColoredImageResource(R.drawable.ic_star_custom, colorTheme)

        initListTitleLayoutAnimationListener(taskLayout)
        taskLayout.setOnClickListener {
            if(!activity.listAlreadyOpened()) {
                activity.setListOpened()
                when (list.getListType()) {
                    toDoListRef -> activity.openToDoList(list.getListId(), task.getTaskId())
                    progressListRef -> activity.openProgressList(list.getListId(), task.getTaskId())
                    routineListRef -> activity.openRoutineList(list.getListId(), task.getTaskId())
                    bulletedListRef -> activity.openBulletedList(list.getListId(), task.getTaskId())
                }
            }
        }

        if(task.isCompleted()) moveToCompleted(false)
        if(isNew) activity.itemsContainer.addView(view, 0)
        else activity.itemsContainer.addView(view)

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

    private fun displayItemsInSortedOrder(tasksToDisplay: ArrayList<Task>,
                                          listsToDisplay: ArrayList<List>? = null) {
        val detailsShown = fragment.getDatabase().homeDetailsShown()
        keyListMap.clear()
        keyTaskMap.clear()
        if(listsToDisplay != null)
            for(list in listsToDisplay)
                keyListMap[list.getListId().toString()] = list
        for(task in tasksToDisplay)
            keyTaskMap[getTaskKey(task)] = task
        sortedItemsOrder = fragment.getManager()
            .getSortedItemsOrder(tasksToDisplay, listsToDisplay)
        indexOfNextItem = 0
        for((index, key) in sortedItemsOrder.withIndex()) {
            if(index == ITEM_COUNT_FIRST_LOAD) {
                indexOfNextItem = index
                fragment.scrollViewLoadingCircle.visibility = View.VISIBLE
                break
            } else if(key.contains("\t")) {
                val task = keyTaskMap[key]
                if(task != null) displayTask(task, detailsShown)
            } else {
                val list = keyListMap[key]
                if(list != null) displayList(list, detailsShown)
            }
            if(index == sortedItemsOrder.lastIndex)
                indexOfNextItem = sortedItemsOrder.size
        }
    }

    fun loadMoreItems() {
        val detailsShown = fragment.getDatabase().homeDetailsShown()
        if(indexOfNextItem == sortedItemsOrder.size) return
        val indexToStopLoading = indexOfNextItem + ITEM_COUNT_PER_INTERVAL
        while(true) {
            val key = sortedItemsOrder[indexOfNextItem++]
            if(key.contains("\t")) {
                val task = keyTaskMap[key]
                if(task != null) displayTask(task, detailsShown)
            } else {
                val list = keyListMap[key]
                if(list != null) displayList(list, detailsShown)
            }
            if(indexOfNextItem == sortedItemsOrder.size) {
                fragment.scrollViewLoadingCircle.visibility = View.GONE
                break
            }
            if(indexOfNextItem == indexToStopLoading) break
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

        for(view in activity.itemsContainer.iterator()) {
            val listTitleLayout: LinearLayout? = view.findViewById(R.id.listTitleLayout)
            if(listTitleLayout != null) releaseTaskView(listTitleLayout)

            val taskLayout: LinearLayout? = view.findViewById(R.id.taskLayout)
            if(taskLayout != null) releaseTaskView(taskLayout)
        }
    }

    fun reloadSection() {
        itemViewMap.clear()
        itemViewIsPressedMap.clear()
        itemViewIsHighlightedMap.clear()
        activity.itemsContainer.removeAllViews()
        fragment.getManager().updateCurrentDate()
        fragment.getManager().loadSection()
    }

    private fun getDateRangeIconCode(): Int {
        val dateRange = fragment.getManager().getDateRange()
        val startDate = dateRange.getStartDate()
        val endDate = dateRange.getEndDate()
        return if(startDate == null || endDate == null)
            R.drawable.ic_access_time_white
        else if(datesAreTheSame(startDate, endDate))
            R.drawable.ic_date_white
        else R.drawable.ic_date_range_white
    }

    private fun getFilterSectionString(sectionsFiltered: Boolean): String {
        val filterSectionStringCode = when(fragment.getManager().getCurrentSectionIndex()) {
            calendarIndex -> if(!sectionsFiltered)
                R.string.filterCalendarString else R.string.calendarFilteredString
            importantIndex -> if(!sectionsFiltered)
                R.string.filterImportantString else R.string.importantFilteredString
            recentlyAddedIndex -> if(!sectionsFiltered)
                R.string.filterRecentlyAddedString else R.string.recentlyAddedFilteredString
            rewardsIndex -> if(!sectionsFiltered)
                R.string.filterRewardsString else R.string.rewardsFilteredString
            completedIndex -> if(!sectionsFiltered)
                R.string.filterCompletedString else R.string.completedFilteredString
            else -> return ""
        }
        return activity.getString(filterSectionStringCode)
    }

    private fun updateSectionFilterOptionLayout() {
        fragment.filterSectionOption.visibility =
            if(fragment.getManager().getCurrentSectionIndex() == rewardsIndex)
                View.GONE else View.VISIBLE
        val sectionFiltered = fragment.getManager().sectionFiltered()
        if(sectionFiltered) {
            fragment.filterSectionText.text = getFilterSectionString(sectionFiltered)
            fragment.filterSectionOption.background = ContextCompat.getDrawable(activity,
                R.drawable.transition_layout_rounded_border_selected)
        } else {
            fragment.filterSectionText.text = getFilterSectionString(sectionFiltered)
            fragment.filterSectionOption.background = ContextCompat.getDrawable(activity,
                R.drawable.transition_layout_rounded_border)
        }
    }

    fun displayCalendarSection(listsToDisplay: ArrayList<List>,
                               tasksToDisplay: ArrayList<Task>) {
        fragment.sectionIcon.setColoredImageResource(
            R.drawable.ic_date_custom, activity.getColorTheme())
        fragment.sectionText.text = activity.getString(R.string.calendarString)
        fragment.dateRangeText.text = activity.getDateRangeString(
            fragment.getManager().getDateRange(), forSetDate = true)
        fragment.dateRangeIcon.setImageResource(getDateRangeIconCode())
        updateSectionFilterOptionLayout()
        clearDisplayedItems()
        displayItemsInSortedOrder(tasksToDisplay, listsToDisplay)
    }

    fun displayImportantSection(listsToDisplay: ArrayList<List>,
                                tasksToDisplay: ArrayList<Task>) {
        fragment.sectionIcon.setColoredImageResource(
            R.drawable.ic_star_custom, activity.getColorTheme())
        fragment.sectionText.text = activity.getString(R.string.importantCapsString)
        fragment.dateRangeText.text = activity.getDateRangeString(
            fragment.getManager().getDateRange(), forAddedDate = true)
        fragment.dateRangeIcon.setImageResource(getDateRangeIconCode())
        updateSectionFilterOptionLayout()
        clearDisplayedItems()
        displayItemsInSortedOrder(tasksToDisplay, listsToDisplay)
    }

    fun displayRecentlyAddedSection(listsToDisplay: ArrayList<List>,
                                    tasksToDisplay: ArrayList<Task>) {
        fragment.sectionIcon.setColoredImageResource(
            R.drawable.ic_add_circle_custom, activity.getColorTheme())
        fragment.sectionText.text = activity.getString(R.string.recentlyAddedString)
        fragment.dateRangeText.text = activity.getDateRangeString(
            fragment.getManager().getDateRange(), forAddedDate = true)
        fragment.dateRangeIcon.setImageResource(getDateRangeIconCode())
        updateSectionFilterOptionLayout()
        clearDisplayedItems()
        displayItemsInSortedOrder(tasksToDisplay, listsToDisplay)
    }

    fun displayRewardsSection(tasksToDisplay: ArrayList<Task>) {
        fragment.sectionIcon.setColoredImageResource(
            R.drawable.ic_card_giftcard_custom, activity.getColorTheme())
        fragment.sectionText.text = activity.getString(R.string.rewardsString)
        fragment.dateRangeText.text = activity.getDateRangeString(
            fragment.getManager().getDateRange(), forAddedDate = true)
        fragment.dateRangeIcon.setImageResource(getDateRangeIconCode())
        updateSectionFilterOptionLayout()
        clearDisplayedItems()
        displayItemsInSortedOrder(tasksToDisplay)
    }

    fun displayCompletedSection(tasksToDisplay: ArrayList<Task>) {
        fragment.sectionIcon.setColoredImageResource(
            R.drawable.ic_check_circle_custom, activity.getColorTheme())
        fragment.sectionText.text = activity.getString(R.string.completedCapsString)
        fragment.dateRangeText.text = activity.getDateRangeString(
            fragment.getManager().getDateRange(), forCompletedDate = true)
        fragment.dateRangeIcon.setImageResource(getDateRangeIconCode())
        updateSectionFilterOptionLayout()
        clearDisplayedItems()
        displayItemsInSortedOrder(tasksToDisplay)
    }
}