package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.transition.TransitionManager
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.abs

fun Activity.getColorTheme(index: Int = 0): Int {
    val colorCode = when(index) {
        0 -> R.color.colorTheme1
        1 -> R.color.colorTheme2
        2 -> R.color.colorTheme3
        3 -> R.color.colorTheme4
        4 -> R.color.colorTheme5
        else -> R.color.colorTheme6
    }
    return ContextCompat.getColor(this, colorCode)
}

fun getDatePickerTheme(list: List): Int {
    return when(list.getColorThemeIndex()) {
        0 -> R.style.DatePickerDialogColorTheme1
        1 -> R.style.DatePickerDialogColorTheme2
        2 -> R.style.DatePickerDialogColorTheme3
        3 -> R.style.DatePickerDialogColorTheme4
        4 -> R.style.DatePickerDialogColorTheme5
        else -> R.style.DatePickerDialogColorTheme6
    }
}

fun Activity.copyToClipboard(textToCopy: String, toastMessage: String = "") {
    val clipboard = getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("note", textToCopy)
    clipboard.setPrimaryClip(clip)
    displayToast(toastMessage)
}

fun beginTransition(layout: LinearLayout) {
    TransitionManager.beginDelayedTransition(layout)
}

fun getTodaysDate(): Calendar {
//    return Calendar.getInstance(TimeZone.getDefault()).resetTimeOfDay()
    return Calendar.getInstance(TimeZone.getDefault())
}

fun getYesterdaysDate(): Calendar {
    val yesterdaysDate = getTodaysDate()
    yesterdaysDate.add(Calendar.DATE, -1)
    return yesterdaysDate
}

fun getTomorrowsDate(): Calendar {
    val tomorrowsDate = getTodaysDate()
    tomorrowsDate.add(Calendar.DATE, 1)
    return tomorrowsDate
}

fun datesAreTheSame(date1: Calendar, date2: Calendar): Boolean {
    return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
            date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
            date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH)
}

fun isYesterday(date: Calendar): Boolean {
    return datesAreTheSame(date, getYesterdaysDate())
}

fun isTomorrow(date: Calendar): Boolean {
    return datesAreTheSame(date, getTomorrowsDate())
}

fun copyDate(date: Calendar): Calendar {
    val year = date.get(Calendar.YEAR)
    val month = date.get(Calendar.MONTH)
    val day = date.get(Calendar.DAY_OF_MONTH)
    return createCalendar(year, month, day)
}

fun createCalendar(year: Int, month: Int, day: Int): Calendar {
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.DAY_OF_MONTH, day)
    return calendar.resetTimeOfDay()
}

fun copyIntList(intList: ArrayList<Int>): ArrayList<Int> {
    val intListCopy = ArrayList<Int>()
    for(int in intList)
        intListCopy.add(int)
    return intListCopy
}

fun isTaskKey(keyString: String): Boolean {
    return keyString.contains("\t")
}

fun String.isFormattedAsWebsite(context: Context): Boolean {
    val httpsString = context.getString(R.string.httpsString)
    val httpString = context.getString(R.string.httpString)

    return startsWith(httpsString, true) ||
            startsWith(httpString, true)
}

fun String.formatAsWebsiteLink(context: Context): String {
    return if(isFormattedAsWebsite(context)) this
    else "${context.getString(R.string.httpsString)}$this"
}

fun getTaskKey(task: Task): String {
    val listId = task.getListId()
    val taskId = task.getTaskId()
    return "$listId\t$taskId"
}

fun String.limitChar(maxChar: Int): String {
    return if(length > maxChar)
        substring(0, maxChar) + "..." else this
}

fun getTimeText(hour: Int, minute: Int): String {
    val cal = getTodaysDate()
    cal.set(Calendar.HOUR_OF_DAY, hour)
    cal.set(Calendar.MINUTE, minute)
    return getTimeText(cal)
}

@SuppressLint("SimpleDateFormat")
fun getTimeText(cal: Calendar): String {
    val date = cal.time
    val sdf = SimpleDateFormat("hh:mm aa")
    var timeText = sdf.format(date)
    if(timeText[0] == '0')
        timeText = timeText.substring(1, timeText.length)
    return timeText
}

fun Activity.getDateText(date: Calendar,
                         forSetDate: Boolean = false,
                         forAddedDate: Boolean = false,
                         forCompletedDate: Boolean = false): String {
    return when {
        datesAreTheSame(getTodaysDate(), date) -> when {
            forSetDate -> getString(R.string.setTodayString)
            forAddedDate -> getString(R.string.addedTodayString)
            forCompletedDate -> getString(R.string.completedTodayString)
            else -> getString(R.string.todayString)
        }
        isTomorrow(date) -> if(forSetDate)
            getString(R.string.setTomorrowString) else
            getString(R.string.tomorrowString)
        isYesterday(date) -> when {
            forSetDate -> getString(R.string.setYesterdayString)
            forAddedDate -> getString(R.string.addedYesterdayString)
            forCompletedDate -> getString(R.string.completedYesterdayString)
            else -> getString(R.string.yesterdayString)
        }
        else -> when {
            forSetDate -> getString(R.string.setForXString, date.toDateString())
            forAddedDate -> getString(R.string.addedOnXString, date.toDateString())
            forCompletedDate -> getString(R.string.completedOnXString, date.toDateString())
            else -> date.toDateString()
        }
    }
}

fun Activity.getRecencyText(date: Calendar): String {
    val todaysDate = getTodaysDate()
    return when {
        datesAreTheSame(todaysDate, date) -> getString(R.string.todayString)
        isTomorrow(date) -> getString(R.string.tomorrowString)
        isYesterday(date) -> getString(R.string.yesterdayString)
        todaysDate.comesAfter(date) -> getXDaysAgoText(todaysDate, date)
        else -> getInXDaysText(todaysDate, date)
    }
}

private fun Activity.getXDaysAgoText(todaysDate: Calendar, date: Calendar): String {
    val daysBetween = daysBetween(todaysDate, date)
    return when {
        daysBetween < 14 -> getString(R.string.xDaysAgoString,
            daysBetween.toString())
        daysBetween < 60 -> getString(R.string.xWeeksAgoString,
            getDaysInWeeks(daysBetween).toString())
        daysBetween < 730 -> getString(R.string.xMonthsAgoString,
            getDaysInMonths(daysBetween).toString())
        else -> getString(R.string.xYearsAgoString,
            getDaysInYears(daysBetween).toString())
    }
}

private fun getDaysInWeeks(nDays: Int): Int {
    return (nDays.toDouble() / 7.0).toInt()
}

private fun getDaysInMonths(nDays: Int): Int {
    return (nDays.toDouble() / 30.0).toInt()
}

private fun getDaysInYears(nDays: Int): Int {
    return (nDays.toDouble() / 365.0).toInt()
}

private fun Activity.getInXDaysText(todaysDate: Calendar, date: Calendar): String {
    val daysBetween = daysBetween(todaysDate, date)
    return when {
        daysBetween < 14 -> getString(R.string.inXDaysString,
            daysBetween.toString())
        daysBetween < 60 -> getString(R.string.inXWeeksString,
            getDaysInWeeks(daysBetween).toString())
        daysBetween < 730 -> getString(R.string.inXMonthsString,
            getDaysInMonths(daysBetween).toString())
        else -> getString(R.string.inXYearsString,
            getDaysInYears(daysBetween).toString())
    }
}

fun daysBetween(startDate: Calendar, endDate: Calendar): Int {
    startDate.resetTimeOfDay()
    endDate.resetTimeOfDay()
    val end = endDate.timeInMillis
    val start = startDate.timeInMillis
    return TimeUnit.MILLISECONDS.toDays(abs(end - start)).toInt()
}

fun Calendar.resetTimeOfDay(): Calendar {
    set(Calendar.MILLISECOND, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.HOUR_OF_DAY, 0)
    return this
}

fun Calendar.toDateString(): String {
    val year = get(Calendar.YEAR)
    val month = get(Calendar.MONTH) + 1
    val day = get(Calendar.DAY_OF_MONTH)
    return "$month/$day/$year"
}

fun Calendar.isWithinDates(dateRange: DateRange): Boolean{
    val startDate = dateRange.getStartDate()
    val endDate = dateRange.getEndDate()
    if(startDate == null || endDate == null) return true
    return isWithinDates(startDate, endDate)
}

fun Calendar.isWithinDates(startDate: Calendar?, endDate: Calendar?): Boolean {
    if(startDate == null || endDate == null) {
        if(endDate != null) {
            if (datesAreTheSame(this, endDate)) return true
            return this.comesBefore(endDate)
        }
        if(startDate != null) {
            if (datesAreTheSame(this, startDate)) return true
            return this.comesAfter(startDate)
        }
        return true
    } else {
        if (datesAreTheSame(this, startDate) || datesAreTheSame(this, endDate)) return true
        return this.comesAfter(startDate) && this.comesBefore(endDate)
    }
}

private fun Activity.getXAndYString(startDateString: String,
                                    endDateString: String,
                                    forSetDate: Boolean = false,
                                    forAddedDate: Boolean = false,
                                    forCompletedDate: Boolean = false): String {
    return when {
        forSetDate -> getString(R.string.xAndYString,
            startDateString, endDateString)
        forAddedDate -> getString(R.string.addedXAndYString,
            startDateString, endDateString)
        forCompletedDate -> getString(R.string.completedXAndYString)
        else -> ""
    }
}

private fun Activity.getXToYString(startDateString: String,
                                   endDateString: String,
                                   forSetDate: Boolean = false,
                                   forAddedDate: Boolean = false,
                                   forCompletedDate: Boolean = false): String {
    return when {
        forSetDate -> getString(R.string.xToYString,
            startDateString, endDateString)
        forAddedDate -> getString(R.string.addedXToYString,
            startDateString, endDateString)
        forCompletedDate -> getString(R.string.completedXToYString)
        else -> ""
    }
}

private fun Activity.getAnyDateString(forSetDate: Boolean = false,
                                      forAddedDate: Boolean = false,
                                      forCompletedDate: Boolean = false): String {
    if(!forSetDate && !forAddedDate && !forCompletedDate) return ""
    val anyDateCode = when {
        forSetDate -> R.string.setAnyDateString
        forAddedDate -> R.string.addedAnyDateString
        forCompletedDate -> R.string.completedAnyDateString
        else -> 0
    }
    return getString(anyDateCode)
}

fun Activity.getDateRangeString(dateRange: DateRange,
                                forSetDate: Boolean = false,
                                forAddedDate: Boolean = false,
                                forCompletedDate: Boolean = false): String {
    if(!forSetDate && !forAddedDate && !forCompletedDate) return ""
    val todaysDate = getTodaysDate()
    val startDate = dateRange.getStartDate()
    val endDate = dateRange.getEndDate()
    if(startDate == null || endDate == null)
        return getAnyDateString(forSetDate, forAddedDate, forCompletedDate)
    if(datesAreTheSame(startDate, endDate))
        return getDateText(startDate,
            forSetDate = forSetDate,
            forAddedDate = forAddedDate,
            forCompletedDate = forCompletedDate)

    val daysBetween = daysBetween(startDate, endDate)
    val startDateString = getDateText(startDate)
    val endDateString = getDateText(endDate)
    if(daysBetween == 1) return getXAndYString(startDateString, endDateString,
        forSetDate = forSetDate,
        forAddedDate = forAddedDate,
        forCompletedDate = forCompletedDate)

    return when {
        datesAreTheSame(todaysDate, startDate) -> getNextXDaysText(daysBetween)
        datesAreTheSame(todaysDate, endDate) -> getLastXDaysText(daysBetween,
            forSetDate = forSetDate,
            forAddedDate = forAddedDate,
            forCompletedDate = forCompletedDate)
        else -> getXToYString(startDateString, endDateString,
            forSetDate = forSetDate,
            forAddedDate = forAddedDate,
            forCompletedDate = forCompletedDate)
    }
}

private fun Activity.getNextXDaysText(daysBetween: Int): String {
    return when {
        daysBetween < 14 -> getString(R.string.nextXDaysString,
            daysBetween.toString())
        daysBetween < 60 -> getString(R.string.nextXWeeksString,
            getDaysInWeeks(daysBetween).toString())
        daysBetween < 730 -> getString(R.string.nextXMonthsString,
            getDaysInMonths(daysBetween).toString())
        else -> getString(R.string.nextXYearsString,
            getDaysInYears(daysBetween).toString())
    }
}

private fun Activity.getLastXDaysText(daysBetween: Int,
                                      forSetDate: Boolean = false,
                                      forAddedDate: Boolean = false,
                                      forCompletedDate: Boolean = false): String {
    if(!forSetDate && !forAddedDate && !forCompletedDate) return ""
    val lastXDaysStringCode = when {
        forSetDate -> R.string.lastXDaysString
        forAddedDate -> R.string.addedLastXDaysString
        forCompletedDate -> R.string.completedLastXDaysString
        else -> 0
    }

    val lastXWeeksStringCode = when {
        forSetDate -> R.string.lastXWeeksString
        forAddedDate -> R.string.addedLastXWeeksString
        forCompletedDate -> R.string.completedLastXWeeksString
        else -> 0
    }

    val lastXMonthsStringCode = when {
        forSetDate -> R.string.lastXMonthsString
        forAddedDate -> R.string.addedLastXMonthsString
        forCompletedDate -> R.string.completedLastXMonthsString
        else -> 0
    }

    val lastXYearsStringCode = when {
        forSetDate -> R.string.lastXYearsString
        forAddedDate -> R.string.addedLastXYearsString
        forCompletedDate -> R.string.completedLastXYearsString
        else -> 0
    }

    return when {
        daysBetween < 14 -> getString(lastXDaysStringCode,
            daysBetween.toString())
        daysBetween < 60 -> getString(lastXWeeksStringCode,
            getDaysInWeeks(daysBetween).toString())
        daysBetween < 730 -> getString(lastXMonthsStringCode,
            getDaysInMonths(daysBetween).toString())
        else -> getString(lastXYearsStringCode,
            getDaysInYears(daysBetween).toString())
    }
}

fun ImageView.setColoredImageResource(resId: Int, color: Int) {
    setImageDrawable(context.getColoredDrawable(resId, color))
}

fun Context.getColoredDrawable(resId: Int, color: Int): Drawable? {
    val unwrappedDrawable = AppCompatResources.getDrawable(this, resId)
    if(unwrappedDrawable == null) return unwrappedDrawable
    val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable)
    DrawableCompat.setTint(wrappedDrawable, color)
    return wrappedDrawable
}

fun Activity.displayToast(message: String) {
    if(message.isEmpty()) return
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun generateId(ids: java.util.ArrayList<Int>): Int {
    val id = Random().nextInt((END_RANDOM_ID + 1) - START_RANDOM_ID) + START_RANDOM_ID
    return if(ids.contains(id)) generateId(ids) else id
}

fun String.comesAlphabeticallyBefore(str: String): Boolean {
    return compareTo(str, true) <= 0
}

fun String.comesAlphabeticallyAfter(str: String): Boolean {
    return compareTo(str, true) >= 0
}

fun Calendar.comesBefore(date: Calendar): Boolean  {
    return timeInMillis <= date.timeInMillis
}

fun Calendar.comesAfter(date: Calendar): Boolean {
    return timeInMillis >= date.timeInMillis
}

fun String.removeNewLines(): String {
    return replace("\n", " ")
}

fun showKeyboard(bottomSheet: BottomSheetDialog? = null, dialog: AlertDialog? = null) {
    val nonNullDialog = bottomSheet?: dialog?: return
    nonNullDialog.window?.setSoftInputMode(WindowManager
        .LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
}

fun Context.getRepeatingDaysString(repeatingDays: ArrayList<Int>): String {
    var repeatingDaysString = ""

    fun addLetter(letter: String) {
        if(repeatingDaysString.isNotEmpty())
            repeatingDaysString += ", "
        repeatingDaysString += letter
    }

    var sunLetter = getString(R.string.sunLetter)
    var monLetter = getString(R.string.monLetter)
    var tueLetter = getString(R.string.tueLetter)
    var wedLetter = getString(R.string.wedLetter)
    var thuLetter = getString(R.string.thuLetter)
    var friLetter = getString(R.string.friLetter)
    var satLetter = getString(R.string.satLetter)

    if(repeatingDays.size == 1) {
        sunLetter = getString(R.string.sundaysString)
        monLetter = getString(R.string.mondaysString)
        tueLetter = getString(R.string.tuesdaysString)
        wedLetter = getString(R.string.wednesdaysString)
        thuLetter = getString(R.string.thursdaysString)
        friLetter = getString(R.string.fridaysString)
        satLetter = getString(R.string.saturdaysString)
    } else if(repeatingDays.size == 2) {
        sunLetter = getString(R.string.sunString)
        monLetter = getString(R.string.monString)
        tueLetter = getString(R.string.tueString)
        wedLetter = getString(R.string.wedString)
        thuLetter = getString(R.string.thuString)
        friLetter = getString(R.string.friString)
        satLetter = getString(R.string.satString)
    }

    for(i in 0..6) {
        when(i) {
            0 -> if(repeatingDays.contains(Calendar.SUNDAY)) addLetter(sunLetter)
            1 -> if(repeatingDays.contains(Calendar.MONDAY)) addLetter(monLetter)
            2 -> if(repeatingDays.contains(Calendar.TUESDAY)) addLetter(tueLetter)
            3 -> if(repeatingDays.contains(Calendar.WEDNESDAY)) addLetter(wedLetter)
            4 -> if(repeatingDays.contains(Calendar.THURSDAY)) addLetter(thuLetter)
            5 -> if(repeatingDays.contains(Calendar.FRIDAY)) addLetter(friLetter)
            6 -> if(repeatingDays.contains(Calendar.SATURDAY)) addLetter(satLetter)
        }
    }

    when (repeatingDaysString) {
        "$monLetter, $tueLetter, $wedLetter, $thuLetter, $friLetter" ->
            repeatingDaysString = getString(R.string.weekdaysString)
        "$sunLetter, $satLetter" -> repeatingDaysString =
            getString(R.string.weekendsString)
        "$sunLetter, $monLetter, $tueLetter, $wedLetter, $thuLetter, $friLetter, $satLetter" ->
            repeatingDaysString = getString(R.string.everydayString)
    }

    return repeatingDaysString
}

fun Activity.getDateAndTimeString(date: Calendar): String {
    return getString(R.string.dateAndTimeString, getDateText(date), getTimeText(date))
}

fun Activity.getDetailsDateTextString(task: Task): String {
    val date = task.getDate()
    val dateString = when {
        task.hasDate() -> if(date != null) getDateText(date) else ""
        task.isRepeating() -> task.getRepeatingDaysString(this)
        else -> ""
    }
    return if(task.hasTime())
        getString(R.string.dateAndTimeString, dateString,
            getTimeText(task.getTimeHour(), task.getTimeMinute()))
    else dateString
}

fun Activity.getDateTextString(task: Task): String {
    val date = task.getDate()
    val dateString = when {
        task.hasDate() -> if(date != null) getRecencyText(date) else ""
        task.isRepeating() -> task.getRepeatingDaysString(this)
        else -> ""
    }
    return if(task.hasTime())
        getString(R.string.dateAtTimeString, dateString,
            getTimeText(task.getTimeHour(), task.getTimeMinute()))
    else dateString
}

fun displayTime(dateIcon: ImageView,
                dateText: TextView, task: Task) {
    dateIcon.visibility = View.VISIBLE
    dateText.visibility = View.VISIBLE
    dateIcon.setImageResource(R.drawable.ic_access_time_gray)
    dateText.text = getTimeText(task.getTimeHour(), task.getTimeMinute())
}

fun Activity.displayDate(dateIcon: ImageView,
                         dateText: TextView, task: Task) {
    dateIcon.visibility = View.VISIBLE
    dateText.visibility = View.VISIBLE
    val dateIconCode =
        when {
            task.hasDate() -> R.drawable.ic_date_gray
            task.isRepeating() -> R.drawable.ic_autorenew_gray
            else -> 0
        }
    if(dateIconCode != 0)
        dateIcon.setImageResource(dateIconCode)
    dateText.text = getDateTextString(task)
}

fun Activity.openBulletedList(id: Int, taskId: Int = SENTINEL) {
    val bulletedListPage = Intent(this, BulletedListActivity::class.java)
    bulletedListPage.putExtra(listIdRef, id)
    if(taskId != SENTINEL) bulletedListPage.putExtra(taskToOpenRef, taskId)
    startActivityForResult(bulletedListPage, OPEN_LIST_CODE)
}

fun Activity.openRoutineList(id: Int, taskId: Int = SENTINEL) {
    val routineListPage = Intent(this, RoutineListActivity::class.java)
    routineListPage.putExtra(listIdRef, id)
    if(taskId != SENTINEL) routineListPage.putExtra(taskToOpenRef, taskId)
    startActivityForResult(routineListPage, OPEN_LIST_CODE)
}

fun Activity.openProgressList(id: Int, taskId: Int = SENTINEL) {
    val progressListPage = Intent(this, ProgressListActivity::class.java)
    progressListPage.putExtra(listIdRef, id)
    if(taskId != SENTINEL) progressListPage.putExtra(taskToOpenRef, taskId)
    startActivityForResult(progressListPage, OPEN_LIST_CODE)
}

fun Activity.openToDoList(id: Int, taskId: Int = SENTINEL) {
    val toDoListPage = Intent(this, ToDoListActivity::class.java)
    toDoListPage.putExtra(listIdRef, id)
    if(taskId != SENTINEL) toDoListPage.putExtra(taskToOpenRef, taskId)
    startActivityForResult(toDoListPage, OPEN_LIST_CODE)
}