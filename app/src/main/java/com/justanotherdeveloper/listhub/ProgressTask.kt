package com.justanotherdeveloper.listhub

class ProgressTask: Task {

    private var listSectionOfTask = ""
    private var inProgress = false

    constructor(listId: Int): super(listId)

    constructor(otherTask: ProgressTask, taskId: Int): super(otherTask, taskId) {
        listSectionOfTask = otherTask.listSectionOfTask
        inProgress = otherTask.inProgress
    }

    constructor(otherTask: Task, taskId: Int): super(otherTask, taskId)

    fun setListSectionOfTask(listSectionOfTask: String) {
        this.listSectionOfTask = listSectionOfTask
    }

    fun getListSectionOfTask(): String {
        return listSectionOfTask
    }

    fun setInProgress(inProgress: Boolean = true) {
        this.inProgress = inProgress
    }

    fun isInProgress(): Boolean {
        return inProgress
    }
}