package com.justanotherdeveloper.listhub

import android.content.Context
import java.lang.NullPointerException

class ListsDatabase(val context: Context) {

    private val tinyDB = TinyDB(context)

//    init { tinyDB.putBoolean(EXAMPLE_LISTS_ADDED, false) }

    fun createExampleLists() {
        CreateExampleLists(this)
        tinyDB.putBoolean(EXAMPLE_LISTS_ADDED, true)
    }

    fun exampleListsAdded(): Boolean {
        return try {
            tinyDB.getBoolean(EXAMPLE_LISTS_ADDED)
        } catch (e: NullPointerException) {
            false
        }
    }

    fun sectionFiltersSaved(): Boolean {
        return try {
            tinyDB.getListBoolean(FILTER_CALENDAR).size == 8
        } catch (e: NullPointerException) {
            false
        }
    }

    fun getSectionFilters(): ArrayList<ArrayList<Boolean>> {
        val sectionFilters = ArrayList<ArrayList<Boolean>>()
        sectionFilters.add(tinyDB.getListBoolean(FILTER_CALENDAR))
        sectionFilters.add(tinyDB.getListBoolean(FILTER_IMPORTANT))
        sectionFilters.add(tinyDB.getListBoolean(FILTER_RECENTLY_ADDED))
        sectionFilters.add(tinyDB.getListBoolean(FILTER_REWARDS))
        sectionFilters.add(tinyDB.getListBoolean(FILTER_COMPLETED))
        return sectionFilters
    }

    fun updateSectionFilter(sectionFilter: ArrayList<Boolean>, sectionIndex: Int) {
        val fileReference = when(sectionIndex) {
            calendarIndex -> FILTER_CALENDAR
            importantIndex -> FILTER_IMPORTANT
            recentlyAddedIndex -> FILTER_RECENTLY_ADDED
            rewardsIndex -> FILTER_REWARDS
            completedIndex -> FILTER_COMPLETED
            else -> ""
        }
        if(fileReference.isNotEmpty())
            tinyDB.putListBoolean(fileReference, sectionFilter)
    }

    fun initSectionFilters() {
        val sectionFilter = ArrayList<Boolean>()
        for(i in 0 until 8) sectionFilter.add(false)
        tinyDB.putListBoolean(FILTER_CALENDAR, sectionFilter)
        tinyDB.putListBoolean(FILTER_IMPORTANT, sectionFilter)
        tinyDB.putListBoolean(FILTER_RECENTLY_ADDED, sectionFilter)
        tinyDB.putListBoolean(FILTER_REWARDS, sectionFilter)
        tinyDB.putListBoolean(FILTER_COMPLETED, sectionFilter)
    }

    fun dateRangesSaved(): Boolean {
        try {
            tinyDB.getObject(DATE_RANGE_CALENDAR, DateRange::class.java)
        } catch (e: NullPointerException) {
            return false
        }
        return true
    }

    fun getDateRanges(): ArrayList<DateRange> {
        val dateRanges = ArrayList<DateRange>()
        dateRanges.add(tinyDB.getObject(DATE_RANGE_CALENDAR, DateRange::class.java))
        dateRanges.add(tinyDB.getObject(DATE_RANGE_IMPORTANT, DateRange::class.java))
        dateRanges.add(tinyDB.getObject(DATE_RANGE_RECENTLY_ADDED, DateRange::class.java))
        dateRanges.add(tinyDB.getObject(DATE_RANGE_REWARDS, DateRange::class.java))
        dateRanges.add(tinyDB.getObject(DATE_RANGE_COMPLETED, DateRange::class.java))
        return dateRanges
    }

    fun updateDateRange(dateRange: DateRange, sectionIndex: Int) {
        val fileReference = when(sectionIndex) {
            calendarIndex -> DATE_RANGE_CALENDAR
            importantIndex -> DATE_RANGE_IMPORTANT
            recentlyAddedIndex -> DATE_RANGE_RECENTLY_ADDED
            rewardsIndex -> DATE_RANGE_REWARDS
            completedIndex -> DATE_RANGE_COMPLETED
            else -> ""
        }
        if(fileReference.isNotEmpty())
            tinyDB.putObject(fileReference, dateRange)
    }

    fun saveDateRanges(dateRanges: ArrayList<DateRange>) {
        tinyDB.putObject(DATE_RANGE_CALENDAR, dateRanges[0])
        tinyDB.putObject(DATE_RANGE_IMPORTANT, dateRanges[1])
        tinyDB.putObject(DATE_RANGE_RECENTLY_ADDED, dateRanges[2])
        tinyDB.putObject(DATE_RANGE_REWARDS, dateRanges[3])
        tinyDB.putObject(DATE_RANGE_COMPLETED, dateRanges[4])
    }

    fun toggleHomeShowDetails() {
        val detailsShown = homeDetailsShown()
        tinyDB.putBoolean(HOME_DETAILS_SHOWN, !detailsShown)
    }

    fun toggleListsShowDetails() {
        val detailsShown = listsDetailsShown()
        tinyDB.putBoolean(LISTS_DETAILS_SHOWN, !detailsShown)
    }

    fun homeDetailsShown(): Boolean {
        return tinyDB.getBoolean(HOME_DETAILS_SHOWN)
    }

    fun listsDetailsShown(): Boolean {
        return tinyDB.getBoolean(LISTS_DETAILS_SHOWN)
    }

    fun listExists(listId: Int): Boolean {
        return getListIds().contains(listId)
    }

    fun updateListTitlesOrder(ids: ArrayList<Int>) {
        tinyDB.putListInt(IDS_FILENAME, ids)
    }

    fun getFilters(): ArrayList<String> {
        return tinyDB.getListString(LISTS_FILTERS)
    }

    fun setFilters(filters: ArrayList<String>) {
        tinyDB.putListString(LISTS_FILTERS, filters)
    }

    fun getLabels(): ArrayList<String> {
        val labels = ArrayList<String>()
        val ids = getListIds()
        for(id in ids) {
            val list = getList(id)
            if(list.hasLabels()) {
                val listLabels = list.getLabels()
                for(label in listLabels) {
                    if(!labels.contains(label))
                        labels.add(label)
                }
            }
        }
        return labels
    }

    fun getSortIndex(): Int {
        return tinyDB.getInt(LISTS_SORT_INDEX)
    }

    fun listsAreSorted(): Boolean {
        return tinyDB.getInt(LISTS_SORT_INDEX) != 0
    }

    fun setSortIndex(sortIndex: Int) {
        tinyDB.putInt(LISTS_SORT_INDEX, sortIndex)
    }

    fun moveTasks(selectedListId: Int, tasksToMove: ArrayList<Task>) {
        val listAsType = getListAsType(getList(selectedListId))

        when (val listType = listAsType.getListType()) {
            progressListRef -> moveTasksToProgressList(
                listAsType as ProgressList, tasksToMove)
            routineListRef -> {
                val routineList = listAsType as RoutineList
                for(task in tasksToMove) {
                    task.setListId(listAsType.getListId())
                    task.setTaskId(routineList.addNewTaskId())
                    routineList.add(task)
                    updateRoutineList(routineList)
                }
            }
            else -> when(listType) {
                toDoListRef -> {
                    val toDoList = listAsType as ToDoList
                    for(task in tasksToMove.asReversed()) {
                        task.setListId(listAsType.getListId())
                        task.setTaskId(toDoList.addNewTaskId())
                        toDoList.add(task)
                        updateToDoList(toDoList)
                    }
                }
                else -> {
                    val bulletedList = listAsType as BulletedList
                    for(task in tasksToMove.asReversed()) {
                        task.setListId(listAsType.getListId())
                        task.setTaskId(bulletedList.addNewTaskId())
                        bulletedList.add(task)
                        updateBulletedList(bulletedList)
                    }
                }
            }
        }
    }

    private fun moveTasksToProgressList(progressList: ProgressList, tasksToMove: ArrayList<Task>) {
        val inProgressWasEmpty = progressList.getCurrentTasks().size == 0
        val completedWasEmpty = progressList.getCompletedTasks().size == 0
        for(task in tasksToMove.asReversed()) {
            val newTaskId = progressList.addNewTaskId()
            val progressTask = ProgressTask(task, newTaskId)
            progressTask.setDateCreated(task.getDateCreated())
            if(task.isCompleted()) {
                progressTask.setCompleted(true)
                val dateCompleted =
                    task.getDateCompleted()?: getTodaysDate()
                progressTask.setDateCompleted(dateCompleted)
            }
            else progressTask.setInProgress(true)
            val listSection = if(task.isCompleted())
                progressList.getCompletedTitle()
            else progressList.getInProgressTitle()
            progressTask.setListSectionOfTask(listSection)
            progressTask.setListId(progressList.getListId())
            progressList.add(progressTask)
        }
        if(inProgressWasEmpty && progressList.getCurrentTasks().size > 0)
            progressList.toggleInProgressVisibility()
        if(completedWasEmpty && progressList.getCompletedTasks().size > 0)
            progressList.toggleCompletedVisibility()
        updateProgressList(progressList)
    }

    fun quickAddTask(taskString: String, listId: Int): Task {
        val listAsType = getListAsType(getList(listId))
        val listType = listAsType.getListType()

        if(listType == progressListRef) {
            val progressList = listAsType as ProgressList
            val inProgressWasEmpty = progressList.getCurrentTasks().size == 0
            val task = ProgressTask(progressList.getListId())
            task.setTask(taskString)
            task.setInProgress(true)
            task.setListSectionOfTask(progressList.getInProgressTitle())
            task.setTaskId(progressList.addNewTaskId())
            progressList.add(task)
            if(inProgressWasEmpty && progressList.getCurrentTasks().size > 0)
                progressList.toggleInProgressVisibility()
            updateProgressList(progressList)
            return task
        } else {
            val task = Task(listAsType.getListId())
            task.setTask(taskString)
            when(listType) {
                toDoListRef -> {
                    val toDoList = listAsType as ToDoList
                    task.setTaskId(toDoList.addNewTaskId())
                    toDoList.add(task)
                    updateToDoList(toDoList)
                    return task
                }
                routineListRef -> {
                    val routineList = listAsType as RoutineList
                    task.setTaskId(routineList.addNewTaskId())
                    routineList.add(task)
                    updateRoutineList(routineList)
                    return task
                }
                else -> {
                    val bulletedList = listAsType as BulletedList
                    task.setTaskId(bulletedList.addNewTaskId())
                    bulletedList.add(task)
                    updateBulletedList(bulletedList)
                    return task
                }
            }
        }
    }

    fun quickCreateList(listType: String, colorThemeIndex: Int = 0,
                        listTitleString: String = ""): Int {
        val listAsType = when(listType) {
            toDoListRef     -> ToDoList()
            progressListRef -> ProgressList()
            routineListRef  -> RoutineList()
            else            -> BulletedList()
        }

        val listTitleCode = when(listType) {
            toDoListRef     -> R.string.untitledToDoListString
            progressListRef -> R.string.untitledProgressListString
            routineListRef  -> R.string.untitledRoutineListString
            else            -> R.string.untitledBulletedListString
        }

        val listTitle = if(listTitleString.isNotEmpty())
            listTitleString else context.getString(listTitleCode)

        val ids = getListIds()
        val id = generateId(ids)

        listAsType.setListId(id)
        listAsType.setTitle(listTitle)
        listAsType.setColorThemeIndex(colorThemeIndex)

        if(listType == progressListRef) {
            val listAsProgressList = listAsType as ProgressList
            listAsProgressList.renameCompletedList(
                context.getString(R.string.defaultCompletedListTitle))
            listAsProgressList.renameInProgressList(
                context.getString(R.string.defaultInProgressListTitle))
//            listAsProgressList.addListSection(
//                context.getString(R.string.defaultUpcomingListTitle))
//            listAsProgressList.addListSection(
//                context.getString(R.string.defaultFutureListTitle))
        }

        ids.addListAfterFavorites(id)

        tinyDB.putListInt(IDS_FILENAME, ids)
        updateListAsType(listAsType)

        return id
    }

    fun saveBulletedList(bulletedList: BulletedList,
                         listDuplicated: Boolean = false) {
        val ids = getListIds()
        val id = generateId(ids)

        if(listDuplicated) {
            bulletedList.setDateCreated(getTodaysDate())
            for (taskId in bulletedList.getCurrentTasks())
                bulletedList.getTask(taskId)?.setListId(id)
        }

        bulletedList.setListId(id)
        if(bulletedList.isStarred())
            ids.add(0, id)
        else ids.addListAfterFavorites(id)

        tinyDB.putListInt(IDS_FILENAME, ids)
        tinyDB.putObject(id.toString(), bulletedList)
    }

    fun saveToDoList(toDoList: ToDoList,
                     listDuplicated: Boolean = false) {
        val ids = getListIds()
        val id = generateId(ids)

        if(listDuplicated) {
            toDoList.setDateCreated(getTodaysDate())
            for(taskId in toDoList.getCurrentTasks())
                toDoList.getTask(taskId)?.setListId(id)
            for(taskId in toDoList.getCompletedTasks())
                toDoList.getTask(taskId)?.setListId(id)
        }

        toDoList.setListId(id)
        if(toDoList.isStarred())
            ids.add(0, id)
        else ids.addListAfterFavorites(id)

        tinyDB.putListInt(IDS_FILENAME, ids)
        tinyDB.putObject(id.toString(), toDoList)
    }

    fun saveProgressList(progressList: ProgressList,
                         listDuplicated: Boolean = false) {
        val ids = getListIds()
        val id = generateId(ids)

        if(listDuplicated) {
            progressList.setDateCreated(getTodaysDate())
            for(section in progressList.getListSections())
                for(taskId in section)
                    progressList.getTask(taskId)?.setListId(id)
            for(taskId in progressList.getCurrentTasks())
                progressList.getTask(taskId)?.setListId(id)
            for(taskId in progressList.getCompletedTasks())
                progressList.getTask(taskId)?.setListId(id)
        }

        progressList.setListId(id)
        if(progressList.isStarred())
            ids.add(0, id)
        else ids.addListAfterFavorites(id)

        tinyDB.putListInt(IDS_FILENAME, ids)
        tinyDB.putObject(id.toString(), progressList)
    }

    fun saveRoutineList(routineList: RoutineList,
                        listDuplicated: Boolean = false) {
        val ids = getListIds()
        val id = generateId(ids)

        if(listDuplicated) {
            routineList.setDateCreated(getTodaysDate())
            for(taskId in routineList.getCurrentTasks())
                routineList.getTask(taskId)?.setListId(id)
            for(taskId in routineList.getCompletedTasks())
                routineList.getTask(taskId)?.setListId(id)
        }

        routineList.setListId(id)
        if(routineList.isStarred())
            ids.add(0, id)
        else ids.addListAfterFavorites(id)

        tinyDB.putListInt(IDS_FILENAME, ids)
        tinyDB.putObject(id.toString(), routineList)
    }

    private fun ArrayList<Int>.addListAfterFavorites(id: Int): Int {
        var index = 0

        for(listId in this) {
            val list = getList(listId)
            if (!list.isStarred()) break
            index++
        }
        add(index, id)
        return index
    }

    fun getListIds(): ArrayList<Int> {
        return tinyDB.getListInt(IDS_FILENAME)
    }

    fun addListToFavorites(id: Int) {
        val ids = tinyDB.getListInt(IDS_FILENAME)
        ids.remove(id)
        ids.add(0, id)
        tinyDB.putListInt(IDS_FILENAME, ids)
    }

    fun removeListFromFavorites(id: Int): Int {
        val ids = tinyDB.getListInt(IDS_FILENAME)
        ids.remove(id)
        val index = ids.addListAfterFavorites(id)
        tinyDB.putListInt(IDS_FILENAME, ids)

        return index
    }

    fun duplicateList(list: List) {
        val listAsType = getListAsType(list)
        when(list.getListType()) {
            toDoListRef -> saveToDoList(listAsType as ToDoList, listDuplicated = true)
            progressListRef -> saveProgressList(listAsType as ProgressList, listDuplicated = true)
            routineListRef -> saveRoutineList(listAsType as RoutineList, listDuplicated = true)
            bulletedListRef -> saveBulletedList(listAsType as BulletedList, listDuplicated = true)
        }
    }

    fun archiveList(list: List) {
        val listAsType = getListAsType(list)
        listAsType.archive()
        updateListAsType(listAsType)
    }

    fun unarchiveList(list: List) {
        val listAsType = getListAsType(list)
        listAsType.unarchive()
        updateListAsType(listAsType)
    }

    private fun getListAsType(list: List): List {
        return when(list.getListType()) {
            toDoListRef -> getToDoList(list.getListId())
            progressListRef -> getProgressList(list.getListId())
            routineListRef -> getRoutineList(list.getListId())
            else -> getBulletedList(list.getListId())
        }
    }

    private fun updateListAsType(listAsType: List) {
        when(listAsType.getListType()) {
            toDoListRef -> updateToDoList(listAsType as ToDoList)
            progressListRef -> updateProgressList(listAsType as ProgressList)
            routineListRef -> updateRoutineList(listAsType as RoutineList)
            bulletedListRef -> updateBulletedList(listAsType as BulletedList)
        }
    }

    fun toggleStarOfList(list: List) {
        val listAsType = getListAsType(list)
        listAsType.toggleStar()
        updateListAsType(listAsType)
    }

    fun addAndRemoveLabels(list: List, labelsToAddOrRemove: ArrayList<String>,
                          checkedLabels: HashMap<String, Boolean>) {
        val listAsType = getListAsType(list)
        var listUpdated = false
        for(label in labelsToAddOrRemove) {
            val labelIsChecked = checkedLabels[label]?: false
            if(labelIsChecked) {
                if(!listAsType.getLabels().contains(label)) {
                    listAsType.addLabel(label)
                    listUpdated = true
                }
            } else {
                if(listAsType.getLabels().contains(label)) {
                    listAsType.removeLabel(label)
                    listUpdated = true
                }
            }
        }
        if(listUpdated) updateListAsType(listAsType)
    }

    fun updateProgressList(progressList: ProgressList, updateListDate: Boolean = true) {
        if(updateListDate) progressList.listUpdated()
        val id = progressList.getListId().toString()
        tinyDB.putObject(id, progressList)
    }

    fun getProgressList(id: Int): ProgressList {
        return tinyDB.getObject(id.toString(), ProgressList::class.java)
    }

    fun updateRoutineList(routineList: RoutineList, updateListDate: Boolean = true) {
        if(updateListDate) routineList.listUpdated()
        val id = routineList.getListId().toString()
        tinyDB.putObject(id, routineList)
    }

    fun getRoutineList(id: Int): RoutineList {
        return tinyDB.getObject(id.toString(), RoutineList::class.java)
    }

    fun updateBulletedList(bulletedList: BulletedList, updateListDate: Boolean = true) {
        if(updateListDate) bulletedList.listUpdated()
        val id = bulletedList.getListId().toString()
        tinyDB.putObject(id, bulletedList)
    }

    fun getBulletedList(id: Int): BulletedList {
        return tinyDB.getObject(id.toString(), BulletedList::class.java)
    }

    fun updateToDoList(toDoList: ToDoList, updateListDate: Boolean = true) {
        if(updateListDate) toDoList.listUpdated()
        val id = toDoList.getListId().toString()
        tinyDB.putObject(id, toDoList)
    }

    fun getToDoList(id: Int): ToDoList {
        return tinyDB.getObject(id.toString(), ToDoList::class.java)
    }

    fun getList(id: Int): List {
        return tinyDB.getObject(id.toString(), List::class.java)
    }

    fun deleteList(id: Int) {
        val ids = tinyDB.getListInt(IDS_FILENAME)
        ids.remove(id)

        tinyDB.putListInt(IDS_FILENAME, ids)
        tinyDB.putObject(id.toString(), null)
        tinyDB.remove(id.toString())
    }
}