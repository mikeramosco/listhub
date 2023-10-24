package com.justanotherdeveloper.listhub

import java.util.*

open class List(private val listType: String) {

    private var title = ""
    private var note = ""
    private var isStarred = false
    private var showCompleted = true
    private var isArchived = false
    private var colorThemeIndex = 0
    private var scrollState = 0
    private var sortIndex = 0
    private var listId = SENTINEL
    private var labels = ArrayList<String>()
    private var dateCreated = getTodaysDate()
    private var dateUpdated = getTodaysDate()

    var taskIds = ArrayList<Int>()
    var list = ArrayList<Int>()
    var completed = ArrayList<Int>()

    fun archive() {
        isArchived = true
    }

    fun unarchive() {
        isArchived = false
    }

    fun isArchived(): Boolean {
        return isArchived
    }

    fun toggleStar() {
        isStarred = !isStarred
    }

    fun isStarred(): Boolean {
        return isStarred
    }

    fun getListType(): String {
        return listType
    }

    fun setDateCreated(dateCreated: Calendar) {
        this.dateCreated = dateCreated
    }

    fun getDateCreated(): Calendar {
        return dateCreated
    }

    fun getDateUpdated(): Calendar {
        return dateUpdated
    }

    fun listUpdated() {
        dateUpdated = getTodaysDate()
    }

    fun setListId(listId: Int) {
        this.listId = listId
    }

    fun getListId(): Int {
        return listId
    }

    fun getTitle(): String {
        return title
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun setColorThemeIndex(colorThemeIndex: Int) {
        this.colorThemeIndex = colorThemeIndex
    }

    fun getColorThemeIndex(): Int {
        return colorThemeIndex
    }

    fun hasLabels(): Boolean {
        return labels.isNotEmpty()
    }

    fun getLabelsString(): String {
        var labelsString = ""
        var labelAdded = false
        for(label in labels) {
            if(labelAdded) labelsString += ", "
            else labelAdded = true
            labelsString += label
        }
        return labelsString
    }

    fun getLabels(): ArrayList<String> {
        return labels
    }

    fun setLabels(labels: ArrayList<String>) {
        this.labels = labels
    }

    fun addLabel(label: String) {
        labels.add(label)
    }

    fun removeLabel(label: String) {
        labels.remove(label)
    }

    fun setScrollState(scrollState: Int) {
        this.scrollState = scrollState
    }

    fun getScrollState(): Int {
        return scrollState
    }

    fun setSortIndex(sortIndex: Int) {
        this.sortIndex = sortIndex
    }

    fun getSortIndex(): Int {
        return sortIndex
    }

    fun isSorted(): Boolean {
        return sortIndex != 0
    }

    fun toggleCompletedVisibility() {
        showCompleted = !showCompleted
    }

    fun completedShown(): Boolean {
        return showCompleted
    }

    fun getCurrentTasks(): ArrayList<Int> {
        return list
    }

    fun getCompletedTasks(): ArrayList<Int> {
        return completed
    }

    fun setNote(note: String) {
        this.note = note
    }

    fun getNote(): String {
        hasNote()
        return note
    }

    fun hasNote(): Boolean {
        @Suppress("SENSELESS_COMPARISON")
        if(note == null) note = ""
        return note.isNotEmpty()
    }
}