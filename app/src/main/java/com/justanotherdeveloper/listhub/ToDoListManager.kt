package com.justanotherdeveloper.listhub

import android.util.SparseArray
import android.view.View
import androidx.core.util.set
import kotlinx.android.synthetic.main.activity_to_do_list.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ToDoListManager(private val activity: ToDoListActivity) {

    private lateinit var list: ToDoList

    private var inSelectState = false
    private var selectedCount = 0
    private var taskSelectedMap = HashMap<Task, Boolean>()

    private var idOfTaskToOpen = SENTINEL
    private var openTaskDialog = false
    private var dialogOpened = false

    private var currentDate = getTodaysDate()

    fun initNewList() {
        list = ToDoList()
        activity.getView().changeTheme()
        activity.getView().updateNoItemsMessageVisibility()
        activity.getDialogs().showNameListDialog(false)
    }

    fun loadList(database: ListsDatabase, listId: Int) {
        list = database.getToDoList(listId)
        activity.getView().reloadToDoList()
        val untitledListTitle = activity.getString(R.string.untitledToDoListString)
        if(list.getTitle() == untitledListTitle && !dialogOpened)
            activity.getDialogs().showNameListDialog()
    }

    fun refreshList(database: ListsDatabase) {
        list = database.getToDoList(list.getListId())
    }

    fun getList(): ToDoList {
        return list
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

    fun removeLinkIfListDoesntExist(task: Task) {
        val linkedListId = task.getLinkedListId()
        if(linkedListId == list.getListId() ||
            !activity.getDatabase().listExists(linkedListId)) {
            task.removeLinkFromList()
            activity.updateToDoList(false)
        }
    }

    fun toggleListStar() {
        getList().toggleStar()
        if(getList().isStarred())
            activity.getDatabase().addListToFavorites(getList().getListId())
        else activity.getDatabase().removeListFromFavorites(getList().getListId())
        activity.updateToDoList()
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

        fun openTask(task: Task) {
            val view = activity.getView().getTaskView(task.getTaskId())
            activity.getDialogs().showAddTaskDialog(task, view)
        }

        if(idOfTaskToOpen != SENTINEL) {
            val task = list.getTask(idOfTaskToOpen)
            if(task != null) openTask(task)
        } else activity.getDialogs().checkIfLinkedListExists()
        idOfTaskToOpen = SENTINEL
    }

    fun addTask(task: Task?) {
        if(task == null) return
        task.setTaskId(getList().addNewTaskId())
        val index = getList().add(task)
        activity.getView().addTask(task, index)
        activity.getView().updateNoItemsMessageVisibility()
        activity.updateToDoList()
    }

    fun sortList(sortIndex: Int = 0) {
        getList().setSortIndex(sortIndex)
        activity.getView().sortList(sortIndex)
        activity.updateToDoList()
    }

    fun getSortedTaskOrder(sortIndex: Int, sortCompletedTasks: Boolean): ArrayList<Task> {
        val taskIds = if(sortCompletedTasks)
            list.getCompletedTasks() else list.getCurrentTasks()
        val tasks = ArrayList<Task>()
        for(taskId in taskIds) {
            val task = list.getTask(taskId)
            if(task != null) tasks.add(task)
        }
        return getSortedTaskOrder(sortIndex, tasks)
    }

    fun duplicateTasks(forDialogTask: Boolean) {
        val tasksToCopy = ArrayList<Task>()

        if(forDialogTask) {
            beginTransition(activity.toDoListParent)
            val taskToCopy = activity.getDialogs()
                .currentDialogTask ?: return
            tasksToCopy.add(taskToCopy)
            activity.getDialogs().currentDialog?.cancel()
        } else {
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
        activity.updateToDoList()
    }

    fun completeTasks() {
        val selectedTasks = ArrayList<Task>()

        for(taskId in list.getCurrentTasks()) {
            val task = list.getTask(taskId)
            if (task != null && taskIsSelected(task)) selectedTasks.add(task)
        }

        for(task in selectedTasks.asReversed()) {
            val index = list.markTaskCompleted(task)
            val view = activity.getView().getTaskView(task.getTaskId())
            activity.getView().moveToCompleted(index, task, view)
        }

        activity.getView().setToDefaultState()
        activity.getView().updateCompletedTitleBar()
        activity.getView().refreshListOrderIfSorted()
        activity.updateToDoList()
    }

    fun deleteCompletedTasks() {
        beginTransition(activity.toDoListParent)
        activity.completedTaskContainer.removeAllViews()
        for(taskId in list.getCompletedTasks()) {
            val task = list.getTask(taskId)
            if(task != null) list.deleteTask(task)
        }
        activity.getView().updateCompletedTitleBar()
        activity.updateToDoList()
    }

    private fun moveTasks(selectedListId: Int, forDialogTask: Boolean, dialogTask: Task?) {
        val tasksToMove = ArrayList<Task>()

        if(forDialogTask)
            if(dialogTask != null)
                tasksToMove.add(dialogTask) else return
        else {
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
        val idTaskMap = SparseArray<Task>()
        val taskIdsToDelete = ArrayList<Int>()

        val dialogTaskId = if(forDialogTask) activity.getDialogs()
            .currentDialogTask?.getTaskId()?: SENTINEL else SENTINEL
        if(forDialogTask) activity.getDialogs().currentDialog?.cancel()

        val dialogTask = if(dialogTaskId != SENTINEL)
            activity.getList().getTask(dialogTaskId) else null

        fun addTaskToDelete(viewsToDelete: ArrayList<View>, task: Task) {
            viewsToDelete.add(activity.getView().getTaskView(task.getTaskId()))
            taskIdsToDelete.add(task.getTaskId())
            idTaskMap[task.getTaskId()] = task
        }

        val currentViewsToDelete = ArrayList<View>()

        for(taskId in list.getCurrentTasks()) {
            val task = list.getTask(taskId)
            if(forDialogTask) {
                if (task != null && taskId == dialogTaskId) {
                    addTaskToDelete(currentViewsToDelete, task)
                    break
                }
            } else {
                if (task != null && taskIsSelected(task))
                    addTaskToDelete(currentViewsToDelete, task)
            }
        }

        val completedViewsToDelete = ArrayList<View>()

        if(!forDialogTask || taskIdsToDelete.isEmpty()) {
            for (taskId in list.getCompletedTasks()) {
                val task = list.getTask(taskId)
                if (forDialogTask) {
                    if (task != null && taskId == dialogTaskId) {
                        addTaskToDelete(completedViewsToDelete, task)
                        break
                    }
                } else {
                    if (task != null && taskIsSelected(task))
                        addTaskToDelete(completedViewsToDelete, task)
                }
            }
        }

        if(selectedListId != SENTINEL) moveTasks(selectedListId, forDialogTask, dialogTask)

        activity.getView().setToDefaultState()
        for(view in currentViewsToDelete) activity.taskContainer.removeView(view)
        for(view in completedViewsToDelete) activity.completedTaskContainer.removeView(view)
        for(taskId in taskIdsToDelete) {
            val task = idTaskMap[taskId]
            list.deleteTask(task, taskId)
        }

        activity.getView().updateCompletedTitleBar()
        activity.updateToDoList()
    }

    fun deleteTask(task: Task) {
        beginTransition(activity.toDoListParent)
        val taskView = activity.getView().getTaskView(task.getTaskId())
        if(task.isCompleted()) activity.completedTaskContainer.removeView(taskView)
        else activity.taskContainer.removeView(taskView)
        list.deleteTask(task)

        activity.getView().updateCompletedTitleBar()
        activity.updateToDoList()
    }

    fun setSelectedTasksDates(date: Calendar? = null, repeatingDays: ArrayList<Int>? = null) {

        fun setDateIfSelected(task: Task) {
            task.setDate(null)
            task.clearRepeatingDays()
            if(date != null) task.setDate(date)
            if(repeatingDays != null) task.setRepeatingDays(repeatingDays)
            val view = activity.getView().getTaskView(task.getTaskId())
            activity.getView().editTask(task, view)
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
        activity.updateToDoList()
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
        for(taskId in list.getCurrentTasks()) {
            val task = list.getTask(taskId)
            if (task != null) taskSelectedMap[task] = false
        }
        for(taskId in list.getCompletedTasks()) {
            val task = list.getTask(taskId)
            if (task != null) taskSelectedMap[task] = false
        }
    }

    fun toggleSelectedTask(task: Task): Boolean {
        val taskIsSelected = taskIsSelected(task)
        taskSelectedMap[task] = !taskIsSelected
        if(!taskIsSelected) selectedCount++ else selectedCount--
        return taskSelectedMap[task]?: false
    }

    fun taskIsSelected(task: Task): Boolean {
        return taskSelectedMap[task]?: false
    }

    fun selectedTasksAreCurrentTasks(): Boolean? {
        var reorderTasks: Boolean? = null

        val dialogTaskId = if(dialogOpened) activity.getDialogs()
            .currentDialogTask?.getTaskId()?: SENTINEL else SENTINEL

        fun setTasksToReorder(tasks: ArrayList<Int>,
                              currentTasks: Boolean): Boolean {
            when {
                tasks.size < 2 -> return true
                reorderTasks == null -> reorderTasks = currentTasks
                reorderTasks != currentTasks -> return true
            }
            return false
        }

        for(taskId in list.getCurrentTasks()) {
            if(dialogOpened) {
                if(taskId == dialogTaskId)
                    return if(setTasksToReorder(list.getCurrentTasks(),
                            true)) null else true
            } else {
                val task = list.getTask(taskId)
                if (task != null && taskIsSelected(task))
                    if(setTasksToReorder(list.getCurrentTasks(),
                            true)) return null
            }
        }

        for(taskId in list.getCompletedTasks()) {
            if(dialogOpened) {
                if(taskId == dialogTaskId)
                    return if(setTasksToReorder(list.getCompletedTasks(),
                            false)) null else false
            } else {
                val task = list.getTask(taskId)
                if (task != null && taskIsSelected(task))
                    if(setTasksToReorder(list.getCompletedTasks(),
                            false)) return null
            }
        }

        return reorderTasks
    }

    fun getSelectedCount(): Int {
        return if(dialogOpened)
            1 else selectedCount
    }
}