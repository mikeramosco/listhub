package com.justanotherdeveloper.listhub

import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class HomeFragmentManager(private val fragment: HomeFragment,
                          private val activity: HomeActivity):
    TaskEditor(activity, activity.getDatabase()) {

    private lateinit var dateRanges: ArrayList<DateRange>
    private lateinit var sectionFilters: ArrayList<ArrayList<Boolean>>

    private var currentSectionIndex = 0
    private var currentDate = getTodaysDate()

    fun loadHomeSectionsItems() {
        fragment.getHomeView().updateShowDetailsOptionState()
        val database = activity.getDatabase()
        if(!database.dateRangesSaved()) initDateRanges()
        dateRanges = database.getDateRanges()
        if(!database.sectionFiltersSaved())
            database.initSectionFilters()
        sectionFilters = database.getSectionFilters()
        loadSection()
    }

    fun setDateRange(startDate: Calendar?, endDate: Calendar?,
                     sectionIndex: Int = currentSectionIndex) {
        val dateRange = dateRanges[sectionIndex]
        dateRange.setDateRange(startDate, endDate)
        fragment.getDatabase().updateDateRange(dateRange, sectionIndex)
        beginTransition(fragment.homeFragmentParent)
        loadSection()
    }

    fun setSectionFilter(checkedFilterOptions: ArrayList<Boolean>,
                         sectionIndex: Int = currentSectionIndex) {
        sectionFilters[sectionIndex] = checkedFilterOptions
        fragment.getDatabase().updateSectionFilter(
            sectionFilters[sectionIndex], sectionIndex)
        beginTransition(fragment.homeFragmentParent)
        loadSection()
    }

    fun getDateRange(sectionIndex: Int = currentSectionIndex): DateRange {
        return dateRanges[sectionIndex]
    }

    fun getSectionFilter(sectionIndex: Int = currentSectionIndex): ArrayList<Boolean> {
        return sectionFilters[sectionIndex]
    }

    private fun List.isRoutineListAndDatedWithinRange(): Boolean {
        if(getListType() != routineListRef) return false
        val routineList = activity.getDatabase().getRoutineList(getListId())
        if(!routineList.hasDate() && !routineList.isRepeating()) return false
        val startDateToCheck = dateRanges[0].getStartDate()?: getTodaysDate()
        return routineList.getDueDate(copyDate(startDateToCheck))
            ?.isWithinDates(dateRanges[0])?: false
    }

    private fun listTypeIncluded(listType: String, sectionFiltered: Boolean): Boolean {
        if(sectionFiltered) {
            when(listType) {
                toDoListRef -> if(!getSectionFilter()[0]) return false
                progressListRef -> if(!getSectionFilter()[1]) return false
                routineListRef -> if(!getSectionFilter()[2]) return false
                bulletedListRef -> if(!getSectionFilter()[3]) return false
            }
        }
        return true
    }

    private fun listItemTypeIncluded(listType: String, sectionFiltered: Boolean): Boolean {
        if(sectionFiltered) {
            when(listType) {
                toDoListRef -> if(!getSectionFilter()[4]) return false
                progressListRef -> if(!getSectionFilter()[5]) return false
                routineListRef -> if(!getSectionFilter()[6]) return false
                bulletedListRef -> if(!getSectionFilter()[7]) return false
            }
        }
        return true
    }

    private fun listIsInSection(list: List, sectionIndex: Int): Boolean {
        return when(sectionIndex) {
            calendarIndex -> list
                .isRoutineListAndDatedWithinRange()
            importantIndex -> list.isStarred() && list.getDateCreated()
                .isWithinDates(dateRanges[1])
            recentlyAddedIndex -> list.getDateCreated()
                .isWithinDates(dateRanges[2])
            else -> false
        }
    }

    private fun searchTasksToDisplay(list: List, database: ListsDatabase,
                                     tasksToDisplay: ArrayList<Task>, sectionIndex: Int) {
        when(list.getListType()) {
            toDoListRef -> searchToDoListTasksToDisplay(
                database.getToDoList(list.getListId()),
                tasksToDisplay, sectionIndex)
            progressListRef -> searchProgressListTasksToDisplay(
                database.getProgressList(list.getListId()),
                tasksToDisplay, sectionIndex)
            routineListRef -> searchRoutineListTasksToDisplay(
                database.getRoutineList(list.getListId()),
                tasksToDisplay, sectionIndex)
            else -> searchBulletedListTasksToDisplay(
                database.getBulletedList(list.getListId()),
                tasksToDisplay, sectionIndex)
        }
    }

    private fun taskIsInSection(task: Task, sectionIndex: Int,
                                isBulletpoint: Boolean = false): Boolean {
        if(task.isCompleted() && !isBulletpoint) {
            return if(sectionIndex == completedIndex)
                task.getDateCompleted()?.isWithinDates(dateRanges[4]) ?: false
            else false
        }
        return when(sectionIndex) {
            calendarIndex -> {
                val startDateToCheck = dateRanges[0]
                    .getStartDate()?: getTodaysDate()
                task.hasDueDate() && task.getDueDate(copyDate(startDateToCheck))
                    ?.isWithinDates(dateRanges[0])?: false
            }
            importantIndex -> task.isStarred() && task.getDateCreated()
                .isWithinDates(dateRanges[1])
            recentlyAddedIndex -> task.getDateCreated()
                .isWithinDates(dateRanges[2])
            rewardsIndex -> !isBulletpoint && task.isReward() && task.getDateCreated()
                .isWithinDates(dateRanges[3])
            else -> false
        }
    }

    private fun searchToDoListTasksToDisplay(toDoList: ToDoList,
                                             tasksToDisplay: ArrayList<Task>,
                                             sectionIndex: Int) {
        for(taskId in toDoList.getCompletedTasks().asReversed()) {
            val task = toDoList.getTask(taskId)
            if (task != null && taskIsInSection(task, sectionIndex))
                tasksToDisplay.add(task)
        }

        for(taskId in toDoList.getCurrentTasks().asReversed()) {
            val task = toDoList.getTask(taskId)
            if (task != null && taskIsInSection(task, sectionIndex))
                tasksToDisplay.add(task)
        }
    }

    private fun searchProgressListTasksToDisplay(progressList: ProgressList,
                                                 tasksToDisplay: ArrayList<Task>,
                                                 sectionIndex: Int) {
        for(taskId in progressList.getCompletedTasks().asReversed()) {
            val task = progressList.getTask(taskId)
            if (task != null && taskIsInSection(task, sectionIndex))
                tasksToDisplay.add(task)
        }

        for(taskId in progressList.getCurrentTasks().asReversed()) {
            val task = progressList.getTask(taskId)
            if (task != null && taskIsInSection(task, sectionIndex))
                tasksToDisplay.add(task)
        }

        for(section in progressList.getListSections().asReversed())
            for(taskId in section.asReversed()) {
                val task = progressList.getTask(taskId)
                if (task != null && taskIsInSection(task, sectionIndex))
                    tasksToDisplay.add(task)
            }
    }

    private fun searchRoutineListTasksToDisplay(routineList: RoutineList,
                                                tasksToDisplay: ArrayList<Task>,
                                                sectionIndex: Int) {
        for(taskId in routineList.getCompletedTasks().asReversed()) {
            val task = routineList.getTask(taskId)
            if (task != null && taskIsInSection(task, sectionIndex))
                tasksToDisplay.add(task)
        }

        for(taskId in routineList.getCurrentTasks().asReversed()) {
            val task = routineList.getTask(taskId)
            if (task != null && taskIsInSection(task, sectionIndex))
                tasksToDisplay.add(task)
        }
    }

    private fun searchBulletedListTasksToDisplay(bulletedList: BulletedList,
                                                 tasksToDisplay: ArrayList<Task>,
                                                 sectionIndex: Int) {
        for(taskId in bulletedList.getCurrentTasks().asReversed()) {
            val task = bulletedList.getTask(taskId)
            if (task != null && taskIsInSection(task, sectionIndex, isBulletpoint = true))
                tasksToDisplay.add(task)
        }
    }

    fun sectionFiltered(): Boolean {
        var sectionFiltered = false
        val sectionFilter = getSectionFilter()
        if(currentSectionIndex == rewardsIndex) return false
        for(optionChecked in sectionFilter)
            if(optionChecked) {
                sectionFiltered = true
                break
            }
        return sectionFiltered
    }

    fun updateCurrentDate() {
        currentDate = getTodaysDate()
    }

    fun sectionReloadedIfDateIsOutdated(): Boolean {
        return if(!datesAreTheSame(currentDate, getTodaysDate())) {
            activity.getListFragment().getListsView().reloadLists()
            fragment.getHomeView().reloadSection()
            true
        } else false
    }

    fun reloadSectionIfCalendarShown() {
        if(currentSectionIndex == calendarIndex) {
            beginTransition(fragment.homeFragmentParent)
            loadSection()
        }
    }

    fun loadSection() {
        if(sectionReloadedIfDateIsOutdated()) return
        val sectionIndex = currentSectionIndex
        val listsToDisplay = ArrayList<List>()
        val tasksToDisplay = ArrayList<Task>()

        val database = activity.getDatabase()
        val listIds = database.getListIds()
        for(id in listIds.asReversed()) {
            val list = database.getList(id)
            if(!list.isArchived()) {
                val sectionFiltered = sectionFiltered()
                if (listTypeIncluded(list.getListType(), sectionFiltered) &&
                    listIsInSection(list, sectionIndex)) listsToDisplay.add(list)
                if (listItemTypeIncluded(list.getListType(), sectionFiltered))
                    searchTasksToDisplay(list, database, tasksToDisplay, sectionIndex)
            }
        }

        displayItems(listsToDisplay, tasksToDisplay, sectionIndex)
    }

    fun setCurrentSectionIndex(sectionIndex: Int) {
        currentSectionIndex = sectionIndex
    }

    fun getCurrentSectionIndex(): Int {
        return currentSectionIndex
    }

    private fun displayItems(listsToDisplay: ArrayList<List>,
                             tasksToDisplay: ArrayList<Task>,
                             sectionIndex: Int) {
        when(sectionIndex) {
            0 -> fragment.getHomeView().displayCalendarSection(listsToDisplay, tasksToDisplay)
            1 -> fragment.getHomeView().displayImportantSection(listsToDisplay, tasksToDisplay)
            2 -> fragment.getHomeView().displayRecentlyAddedSection(listsToDisplay, tasksToDisplay)
            3 -> fragment.getHomeView().displayRewardsSection(tasksToDisplay)
            4 -> fragment.getHomeView().displayCompletedSection(tasksToDisplay)
        }
    }

    private fun initDateRanges() {
        val dateRanges = ArrayList<DateRange>()
        dateRanges.add(DateRange().initDateRange(true))
        dateRanges.add(DateRange().initDateRange())
        dateRanges.add(DateRange().initDateRange())
        dateRanges.add(DateRange().initDateRange())
        dateRanges.add(DateRange().initDateRange())
        activity.getDatabase()
            .saveDateRanges(dateRanges)
    }

    private fun getTaskFromKey(keyString: String): Task? {
        val keyContents = keyString.split("\t")
        val listId = keyContents[0].toInt()
        val taskId = keyContents[1].toInt()
        val list = fragment.getDatabase().getList(listId)
        return when(list.getListType()) {
            toDoListRef -> fragment.getDatabase().getToDoList(listId).getTask(taskId)
            progressListRef -> fragment.getDatabase().getProgressList(listId).getTask(taskId)
            routineListRef -> fragment.getDatabase().getRoutineList(listId).getTask(taskId)
            bulletedListRef -> fragment.getDatabase().getBulletedList(listId).getTask(taskId)
            else -> null
        }
    }

    private fun ArrayList<String>.prioritizeImportantItems() {
        val importantItems = ArrayList<String>()
        for(key in this) {
            if(isTaskKey(key)) {
                val task = getTaskFromKey(key)
                if(task != null && task.isStarred()) importantItems.add(key)
            } else {
                val list = activity.getDatabase().getList(key.toInt())
                if(list.isStarred()) importantItems.add(key)
            }
        }

        for(key in importantItems) remove(key)
        for(key in this) importantItems.add(key)
        clear()
        for(key in importantItems) add(key)
    }

    fun getSortedItemsOrder(tasksToDisplay: ArrayList<Task>,
                            listsToDisplay: ArrayList<List>?): ArrayList<String> {
        val itemDateMap = HashMap<String, Calendar>()
        val itemKeys = ArrayList<String>()
        val sortedItemsOrder = ArrayList<String>()

        for(task in tasksToDisplay) {
            val key = getTaskKey(task)
            val date = when(currentSectionIndex) {
                calendarIndex -> {
                    val startDateToCheck = getDateRange()
                        .getStartDate()?: getTodaysDate()
                    task.getDueDate(copyDate(startDateToCheck))
                }
                completedIndex -> task.getDateCompleted()
                else -> task.getDateCreated()
            }
            if(date != null) {
                itemDateMap[key] = date
                itemKeys.add(key)
            }
        }

        if(listsToDisplay != null) for(list in listsToDisplay) {
            val key = list.getListId().toString()
            val date = when(currentSectionIndex) {
                calendarIndex -> {
                    val startDateToCheck = getDateRange()
                        .getStartDate()?: getTodaysDate()
                    activity.getDatabase()
                        .getRoutineList(list.getListId())
                        .getDueDate(copyDate(startDateToCheck))
                }
                else -> list.getDateCreated()
            }
            if(date != null) {
                itemDateMap[key] = date
                itemKeys.add(key)
            }
        }

        for(keyToAdd in itemKeys) {
            var taskAdded = false
            for ((index, key) in sortedItemsOrder.withIndex()) {
                val keyDate = itemDateMap[key]?: getTodaysDate()
                if (currentSectionIndex == calendarIndex) {
                    val keyToAddDate = itemDateMap[keyToAdd]?: getTodaysDate()
                    if(datesAreTheSame(keyDate, keyToAddDate)) {

                        val keyToAddHasTime = if (isTaskKey(keyToAdd))
                            fragment.getHomeView().getTaskOfKey(keyToAdd)
                                ?.hasTime() ?: false else false

                        val keyHasTime = if (isTaskKey(key))
                            fragment.getHomeView().getTaskOfKey(key)
                                ?.hasTime() ?: false else false

                        if (!keyHasTime && keyToAddHasTime) {
                            sortedItemsOrder.add(index, keyToAdd)
                            taskAdded = true
                            break
                        } else if (keyHasTime == keyToAddHasTime) {
                            // in order by time
                            if (keyToAddDate.comesBefore(keyDate)) {
                                sortedItemsOrder.add(index, keyToAdd)
                                taskAdded = true
                                break
                            }
                        }
                    } else {
                        // reverse order by date
                        if (keyToAddDate.comesAfter(keyDate)) {
                            sortedItemsOrder.add(index, keyToAdd)
                            taskAdded = true
                            break
                        }
                    }
                } else {
                    val keyToAddDate = itemDateMap[keyToAdd]?: getTodaysDate()
                    if (keyToAddDate.comesAfter(keyDate)) {
                        sortedItemsOrder.add(index, keyToAdd)
                        taskAdded = true
                        break
                    }
                }
            }
            if (!taskAdded)
                sortedItemsOrder.add(keyToAdd)
        }

        if(currentSectionIndex == calendarIndex)
            sortedItemsOrder.prioritizeImportantItems()

        return sortedItemsOrder
    }
}