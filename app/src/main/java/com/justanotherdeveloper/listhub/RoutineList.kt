package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.content.Context
import java.util.*
import kotlin.collections.ArrayList

class RoutineList: List(routineListRef) {

    @SuppressLint("UseSparseArrays")
    private var taskMap = HashMap<Int, Task?>()
    private var date: Calendar? = null
    private var repeatingDays = ArrayList<Int>()
    private var showRoutineNumbers = true

    fun deleteTask(task: Task, taskId: Int = task.getTaskId()) {
        removeTaskFromList(task, taskId)
        taskIds.remove(taskId)
        taskMap.remove(taskId)
    }

    private fun removeTaskFromList(task: Task, taskId: Int) {
        val removed =
            if(task.isCompleted())
                completed.remove(taskId)
            else list.remove(taskId)
        if(!removed)
            if(!task.isCompleted())
                completed.remove(taskId)
            else list.remove(taskId)
    }

    fun toggleRoutineNumbersVisibility() {
        showRoutineNumbers = !showRoutineNumbers
    }

    fun routineNumbersShown(): Boolean {
        return showRoutineNumbers
    }

    fun addNewTaskId(): Int {
        val taskId = generateId(taskIds)
        taskIds.add(taskId)
        return taskId
    }

    fun getRoutineTaskIdsOrder(): ArrayList<Int> {
        return taskIds
    }

    fun add(task: Task): Int {
        taskMap[task.getTaskId()] = task
        val listOfTask =
            if(task.isCompleted()) completed else list
        return if(task.isStarred()) {
            taskIds.remove(task.getTaskId())
            taskIds.add(0, task.getTaskId())
            listOfTask.add(0, task.getTaskId())
            0
        } else {
            removeNullTaskIds(listOfTask)
            listOfTask.add(task.getTaskId())
            listOfTask.lastIndex
        }
    }

    private fun removeNullTaskIds(listOfTask: ArrayList<Int>) {
        val nullTaskIds = ArrayList<Int>()

        for(taskId in listOfTask)
            if(getTask(taskId) == null)
                nullTaskIds.add(taskId)

        if(nullTaskIds.isNotEmpty())
            for(taskId in nullTaskIds)
                listOfTask.remove(taskId)
    }

    fun getTask(taskId: Int): Task? {
        return taskMap[taskId]
    }

    private fun addTaskAfterStarred(task: Task, listOfTask: ArrayList<Int>): Int {
        addTaskIdAfterStarred(task)
        var index = 0

        for(savedTaskId in listOfTask) {
            val savedTask = taskMap[savedTaskId]
            if(savedTask != null) {
                if (!savedTask.isStarred()) break
                index++
            }
        }
        listOfTask.add(index, task.getTaskId())

        return index
    }

    private fun moveTaskToList(task: Task, fromList: ArrayList<Int>,
                               toList: ArrayList<Int>): Int {
        fromList.remove(task.getTaskId())

        var toListIndex = 0
        for(taskId in taskIds) {
            if(toListIndex == toList.size) {
                toList.add(task.getTaskId())
                break
            }
            if(taskId == toList[toListIndex])
                toListIndex++
            else if(taskId == task.getTaskId()) {
                toList.add(toListIndex, task.getTaskId())
                break
            }
        }
        return toListIndex
    }

    fun taskIsCompleted(task: Task): Boolean {
        return completed.contains(task.getTaskId())
    }

    fun markTaskCompleted(task: Task): Int {
        task.setCompleted(true)
        return moveTaskToList(task, list, completed)
    }

    fun removeTaskFromCompleted(task: Task): Int {
        task.setCompleted(false)
        return moveTaskToList(task, completed, list)
    }

    fun addTaskToStarred(task: Task) {
        task.setStarred()
        taskIds.remove(task.getTaskId())
        taskIds.add(0, task.getTaskId())
        if(task.isCompleted()) {
            completed.remove(task.getTaskId())
            completed.add(0, task.getTaskId())
        } else {
            list.remove(task.getTaskId())
            list.add(0, task.getTaskId())
        }
    }

    fun removeTaskFromStarred(task: Task): Int {
        task.setStarred(false)

        val listOfTask =
            if(task.isCompleted()) completed else list

        listOfTask.remove(task.getTaskId())

        return addTaskAfterStarred(task, listOfTask)
    }

    private fun addTaskIdAfterStarred(task: Task) {
        taskIds.remove(task.getTaskId())

        var index = 0

        for(savedTask in getRoutineTasksInOrder()) {
            if (!savedTask.isStarred()) break
            index++
        }
        taskIds.add(index, task.getTaskId())
    }

    fun getRoutineTasksInOrder(): ArrayList<Task> {
        val routineTasksInOrder = ArrayList<Task>()
        removeNullTaskIds(list)
        removeNullTaskIds(completed)
        for(id in taskIds) {
            var taskFound = false

            fun taskAdded(task: Task): Boolean {
                return if(id == task.getTaskId()) {
                    taskFound = true
                    routineTasksInOrder.add(task)
                    true
                } else false
            }

            for(taskId in list) {
                val task = taskMap[taskId]
                if(task != null && taskAdded(task)) break
            }

            if(!taskFound) {
                for (taskId in completed) {
                    val task = taskMap[taskId]
                    if (task != null && taskAdded(task)) break
                }
            }
        }
        return routineTasksInOrder
    }

    private fun refreshListOrder(list: ArrayList<Int>) {
        val taskHolder = ArrayList<Int>()
        for(task in list) taskHolder.add(task)
        list.clear()
        for(id in taskIds) {
            for(taskId in taskHolder) {
                if(id == taskId) {
                    list.add(taskId)
                    break
                }
            }
        }
    }

    fun refreshRoutineListsOrder() {
        refreshListOrder(list)
        refreshListOrder(completed)
    }

    fun setDate(date: Calendar?) {
        date?.resetTimeOfDay()
        this.date = date
    }

    fun getDate(): Calendar? {
        date?.resetTimeOfDay()
        return date
    }

    fun hasDate(): Boolean {
        return date != null
    }

    fun getDueDate(dateCursor: Calendar = getTodaysDate()): Calendar? {
        if(date != null) return getDate()
        if(isRepeating()) {
            var dayOfWeek = dateCursor.get(Calendar.DAY_OF_WEEK)
            while(true) {
                if (repeatingDays.contains(dayOfWeek)) {
                    dateCursor.resetTimeOfDay()
                    return dateCursor
                }
                dateCursor.add(Calendar.DATE, 1)
                dayOfWeek = dateCursor.get(Calendar.DAY_OF_WEEK)
            }
        }
        return null
    }

    fun clearRepeatingDays() {
        repeatingDays.clear()
    }

    fun setRepeatingDays(repeatingDays: ArrayList<Int>) {
        this.repeatingDays = repeatingDays
    }

    fun getRepeatingDays(): ArrayList<Int> {
        return repeatingDays
    }

    fun getRepeatingDaysString(context: Context): String {
        return context.getRepeatingDaysString(repeatingDays)
    }

    fun isRepeating(): Boolean {
        return repeatingDays.size > 0
    }
}