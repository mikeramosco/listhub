package com.justanotherdeveloper.listhub

import android.app.Activity

open class TaskEditor(private val activity: Activity,
                      private val database: ListsDatabase) {

    fun removeTaskFromCompleted(task: Task, listType: String) {
        when(listType) {
            toDoListRef -> removeToDoListTaskFromCompleted(task)
            progressListRef -> removeProgressListTaskFromCompleted(task as ProgressTask)
            routineListRef -> removeRoutineListStepFromCompleted(task)
        }
    }

    private fun removeToDoListTaskFromCompleted(task: Task) {
        task.setCompleted(false)
        val toDoList = database.getToDoList(task.getListId())
        val toDoListTask = toDoList.getTask(task.getTaskId())?: return
        toDoList.removeTaskFromCompleted(toDoListTask)
        database.updateToDoList(toDoList)
    }

    private fun removeProgressListTaskFromCompleted(task: ProgressTask) {
        val progressList = database.getProgressList(task.getListId())
        val progressListTask = progressList.getTask(task.getTaskId())?: return
        progressList.setTaskForOtherSection(progressListTask, progressList.getInProgressTitle())
        setListSection(task, progressList.getInProgressTitle(), progressList)
        database.updateProgressList(progressList)
    }

    private fun removeRoutineListStepFromCompleted(task: Task) {
        task.setCompleted(false)
        val routineList = database.getRoutineList(task.getListId())
        val routineListTask = routineList.getTask(task.getTaskId())?: return
        routineList.removeTaskFromCompleted(routineListTask)
        database.updateRoutineList(routineList)
    }

    fun markTaskCompleted(task: Task, listType: String) {
        when(listType) {
            toDoListRef -> markToDoListTaskCompleted(task)
            progressListRef -> markProgressListTaskCompleted(task as ProgressTask)
            routineListRef -> markRoutineListStepCompleted(task)
        }
    }

    private fun markToDoListTaskCompleted(task: Task) {
        task.setCompleted(true)
        val toDoList = database.getToDoList(task.getListId())
        val toDoListTask = toDoList.getTask(task.getTaskId())?: return
        toDoList.markTaskCompleted(toDoListTask)
        if(task.isRepeating() && !task.alreadyRepeated())
            copyToDoListTask(task, toDoListTask, toDoList)
        if(task.hasReward() && !task.rewardAlreadyAdded())
            addToDoListRewardTask(task, toDoListTask, toDoList)
        database.updateToDoList(toDoList)
    }

    private fun copyToDoListTask(task: Task, toDoListTask: Task, toDoList: ToDoList) {
        task.setRepeated()
        toDoListTask.setRepeated()
        val taskId = toDoList.addNewTaskId()
        val taskCopy = Task(toDoListTask, taskId)
        taskCopy.setAsRepeat()
        taskCopy.setCompleted(false)
        toDoList.add(taskCopy)
    }

    private fun addToDoListRewardTask(task: Task, toDoListTask: Task, toDoList: ToDoList) {
        task.setRewardAdded()
        toDoListTask.setRewardAdded()
        val rewardTask = Task(toDoList.getListId())
        rewardTask.setTaskId(toDoList.addNewTaskId())
        rewardTask.setTask(toDoListTask.getReward())
        rewardTask.setTaskForReward(toDoListTask.getTask())
        rewardTask.setNote(activity.getString(
            R.string.taskForRewardNote, toDoListTask.getTask()))
        toDoList.add(rewardTask)
    }

    private fun markProgressListTaskCompleted(task: ProgressTask) {
        val progressList = database.getProgressList(task.getListId())
        val progressListTask = progressList.getTask(task.getTaskId())?: return
        progressList.setTaskForOtherSection(progressListTask, progressList.getCompletedTitle())
        setListSection(task, progressList.getCompletedTitle(), progressList)
        if(task.isRepeating() && !task.alreadyRepeated())
            copyProgressListTask(task, progressListTask, progressList)
        if(task.hasReward() && !task.rewardAlreadyAdded())
            addProgressListRewardTask(task, progressListTask, progressList)
        database.updateProgressList(progressList)
    }

    private fun setListSection(task: ProgressTask, otherSection: String,
                               progressList: ProgressList) {
        val currentSection = task.getListSectionOfTask()
        if(currentSection == progressList.getInProgressTitle()) task.setInProgress(false)
        else if(currentSection == progressList.getCompletedTitle()) task.setCompleted(false)
        task.setListSectionOfTask(otherSection)
        if(otherSection == progressList.getInProgressTitle()) task.setInProgress(true)
        else if(otherSection == progressList.getCompletedTitle()) task.setCompleted(true)
        updateTitleBars(currentSection, otherSection, progressList)
    }

    private fun updateTitleBars(currentSection: String, otherSection: String, progressList: ProgressList) {
        var currentFound = false
        var otherFound = false

        for((i, section) in progressList.getListSections().withIndex()) {
            val sectionTitle = progressList.getListSectionTitle(i)
            if(sectionTitle == currentSection || sectionTitle == otherSection) {
                if(sectionTitle == currentSection) currentFound = true
                if(sectionTitle == otherSection) otherFound = true
                if(section.size == 0 && progressList.listSectionShown(i)
                    && sectionTitle == currentSection)
                    progressList.toggleSectionVisibility(i)
                else if(section.size == 1 && !progressList.listSectionShown(i)
                    && sectionTitle == otherSection)
                    progressList.toggleSectionVisibility(i)
            }
            if(currentFound && otherFound) return
        }

        var sectionTitle = progressList.getInProgressTitle()
        if(sectionTitle == currentSection || sectionTitle == otherSection) {
            if (sectionTitle == currentSection) currentFound = true
            if (sectionTitle == otherSection) otherFound = true
            if (progressList.getCurrentTasks().size == 0
                && progressList.inProgressShown()
                && sectionTitle == currentSection)
                progressList.toggleInProgressVisibility()
            else if (progressList.getCurrentTasks().size == 1
                && !progressList.inProgressShown()
                && sectionTitle == otherSection)
                progressList.toggleInProgressVisibility()
        }

        if(currentFound && otherFound) return

        sectionTitle = progressList.getCompletedTitle()
        if(progressList.getCompletedTasks().size == 0
            && progressList.completedShown()
            && sectionTitle == currentSection)
            progressList.toggleCompletedVisibility()
        else if(progressList.getCompletedTasks().size == 1
            && !progressList.completedShown()
            && sectionTitle == otherSection)
            progressList.toggleCompletedVisibility()
    }

    private fun copyProgressListTask(task: ProgressTask,
                                     progressListTask: ProgressTask,
                                     progressList: ProgressList) {
        task.setRepeated()
        progressListTask.setRepeated()
        val taskId = progressList.addNewTaskId()
        val taskCopy = ProgressTask(progressListTask, taskId)
        taskCopy.setAsRepeat()
        setListSection(taskCopy, progressList
            .getInProgressTitle(), progressList)
        progressList.add(taskCopy)
    }

    private fun addProgressListRewardTask(task: ProgressTask,
                                          progressListTask: ProgressTask,
                                          progressList: ProgressList) {
        task.setRewardAdded()
        progressListTask.setRewardAdded()
        val rewardTask = ProgressTask(progressList.getListId())
        rewardTask.setTaskId(progressList.addNewTaskId())
        rewardTask.setTask(progressListTask.getReward())
        rewardTask.setTaskForReward(progressListTask.getTask())
        rewardTask.setNote(activity.getString(
            R.string.taskForRewardNote, progressListTask.getTask()))
        setListSection(rewardTask, progressList
            .getInProgressTitle(), progressList)
        progressList.add(rewardTask)
    }

    private fun markRoutineListStepCompleted(task: Task) {
        task.setCompleted(false)
        val routineList = database.getRoutineList(task.getListId())
        val routineListTask = routineList.getTask(task.getTaskId())?: return
        routineList.markTaskCompleted(routineListTask)
        database.updateRoutineList(routineList)
    }

    fun removeTaskFromStarred(task: Task, listType: String) {
        when(listType) {
            toDoListRef -> removeToDoListTaskFromStarred(task)
            progressListRef -> removeProgressListTaskFromStarred(task as ProgressTask)
            routineListRef -> removeRoutineListStepFromStarred(task)
            bulletedListRef -> removeBulletpointFromStarred(task)
        }
    }

    private fun removeToDoListTaskFromStarred(task: Task) {
        task.setStarred(false)
        val toDoList = database.getToDoList(task.getListId())
        val toDoListTask = toDoList.getTask(task.getTaskId())?: return
        toDoList.removeTaskFromStarred(toDoListTask)
        database.updateToDoList(toDoList)
    }

    private fun removeProgressListTaskFromStarred(task: ProgressTask) {
        task.setStarred(false)
        val progressList = database.getProgressList(task.getListId())
        val progressListTask = progressList.getTask(task.getTaskId())?: return
        progressList.removeTaskFromStarred(progressListTask)
        database.updateProgressList(progressList)
    }

    private fun removeRoutineListStepFromStarred(task: Task) {
        task.setStarred(false)
        val routineList = database.getRoutineList(task.getListId())
        val routineListTask = routineList.getTask(task.getTaskId())?: return
        routineList.removeTaskFromStarred(routineListTask)
        database.updateRoutineList(routineList)
    }

    private fun removeBulletpointFromStarred(task: Task) {
        task.setStarred(false)
        val bulletedList = database.getBulletedList(task.getListId())
        val bulletedListTask = bulletedList.getTask(task.getTaskId())?: return
        bulletedList.removeTaskFromStarred(bulletedListTask)
        database.updateBulletedList(bulletedList)
    }

    fun addTaskToStarred(task: Task, listType: String) {
        when(listType) {
            toDoListRef -> addToDoListTaskToStarred(task)
            progressListRef -> addProgressListTaskToStarred(task as ProgressTask)
            routineListRef -> addRoutineListStepToStarred(task)
            bulletedListRef -> addBulletpointToStarred(task)
        }
    }

    private fun addToDoListTaskToStarred(task: Task) {
        task.setStarred()
        val toDoList = database.getToDoList(task.getListId())
        val toDoListTask = toDoList.getTask(task.getTaskId())?: return
        toDoList.addTaskToStarred(toDoListTask)
        database.updateToDoList(toDoList)
    }

    private fun addProgressListTaskToStarred(task: ProgressTask) {
        task.setStarred()
        val progressList = database.getProgressList(task.getListId())
        val progressListTask = progressList.getTask(task.getTaskId())?: return
        progressList.addTaskToStarred(progressListTask)
        database.updateProgressList(progressList)
    }

    private fun addRoutineListStepToStarred(task: Task) {
        task.setStarred()
        val routineList = database.getRoutineList(task.getListId())
        val routineListTask = routineList.getTask(task.getTaskId())?: return
        routineList.addTaskToStarred(routineListTask)
        database.updateRoutineList(routineList)
    }

    private fun addBulletpointToStarred(task: Task) {
        task.setStarred()
        val bulletedList = database.getBulletedList(task.getListId())
        val bulletedListTask = bulletedList.getTask(task.getTaskId())?: return
        bulletedList.addTaskToStarred(bulletedListTask)
        database.updateBulletedList(bulletedList)
    }
}