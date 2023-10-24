package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseBooleanArray
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.util.set
import androidx.core.view.ViewCompat
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.core.view.size
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*

@SuppressLint("InflateParams")
open class ListDialogs(private val listType: String,
                       private val activity: Activity,
                       private val database: ListsDatabase) {

    var currentDialog: BottomSheetDialog? = null
    var currentDialogView: View? = null
    var currentDialogTask: Task? = null

    fun addTaskDialogIsShowing(): Boolean {
        return currentDialogView != null
    }

    fun clearDialogData() {
        currentDialog = null
        currentDialogTask = null
        currentDialogView = null
        setDialogClosed()
    }

    private fun setDialogClosed() {
        when(listType) {
            toDoListRef -> (activity as ToDoListActivity)
                .getManager().setDialogOpened(false)
            progressListRef -> (activity as ProgressListActivity)
                .getManager().setDialogOpened(false)
            routineListRef -> (activity as RoutineListActivity)
                .getManager().setDialogOpened(false)
            else -> (activity as BulletedListActivity)
                .getManager().setDialogOpened(false)
        }
    }

    fun showDatePickerDialog(setDate: Calendar?, list: List,
                             supportFragmentManager: FragmentManager) {
        val date = setDate ?: getTodaysDate()
        val datePicker = DatePickerFragment(date, getDatePickerTheme(list))
        datePicker.show(supportFragmentManager, "date picker")
    }

    fun showChooseRepeatingDaysDialog() {
        val repeatingDays = if(listType == routineListRef)
            (activity as RoutineListActivity).getList().getRepeatingDays()
        else currentDialogTask?.getRepeatingDays() ?: ArrayList()
        val view = activity.layoutInflater.inflate(
            R.layout.dialog_choose_repeating_days, null)
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        builder.setView(view)
        val chooseRepeatingDaysDialog = builder.create()
        chooseRepeatingDaysDialog.setCancelable(true)

        val defaultColor = ContextCompat.getColor(activity, R.color.colorLightGray)
        val disabledColor = ContextCompat.getColor(activity, R.color.colorAccent)
        val defaultBackground = ContextCompat.getDrawable(
            activity, R.drawable.ic_circle_outline_gray)
        val selectedBackground = ContextCompat.getDrawable(
            activity, R.drawable.ic_circle_filled_custom)

        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val doneOption = view.findViewById<TextView>(R.id.doneOption)
        doneOption.setTextColor(disabledColor)

        var repeatingDaysCount = 0

        val dayOptions = ArrayList<TextView>()
        dayOptions.add(view.findViewById(R.id.sundayOption))
        dayOptions.add(view.findViewById(R.id.mondayOption))
        dayOptions.add(view.findViewById(R.id.tuesdayOption))
        dayOptions.add(view.findViewById(R.id.wednesdayOption))
        dayOptions.add(view.findViewById(R.id.thursdayOption))
        dayOptions.add(view.findViewById(R.id.fridayOption))
        dayOptions.add(view.findViewById(R.id.saturdayOption))

        val daysSelected = ArrayList<Boolean>()
        for(option in dayOptions)
            daysSelected.add(false)

        fun toggleOption(dayIndex: Int) {
            val selected = daysSelected[dayIndex]
            if(selected) {
                daysSelected[dayIndex] = false
                dayOptions[dayIndex].setTextColor(defaultColor)
                dayOptions[dayIndex].background = defaultBackground
                if(--repeatingDaysCount == 0) doneOption.setTextColor(disabledColor)
            } else {
                if(repeatingDaysCount++ == 0) doneOption.setTextColor(Color.WHITE)
                daysSelected[dayIndex] = true
                dayOptions[dayIndex].setTextColor(Color.BLACK)
                dayOptions[dayIndex].background = selectedBackground
            }
        }

        if(repeatingDays.contains(Calendar.SUNDAY)) toggleOption(0)
        if(repeatingDays.contains(Calendar.MONDAY)) toggleOption(1)
        if(repeatingDays.contains(Calendar.TUESDAY)) toggleOption(2)
        if(repeatingDays.contains(Calendar.WEDNESDAY)) toggleOption(3)
        if(repeatingDays.contains(Calendar.THURSDAY)) toggleOption(4)
        if(repeatingDays.contains(Calendar.FRIDAY)) toggleOption(5)
        if(repeatingDays.contains(Calendar.SATURDAY)) toggleOption(6)

        for((i, option )in dayOptions.withIndex())
            option.setOnClickListener { toggleOption(i) }

        var repeatingDaysSaved = false

        cancelOption.setOnClickListener { chooseRepeatingDaysDialog.cancel() }

        doneOption.setOnClickListener {
            if(repeatingDaysCount > 0) {
                repeatingDaysSaved = true
                chooseRepeatingDaysDialog.cancel()
            }
        }

        chooseRepeatingDaysDialog.setOnCancelListener {
            chooseRepeatingDaysDialog.dismiss()
            if(repeatingDaysSaved) {
                if(listType == routineListRef) (activity as RoutineListActivity)
                    .getDialogs().setRepeatingDays(daysSelected)
                else setRepeatingDays(daysSelected)
            }
        }

        chooseRepeatingDaysDialog.show()
    }

    private fun setRepeatingDays(daysSelected: ArrayList<Boolean>) {
        val repeatingDays = ArrayList<Int>()
        if(daysSelected[0]) repeatingDays.add(Calendar.SUNDAY)
        if(daysSelected[1]) repeatingDays.add(Calendar.MONDAY)
        if(daysSelected[2]) repeatingDays.add(Calendar.TUESDAY)
        if(daysSelected[3]) repeatingDays.add(Calendar.WEDNESDAY)
        if(daysSelected[4]) repeatingDays.add(Calendar.THURSDAY)
        if(daysSelected[5]) repeatingDays.add(Calendar.FRIDAY)
        if(daysSelected[6]) repeatingDays.add(Calendar.SATURDAY)
        if(currentDialogTask != null) {
            currentDialogTask?.setRepeatingDays(repeatingDays)
            val repeatingDaysString =
                currentDialogTask?.getRepeatingDaysString(activity)
                    ?: return
            addDateOnDialog(repeatingDaysString, isRepeating = true)
        } else setSelectedTaskDates(repeatingDays)
    }

    private fun setSelectedTaskDates(repeatingDays: ArrayList<Int>) {
        when(listType) {
            toDoListRef -> (activity as ToDoListActivity).getManager()
                .setSelectedTasksDates(repeatingDays = repeatingDays)
            progressListRef -> (activity as ProgressListActivity).getManager()
                .setSelectedTasksDates(repeatingDays = repeatingDays)
            bulletedListRef -> (activity as BulletedListActivity).getManager()
                .setSelectedTasksDates(repeatingDays = repeatingDays)
        }
    }

    private fun getList(): List {
        return when(listType) {
            toDoListRef -> (activity as ToDoListActivity).getList()
            progressListRef -> (activity as ProgressListActivity).getList()
            routineListRef -> (activity as RoutineListActivity).getList()
            else -> (activity as BulletedListActivity).getList()
        }
    }

    fun addTimeOnDialog(timeText: String, moveOptionToLeft: Boolean = false) {
        val colorTheme = activity.getColorTheme(getList().getColorThemeIndex())
        val view = currentDialogView?: return
        val addTimeText = view.findViewById<TextView>(R.id.addTimeText)
        addTimeText.text = timeText
        addTimeText.setTextColor(colorTheme)
        val addTimeIcon = view.findViewById<ImageView>(R.id.addTimeIcon)
        addTimeIcon.setColoredImageResource(R.drawable.ic_access_time_custom, colorTheme)
        val removeTimeIcon = view.findViewById<ImageView>(R.id.removeTimeIcon)
        removeTimeIcon.visibility = View.VISIBLE
        removeTimeIcon.setColoredImageResource(R.drawable.ic_close_custom, colorTheme)
        if(moveOptionToLeft) {
            val addDateOption =
                view.findViewById<LinearLayout>(R.id.addTimeOption)
            moveOptionToLeft(addDateOption)
        }
    }

    fun addDateOnDialog(calendarText: String, isRepeating: Boolean = false,
                        moveOptionToLeft: Boolean = false) {
        val colorTheme = activity.getColorTheme(getList().getColorThemeIndex())
        val addDateText = currentDialogView?.findViewById<TextView>(R.id.addDateText)
        addDateText?.text = calendarText
        addDateText?.setTextColor(colorTheme)
        val addDateIcon = currentDialogView?.findViewById<ImageView>(R.id.addDateIcon)
        val iconCode = if(isRepeating)
            R.drawable.ic_autorenew_custom else R.drawable.ic_date_custom
        addDateIcon?.setColoredImageResource(iconCode, colorTheme)
        val removeDateIcon =
            currentDialogView?.findViewById<ImageView>(R.id.removeDateIcon)
        removeDateIcon?.visibility = View.VISIBLE
        removeDateIcon?.setColoredImageResource(R.drawable.ic_close_custom, colorTheme)
        if(moveOptionToLeft) {
            val addDateOption = currentDialogView
                ?.findViewById<LinearLayout>(R.id.addDateOption)
            if(addDateOption != null) moveOptionToLeft(addDateOption)
        }
    }

    fun addWebsiteLinkOnDialog(websiteLink: String, moveOptionToLeft: Boolean = false) {
        val colorTheme = activity.getColorTheme(getList().getColorThemeIndex())
        val view = currentDialogView?: return
        val linkToWebsiteText = view.findViewById<TextView>(R.id.linkToWebsiteText)
        linkToWebsiteText.text = websiteLink
            .formatAsWebsiteLink(activity).limitChar(MAX_TEXT_DISPLAY_LENGTH)
        linkToWebsiteText.setTextColor(colorTheme)
        val linkToWebsiteIcon = view.findViewById<ImageView>(R.id.linkToWebsiteIcon)
        linkToWebsiteIcon.setColoredImageResource(R.drawable.ic_link_custom, colorTheme)
        val unlinkListIcon = view.findViewById<ImageView>(R.id.unlinkWebsiteIcon)
        unlinkListIcon.visibility = View.VISIBLE
        unlinkListIcon.setColoredImageResource(R.drawable.ic_close_custom, colorTheme)
        if(moveOptionToLeft) {
            val linkToListOption =
                view.findViewById<LinearLayout>(R.id.linkToWebsiteOption)
            moveOptionToLeft(linkToListOption)
        }
    }

    fun addLinkOnDialog(linkedListId: Int, moveOptionToLeft: Boolean = false) {
        val colorTheme = activity.getColorTheme(getList().getColorThemeIndex())
        val linkToListText =
            currentDialogView?.findViewById<TextView>(R.id.linkToListText)
        val list = database.getList(linkedListId)
        val listTitle = list.getTitle().limitChar(MAX_TEXT_DISPLAY_LENGTH)
        linkToListText?.text = listTitle
        linkToListText?.setTextColor(colorTheme)
        val linkToListIcon =
            currentDialogView?.findViewById<ImageView>(R.id.linkToListIcon)
        val iconCode = when(list.getListType()) {
            toDoListRef -> R.drawable.ic_list_custom
            progressListRef -> R.drawable.ic_view_list_custom
            routineListRef -> R.drawable.ic_format_list_numbered_custom
            else -> R.drawable.ic_format_list_bulleted_custom
        }
        linkToListIcon?.setColoredImageResource(iconCode, colorTheme)
        val unlinkListIcon =
            currentDialogView?.findViewById<ImageView>(R.id.unlinkListIcon)
        unlinkListIcon?.visibility = View.VISIBLE
        unlinkListIcon?.setColoredImageResource(R.drawable.ic_close_custom, colorTheme)
        if(moveOptionToLeft) {
            val linkToListOption = currentDialogView
                ?.findViewById<LinearLayout>(R.id.linkToListOption)
            if(linkToListOption != null) moveOptionToLeft(linkToListOption)
        }
    }

    fun addRewardOnDialog(moveOptionToLeft: Boolean = false) {
        val colorTheme = activity.getColorTheme(getList().getColorThemeIndex())
        val removeRewardIcon =
            currentDialogView?.findViewById<ImageView>(R.id.removeRewardIcon)
        if(removeRewardIcon?.visibility == View.GONE) {
            val addRewardText =
                currentDialogView?.findViewById<TextView>(R.id.addRewardText)
            addRewardText?.text = activity.getString(R.string.viewRewardString)
            addRewardText?.setTextColor(colorTheme)
            val addRewardIcon =
                currentDialogView?.findViewById<ImageView>(R.id.addRewardIcon)
            addRewardIcon?.setColoredImageResource(R.drawable.ic_card_giftcard_custom, colorTheme)
            removeRewardIcon.visibility = View.VISIBLE
            removeRewardIcon.setColoredImageResource(R.drawable.ic_close_custom, colorTheme)
            if(moveOptionToLeft) {
                val addRewardOption = currentDialogView
                    ?.findViewById<LinearLayout>(R.id.addRewardOption)
                if(addRewardOption != null) moveOptionToLeft(addRewardOption)
            }
        }
    }

    fun addNoteOnDialog(moveOptionToLeft: Boolean = false) {
        val colorTheme = activity.getColorTheme(getList().getColorThemeIndex())
        val deleteNoteIcon =
            currentDialogView?.findViewById<ImageView>(R.id.deleteNoteIcon)
        if(deleteNoteIcon?.visibility == View.GONE) {
            val addNoteText = currentDialogView?.findViewById<TextView>(R.id.addNoteText)
            addNoteText?.text = activity.getString(R.string.viewNoteString)
            addNoteText?.setTextColor(colorTheme)
            val addNoteIcon =
                currentDialogView?.findViewById<ImageView>(R.id.addNoteIcon)
            addNoteIcon?.setColoredImageResource(R.drawable.ic_description_custom, colorTheme)
            deleteNoteIcon.visibility = View.VISIBLE
            deleteNoteIcon.setColoredImageResource(R.drawable.ic_close_custom, colorTheme)
            if(moveOptionToLeft) {
                val addNoteOption = currentDialogView
                    ?.findViewById<LinearLayout>(R.id.addNoteOption)
                if(addNoteOption != null) moveOptionToLeft(addNoteOption)
            }
        }
    }

    private fun moveOptionToLeft(optionView: LinearLayout) {
        val optionsContents = currentDialogView
            ?.findViewById<LinearLayout>(R.id.optionsContents)
        optionsContents?.removeView(optionView)
        optionsContents?.addView(optionView, 1)
    }

    private fun removeWebsiteLinkFromDialog() {
        val view = currentDialogView?: return
        val linkToWebsiteText = view.findViewById<TextView>(R.id.linkToWebsiteText)
        linkToWebsiteText.text = activity.getString(R.string.linkToWebsiteString)
        linkToWebsiteText.setTextColor(Color.WHITE)
        val linkToListIcon = view.findViewById<ImageView>(R.id.linkToWebsiteIcon)
        linkToListIcon.setImageResource(R.drawable.ic_link_white)
        val unlinkListIcon = view.findViewById<ImageView>(R.id.unlinkWebsiteIcon)
        unlinkListIcon.visibility = View.GONE
    }

    private fun removeLinkFromDialog() {
        val linkToListText =
            currentDialogView?.findViewById<TextView>(R.id.linkToListText)
        linkToListText?.text = activity.getString(R.string.linkToListString)
        linkToListText?.setTextColor(Color.WHITE)
        val linkToListIcon =
            currentDialogView?.findViewById<ImageView>(R.id.linkToListIcon)
        linkToListIcon?.setImageResource(R.drawable.ic_link_white)
        val unlinkListIcon =
            currentDialogView?.findViewById<ImageView>(R.id.unlinkListIcon)
        unlinkListIcon?.visibility = View.GONE
    }

    private fun removeRewardFromDialog() {
        val removeRewardIcon =
            currentDialogView?.findViewById<ImageView>(R.id.removeRewardIcon)
        if(removeRewardIcon?.visibility == View.VISIBLE) {
            val addRewardText =
                currentDialogView?.findViewById<TextView>(R.id.addRewardText)
            addRewardText?.text = activity.getString(R.string.addRewardString)
            addRewardText?.setTextColor(Color.WHITE)
            val addRewardIcon =
                currentDialogView?.findViewById<ImageView>(R.id.addRewardIcon)
            addRewardIcon?.setImageResource(R.drawable.ic_card_giftcard_white)
            removeRewardIcon.visibility = View.GONE
        }
    }

    private fun removeNoteFromDialog() {
        val deleteNoteIcon =
            currentDialogView?.findViewById<ImageView>(R.id.deleteNoteIcon)
        if(deleteNoteIcon?.visibility == View.VISIBLE) {
            val addNoteText =
                currentDialogView?.findViewById<TextView>(R.id.addNoteText)
            addNoteText?.text = activity.getString(R.string.addNoteString)
            addNoteText?.setTextColor(Color.WHITE)
            val addNoteIcon =
                currentDialogView?.findViewById<ImageView>(R.id.addNoteIcon)
            addNoteIcon?.setImageResource(R.drawable.ic_description_white)
            deleteNoteIcon.visibility = View.GONE
        }
    }

    private fun removeDateFromDialog() {
        val addDateText =
            currentDialogView?.findViewById<TextView>(R.id.addDateText)
        addDateText?.text = activity.getString(R.string.addToCalendarString)
        addDateText?.setTextColor(Color.WHITE)
        val addDateIcon =
            currentDialogView?.findViewById<ImageView>(R.id.addDateIcon)
        addDateIcon?.setImageResource(R.drawable.ic_date_white)
        val removeDateIcon =
            currentDialogView?.findViewById<ImageView>(R.id.removeDateIcon)
        removeDateIcon?.visibility = View.GONE
    }

    private fun removeTimeFromDialog() {
        val view = currentDialogView?: return
        val addTimeText = view.findViewById<TextView>(R.id.addTimeText)
        addTimeText.text = activity.getString(R.string.addTimeString)
        addTimeText.setTextColor(Color.WHITE)
        val addTimeIcon = view.findViewById<ImageView>(R.id.addTimeIcon)
        addTimeIcon.setImageResource(R.drawable.ic_access_time_white)
        val removeTimeIcon = view.findViewById<ImageView>(R.id.removeTimeIcon)
        removeTimeIcon.visibility = View.GONE
    }

    fun showAddWebsiteLinkDialog() {
        val addWebsiteLinkDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(
            R.layout.bottomsheet_add_new_item, null)
        addWebsiteLinkDialog.setContentView(view)

        val task = currentDialogTask?: return
        val originalWebsiteLink = task.getWebsiteLink()

        val websitePrefixText = view.findViewById<TextView>(R.id.websitePrefixText)
        websitePrefixText.visibility = View.VISIBLE

        val websiteLinkField = view.findViewById<EditText>(R.id.newItemField)
        websiteLinkField.hint = activity.getString(R.string.websiteLinkHint)

        websiteLinkField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                val websiteLinkString = websiteLinkField.text.toString()
                websitePrefixText.visibility =
                    if(websiteLinkString.isFormattedAsWebsite(activity))
                        View.GONE else View.VISIBLE
            }
        })
        websiteLinkField.setText(task.getWebsiteLink())

        val colorTheme = activity.getColorTheme(getList().getColorThemeIndex())
        val colorStateList = ColorStateList.valueOf(colorTheme)
        ViewCompat.setBackgroundTintList(websiteLinkField, colorStateList)

        val addButton = view.findViewById<LinearLayout>(R.id.addButton)
        val openLinkButton = view.findViewById<LinearLayout>(R.id.openLinkButton)
        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(addButton)
        dialogOptions.add(openLinkButton)
        initDialogOptions(addWebsiteLinkDialog, dialogOptions)

        val addButtonIcon = view.findViewById<ImageView>(R.id.addButtonIcon)
        val addButtonBackground = view.findViewById<ImageView>(R.id.addButtonBackground)
        val openLinkButtonBackground = view.findViewById<ImageView>(R.id.openLinkButtonBackground)

        view.findViewById<ConstraintLayout>(R.id.openLinkButtonLayout).visibility = View.VISIBLE

        addButton.visibility = View.VISIBLE
        addButtonBackground.setColoredImageResource(
            R.drawable.ic_circle_filled_custom, colorTheme)
        addButtonIcon.setImageResource(R.drawable.ic_save_black)

        openLinkButtonBackground.setImageResource(R.drawable.ic_circle_filled_custom)

        fun addButtonClicked() {
            addWebsiteLinkDialog.cancel()
        }

        addButton.setOnClickListener {
            addButtonClicked()
        }

        openLinkButton.setOnClickListener {
            val websiteLinkFromField = websiteLinkField.text.toString()
            if(websiteLinkFromField.isNotEmpty()) {
                val websiteLink = websiteLinkFromField.formatAsWebsiteLink(activity)
                val openWebsite = Intent(Intent.ACTION_VIEW, Uri.parse(websiteLink))
                openWebsite.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.intent.setPackage("com.android.chrome")
                try {
                    activity.startActivity(openWebsite)
                } catch (e: ActivityNotFoundException) {
                    activity.intent.setPackage(null)
                    activity.startActivity(openWebsite)
                }
            }
        }

        websiteLinkField.setOnEditorActionListener { _, actionId, _ ->
            val donePressed = actionId == EditorInfo.IME_ACTION_DONE
            if(donePressed) addButtonClicked()
            donePressed
        }

        addWebsiteLinkDialog.setOnCancelListener {
            val websiteLinkString = websiteLinkField.text.toString()
            if(originalWebsiteLink != websiteLinkString) {
                task.setWebsiteLink(websiteLinkString)
                updateDialogWebsiteLinkState()
            }
        }

        websiteLinkField.requestFocus()
        addWebsiteLinkDialog.show()
    }

    fun showAddRewardDialog() {
        val addRewardDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(
            R.layout.bottomsheet_add_new_item, null)
        addRewardDialog.setContentView(view)

        val task = currentDialogTask?: return
        val originalReward = task.getReward()

        val rewardField = view.findViewById<EditText>(R.id.newItemField)
        rewardField.hint = activity.getString(R.string.setARewardString)
        rewardField.setText(task.getReward())

        val colorTheme = activity.getColorTheme(getList().getColorThemeIndex())
        val colorStateList = ColorStateList.valueOf(colorTheme)
        ViewCompat.setBackgroundTintList(rewardField, colorStateList)

        val addButton = view.findViewById<LinearLayout>(R.id.addButton)
        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(addButton)
        initDialogOptions(addRewardDialog, dialogOptions)

        val addButtonBackground = view.findViewById<ImageView>(R.id.addButtonBackground)
        val addButtonIcon = view.findViewById<ImageView>(R.id.addButtonIcon)

        addButton.visibility = View.VISIBLE
        addButtonBackground.setColoredImageResource(
            R.drawable.ic_circle_filled_custom, colorTheme)
        addButtonIcon.setImageResource(R.drawable.ic_save_black)

        fun addButtonClicked() {
            addRewardDialog.cancel()
        }

        addButton.setOnClickListener {
            addButtonClicked()
        }

        rewardField.setOnEditorActionListener { _, actionId, _ ->
            val donePressed = actionId == EditorInfo.IME_ACTION_DONE
            if(donePressed) addButtonClicked()
            donePressed
        }

        addRewardDialog.setOnCancelListener {
            val rewardString = rewardField.text.toString()
            if(originalReward != rewardString) {
                task.setReward(rewardString)
                updateDialogRewardState()
            }
        }

        rewardField.requestFocus()
        addRewardDialog.show()
    }

    private fun updateDialogWebsiteLinkState() {
        val task = currentDialogTask?: return
        if(task.hasWebsiteLink()) addWebsiteLinkOnDialog(task.getWebsiteLink())
        else removeWebsiteLinkFromDialog()
    }

    private fun updateDialogRewardState() {
        val task = currentDialogTask?: return
        if(task.hasReward()) addRewardOnDialog()
        else removeRewardFromDialog()
    }

    fun openTaskNote(taskString: String) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_view_note, null)
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        builder.setView(view)
        val viewNoteDialog = builder.create()
        viewNoteDialog.setCancelable(true)

        val task = currentDialogTask?: return

        val taskText = view.findViewById<TextView>(R.id.taskText)
        val noteField = view.findViewById<EditText>(R.id.noteField)
        val saveIcon = view.findViewById<ImageView>(R.id.saveIcon)

        when {
            taskString.isNotEmpty() -> taskText.text = taskString
            listType == routineListRef -> taskText.text =
                activity.getString(R.string.stepNoteString)
            listType == bulletedListRef -> taskText.text =
                activity.getString(R.string.bulletpointNoteString)
            task.isReward() -> taskText.text =
                activity.getString(R.string.rewardNoteString)
        }

        noteField.setText(currentDialogTask?.getNote())

        saveIcon.setOnClickListener { viewNoteDialog.cancel() }

        viewNoteDialog.setOnCancelListener {
            val noteString = noteField.text.toString()
            currentDialogTask?.setNote(noteString)
            updateDialogNoteState()
            viewNoteDialog.dismiss()
        }

        viewNoteDialog.show()
    }

    private fun updateDialogNoteState() {
        val task = currentDialogTask?: return
        if(task.hasNote())addNoteOnDialog()
        else removeNoteFromDialog()
    }

    fun showManageLabelsDialog(nameListDialog: View? = null) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_manage_labels, null)
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        builder.setView(view)
        val manageLabelsDialog = builder.create()
        manageLabelsDialog.setCancelable(true)

        val colorTheme = activity.getColorTheme(getList().getColorThemeIndex())

        val newLabelOption = view.findViewById<LinearLayout>(R.id.newLabelOption)
        val newLabelOptionIcon = view.findViewById<ImageView>(R.id.newLabelOptionIcon)
        val newLabelOptionText = view.findViewById<TextView>(R.id.newLabelOptionText)
        val labelsContainer = view.findViewById<LinearLayout>(R.id.labelsContainer)
        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val saveOption = view.findViewById<TextView>(R.id.saveOption)

        val primaryColor = activity.getColorTheme()
        newLabelOptionIcon.setColoredImageResource(R.drawable.ic_add_custom, primaryColor)
        newLabelOptionText.setTextColor(primaryColor)

        var listLabels = ArrayList<String>()
        if(getList().hasLabels())
            listLabels = getList().getLabels()

        val checkedLabels = HashMap<String, Boolean>()
        val labels = database.getLabels()
        for(label in labels) {
            val labelView = activity.layoutInflater.inflate(
                R.layout.view_checkbox_item_option, null)
            val checkbox = labelView.findViewById<ImageView>(R.id.checkbox)
            val labelText = labelView.findViewById<TextView>(R.id.labelText)
            labelText.text = label

            checkedLabels[label] = listLabels.contains(label)
            val listContainsLabel = checkedLabels[label]?: false

            if(listContainsLabel)
                checkbox.setColoredImageResource(R.drawable.ic_check_box_custom, colorTheme)

            labelView.setOnClickListener {
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
            showAddNewLabelDialog(labels, checkedLabels, labelsContainer)
        }

        var cancelClicked = false

        cancelOption.setOnClickListener {
            cancelClicked = true
            manageLabelsDialog.cancel()
        }

        saveOption.setOnClickListener {
            manageLabelsDialog.cancel()
        }

        fun setNewLabels() {
            val newLabels = ArrayList<String>()
            for(label in labels) {
                val isChecked = checkedLabels[label]?: false
                if(isChecked) newLabels.add(label)
            }
            getList().setLabels(newLabels)
            displayLabels()
            updateList()
        }

        manageLabelsDialog.setOnCancelListener {
            if(!cancelClicked) {
                setNewLabels()
                if(nameListDialog != null)
                    updateDialogLabels(nameListDialog)
                manageLabelsDialog.dismiss()
            }
        }

        manageLabelsDialog.show()

        if(labelsContainer.size == 0)
            showAddNewLabelDialog(labels, checkedLabels, labelsContainer)
    }

    private fun displayLabels() {
        when(listType) {
            toDoListRef -> (activity as ToDoListActivity)
                .getView().displayLabels(getList().getLabelsString())
            progressListRef -> (activity as ProgressListActivity)
                .getView().displayLabels(getList().getLabelsString())
            routineListRef -> (activity as RoutineListActivity)
                .getView().displayLabels(getList().getLabelsString())
            bulletedListRef -> (activity as BulletedListActivity)
                .getView().displayLabels(getList().getLabelsString())
        }
    }

    private fun updateList() {
        when(listType) {
            toDoListRef -> (activity as ToDoListActivity).updateToDoList()
            progressListRef -> (activity as ProgressListActivity).updateProgressList()
            routineListRef -> (activity as RoutineListActivity).updateRoutineList()
            bulletedListRef -> (activity as BulletedListActivity).updateBulletedList()
        }
    }

    private fun showAddNewLabelDialog(labels: ArrayList<String>,
                                      checkedLabels: HashMap<String, Boolean>,
                                      labelsContainer: LinearLayout) {
        val addNewLabelDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(
            R.layout.bottomsheet_add_new_item, null)
        addNewLabelDialog.setContentView(view)

        val newLabelField = view.findViewById<EditText>(R.id.newItemField)

        val colorTheme = activity.getColorTheme(getList().getColorThemeIndex())
        val colorStateList = ColorStateList.valueOf(colorTheme)
        ViewCompat.setBackgroundTintList(newLabelField, colorStateList)

        val addButton = view.findViewById<LinearLayout>(R.id.addButton)
        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(addButton)
        initDialogOptions(addNewLabelDialog, dialogOptions)

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

        val noLabelsString = activity.getString(R.string.noLabelsString)

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

                val labelView = activity.layoutInflater.inflate(
                    R.layout.view_checkbox_item_option, null)
                val checkbox = labelView.findViewById<ImageView>(R.id.checkbox)
                val labelText = labelView.findViewById<TextView>(R.id.labelText)
                labelText.text = newLabel

                checkedLabels[newLabel] = true
                checkbox.setColoredImageResource(R.drawable.ic_check_box_custom, colorTheme)

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

    private fun updateDialogLabels(nameListDialog: View?) {
        val labelText = nameListDialog?.findViewById<TextView>(R.id.labelText)
        val manageLabelsButton = nameListDialog
            ?.findViewById<TextView>(R.id.manageLabelsButton)
        val nameListDialogParent = nameListDialog
            ?.findViewById<LinearLayout>(R.id.nameListDialogParent)
        val list = getList()
        if(list.hasLabels()) {
            val displayedLabel = labelText?.text.toString()
            val currentLabel = list.getLabelsString()
            if(displayedLabel != currentLabel || labelText?.visibility == View.GONE) {
                labelText?.visibility = View.GONE
                if (nameListDialogParent != null) beginTransition(nameListDialogParent)
                labelText?.visibility = View.VISIBLE
                labelText?.text = currentLabel
                manageLabelsButton?.text = activity.getString(R.string.manageLabelsString)
            }
        } else {
            if(nameListDialogParent != null) beginTransition(nameListDialogParent)
            labelText?.visibility = View.GONE
            manageLabelsButton?.text = activity.getString(R.string.addLabelString)
        }
    }

    fun setCurrentDialogTime(hour: Int, minute: Int) {
        val task = currentDialogTask?: return
        task.setTime(hour, minute)
        addTimeOnDialog(getTimeText(hour, minute))
        if(listType != routineListRef) {
            val taskHasDate = task.hasDate()
            val taskIsRepeating = task.isRepeating()
            if (!taskHasDate && !taskIsRepeating)
                setCurrentDialogDate(getTodaysDate())
        }
    }

    fun setCurrentDialogDate(date: Calendar) {
        currentDialogTask?.setDate(date)
        addDateOnDialog(activity.getDateText(date))
    }

    fun showSortByDialog() {
        val sortByDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_sort_by, null)
        sortByDialog.setContentView(view)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.dueDateOption))
        dialogOptions.add(view.findViewById(R.id.creationDateOption))
        dialogOptions.add(view.findViewById(R.id.alphabeticallyOption))
        initDialogOptions(sortByDialog, dialogOptions)

        if(listType == routineListRef)
            dialogOptions[0].visibility = View.GONE

        fun clickOption(option: LinearLayout) {
            val sortIndex = when(option) {
                dialogOptions[0] -> sortDueDateDescendingIndex
                dialogOptions[1] -> sortNewestFirstIndex
                else -> sortAToZIndex
            }

            sortList(sortIndex)

            sortByDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        sortByDialog.show()
    }

    private fun sortList(sortIndex: Int) {
        when(listType) {
            toDoListRef -> (activity as ToDoListActivity).getManager().sortList(sortIndex)
            progressListRef -> (activity as ProgressListActivity).getManager().sortList(sortIndex)
            routineListRef -> (activity as RoutineListActivity).getManager().sortList(sortIndex)
            bulletedListRef -> (activity as BulletedListActivity).getManager().sortList(sortIndex)
        }
    }

    fun showSelectListDialog(toLink: Boolean = false, toMove: Boolean = false,
                             forDialogTask: Boolean = false) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_select_item, null)
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        builder.setView(view)
        val selectListDialog = builder.create()
        selectListDialog.setCancelable(true)

        val selectItemTitleText = view.findViewById<TextView>(R.id.selectItemTitleText)
        val itemContents = view.findViewById<LinearLayout>(R.id.itemContents)
        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val selectOption = view.findViewById<TextView>(R.id.selectOption)

        val selectItemTitleString =
            if(toLink) activity.getString(R.string.linkToListString)
            else activity.getString(R.string.moveToOtherListString)
        val selectOptionString =
            if(toLink) activity.getString(R.string.linkString)
            else activity.getString(R.string.moveString)
        selectItemTitleText.text = selectItemTitleString
        selectOption.text = selectOptionString

        val otherListIds = database.getListIds()
        otherListIds.remove(getList().getListId())
        val selectedItems = SparseBooleanArray()
        val colorTheme = activity.getColorTheme(getList().getColorThemeIndex())
        var aSectionIsSelected = false
        selectOption.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent))

        fun setSectionSelected() {
            aSectionIsSelected = true
            selectOption.setTextColor(Color.WHITE)
        }

        fun selectOption() {
            var selectedListId = 0
            for(id in otherListIds) {
                val itemSelected = selectedItems[id]
                if(itemSelected) {
                    selectedListId = id
                    break
                }
            }
            if(toLink) {
                val task = currentDialogTask
                if (task != null) {
                    addLinkOnDialog(selectedListId)
                    task.linkToList(selectedListId)
                }
            } else if(toMove) {
                val moveTasksPrompt = if(getSelectedCount() == 1)
                    activity.getString(R.string.moveTaskPrompt)
                else activity.getString(R.string.moveTasksPrompt)
                showConfirmDialog(moveTasksPrompt, activity.getString(R.string.moveString),
                    moveTasks = true, listId = selectedListId, forDialogTask = forDialogTask)
            }
            selectListDialog.cancel()
        }

        val otherLists = ArrayList<List>()
        for(id in otherListIds) otherLists.add(database.getList(id))
        val otherListsOrdered = sortListsByLastUpdated(otherLists)

        fun uncheckOtherItems(selectedId: Int) {
            for((i, list) in otherListsOrdered.withIndex()) {
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
            val itemView = activity.layoutInflater.inflate(
                R.layout.view_select_item_option, null)
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

        for(list in otherListsOrdered) {
            val listType = list.getListType()
            val listTitle = list.getTitle()
            addListView(list.getListId(), listTitle, listType)
        }

        val itemScrollView = view.findViewById<ScrollView>(R.id.itemScrollView)
        val dialogOptions = ArrayList<View>()
        for(itemView in itemContents.iterator())
            dialogOptions.add(itemView)
        initDialogScrollOptions(itemScrollView, itemContents, dialogOptions)

        val newListOption = view.findViewById<LinearLayout>(R.id.newListOption)
        val newListOptionIcon = view.findViewById<ImageView>(R.id.newListOptionIcon)
        val newListOptionText = view.findViewById<TextView>(R.id.newListOptionText)

        val primaryTheme = activity.getColorTheme()
        newListOptionIcon.setColoredImageResource(R.drawable.ic_add_custom, primaryTheme)
        newListOptionText.setTextColor(primaryTheme)
        newListOption.visibility = View.VISIBLE
        newListOption.setOnClickListener {
            showQuickCreateListDialog(selectListDialog, toLink, toMove, forDialogTask)
        }

        cancelOption.setOnClickListener {
            selectListDialog.cancel()
        }

        selectOption.setOnClickListener {
            if(aSectionIsSelected)
                selectOption()
        }

        selectListDialog.show()

        if(otherListIds.size == 0) newListOption.performClick()
    }



    private fun showQuickCreateListDialog(selectListToLinkDialog: AlertDialog,
                                          toLink: Boolean, toMove: Boolean,
                                          forDialogTask: Boolean) {
        val quickCreateListDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(
            R.layout.bottomsheet_quick_create_list, null)
        quickCreateListDialog.setContentView(view)

        if(toMove) view.findViewById<TextView>(R.id.quickCreateListTitleText).text =
            activity.getString(R.string.moveToNewListString)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.createToDoListOption))
        dialogOptions.add(view.findViewById(R.id.createProgressListOption))
        dialogOptions.add(view.findViewById(R.id.createRoutineListOption))
        dialogOptions.add(view.findViewById(R.id.createBulletedListOption))
        initDialogOptions(quickCreateListDialog, dialogOptions)

        fun clickOption(option: LinearLayout) {
            val listType = when(option) {
                dialogOptions[0] -> toDoListRef
                dialogOptions[1] -> progressListRef
                dialogOptions[2] -> routineListRef
                else ->             bulletedListRef
            }

            if(toLink) {
                val taskField = currentDialogView?.findViewById<EditText>(R.id.taskField)
                val taskString = taskField?.text?.toString() ?: ""
                val colorThemeIndex = getList().getColorThemeIndex()

                val newListId = database.quickCreateList(
                    listType, colorThemeIndex, taskString)

                val task = currentDialogTask
                if (task != null) {
                    addLinkOnDialog(newListId)
                    task.linkToList(newListId)
                }
            } else if(toMove) {
                val moveTasksPrompt = if(getSelectedCount() == 1)
                    activity.getString(R.string.moveTaskPrompt)
                else activity.getString(R.string.moveTasksPrompt)
                showConfirmDialog(moveTasksPrompt, activity.getString(R.string.moveString),
                    moveTasks = true, listType = listType, forDialogTask = forDialogTask)
            }

            selectListToLinkDialog.cancel()
            quickCreateListDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        quickCreateListDialog.show()
    }

    private fun getSelectedCount(): Int {
        return when(listType) {
            toDoListRef -> (activity as ToDoListActivity).getManager().getSelectedCount()
            progressListRef -> (activity as ProgressListActivity).getManager().getSelectedCount()
            routineListRef -> (activity as RoutineListActivity).getManager().getSelectedCount()
            else -> (activity as BulletedListActivity).getManager().getSelectedCount()
        }
    }

    fun checkIfLinkedListExists() {
        val task = currentDialogTask?: return
        if(!task.isLinkedToList()) return
        if(!database.listExists(task.getLinkedListId())) {
            task.removeLinkFromList()
            removeLinkFromDialog()
        } else addLinkOnDialog(task.getLinkedListId())
    }

    fun initRemoveOptionIcons() {
        val view = currentDialogView?: return
        val unlinkWebsiteIcon = view.findViewById<ImageView>(R.id.unlinkWebsiteIcon)
        unlinkWebsiteIcon.setOnClickListener {
            showConfirmDialog(activity.getString(R.string.unlinkWebsitePrompt),
                activity.getString(R.string.unlinkString), unlinkWebsite = true)
        }

        val removeDateIcon = view.findViewById<ImageView>(R.id.removeDateIcon)
        removeDateIcon.setOnClickListener {
            showConfirmDialog(activity.getString(R.string.removeDatePrompt),
                activity.getString(R.string.removeString), removeDate = true)
        }

        val removeTimeIcon = view.findViewById<ImageView>(R.id.removeTimeIcon)
        removeTimeIcon.setOnClickListener {
            showConfirmDialog(activity.getString(R.string.removeTimePrompt),
                activity.getString(R.string.removeString), removeTime = true)
        }

        val deleteNoteIcon = view.findViewById<ImageView>(R.id.deleteNoteIcon)
        deleteNoteIcon.setOnClickListener {
            showConfirmDialog(activity.getString(R.string.deleteNotePrompt),
                activity.getString(R.string.deleteString), deleteNote = true)
        }

        val removeRewardIcon = view.findViewById<ImageView>(R.id.removeRewardIcon)
        removeRewardIcon.setOnClickListener {
            showConfirmDialog(activity.getString(R.string.removeRewardPrompt),
                activity.getString(R.string.removeString), removeReward = true)
        }

        val unlinkListIcon = view.findViewById<ImageView>(R.id.unlinkListIcon)
        unlinkListIcon.setOnClickListener {
            showConfirmDialog(activity.getString(R.string.unlinkListPrompt),
                activity.getString(R.string.unlinkString), unlinkTask = true)
        }
    }

    fun removeTaskOptionsFromDialog() {
        val taskHasDueDate = currentDialogTask?.hasDueDate()?: false
        if(taskHasDueDate) removeDateFromDialog()
        val taskHasTime = currentDialogTask?.hasTime()?: false
        if(taskHasTime) removeTimeFromDialog()
        val taskNote = currentDialogTask?.getNote()?: ""
        if(taskNote.isNotEmpty()) removeNoteFromDialog()
        val taskReward = currentDialogTask?.getReward()?: ""
        if(taskReward.isNotEmpty()) removeRewardFromDialog()
        val taskIsLinked = currentDialogTask?.isLinkedToList()?: false
        if(taskIsLinked) removeLinkFromDialog()
        val taskHasWebsite = currentDialogTask?.hasWebsiteLink()?: false
        if(taskHasWebsite) removeWebsiteLinkFromDialog()
    }

    fun openCalendarDialog(supportFragmentManager: FragmentManager) {
        val taskHasDate = currentDialogTask?.hasDate()
        val taskIsRepeating = currentDialogTask?.isRepeating()
        if(taskHasDate == null || taskIsRepeating == null) return
        when {
            taskHasDate -> showDatePickerDialog(currentDialogTask?.getDate(),
                getList(), supportFragmentManager)
            taskIsRepeating -> showChooseRepeatingDaysDialog()
            else -> showChooseCalendarMethodDialog(supportFragmentManager)
        }
    }

    fun openTimeDialog(supportFragmentManager: FragmentManager) {
        val taskHasTime = currentDialogTask?.hasTime()?: false
        val currentTime = getTodaysDate()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)
        val hour = if(taskHasTime) currentDialogTask
            ?.getTimeHour()?: currentHour else currentHour
        val minute = if(taskHasTime) currentDialogTask
            ?.getTimeMinute()?: currentMinute else currentMinute
        showTimePickerDialog(hour, minute, getList(), supportFragmentManager)
    }

    private fun showTimePickerDialog(hour: Int, minute: Int, list: List,
                                     supportFragmentManager: FragmentManager) {
        val timePicker = TimePickerFragment(hour, minute, getDatePickerTheme(list))
        timePicker.show(supportFragmentManager, "time picker")
    }

    fun showChooseCalendarMethodDialog(supportFragmentManager: FragmentManager) {
        val chooseCalendarMethodDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(
            R.layout.bottomsheet_choose_calendar_method, null)
        chooseCalendarMethodDialog.setContentView(view)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.addDateOption))
        dialogOptions.add(view.findViewById(R.id.repeatOption))
        initDialogOptions(chooseCalendarMethodDialog, dialogOptions)

        fun clickOption(option: LinearLayout) {
            when(option) {
                dialogOptions[0] -> showDatePickerDialog(currentDialogTask?.getDate(),
                    getList(), supportFragmentManager)
                dialogOptions[1] -> showChooseRepeatingDaysDialog()
            }

            chooseCalendarMethodDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        chooseCalendarMethodDialog.show()
    }

    fun copyTaskToClipboard(taskString: String) {
        val toastMessage = activity.getString(
            R.string.taskCopiedToClipboardMessage,
            taskString.limitChar(MAX_TEXT_DISPLAY_LENGTH))
        activity.copyToClipboard(taskString, toastMessage)
    }

    fun openOrSetLink(addTaskDialog: BottomSheetDialog) {
        val currentTask = currentDialogTask?: return
        if(currentTask.isLinkedToList()) {
            openList(currentTask)
            if(currentTask.getTask().isNotEmpty())
                addTaskDialog.cancel()
            setTaskToOpen(currentTask)
        } else showSelectListDialog(toLink = true)
    }

    private fun openList(currentTask: Task) {
        when(listType) {
            toDoListRef -> (activity as ToDoListActivity)
                .openList(currentTask.getLinkedListId())
            progressListRef -> (activity as ProgressListActivity)
                .openList(currentTask.getLinkedListId())
            routineListRef -> (activity as RoutineListActivity)
                .openList(currentTask.getLinkedListId())
            bulletedListRef -> (activity as BulletedListActivity)
                .openList(currentTask.getLinkedListId())
        }
    }

    private fun setTaskToOpen(currentTask: Task) {
        when(listType) {
            toDoListRef -> (activity as ToDoListActivity)
                .getManager().setTaskToOpen(currentTask.getTaskId())
            progressListRef -> (activity as ProgressListActivity)
                .getManager().setTaskToOpen(currentTask.getTaskId())
            routineListRef -> (activity as RoutineListActivity)
                .getManager().setTaskToOpen(currentTask.getTaskId())
            bulletedListRef -> (activity as BulletedListActivity)
                .getManager().setTaskToOpen(currentTask.getTaskId())
        }
    }

    fun initTaskFieldListener(taskField: EditText, addButton: LinearLayout,
                              task: Task?, addButtonBackground: ImageView,
                              addButtonIcon: ImageView, colorTheme: Int,
                              optionsScrollView: HorizontalScrollView) {
        fun disableAddButton() {
            addButton.visibility = View.GONE
            val iconCode = if(task == null)
                R.drawable.ic_add_gray else R.drawable.ic_save_gray
            addButtonBackground.setImageResource(R.drawable.ic_circle_filled_gray)
            addButtonIcon.setImageResource(iconCode)
        }

        fun enableAddButton() {
            addButton.visibility = View.VISIBLE
            val iconCode = if(task == null)
                R.drawable.ic_add_black else R.drawable.ic_save_black
            addButtonBackground.setColoredImageResource(
                R.drawable.ic_circle_filled_custom, colorTheme)
            addButtonIcon.setImageResource(iconCode)
        }

        disableAddButton()
        taskField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                val taskString = taskField.text.toString()
                if(taskString.isEmpty())
                    disableAddButton()
                else enableAddButton()

                if(task == null) optionsScrollView.visibility =
                    if(taskString.contains("\n"))
                        View.GONE else View.VISIBLE

            }
        })
    }

    fun unlinkTask() {
        removeLinkFromDialog()
        currentDialogTask?.removeLinkFromList()
    }

    fun unlinkWebsite() {
        removeWebsiteLinkFromDialog()
        currentDialogTask?.removeWebsiteLink()
    }

    fun deleteNote() {
        removeNoteFromDialog()
        currentDialogTask?.setNote("")
    }

    fun removeReward() {
        removeRewardFromDialog()
        currentDialogTask?.setReward("")
    }

    fun removeDate() {
        removeDateFromDialog()
        currentDialogTask?.setDate(null)
        currentDialogTask?.clearRepeatingDays()
        removeTime()
    }

    fun removeTime() {
        removeTimeFromDialog()
        currentDialogTask?.removeTime()
    }

    fun archiveList() {
        getList().archive()
        database.archiveList(getList())
    }

    fun unarchiveList() {
        getList().unarchive()
        database.unarchiveList(getList())
    }

    fun deleteList() {
        database.deleteList(getList().getListId())
        activity.finish()
    }

    fun moveTasks(listId: Int, listType: String, forDialogTask: Boolean) {
        val selectedListId = if(listType.isNotEmpty())
            database.quickCreateList(listType,
                getList().getColorThemeIndex()) else listId
        initMoveTasksProcess(selectedListId, forDialogTask)
    }

    private fun initMoveTasksProcess(selectedListId: Int, forDialogTask: Boolean) {
        when(listType) {
            toDoListRef -> (activity as ToDoListActivity)
                .getManager().initMoveTasksProcess(selectedListId, forDialogTask)
            progressListRef -> (activity as ProgressListActivity)
                .getManager().initMoveTasksProcess(selectedListId, forDialogTask)
            routineListRef -> (activity as RoutineListActivity)
                .getManager().initMoveTasksProcess(selectedListId, forDialogTask)
            bulletedListRef -> (activity as BulletedListActivity)
                .getManager().initMoveTasksProcess(selectedListId, forDialogTask)
        }
    }

    private fun listIsUntitled(listTitle: String): Boolean {
        return when {
            listType == toDoListRef && listTitle == activity
                .getString(R.string.untitledToDoListString) -> true
            listType == progressListRef && listTitle == activity
                .getString(R.string.untitledProgressListString) -> true
            listType == routineListRef && listTitle == activity
                .getString(R.string.untitledRoutineListString) -> true
            listType == bulletedListRef && listTitle ==  activity
                .getString(R.string.untitledBulletedListString) -> true
            else -> false
        }
    }

    fun updateNameListDialogNoteState(nameListDialog: View) {
        val noteText = nameListDialog.findViewById<TextView>(R.id.noteText)
        val viewNoteButton = nameListDialog
            .findViewById<TextView>(R.id.viewNoteButton)
        val nameListDialogParent = nameListDialog
            .findViewById<LinearLayout>(R.id.nameListDialogParent)
        val list = getList()
        if(list.hasNote()) {
            noteText.visibility = View.GONE
            beginTransition(nameListDialogParent)
            noteText.visibility = View.VISIBLE
            noteText.text = list.getNote().removeNewLines()
            viewNoteButton.text = activity.getString(R.string.viewNoteString)
        } else {
            beginTransition(nameListDialogParent)
            noteText.visibility = View.GONE
            viewNoteButton.text = activity.getString(R.string.addNoteString)
        }
    }

    fun showListNoteDialog(nameListDialog: View, listTitleString: String) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_view_note, null)
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        builder.setView(view)
        val viewNoteDialog = builder.create()
        viewNoteDialog.setCancelable(true)

        val taskText = view.findViewById<TextView>(R.id.taskText)
        val noteField = view.findViewById<EditText>(R.id.noteField)
        val saveIcon = view.findViewById<ImageView>(R.id.saveIcon)

        if(listTitleString.isNotEmpty()) taskText.text = listTitleString
        else taskText.text = activity.getString(R.string.listNoteString)

        noteField.setText(getList().getNote())

        saveIcon.setOnClickListener { viewNoteDialog.cancel() }

        viewNoteDialog.setOnCancelListener {
            val noteString = noteField.text.toString()
            getList().setNote(noteString)
            displayNote()
            updateList()
            updateNameListDialogNoteState(nameListDialog)
            viewNoteDialog.dismiss()
        }

        viewNoteDialog.show()
    }

    private fun displayNote() {
        when(listType) {
            toDoListRef -> (activity as ToDoListActivity).getView().displayNote()
            progressListRef -> (activity as ProgressListActivity).getView().displayNote()
            routineListRef -> (activity as RoutineListActivity).getView().displayNote()
            bulletedListRef -> (activity as BulletedListActivity).getView().displayNote()
        }
    }

    fun showNameListDialog(listExists: Boolean = true) {
        val currentColorThemeIndex = getList().getColorThemeIndex()

        val view = activity.layoutInflater.inflate(R.layout.dialog_name_list, null)
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        builder.setView(view)
        val nameToDoListDialog = builder.create()
        nameToDoListDialog.setCancelable(true)

        val createListText = view.findViewById<TextView>(R.id.createListText)
        val listTitleField = view.findViewById<EditText>(R.id.listTitleField)
        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val saveOption = view.findViewById<TextView>(R.id.saveOption)

        val manageLabelsButton = view.findViewById<TextView>(R.id.manageLabelsButton)
        val labelText = view.findViewById<TextView>(R.id.labelText)

        manageLabelsButton.setOnClickListener {
            showManageLabelsDialog(view)
        }

        val viewNoteButton = view.findViewById<TextView>(R.id.viewNoteButton)

        viewNoteButton.setOnClickListener {
            showListNoteDialog(view, listTitleField.text.toString())
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

            // Change Activity View Theme
            changeTheme(index)
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

        if(listExists) {
            var listTitle = getList().getTitle()
            if(listIsUntitled(listTitle)) listTitle = ""
            listTitleField.setText(listTitle)
            val renameListString = when(listType) {
                toDoListRef -> activity.getString(R.string.renameToDoListString)
                progressListRef -> activity.getString(R.string.renameProgressListString)
                routineListRef -> activity.getString(R.string.renameRoutineListString)
                else -> activity.getString(R.string.renameBulletedListString)
            }
            createListText.text = renameListString
            saveOption.text = activity.getString(R.string.saveString)
            if(currentColorThemeIndex != 0)
                clickOption(colorOptions[currentColorThemeIndex])
            if(getList().hasNote()) updateNameListDialogNoteState(view)
            if(getList().hasLabels()) {
                labelText.visibility = View.VISIBLE
                labelText.text = getList().getLabelsString()
                manageLabelsButton.text = activity.getString(R.string.manageLabelsString)
            }

            val detailsOption = view.findViewById<LinearLayout>(R.id.detailsOption)
            detailsOption.visibility = View.VISIBLE
            detailsOption.setOnClickListener {
                when(listType) {
                    toDoListRef -> activity.openToDoListDetailsDialog(
                        database.getToDoList(getList().getListId()), listTitleField)
                    progressListRef -> activity.openProgressListDetailsDialog(
                        database.getProgressList(getList().getListId()), listTitleField)
                    bulletedListRef -> activity.openBulletedListDetailsDialog(
                        database.getBulletedList(getList().getListId()), listTitleField)
                }
            }

        } else {
            val createListString = when(listType) {
                toDoListRef -> activity.getString(R.string.createToDoListString)
                progressListRef -> activity.getString(R.string.createProgressListString)
                routineListRef -> activity.getString(R.string.createRoutineListString)
                else -> activity.getString(R.string.createBulletedListString)
            }
            createListText.text = createListString
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
                setTitle(listTitle)
                getList().setTitle(listTitle)
                if (!listExists) saveList()
                else updateList()
            }
        }

        var saveClicked = false

        saveOption.setOnClickListener {
            val listTitle = listTitleField.text.toString()
            if(listTitle.isNotEmpty()) {
                saveClicked = true
                nameToDoListDialog.cancel()
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
                    changeTheme(currentColorThemeIndex)
            }
        }

        cancelOption.setOnClickListener {
            cancelClicked = true
            cancelDialog()
            nameToDoListDialog.cancel()
        }

        nameToDoListDialog.setOnCancelListener {
            nameToDoListDialog.dismiss()
            if(!listExists) {
                if(saveClicked) saveOption()
                else activity.finish()
            }
            else if(!cancelClicked) saveOption()
        }

        listTitleField.requestFocus()
        nameToDoListDialog.show()
        showKeyboard(dialog = nameToDoListDialog)
    }

    private fun saveList() {
        when(listType) {
            toDoListRef ->  database.saveToDoList(getList() as ToDoList)
            progressListRef -> database.saveProgressList(getList() as ProgressList)
            routineListRef -> database.saveRoutineList(getList() as RoutineList)
            bulletedListRef -> database.saveBulletedList(getList() as BulletedList)
        }
    }

    private fun setTitle(listTitle: String) {
        when(listType) {
            toDoListRef -> (activity as ToDoListActivity).getView().setTitle(listTitle)
            progressListRef -> (activity as ProgressListActivity).getView().setTitle(listTitle)
            routineListRef -> (activity as RoutineListActivity).getView().setTitle(listTitle)
            bulletedListRef -> (activity as BulletedListActivity).getView().setTitle(listTitle)
        }
    }

    private fun changeTheme(currentColorThemeIndex: Int) {
        when(listType) {
            toDoListRef -> (activity as ToDoListActivity)
                .getView().changeTheme(currentColorThemeIndex)
            progressListRef -> (activity as ProgressListActivity)
                .getView().changeTheme(currentColorThemeIndex)
            routineListRef -> (activity as RoutineListActivity)
                .getView().changeTheme(currentColorThemeIndex)
            bulletedListRef -> (activity as BulletedListActivity)
                .getView().changeTheme(currentColorThemeIndex)
        }
    }

    private fun showConfirmDialog(confirmMessage: String, confirmString: String,
                                  deleteNote: Boolean = false, removeDate: Boolean = false,
                                  deleteTasks: Boolean = false, unlinkTask: Boolean = false,
                                  moveTasks: Boolean = false, listId: Int = SENTINEL,
                                  listType: String = "", removeReward: Boolean = false,
                                  removeTime: Boolean = false, unlinkWebsite: Boolean = false,
                                  forDialogTask: Boolean = false) {
        when(this.listType) {
            toDoListRef -> (activity as ToDoListActivity).getDialogs()
                .showConfirmDialog(confirmMessage, confirmString,
                    deleteNote = deleteNote, removeDate = removeDate,
                    deleteTasks = deleteTasks, unlinkTask = unlinkTask,
                    moveTasks = moveTasks, listId = listId,
                    listType = listType, removeReward = removeReward,
                    removeTime = removeTime, unlinkWebsite = unlinkWebsite,
                    forDialogTask = forDialogTask)
            progressListRef -> (activity as ProgressListActivity).getDialogs()
                .showConfirmDialog(confirmMessage, confirmString,
                    deleteNote = deleteNote, removeDate = removeDate,
                    deleteTasks = deleteTasks, unlinkTask = unlinkTask,
                    moveTasks = moveTasks, listId = listId,
                    listType = listType, removeReward = removeReward,
                    removeTime = removeTime, unlinkWebsite = unlinkWebsite,
                    forDialogTask = forDialogTask)
            routineListRef -> (activity as RoutineListActivity).getDialogs()
                .showConfirmDialog(confirmMessage, confirmString,
                    deleteNote = deleteNote, deleteTasks = deleteTasks,
                    unlinkTask = unlinkTask, moveTasks = moveTasks,
                    listId = listId, listType = listType,
                    removeTime = removeTime, unlinkWebsite = unlinkWebsite,
                    forDialogTask = forDialogTask)
            bulletedListRef -> (activity as BulletedListActivity).getDialogs()
                .showConfirmDialog(confirmMessage, confirmString,
                    deleteNote = deleteNote, removeDate = removeDate,
                    deleteTasks = deleteTasks, unlinkTask = unlinkTask,
                    moveTasks = moveTasks, listId = listId, listType = listType,
                    removeTime = removeTime, unlinkWebsite = unlinkWebsite,
                    forDialogTask = forDialogTask)
        }
    }
}