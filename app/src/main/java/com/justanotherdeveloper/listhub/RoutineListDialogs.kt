package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.size
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_routine_list.*
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("InflateParams")
class RoutineListDialogs(private val activity: RoutineListActivity):
    ListDialogs(routineListRef, activity, activity.getDatabase()) {

    private var nameListDialog: View? = null
    private var listCalendar: Calendar? = null
    private var listRepeatingDays = ArrayList<Int>()

    fun nameListDialogShowing(): Boolean {
        return nameListDialog != null
    }

    private fun removeDateFromNameListDialog() {
        val nameListDialogParent = nameListDialog
            ?.findViewById<LinearLayout>(R.id.nameListDialogParent)
        if(nameListDialogParent != null) beginTransition(nameListDialogParent)
        val dateText = nameListDialog?.findViewById<TextView>(R.id.dateText)
        dateText?.visibility = View.GONE
        val dateIcon = nameListDialog?.findViewById<ImageView>(R.id.dateIcon)
        dateIcon?.setImageResource(R.drawable.ic_date_gray)
        val addToCalendarButton = nameListDialog
            ?.findViewById<TextView>(R.id.addToCalendarButton)
        addToCalendarButton?.text = activity.getString(R.string.addToCalendarString)
    }

    private fun addDateOnNameListDialog(calendarText: String, isRepeating: Boolean = false) {
        val dateText = nameListDialog?.findViewById<TextView>(R.id.dateText)
        val displayedText = dateText?.text.toString()
        if(displayedText != calendarText || dateText?.visibility == View.GONE) {
            dateText?.visibility = View.GONE
            val nameListDialogParent = nameListDialog
                ?.findViewById<LinearLayout>(R.id.nameListDialogParent)
            if (nameListDialogParent != null) beginTransition(nameListDialogParent)
            dateText?.visibility = View.VISIBLE
            dateText?.text = calendarText
            val dateIcon = nameListDialog?.findViewById<ImageView>(R.id.dateIcon)
            val dateIconCode = if (isRepeating)
                R.drawable.ic_autorenew_gray else R.drawable.ic_date_gray
            dateIcon?.setImageResource(dateIconCode)
            val addToCalendarButton = nameListDialog
                ?.findViewById<TextView>(R.id.addToCalendarButton)
            addToCalendarButton?.text = activity.getString(R.string.editCalendarString)
        }
    }

    fun setRepeatingDays(daysSelected: ArrayList<Boolean>) {
        listRepeatingDays.clear()
        if(daysSelected[0]) listRepeatingDays.add(Calendar.SUNDAY)
        if(daysSelected[1]) listRepeatingDays.add(Calendar.MONDAY)
        if(daysSelected[2]) listRepeatingDays.add(Calendar.TUESDAY)
        if(daysSelected[3]) listRepeatingDays.add(Calendar.WEDNESDAY)
        if(daysSelected[4]) listRepeatingDays.add(Calendar.THURSDAY)
        if(daysSelected[5]) listRepeatingDays.add(Calendar.FRIDAY)
        if(daysSelected[6]) listRepeatingDays.add(Calendar.SATURDAY)
        activity.getManager().setListRepeatingDays(listRepeatingDays)
        if(nameListDialogShowing()) addDateOnNameListDialog(
            activity.getRepeatingDaysString(listRepeatingDays), true)
    }

    fun setListDialogDate(date: Calendar) {
        listCalendar = date
        addDateOnNameListDialog(activity.getDateText(date))
    }

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
        dialogOptions.add(view.findViewById(R.id.addToCalendarOption))          // 8
        dialogOptions.add(view.findViewById(R.id.resetRoutineOption))           // 9
        dialogOptions.add(view.findViewById(R.id.archiveListOption))            // 10
        initDialogOptions(moreOptionsDialog, dialogOptions)

        dialogOptions[8].visibility = View.VISIBLE

        val list = activity.getList()

        val isArchived = list.isArchived()
        val title = list.getTitle()
        val archiveListPrompt = if(isArchived)
            activity.getString(R.string.unarchiveListPrompt, title)
        else activity.getString(R.string.archiveListPrompt, title)
        val archiveConfirmString = if(isArchived)
            activity.getString(R.string.unarchiveCapsString)
        else activity.getString(R.string.archiveCapsString)
        if(isArchived) view.findViewById<TextView>(R.id.archiveListText)
            .text = activity.getString(R.string.unarchiveListString)

        if(list.getCompletedTasks().size > 0)
            dialogOptions[9].visibility = View.VISIBLE

        if(list.hasDate() || list.isRepeating())
            view.findViewById<TextView>(R.id.addToCalendarText).text =
                activity.getString(R.string.editRoutineCalendarString)

        if(list.isStarred()) {
            view.findViewById<ImageView>(R.id.starIcon).setImageResource(R.drawable.ic_star_white)
            view.findViewById<TextView>(R.id.starListText).text =
                activity.getString(R.string.unStarListString)
        }

        if(list.hasLabels())
            view.findViewById<TextView>(R.id.manageLabelsText).text =
                activity.getString(R.string.manageLabelsString)

        view.findViewById<TextView>(R.id.reorderOptionText).text =
            activity.getString(R.string.reorderRoutineString)

        if(activity.taskContainer.size + activity.completedTaskContainer.size < 2)
            dialogOptions[4].visibility = View.GONE

        val visibleCompletedSize =
            if(activity.completedTaskContainer.visibility == View.VISIBLE)
                activity.completedTaskContainer.size else 0
        if(activity.taskContainer.size == 0 && visibleCompletedSize == 0)
            dialogOptions[5].visibility = View.GONE

        dialogOptions[6].visibility = View.GONE

        fun clickOption(option: LinearLayout) {
            when(option) {
                dialogOptions[0] -> activity.getManager().toggleListStar()
                dialogOptions[1] -> showNameRoutineListDialog()
                dialogOptions[2] -> showManageLabelsDialog(nameListDialog)
                dialogOptions[3] -> showSortByDialog()
                dialogOptions[4] -> activity.openReorderPage()
                dialogOptions[5] -> activity.getView().selectAll()
                dialogOptions[7] -> showConfirmDialog(
                    activity.getString(R.string.deleteListPrompt, activity.getList().getTitle()),
                    activity.getString(R.string.deleteString), deleteList = true)
                dialogOptions[8] -> showChooseCalendarMethodForEditListDialog()
                dialogOptions[9] -> activity.getManager().resetList()
                dialogOptions[10] -> showConfirmDialog(
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
            dialogOptions[2].visibility = View.GONE
            dialogOptions[5].visibility = View.GONE
        }

        dialogOptions[1].visibility = View.GONE
        view.findViewById<LinearLayout>(R.id.moveToOtherSectionOption).visibility = View.GONE

        view.findViewById<TextView>(R.id.reorderOptionText).text =
            activity.getString(R.string.reorderRoutineString)

        if(activity.taskContainer.size + activity.completedTaskContainer.size < 2)
            dialogOptions[6].visibility = View.GONE

        val completeTasksText = view.findViewById<TextView>(R.id.completeTasksText)
        val duplicateTasksText = view.findViewById<TextView>(R.id.duplicateTasksText)
        val deleteTasksText = view.findViewById<TextView>(R.id.deleteTasksText)

        completeTasksText.text = activity.getString(R.string.completeStepsString)
        duplicateTasksText.text = activity.getString(R.string.duplicateStepsString)
        deleteTasksText.text = activity.getString(R.string.deleteStepsString)

        var completeTasksPrompt = activity.getString(R.string.completeStepsPrompt)
        var duplicateTasksPrompt = activity.getString(R.string.duplicateStepsPrompt)
        var deleteTasksPrompt = activity.getString(R.string.deleteStepsPrompt)

        if(activity.getManager().getSelectedCount() == 1) {
            completeTasksText.text = activity.getString(R.string.completeStepString)
            duplicateTasksText.text = activity.getString(R.string.duplicateStepString)
            deleteTasksText.text = activity.getString(R.string.deleteStepString)

            completeTasksPrompt = activity.getString(R.string.completeStepPrompt)
            duplicateTasksPrompt = activity.getString(R.string.duplicateStepPrompt)
            deleteTasksPrompt = activity.getString(R.string.deleteStepPrompt)
        }

        fun clickOption(option: LinearLayout) {
            when(option) {
                dialogOptions[0] -> activity.getView().selectAll()
                dialogOptions[2] -> showConfirmDialog(completeTasksPrompt,
                    activity.getString(R.string.moveString), completeTasks = true)
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

    private fun showChooseCalendarMethodForEditListDialog() {
        val chooseCalendarMethodDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_choose_calendar_method, null)
        chooseCalendarMethodDialog.setContentView(view)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.addDateOption))
        dialogOptions.add(view.findViewById(R.id.repeatOption))
        dialogOptions.add(view.findViewById(R.id.removeDateOption))
        initDialogOptions(chooseCalendarMethodDialog, dialogOptions)

        dialogOptions[2].visibility = View.VISIBLE

        fun clickOption(option: LinearLayout) {
            when(option) {
                dialogOptions[0] -> showDatePickerDialog(activity.getList().getDate(),
                    activity.getList(), activity.supportFragmentManager)
                dialogOptions[1] -> showChooseRepeatingDaysDialog()
                dialogOptions[2] -> {
                    activity.getManager().removeListDate()
                    removeDateFromNameListDialog()
                }
            }

            chooseCalendarMethodDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        chooseCalendarMethodDialog.show()
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
        checkbox.setOnClickListener {
            val taskIsCompleted = currentDialogTask?.isCompleted()
            if(taskIsCompleted != null) {
                if(taskIsCompleted) {
                    checkbox.setImageResource(R.drawable.ic_radio_button_unchecked_gray)
                    currentDialogTask?.setCompleted(false)
                } else {
                    checkbox.setColoredImageResource(R.drawable.ic_check_circle_custom, colorTheme)
                    currentDialogTask?.setCompleted(true)
                }
            }
        }

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

        scrollViewDialogOptions[0].visibility = View.GONE
        view.findViewById<LinearLayout>(R.id.addRewardOption).visibility = View.GONE

        val taskField = view.findViewById<EditText>(R.id.taskField)
        val colorStateList = ColorStateList.valueOf(colorTheme)
        ViewCompat.setBackgroundTintList(taskField, colorStateList)

        fun addTask() {
            removeTaskOptionsFromDialog()
            val taskString = taskField.text.toString()
            if(taskString.contains("\n")) {
                val taskCompleted = currentDialogTask?.isCompleted()?: false
                val taskContents = taskString.split("\n")
                for(taskItem in taskContents) {
                    if(taskItem.isNotEmpty()) {
                        currentDialogTask = Task(activity.getList().getListId())
                        currentDialogTask?.setCompleted(taskCompleted)
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
                scrollViewDialogOptions[1] -> openTaskNote(
                    taskField.text.toString())
                scrollViewDialogOptions[2] -> openOrSetLink(addTaskDialog)
                scrollViewDialogOptions[3] -> openDetailsDialog()
                scrollViewDialogOptions[4] -> copyTaskToClipboard(
                    taskField.text.toString())
                scrollViewDialogOptions[5] -> {
                    addTaskDialog.cancel()
                    showConfirmDialog(activity.getString(R.string.deleteStepPrompt),
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
            if(task.hasTime())
                addTimeOnDialog(getTimeText(task.getTimeHour(),
                    task.getTimeMinute()), moveOptionToLeft = true)
            if(task.hasNote()) addNoteOnDialog(true)
            if(task.hasWebsiteLink()) addWebsiteLinkOnDialog(
                task.getWebsiteLink(), true)
            if(task.isLinkedToList()) addLinkOnDialog(
                task.getLinkedListId(),true)
        } else {
            taskField.hint = activity.getString(R.string.addAStepString)
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
                activity.updateRoutineList()
            } else if(taskHasContent || taskField.text.toString().isNotEmpty())
                showConfirmDialog(
                    activity.getString(R.string.cancelAddStepPrompt),
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
        activity.openRoutineStepDetailsDialog(task, activity.getDatabase(), taskField)
    }

    fun showConfirmDialog(confirmMessage: String, confirmString: String,
                          deleteNote: Boolean = false, unlinkTask: Boolean = false,
                          duplicateTasks: Boolean = false, deleteTasks: Boolean = false,
                          completeTasks: Boolean = false, deleteList: Boolean = false,
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
                    duplicateTasks -> activity.getManager().duplicateTasks(forDialogTask)
                    deleteTasks -> activity.getManager().deleteTasks()
                    completeTasks -> activity.getManager().completeTasks()
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

    fun showNameRoutineListDialog(listExists: Boolean = true) {
        val currentColorThemeIndex = activity.getList().getColorThemeIndex()

        val view = activity.layoutInflater.inflate(R.layout.dialog_name_list, null)
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        builder.setView(view)
        val nameRoutineListDialog = builder.create()
        nameRoutineListDialog.setCancelable(true)

        nameListDialog = view

        val createListText = view.findViewById<TextView>(R.id.createListText)
        val listTitleField = view.findViewById<EditText>(R.id.listTitleField)
        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val saveOption = view.findViewById<TextView>(R.id.saveOption)

        val manageLabelsButton = view.findViewById<TextView>(R.id.manageLabelsButton)
        val labelText = view.findViewById<TextView>(R.id.labelText)

        manageLabelsButton.setOnClickListener {
            showManageLabelsDialog(nameListDialog)
        }

        val viewNoteButton = view.findViewById<TextView>(R.id.viewNoteButton)

        viewNoteButton.setOnClickListener {
            showListNoteDialog(view, listTitleField.text.toString())
        }

        val addToCalendarLayout = view.findViewById<LinearLayout>(R.id.addToCalendarLayout)
        val addToCalendarButton = view.findViewById<TextView>(R.id.addToCalendarButton)

        addToCalendarButton.setOnClickListener {
            showChooseCalendarMethodForEditListDialog()
        }

        listTitleField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                val listTitle = listTitleField.text.toString()
                val saveOptionColor = if(listTitle.isEmpty())
                    ContextCompat.getColor(activity, R.color.colorAccent) else Color.WHITE
                saveOption.setTextColor(saveOptionColor)
            }
        })

        var colorThemeIndex = 0

        val colorOptions = ArrayList<LinearLayout>()
        colorOptions.add(view.findViewById(R.id.colorOption1))
        colorOptions.add(view.findViewById(R.id.colorOption2))
        colorOptions.add(view.findViewById(R.id.colorOption3))
        colorOptions.add(view.findViewById(R.id.colorOption4))
        colorOptions.add(view.findViewById(R.id.colorOption5))
        colorOptions.add(view.findViewById(R.id.colorOption6))

        val optionSetIcons = ArrayList<ImageView>()
        optionSetIcons.add(view.findViewById(R.id.option1SetIcon))
        optionSetIcons.add(view.findViewById(R.id.option2SetIcon))
        optionSetIcons.add(view.findViewById(R.id.option3SetIcon))
        optionSetIcons.add(view.findViewById(R.id.option4SetIcon))
        optionSetIcons.add(view.findViewById(R.id.option5SetIcon))
        optionSetIcons.add(view.findViewById(R.id.option6SetIcon))

        fun changeTheme(color: Int, index: Int) {
            // Change EditText Underline
            val colorStateList = ColorStateList.valueOf(color)
            ViewCompat.setBackgroundTintList(listTitleField, colorStateList)
            viewNoteButton.setTextColor(color)
            manageLabelsButton.setTextColor(color)
            addToCalendarButton.setTextColor(color)

            // Change Activity View Theme
            activity.getView().changeTheme(index)
        }

        fun setOption(clickedOption: Int, colorCode: Int) {
            if(colorThemeIndex == clickedOption) return
            optionSetIcons[colorThemeIndex].visibility = View.GONE
            optionSetIcons[clickedOption].visibility = View.VISIBLE
            colorThemeIndex = clickedOption

            val color = ContextCompat.getColor(activity, colorCode)
            changeTheme(color, clickedOption)
        }

        fun clickOption(option: LinearLayout) {
            when(option) {
                colorOptions[0] -> setOption(0, R.color.colorTheme1)
                colorOptions[1] -> setOption(1, R.color.colorTheme2)
                colorOptions[2] -> setOption(2, R.color.colorTheme3)
                colorOptions[3] -> setOption(3, R.color.colorTheme4)
                colorOptions[4] -> setOption(4, R.color.colorTheme5)
                colorOptions[5] -> setOption(5, R.color.colorTheme6)
            }
        }

        val list = activity.getList()

        if(listExists) {
            val untitledListTitle = activity.getString(R.string.untitledRoutineListString)
            var listTitle = activity.getList().getTitle()
            if(untitledListTitle == listTitle) listTitle = ""
            listTitleField.setText(listTitle)
            createListText.text = activity.getString(R.string.renameRoutineListString)
            saveOption.text = activity.getString(R.string.saveString)
            addToCalendarLayout.visibility = View.VISIBLE
            if(list.hasDate()) {
                val date = activity.getList().getDate()
                if(date != null) {
                    val dateString = activity.getDateText(date)
                    addDateOnNameListDialog(dateString)
                }
            } else if(list.isRepeating()) addDateOnNameListDialog(list
                .getRepeatingDaysString(activity), true)
            if(currentColorThemeIndex != 0)
                clickOption(colorOptions[currentColorThemeIndex])
            if(activity.getList().hasNote()) updateNameListDialogNoteState(view)
            if(list.hasLabels()) {
                labelText.visibility = View.VISIBLE
                labelText.text = list.getLabelsString()
                manageLabelsButton.text = activity.getString(R.string.manageLabelsString)
            }

            val detailsOption = view.findViewById<LinearLayout>(R.id.detailsOption)
            detailsOption.visibility = View.VISIBLE
            detailsOption.setOnClickListener {
                activity.openRoutineListDetailsDialog(activity.getList(), listTitleField)
            }
        } else {
            createListText.text = activity.getString(R.string.createRoutineListString)
            saveOption.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent))
            view.findViewById<LinearLayout>(R.id.labelsLayout).visibility = View.GONE
            view.findViewById<LinearLayout>(R.id.noteLayout).visibility = View.GONE
        }

        for(option in colorOptions) {
            initButtonAnimationListener(option)
            option.setOnClickListener { clickOption(option) }
        }

        fun saveOption() {
            val listTitle = listTitleField.text.toString()
            if(listTitle.isNotEmpty()) {
                activity.listTitleText.text = listTitle
                activity.getList().setTitle(listTitle)
                if (!listExists) activity.getDatabase()
                    .saveRoutineList(activity.getList())
                else activity.updateRoutineList()
            }
        }

        var saveClicked = false

        saveOption.setOnClickListener {
            val listTitle = listTitleField.text.toString()
            if(listTitle.isNotEmpty()) {
                saveClicked = true
                nameRoutineListDialog.cancel()
            }
        }

        listTitleField.setOnEditorActionListener { _, actionId, _ ->
            val donePressed = actionId == EditorInfo.IME_ACTION_DONE
            if(donePressed) saveOption.performClick()
            donePressed
        }

        var cancelClicked = false

        fun cancelDialog() {
            if(!listExists) activity.finish()
            else {
                if(currentColorThemeIndex != colorThemeIndex)
                    activity.getView().changeTheme(currentColorThemeIndex)
            }
        }

        cancelOption.setOnClickListener {
            cancelClicked = true
            cancelDialog()
            nameRoutineListDialog.cancel()
        }

        nameRoutineListDialog.setOnCancelListener {
            nameRoutineListDialog.dismiss()
            if(!listExists) {
                if(saveClicked) saveOption()
                else activity.finish()
            }
            else if(!cancelClicked) saveOption()
            nameListDialog = null
        }

        listTitleField.requestFocus()
        nameRoutineListDialog.show()
        showKeyboard(dialog = nameRoutineListDialog)
    }

}