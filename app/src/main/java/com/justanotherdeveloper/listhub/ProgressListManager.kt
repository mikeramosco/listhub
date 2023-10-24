package com.justanotherdeveloper.listhub

import android.util.SparseArray
import android.view.View
import androidx.core.util.set
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_progress_list.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ProgressListManager(private val activity: ProgressListActivity) {

    private lateinit var list: ProgressList

    private var inReorderSectionState = false

    private var inSelectState = false
    private var selectedCount = 0
    private var taskSelectedMap = HashMap<ProgressTask, Boolean>()

    private var idOfTaskToOpen = SENTINEL
    private var openTaskDialog = false
    private var dialogOpened = false

    private var currentDate = getTodaysDate()

    fun sectionsExist(): Boolean {
        return list.getListSections().isNotEmpty()
    }

    fun setSelectedTasksDates(date: Calendar? = null, repeatingDays: ArrayList<Int>? = null) {
        fun setDateIfSelected(task: ProgressTask) {
            task.setDate(null)
            task.clearRepeatingDays()
            if(date != null) task.setDate(date)
            if(repeatingDays != null) task.setRepeatingDays(repeatingDays)
            val view = activity.getView().getTaskView(task.getTaskId())
            activity.getView().editTask(task, view)
        }

        for(section in list.getListSections())
            for(taskId in section) {
                val task = list.getTask(taskId)
                if (task != null && taskIsSelected(task)) setDateIfSelected(task)
            }

        for(taskId in list.getCurrentTasks()) {
            val task = list.getTask(taskId)
            if (task != null && taskIsSelected(task)) setDateIfSelected(task)
        }

        for(taskId in list.getCompletedTasks()) {
            val task = list.getTask(taskId)
            if (task != null && taskIsSelected(task)) setDateIfSelected(task)
        }

        activity.getView().setToDefaultState()
        activity.updateProgressList()
    }

    fun deleteCompletedTasks() {
        beginTransition(activity.progressListParent)
        activity.completedTaskContainer.removeAllViews()
        for(taskId in list.getCompletedTasks()) {
            val task = list.getTask(taskId)
            if(task != null) list.deleteTask(task)
        }
        activity.getView().updateTitleBars(
            activity.completedTaskContainer)
        activity.updateProgressList()
    }

    fun getReorderableLists(): ArrayList<String> {
        val ableListsToReorder = ArrayList<String>()
        for((i, section) in list.getListSections().withIndex())
            if(section.size >= 2) ableListsToReorder.add(list.getListSectionTitle(i))
        if(list.getCurrentTasks().size >= 2) ableListsToReorder.add(list.getInProgressTitle())
        if(list.getCompletedTasks().size >= 2) ableListsToReorder.add(list.getCompletedTitle())
        return ableListsToReorder
    }

    fun removeLinkIfListDoesntExist(task: Task) {
        val linkedListId = task.getLinkedListId()
        if(linkedListId == list.getListId() ||
            !activity.getDatabase().listExists(linkedListId)) {
            task.removeLinkFromList()
            activity.updateProgressList(false)
        }
    }

    fun updateTasksIfDateOutdated() {
        val todaysDate = getTodaysDate()
        if(datesAreTheSame(currentDate, todaysDate)) return
        currentDate = todaysDate
        for(taskId in list.taskIds) {
            val task = list.getTask(taskId)
            if (task != null && task.hasDueDate())
                activity.getView().editTask(task,
                    activity.getView().getTaskView(taskId))
        }
    }

    fun refreshList(database: ListsDatabase) {
        list = database.getProgressList(list.getListId())
    }

    fun initNewList() {
        list = ProgressList()
        list.renameCompletedList(activity.getString(R.string.defaultCompletedListTitle))
        list.renameInProgressList(activity.getString(R.string.defaultInProgressListTitle))
//        list.addListSection(activity.getString(R.string.defaultUpcomingListTitle))
//        list.addListSection(activity.getString(R.string.defaultFutureListTitle))
        activity.getView().initProgressListSections()
        activity.getView().changeTheme()
        activity.getView().toggleListSectionsVisibility()
        activity.getDialogs().showNameListDialog(false)
    }

    fun loadList(database: ListsDatabase, listId: Int) {
        list = database.getProgressList(listId)
        activity.getView().reloadProgressList()
        val untitledListTitle = activity.getString(R.string.untitledProgressListString)
        if(list.getTitle() == untitledListTitle && !dialogOpened)
            activity.getDialogs().showNameListDialog()
    }

    fun sectionExists(section: String): Boolean {
        return when(section) {
            list.getInProgressTitle() -> true
            list.getCompletedTitle() -> true
            else -> {
                for (title in list.getListSectionTitles())
                    if (title == section) return true
                false
            }
        }
    }

    fun dialogAlreadyOpened(): Boolean {
        if(dialogOpened) dialogOpened = activity
            .getDialogs().currentDialog != null
        return dialogOpened
    }

    fun setDialogOpened(dialogOpened: Boolean = true) {
        this.dialogOpened = dialogOpened
    }

    fun setTaskToOpen(taskId: Int) {
        openTaskDialog = true
        idOfTaskToOpen = taskId
    }

    fun checkForTaskToOpen() {
        if(!openTaskDialog) return
        openTaskDialog = false

        fun openTask(task: ProgressTask) {
            val view = activity.getView().getTaskView(task.getTaskId())
            activity.getDialogs().showAddTaskDialog(task, view)
        }

        if(idOfTaskToOpen != SENTINEL) {
            val task = list.getTask(idOfTaskToOpen)
            if(task != null) openTask(task)
        } else activity.getDialogs().checkIfLinkedListAndSectionExists()
        idOfTaskToOpen = SENTINEL
    }


    fun getSortedTaskOrder(sortIndex: Int, taskIds: ArrayList<Int>): ArrayList<Task> {
        val tasksAsTasks = ArrayList<Task>()
        for(taskId in taskIds) {
            val task = list.getTask(taskId)
            if(task != null) tasksAsTasks.add(task)
        }
        return getSortedTaskOrder(sortIndex, tasksAsTasks)
    }

    fun toggleListStar() {
        list.toggleStar()
        if(list.isStarred())
            activity.getDatabase().addListToFavorites(list.getListId())
        else activity.getDatabase().removeListFromFavorites(list.getListId())
        activity.updateProgressList()
    }

    fun addTask(task: ProgressTask?) {
        if(task == null) return
        task.setTaskId(list.addNewTaskId())
        val index = list.add(task)
        activity.getView().addTask(task, index)
        activity.updateProgressList()
    }

    fun getList(): ProgressList {
        return list
    }

    fun setListSection(task: ProgressTask, otherSection: String) {
        val currentSection = task.getListSectionOfTask()
        if(currentSection == list.getInProgressTitle()) task.setInProgress(false)
        else if(currentSection == list.getCompletedTitle()) task.setCompleted(false)
        task.setListSectionOfTask(otherSection)
        if(otherSection == list.getInProgressTitle()) task.setInProgress(true)
        else if(otherSection == list.getCompletedTitle()) task.setCompleted(true)
    }

    private fun moveTasks(selectedListId: Int, forDialogTask: Boolean, dialogTask: ProgressTask?) {
        val tasksToMove = ArrayList<Task>()

        if(forDialogTask)
            if(dialogTask != null)
                tasksToMove.add(dialogTask) else return
        else {
            for (section in list.getListSections())
                for (taskId in section) {
                    val task = list.getTask(taskId)
                    if (task != null && taskIsSelected(task)) tasksToMove.add(task)
                }

            for (taskId in list.getCurrentTasks()) {
                val task = list.getTask(taskId)
                if (task != null && taskIsSelected(task)) tasksToMove.add(task)
            }

            for (taskId in list.getCompletedTasks()) {
                val task = list.getTask(taskId)
                if (task != null && taskIsSelected(task)) tasksToMove.add(task)
            }
        }

        activity.getDatabase().moveTasks(selectedListId, tasksToMove)
    }

    fun initMoveTasksProcess(selectedListId: Int, forDialogTask: Boolean) {
        deleteTasks(selectedListId, forDialogTask)
        activity.openList(selectedListId)
    }

    fun deleteTasks(selectedListId: Int = SENTINEL, forDialogTask: Boolean = false) {
        val idTaskMap = SparseArray<ProgressTask>()
        val taskIdsToDelete = ArrayList<Int>()
        val viewsToDelete = ArrayList<View>()
        val tasksToDelete = ArrayList<ProgressTask>()

        fun addTaskToDelete(task: ProgressTask) {
            viewsToDelete.add(activity.getView().getTaskView(task.getTaskId()))
            tasksToDelete.add(task)
            taskIdsToDelete.add(task.getTaskId())
            idTaskMap[task.getTaskId()] = task
        }

        var dialogTask: ProgressTask? = null

        if(forDialogTask) {
            dialogTask = activity.getDialogs()
                .getTaskAsProgressTask() ?: return
            addTaskToDelete(dialogTask)
            activity.getDialogs().currentDialog?.cancel()
        } else {
            for (section in list.getListSections())
                for (taskId in section) {
                    val task = list.getTask(taskId)
                    if (task != null && taskIsSelected(task))
                        addTaskToDelete(task)
                }

            for (taskId in list.getCurrentTasks()) {
                val task = list.getTask(taskId)
                if (task != null && taskIsSelected(task))
                    addTaskToDelete(task)
            }

            for (taskId in list.getCompletedTasks()) {
                val task = list.getTask(taskId)
                if (task != null && taskIsSelected(task))
                    addTaskToDelete(task)
            }
        }

        if(selectedListId != SENTINEL) moveTasks(selectedListId, forDialogTask, dialogTask)

        beginTransition(activity.progressListParent)
        for((i, view) in viewsToDelete.withIndex()) {
            val taskContainer = activity.getView().getTaskContainer(tasksToDelete[i])
            taskContainer.removeView(view)
            activity.getView().updateTitleBars(taskContainer, animate = false)
        }
        for(taskId in taskIdsToDelete) {
            val task = idTaskMap[taskId]
            list.deleteTask(task, taskId)
        }

        activity.getView().setToDefaultState()
        activity.updateProgressList()
    }

    fun deleteTask(task: ProgressTask) {
        beginTransition(activity.progressListParent)
        val taskView = activity.getView().getTaskView(task.getTaskId())
        val taskContainer = activity.getView().getTaskContainer(task)
        taskContainer.removeView(taskView)
        activity.getView().updateTitleBars(taskContainer, animate = false)
        list.deleteTask(task)
        activity.updateProgressList()
    }

    fun duplicateTasks(forDialogTask: Boolean) {
        val tasksToCopy = ArrayList<ProgressTask>()

        if(forDialogTask) {
            beginTransition(activity.progressListParent)
            val taskToCopy = activity.getDialogs()
                .getTaskAsProgressTask()?: return
            tasksToCopy.add(taskToCopy)
            activity.getDialogs().currentDialog?.cancel()
        } else {
            for (section in list.getListSections())
                for (taskId in section) {
                    val task = list.getTask(taskId)
                    if (task != null && taskIsSelected(task)) tasksToCopy.add(task)
                }

            for (taskId in list.getCurrentTasks()) {
                val task = list.getTask(taskId)
                if (task != null && taskIsSelected(task)) tasksToCopy.add(task)
            }

            for (taskId in list.getCompletedTasks()) {
                val task = list.getTask(taskId)
                if (task != null && taskIsSelected(task)) tasksToCopy.add(task)
            }
        }

        for(task in tasksToCopy)
            activity.getView().copyTask(task, false)

        activity.getView().setToDefaultState()
        activity.updateProgressList()
    }

    fun moveTasksToOtherSection(sectionToMoveTo: String = list.getCompletedTitle()) {
        val tasksToMove = ArrayList<ProgressTask>()

        for(section in list.getListSections())
            for(taskId in section) {
                val task = list.getTask(taskId)
                if (task != null && taskIsSelected(task)) tasksToMove.add(task)
            }

        for(taskId in list.getCurrentTasks()) {
            val task = list.getTask(taskId)
            if (task != null && taskIsSelected(task)) tasksToMove.add(task)
        }

        for(taskId in list.getCompletedTasks()) {
            val task = list.getTask(taskId)
            if (task != null && taskIsSelected(task))  tasksToMove.add(task)
        }

        for(task in tasksToMove.asReversed()) {
            val view = activity.getView().getTaskView(task.getTaskId())
            val currentSection = task.getListSectionOfTask()
            val moveTask = currentSection != sectionToMoveTo
            if(moveTask) activity.getView().moveTaskToOtherSection(
                task, currentSection, sectionToMoveTo,view, animate = false)
        }

        activity.getView().setToDefaultState()
        activity.getView().refreshListOrderIfSorted()
        activity.updateProgressList()
    }

    fun getSectionOfSelectedTasks(): String {
        if(dialogOpened) return activity.getDialogs()
            .getTaskAsProgressTask()?.getListSectionOfTask()?: ""

        var sectionOfSelectedTasks = ""

        fun setSectionOfSelectedTasks(task: ProgressTask,
                                      section: ArrayList<Int>): Boolean {
            when {
                section.size < 2 -> return true
                sectionOfSelectedTasks.isEmpty() ->
                    sectionOfSelectedTasks = task.getListSectionOfTask()
                sectionOfSelectedTasks != task.getListSectionOfTask() -> return true
            }
            return false
        }

        for(section in list.getListSections())
            for(taskId in section) {
                val task = list.getTask(taskId)
                if (task != null && taskIsSelected(task))
                    if (setSectionOfSelectedTasks(task, section)) return ""
            }

        for(taskId in list.getCurrentTasks()) {
            val task = list.getTask(taskId)
            if (task != null && taskIsSelected(task))
                if(setSectionOfSelectedTasks(
                        task, list.getCurrentTasks())) return ""
        }

        for(taskId in list.getCompletedTasks()) {
            val task = list.getTask(taskId)
            if (task != null && taskIsSelected(task))
                if(setSectionOfSelectedTasks(
                        task, list.getCompletedTasks())) return ""
        }

        return sectionOfSelectedTasks
    }

    fun addNewSection(newSection: String) {
        val colorTheme = activity.getColorTheme(
            list.getColorThemeIndex())
        list.addListSection(newSection)
        activity.getView().addSectionView(colorTheme,
            newSection, isNewSection = true)
        activity.getView().toggleMoveToSectionOptionsLayoutVisibility()
        activity.updateProgressList()
    }

    fun renameSection(originalTitle: String, sectionTitle: String) {
        val sectionTitleView = when(originalTitle) {
            list.getInProgressTitle() -> {
                list.renameInProgressList(sectionTitle)
                activity.inProgressTaskText
            }
            list.getCompletedTitle() -> {
                list.renameCompletedList(sectionTitle)
                activity.completedTaskText
            }
            else -> {
                val sectionIndex = list.getListSectionIndex(originalTitle)
                list.renameListSectionTitle(sectionIndex, sectionTitle)
                val sectionView = activity.listSectionsContainer[sectionIndex]
                sectionView.findViewById(R.id.sectionTitleText)
            }
        }
        beginTransition(activity.progressListParent)
        sectionTitleView.text = sectionTitle
        activity.updateProgressList()
    }

    fun deleteSection(section: String) {
        val sectionIndex = list.getListSectionIndex(section)
        val sectionView = activity.listSectionsContainer[sectionIndex]
        list.removeListSection(sectionIndex)
        beginTransition(activity.progressListParent)
        activity.listSectionsContainer.removeView(sectionView)
        activity.getView().toggleMoveToSectionOptionsLayoutVisibility()
        activity.updateProgressList()
    }

    fun sortList(sortIndex: Int = 0) {
        list.setSortIndex(sortIndex)
        activity.getView().sortList(sortIndex)
        activity.updateProgressList()
    }

    fun inReorderSectionState(): Boolean {
        return inReorderSectionState
    }

    fun setReorderSectionsState(inReorderSectionState: Boolean) {
        this.inReorderSectionState = inReorderSectionState
    }

    fun inSelectState(): Boolean {
        return inSelectState
    }

    fun setSelectState(inSelectState: Boolean) {
        this.inSelectState = inSelectState
        if(inSelectState) initSelectedTasksLists()
    }

    private fun initSelectedTasksLists() {
        selectedCount = 0
        taskSelectedMap.clear()
        for(listSection in list.getListSections())
            for(taskId in listSection) {
                val task = list.getTask(taskId)
                if(task != null) taskSelectedMap[task] = false
            }
        for(taskId in list.getCurrentTasks()) {
            val task = list.getTask(taskId)
            if(task != null) taskSelectedMap[task] = false
        }
        for(taskId in list.getCompletedTasks()) {
            val task = list.getTask(taskId)
            if(task != null) taskSelectedMap[task] = false
        }
    }

    fun toggleSelectedTask(task: ProgressTask): Boolean {
        val taskIsSelected = taskIsSelected(task)
        taskSelectedMap[task] = !taskIsSelected
        if(!taskIsSelected) selectedCount++ else selectedCount--
        return taskSelectedMap[task]?: false
    }

    fun taskIsSelected(task: ProgressTask): Boolean {
        return taskSelectedMap[task]?: false
    }

    fun getSelectedCount(): Int {
        return if(dialogOpened)
            1 else selectedCount
    }
}