package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseBooleanArray
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.util.set
import androidx.core.view.ViewCompat
import androidx.core.view.get
import androidx.core.view.iterator
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_home.*

@SuppressLint("InflateParams")
class HomeDialogs(private val activity: HomeActivity) {

    private var selectedListId = SENTINEL

    fun showAddListOrItemDialog(hideAddToListOption: Boolean = false) {
        activity.getListFragment().getListsView().setToDefaultState()
        val addListOrItemDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_add_list_or_item, null)
        addListOrItemDialog.setContentView(view)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.addToListOption))
        dialogOptions.add(view.findViewById(R.id.createToDoListOption))
        dialogOptions.add(view.findViewById(R.id.createProgressListOption))
        dialogOptions.add(view.findViewById(R.id.createRoutineListOption))
        dialogOptions.add(view.findViewById(R.id.createBulletedListOption))
        initDialogOptions(addListOrItemDialog, dialogOptions)

        val noListsExist = activity.getDatabase().getListIds().isEmpty()
        if(noListsExist || hideAddToListOption) dialogOptions[0].visibility = View.GONE

        val nLists = activity.getDatabase().getListIds().size
        if(nLists == 0) dialogOptions[0].visibility = View.GONE

        fun clickOption(option: LinearLayout) {
            when(option) {
                dialogOptions[0] -> showAddItemDialog()
                dialogOptions[1] -> activity.openNewToDoList()
                dialogOptions[2] -> activity.openNewProgressList()
                dialogOptions[3] -> activity.openNewRoutineList()
                dialogOptions[4] -> activity.openNewBulletedList()
            }

            addListOrItemDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        addListOrItemDialog.show()
    }

    @Suppress("DEPRECATION")
    private fun showAddItemDialog() {
        val addTaskDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_add_progress_task, null)
        addTaskDialog.setContentView(view)

        selectedListId = SENTINEL

        val colorTheme = activity.getColorTheme()

        view.findViewById<HorizontalScrollView>(R.id.optionsScrollView).visibility = View.GONE

        val addToListSelectionLayout = view.findViewById<LinearLayout>(R.id.addToSectionSelectionLayout)
        val selectedListText = view.findViewById<TextView>(R.id.selectedItemText)
        val addToListArrowIcon = view.findViewById<ImageView>(R.id.addToListArrowIcon)

        selectedListText.text = activity.getString(R.string.selectAListString)

        addToListArrowIcon.setColoredImageResource(R.drawable.ic_keyboard_arrow_down_custom, colorTheme)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.addButton))
        initDialogOptions(addTaskDialog, dialogOptions)

        val taskField = view.findViewById<EditText>(R.id.taskField)
        val colorStateList = ColorStateList.valueOf(colorTheme)
        ViewCompat.setBackgroundTintList(taskField, colorStateList)

        taskField.hint = activity.getString(R.string.addAnItemString)

        addToListSelectionLayout.setOnClickListener {
            showSelectListDialog(selectedListText, taskField)
        }

        var addButtonClicked = false

        fun addButtonClicked() {
            val taskString = taskField.text.toString()
            if(taskString.isEmpty()) addTaskDialog.cancel()
            if(taskString.isEmpty() || selectedListId == SENTINEL) return
            if(addButtonClicked) return
            addButtonClicked = true
            val task = activity.getDatabase().quickAddTask(taskString, selectedListId)
            addTaskDialog.cancel()
            val handler = Handler()
            handler.postDelayed({
                activity.displayToast(
                    activity.getString(R.string.newItemAddedMessage,
                    taskString.limitChar(MAX_TEXT_DISPLAY_LENGTH),
                    activity.getDatabase().getList(selectedListId)
                        .getTitle().limitChar(MAX_TEXT_DISPLAY_LENGTH)))
            }, DELAY_UNTIL_DIALOG_CANCELLED)

            val homeFragment = activity.getHomeFragment()
            if(homeFragment.getManager().getCurrentSectionIndex() == recentlyAddedIndex) {
                beginTransition(homeFragment.homeFragmentParent)
                homeFragment.getHomeView().displayTask(task, isNew = true)
            }
        }

        fun clickOption(option: LinearLayout) {
            when(option) {
                dialogOptions[0] -> addButtonClicked()
            }
        }

        taskField.setOnEditorActionListener { _, actionId, _ ->
            val donePressed = actionId == EditorInfo.IME_ACTION_DONE
            if(donePressed) addButtonClicked()
            donePressed
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        val addButtonBackground = view.findViewById<ImageView>(R.id.addButtonBackground)
        val addButtonIcon = view.findViewById<ImageView>(R.id.addButtonIcon)

        fun disableAddButton() {
            dialogOptions[0].visibility = View.GONE
            addButtonBackground.setImageResource(R.drawable.ic_circle_filled_gray)
            addButtonIcon.setImageResource(R.drawable.ic_add_gray)
        }

        fun enableAddButton() {
            dialogOptions[0].visibility = View.VISIBLE
            addButtonBackground.setColoredImageResource(
                R.drawable.ic_circle_filled_custom, colorTheme)
            addButtonIcon.setImageResource(R.drawable.ic_add_black)
        }

        disableAddButton()
        taskField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                val taskString = taskField.text.toString()
                if(taskString.isEmpty() || selectedListId == SENTINEL)
                    disableAddButton() else enableAddButton()
            }
        })

        addTaskDialog.setOnCancelListener {
            if(!addButtonClicked) {
                val taskString = taskField.text.toString()
                if (taskString.isNotEmpty() || selectedListId != SENTINEL)
                    showConfirmDialog(
                        activity.getString(R.string.cancelAddItemPrompt),
                        activity.getString(R.string.closeString),
                        addTaskDialog = addTaskDialog, closeAddTaskDialog = true
                    )
            }
        }

        taskField.requestFocus()
        addTaskDialog.show()
    }

    private fun showSelectListDialog(selectedListText: TextView, taskField: EditText) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_select_item, null)
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        builder.setView(view)
        val selectListToLinkDialog = builder.create()
        selectListToLinkDialog.setCancelable(true)

        val selectItemTitleText = view.findViewById<TextView>(R.id.selectItemTitleText)
        val itemContents = view.findViewById<LinearLayout>(R.id.itemContents)
        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val selectOption = view.findViewById<TextView>(R.id.selectOption)

        selectItemTitleText.text = activity.getString(R.string.selectListString)
        selectOption.text = activity.getString(R.string.selectString)

        val listIds = activity.getDatabase().getListIds()
        val selectedItems = SparseBooleanArray()
        val colorTheme = activity.getColorTheme()
        var aSectionIsSelected = false
        selectOption.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent))

        fun setSectionSelected() {
            aSectionIsSelected = true
            selectOption.setTextColor(Color.WHITE)
        }

        fun selectOption() {
            for(id in listIds) {
                val itemSelected = selectedItems[id]
                if(itemSelected) {
                    selectedListId = id
                    break
                }
            }
            selectedListText.text = activity.getDatabase()
                .getList(selectedListId).getTitle()
            taskField.setText(taskField.text.toString())
            taskField.setSelection(taskField.length())
            selectListToLinkDialog.cancel()
        }

        val lists = ArrayList<List>()
        for(id in listIds) lists.add(activity.getDatabase().getList(id))
        val orderedLists = sortListsByLastUpdated(lists)

        fun uncheckOtherItems(selectedId: Int) {
            for((i, list) in orderedLists.withIndex()) {
                val id = list.getListId()
                if(id != selectedId) {
                    val itemView = itemContents[i]
                    val checkbox = itemView.findViewById<ImageView>(R.id.checkbox)
                    checkbox.setImageResource(R.drawable.ic_radio_button_unchecked_gray)
                    selectedItems[id] = false
                }
            }
        }

        fun addListView(id: Int, listTitle: String, listType: String) {
            val itemView = activity.layoutInflater.inflate(R.layout.view_select_item_option, null)
            val itemIcon = itemView.findViewById<ImageView>(R.id.itemIcon)
            val itemTextSpacer = itemView.findViewById<View>(R.id.itemTextSpacer)
            val checkbox = itemView.findViewById<ImageView>(R.id.checkbox)
            val itemText = itemView.findViewById<TextView>(R.id.itemText)

            itemText.text = listTitle
            selectedItems[id] = false

            itemTextSpacer.visibility = View.GONE
            itemIcon.visibility = View.VISIBLE
            val itemIconCode = when(listType) {
                toDoListRef -> R.drawable.ic_list_white
                progressListRef -> R.drawable.ic_view_list_white
                routineListRef -> R.drawable.ic_format_list_numbered_white
                else -> R.drawable.ic_format_list_bulleted_white
            }
            itemIcon.setImageResource(itemIconCode)

            itemView.setOnClickListener {
                setSectionSelected()
                val itemSelected = selectedItems[id]
                if(!itemSelected) {
                    checkbox.setColoredImageResource(
                        R.drawable.ic_radio_button_checked_custom, colorTheme)
                    uncheckOtherItems(id)
                    selectedItems[id] = true
                } else selectOption()
            }

            itemContents.addView(itemView)
        }

        for(list in orderedLists) {
            val listType = list.getListType()
            val listTitle = list.getTitle()
            addListView(list.getListId(), listTitle, listType)
        }

        val itemScrollView = view.findViewById<ScrollView>(R.id.itemScrollView)
        val dialogOptions = ArrayList<View>()
        for(itemView in itemContents.iterator())
            dialogOptions.add(itemView)
        initDialogScrollOptions(itemScrollView, itemContents, dialogOptions)

        cancelOption.setOnClickListener {
            selectListToLinkDialog.cancel()
        }

        selectOption.setOnClickListener {
            if(aSectionIsSelected)
                selectOption()
        }

        selectListToLinkDialog.show()
    }

    @Suppress("SameParameterValue")
    fun showConfirmDialog(confirmMessage: String, confirmString: String,
                          addTaskDialog: BottomSheetDialog? = null,
                          closeAddTaskDialog: Boolean = false,
                          createExamples: Boolean = false) {
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
                    createExamples -> activity.createExamples()
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