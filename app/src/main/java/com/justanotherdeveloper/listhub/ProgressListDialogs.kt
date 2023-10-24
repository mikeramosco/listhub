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
import androidx.core.view.iterator
import androidx.core.view.size
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_progress_list.*
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("InflateParams")
class ProgressListDialogs(private val activity: ProgressListActivity):
    ListDialogs(progressListRef, activity, activity.getDatabase()) {

    private fun getVisibleSectionSizes(): ArrayList<Int> {
        val taskContainerSizes = ArrayList<Int>()
        for(sectionView in activity.listSectionsContainer) {
            val taskContainer = sectionView
                .findViewById<LinearLayout>(R.id.taskContainer)
            if(taskContainer.visibility == View.VISIBLE)
                taskContainerSizes.add(taskContainer.size)
        }
        if(activity.inProgressTaskContainer.visibility == View.VISIBLE)
            taskContainerSizes.add(activity.inProgressTaskContainer.size)
        if(activity.completedTaskContainer.visibility == View.VISIBLE)
            taskContainerSizes.add(activity.completedTaskContainer.size)
        return taskContainerSizes
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
        dialogOptions.add(view.findViewById(R.id.archiveListOption))            // 8
        dialogOptions.add(view.findViewById(R.id.reorderSectionsOption))        // 9
        initDialogOptions(moreOptionsDialog, dialogOptions)

        if(activity.getList().getListSections().size > 1)
            dialogOptions[9].visibility = View.VISIBLE

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

        if(activity.getList().hasLabels()) {
            view.findViewById<TextView>(R.id.manageLabelsText).text =
                activity.getString(R.string.manageLabelsString)
        }

        val visibleSectionSizes = getVisibleSectionSizes()

        var sectionToReorder = ""
        val reorderText = view.findViewById<TextView>(R.id.reorderOptionText)
        val reorderableLists = activity.getManager().getReorderableLists()
        when {
            reorderableLists.size == 0 -> dialogOptions[4].visibility = View.GONE
            reorderableLists.size == 1 -> {
                sectionToReorder = reorderableLists[0]
                reorderText.text = activity.getString(
                    R.string.reorderSingleListString, sectionToReorder)
            }
            else -> reorderText.text = activity.getString(R.string.reorderTasksString)
        }

        var sectionsHaveNoTasks = false
        if(visibleSectionSizes.isEmpty())
            sectionsHaveNoTasks = true
        else for(size in visibleSectionSizes)
            if(size == 0) {
                sectionsHaveNoTasks = true
                break
            }
        if(sectionsHaveNoTasks)
            dialogOptions[5].visibility = View.GONE

        if(activity.completedTaskContainer.size == 0)
            dialogOptions[6].visibility = View.GONE

        fun clickOption(option: LinearLayout) {
            when(option) {
                dialogOptions[0] -> activity.getManager().toggleListStar()
                dialogOptions[1] -> showNameListDialog()
                dialogOptions[2] -> showManageLabelsDialog()
                dialogOptions[3] -> showSortByDialog()
                dialogOptions[4] -> initReorderProcess(
                    reorderableLists, sectionToReorder)
                dialogOptions[5] -> activity.getView().selectAll()
                dialogOptions[6] -> showConfirmDialog(
                    activity.getString(R.string.deleteCompletedTasksPrompt),
                    activity.getString(R.string.deleteString),
                    deleteCompletedTasks = true)
                dialogOptions[7] -> showConfirmDialog(
                    activity.getString(R.string.deleteListPrompt, activity.getList().getTitle()),
                    activity.getString(R.string.deleteString), deleteList = true)
                dialogOptions[8] -> showConfirmDialog(
                    archiveListPrompt, archiveConfirmString,
                    archiveList = !isArchived, unarchiveList = isArchived)
                dialogOptions[9] -> activity.getView().enterReorderSectionsState()
            }

            moreOptionsDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        moreOptionsDialog.show()
    }

    private fun initReorderProcess(reorderableLists: ArrayList<String>, sectionToReorder: String) {
        if(sectionToReorder.isNotEmpty()) activity.openReorderPage(sectionToReorder)
        else showSelectSectionDialog(reorderableLists = reorderableLists)
    }

    fun showConfirmDialog(confirmMessage: String, confirmString: String,
                          deleteNote: Boolean = false, removeDate: Boolean = false,
                          duplicateTasks: Boolean = false, deleteTasks: Boolean = false,
                          deleteCompletedTasks: Boolean = false, unlinkTask: Boolean = false,
                          completeTasks: Boolean = false, deleteList: Boolean = false,
                          section: String = "", deleteSection: Boolean = false,
                          moveTasks: Boolean = false, listId: Int = SENTINEL,
                          listType: String = "", removeReward: Boolean = false,
                          deleteTask: Boolean = false, task: ProgressTask? = null,
                          removeTime: Boolean = false, archiveList: Boolean = false,
                          unarchiveList: Boolean = false, unlinkWebsite: Boolean = false,
                          addTaskDialog: BottomSheetDialog? = null,
                          closeAddTaskDialog: Boolean = false, forDialogTask: Boolean = false) {
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
                    removeReward -> removeReward()
                    deleteNote -> deleteNote()
                    removeDate -> removeDate()
                    duplicateTasks -> activity.getManager().duplicateTasks(forDialogTask)
                    deleteTasks -> activity.getManager().deleteTasks()
                    deleteCompletedTasks -> activity.getManager().deleteCompletedTasks()
                    completeTasks -> activity.getManager().moveTasksToOtherSection()
                    deleteList -> deleteList()
                    deleteSection -> activity.getManager().deleteSection(section)
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

    fun showSelectSectionDialog(editingTask: Boolean = false, listSelectionText: TextView? = null,
                                task: ProgressTask? = null, taskView: View? = null,
                                reorderableLists: ArrayList<String>? = null,
                                moveSelectedTasks: Boolean = false) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_select_item, null)
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        builder.setView(view)
        val selectSectionDialog = builder.create()
        selectSectionDialog.setCancelable(true)

        val selectSectionText = view.findViewById<TextView>(R.id.selectItemTitleText)
        val sectionContainer = view.findViewById<LinearLayout>(R.id.itemContents)
        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val selectOption = view.findViewById<TextView>(R.id.selectOption)

        if(task != null && moveSelectedTasks) {
            selectOption.text = activity.getString(R.string.moveString)
            selectSectionText.text = activity.getString(R.string.moveToOtherSectionString)
        } else if(reorderableLists != null)
            selectSectionText.text = activity.getString(R.string.reorderSectionString)

        val selectedSections = HashMap<String, Boolean>()
        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())
        val currentSelectedSection =
            listSelectionText?.text?.toString() ?:
            task?.getListSectionOfTask() ?:
            ""

        var aSectionIsSelected = currentSelectedSection.isNotEmpty()
        if(!aSectionIsSelected)
            selectOption.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent))

        fun setSectionSelected() {
            aSectionIsSelected = true
            selectOption.setTextColor(Color.WHITE)
        }

        fun selectOption() {
            var selectedSection = ""
            for(sectionView in sectionContainer.iterator()) {
                val sectionText = sectionView.findViewById<TextView>(R.id.itemText)
                val section = sectionText.text.toString()
                val sectionSelected = selectedSections[section]?: false
                if(sectionSelected) {
                    selectedSection = section
                    break
                }
            }
            if(currentSelectedSection != selectedSection) {
                when {
                    listSelectionText != null -> {
                        listSelectionText.text = selectedSection
                        if (!editingTask) setTaskSectionData(selectedSection)
                    }
                    task != null && taskView != null -> {
                        activity.getView().editTask(
                            task, taskView,
                            selectedSection
                        )
                        activity.updateProgressList()
                    }
                    reorderableLists != null -> activity.openReorderPage(selectedSection)
                    moveSelectedTasks -> activity.getManager()
                        .moveTasksToOtherSection(selectedSection)
                }
            }
            selectSectionDialog.cancel()
        }

        fun uncheckOtherSections(section: String) {
            for(sectionView in sectionContainer.iterator()) {
                val sectionText = sectionView.findViewById<TextView>(R.id.itemText)
                val viewSection = sectionText.text.toString()
                if(viewSection != section) {
                    val checkbox = sectionView.findViewById<ImageView>(R.id.checkbox)
                    checkbox.setImageResource(R.drawable.ic_radio_button_unchecked_gray)
                    selectedSections[viewSection] = false
                }
            }
        }

        fun addSectionView(section: String) {
            val sectionView = activity.layoutInflater.inflate(R.layout.view_select_item_option, null)
            val checkbox = sectionView.findViewById<ImageView>(R.id.checkbox)
            val sectionText = sectionView.findViewById<TextView>(R.id.itemText)

            sectionText.text = section
            selectedSections[section] = if(section == currentSelectedSection) {
                checkbox.setColoredImageResource(
                    R.drawable.ic_radio_button_checked_custom, colorTheme)
                true
            } else false

            sectionView.setOnClickListener {
                setSectionSelected()
                val sectionSelected = selectedSections[section]?: false
                if(!sectionSelected) {
                    checkbox.setColoredImageResource(
                        R.drawable.ic_radio_button_checked_custom, colorTheme)
                    uncheckOtherSections(section)
                    selectedSections[section] = true
                } else selectOption()
            }

            sectionContainer.addView(sectionView)
        }

        if(reorderableLists != null)
            for(section in reorderableLists)
                addSectionView(section)
        else {
            for (section in activity.getList()
                .getListSectionTitles()) addSectionView(section)
            addSectionView(activity.getList().getInProgressTitle())
            addSectionView(activity.getList().getCompletedTitle())
        }

        val itemScrollView = view.findViewById<ScrollView>(R.id.itemScrollView)
        val dialogOptions = ArrayList<View>()
        for(sectionView in sectionContainer.iterator())
            dialogOptions.add(sectionView)
        initDialogScrollOptions(itemScrollView, sectionContainer, dialogOptions)


        cancelOption.setOnClickListener {
            selectSectionDialog.cancel()
        }

        selectOption.setOnClickListener {
            if(aSectionIsSelected)
                selectOption()
        }

        selectSectionDialog.show()
    }

    fun getTaskAsProgressTask(): ProgressTask? {
        val task = currentDialogTask?: return null
        return task as ProgressTask
    }

    private fun setTaskSectionData(section: String) {
        val progressTask = getTaskAsProgressTask()?: return
        progressTask.setListSectionOfTask(section)
        progressTask.setInProgress(section
                == activity.getList().getInProgressTitle())
        progressTask.setCompleted(section
                == activity.getList().getCompletedTitle())
    }

    fun checkIfLinkedListAndSectionExists() {
        checkIfLinkedListExists()

        val view = currentDialogView?: return
        val listSectionText = view.findViewById<TextView>(R.id.selectedItemText)
        if(!activity.getManager().sectionExists(listSectionText.text.toString()))
            listSectionText.text = activity.getList().getInProgressTitle()
    }

    fun showAddTaskDialog(task: ProgressTask? = null, taskView: View? = null, selectedSection: String = "") {
        if(activity.getManager().dialogAlreadyOpened()) return
        else activity.getManager().setDialogOpened()
        val addTaskDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_add_progress_task, null)
        addTaskDialog.setContentView(view)

        currentDialog = addTaskDialog
        currentDialogTask = task ?: ProgressTask(activity.getList().getListId())
        currentDialogView = view

        val editingTask = task != null

        initRemoveOptionIcons()

        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())

        val addToSectionSelectionLayout = view.findViewById<LinearLayout>(R.id.addToSectionSelectionLayout)
        val addToText = view.findViewById<TextView>(R.id.addToText)
        val listSectionText = view.findViewById<TextView>(R.id.selectedItemText)
        val addToListArrowIcon = view.findViewById<ImageView>(R.id.addToListArrowIcon)

        if(!editingTask) {
            listSectionText.text = if(selectedSection.isNotEmpty())
                selectedSection else activity.getList().getInProgressTitle()
            setTaskSectionData(listSectionText.text.toString())
        }

        addToText.setTextColor(colorTheme)
        listSectionText.setTextColor(colorTheme)
        addToListArrowIcon.setColoredImageResource(R.drawable.ic_keyboard_arrow_down_custom, colorTheme)

        addToSectionSelectionLayout.setOnClickListener {
            showSelectSectionDialog(editingTask, listSectionText)
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
        scrollViewDialogOptions.add(view.findViewById(R.id.addRewardOption))        // 4
        scrollViewDialogOptions.add(view.findViewById(R.id.copyOption))             // 5
        scrollViewDialogOptions.add(view.findViewById(R.id.deleteOption))           // 6
        scrollViewDialogOptions.add(view.findViewById(R.id.addButton))              // 7
        scrollViewDialogOptions.add(view.findViewById(R.id.addTimeOption))          // 8
        scrollViewDialogOptions.add(view.findViewById(R.id.linkToWebsiteOption))    // 9
        scrollViewDialogOptions.add(view.findViewById(R.id.moreOption))             // 10
        initDialogHorizontalScrollOptions(optionsScrollView,
            optionsContents, addTaskDialog, scrollViewDialogOptions)

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
                        currentDialogTask = ProgressTask(activity.getList().getListId())
                        setTaskSectionData(listSectionText.text.toString())
                        currentDialogTask?.setTask(taskItem)
                        activity.getManager().addTask(getTaskAsProgressTask())
                    }
                }
            } else {
                currentDialogTask?.setTask(taskString)
                activity.getManager().addTask(getTaskAsProgressTask())
            }

            taskField.setText("")
            currentDialogTask = ProgressTask(activity.getList().getListId())
            setTaskSectionData(listSectionText.text.toString())
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
                scrollViewDialogOptions[4] -> showAddRewardDialog()
                scrollViewDialogOptions[5] -> copyTaskToClipboard(
                    taskField.text.toString())
                scrollViewDialogOptions[6] -> {
                    addTaskDialog.cancel()
                    showConfirmDialog(activity.getString(R.string.deleteTaskPrompt),
                        activity.getString(R.string.deleteString),
                        deleteTask = true, task = task)
                }
                scrollViewDialogOptions[7] -> addButtonClicked()
                scrollViewDialogOptions[8] -> openTimeDialog(
                    activity.supportFragmentManager)
                scrollViewDialogOptions[9] -> showAddWebsiteLinkDialog()
                scrollViewDialogOptions[10] -> showSelectedMoreOptionsDialog(true)
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

        initTaskFieldListener(taskField, scrollViewDialogOptions[7], task,
            addButtonBackground, addButtonIcon, colorTheme, optionsScrollView)

        if(task != null) {
            taskField.hint = task.getTask()
            taskField.setText(task.getTask())
            listSectionText.text = task.getListSectionOfTask()
            addToText.text = activity.getString(R.string.sectionLabelString)
            if(task.hasDate()) addDateOnDialog(
                activity.getDateText(task.getDate()!!), moveOptionToLeft = true)
            if(task.isRepeating()) addDateOnDialog(
                task.getRepeatingDaysString(activity),
                isRepeating = true, moveOptionToLeft = true)
            if(task.hasTime())
                addTimeOnDialog(getTimeText(task.getTimeHour(),
                    task.getTimeMinute()), moveOptionToLeft = true)
            if(task.hasReward()) addRewardOnDialog(true)
            if(task.hasNote()) addNoteOnDialog(true)
            if(task.hasWebsiteLink()) addWebsiteLinkOnDialog(
                task.getWebsiteLink(), true)
            if(task.isLinkedToList()) addLinkOnDialog(
                task.getLinkedListId(),true)
        } else {
            scrollViewDialogOptions[6].visibility = View.GONE
            scrollViewDialogOptions[10].visibility = View.GONE
        }

        addTaskDialog.setOnCancelListener {
            val taskHasContent = currentDialogTask?.hasContent()?: false
            if(task != null && taskView != null) {
                clearDialogData()
                val taskString = taskField.text.toString()
                if(taskString.isNotEmpty()) task.setTask(taskString)
                activity.getView().editTask(task, taskView,
                    listSectionText.text.toString())
                activity.updateProgressList()
            } else if(taskHasContent || taskField.text.toString().isNotEmpty())
                showConfirmDialog(
                    activity.getString(R.string.cancelAddTaskPrompt),
                    activity.getString(R.string.closeString),
                    addTaskDialog = addTaskDialog, closeAddTaskDialog = true)
            else clearDialogData()
        }

        taskField.requestFocus()
        addTaskDialog.show()
    }

    private fun openDetailsDialog() {
        val task = getTaskAsProgressTask()?: return
        val taskField = currentDialogView?.findViewById<EditText>(R.id.taskField)
        activity.openProgressListTaskDetailsDialog(
            task, activity.getDatabase(), taskField = taskField)
    }

    fun showSelectedMoreOptionsDialog(forDialogTask: Boolean = false) {
        val moreOptionsDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_list_selected_more_options, null)
        moreOptionsDialog.setContentView(view)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.selectAllOption))          // 0
        dialogOptions.add(view.findViewById(R.id.addToCalendarOption))      // 1
        dialogOptions.add(view.findViewById(R.id.completeTasksOption))      // 2
        dialogOptions.add(view.findViewById(R.id.moveTasksOption))          // 3
        dialogOptions.add(view.findViewById(R.id.duplicateTasksOption))     // 4
        dialogOptions.add(view.findViewById(R.id.deleteTasksOption))        // 5
        dialogOptions.add(view.findViewById(R.id.moveToOtherSectionOption)) // 6
        dialogOptions.add(view.findViewById(R.id.reorderOption))            // 7
        initDialogOptions(moreOptionsDialog, dialogOptions)

        if(forDialogTask) {
            dialogOptions[0].visibility = View.GONE
            dialogOptions[1].visibility = View.GONE
            dialogOptions[2].visibility = View.GONE
            dialogOptions[5].visibility = View.GONE
            dialogOptions[6].visibility = View.GONE
        }

        val reorderText = view.findViewById<TextView>(R.id.reorderOptionText)
        val reorderableLists = activity.getManager().getReorderableLists()
        var sectionToReorder = activity.getManager().getSectionOfSelectedTasks()
        when {
            sectionToReorder.isNotEmpty() && reorderableLists
                .contains(sectionToReorder) -> reorderText.text =
                activity.getString(R.string.reorderSingleListString, sectionToReorder)
            forDialogTask || reorderableLists.size == 0 ->
                dialogOptions[7].visibility = View.GONE
            reorderableLists.size == 1 -> {
                sectionToReorder = reorderableLists[0]
                reorderText.text = activity.getString(
                    R.string.reorderSingleListString, sectionToReorder)
            }
            else -> reorderText.text = activity.getString(R.string.reorderTasksString)
        }

        var completeTasksPrompt = activity.getString(R.string.completeTasksPrompt)
        var duplicateTasksPrompt = activity.getString(R.string.duplicateTasksPrompt)
        var deleteTasksPrompt = activity.getString(R.string.deleteTasksPrompt)

        if(activity.getManager().getSelectedCount() == 1) {
            view.findViewById<TextView>(R.id.completeTasksText).text =
                activity.getString(R.string.completeTaskString)
            view.findViewById<TextView>(R.id.duplicateTasksText).text =
                activity.getString(R.string.duplicateTaskString)
            view.findViewById<TextView>(R.id.deleteTasksText).text =
                activity.getString(R.string.deleteTaskString)
            completeTasksPrompt = activity.getString(R.string.completeTaskPrompt)
            duplicateTasksPrompt = activity.getString(R.string.duplicateTaskPrompt)
            deleteTasksPrompt = activity.getString(R.string.deleteTaskPrompt)
        }

        fun clickOption(option: LinearLayout) {
            when(option) {
                dialogOptions[0] -> activity.getView().selectAll()
                dialogOptions[1] -> showChooseCalendarMethodDialog(
                    activity.supportFragmentManager)
                dialogOptions[2] -> showConfirmDialog(completeTasksPrompt,
                    activity.getString(R.string.moveString), completeTasks = true)
                dialogOptions[3] -> showSelectListDialog(
                    toMove = true, forDialogTask = forDialogTask)
                dialogOptions[4] -> showConfirmDialog(duplicateTasksPrompt,
                    activity.getString(R.string.duplicateString), duplicateTasks = true, forDialogTask = forDialogTask)
                dialogOptions[5] -> showConfirmDialog(deleteTasksPrompt,
                    activity.getString(R.string.deleteString), deleteTasks = true)
                dialogOptions[6] -> showSelectSectionDialog(moveSelectedTasks = true)
                dialogOptions[7] -> {
                    if(forDialogTask) currentDialog?.cancel()
                    initReorderProcess(reorderableLists, sectionToReorder)
                }
            }

            moreOptionsDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        moreOptionsDialog.show()
    }

    fun showEditSectionDialog(section: String) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_rename_section, null)
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        builder.setView(view)
        val editSectionDialog = builder.create()
        editSectionDialog.setCancelable(true)

        val deleteSectionOption = view.findViewById<TextView>(R.id.deleteSectionOption)
        val sectionTitleField = view.findViewById<EditText>(R.id.sectionTitleField)
        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val saveOption = view.findViewById<TextView>(R.id.saveOption)

        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())
        deleteSectionOption.setTextColor(activity.getColorTheme())
        val colorStateList = ColorStateList.valueOf(colorTheme)
        ViewCompat.setBackgroundTintList(sectionTitleField, colorStateList)

        if(section == activity.getList().getInProgressTitle()
            || section == activity.getList().getCompletedTitle())
            deleteSectionOption.visibility = View.GONE

        sectionTitleField.setText(section)

        val sections = activity.getList().getAllListSectionTitles()
        sectionTitleField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                val sectionTitle = sectionTitleField.text.toString()
                val saveOptionColor =
                    if(sectionTitle.isEmpty() || sections.contains(sectionTitle))
                        ContextCompat.getColor(activity, R.color.colorAccent) else Color.WHITE
                saveOption.setTextColor(saveOptionColor)
            }
        })

        var deleteClicked = false
        deleteSectionOption.setOnClickListener {
            deleteClicked = true
            showConfirmDialog(activity.getString(R.string.deleteSectionPrompt, section),
                activity.getString(R.string.deleteString),
                section = section, deleteSection = true)
            editSectionDialog.cancel()
        }

        var cancelClicked = false
        cancelOption.setOnClickListener {
            cancelClicked = true
            editSectionDialog.cancel()
        }

        saveOption.setOnClickListener {
            val sectionTitle = sectionTitleField.text.toString()
            if(sectionTitle.isNotEmpty() && !sections.contains(sectionTitle))
                editSectionDialog.cancel()
        }

        editSectionDialog.setOnCancelListener {
            if(!cancelClicked && !deleteClicked) {
                val sectionTitle = sectionTitleField.text.toString()
                if (sectionTitle.isNotEmpty() && !sections.contains(sectionTitle)) {
                    if (section != sectionTitle) activity.getManager()
                        .renameSection(section, sectionTitle)
                }
            }
        }

        sectionTitleField.setOnEditorActionListener { _, actionId, _ ->
            val donePressed = actionId == EditorInfo.IME_ACTION_DONE
            if(donePressed) editSectionDialog.cancel()
            donePressed
        }

        sectionTitleField.requestFocus()
        editSectionDialog.show()
    }

    fun showAddSectionDialog() {
        val addNewSectionDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_add_new_item, null)
        addNewSectionDialog.setContentView(view)

        val sections = activity.getList().getAllListSectionTitles()

        val newSectionField = view.findViewById<EditText>(R.id.newItemField)
        newSectionField.hint = activity.getString(R.string.addNewSectionString)

        val colorTheme = activity.getColorTheme(activity.getList().getColorThemeIndex())
        val colorStateList = ColorStateList.valueOf(colorTheme)
        ViewCompat.setBackgroundTintList(newSectionField, colorStateList)

        val addButton = view.findViewById<LinearLayout>(R.id.addButton)
        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(addButton)
        initDialogOptions(addNewSectionDialog, dialogOptions)

        val addButtonBackground = view.findViewById<ImageView>(R.id.addButtonBackground)
        val addButtonIcon = view.findViewById<ImageView>(R.id.addButtonIcon)

        fun disableAddButton() {
            addButton.visibility = View.GONE
            addButtonBackground.setImageResource(R.drawable.ic_circle_filled_gray)
            addButtonIcon.setImageResource(R.drawable.ic_add_gray)
        }

        fun enableAddButton() {
            addButton.visibility = View.VISIBLE
            addButtonBackground.setColoredImageResource(
                R.drawable.ic_circle_filled_custom, colorTheme)
            addButtonIcon.setImageResource(R.drawable.ic_add_black)
        }

        disableAddButton()
        newSectionField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                val newSection = newSectionField.text.toString()
                if(newSection.isEmpty() || sections.contains(newSection))
                    disableAddButton() else enableAddButton()
            }
        })

        var addButtonPressed = false

        fun addButtonClicked(){
            val newSection = newSectionField.text.toString()
            if(newSection.isEmpty()) addNewSectionDialog.cancel()
            if(newSection.isEmpty() || sections.contains(newSection)) return
            if(!addButtonPressed) {
                addButtonPressed = true
                activity.getManager().addNewSection(
                    newSectionField.text.toString())
                addNewSectionDialog.cancel()
            }
        }

        addButton.setOnClickListener {
            addButtonClicked()
        }

        newSectionField.setOnEditorActionListener { _, actionId, _ ->
            val donePressed = actionId == EditorInfo.IME_ACTION_DONE
            if(donePressed) addButtonClicked()
            donePressed
        }

        newSectionField.requestFocus()
        addNewSectionDialog.show()
    }
}