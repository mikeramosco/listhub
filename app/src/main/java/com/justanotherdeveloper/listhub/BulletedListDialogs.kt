package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.size
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_bulleted_list.*
import kotlin.collections.ArrayList

@SuppressLint("InflateParams")
class BulletedListDialogs(private val activity: BulletedListActivity):
    ListDialogs(bulletedListRef, activity, activity.getDatabase()) {

    fun showMoreOptionsDialog() {
        val moreOptionsDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_list_more_options, null)
        moreOptionsDialog.setContentView(view)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.starListOption))               // 0
        dialogOptions.add(view.findViewById(R.id.renameListOption))             // 1
        dialogOptions.add(view.findViewById(R.id.manageLabelsOption))           // 2
        dialogOptions.add(view.findViewById(R.id.sortByOption))                 // 3
        dialogOptions.add(view.findViewById(R.id.reorderOption))                // 4
        dialogOptions.add(view.findViewById(R.id.selectAllOption))              // 5
        dialogOptions.add(view.findViewById(R.id.deleteCompletedTasksOption))   // 6
        dialogOptions.add(view.findViewById(R.id.deleteListOption))             // 7
        dialogOptions.add(view.findViewById(R.id.archiveListOption))            // 8
        initDialogOptions(moreOptionsDialog, dialogOptions)

        val isArchived = activity.getList().isArchived()
        val title = activity.getList().getTitle()
        val archiveListPrompt = if(isArchived)
            activity.getString(R.string.unarchiveListPrompt, title)
        else activity.getString(R.string.archiveListPrompt, title)
        val archiveConfirmString = if(isArchived)
            activity.getString(R.string.unarchiveCapsString)
        else activity.getString(R.string.archiveCapsString)
        if(isArchived) view.findViewById<TextView>(R.id.archiveListText)
            .text = activity.getString(R.string.unarchiveListString)

        if(activity.getList().isStarred()) {
            view.findViewById<ImageView>(R.id.starIcon).setImageResource(R.drawable.ic_star_white)
            view.findViewById<TextView>(R.id.starListText).text =
                activity.getString(R.string.unStarListString)
        }

        if(activity.getList().hasLabels())
            view.findViewById<TextView>(R.id.manageLabelsText).text =
                activity.getString(R.string.manageLabelsString)

        view.findViewById<TextView>(R.id.reorderOptionText).text =
            activity.getString(R.string.reorderItemsString)

        if(activity.taskContainer.size < 2) dialogOptions[4].visibility = View.GONE

        if(activity.taskContainer.size == 0)
            dialogOptions[5].visibility = View.GONE

        dialogOptions[6].visibility = View.GONE

        fun clickOption(option: LinearLayout) {
            when(option) {
                dialogOptions[0] -> activity.getManager().toggleListStar()
                dialogOptions[1] -> showNameListDialog()
                dialogOptions[2] -> showManageLabelsDialog()
                dialogOptions[3] -> showSortByDialog()
                dialogOptions[4] -> activity.openReorderPage()
                dialogOptions[5] -> activity.getView().selectAll()
                dialogOptions[7] -> showConfirmDialog(
                    activity.getString(R.string.deleteListPrompt, activity.getList().getTitle()),
                    activity.getString(R.string.deleteString), deleteList = true)
                dialogOptions[8] -> showConfirmDialog(
                    archiveListPrompt, archiveConfirmString,
                    archiveList = !isArchived, unarchiveList = isArchived)
            }

            moreOptionsDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        moreOptionsDialog.show()
    }

    fun showSelectedMoreOptionsDialog(forDialogTask: Boolean = false) {
        val moreOptionsDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_list_selected_more_options, null)
        moreOptionsDialog.setContentView(view)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.selectAllOption))      // 0
        dialogOptions.add(view.findViewById(R.id.addToCalendarOption))  // 1
        dialogOptions.add(view.findViewById(R.id.completeTasksOption))  // 2
        dialogOptions.add(view.findViewById(R.id.moveTasksOption))      // 3
        dialogOptions.add(view.findViewById(R.id.duplicateTasksOption)) // 4
        dialogOptions.add(view.findViewById(R.id.deleteTasksOption))    // 5
        dialogOptions.add(view.findViewById(R.id.reorderOption))        // 6
        initDialogOptions(moreOptionsDialog, dialogOptions)

        if(forDialogTask) {
            dialogOptions[0].visibility = View.GONE
            dialogOptions[1].visibility = View.GONE
            dialogOptions[5].visibility = View.GONE
        }

        dialogOptions[2].visibility = View.GONE

        view.findViewById<LinearLayout>(R.id.moveToOtherSectionOption).visibility = View.GONE

        view.findViewById<TextView>(R.id.reorderOptionText).text =
            activity.getString(R.string.reorderItemsString)

        if(activity.taskContainer.size < 2) dialogOptions[6].visibility = View.GONE

        var duplicateTasksPrompt = activity.getString(R.string.duplicateItemsPrompt)
        var deleteTasksPrompt = activity.getString(R.string.deleteItemsPrompt)

        if(activity.getManager().getSelectedCount() == 1) {
            view.findViewById<TextView>(R.id.duplicateTasksText).text =
                activity.getString(R.string.duplicateItemString)
            view.findViewById<TextView>(R.id.deleteTasksText).text =
                activity.getString(R.string.deleteItemString)
            duplicateTasksPrompt = activity.getString(R.string.duplicateItemPrompt)
            deleteTasksPrompt = activity.getString(R.string.deleteItemPrompt)
        }

        fun clickOption(option: LinearLayout) {
            when(option) {
                dialogOptions[0] -> activity.getView().selectAll()
                dialogOptions[1] -> showChooseCalendarMethodDialog(
                    activity.supportFragmentManager)
                dialogOptions[3] -> showSelectListDialog(
                    toMove = true, forDialogTask = forDialogTask)
                dialogOptions[4] -> showConfirmDialog(duplicateTasksPrompt,
                    activity.getString(R.string.duplicateString), duplicateTasks = true, forDialogTask = forDialogTask)
                dialogOptions[5] -> showConfirmDialog(deleteTasksPrompt,
                    activity.getString(R.string.deleteString), deleteTasks = true)
                dialogOptions[6] -> activity.openReorderPage()
            }

            moreOptionsDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        moreOptionsDialog.show()
    }

    fun showAddTaskDialog(task: Task? = null, taskView: View? = null) {
        if(activity.getManager().dialogAlreadyOpened()) return
        else activity.getManager().setDialogOpened()
        val addTaskDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_add_to_do_list_task, null)
        addTaskDialog.setContentView(view)

        currentDialog = addTaskDialog
        currentDialogTask = task ?: Task(activity.getList().getListId())
        currentDialogView = view

        initRemoveOptionIcons()

        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())

        val checkbox = view.findViewById<ImageView>(R.id.checkbox)
        val addBulletpointSpacer = view.findViewById<View>(R.id.addBulletpointSpacer)
        checkbox.visibility = View.GONE
        addBulletpointSpacer.visibility = View.VISIBLE

        val optionsScrollView =
            view.findViewById<HorizontalScrollView>(R.id.optionsScrollView)
        val optionsContents =
            view.findViewById<LinearLayout>(R.id.optionsContents)

        val scrollViewDialogOptions = ArrayList<LinearLayout>()
        scrollViewDialogOptions.add(view.findViewById(R.id.addDateOption))          // 0
        scrollViewDialogOptions.add(view.findViewById(R.id.addNoteOption))          // 1
        scrollViewDialogOptions.add(view.findViewById(R.id.linkToListOption))       // 2
        scrollViewDialogOptions.add(view.findViewById(R.id.detailsOption))          // 3
        scrollViewDialogOptions.add(view.findViewById(R.id.copyOption))             // 4
        scrollViewDialogOptions.add(view.findViewById(R.id.deleteOption))           // 5
        scrollViewDialogOptions.add(view.findViewById(R.id.addButton))              // 6
        scrollViewDialogOptions.add(view.findViewById(R.id.addTimeOption))          // 7
        scrollViewDialogOptions.add(view.findViewById(R.id.linkToWebsiteOption))    // 8
        scrollViewDialogOptions.add(view.findViewById(R.id.moreOption))             // 9
        initDialogHorizontalScrollOptions(optionsScrollView,
            optionsContents, addTaskDialog, scrollViewDialogOptions)

        view.findViewById<LinearLayout>(R.id.addRewardOption).visibility = View.GONE

        val taskField = view.findViewById<EditText>(R.id.taskField)
        val colorStateList = ColorStateList.valueOf(colorTheme)
        ViewCompat.setBackgroundTintList(taskField, colorStateList)

        fun addTask() {
            removeTaskOptionsFromDialog()
            val taskString = taskField.text.toString()
            if(taskString.contains("\n")) {
                val taskContents = taskString.split("\n")
                for(taskItem in taskContents.asReversed()) {
                    if(taskItem.isNotEmpty()) {
                        currentDialogTask = Task(activity.getList().getListId())
                        currentDialogTask?.setTask(taskItem)
                        activity.getManager().addTask(currentDialogTask)
                    }
                }
            } else {
                currentDialogTask?.setTask(taskString)
                activity.getManager().addTask(currentDialogTask)
            }

            checkbox.setImageResource(R.drawable.ic_radio_button_unchecked_gray)
            taskField.setText("")
            currentDialogTask = Task(activity.getList().getListId())
        }

        fun addButtonClicked() {
            if(taskField.text.toString().isEmpty()) addTaskDialog.cancel()
            else { if(task == null) addTask() else addTaskDialog.cancel() }
        }

        fun clickOption(option: LinearLayout) {
            when(option) {
                scrollViewDialogOptions[0] -> openCalendarDialog(
                    activity.supportFragmentManager)
                scrollViewDialogOptions[1] -> openTaskNote(
                    taskField.text.toString())
                scrollViewDialogOptions[2] -> openOrSetLink(addTaskDialog)
                scrollViewDialogOptions[3] -> openDetailsDialog()
                scrollViewDialogOptions[4] -> copyTaskToClipboard(
                    taskField.text.toString())
                scrollViewDialogOptions[5] -> {
                    addTaskDialog.cancel()
                    showConfirmDialog(activity.getString(R.string.deleteTaskPrompt),
                        activity.getString(R.string.deleteString),
                        deleteTask = true, task = task)
                }
                scrollViewDialogOptions[6] -> addButtonClicked()
                scrollViewDialogOptions[7] -> openTimeDialog(
                    activity.supportFragmentManager)
                scrollViewDialogOptions[8] -> showAddWebsiteLinkDialog()
                scrollViewDialogOptions[9] -> showSelectedMoreOptionsDialog(true)
            }
        }

        taskField.setOnEditorActionListener { _, actionId, _ ->
            val donePressed = actionId == EditorInfo.IME_ACTION_DONE
            if(donePressed) addButtonClicked()
            donePressed
        }

        for(option in scrollViewDialogOptions)
            option.setOnClickListener { clickOption(option) }

        val addButtonBackground = view.findViewById<ImageView>(R.id.addButtonBackground)
        val addButtonIcon = view.findViewById<ImageView>(R.id.addButtonIcon)

        initTaskFieldListener(taskField, scrollViewDialogOptions[6], task,
            addButtonBackground, addButtonIcon, colorTheme, optionsScrollView)

        if(task != null) {
            taskField.hint = task.getTask()
            taskField.setText(task.getTask())
            if(task.isCompleted()) checkbox.setColoredImageResource(
                R.drawable.ic_check_circle_custom, colorTheme)
            if(task.hasDate()) addDateOnDialog(
                activity.getDateText(task.getDate()!!), moveOptionToLeft = true)
            if(task.isRepeating()) addDateOnDialog(
                task.getRepeatingDaysString(activity),
                isRepeating = true, moveOptionToLeft = true)
            if(task.hasTime())
                addTimeOnDialog(getTimeText(task.getTimeHour(),
                    task.getTimeMinute()), moveOptionToLeft = true)
            if(task.hasNote()) addNoteOnDialog(true)
            if(task.hasWebsiteLink()) addWebsiteLinkOnDialog(
                task.getWebsiteLink(), true)
            if(task.isLinkedToList()) addLinkOnDialog(
                task.getLinkedListId(),true)
        } else {
            taskField.hint = activity.getString(R.string.addAnItemString)
            scrollViewDialogOptions[5].visibility = View.GONE
            scrollViewDialogOptions[9].visibility = View.GONE
        }

        addTaskDialog.setOnCancelListener {
            val taskHasContent = currentDialogTask?.hasContent()?: false
            if(task != null && taskView != null) {
                clearDialogData()
                val taskString = taskField.text.toString()
                if(taskString.isNotEmpty()) task.setTask(taskString)
                activity.getView().editTask(task, taskView)
                activity.updateBulletedList()
            } else if(taskHasContent || taskField.text.toString().isNotEmpty())
                showConfirmDialog(
                    activity.getString(R.string.cancelAddItemPrompt),
                    activity.getString(R.string.closeString),
                    addTaskDialog = addTaskDialog, closeAddTaskDialog = true)
            else clearDialogData()
        }

        taskField.requestFocus()
        addTaskDialog.show()
    }

    private fun openDetailsDialog() {
        val task = currentDialogTask?: return
        val taskField = currentDialogView?.findViewById<EditText>(R.id.taskField)
        activity.openBulletpointDetailsDialog(task, activity.getDatabase(), taskField)
    }

    fun showConfirmDialog(confirmMessage: String, confirmString: String,
                          deleteNote: Boolean = false, removeDate: Boolean = false,
                          duplicateTasks: Boolean = false, deleteTasks: Boolean = false,
                          deleteList: Boolean = false, unlinkTask: Boolean = false,
                          moveTasks: Boolean = false, listId: Int = SENTINEL,
                          deleteTask: Boolean = false, task: Task? = null,
                          listType: String = "", removeTime: Boolean = false,
                          archiveList: Boolean = false, unarchiveList: Boolean = false,
                          addTaskDialog: BottomSheetDialog? = null,
                          closeAddTaskDialog: Boolean = false,
                          unlinkWebsite: Boolean = false, forDialogTask: Boolean = false) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_confirm_delete, null)
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        builder.setView(view)
        val confirmDeleteNoteDialog = builder.create()
        confirmDeleteNoteDialog.setCancelable(true)

        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val confirmOption = view.findViewById<TextView>(R.id.removeOption)

        val confirmDeleteMessage = view.findViewById<TextView>(R.id.confirmDeleteMessage)
        confirmDeleteMessage.text = confirmMessage
        confirmOption.text = confirmString

        var confirmClicked = false

        confirmOption.setOnClickListener {
            confirmClicked = true
            confirmDeleteNoteDialog.cancel()
        }

        cancelOption.setOnClickListener {
            confirmDeleteNoteDialog.cancel()
        }

        confirmDeleteNoteDialog.setOnCancelListener {
            confirmDeleteNoteDialog.dismiss()
            if(confirmClicked) {
                when {
                    deleteNote -> deleteNote()
                    removeDate -> removeDate()
                    duplicateTasks -> activity.getManager().duplicateTasks(forDialogTask)
                    deleteTasks -> activity.getManager().deleteTasks()
                    deleteList -> deleteList()
                    unlinkTask -> unlinkTask()
                    moveTasks -> moveTasks(listId, listType, forDialogTask)
                    deleteTask -> if(task != null)
                        activity.getManager().deleteTask(task)
                    removeTime -> removeTime()
                    archiveList -> archiveList()
                    unarchiveList -> unarchiveList()
                    unlinkWebsite -> unlinkWebsite()
                    closeAddTaskDialog -> clearDialogData()
                }
            } else {
                when {
                    closeAddTaskDialog -> addTaskDialog?.show()
                }
            }
        }

        confirmDeleteNoteDialog.show()
    }
}