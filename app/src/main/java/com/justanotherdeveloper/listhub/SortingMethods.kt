package com.justanotherdeveloper.listhub

fun getSortedTaskOrder(sortIndex: Int, tasks: ArrayList<Task>): ArrayList<Task> {
    return when(sortIndex) {
        sortDueDateDescendingIndex -> sortTasksByDueDate(tasks)
        sortDueDateAscendingIndex -> sortTasksByDueDate(tasks, false)
        sortNewestFirstIndex -> sortTasksByCreationDate(tasks)
        sortOldestFirstIndex -> sortTasksByCreationDate(tasks, false)
        sortAToZIndex -> sortTasksAlphabetically(tasks)
        sortZToAIndex -> sortTasksAlphabetically(tasks, false)
        else -> tasks
    }
}

private fun sortTasksByDueDate(tasks: ArrayList<Task>,
                               sortNearFirst: Boolean = true): ArrayList<Task> {
    val sortedTasks = ArrayList<Task>()
    for(taskToAdd in tasks) {
        var taskAdded = false
        for ((index, task) in sortedTasks.withIndex()) {
            val taskDueDate = task.getDueDate()
            val taskToAddDueDate = taskToAdd.getDueDate()
            if (taskDueDate == null && taskToAddDueDate != null) {
                sortedTasks.add(index, taskToAdd)
                taskAdded = true
                break
            } else if (taskDueDate != null && taskToAddDueDate != null) {
                if(datesAreTheSame(taskDueDate, taskToAddDueDate)) {
                    if (!task.hasTime() && taskToAdd.hasTime()) {
                        sortedTasks.add(index, taskToAdd)
                        taskAdded = true
                        break
                    } else if (task.hasTime() == taskToAdd.hasTime()) {
                        if (sortNearFirst) {
                            if (taskToAddDueDate.comesBefore(taskDueDate)) {
                                sortedTasks.add(index, taskToAdd)
                                taskAdded = true
                                break
                            }
                        } else {
                            if (taskToAddDueDate.comesBefore(taskDueDate)) {
                                sortedTasks.add(index, taskToAdd)
                                taskAdded = true
                                break
                            }
                        }
                    }
                } else {
                    if (sortNearFirst) {
                        if (taskToAddDueDate.comesBefore(taskDueDate)) {
                            sortedTasks.add(index, taskToAdd)
                            taskAdded = true
                            break
                        }
                    } else {
                        if (taskToAddDueDate.comesAfter(taskDueDate)) {
                            sortedTasks.add(index, taskToAdd)
                            taskAdded = true
                            break
                        }
                    }
                }
            }
        }
        if (!taskAdded)
            sortedTasks.add(taskToAdd)
    }
    return sortedTasks
}

private fun sortTasksByCreationDate(tasks: ArrayList<Task>,
                                    sortNewestFirst: Boolean = true): ArrayList<Task> {
    val sortedTasks = ArrayList<Task>()
    for(taskToAdd in tasks.asReversed()) {
        var taskAdded = false
        for ((index, task) in sortedTasks.withIndex()) {
            val taskCreationDate = task.getDateCreated()
            if (sortNewestFirst) {
                if (taskToAdd.getDateCreated().comesAfter(taskCreationDate)) {
                    sortedTasks.add(index, taskToAdd)
                    taskAdded = true
                    break
                }
            } else {
                if (taskToAdd.getDateCreated().comesBefore(taskCreationDate)) {
                    sortedTasks.add(index, taskToAdd)
                    taskAdded = true
                    break
                }
            }
        }
        if (!taskAdded)
            sortedTasks.add(taskToAdd)
    }
    return sortedTasks
}

private fun sortTasksAlphabetically(tasks: ArrayList<Task>,
                                    sortAToZ: Boolean = true): ArrayList<Task> {
    val sortedTasks = ArrayList<Task>()
    for(taskToAdd in tasks.asReversed()) {
        var taskAdded = false
        for ((index, task) in sortedTasks.withIndex()) {
            val taskText = task.getTask()
            if (sortAToZ) {
                if (taskToAdd.getTask().comesAlphabeticallyBefore(taskText)) {
                    sortedTasks.add(index, taskToAdd)
                    taskAdded = true
                    break
                }
            } else {
                if (taskToAdd.getTask().comesAlphabeticallyAfter(taskText)) {
                    sortedTasks.add(index, taskToAdd)
                    taskAdded = true
                    break
                }
            }
        }
        if (!taskAdded)
            sortedTasks.add(taskToAdd)
    }
    return sortedTasks
}

fun sortListsByLastUpdated(lists: ArrayList<List>, sortDescending: Boolean = true): ArrayList<List> {
    val sortedLists = ArrayList<List>()
    for(listToAdd in lists) {
        var listAdded = false
        for ((index, list) in sortedLists.withIndex()) {
            val listDateUpdated = list.getDateUpdated()
            if (sortDescending) {
                if (listToAdd.getDateUpdated().comesAfter(listDateUpdated)) {
                    sortedLists.add(index, listToAdd)
                    listAdded = true
                    break
                }
            } else {
                if (listToAdd.getDateUpdated().comesBefore(listDateUpdated)) {
                    sortedLists.add(index, listToAdd)
                    listAdded = true
                    break
                }
            }
        }
        if (!listAdded)
            sortedLists.add(listToAdd)
    }
    return sortedLists
}

fun sortListsByCreationDate(lists: ArrayList<List>, sortNewestFirst: Boolean = true): ArrayList<List> {
    val sortedLists = ArrayList<List>()
    for(listToAdd in lists.asReversed()) {
        var listAdded = false
        for ((index, list) in sortedLists.withIndex()) {
            val listCreationDate = list.getDateCreated()
            if (sortNewestFirst) {
                if (listToAdd.getDateCreated().comesAfter(listCreationDate)) {
                    sortedLists.add(index, listToAdd)
                    listAdded = true
                    break
                }
            } else {
                if (listToAdd.getDateCreated().comesBefore(listCreationDate)) {
                    sortedLists.add(index, listToAdd)
                    listAdded = true
                    break
                }
            }
        }
        if (!listAdded)
            sortedLists.add(listToAdd)
    }
    return sortedLists
}

fun sortListsAlphabetically(lists: ArrayList<List>, sortAToZ: Boolean = true): ArrayList<List> {
    val sortedLists = ArrayList<List>()
    for(listToAdd in lists.asReversed()) {
        var listAdded = false
        for ((index, list) in sortedLists.withIndex()) {
            val listTitle = list.getTitle()
            if (sortAToZ) {
                if (listToAdd.getTitle().comesAlphabeticallyBefore(listTitle)) {
                    sortedLists.add(index, listToAdd)
                    listAdded = true
                    break
                }
            } else {
                if (listToAdd.getTitle().comesAlphabeticallyAfter(listTitle)) {
                    sortedLists.add(index, listToAdd)
                    listAdded = true
                    break
                }
            }
        }
        if (!listAdded)
            sortedLists.add(listToAdd)
    }
    return sortedLists
}