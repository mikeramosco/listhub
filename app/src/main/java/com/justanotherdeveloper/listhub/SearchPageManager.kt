package com.justanotherdeveloper.listhub

import kotlinx.android.synthetic.main.activity_search_page.*
import java.util.*
import kotlin.collections.ArrayList

class SearchPageManager(private val activity: SearchPageActivity):
    TaskEditor(activity, activity.getDatabase()) {

    private var checkedFilterOptions = ArrayList<Boolean>()
    private var currentDate = getTodaysDate()
    private var listOpened = false

    fun setCheckedFilterOptions(checkedFilterOptions: ArrayList<Boolean>) {
        this.checkedFilterOptions = checkedFilterOptions
        activity.getView().updateFilterIcon(checkedFilterOptions)
    }

    fun getCheckedFilterOptions(): ArrayList<Boolean> {
        if(checkedFilterOptions.size == 0)
            for(i in 0 until 6)
                checkedFilterOptions.add(false)
        return checkedFilterOptions
    }

    private fun listsOnly(): Boolean {
        return getCheckedFilterOptions()[0]
    }

    private fun searchLabels(): Boolean {
        return getCheckedFilterOptions()[1]
    }

    private fun searchNotes(): Boolean {
        return getCheckedFilterOptions()[2]
    }

    private fun searchRewards(): Boolean {
        return getCheckedFilterOptions()[3]
    }

    private fun hideCompleted(): Boolean {
        return getCheckedFilterOptions()[4]
    }

    private fun hideArchived(): Boolean {
        return getCheckedFilterOptions()[5]
    }

    fun listAlreadyOpened(): Boolean {
        return listOpened
    }

    fun setListOpened(listOpened: Boolean = true) {
        this.listOpened = listOpened
    }

    fun updateCurrentDate() {
        currentDate = getTodaysDate()
    }

    fun searchResetIfDateIsOutdated(animate: Boolean = false): Boolean {
        return if(!datesAreTheSame(currentDate, getTodaysDate())) {
            activity.getView().resetSearchPage(animate = animate)
            true
        } else false
    }

    fun applySearch(animate: Boolean = true) {
        if(searchResetIfDateIsOutdated(animate)) return
        val searchString = activity.searchField.text.toString()
        if(searchString.isEmpty()) {
            if(animate) beginTransition(activity.searchPageParent)
            activity.getView().clearDisplayedItems(false)
            return
        }
        val listsToDisplay = ArrayList<List>()
        val tasksToDisplay = ArrayList<Task>()

        val database = activity.getDatabase()
        val listIds = database.getListIds()
        for(id in listIds) {
            val list = database.getList(id)
            val showIfArchived = if(hideArchived())
                !list.isArchived() else true
            if(showIfArchived) {
                if (listIsSearched(list, searchString)) listsToDisplay.add(list)
                if (!listsOnly()) searchTasksToDisplay(list, database, searchString, tasksToDisplay)
            }
        }

        if(animate) beginTransition(activity.searchPageParent)
        activity.getView().displaySearchedItems(listsToDisplay, tasksToDisplay)
    }

    private fun listIsSearched(list: List, searchString: String): Boolean {
        if(isSearched(list.getTitle(), searchString)) return true
        if(searchLabels() && isSearched(list.getLabelsString(), searchString)) return true
        return false
    }

    private fun isSearched(text: String, searchString: String): Boolean {
        return text.toLowerCase(Locale.US).contains(searchString.toLowerCase(Locale.US))
    }

    private fun searchTasksToDisplay(list: List, database: ListsDatabase,
                                     searchString: String,
                                     tasksToDisplay: ArrayList<Task>) {
        when(list.getListType()) {
            toDoListRef -> searchToDoListTasksToDisplay(
                database.getToDoList(list.getListId()), searchString, tasksToDisplay)
            progressListRef -> searchProgressListTasksToDisplay(
                database.getProgressList(list.getListId()), searchString, tasksToDisplay)
            routineListRef -> searchRoutineListTasksToDisplay(
                database.getRoutineList(list.getListId()), searchString, tasksToDisplay)
            else -> searchBulletedListTasksToDisplay(
                database.getBulletedList(list.getListId()), searchString, tasksToDisplay)
        }
    }

    private fun taskIsSearched(task: Task, searchString: String, listType: String): Boolean {
        if(hideCompleted() && task.isCompleted() && listType != bulletedListRef) return false
        if(isSearched(task.getTask(), searchString)) return true
        if(searchNotes() && isSearched(task.getNote(), searchString)) return true
        if(searchRewards() && isSearched(task.getReward(), searchString)
            && listType != routineListRef && listType != bulletedListRef) return true
        return false
    }

    private fun searchToDoListTasksToDisplay(toDoList: ToDoList,
                                             searchString: String,
                                             tasksToDisplay: ArrayList<Task>) {
        for(taskId in toDoList.getCurrentTasks()) {
            val task = toDoList.getTask(taskId)
            if (task != null && taskIsSearched(task, searchString, toDoListRef))
                tasksToDisplay.add(task)
        }

        for(taskId in toDoList.getCompletedTasks()) {
            val task = toDoList.getTask(taskId)
            if (task != null && taskIsSearched(task, searchString, toDoListRef))
                tasksToDisplay.add(task)
        }
    }

    private fun searchProgressListTasksToDisplay(progressList: ProgressList,
                                                 searchString: String,
                                                 tasksToDisplay: ArrayList<Task>) {
        for(section in progressList.getListSections())
            for(taskId in section) {
                val task = progressList.getTask(taskId)
                if (task != null && taskIsSearched(task, searchString, progressListRef))
                    tasksToDisplay.add(task)
            }

        for(taskId in progressList.getCurrentTasks()) {
            val task = progressList.getTask(taskId)
            if (task != null && taskIsSearched(task, searchString, progressListRef))
                tasksToDisplay.add(task)
        }

        for(taskId in progressList.getCompletedTasks()) {
            val task = progressList.getTask(taskId)
            if (task != null && taskIsSearched(task, searchString, progressListRef))
                tasksToDisplay.add(task)
        }
    }

    private fun searchRoutineListTasksToDisplay(routineList: RoutineList,
                                                searchString: String,
                                                tasksToDisplay: ArrayList<Task>) {
        for(taskId in routineList.getCurrentTasks()) {
            val task = routineList.getTask(taskId)
            if (task != null && taskIsSearched(task, searchString, routineListRef))
                tasksToDisplay.add(task)
        }

        for(taskId in routineList.getCompletedTasks()) {
            val task = routineList.getTask(taskId)
            if (task != null && taskIsSearched(task, searchString, routineListRef))
                tasksToDisplay.add(task)
        }
    }

    private fun searchBulletedListTasksToDisplay(bulletedList: BulletedList,
                                                 searchString: String,
                                                 tasksToDisplay: ArrayList<Task>) {
        for(taskId in bulletedList.getCurrentTasks()) {
            val task = bulletedList.getTask(taskId)
            if (task != null && taskIsSearched(task, searchString, bulletedListRef))
                tasksToDisplay.add(task)
        }
    }


    fun getSortedItemsOrder(tasksToDisplay: ArrayList<Task>,
                            listsToDisplay: ArrayList<List>): ArrayList<String> {
        val itemDateMap = HashMap<String, Calendar>()
        val itemKeys = ArrayList<String>()
        val sortedItemsOrder = ArrayList<String>()

        for(task in tasksToDisplay) {
            val key = getTaskKey(task)
            val date = task.getDateCreated()
            itemDateMap[key] = date
            itemKeys.add(key)
        }

        for(list in listsToDisplay) {
            val key = list.getListId().toString()
            val date = list.getDateCreated()
            itemDateMap[key] = date
            itemKeys.add(key)
        }

        for(keyToAdd in itemKeys) {
            var taskAdded = false
            for ((index, key) in sortedItemsOrder.withIndex()) {
                val keyDate = itemDateMap[key]?: getTodaysDate()
                val keyToAddDate = itemDateMap[keyToAdd]?: getTodaysDate()
                if (keyToAddDate.comesAfter(keyDate)) {
                    sortedItemsOrder.add(index, keyToAdd)
                    taskAdded = true
                    break
                }
            }
            if (!taskAdded)
                sortedItemsOrder.add(keyToAdd)
        }

        return sortedItemsOrder
    }
}