package com.justanotherdeveloper.listhub

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import kotlinx.android.synthetic.main.activity_to_do_list.*

// Page removed from final app
class ToDoListActivity : AppCompatActivity(),
    DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var view: ToDoListView
    private lateinit var listeners: ToDoListListeners
    private lateinit var dialogs: ToDoListDialogs
    private lateinit var manager: ToDoListManager
    private lateinit var database: ListsDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_do_list)

        database = ListsDatabase(this)
        view = ToDoListView(this)
        dialogs = ToDoListDialogs(this)
        listeners = ToDoListListeners(this)
        manager = ToDoListManager(this)

        val taskId = intent.getIntExtra(taskToOpenRef, SENTINEL)
        if(taskId != SENTINEL) manager.setTaskToOpen(taskId)
        loadOrStartList()
    }

    private fun saveScrollState() {
        getList().setScrollState(tasksScrollView.scrollY)
        updateToDoList(false)
    }

    private fun loadOrStartList() {
        val listId = intent.getIntExtra(listIdRef, SENTINEL)
        if(listId == SENTINEL) manager.initNewList()
        else manager.loadList(database, listId)
    }

    fun getView(): ToDoListView {
        return view
    }

    fun getDialogs(): ToDoListDialogs {
        return dialogs
    }

    fun getList(): ToDoList {
        return manager.getList()
    }

    fun getManager(): ToDoListManager {
        return manager
    }

    fun getDatabase(): ListsDatabase {
        return database
    }

    fun updateToDoList(updateListDate: Boolean = true) {
        database.updateToDoList(getList(), updateListDate)
    }

    fun openList(listId: Int) {
        when(database.getList(listId).getListType()) {
            toDoListRef -> openToDoList(listId)
            progressListRef -> openProgressList(listId)
            routineListRef -> openRoutineList(listId)
            else -> openBulletedList(listId)
        }
    }

    fun openReorderPage(reorderTasks: Boolean = true) {
        if(manager.inSelectState()) view.setToDefaultState()
        val reorderPage = Intent(this, ReorderListActivity::class.java)
        val listToReorder = if(reorderTasks) reorderTasksRef else reorderCompletedRef
        reorderPage.putExtra(listTypeRef, toDoListRef)
        reorderPage.putExtra(listIdRef, getList().getListId())
        reorderPage.putExtra(listToReorderRef, listToReorder)
        startActivityForResult(reorderPage, REORDER_PAGE_CODE)
    }

    private fun reloadToDoList(reloadAll: Boolean = true) {
        if(!database.listExists(getList()
                .getListId())) {
            finish()
            return
        }
        manager.refreshList(database)
        view.reloadToDoList(reloadAll)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            OPEN_LIST_CODE -> reloadToDoList()
            REORDER_PAGE_CODE -> {
                val listReordered = data?.getBooleanExtra(
                    listReorderedRef, false)?: false
                if(listReordered) reloadToDoList(false)
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
