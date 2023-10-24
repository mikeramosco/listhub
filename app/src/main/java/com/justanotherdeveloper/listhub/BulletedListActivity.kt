package com.justanotherdeveloper.listhub

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import kotlinx.android.synthetic.main.activity_bulleted_list.*

class BulletedListActivity : AppCompatActivity(),
    DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var view: BulletedListView
    private lateinit var listeners: BulletedListListeners
    private lateinit var dialogs: BulletedListDialogs
    private lateinit var manager: BulletedListManager
    private lateinit var database: ListsDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bulleted_list)

        database = ListsDatabase(this)
        view = BulletedListView(this)
        dialogs = BulletedListDialogs(this)
        listeners = BulletedListListeners(this)
        manager = BulletedListManager(this)

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
        updateBulletedList(false)
    }

    fun getView(): BulletedListView {
        return view
    }

    fun getDialogs(): BulletedListDialogs {
        return dialogs
    }

    fun getList(): BulletedList {
        return manager.getList()
    }

    fun getManager(): BulletedListManager {
        return manager
    }

    fun getDatabase(): ListsDatabase {
        return database
    }

    fun updateBulletedList(updateListDate: Boolean = true) {
        database.updateBulletedList(getList(), updateListDate)
    }

    fun openList(listId: Int) {
        when(database.getList(listId).getListType()) {
            toDoListRef -> openToDoList(listId)
            progressListRef -> openProgressList(listId)
            routineListRef -> openRoutineList(listId)
            else -> openBulletedList(listId)
        }
    }

    fun openReorderPage() {
        if(manager.inSelectState()) view.setToDefaultState()
        val reorderPage = Intent(this, ReorderListActivity::class.java)
        reorderPage.putExtra(listTypeRef, bulletedListRef)
        reorderPage.putExtra(listIdRef, getList().getListId())
        startActivityForResult(reorderPage, REORDER_PAGE_CODE)
    }

    private fun reloadBulletedList(reloadAll: Boolean = true) {
        if(!database.listExists(getList()
                .getListId())) {
            finish()
            return
        }
        manager.refreshList(database)
        view.reloadBulletedList(reloadAll)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            OPEN_LIST_CODE -> reloadBulletedList()
            REORDER_PAGE_CODE -> {
                val listReordered = data?.getBooleanExtra(
                    listReorderedRef, false)?: false
                if(listReordered) reloadBulletedList(false)
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
        if(manager.inSelectState()) view.setToDefaultState()
        else super.onBackPressed()
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
