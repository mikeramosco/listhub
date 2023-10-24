package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint

class ProgressList: List(progressListRef) {

    @SuppressLint("UseSparseArrays")
    private var taskMap = HashMap<Int, ProgressTask?>()
    private var listSections = ArrayList<ArrayList<Int>>()
    private var listSectionsTitles = ArrayList<String>()
    private var inProgressTitle = ""
    private var completedTitle = ""
    private var showListSections = ArrayList<Boolean>()
    private var showInProgress = false

    init { toggleCompletedVisibility() }

    fun deleteTask(task: ProgressTask, taskId: Int = task.getTaskId()) {
        val removed = getListOfTask(task).remove(taskId)
        if(!removed) removeTaskFromList(taskId)
        taskIds.remove(taskId)
        taskMap.remove(taskId)
    }

    private fun removeTaskFromList(taskId: Int) {
        var removed = list.remove(taskId)
        if(removed) return
        removed = completed.remove(taskId)
        if(removed) return
        for(section in listSections) {
            removed = section.remove(taskId)
            if(removed) return
        }
    }

    fun renameCompletedList(completedTitle: String) {
        this.completedTitle = completedTitle
        for(taskId in completed) {
            val task = taskMap[taskId]
            task?.setListSectionOfTask(completedTitle)
        }
    }

    fun getCompletedTitle(): String {
        return completedTitle
    }

    fun renameInProgressList(inProgressTitle: String) {
        this.inProgressTitle = inProgressTitle
        for(taskId in list) {
            val task = taskMap[taskId]
            task?.setListSectionOfTask(inProgressTitle)
        }
    }

    fun getInProgressTitle(): String {
        return inProgressTitle
    }

    fun renameListSectionTitle(index: Int, title: String) {
        listSectionsTitles[index] = title
        for(taskId in listSections[index]) {
            val task = taskMap[taskId]
            task?.setListSectionOfTask(title)
        }
    }

    fun getListSectionTitle(index: Int): String {
        return listSectionsTitles[index]
    }

    fun getListSectionIndex(section: String): Int {
        return listSectionsTitles.indexOf(section)
    }

    fun getListSectionTitles(): ArrayList<String> {
        return listSectionsTitles
    }

    fun getAllListSectionTitles(): ArrayList<String> {
        val allListSectionTitles = ArrayList<String>()
        for(section in listSectionsTitles)
            allListSectionTitles.add(section)
        allListSectionTitles.add(inProgressTitle)
        allListSectionTitles.add(completedTitle)
        return allListSectionTitles
    }

    fun addListSection(title: String) {
        listSections.add(0, ArrayList())
        listSectionsTitles.add(0, title)
        showListSections.add(0, false)
    }

    fun removeListSection(index: Int) {
        for(taskId in listSections[index])
            taskIds.remove(taskId)
        listSections.removeAt(index)
        listSectionsTitles.removeAt(index)
        showListSections.removeAt(index)
    }

    fun getListSectionsVisibility(): ArrayList<Boolean> {
        return showListSections
    }

    fun toggleSectionVisibility(index: Int) {
        showListSections[index] = !showListSections[index]
    }

    fun listSectionShown(index: Int): Boolean {
        return showListSections[index]
    }

    fun toggleInProgressVisibility() {
        showInProgress = !showInProgress
    }

    fun inProgressShown(): Boolean {
        return showInProgress
    }

    fun addNewTaskId(): Int {
        val taskId = generateId(taskIds)
        taskIds.add(taskId)
        return taskId
    }

    fun add(task: ProgressTask): Int {
        taskMap[task.getTaskId()] = task
        val listOfTask = getListOfTask(task)
        return if(task.isStarred()) {
            listOfTask.add(0, task.getTaskId())
            0
        } else addTaskAfterStarred(task, listOfTask)
    }

    private fun addTaskAfterStarred(task: ProgressTask, listOfTask: ArrayList<Int>): Int {
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

    private fun moveTaskToList(task: ProgressTask, fromList: ArrayList<Int>,
                               toList: ArrayList<Int>): Int {
        fromList.remove(task.getTaskId())
        return if(task.isStarred()) {
            toList.add(0, task.getTaskId())
            0
        } else addTaskAfterStarred(task, toList)
    }

    fun getTask(taskId: Int): ProgressTask? {
        return taskMap[taskId]
    }

    fun setTaskForOtherSection(task: ProgressTask, otherSection: String): Int {
        val currentSection = task.getListSectionOfTask()
        if(currentSection == inProgressTitle) task.setInProgress(false)
        else if(currentSection == completedTitle) task.setCompleted(false)
        val listOfTask = getListOfTask(task)
        task.setListSectionOfTask(otherSection)
        val nextListOfTask = getListOfTask(task)
        if(otherSection == inProgressTitle) task.setInProgress(true)
        else if(otherSection == completedTitle) task.setCompleted(true)
        return moveTaskToList(task, listOfTask, nextListOfTask)
    }

    fun addTaskToStarred(task: ProgressTask) {
        task.setStarred()
        val listOfTask = getListOfTask(task)
        listOfTask.remove(task.getTaskId())
        listOfTask.add(0, task.getTaskId())
    }

    fun removeTaskFromStarred(task: ProgressTask): Int {
        task.setStarred(false)
        val listOfTask = getListOfTask(task)
        listOfTask.remove(task.getTaskId())
        return addTaskAfterStarred(task, listOfTask)
    }

    private fun getListOfTask(task: ProgressTask): ArrayList<Int> {
        return when (val listTitleOfTask = task.getListSectionOfTask()) {
            inProgressTitle -> list
            completedTitle -> completed
            else -> {
                var listOfTask = ArrayList<Int>()
                for((i, listTitle) in listSectionsTitles.withIndex()) {
                    if(listTitle == listTitleOfTask) {
                        listOfTask = listSections[i]
                        break
                    }
                }
                listOfTask
            }
        }
    }

    fun getNextListOfTaskTitle(task: ProgressTask): String {
        return when (val listTitleOfTask = task.getListSectionOfTask()) {
            inProgressTitle -> completedTitle
            else -> {
                val lastIndex = listSectionsTitles.lastIndex
                var listOfTask = ""
                for((i, listTitle) in listSectionsTitles.withIndex()) {
                    if(listTitle == listTitleOfTask) {
                        listOfTask = if(i == lastIndex)
                            inProgressTitle else listSectionsTitles[i + 1]
                        break
                    }
                }
                listOfTask
            }
        }
    }

    fun getListSectionTasks(index: Int): ArrayList<Int> {
        return listSections[index]
    }

    fun getListSections(): ArrayList<ArrayList<Int>> {
        return listSections
    }

    fun moveSection(index: Int, moveUp: Boolean) {
        val section = listSections[index]
        val sectionTitle = listSectionsTitles[index]
        val sectionShown = showListSections[index]

        listSections.removeAt(index)
        listSectionsTitles.removeAt(index)
        showListSections.removeAt(index)

        val newIndex = if(moveUp) index-1 else index+1

        listSections.add(newIndex, section)
        listSectionsTitles.add(newIndex, sectionTitle)
        showListSections.add(newIndex, sectionShown)
    }
}