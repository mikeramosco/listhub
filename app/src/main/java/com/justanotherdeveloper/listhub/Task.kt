package com.justanotherdeveloper.listhub

import android.content.Context
import java.util.*
import kotlin.collections.ArrayList

open class Task {

    private var task = ""
    private var note = ""
    private var reward = ""
    private var rewardedTask = ""
    private var websiteLink = ""
    private var completed = false
    private var starred = false
    private var repeated = false
    private var isRepeat = false
    private var rewardAdded = false
    private var hour = SENTINEL
    private var minute = SENTINEL
    private var listId = SENTINEL
    private var taskId = SENTINEL
    private var linkedListId = SENTINEL
    private var repeatingDays = ArrayList<Int>()
    private var date: Calendar? = null
    private var dateCompleted: Calendar? = null
    private var dateCreated = getTodaysDate()

    constructor(listId: Int) {
        this.listId = listId
    }

    constructor(otherTask: Task, taskId: Int) {
        this.taskId = taskId
        task = otherTask.task
        listId = otherTask.listId
        note = otherTask.note
        reward = otherTask.reward
        rewardAdded = otherTask.rewardAdded
        rewardedTask = otherTask.rewardedTask
        starred = otherTask.starred
        websiteLink = otherTask.websiteLink
        linkedListId = otherTask.linkedListId
        hour = otherTask.hour
        minute = otherTask.minute
        if(otherTask.completed) setCompleted(true)
        if(otherTask.hasDate())
            date = copyDate(otherTask.getDate()!!)
        if(otherTask.isRepeating()) {
            repeatingDays = copyIntList(
                otherTask.getRepeatingDays())
            repeated = otherTask.repeated
        }
    }

    fun hasContent(): Boolean {
        return note.isNotEmpty() || reward.isNotEmpty() || websiteLink.isNotEmpty() ||
                linkedListId != SENTINEL || hour != SENTINEL || minute != SENTINEL ||
                hasDueDate()
    }

    fun setListId(listId: Int) {
        this.listId = listId
    }

    fun getListId(): Int {
        return listId
    }

    fun getTaskId(): Int {
        return taskId
    }

    fun setTaskId(taskId: Int) {
        this.taskId = taskId
    }

    fun setTask(task: String) {
        this.task = task.removeNewLines()
    }

    fun getTask(): String {
        return task
    }

    fun setNote(note: String) {
        this.note = note
    }

    fun getNote(): String {
        return note
    }

    fun hasNote(): Boolean {
        return note.isNotEmpty()
    }

    fun setWebsiteLink(websiteLink: String) {
        this.websiteLink = websiteLink
    }

    fun getWebsiteLink(): String {
        hasWebsiteLink()
        return websiteLink
    }

    fun hasWebsiteLink(): Boolean {
        @Suppress("SENSELESS_COMPARISON")
        if(websiteLink == null) websiteLink = ""
        return websiteLink.isNotEmpty()
    }

    fun removeWebsiteLink() {
        websiteLink = ""
    }

    fun setReward(reward: String) {
        rewardAdded = false
        this.reward = reward
    }

    fun getReward(): String {
        return reward
    }

    fun hasReward(): Boolean {
        return reward.isNotEmpty()
    }

    fun setRewardAdded() {
        rewardAdded = true
    }

    fun rewardAlreadyAdded(): Boolean {
        return false
//        return rewardAdded
    }

    fun isReward(): Boolean {
        return rewardedTask.isNotEmpty()
    }

    fun setTaskForReward(taskForReward: String) {
        this.rewardedTask = taskForReward
    }

    fun getRewardedTask(): String {
        return rewardedTask
    }

    fun setCompleted(completed: Boolean = true) {
        this.completed = completed
        if(completed) dateCompleted = getTodaysDate()
    }

    fun isCompleted(): Boolean {
        return completed
    }

    fun setStarred(starred: Boolean = true) {
        this.starred = starred
    }

    fun isStarred(): Boolean {
        return starred
    }

    fun setRepeated() {
        repeated = true
    }

    fun alreadyRepeated(): Boolean {
        return false
//        return repeated
    }

    fun setAsRepeat() {
        isRepeat = true
    }

    private fun isRepeat(): Boolean {
        @Suppress("SENSELESS_COMPARISON")
        if(isRepeat == null) isRepeat = false
        return isRepeat
    }

    fun hasTime(): Boolean {
        return hour != SENTINEL
    }

    fun getTimeHour(): Int {
        return hour
    }

    fun getTimeMinute(): Int {
        return minute
    }

    fun setTime(hour: Int, minute: Int) {
        this.hour = hour
        this.minute = minute
        date?.set(Calendar.HOUR_OF_DAY, hour)
        date?.set(Calendar.MINUTE, minute)
    }

    fun removeTime() {
        hour = SENTINEL
        minute = SENTINEL
        date?.resetTimeOfDay()
    }

    fun setDate(date: Calendar?) {
        if(!hasTime()) date?.resetTimeOfDay()
        this.date = date
    }

    fun getDate(): Calendar? {
        if(hasTime()) {
            date?.set(Calendar.HOUR_OF_DAY, hour)
            date?.set(Calendar.MINUTE, minute)
        } else date?.resetTimeOfDay()
        return date
    }

    fun hasDate(): Boolean {
        return date != null
    }

    fun hasDueDate(): Boolean {
        return hasDate() || isRepeating()
    }

    fun getDueDate(dateCursor: Calendar = getTodaysDate()): Calendar? {
        if(hasDate()) return getDate()
        if(isRepeating()) {
            var dayOfWeek = dateCursor.get(Calendar.DAY_OF_WEEK)
            while(true) {
                if (!isRepeat() || !datesAreTheSame(dateCursor, dateCreated)) {
                    if (repeatingDays.contains(dayOfWeek)) {
                        if (hasTime()) {
                            dateCursor.set(Calendar.HOUR_OF_DAY, hour)
                            dateCursor.set(Calendar.MINUTE, minute)
                        } else dateCursor.resetTimeOfDay()
                        return dateCursor
                    }
                }
                dateCursor.add(Calendar.DATE, 1)
                dayOfWeek = dateCursor.get(Calendar.DAY_OF_WEEK)
            }
        }
        return null
    }

    fun clearRepeatingDays() {
        repeatingDays.clear()
        repeated = false
    }

    fun setRepeatingDays(repeatingDays: ArrayList<Int>) {
        this.repeatingDays = repeatingDays
    }

    fun getRepeatingDays(): ArrayList<Int> {
        return repeatingDays
    }

    fun getRepeatingDaysString(context: Context): String {
        return context.getRepeatingDaysString(repeatingDays)
    }

    fun isRepeating(): Boolean {
        return repeatingDays.size > 0
    }

    fun setDateCreated(dateCreated: Calendar) {
        this.dateCreated = dateCreated
    }

    fun getDateCreated(): Calendar {
        return dateCreated
    }

    fun setDateCompleted(dateCompleted: Calendar) {
        this.dateCompleted = dateCompleted
    }

    fun getDateCompleted(): Calendar? {
        return dateCompleted
    }

    fun isLinkedToList(): Boolean {
        return linkedListId != SENTINEL
    }

    fun removeLinkFromList() {
        linkedListId = SENTINEL
    }

    fun linkToList(linkedListId: Int) {
        this.linkedListId = linkedListId
    }

    fun getLinkedListId(): Int {
        return linkedListId
    }
}