package com.justanotherdeveloper.listhub

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import kotlinx.android.synthetic.main.activity_progress_list.*

class ProgressListActivity : AppCompatActivity(),
    DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener  {

    private lateinit var view: ProgressListView
    private lateinit var listeners: ProgressListListeners
    private lateinit var dialogs: ProgressListDialogs
    private lateinit var manager: ProgressListManager
    private lateinit var database: ListsDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_list)

        database = ListsDatabase(this)
        view = ProgressListView(this)
        dialogs = ProgressListDialogs(this)
        listeners = ProgressListListeners(this)
        manager = ProgressListManager(this)

        val taskId = intent.getIntExtra(taskToOpenRef, SENTINEL)
        if(taskId != SENTINEL) manager.setTaskToOpen(taskId)
        loadOrStartList()
    }

    private fun loadOrStartList() {
        val listId = intent.getIntExtra(listIdRef, SENTINEL)
        if(listId == SENTINEL) manager.initNewList()
        else manager.loadList(database, listId)
    }

    private fun saveScrollState() {
        getList().setScrollState(tasksScrollView.scrollY)
        updateProgressList(false)
    }

    fun getView(): ProgressListView {
        return view
    }

    fun getDialogs(): ProgressListDialogs {
        return dialogs
    }

    fun getManager(): ProgressListManager {
        return manager
    }

    fun getDatabase(): ListsDatabase {
        return database
    }

    fun getList(): ProgressList {
        return manager.getList()
    }

    fun updateProgressList(updateListDate: Boolean = true) {
        database.updateProgressList(getList(), updateListDate)
    }

    fun openList(listId: Int) {
        when(database.getList(listId).getListType()) {
            toDoListRef -> openToDoList(listId)
            progressListRef -> openProgressList(listId)
            routineListRef -> openRoutineList(listId)
            else -> openBulletedList(listId)
        }
    }

    fun openReorderPage(listToReorder: String) {
        if(manager.inSelectState()) view.setToDefaultState()
        val reorderPage = Intent(this, ReorderListActivity::class.java)
        reorderPage.putExtra(listTypeRef, progressListRef)
        reorderPage.putExtra(listIdRef, getList().getListId())
        reorderPage.putExtra(listToReorderRef, listToReorder)
        startActivityForResult(reorderPage, REORDER_PAGE_CODE)
    }

    private fun reloadProgressList(reloadAll: Boolean = true) {
        if(!database.listExists(getList()
                .getListId())) {
            finish()
            return
        }
        manager.refreshList(database)
        view.reloadProgressList(reloadAll)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            OPEN_LIST_CODE -> reloadProgressList()
            REORDER_PAGE_CODE -> {
                val listReordered = data?.getBooleanExtra(
                    listReorderedRef, false)?: false
                if(listReordered) reloadProgressList(false)
            }
        }
    }

    override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
        if(dialogs.addTaskDialogIsShowing())
            dialogs.setCurrentDialogTime(hour, minute)
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        val date = createCalendar(year, month, day)
        if(dialogs.addTaskDialogIsShowing())
            dialogs.setCurrentDialogDate(date)
        else manager.setSelectedTasksDates(date)
    }

    override fun onBackPressed() {
        when {
            manager.inSelectState() -> view.setToDefaultState()
            manager.inReorderSectionState() -> view.exitReorderSectionsState()
            else -> super.onBackPressed()
        }
    }

    override fun onPause() {
        saveScrollState()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        manager.updateTasksIfDateOutdated()
    }
}
