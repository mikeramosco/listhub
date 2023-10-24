package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.iterator
import androidx.core.view.size
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_lists.*
import java.util.HashMap

@SuppressLint("InflateParams")
class ListsFragmentDialogs(private val fragment: ListsFragment) {

    fun showSortByDialog() {
        val sortByDialog = BottomSheetDialog(fragment.requireContext())
        val view = fragment.layoutInflater.inflate(R.layout.bottomsheet_sort_list_titles_by, null)
        sortByDialog.setContentView(view)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.removeSortingOption))              // 0
        dialogOptions.add(view.findViewById(R.id.descendingLastUpdatedOption))      // 1
        dialogOptions.add(view.findViewById(R.id.ascendingLastUpdatedOption))       // 2
        dialogOptions.add(view.findViewById(R.id.descendingCreationDateOption))     // 3
        dialogOptions.add(view.findViewById(R.id.ascendingCreationDateOption))      // 4
        dialogOptions.add(view.findViewById(R.id.descendingAlphabeticallyOption))   // 5
        dialogOptions.add(view.findViewById(R.id.ascendingAlphabeticallyOption))    // 6
        initDialogOptions(sortByDialog, dialogOptions)

        fun clickOption(option: LinearLayout) {
            val sortIndex = when(option) {
                dialogOptions[0] -> 0
                dialogOptions[1] -> sortLastUpdatedDescendingIndex
                dialogOptions[2] -> sortLastUpdatedAscendingIndex
                dialogOptions[3] -> sortNewestFirstIndex
                dialogOptions[4] -> sortOldestFirstIndex
                dialogOptions[5] -> sortAToZIndex
                else -> sortZToAIndex
            }

            fragment.getManager().sortList(sortIndex)

            sortByDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        sortByDialog.show()
    }

    fun showFilterListsDialog() {
        val view = fragment.layoutInflater.inflate(R.layout.dialog_filter_lists, null)
        val builder = AlertDialog.Builder(fragment.requireContext())
        builder.setCancelable(false)
        builder.setView(view)
        val filterListsDialog = builder.create()
        filterListsDialog.setCancelable(true)

        val colorTheme = fragment.requireActivity().getColorTheme()

        val archivedLayout = view.findViewById<LinearLayout>(R.id.archivedLayout)
        val favoritesLayout = view.findViewById<LinearLayout>(R.id.favoritesLayout)
        val toDoListsLayout = view.findViewById<LinearLayout>(R.id.toDoListsLayout)
        val progressListsLayout = view.findViewById<LinearLayout>(R.id.progressListsLayout)
        val routineListsLayout = view.findViewById<LinearLayout>(R.id.routineListsLayout)
        val bulletedListsLayout = view.findViewById<LinearLayout>(R.id.bulletedListsLayout)

        val archivedCheckbox = view.findViewById<ImageView>(R.id.archivedCheckbox)
        val favoritesCheckbox = view.findViewById<ImageView>(R.id.favoritesCheckbox)
        val toDoListsCheckbox = view.findViewById<ImageView>(R.id.toDoListsCheckbox)
        val progressListsCheckbox = view.findViewById<ImageView>(R.id.progressListsCheckbox)
        val routineListsCheckbox = view.findViewById<ImageView>(R.id.routineListsCheckbox)
        val bulletedListsCheckbox = view.findViewById<ImageView>(R.id.bulletedListsCheckbox)

        val labelsHeader = view.findViewById<LinearLayout>(R.id.labelsHeader)
        val labelsContainer = view.findViewById<LinearLayout>(R.id.labelsContainer)

        val removeFiltersOption = view.findViewById<TextView>(R.id.removeFiltersOption)
        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val applyOption = view.findViewById<TextView>(R.id.applyOption)

        val filters = fragment.getDatabase().getFilters()
        val checkedOptions = HashMap<String, Boolean>()

        fun initListTypeOptions(optionRef: String,
                                optionLayout: LinearLayout,
                                optionCheckbox: ImageView) {
            checkedOptions[optionRef] = filters.contains(optionRef)
            val filtersContainsOption = checkedOptions[optionRef]?: false
            if(filtersContainsOption)
                optionCheckbox.setColoredImageResource(R.drawable.ic_check_box_custom, colorTheme)

            optionLayout.setOnClickListener {
                val containsOption = checkedOptions[optionRef]?: false
                checkedOptions[optionRef] = if(containsOption) {
                    optionCheckbox.setImageResource(R.drawable.ic_check_box_outline_blank_gray)
                    false
                } else {
                    optionCheckbox.setColoredImageResource(R.drawable.ic_check_box_custom, colorTheme)
                    true
                }
                if(optionRef == progressListRef)
                    toDoListsLayout.performClick()
            }
        }

        initListTypeOptions(favoritesRef, favoritesLayout, favoritesCheckbox)
        initListTypeOptions(toDoListRef, toDoListsLayout, toDoListsCheckbox)
        initListTypeOptions(progressListRef, progressListsLayout, progressListsCheckbox)
        initListTypeOptions(routineListRef, routineListsLayout, routineListsCheckbox)
        initListTypeOptions(bulletedListRef, bulletedListsLayout, bulletedListsCheckbox)
        initListTypeOptions(archivedRef, archivedLayout, archivedCheckbox)

        fun addLabelView(label: String) {
            val labelView = fragment.layoutInflater.inflate(R.layout.view_checkbox_item_option, null)
            val checkbox = labelView.findViewById<ImageView>(R.id.checkbox)
            val labelText = labelView.findViewById<TextView>(R.id.labelText)
            labelText.text = label

            checkedOptions[label] = filters.contains(label)
            val listContainsLabel = checkedOptions[label]?: false

            if(listContainsLabel)
                checkbox.setColoredImageResource(R.drawable.ic_check_box_custom, colorTheme)

            labelView.setOnClickListener {
                val containsLabel = checkedOptions[label]?: false
                checkedOptions[label] = if(containsLabel) {
                    checkbox.setImageResource(R.drawable.ic_check_box_outline_blank_gray)
                    false
                } else {
                    checkbox.setColoredImageResource(R.drawable.ic_check_box_custom, colorTheme)
                    true
                }
            }

            if(listContainsLabel) labelsContainer.addView(labelView, 0)
            else labelsContainer.addView(labelView)
        }

        val noLabelsString = fragment.getString(R.string.noLabelsString)
        val labels = fragment.getDatabase().getLabels()
        for(label in labels) addLabelView(label)
        addLabelView(noLabelsString)
        if(labelsContainer.size == 0) labelsHeader.visibility = View.GONE

        val itemScrollView = view.findViewById<ScrollView>(R.id.itemScrollView)
        val itemContents = view.findViewById<LinearLayout>(R.id.itemContents)
        val dialogOptions = ArrayList<View>()
        dialogOptions.add(favoritesLayout)
        dialogOptions.add(toDoListsLayout)
        dialogOptions.add(progressListsLayout)
        dialogOptions.add(routineListsLayout)
        dialogOptions.add(bulletedListsLayout)
        dialogOptions.add(archivedLayout)
        for(labelView in labelsContainer.iterator())
            dialogOptions.add(labelView)
        initDialogScrollOptions(itemScrollView, itemContents, dialogOptions)

        var cancelClicked = false

        cancelOption.setOnClickListener {
            cancelClicked = true
            filterListsDialog.cancel()
        }

        removeFiltersOption.setOnClickListener {
            cancelClicked = true
            fragment.getDatabase().setFilters(ArrayList())
            fragment.getListsView().applyFilters(ArrayList())
            filterListsDialog.cancel()
        }

        applyOption.setOnClickListener {
            filterListsDialog.cancel()
        }

        filterListsDialog.setOnCancelListener {
            if(!cancelClicked) {
                val newFilters = ArrayList<String>()

                fun addListTypeToFiltersIfChecked(optionRef: String) {
                    val listTypeChecked = checkedOptions[optionRef]?: false
                    if(listTypeChecked) newFilters.add(optionRef)
                }

                addListTypeToFiltersIfChecked(favoritesRef)
                addListTypeToFiltersIfChecked(toDoListRef)
                addListTypeToFiltersIfChecked(progressListRef)
                addListTypeToFiltersIfChecked(routineListRef)
                addListTypeToFiltersIfChecked(bulletedListRef)
                addListTypeToFiltersIfChecked(archivedRef)
                addListTypeToFiltersIfChecked(noLabelsString)

                for(label in labels) {
                    val isChecked = checkedOptions[label]?: false
                    if(isChecked) newFilters.add(label)
                }
                fragment.getDatabase().setFilters(newFilters)
                fragment.getListsView().applyFilters(newFilters)
                filterListsDialog.dismiss()
            }
        }

        filterListsDialog.show()
    }

    fun showManageLabelsDialog() {
        val view = fragment.layoutInflater.inflate(R.layout.dialog_manage_labels, null)
        val builder = AlertDialog.Builder(fragment.requireContext())
        builder.setCancelable(false)
        builder.setView(view)
        val manageLabelsDialog = builder.create()
        manageLabelsDialog.setCancelable(true)

        val colorTheme = fragment.requireActivity().getColorTheme()

        val newLabelOption = view.findViewById<LinearLayout>(R.id.newLabelOption)
        val newLabelOptionIcon = view.findViewById<ImageView>(R.id.newLabelOptionIcon)
        val newLabelOptionText = view.findViewById<TextView>(R.id.newLabelOptionText)
        val labelsContainer = view.findViewById<LinearLayout>(R.id.labelsContainer)
        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val saveOption = view.findViewById<TextView>(R.id.saveOption)

        newLabelOptionIcon.setColoredImageResource(R.drawable.ic_add_custom, colorTheme)
        newLabelOptionText.setTextColor(colorTheme)

        val checkedLabels = HashMap<String, Boolean>()
        val labels = fragment.getDatabase().getLabels()
        val allLabels = fragment.getManager().getAllLabelsOfSelected(labels)
        val labelsToAddOrRemove = ArrayList<String>()
        for(label in labels) {
            val labelView = fragment.layoutInflater.inflate(R.layout.view_checkbox_item_option, null)
            val checkbox = labelView.findViewById<ImageView>(R.id.checkbox)
            val labelText = labelView.findViewById<TextView>(R.id.labelText)
            labelText.text = label

            checkedLabels[label] = allLabels.contains(label)
            val listContainsLabel = checkedLabels[label]?: false

            if(listContainsLabel)
                checkbox.setColoredImageResource(R.drawable.ic_check_box_custom, colorTheme)

            labelView.setOnClickListener {
                if(!labelsToAddOrRemove.contains(label))
                    labelsToAddOrRemove.add(label)
                val containsLabel = checkedLabels[label]?: false
                checkedLabels[label] = if(containsLabel) {
                    checkbox.setImageResource(R.drawable.ic_check_box_outline_blank_gray)
                    false
                } else {
                    checkbox.setColoredImageResource(R.drawable.ic_check_box_custom, colorTheme)
                    true
                }
            }

            if(listContainsLabel) labelsContainer.addView(labelView, 0)
            else labelsContainer.addView(labelView)
        }

        val labelsScrollView = view.findViewById<ScrollView>(R.id.labelsScrollView)
        val dialogOptions = ArrayList<View>()
        for(labelView in labelsContainer.iterator())
            dialogOptions.add(labelView)
        initDialogScrollOptions(labelsScrollView, labelsContainer, dialogOptions)

        newLabelOption.setOnClickListener {
            showAddNewLabelDialog(labels, labelsToAddOrRemove, checkedLabels, labelsContainer)
        }

        var cancelClicked = false

        cancelOption.setOnClickListener {
            cancelClicked = true
            manageLabelsDialog.cancel()
        }

        saveOption.setOnClickListener {
            manageLabelsDialog.cancel()
        }

        manageLabelsDialog.setOnCancelListener {
            if(!cancelClicked && labelsToAddOrRemove.size > 0) {
                val newLabels = ArrayList<String>()
                for(label in labels) {
                    val isChecked = checkedLabels[label]?: false
                    if(isChecked) newLabels.add(label)
                }
                fragment.getManager().updateLabelsOfSelected(
                    labelsToAddOrRemove, checkedLabels)
                fragment.getListsView().reloadLists()
                fragment.getListsView().setToDefaultState()
                manageLabelsDialog.dismiss()
                fragment.getHomeActivity().getHomeFragment().getHomeView().reloadSection()
            }
        }

        manageLabelsDialog.show()

        if(labelsContainer.size == 0)
            showAddNewLabelDialog(labels, labelsToAddOrRemove, checkedLabels, labelsContainer)
    }

    private fun showAddNewLabelDialog(labels: ArrayList<String>,
                                      labelsToAddOrRemove: ArrayList<String>,
                                      checkedLabels: HashMap<String, Boolean>,
                                      labelsContainer: LinearLayout) {
        val addNewLabelDialog = BottomSheetDialog(fragment.requireContext())
        val view = fragment.layoutInflater.inflate(R.layout.bottomsheet_add_new_item, null)
        addNewLabelDialog.setContentView(view)

        val newLabelField = view.findViewById<EditText>(R.id.newItemField)

        val colorTheme = fragment.requireActivity().getColorTheme()
        val colorStateList = ColorStateList.valueOf(colorTheme)
        ViewCompat.setBackgroundTintList(newLabelField, colorStateList)

        val addButton = view.findViewById<LinearLayout>(R.id.addButton)
        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(addButton)
        initDialogOptions(addNewLabelDialog, dialogOptions)

        val addButtonBackground = view.findViewById<ImageView>(R.id.addButtonBackground)
        addButtonBackground.setColoredImageResource(R.drawable.ic_circle_filled_custom, colorTheme)
        val addButtonIcon = view.findViewById<ImageView>(R.id.addButtonIcon)

        fun disableAddButton() {
            addButton.visibility = View.GONE
            addButtonBackground.setImageResource(R.drawable.ic_circle_filled_gray)
            addButtonIcon.setImageResource(R.drawable.ic_add_gray)
        }

        fun enableAddButton() {
            addButton.visibility = View.VISIBLE
            addButtonBackground.setImageResource(R.drawable.ic_circle_filled_custom)
            addButtonIcon.setImageResource(R.drawable.ic_add_black)
        }

        val noLabelsString = fragment.getString(R.string.noLabelsString)

        disableAddButton()
        newLabelField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                val newLabel = newLabelField.text.toString()
                if(newLabel.isEmpty() || labels.contains(newLabel)
                    || newLabel == noLabelsString)
                    disableAddButton() else enableAddButton()
            }
        })

        var addButtonPressed = false

        fun addButtonClicked() {
            val newLabel = newLabelField.text.toString()
            if(newLabel.isEmpty()) addNewLabelDialog.cancel()
            if(newLabel.isEmpty() || labels.contains(newLabel)
                || newLabel == noLabelsString) return
            if(!addButtonPressed) {
                addButtonPressed = true
                labels.add(newLabel)

                val labelView = fragment.layoutInflater.inflate(R.layout.view_checkbox_item_option, null)
                val checkbox = labelView.findViewById<ImageView>(R.id.checkbox)
                val labelText = labelView.findViewById<TextView>(R.id.labelText)
                labelText.text = newLabel

                checkedLabels[newLabel] = true
                checkbox.setColoredImageResource(R.drawable.ic_check_box_custom, colorTheme)
                labelsToAddOrRemove.add(newLabel)

                labelView.setOnClickListener {
                    val containsLabel = checkedLabels[newLabel] ?: false
                    checkedLabels[newLabel] = if (containsLabel) {
                        checkbox.setImageResource(R.drawable.ic_check_box_outline_blank_gray)
                        false
                    } else {
                        checkbox.setColoredImageResource(R.drawable.ic_check_box_custom, colorTheme)
                        true
                    }
                }

                labelsContainer.addView(labelView, 0)
                addNewLabelDialog.cancel()
            }
        }

        addButton.setOnClickListener {
            addButtonClicked()
        }

        newLabelField.setOnEditorActionListener { _, actionId, _ ->
            val donePressed = actionId == EditorInfo.IME_ACTION_DONE
            if(donePressed) addButtonClicked()
            donePressed
        }

        newLabelField.requestFocus()
        addNewLabelDialog.show()
    }

    fun showSelectedMoreOptionsDialog() {
        val moreOptionsDialog = BottomSheetDialog(fragment.requireContext())
        val view = fragment.layoutInflater.inflate(
            R.layout.bottomsheet_list_titles_selected_more_options, null)
        moreOptionsDialog.setContentView(view)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.selectAllOption))      // 0
        dialogOptions.add(view.findViewById(R.id.manageLabelsOption))   // 1
        dialogOptions.add(view.findViewById(R.id.duplicateOption))      // 2
        dialogOptions.add(view.findViewById(R.id.deleteOption))         // 3
        dialogOptions.add(view.findViewById(R.id.reorderOption))        // 4
        dialogOptions.add(view.findViewById(R.id.archiveOption))        // 5
        dialogOptions.add(view.findViewById(R.id.unarchiveOption))      // 6
        initDialogOptions(moreOptionsDialog, dialogOptions)

        if(fragment.getDatabase().getFilters().contains(archivedRef))
            dialogOptions[5].visibility = View.GONE
        else dialogOptions[6].visibility = View.GONE

        view.findViewById<TextView>(R.id.manageLabelsText).text =
            fragment.manageLabelsText.text.toString()

        if(fragment.getManager().getSelectedCount() == 1) {
            view.findViewById<TextView>(R.id.duplicateListsText).text =
                fragment.getString(R.string.duplicateListString)
            view.findViewById<TextView>(R.id.deleteListsText).text =
                fragment.getString(R.string.deleteListString)
        }

        if(fragment.listTitlesContainer.size < 2) dialogOptions[4].visibility = View.GONE

        fun clickOption(option: LinearLayout) {
            when(option) {
                dialogOptions[0] -> fragment.getListsView().selectAll()
                dialogOptions[1] -> fragment.getDialogs().showManageLabelsDialog()
                dialogOptions[2] -> fragment.getManager().duplicateOptionClicked()
                dialogOptions[3] -> fragment.getManager().deleteOptionClicked()
                dialogOptions[4] -> fragment.getHomeActivity().openReorderPage()
                dialogOptions[5] -> fragment.getManager().archiveOptionClicked()
                dialogOptions[6] -> fragment.getManager().unarchiveOptionClicked()
            }

            moreOptionsDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        moreOptionsDialog.show()
    }

    fun showConfirmDialog(confirmMessage: String, confirmString: String,
                          deleteLists: Boolean = false, duplicateLists: Boolean = false,
                          archiveLists: Boolean = false, unarchiveLists: Boolean = false) {
        val view = fragment.layoutInflater.inflate(R.layout.dialog_confirm_delete, null)
        val builder = AlertDialog.Builder(fragment.requireContext())
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
                    deleteLists -> {
                        fragment.getListsView().setToDefaultState()
                        fragment.getManager().deleteSelectedLists()
                    }
                    duplicateLists -> fragment.getManager().duplicateSelectedLists()
                    archiveLists -> fragment.getManager().archiveSelectedLists()
                    unarchiveLists -> fragment.getManager().unarchiveSelectedLists()
                }
            }
        }

        confirmDeleteNoteDialog.show()
    }
}