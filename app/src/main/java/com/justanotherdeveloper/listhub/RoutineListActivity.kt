package com.justanotherdeveloper.listhub

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import kotlinx.android.synthetic.main.activity_routine_list.*

class RoutineListActivity : AppCompatActivity(),
    DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var view: RoutineListView
    private lateinit var listeners: RoutineListListeners
    private lateinit var dialogs: RoutineListDialogs
    private lateinit var manager: RoutineListManager
    private lateinit var database: ListsDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routine_list)

        database = ListsDatabase(this)
        view = RoutineListView(this)
        dialogs = RoutineListDialogs(this)
        listeners = RoutineListListeners(this)
        manager = RoutineListManager(this)

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
        updateRoutineList(false)
    }

    fun getView(): RoutineListView {
        return view
    }

    fun getDialogs(): RoutineListDialogs {
        return dialogs
    }

    fun getList(): RoutineList {
        return manager.getList()
    }

    fun getManager(): RoutineListManager {
        return manager
    }

    fun getDatabase(): ListsDatabase {
        return database
    }

    fun updateRoutineList(updateListDate: Boolean = true) {
        database.updateRoutineList(getList(), updateListDate)
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
        reorderPage.putExtra(listTypeRef, routineListRef)
        reorderPage.putExtra(listIdRef, getList().getListId())
        startActivityForResult(reorderPage, REORDER_PAGE_CODE)
    }

    private fun reloadRoutineList(reloadAll: Boolean = true) {
        if(!database.listExists(getList()
                .getListId())) {
            finish()
            return
        }
        manager.refreshList(database)
        view.reloadRoutineList(reloadAll)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            OPEN_LIST_CODE -> reloadRoutineList()
            REORDER_PAGE_CODE -> {
                val listReordered = data?.getBooleanExtra(
                    listReorderedRef, false)?: false
                if(listReordered) reloadRoutineList(false)
            }
        }
    }

    override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
        if(dialogs.addTaskDialogIsShowing())
            dialogs.setCurrentDialogTime(hour, minute)
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        val date = createCalendar(year, month, day)
        manager.setListDate(date)
        if(dialogs.nameListDialogShowing())
            dialogs.setListDialogDate(date)
    }

    override fun onBackPressed() {
        if(manager.inSelectState()) view.setToDefaultState()
        else super.onBackPressed()
    }

    override fun onPause() {
        saveScrollState()
        super.onPause()
    }
}
