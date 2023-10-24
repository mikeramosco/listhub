package com.justanotherdeveloper.listhub

import android.util.SparseArray
import android.view.View
import androidx.core.util.set
import kotlinx.android.synthetic.main.activity_routine_list.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RoutineListManager(private val activity: RoutineListActivity) {

    private lateinit var list: RoutineList

    private var inSelectState = false
    private var selectedCount = 0
    private var taskSelectedMap = HashMap<Task, Boolean>()

    private var idOfTaskToOpen = SENTINEL
    private var openTaskDialog = false
    private var dialogOpened = false

    fun initNewList() {
        list = RoutineList()
        activity.getView().changeTheme()
        activity.getView().updateNoItemsMessageVisibility()
        activity.getDialogs().showNameRoutineListDialog(false)
    }

    fun loadList(database: ListsDatabase, listId: Int) {
        list = database.getRoutineList(listId)
        activity.getView().reloadRoutineList()
        val untitledListTitle = activity.getString(R.string.untitledRoutineListString)
        if(list.getTitle() == untitledListTitle && !dialogOpened)
            activity.getDialogs().showNameRoutineListDialog()
    }

    fun refreshList(database: ListsDatabase) {
        list = database.getRoutineList(list.getListId())
    }

    fun getList(): RoutineList {
        return list
    }

    fun removeLinkIfListDoesntExist(task: Task) {
        val linkedListId = task.getLinkedListId()
        if(linkedListId == list.getListId() ||
            !activity.getDatabase().listExists(linkedListId)) {
            task.removeLinkFromList()
            activity.updateRoutineList(false)
        }
    }

    fun toggleListStar() {
        getList().toggleStar()
        if(getList().isStarred())
            activity.getDatabase().addListToFavorites(getList().getListId())
        else activity.getDatabase().removeListFromFavorites(getList().getListId())
        activity.updateRoutineList()
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

    fun resetList() {
        val tasksToReset = ArrayList<Task>()

        for(taskId in list.getCompletedTasks()) {
            val task = list.getTask(taskId)
            if(task != null) tasksToReset.add(task)
        }

        beginTransition(activity.routineListParent)
        for(task in tasksToReset) {
            val view = activity.getView().getTaskView(task.getTaskId())
            activity.getView().uncheckTask(task, view, animate = false, resettingList = true)
        }
        activity.getView().refreshListOrderIfSorted()
        activity.updateRoutineList()
    }

    fun addTask(task: Task?) {
        if(task == null) return
        task.setTaskId(getList().addNewTaskId())
        val index = getList().add(task)
        activity.getView().addTask(task, index)
        activity.getView().updateNoItemsMessageVisibility()
        activity.updateRoutineList()
    }

    fun sortList(sortIndex: Int = 0) {
        getList().setSortIndex(sortIndex)
        activity.getView().sortList(sortIndex)
        activity.updateRoutineList()
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
            beginTransition(activity.routineListParent)
            val taskToCopy = activity.getDialogs()
                .currentDialogTask?: return
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
        activity.updateRoutineList()
    }

    fun completeTasks() {
        val orderedTasks = ArrayList<Task>()

        for(taskId in list.getCurrentTasks()) {
            val task = list.getTask(taskId)
            if (task != null && taskIsSelected(task)) orderedTasks.add(task)
        }

        for(task in orderedTasks) {
            val index = list.markTaskCompleted(task)
            val view = activity.getView().getTaskView(task.getTaskId())
            activity.getView().moveToCompleted(index, task, view)
        }

        activity.getView().setToDefaultState()
        activity.getView().updateCompletedTitleBar()
        activity.getView().refreshListOrderIfSorted()
        activity.updateRoutineList()
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

        activity.getView().updateRoutineNumbers()
        activity.getView().updateCompletedTitleBar()
        activity.updateRoutineList()
    }

    fun deleteTask(task: Task) {
        beginTransition(activity.routineListParent)
        val taskView = activity.getView().getTaskView(task.getTaskId())
        if(task.isCompleted()) activity.completedTaskContainer.removeView(taskView)
        else activity.taskContainer.removeView(taskView)
        list.deleteTask(task)

        activity.getView().updateRoutineNumbers()
        activity.getView().updateCompletedTitleBar()
        activity.updateRoutineList()
    }

    fun removeListDate() {
        if(list.hasDate()) list.setDate(null)
        if(list.isRepeating()) list.clearRepeatingDays()
        activity.getView().removeListDate()
        activity.updateRoutineList()
    }

    fun setListRepeatingDays(listRepeatingDays: ArrayList<Int>) {
        if(list.hasDate()) list.setDate(null)
        val repeatingList = ArrayList<Int>()
        for(day in listRepeatingDays) repeatingList.add(day)
        list.setRepeatingDays(repeatingList)
        activity.getView().displayListDate()
        activity.updateRoutineList()
    }

    fun setListDate(date: Calendar) {
        if(list.isRepeating()) list.clearRepeatingDays()
        val listDate = createCalendar(date.get(Calendar.YEAR),
            date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH))
        list.setDate(listDate)
        activity.getView().displayListDate()
        activity.updateRoutineList()
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
        for(taskId in list.getCompletedTasks()) {
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