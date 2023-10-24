package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class SearchPageActivity : AppCompatActivity() {

    private lateinit var view: SearchPageView
    private lateinit var manager: SearchPageManager
    private lateinit var listeners: SearchPageListeners
    private lateinit var database: ListsDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_page)

        database = ListsDatabase(this)
        view = SearchPageView(this)
        manager = SearchPageManager(this)
        listeners = SearchPageListeners(this)
    }

    fun getView(): SearchPageView {
        return view
    }

    fun getManager(): SearchPageManager {
        return manager
    }

    fun getDatabase(): ListsDatabase {
        return database
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == OPEN_LIST_CODE) {
            manager.setListOpened(false)
            view.resetSearchPage()
        }
    }

    override fun onResume() {
        super.onResume()
        manager.searchResetIfDateIsOutdated()
    }

    @SuppressLint("InflateParams")
    fun showFilterSearchDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_filter_search, null)
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(view)
        val filterListsDialog = builder.create()
        filterListsDialog.setCancelable(true)

        val colorTheme = getColorTheme()

        val listsOnlyLayout = view.findViewById<LinearLayout>(R.id.listsOnlyLayout)
        val labelsLayout = view.findViewById<LinearLayout>(R.id.labelsLayout)
        val notesLayout = view.findViewById<LinearLayout>(R.id.notesLayout)
        val rewardsLayout = view.findViewById<LinearLayout>(R.id.rewardsLayout)
        val hideCompletedLayout = view.findViewById<LinearLayout>(R.id.hideCompletedLayout)
        val hideArchivedLayout = view.findViewById<LinearLayout>(R.id.hideArchivedLayout)

        val listsOnlyCheckbox = view.findViewById<ImageView>(R.id.listsOnlyCheckbox)
        val labelsCheckbox = view.findViewById<ImageView>(R.id.labelsCheckbox)
        val notesCheckbox = view.findViewById<ImageView>(R.id.notesCheckbox)
        val rewardsCheckbox = view.findViewById<ImageView>(R.id.rewardsCheckbox)
        val hideCompletedCheckbox = view.findViewById<ImageView>(R.id.hideCompletedCheckbox)
        val hideArchivedCheckbox = view.findViewById<ImageView>(R.id.hideArchivedCheckbox)

        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val applyOption = view.findViewById<TextView>(R.id.applyOption)

        val originalOptions = manager.getCheckedFilterOptions()
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

        val options = ArrayList<LinearLayout>()
        options.add(listsOnlyLayout)
        options.add(labelsLayout)
        options.add(notesLayout)
        options.add(rewardsLayout)
        options.add(hideCompletedLayout)
        options.add(hideArchivedLayout)

        options[0].setOnClickListener {
            toggleCheckbox(0, listsOnlyCheckbox) }
        options[1].setOnClickListener {
            toggleCheckbox(1, labelsCheckbox) }
        options[2].setOnClickListener {
            toggleCheckbox(2, notesCheckbox) }
        options[3].setOnClickListener {
            toggleCheckbox(3, rewardsCheckbox) }
        options[4].setOnClickListener {
            toggleCheckbox(4, hideCompletedCheckbox) }
        options[5].setOnClickListener {
            toggleCheckbox(5, hideArchivedCheckbox) }

        for((i, option) in originalOptions.withIndex())
            if(option) options[i].performClick()

        var cancelClicked = false
        view.findViewById<TextView>(R.id.removeFiltersOption).setOnClickListener {
            val removedFilters = ArrayList<Boolean>()
            for(i in 0 until 6) removedFilters.add(false)
            manager.setCheckedFilterOptions(removedFilters)
            manager.applySearch()
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
            if(!cancelClicked) {
                manager.setCheckedFilterOptions(checkedFilterOptions)
                manager.applySearch()
            }
        }

        filterListsDialog.show()
    }
}
