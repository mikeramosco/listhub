package com.justanotherdeveloper.listhub

import android.view.View
import kotlinx.android.synthetic.main.activity_bulleted_list.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class BulletedListManager(private val activity: BulletedListActivity) {

    private lateinit var list: BulletedList

    private var inSelectState = false
    private var selectedCount = 0
    private var taskSelectedMap = HashMap<Task, Boolean>()

    private var idOfTaskToOpen = SENTINEL
    private var openTaskDialog = false
    private var dialogOpened = false

    private var currentDate = getTodaysDate()

    fun initNewList() {
        list = BulletedList()
        activity.getView().changeTheme()
        activity.getView().updateNoItemsMessageVisibility()
        activity.getDialogs().showNameListDialog(false)
    }

    fun loadList(database: ListsDatabase, listId: Int) {
        list = database.getBulletedList(listId)
        activity.getView().reloadBulletedList()
        val untitledListTitle = activity.getString(R.string.untitledBulletedListString)
        if(list.getTitle() == untitledListTitle && !dialogOpened)
            activity.getDialogs().showNameListDialog()
    }

    fun refreshList(database: ListsDatabase) {
        list = database.getBulletedList(list.getListId())
    }

    fun getList(): BulletedList {
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
            activity.updateBulletedList(false)
        }
    }

    fun toggleListStar() {
        getList().toggleStar()
        if(getList().isStarred())
            activity.getDatabase().addListToFavorites(getList().getListId())
        else activity.getDatabase().removeListFromFavorites(getList().getListId())
        activity.updateBulletedList()
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
        activity.updateBulletedList()
    }

    fun sortList(sortIndex: Int = 0) {
        getList().setSortIndex(sortIndex)
        activity.getView().sortList(sortIndex)
        activity.updateBulletedList()
    }

    fun getSortedTaskOrder(sortIndex: Int): ArrayList<Task> {
        val tasks = ArrayList<Task>()
        for(taskId in list.getCurrentTasks()) {
            val task = list.getTask(taskId)
            if(task != null) tasks.add(task)
        }
        return getSortedTaskOrder(sortIndex, tasks)
    }

    fun duplicateTasks(forDialogTask: Boolean) {
        val tasksToCopy = ArrayList<Task>()

        if(forDialogTask) {
            beginTransition(activity.bulletedListParent)
            val taskToCopy = activity.getDialogs()
                .currentDialogTask?: return
            tasksToCopy.add(taskToCopy)
            activity.getDialogs().currentDialog?.cancel()
        } else {
            for (taskId in list.getCurrentTasks()) {
                val task = list.getTask(taskId)
                if (task != null && taskIsSelected(task)) tasksToCopy.add(task)
            }
        }

        for(task in tasksToCopy)
            activity.getView().copyTask(task, false)

        activity.getView().setToDefaultState()
        activity.updateBulletedList()
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
        }

        activity.getDatabase().moveTasks(selectedListId, tasksToMove)
    }

    fun initMoveTasksProcess(selectedListId: Int, forDialogTask: Boolean) {
        deleteTasks(selectedListId, forDialogTask)
        activity.openList(selectedListId)
    }

    fun deleteTasks(selectedListId: Int = SENTINEL, forDialogTask: Boolean = false) {
        val taskIdsToDelete = ArrayList<Int>()
        val currentViewsToDelete = ArrayList<View>()

        var dialogTask: Task? = null

        if(forDialogTask) {
            dialogTask = activity.getDialogs()
                .currentDialogTask ?: return
            currentViewsToDelete.add(activity.getView().getTaskView(dialogTask.getTaskId()))
            taskIdsToDelete.add(dialogTask.getTaskId())
            activity.getDialogs().currentDialog?.cancel()
        } else {
            for (taskId in list.getCurrentTasks()) {
                val task = list.getTask(taskId)
                if (task != null && taskIsSelected(task)) {
                    currentViewsToDelete.add(activity.getView().getTaskView(task.getTaskId()))
                    taskIdsToDelete.add(task.getTaskId())
                }
            }
        }

        if(selectedListId != SENTINEL) moveTasks(selectedListId, forDialogTask, dialogTask)

        activity.getView().setToDefaultState()
        for(view in currentViewsToDelete) activity.taskContainer.removeView(view)
        for(taskId in taskIdsToDelete) list.deleteTask(taskId)

        activity.getView().updateNoItemsMessageVisibility()
        activity.updateBulletedList()
    }

    fun deleteTask(task: Task) {
        beginTransition(activity.bulletedListParent)
        val taskView = activity.getView().getTaskView(task.getTaskId())
        activity.taskContainer.removeView(taskView)
        list.deleteTask(task.getTaskId())
        activity.getView().updateNoItemsMessageVisibility()
        activity.updateBulletedList()
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

        activity.getView().setToDefaultState()
        activity.updateBulletedList()
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
            if(task != null) taskSelectedMap[task] = false
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

    fun getSelectedCount(): Int {
        return if(dialogOpened)
            1 else selectedCount
    }
}