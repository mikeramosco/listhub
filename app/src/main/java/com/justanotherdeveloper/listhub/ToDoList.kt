package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint

class ToDoList: List(toDoListRef) {

    @SuppressLint("UseSparseArrays")
    private var taskMap = HashMap<Int, Task?>()

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

    fun addNewTaskId(): Int {
        val taskId = generateId(taskIds)
        taskIds.add(taskId)
        return taskId
    }

    fun add(task: Task): Int {
        taskMap[task.getTaskId()] = task
        val listOfTask =
            if(task.isCompleted()) completed else list
        return if(task.isStarred()) {
            listOfTask.add(0, task.getTaskId())
            0
        } else addTaskAfterStarred(task, listOfTask)
    }

    private fun addTaskAfterStarred(task: Task, listOfTask: ArrayList<Int>): Int {
        var index = 0

        removeNullTaskIds(listOfTask)

        for(savedTaskId in listOfTask) {
            val savedTask = taskMap[savedTaskId]
            if (savedTask != null) {
                if (!savedTask.isStarred()) break
                index++
            }
        }
        listOfTask.add(index, task.getTaskId())

        return index
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

    private fun moveTaskToList(task: Task, fromList: ArrayList<Int>,
                               toList: ArrayList<Int>): Int {
        fromList.remove(task.getTaskId())
        return if(task.isStarred()) {
            toList.add(0, task.getTaskId())
            0
        } else addTaskAfterStarred(task, toList)
    }

    fun taskIsCompleted(task: Task): Boolean {
        return completed.contains(task.getTaskId())
    }

    fun getTask(taskId: Int): Task? {
        return taskMap[taskId]
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
}