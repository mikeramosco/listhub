package com.justanotherdeveloper.listhub

import java.util.*

class DateRange {
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var savedDate = getTodaysDate()

    fun initDateRange(nextSevenDays: Boolean = false): DateRange {
        startDate = getTodaysDate()
        endDate = getTodaysDate()
        if(nextSevenDays)
            endDate?.add(Calendar.DATE, 7)
        else startDate?.add(Calendar.DATE, -7)
        return this
    }

    fun getStartDate(): Calendar? {
        checkIfDatesUpdated()
        return startDate
    }

    fun getEndDate(): Calendar? {
        checkIfDatesUpdated()
        return endDate
    }

    fun isSingleDate(): Boolean {
        val startDate = this.startDate
        val endDate = this.endDate
        return if(startDate == null || endDate == null) false
        else datesAreTheSame(startDate, endDate)
    }

    fun setDateRange(startDate: Calendar?, endDate: Calendar?) {
        savedDate = getTodaysDate()
        savedDate.resetTimeOfDay()
        startDate?.resetTimeOfDay()
        endDate?.resetTimeOfDay()
        this.startDate = startDate
        this.endDate = endDate
    }

    private fun checkIfDatesUpdated() {
        if(startDate == null || endDate == null) return
        val todaysDate = getTodaysDate()
        if(!datesAreTheSame(todaysDate, savedDate)) {
            val daysBetween = daysBetween(todaysDate, savedDate)
            startDate?.add(Calendar.DATE, daysBetween)
            endDate?.add(Calendar.DATE, daysBetween)
            savedDate = getTodaysDate()
            savedDate.resetTimeOfDay()
        }
    }
}