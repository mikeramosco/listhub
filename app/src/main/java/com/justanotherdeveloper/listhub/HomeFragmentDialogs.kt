package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("InflateParams")
class HomeFragmentDialogs(private val fragment: HomeFragment,
                          private val activity: HomeActivity) {

    private var dialogParent: LinearLayout? = null
    private var startDateText: TextView? = null
    private var endDateText: TextView? = null
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    private var startDateClicked = true

    private fun showDatePickerDialog(setDate: Calendar, minDate: Calendar?, maxDate: Calendar?) {
        val datePicker = DatePickerFragment(setDate, minDate,
            maxDate, R.style.DatePickerDialogColorTheme1)
        datePicker.show(activity.supportFragmentManager, "date picker")
    }

    fun showSelectHomeSectionDialog() {
        val selectHomeSectionDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_select_home_section, null)
        selectHomeSectionDialog.setContentView(view)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.calendarOption))
        dialogOptions.add(view.findViewById(R.id.importantOption))
        dialogOptions.add(view.findViewById(R.id.recentlyAddedOption))
        dialogOptions.add(view.findViewById(R.id.rewardsOption))
        dialogOptions.add(view.findViewById(R.id.completedOption))
        initDialogOptions(selectHomeSectionDialog, dialogOptions)

        fun clickOption(option: LinearLayout) {
            for((index, dialogOption) in dialogOptions.withIndex())
                if(option == dialogOption) fragment.getManager().setCurrentSectionIndex(index)

            beginTransition(fragment.homeFragmentParent)
            fragment.getManager().loadSection()
            selectHomeSectionDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        selectHomeSectionDialog.show()
    }

    fun showSetDateRangeDialog() {
        val setDateRangeDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_set_date_range, null)
        setDateRangeDialog.setContentView(view)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.addedTodayOption))         // 0
        dialogOptions.add(view.findViewById(R.id.addedYesterdayOption))     // 1
        dialogOptions.add(view.findViewById(R.id.addedWithinDaysOption))    // 2
        dialogOptions.add(view.findViewById(R.id.addedWithinDaysOption2))   // 3
        dialogOptions.add(view.findViewById(R.id.customDateRangeOption))    // 4
        dialogOptions.add(view.findViewById(R.id.allTimeOption))            // 5
        initDialogOptions(setDateRangeDialog, dialogOptions)

        val currentSectionIndex = fragment.getManager().getCurrentSectionIndex()

        val addedTodayText = view.findViewById<TextView>(R.id.addedTodayText)
        val addedYesterdayText = view.findViewById<TextView>(R.id.addedYesterdayText)
        val addedWithinDaysText = view.findViewById<TextView>(R.id.addedWithinDaysText)
        val addedWithinWeeksText = view.findViewById<TextView>(R.id.addedWithinWeeksText)
        val customDateRangeText = view.findViewById<TextView>(R.id.customDateRangeText)
        val anyDateText = view.findViewById<TextView>(R.id.anyDateText)

        val nDays = "7"
//        val nWeeks = "4"

        when (currentSectionIndex) {
            calendarIndex -> {
                dialogOptions[3].visibility = View.VISIBLE
                addedTodayText.text = activity.getString(R.string.setTodayString)
                addedYesterdayText.text = activity.getString(R.string.setTomorrowString)
                addedWithinDaysText.text = activity.getString(R.string.nextXDaysString, nDays)
                addedWithinWeeksText.text = activity.getString(R.string.lastXDaysString, nDays)
                customDateRangeText.text = activity.getString(R.string.setWithinCustomDateRangeString)
                anyDateText.text = activity.getString(R.string.setAnyDateString)
            }
            completedIndex -> {
                addedTodayText.text = activity.getString(R.string.completedTodayString)
                addedYesterdayText.text = activity.getString(R.string.completedYesterdayString)
                addedWithinDaysText.text = activity.getString(R.string.completedLastXDaysString, nDays)
//                addedWithinWeeksText.text = activity.getString(R.string.completedLastXWeeksString, nWeeks)
                customDateRangeText.text = activity.getString(R.string.completedWithinCustomDateRangeString)
                anyDateText.text = activity.getString(R.string.completedAnyDateString)
            }
            else -> {
                addedWithinDaysText.text = activity.getString(R.string.addedLastXDaysString, nDays)
//                addedWithinWeeksText.text = activity.getString(R.string.addedLastXWeeksString, nWeeks)
            }
        }

        fun setDateRange(daysBetween: Int, forNextDays: Boolean = false) {
            val setDate = getTodaysDate()
//            val calendarSectionShown = calendarSectionShown()
            val amountToAdd = if(forNextDays) daysBetween else -daysBetween
            setDate.add(Calendar.DATE, amountToAdd)
            if(forNextDays)
                fragment.getManager().setDateRange(getTodaysDate(), setDate)
            else fragment.getManager().setDateRange(setDate, getTodaysDate())
        }

        fun clickOption(option: LinearLayout) {
            var closeDialog = true

            when(option) {
                dialogOptions[0] -> fragment.getManager()
                    .setDateRange(getTodaysDate(), getTodaysDate())
                dialogOptions[1] -> if(calendarSectionShown())
                    fragment.getManager().setDateRange(
                        getTomorrowsDate(), getTomorrowsDate()) else
                    fragment.getManager().setDateRange(
                        getYesterdaysDate(), getYesterdaysDate())
                dialogOptions[2] -> setDateRange(7, calendarSectionShown())
//                dialogOptions[3] -> setDateRange(28)
                dialogOptions[3] -> setDateRange(7)
                dialogOptions[4] -> {
                    closeDialog = false
                    showCustomDateRangeDialog(setDateRangeDialog)
                }
                dialogOptions[5] -> fragment.getManager()
                    .setDateRange(null, null)
            }

            if(closeDialog) setDateRangeDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        setDateRangeDialog.show()
    }

    fun setDateOnDialog(date: Calendar) {
        val dialogParent = this.dialogParent?: return
        beginTransition(dialogParent)
        if(startDateClicked) {
            startDate = date
            startDateText?.text = activity.getDateText(date)
        } else {
            endDate = date
            endDateText?.text = activity.getDateText(date)
        }
    }

    private fun calendarSectionShown(): Boolean {
        return fragment.getManager().getCurrentSectionIndex() == calendarIndex
    }

    private fun showCustomDateRangeDialog(setDateRangeDialog: BottomSheetDialog) {
        val currentDateRange = fragment.getManager().getDateRange()

        val startDate = currentDateRange.getStartDate()?: getTodaysDate()
        val endDate = currentDateRange.getEndDate()?: getTodaysDate()

        val view = activity.layoutInflater.inflate(R.layout.dialog_set_date_range, null)
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        builder.setView(view)
        val customDateRangeDialog = builder.create()
        customDateRangeDialog.setCancelable(true)

        val dialogParent = view.findViewById<LinearLayout>(R.id.dialogParent)

        val startDateLayout = view.findViewById<LinearLayout>(R.id.startDateLayout)
        val endDateLayout = view.findViewById<LinearLayout>(R.id.endDateLayout)
        val endDateOptions = view.findViewById<LinearLayout>(R.id.endDateOptions)
        val singleDateOnlyLayout = view.findViewById<LinearLayout>(R.id.singleDateOnlyLayout)

        val startDateLabel = view.findViewById<TextView>(R.id.startDateLabel)
        val startDateText = view.findViewById<TextView>(R.id.startDateText)
        val endDateText = view.findViewById<TextView>(R.id.endDateText)
        val singleDateOnlyCheckbox = view.findViewById<ImageView>(R.id.singleDateOnlyCheckbox)

        var singleDateOnly = false

        this.dialogParent = dialogParent
        this.startDateText = startDateText
        this.endDateText = endDateText
        this.startDate = copyDate(startDate)
        this.endDate = copyDate(endDate)

        startDateText.text = activity.getDateText(startDate)
        endDateText.text = activity.getDateText(endDate)

        val maxDate = if(calendarSectionShown())
            null else getTodaysDate()

        fun editStartDateClicked() {
            startDateClicked = true
            val setDate = this.startDate?: getTodaysDate()
            val maxStartDate = if(singleDateOnly)
                maxDate else this.endDate?: getTodaysDate()
            showDatePickerDialog(setDate, null, maxStartDate)
        }

        fun editEndDateClicked() {
            startDateClicked = false
            val setDate = this.endDate?: getTodaysDate()
            val minDate = this.startDate?: getTodaysDate()
            showDatePickerDialog(setDate, minDate, maxDate)
        }

        startDateLayout.setOnClickListener {
            editStartDateClicked()
        }

        endDateLayout.setOnClickListener {
            editEndDateClicked()
        }

        fun singleDateOnlyClicked() {
            singleDateOnly = !singleDateOnly
            if(singleDateOnly) {
                singleDateOnlyCheckbox.setColoredImageResource(
                    R.drawable.ic_check_box_custom, activity.getColorTheme())
                endDateLayout.visibility = View.GONE
                endDateOptions.visibility = View.GONE
                startDateLabel.text = activity.getString(R.string.singleDateLabel)
            } else {
                singleDateOnlyCheckbox.setImageResource(
                    R.drawable.ic_check_box_outline_blank_gray)
                endDateLayout.visibility = View.VISIBLE
                endDateOptions.visibility = View.VISIBLE
                startDateLabel.text = activity.getString(R.string.startDateLabel)

                val currentStartDate = this.startDate
                val currentEndDate = this.endDate
                if(currentStartDate != null && currentEndDate != null &&
                    currentStartDate.comesAfter(currentEndDate)) {
                    this.endDate = copyDate(currentStartDate)
                    endDateText?.text = activity.getDateText(currentStartDate)
                }
            }
        }

        singleDateOnlyLayout.setOnClickListener {
            beginTransition(dialogParent)
            singleDateOnlyClicked()
        }

        if(currentDateRange.isSingleDate())
            singleDateOnlyClicked()

        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val saveOption = view.findViewById<TextView>(R.id.saveOption)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.editStartDateButton))
        dialogOptions.add(view.findViewById(R.id.previousStartDateButton))
        dialogOptions.add(view.findViewById(R.id.nextStartDateButton))
        dialogOptions.add(view.findViewById(R.id.editEndDateButton))
        dialogOptions.add(view.findViewById(R.id.previousEndDateButton))
        dialogOptions.add(view.findViewById(R.id.nextEndDateButton))
        initDialogOptions(dialogOptions = dialogOptions)

        fun moveDateByOne(moveStartDate: Boolean, moveToNext: Boolean) {
            val amountToAdd = if(moveToNext) 1 else -1
            if(moveStartDate) {
                val setDate = this.startDate?: getTodaysDate()
                val maxStartDate = if(singleDateOnly)
                    maxDate else this.endDate?: getTodaysDate()
                val testDate = copyDate(setDate)
                testDate.add(Calendar.DATE, amountToAdd)
                if(testDate.isWithinDates(null, maxStartDate)) {
                    beginTransition(dialogParent)
                    this.startDate = testDate
                    startDateText?.text = activity.getDateText(testDate)
                }
            } else {
                val setDate = this.endDate?: getTodaysDate()
                val minDate = this.startDate?: getTodaysDate()
                val testDate = copyDate(setDate)
                testDate.add(Calendar.DATE, amountToAdd)
                if(testDate.isWithinDates(minDate, maxDate)) {
                    beginTransition(dialogParent)
                    this.endDate = testDate
                    endDateText?.text = activity.getDateText(testDate)
                }
            }
        }

        fun clickOption(option: LinearLayout) {
            when(option) {
                dialogOptions[0] -> editStartDateClicked()
                dialogOptions[1] -> moveDateByOne(moveStartDate = true, moveToNext = false)
                dialogOptions[2] -> moveDateByOne(moveStartDate = true, moveToNext = true)
                dialogOptions[3] -> editEndDateClicked()
                dialogOptions[4] -> moveDateByOne(moveStartDate = false, moveToNext = false)
                dialogOptions[5] -> moveDateByOne(moveStartDate = false, moveToNext = true)
            }
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        cancelOption.setOnClickListener {
            customDateRangeDialog.cancel()
        }

        saveOption.setOnClickListener {
            val dateRangeStartDate = this.startDate?: getTodaysDate()
            val dateRangeEndDate = if(singleDateOnly)
                this.startDate?: getTodaysDate() else this.endDate?: getTodaysDate()
            fragment.getManager().setDateRange(
                copyDate(dateRangeStartDate),
                copyDate(dateRangeEndDate))
            customDateRangeDialog.cancel()
            setDateRangeDialog.cancel()
        }

        customDateRangeDialog.setOnCancelListener {
            this.dialogParent = null
            this.startDateText = null
            this.endDateText = null
            this.startDate = null
            this.endDate = null
        }

        customDateRangeDialog.show()
    }

    fun showFilterSearchDialog() {
        if(fragment.getManager().getCurrentSectionIndex() == rewardsIndex) return
        val view = activity.layoutInflater.inflate(R.layout.dialog_filter_home_section, null)
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        builder.setView(view)
        val filterListsDialog = builder.create()
        filterListsDialog.setCancelable(true)

        val colorTheme = activity.getColorTheme()

        val listTypesHeader = view.findViewById<LinearLayout>(R.id.listTypesHeader)

        val toDoListsLayout = view.findViewById<LinearLayout>(R.id.toDoListsLayout)
        val progressListsLayout = view.findViewById<LinearLayout>(R.id.progressListsLayout)
        val routineListsLayout = view.findViewById<LinearLayout>(R.id.routineListsLayout)
        val bulletedListsLayout = view.findViewById<LinearLayout>(R.id.bulletedListsLayout)

        val toDoListsCheckbox = view.findViewById<ImageView>(R.id.toDoListsCheckbox)
        val progressListsCheckbox = view.findViewById<ImageView>(R.id.progressListsCheckbox)
        val routineListsCheckbox = view.findViewById<ImageView>(R.id.routineListsCheckbox)
        val bulletedListsCheckbox = view.findViewById<ImageView>(R.id.bulletedListsCheckbox)

        val toDoListTasksLayout = view.findViewById<LinearLayout>(R.id.toDoListTasksLayout)
        val progressListTasksLayout = view.findViewById<LinearLayout>(R.id.progressListTasksLayout)
        val routineStepsLayout = view.findViewById<LinearLayout>(R.id.routineStepsLayout)
        val bulletpointsLayout = view.findViewById<LinearLayout>(R.id.bulletpointsLayout)

        val toDoListTasksCheckbox = view.findViewById<ImageView>(R.id.toDoListTasksCheckbox)
        val progressListTasksCheckbox = view.findViewById<ImageView>(R.id.progressListTasksCheckbox)
        val routineStepsCheckbox = view.findViewById<ImageView>(R.id.routineStepsCheckbox)
        val bulletpointsCheckbox = view.findViewById<ImageView>(R.id.bulletpointsCheckbox)

        when(fragment.getManager().getCurrentSectionIndex()) {
            calendarIndex -> {
                toDoListsLayout.visibility = View.GONE
                progressListsLayout.visibility = View.GONE
                bulletedListsLayout.visibility = View.GONE
                routineStepsLayout.visibility = View.GONE
            }
            rewardsIndex -> {
                listTypesHeader.visibility = View.GONE
                toDoListsLayout.visibility = View.GONE
                progressListsLayout.visibility = View.GONE
                routineListsLayout.visibility = View.GONE
                bulletedListsLayout.visibility = View.GONE
                routineStepsLayout.visibility = View.GONE
                bulletpointsLayout.visibility = View.GONE
            }
            completedIndex -> {
                listTypesHeader.visibility = View.GONE
                toDoListsLayout.visibility = View.GONE
                progressListsLayout.visibility = View.GONE
                routineListsLayout.visibility = View.GONE
                bulletedListsLayout.visibility = View.GONE
                bulletpointsLayout.visibility = View.GONE
            }
        }

        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val applyOption = view.findViewById<TextView>(R.id.applyOption)

        val originalOptions = fragment.getManager().getSectionFilter()
        val checkedFilterOptions = ArrayList<Boolean>()
        for(option in originalOptions) checkedFilterOptions.add(false)

        fun toggleCheckbox(optionIndex: Int, checkbox: ImageView) {
            checkedFilterOptions[optionIndex] = !checkedFilterOptions[optionIndex]
            val isChecked = checkedFilterOptions[optionIndex]
            if(isChecked) checkbox.setColoredImageResource(
                R.drawable.ic_check_box_custom, colorTheme)
            else checkbox.setImageResource(
                R.drawable.ic_check_box_outline_blank_gray)
        }

        val itemScrollView = view.findViewById<ScrollView>(R.id.itemScrollView)
        val filterOptionsContainer = view.findViewById<LinearLayout>(R.id.filterOptionsContainer)

        val options = ArrayList<View>()
        options.add(toDoListsLayout)
        options.add(progressListsLayout)
        options.add(routineListsLayout)
        options.add(bulletedListsLayout)
        options.add(toDoListTasksLayout)
        options.add(progressListTasksLayout)
        options.add(routineStepsLayout)
        options.add(bulletpointsLayout)
        initDialogScrollOptions(itemScrollView, filterOptionsContainer, options)

        options[0].setOnClickListener {
            toggleCheckbox(0, toDoListsCheckbox) }
        options[1].setOnClickListener {
            options[0].performClick()
            toggleCheckbox(1, progressListsCheckbox) }
        options[2].setOnClickListener {
            toggleCheckbox(2, routineListsCheckbox) }
        options[3].setOnClickListener {
            toggleCheckbox(3, bulletedListsCheckbox) }
        options[4].setOnClickListener {
            toggleCheckbox(4, toDoListTasksCheckbox) }
        options[5].setOnClickListener {
            options[4].performClick()
            toggleCheckbox(5, progressListTasksCheckbox) }
        options[6].setOnClickListener {
            toggleCheckbox(6, routineStepsCheckbox) }
        options[7].setOnClickListener {
            toggleCheckbox(7, bulletpointsCheckbox) }

        for((i, option) in originalOptions.withIndex())
            if(option && i != 0 && i != 4) options[i].performClick()

        var cancelClicked = false
        view.findViewById<TextView>(R.id.removeFiltersOption).setOnClickListener {
            val removedFilters = ArrayList<Boolean>()
            for(i in 0 until 8) removedFilters.add(false)
            fragment.getManager().setSectionFilter(removedFilters)
            cancelClicked = true
            filterListsDialog.cancel()
        }

        cancelOption.setOnClickListener {
            cancelClicked = true
            filterListsDialog.cancel()
        }

        applyOption.setOnClickListener {
            filterListsDialog.cancel()
        }

        filterListsDialog.setOnCancelListener {
            if(!cancelClicked) fragment.getManager()
                .setSectionFilter(checkedFilterOptions)
        }

        filterListsDialog.show()
    }
}