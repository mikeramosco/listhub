package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint

class BulletedList: List(bulletedListRef) {

    @SuppressLint("UseSparseArrays")
    private var taskMap = HashMap<Int, Task?>()

    fun deleteTask(taskId: Int) {
        list.remove(taskId)
        taskIds.remove(taskId)
        taskMap.remove(taskId)
    }

    fun addNewTaskId(): Int {
        val taskId = generateId(taskIds)
        taskIds.add(taskId)
        return taskId
    }

    fun add(task: Task): Int {
        taskMap[task.getTaskId()] = task
        return if(task.isStarred()) {
            list.add(0, task.getTaskId())
            0
        } else addTaskAfterStarred(task)
    }

    private fun addTaskAfterStarred(task: Task): Int {
        var index = 0

        removeNullTaskIds()

        for(savedTaskId in list) {
            val savedTask = taskMap[savedTaskId]
            if (savedTask != null) {
                if (!savedTask.isStarred()) break
                index++
            }
        }
        list.add(index, task.getTaskId())

        return index
    }

    private fun removeNullTaskIds() {
        val nullTaskIds = ArrayList<Int>()

        for(taskId in list)
            if(getTask(taskId) == null)
                nullTaskIds.add(taskId)

        if(nullTaskIds.isNotEmpty())
            for(taskId in nullTaskIds)
                list.remove(taskId)
    }

    fun getTask(taskId: Int): Task? {
        return taskMap[taskId]
    }

    fun addTaskToStarred(task: Task) {
        task.setStarred()
        list.remove(task.getTaskId())
        list.add(0, task.getTaskId())
    }

    fun removeTaskFromStarred(task: Task): Int {
        task.setStarred(false)
        list.remove(task.getTaskId())
        return addTaskAfterStarred(task)
    }
}