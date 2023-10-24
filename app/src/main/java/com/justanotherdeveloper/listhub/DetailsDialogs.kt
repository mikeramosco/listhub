package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

@SuppressLint("InflateParams")
fun Activity.openToDoListDetailsDialog(list: ToDoList, listField: EditText? = null) {
    val view = layoutInflater.inflate(R.layout.dialog_view_list_details, null)
    val builder = AlertDialog.Builder(this)
    builder.setCancelable(false)
    builder.setView(view)
    val confirmDeleteNoteDialog = builder.create()
    confirmDeleteNoteDialog.setCancelable(true)

    val listTitleDetailsText = view.findViewById<TextView>(R.id.listTitleDetailsText)
    val numTasksDetailsText = view.findViewById<TextView>(R.id.numTasksDetailsText)
    val numCompletedLabelText = view.findViewById<TextView>(R.id.numCompletedLabelText)
    val numCompletedDetailsText = view.findViewById<TextView>(R.id.numCompletedDetailsText)
    val dateCreatedDetailsText = view.findViewById<TextView>(R.id.dateCreatedDetailsText)
    val labelsLabelText = view.findViewById<TextView>(R.id.labelsLabelText)
    val labelsDetailsText = view.findViewById<TextView>(R.id.labelsDetailsText)
    val archivedLabelText = view.findViewById<TextView>(R.id.archivedLabelText)
    val archivedDetailsText = view.findViewById<TextView>(R.id.archivedDetailsText)
    val noteLabelText = view.findViewById<TextView>(R.id.noteLabelText)
    val noteDetailsText = view.findViewById<TextView>(R.id.noteDetailsText)

    if(list.hasNote()) {
        noteLabelText.visibility = View.VISIBLE
        noteDetailsText.visibility = View.VISIBLE
        noteDetailsText.text = list.getNote()
    }

    var taskDetails = listField?.text?.toString()?: ""
    if(taskDetails.isEmpty()) taskDetails = list.getTitle()
    listTitleDetailsText.text =
        if(taskDetails.isNotEmpty()) taskDetails
        else getString(R.string.emptyDetailString)

    numTasksDetailsText.text = list.getCurrentTasks().size.toString()

    numCompletedLabelText.visibility = View.VISIBLE
    numCompletedDetailsText.visibility = View.VISIBLE
    numCompletedDetailsText.text = list.getCompletedTasks().size.toString()

    dateCreatedDetailsText.text = getDateAndTimeString(list.getDateCreated())

    if(list.hasLabels()) {
        labelsLabelText.visibility = View.VISIBLE
        labelsDetailsText.visibility = View.VISIBLE
        labelsDetailsText.text = list.getLabelsString()
    }

    if(list.isArchived()) {
        archivedLabelText.visibility = View.VISIBLE
        archivedDetailsText.visibility = View.VISIBLE
    }

    view.findViewById<TextView>(R.id.closeOption).setOnClickListener {
        confirmDeleteNoteDialog.cancel()
    }

    confirmDeleteNoteDialog.show()
}

@SuppressLint("InflateParams")
fun Activity.openProgressListDetailsDialog(list: ProgressList, listField: EditText? = null) {
    val view = layoutInflater.inflate(R.layout.dialog_view_list_details, null)
    val builder = AlertDialog.Builder(this)
    builder.setCancelable(false)
    builder.setView(view)
    val confirmDeleteNoteDialog = builder.create()
    confirmDeleteNoteDialog.setCancelable(true)

    val listDetailsTitleText = view.findViewById<TextView>(R.id.listDetailsTitleText)
    val listTitleDetailsText = view.findViewById<TextView>(R.id.listTitleDetailsText)
    val numTasksDetailsText = view.findViewById<TextView>(R.id.numTasksDetailsText)
    val numCompletedLabelText = view.findViewById<TextView>(R.id.numCompletedLabelText)
    val numCompletedDetailsText = view.findViewById<TextView>(R.id.numCompletedDetailsText)
    val numSectionsLabelText = view.findViewById<TextView>(R.id.numSectionsLabelText)
    val numSectionsDetailsText = view.findViewById<TextView>(R.id.numSectionsDetailsText)
    val dateCreatedDetailsText = view.findViewById<TextView>(R.id.dateCreatedDetailsText)
    val labelsLabelText = view.findViewById<TextView>(R.id.labelsLabelText)
    val labelsDetailsText = view.findViewById<TextView>(R.id.labelsDetailsText)
    val archivedLabelText = view.findViewById<TextView>(R.id.archivedLabelText)
    val archivedDetailsText = view.findViewById<TextView>(R.id.archivedDetailsText)
    val noteLabelText = view.findViewById<TextView>(R.id.noteLabelText)
    val noteDetailsText = view.findViewById<TextView>(R.id.noteDetailsText)

    if(list.hasNote()) {
        noteLabelText.visibility = View.VISIBLE
        noteDetailsText.visibility = View.VISIBLE
        noteDetailsText.text = list.getNote()
    }

    listDetailsTitleText.text = getString(R.string.sectionsListDetails)

    var taskDetails = listField?.text?.toString()?: ""
    if(taskDetails.isEmpty()) taskDetails = list.getTitle()
    listTitleDetailsText.text =
        if(taskDetails.isNotEmpty()) taskDetails
        else getString(R.string.emptyDetailString)

    var numTasks = list.getCurrentTasks().size
    var numSections = 2
    for(section in list.getListSections()) {
        numTasks += section.size
        numSections++
    }
    numTasksDetailsText.text = numTasks.toString()
    numSectionsLabelText.visibility = View.VISIBLE
    numSectionsDetailsText.visibility = View.VISIBLE
    numSectionsDetailsText.text = numSections.toString()

    numCompletedLabelText.visibility = View.VISIBLE
    numCompletedDetailsText.visibility = View.VISIBLE
    numCompletedDetailsText.text = list.getCompletedTasks().size.toString()

    dateCreatedDetailsText.text = getDateAndTimeString(list.getDateCreated())

    if(list.hasLabels()) {
        labelsLabelText.visibility = View.VISIBLE
        labelsDetailsText.visibility = View.VISIBLE
        labelsDetailsText.text = list.getLabelsString()
    }

    if(list.isArchived()) {
        archivedLabelText.visibility = View.VISIBLE
        archivedDetailsText.visibility = View.VISIBLE
    }

    view.findViewById<TextView>(R.id.closeOption).setOnClickListener {
        confirmDeleteNoteDialog.cancel()
    }

    confirmDeleteNoteDialog.show()
}

@SuppressLint("InflateParams")
fun Activity.openRoutineListDetailsDialog(list: RoutineList, listField: EditText? = null) {
    val view = layoutInflater.inflate(R.layout.dialog_view_list_details, null)
    val builder = AlertDialog.Builder(this)
    builder.setCancelable(false)
    builder.setView(view)
    val confirmDeleteNoteDialog = builder.create()
    confirmDeleteNoteDialog.setCancelable(true)

    val listDetailsTitleText = view.findViewById<TextView>(R.id.listDetailsTitleText)
    val listTitleDetailsText = view.findViewById<TextView>(R.id.listTitleDetailsText)
    val numTasksLabelText = view.findViewById<TextView>(R.id.numTasksLabelText)
    val numTasksDetailsText = view.findViewById<TextView>(R.id.numTasksDetailsText)
    val dueDateLabelText = view.findViewById<TextView>(R.id.dueDateLabelText)
    val dueDateDetailsText = view.findViewById<TextView>(R.id.dueDateDetailsText)
    val dateCreatedDetailsText = view.findViewById<TextView>(R.id.dateCreatedDetailsText)
    val labelsLabelText = view.findViewById<TextView>(R.id.labelsLabelText)
    val labelsDetailsText = view.findViewById<TextView>(R.id.labelsDetailsText)
    val archivedLabelText = view.findViewById<TextView>(R.id.archivedLabelText)
    val archivedDetailsText = view.findViewById<TextView>(R.id.archivedDetailsText)
    val noteLabelText = view.findViewById<TextView>(R.id.noteLabelText)
    val noteDetailsText = view.findViewById<TextView>(R.id.noteDetailsText)

    if(list.hasNote()) {
        noteLabelText.visibility = View.VISIBLE
        noteDetailsText.visibility = View.VISIBLE
        noteDetailsText.text = list.getNote()
    }

    listDetailsTitleText.text = getString(R.string.routineListDetails)

    var taskDetails = listField?.text?.toString()?: ""
    if(taskDetails.isEmpty()) taskDetails = list.getTitle()
    listTitleDetailsText.text =
        if(taskDetails.isNotEmpty()) taskDetails
        else getString(R.string.emptyDetailString)

    numTasksLabelText.text = getString(R.string.numStepsLabel)

    val numSteps = list.getCurrentTasks().size + list.getCompletedTasks().size
    numTasksDetailsText.text = numSteps.toString()

    if(list.hasDate()) {
        val dueDate = list.getDate()
        if(dueDate != null) {
            dueDateLabelText.visibility = View.VISIBLE
            dueDateDetailsText.visibility = View.VISIBLE
            dueDateDetailsText.text = getDateText(dueDate)
        }
    } else if(list.isRepeating()) {
        dueDateLabelText.visibility = View.VISIBLE
        dueDateDetailsText.visibility = View.VISIBLE
        dueDateLabelText.text = getString(R.string.repeatsLabel)
        dueDateDetailsText.text = list.getRepeatingDaysString(this)
    }

    dateCreatedDetailsText.text = getDateAndTimeString(list.getDateCreated())

    if(list.hasLabels()) {
        labelsLabelText.visibility = View.VISIBLE
        labelsDetailsText.visibility = View.VISIBLE
        labelsDetailsText.text = list.getLabelsString()
    }

    if(list.isArchived()) {
        archivedLabelText.visibility = View.VISIBLE
        archivedDetailsText.visibility = View.VISIBLE
    }

    view.findViewById<TextView>(R.id.closeOption).setOnClickListener {
        confirmDeleteNoteDialog.cancel()
    }

    confirmDeleteNoteDialog.show()
}

@SuppressLint("InflateParams")
fun Activity.openBulletedListDetailsDialog(list: BulletedList, listField: EditText? = null) {
    val view = layoutInflater.inflate(R.layout.dialog_view_list_details, null)
    val builder = AlertDialog.Builder(this)
    builder.setCancelable(false)
    builder.setView(view)
    val confirmDeleteNoteDialog = builder.create()
    confirmDeleteNoteDialog.setCancelable(true)

    val listDetailsTitleText = view.findViewById<TextView>(R.id.listDetailsTitleText)
    val listTitleDetailsText = view.findViewById<TextView>(R.id.listTitleDetailsText)
    val numTasksLabelText = view.findViewById<TextView>(R.id.numTasksLabelText)
    val numTasksDetailsText = view.findViewById<TextView>(R.id.numTasksDetailsText)
    val dateCreatedDetailsText = view.findViewById<TextView>(R.id.dateCreatedDetailsText)
    val labelsLabelText = view.findViewById<TextView>(R.id.labelsLabelText)
    val labelsDetailsText = view.findViewById<TextView>(R.id.labelsDetailsText)
    val archivedLabelText = view.findViewById<TextView>(R.id.archivedLabelText)
    val archivedDetailsText = view.findViewById<TextView>(R.id.archivedDetailsText)
    val noteLabelText = view.findViewById<TextView>(R.id.noteLabelText)
    val noteDetailsText = view.findViewById<TextView>(R.id.noteDetailsText)

    if(list.hasNote()) {
        noteLabelText.visibility = View.VISIBLE
        noteDetailsText.visibility = View.VISIBLE
        noteDetailsText.text = list.getNote()
    }

    listDetailsTitleText.text = getString(R.string.bulletpointListDetails)

    var taskDetails = listField?.text?.toString()?: ""
    if(taskDetails.isEmpty()) taskDetails = list.getTitle()
    listTitleDetailsText.text =
        if(taskDetails.isNotEmpty()) taskDetails
        else getString(R.string.emptyDetailString)

    numTasksLabelText.text = getString(R.string.numBulletpointsLabel)

    numTasksDetailsText.text = list.getCurrentTasks().size.toString()

    dateCreatedDetailsText.text = getDateAndTimeString(list.getDateCreated())

    if(list.hasLabels()) {
        labelsLabelText.visibility = View.VISIBLE
        labelsDetailsText.visibility = View.VISIBLE
        labelsDetailsText.text = list.getLabelsString()
    }

    if(list.isArchived()) {
        archivedLabelText.visibility = View.VISIBLE
        archivedDetailsText.visibility = View.VISIBLE
    }

    view.findViewById<TextView>(R.id.closeOption).setOnClickListener {
        confirmDeleteNoteDialog.cancel()
    }

    confirmDeleteNoteDialog.show()
}

@SuppressLint("InflateParams")
fun Activity.openToDoListTaskDetailsDialog(task: Task, database: ListsDatabase,
                                           taskField: EditText? = null) {
    val view = layoutInflater.inflate(R.layout.dialog_view_task_details, null)
    val builder = AlertDialog.Builder(this)
    builder.setCancelable(false)
    builder.setView(view)
    val confirmDeleteNoteDialog = builder.create()
    confirmDeleteNoteDialog.setCancelable(true)

    val taskDetailsTitleText = view.findViewById<TextView>(R.id.taskDetailsTitleText)
    val taskLabelText = view.findViewById<TextView>(R.id.taskLabelText)
    val taskDetailsText = view.findViewById<TextView>(R.id.taskDetailsText)
    val typeDetailsText = view.findViewById<TextView>(R.id.typeDetailsText)
    val rewardedTaskLabel = view.findViewById<TextView>(R.id.rewardedTaskLabel)
    val rewardedTaskText = view.findViewById<TextView>(R.id.rewardedTaskText)
    val fromListDetailsText = view.findViewById<TextView>(R.id.fromListDetailsText)
    val linkedListLabelText = view.findViewById<TextView>(R.id.linkedListLabelText)
    val linkedListDetailsText = view.findViewById<TextView>(R.id.linkedListDetailsText)
    val rewardLabelText = view.findViewById<TextView>(R.id.rewardLabelText)
    val rewardDetailsText = view.findViewById<TextView>(R.id.rewardDetailsText)
    val noteLabelText = view.findViewById<TextView>(R.id.noteLabelText)
    val noteDetailsText = view.findViewById<TextView>(R.id.noteDetailsText)
    val dueDateLabelText = view.findViewById<TextView>(R.id.dueDateLabelText)
    val dueDateDetailsText = view.findViewById<TextView>(R.id.dueDateDetailsText)
    val dateCreatedLabelText = view.findViewById<TextView>(R.id.dateCreatedLabelText)
    val dateCreatedDetailsText = view.findViewById<TextView>(R.id.dateCreatedDetailsText)
    val dateCompletedLabelText = view.findViewById<TextView>(R.id.dateCompletedLabelText)
    val dateCompletedDetailsText = view.findViewById<TextView>(R.id.dateCompletedDetailsText)
    val websiteLinkLabelText = view.findViewById<TextView>(R.id.websiteLinkLabelText)
    val websiteLinkDetailsText = view.findViewById<TextView>(R.id.websiteLinkDetailsText)

    if(task.hasWebsiteLink()) {
        websiteLinkLabelText.visibility = View.VISIBLE
        websiteLinkDetailsText.visibility = View.VISIBLE
        websiteLinkDetailsText.text = task.getWebsiteLink().formatAsWebsiteLink(this)
    }

    if(task.isReward()) {
        taskDetailsTitleText.text = getString(R.string.rewardDetailsString)
        taskLabelText.text = getString(R.string.rewardLabel)
        dateCreatedLabelText.text = getString(R.string.dateRewardedLabel)
    }

    var taskDetails = taskField?.text?.toString()?: ""
    if(taskDetails.isEmpty()) taskDetails = task.getTask()
    taskDetailsText.text =
        if(taskDetails.isNotEmpty()) taskDetails
        else getString(R.string.emptyDetailString)

    typeDetailsText.text =
        if(task.isReward()) getString(R.string.toDoListRewardString)
        else getString(R.string.toDoListTaskString)

    if(task.isReward()) {
        rewardedTaskLabel.visibility = View.VISIBLE
        rewardedTaskText.visibility = View.VISIBLE
        rewardedTaskText.text = task.getRewardedTask()
    }

    fromListDetailsText.text = database.getList(task.getListId()).getTitle()

    if(task.isLinkedToList()) {
        val linkedListId = task.getLinkedListId()
        val linkedListExists = database.listExists(linkedListId)
        if(linkedListExists) {
            linkedListLabelText.visibility = View.VISIBLE
            linkedListDetailsText.visibility = View.VISIBLE
            linkedListDetailsText.text = database.getList(linkedListId).getTitle()
        }
    }

    if(task.hasReward()) {
        rewardLabelText.visibility = View.VISIBLE
        rewardDetailsText.visibility = View.VISIBLE
        rewardDetailsText.text = task.getReward()
    }

    if(task.hasNote()) {
        noteLabelText.visibility = View.VISIBLE
        noteDetailsText.visibility = View.VISIBLE
        noteDetailsText.text = task.getNote()
    }

    if(task.hasDate()) {
        val dueDate = task.getDate()
        if(dueDate != null) {
            dueDateLabelText.visibility = View.VISIBLE
            dueDateDetailsText.visibility = View.VISIBLE
            dueDateDetailsText.text = getDetailsDateTextString(task)
        }
    } else if(task.isRepeating()) {
        dueDateLabelText.visibility = View.VISIBLE
        dueDateDetailsText.visibility = View.VISIBLE
        dueDateLabelText.text = getString(R.string.repeatsLabel)
        dueDateDetailsText.text = getDetailsDateTextString(task)
    }

    dateCreatedDetailsText.text = getDateAndTimeString(task.getDateCreated())

    if(task.isCompleted()) {
        val dateCompleted = task.getDateCompleted()
        if(dateCompleted != null) {
            dateCompletedLabelText.visibility = View.VISIBLE
            dateCompletedDetailsText.visibility = View.VISIBLE
            dateCompletedDetailsText.text = getDateAndTimeString(dateCompleted)
        }
    }

    view.findViewById<TextView>(R.id.closeOption).setOnClickListener {
        confirmDeleteNoteDialog.cancel()
    }

    confirmDeleteNoteDialog.show()
}

@SuppressLint("InflateParams")
fun Activity.openProgressListTaskDetailsDialog(task: ProgressTask, database: ListsDatabase,
                                               taskField: EditText? = null) {
    val view = layoutInflater.inflate(R.layout.dialog_view_task_details, null)
    val builder = AlertDialog.Builder(this)
    builder.setCancelable(false)
    builder.setView(view)
    val confirmDeleteNoteDialog = builder.create()
    confirmDeleteNoteDialog.setCancelable(true)

    val taskDetailsTitleText = view.findViewById<TextView>(R.id.taskDetailsTitleText)
    val taskLabelText = view.findViewById<TextView>(R.id.taskLabelText)
    val taskDetailsText = view.findViewById<TextView>(R.id.taskDetailsText)
    val typeDetailsText = view.findViewById<TextView>(R.id.typeDetailsText)
    val rewardedTaskLabel = view.findViewById<TextView>(R.id.rewardedTaskLabel)
    val rewardedTaskText = view.findViewById<TextView>(R.id.rewardedTaskText)
    val fromListDetailsText = view.findViewById<TextView>(R.id.fromListDetailsText)
    val fromSectionLabel = view.findViewById<TextView>(R.id.fromSectionLabel)
    val fromSectionDetails = view.findViewById<TextView>(R.id.fromSectionDetails)
    val linkedListLabelText = view.findViewById<TextView>(R.id.linkedListLabelText)
    val linkedListDetailsText = view.findViewById<TextView>(R.id.linkedListDetailsText)
    val rewardLabelText = view.findViewById<TextView>(R.id.rewardLabelText)
    val rewardDetailsText = view.findViewById<TextView>(R.id.rewardDetailsText)
    val noteLabelText = view.findViewById<TextView>(R.id.noteLabelText)
    val noteDetailsText = view.findViewById<TextView>(R.id.noteDetailsText)
    val dueDateLabelText = view.findViewById<TextView>(R.id.dueDateLabelText)
    val dueDateDetailsText = view.findViewById<TextView>(R.id.dueDateDetailsText)
    val dateCreatedLabelText = view.findViewById<TextView>(R.id.dateCreatedLabelText)
    val dateCreatedDetailsText = view.findViewById<TextView>(R.id.dateCreatedDetailsText)
    val dateCompletedLabelText = view.findViewById<TextView>(R.id.dateCompletedLabelText)
    val dateCompletedDetailsText = view.findViewById<TextView>(R.id.dateCompletedDetailsText)
    val websiteLinkLabelText = view.findViewById<TextView>(R.id.websiteLinkLabelText)
    val websiteLinkDetailsText = view.findViewById<TextView>(R.id.websiteLinkDetailsText)

    if(task.hasWebsiteLink()) {
        websiteLinkLabelText.visibility = View.VISIBLE
        websiteLinkDetailsText.visibility = View.VISIBLE
        websiteLinkDetailsText.text = task.getWebsiteLink().formatAsWebsiteLink(this)
    }

    if(task.isReward()) {
        taskDetailsTitleText.text = getString(R.string.rewardDetailsString)
        taskLabelText.text = getString(R.string.rewardLabel)
        dateCreatedLabelText.text = getString(R.string.dateRewardedLabel)
    }

    var taskDetails = taskField?.text?.toString()?: ""
    if(taskDetails.isEmpty()) taskDetails = task.getTask()
    taskDetailsText.text =
        if(taskDetails.isNotEmpty()) taskDetails
        else getString(R.string.emptyDetailString)

    typeDetailsText.text =
        if(task.isReward()) getString(R.string.progressListRewardString)
        else getString(R.string.progressListTaskString)

    if(task.isReward()) {
        rewardedTaskLabel.visibility = View.VISIBLE
        rewardedTaskText.visibility = View.VISIBLE
        rewardedTaskText.text = task.getRewardedTask()
    }

    fromListDetailsText.text = database.getList(task.getListId()).getTitle()

    fromSectionLabel.visibility = View.VISIBLE
    fromSectionDetails.visibility = View.VISIBLE
    fromSectionDetails.text = task.getListSectionOfTask()

    if(task.isLinkedToList()) {
        val linkedListId = task.getLinkedListId()
        val linkedListExists = database.listExists(linkedListId)
        if(linkedListExists) {
            linkedListLabelText.visibility = View.VISIBLE
            linkedListDetailsText.visibility = View.VISIBLE
            linkedListDetailsText.text = database.getList(linkedListId).getTitle()
        }
    }

    if(task.hasReward()) {
        rewardLabelText.visibility = View.VISIBLE
        rewardDetailsText.visibility = View.VISIBLE
        rewardDetailsText.text = task.getReward()
    }

    if(task.hasNote()) {
        noteLabelText.visibility = View.VISIBLE
        noteDetailsText.visibility = View.VISIBLE
        noteDetailsText.text = task.getNote()
    }

    if(task.hasDate()) {
        val dueDate = task.getDate()
        if(dueDate != null) {
            dueDateLabelText.visibility = View.VISIBLE
            dueDateDetailsText.visibility = View.VISIBLE
            dueDateDetailsText.text = getDetailsDateTextString(task)
        }
    } else if(task.isRepeating()) {
        dueDateLabelText.visibility = View.VISIBLE
        dueDateDetailsText.visibility = View.VISIBLE
        dueDateLabelText.text = getString(R.string.repeatsLabel)
        dueDateDetailsText.text = getDetailsDateTextString(task)
    }

    dateCreatedDetailsText.text = getDateAndTimeString(task.getDateCreated())

    if(task.isCompleted()) {
        val dateCompleted = task.getDateCompleted()
        if(dateCompleted != null) {
            dateCompletedLabelText.visibility = View.VISIBLE
            dateCompletedDetailsText.visibility = View.VISIBLE
            dateCompletedDetailsText.text = getDateAndTimeString(dateCompleted)
        }
    }

    view.findViewById<TextView>(R.id.closeOption).setOnClickListener {
        confirmDeleteNoteDialog.cancel()
    }

    confirmDeleteNoteDialog.show()
}

@SuppressLint("InflateParams")
fun Activity.openRoutineStepDetailsDialog(task: Task, database: ListsDatabase,
                                          taskField: EditText? = null) {
    val view = layoutInflater.inflate(R.layout.dialog_view_task_details, null)
    val builder = AlertDialog.Builder(this)
    builder.setCancelable(false)
    builder.setView(view)
    val confirmDeleteNoteDialog = builder.create()
    confirmDeleteNoteDialog.setCancelable(true)

    val taskDetailsTitleText = view.findViewById<TextView>(R.id.taskDetailsTitleText)
    val taskLabelText = view.findViewById<TextView>(R.id.taskLabelText)
    val taskDetailsText = view.findViewById<TextView>(R.id.taskDetailsText)
    val typeDetailsText = view.findViewById<TextView>(R.id.typeDetailsText)
    val fromListDetailsText = view.findViewById<TextView>(R.id.fromListDetailsText)
    val linkedListLabelText = view.findViewById<TextView>(R.id.linkedListLabelText)
    val linkedListDetailsText = view.findViewById<TextView>(R.id.linkedListDetailsText)
    val noteLabelText = view.findViewById<TextView>(R.id.noteLabelText)
    val noteDetailsText = view.findViewById<TextView>(R.id.noteDetailsText)
    val dueDateLabelText = view.findViewById<TextView>(R.id.dueDateLabelText)
    val dueDateDetailsText = view.findViewById<TextView>(R.id.dueDateDetailsText)
    val dateCreatedDetailsText = view.findViewById<TextView>(R.id.dateCreatedDetailsText)
    val dateCompletedLabelText = view.findViewById<TextView>(R.id.dateCompletedLabelText)
    val dateCompletedDetailsText = view.findViewById<TextView>(R.id.dateCompletedDetailsText)
    val websiteLinkLabelText = view.findViewById<TextView>(R.id.websiteLinkLabelText)
    val websiteLinkDetailsText = view.findViewById<TextView>(R.id.websiteLinkDetailsText)

    if(task.hasWebsiteLink()) {
        websiteLinkLabelText.visibility = View.VISIBLE
        websiteLinkDetailsText.visibility = View.VISIBLE
        websiteLinkDetailsText.text = task.getWebsiteLink().formatAsWebsiteLink(this)
    }

    taskDetailsTitleText.text = getString(R.string.stepDetailsString)

    taskLabelText.text = getString(R.string.stepLabel)

    var taskDetails = taskField?.text?.toString()?: ""
    if(taskDetails.isEmpty()) taskDetails = task.getTask()
    taskDetailsText.text =
        if(taskDetails.isNotEmpty()) taskDetails
        else getString(R.string.emptyDetailString)

    typeDetailsText.text = getString(R.string.routineListTaskString)

    fromListDetailsText.text = database.getList(task.getListId()).getTitle()

    if(task.isLinkedToList()) {
        val linkedListId = task.getLinkedListId()
        val linkedListExists = database.listExists(linkedListId)
        if(linkedListExists) {
            linkedListLabelText.visibility = View.VISIBLE
            linkedListDetailsText.visibility = View.VISIBLE
            linkedListDetailsText.text = database.getList(linkedListId).getTitle()
        }
    }

    if(task.hasNote()) {
        noteLabelText.visibility = View.VISIBLE
        noteDetailsText.visibility = View.VISIBLE
        noteDetailsText.text = task.getNote()
    }

    if(task.hasTime()) {
        dueDateLabelText.visibility = View.VISIBLE
        dueDateDetailsText.visibility = View.VISIBLE
        dueDateLabelText.text = getString(R.string.setTimeLabel)
        dueDateDetailsText.text = getTimeText(task.getTimeHour(), task.getTimeMinute())
    }

    dateCreatedDetailsText.text = getDateAndTimeString(task.getDateCreated())

    val dateCompleted = task.getDateCompleted()
    if(dateCompleted != null) {
        dateCompletedLabelText.visibility = View.VISIBLE
        dateCompletedDetailsText.visibility = View.VISIBLE
        dateCompletedLabelText.text = getString(R.string.lastCompletedLabel)
        dateCompletedDetailsText.text = getDateAndTimeString(dateCompleted)
    }

    view.findViewById<TextView>(R.id.closeOption).setOnClickListener {
        confirmDeleteNoteDialog.cancel()
    }

    confirmDeleteNoteDialog.show()
}

@SuppressLint("InflateParams")
fun Activity.openBulletpointDetailsDialog(task: Task, database: ListsDatabase,
                                          taskField: EditText? = null) {
    val view = layoutInflater.inflate(R.layout.dialog_view_task_details, null)
    val builder = AlertDialog.Builder(this)
    builder.setCancelable(false)
    builder.setView(view)
    val confirmDeleteNoteDialog = builder.create()
    confirmDeleteNoteDialog.setCancelable(true)

    val taskDetailsTitleText = view.findViewById<TextView>(R.id.taskDetailsTitleText)
    val taskLabelText = view.findViewById<TextView>(R.id.taskLabelText)
    val taskDetailsText = view.findViewById<TextView>(R.id.taskDetailsText)
    val typeDetailsText = view.findViewById<TextView>(R.id.typeDetailsText)
    val fromListDetailsText = view.findViewById<TextView>(R.id.fromListDetailsText)
    val linkedListLabelText = view.findViewById<TextView>(R.id.linkedListLabelText)
    val linkedListDetailsText = view.findViewById<TextView>(R.id.linkedListDetailsText)
    val noteLabelText = view.findViewById<TextView>(R.id.noteLabelText)
    val noteDetailsText = view.findViewById<TextView>(R.id.noteDetailsText)
    val dueDateLabelText = view.findViewById<TextView>(R.id.dueDateLabelText)
    val dueDateDetailsText = view.findViewById<TextView>(R.id.dueDateDetailsText)
    val dateCreatedDetailsText = view.findViewById<TextView>(R.id.dateCreatedDetailsText)
    val websiteLinkLabelText = view.findViewById<TextView>(R.id.websiteLinkLabelText)
    val websiteLinkDetailsText = view.findViewById<TextView>(R.id.websiteLinkDetailsText)

    if(task.hasWebsiteLink()) {
        websiteLinkLabelText.visibility = View.VISIBLE
        websiteLinkDetailsText.visibility = View.VISIBLE
        websiteLinkDetailsText.text = task.getWebsiteLink().formatAsWebsiteLink(this)
    }

    taskDetailsTitleText.text = getString(R.string.itemDetailsString)

    taskLabelText.text = getString(R.string.itemLabel)

    var taskDetails = taskField?.text?.toString()?: ""
    if(taskDetails.isEmpty()) taskDetails = task.getTask()
    taskDetailsText.text =
        if(taskDetails.isNotEmpty()) taskDetails
        else getString(R.string.emptyDetailString)

    typeDetailsText.text = getString(R.string.bulletedListItemString)

    fromListDetailsText.text = database.getList(task.getListId()).getTitle()

    if(task.isLinkedToList()) {
        val linkedListId = task.getLinkedListId()
        val linkedListExists = database.listExists(linkedListId)
        if(linkedListExists) {
            linkedListLabelText.visibility = View.VISIBLE
            linkedListDetailsText.visibility = View.VISIBLE
            linkedListDetailsText.text = database.getList(linkedListId).getTitle()
        }
    }

    if(task.hasNote()) {
        noteLabelText.visibility = View.VISIBLE
        noteDetailsText.visibility = View.VISIBLE
        noteDetailsText.text = task.getNote()
    }

    if(task.hasDate()) {
        val dueDate = task.getDate()
        if(dueDate != null) {
            dueDateLabelText.visibility = View.VISIBLE
            dueDateDetailsText.visibility = View.VISIBLE
            dueDateDetailsText.text = getDetailsDateTextString(task)
        }
    } else if(task.isRepeating()) {
        dueDateLabelText.visibility = View.VISIBLE
        dueDateDetailsText.visibility = View.VISIBLE
        dueDateLabelText.text = getString(R.string.repeatsLabel)
        dueDateDetailsText.text = getDetailsDateTextString(task)
    }

    dateCreatedDetailsText.text = getDateAndTimeString(task.getDateCreated())

    view.findViewById<TextView>(R.id.closeOption).setOnClickListener {
        confirmDeleteNoteDialog.cancel()
    }

    confirmDeleteNoteDialog.show()
}