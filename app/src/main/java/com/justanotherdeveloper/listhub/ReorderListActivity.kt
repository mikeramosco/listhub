package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_reorder_list.*
import java.util.*
import kotlin.collections.ArrayList


class ReorderListActivity : AppCompatActivity() {

    private lateinit var database: ListsDatabase

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter

    private lateinit var toDoList: ToDoList
    private lateinit var toDoListToReorder: ArrayList<Int>

    private lateinit var progressList: ProgressList
    private lateinit var progressListToReorder: ArrayList<Int>

    private lateinit var routineList: RoutineList
    private lateinit var routineListToReorder: ArrayList<Int>

    private lateinit var bulletedList: BulletedList
    private lateinit var bulletedListToReorder: ArrayList<Int>

    private lateinit var stringListToReorder: ArrayList<String>

    private lateinit var originalOrder: String

    private var selectedTaskLayout: LinearLayout? = null
    private var isReversed = false
    private var listType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reorder_list)

        database = ListsDatabase(this)
        stringListToReorder = ArrayList()

        isReversed = intent.getBooleanExtra(orderReversedRef, false)
        val listId = intent.getIntExtra(listIdRef, SENTINEL)
        listType = intent.getStringExtra(listTypeRef) ?: return
        when(listType) {
            toDoListRef -> initToDoListToReorder(listId)
            progressListRef -> initProgressListToReorder(listId)
            routineListRef -> initRoutineListToReorder(listId)
            bulletedListRef -> initBulletedListToReorder(listId)
        }

        handleItemTouchHelper()

        initButtonAnimationListener(exitButton)
        initButtonAnimationListener(saveButton)
        initButtonAnimationListener(moreOptions)
        exitButton.setOnClickListener { onBackPressed() }
        saveButton.setOnClickListener { saveOrder() }
        moreOptions.setOnClickListener { showMoreOptionsDialog() }
    }

    private fun getListAsString(): String {
        var listAsString = ""
        var firstAdded = false
        for(str in stringListToReorder) {
            if(firstAdded) {
               listAsString += "\n"
            } else firstAdded = true
            listAsString += str
        }
        return listAsString
    }

    private fun saveOrder() {
        when(listType) {
            toDoListRef -> database.updateToDoList(toDoList)
            progressListRef -> database.updateProgressList(progressList)
            routineListRef -> {
                routineList.refreshRoutineListsOrder()
                database.updateRoutineList(routineList)
            }
            bulletedListRef -> database.updateBulletedList(bulletedList)
        }
        val intentData = Intent()
        intentData.putExtra(listReorderedRef, true)
        setResult(Activity.RESULT_OK, intentData)
        finish()
    }

    private fun reverseList(tasks: ArrayList<Int>? = null,
                            progressTasks: ArrayList<Int>? = null,
                            taskIds: ArrayList<Int>? = null) {
        when {
            tasks != null -> {
                val tempList = ArrayList<Int>()
                for(taskId in tasks) tempList.add(0, taskId)
                tasks.clear()
                for(taskId in tempList) tasks.add(taskId)
            }
            progressTasks != null -> {
                val tempList = ArrayList<Int>()
                for(taskId in progressTasks) tempList.add(0, taskId)
                progressTasks.clear()
                for(taskId in tempList) progressTasks.add(taskId)
            }
            taskIds != null -> {
                val tempList = ArrayList<Int>()
                for(task in taskIds) tempList.add(0, task)
                taskIds.clear()
                for(task in tempList) taskIds.add(task)
            }
        }
    }

    private fun initBulletedListToReorder(listId: Int) {
        bulletedList = database.getBulletedList(listId)
        bulletedListToReorder = bulletedList.getCurrentTasks()
        if(isReversed) reverseList(tasks = bulletedListToReorder)
        reorderTasksText.text = getString(R.string.reorderItemsString)
        for(taskId in bulletedListToReorder) {
            val task = bulletedList.getTask(taskId)
            if(task != null) stringListToReorder.add(task.getTask())
        }
        initRecyclerView()
    }

    private fun initRoutineListToReorder(listId: Int) {
        routineList = database.getRoutineList(listId)
        routineListToReorder = routineList.getRoutineTaskIdsOrder()
        if(isReversed) reverseList(taskIds = routineListToReorder)
        reorderTasksText.text = getString(R.string.reorderRoutineString)
        for(task in routineList.getRoutineTasksInOrder())
            stringListToReorder.add(task.getTask())
        initRecyclerView()
    }

    private fun initProgressListToReorder(listId: Int) {
        progressList = database.getProgressList(listId)
        val listToReorder = intent.getStringExtra(listToReorderRef)?: return
        progressListToReorder = when(listToReorder) {
            progressList.getInProgressTitle() -> progressList.getCurrentTasks()
            progressList.getCompletedTitle() -> progressList.getCompletedTasks()
            else -> {
                var list = ArrayList<Int>()
                for((i, sectionTitle) in progressList
                    .getListSectionTitles().withIndex())
                    if(sectionTitle == listToReorder) {
                        list = progressList.getListSections()[i]
                        break
                    }
                list
            }
        }
        if(isReversed) reverseList(progressTasks = progressListToReorder)
        reorderTasksText.text = getString(R.string.reorderSingleListString, listToReorder)

        for(taskId in progressListToReorder) {
            val task = progressList.getTask(taskId)
            if(task != null) stringListToReorder.add(task.getTask())
        }
        initRecyclerView()
    }

    private fun initToDoListToReorder(listId: Int) {
        toDoList = database.getToDoList(listId)
        toDoListToReorder = when(intent.getStringExtra(listToReorderRef)) {
            reorderTasksRef -> {
                reorderTasksText.text = getString(R.string.reorderTasksString)
                toDoList.getCurrentTasks()
            }
            else -> {
                reorderTasksText.text = getString(R.string.reorderCompletedString)
                toDoList.getCompletedTasks()
            }
        }
        if(isReversed) reverseList(tasks = toDoListToReorder)

        for(taskId in toDoListToReorder) {
            val task = toDoList.getTask(taskId)
            if(task != null) stringListToReorder.add(task.getTask())
        }
        initRecyclerView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initRecyclerView() {
        originalOrder = if(isReversed)
            "" else stringListToReorder.toString()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerAdapter = RecyclerAdapter(stringListToReorder, this, true)
        recyclerView.adapter = recyclerAdapter
    }

    private lateinit var itemTouchHelper: ItemTouchHelper

    fun getItemTouchHelper(): ItemTouchHelper {
        return itemTouchHelper
    }

    private fun handleItemTouchHelper() {
        itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                val taskSelected = actionState != 0
                if(taskSelected) selectedTaskLayout =
                    viewHolder?.itemView?.findViewById(R.id.taskLayout)
                selectedTaskLayout?.let { animateButton(it, taskSelected) }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                Collections.swap(stringListToReorder, fromPosition, toPosition)
                when(listType) {
                    toDoListRef -> Collections.swap(
                        toDoListToReorder, fromPosition, toPosition)
                    progressListRef -> Collections.swap(
                        progressListToReorder, fromPosition, toPosition)
                    routineListRef -> Collections.swap(
                        routineListToReorder, fromPosition, toPosition)
                    bulletedListRef -> Collections.swap(
                        bulletedListToReorder, fromPosition, toPosition)
                }

                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { }
        }

    @SuppressLint("InflateParams")
    private fun showConfirmCloseDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_confirm_delete, null)
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(view)
        val confirmDeleteNoteDialog = builder.create()
        confirmDeleteNoteDialog.setCancelable(true)

        val cancelOption = view.findViewById<TextView>(R.id.cancelOption)
        val confirmOption = view.findViewById<TextView>(R.id.removeOption)

        val confirmDeleteMessage = view.findViewById<TextView>(R.id.confirmDeleteMessage)
        confirmDeleteMessage.text = getString(R.string.exitReorderPagePrompt)
        confirmOption.text = getString(R.string.exitString)

        var confirmClicked = false

        confirmOption.setOnClickListener {
            confirmClicked = true
            confirmDeleteNoteDialog.cancel()
        }

        cancelOption.setOnClickListener {
            confirmDeleteNoteDialog.cancel()
        }

        confirmDeleteNoteDialog.setOnCancelListener {
            confirmDeleteNoteDialog.dismiss()
            if(confirmClicked) finish()
        }

        confirmDeleteNoteDialog.show()
    }

    @SuppressLint("InflateParams")
    private fun showMoreOptionsDialog() {
        val moreOptionsDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheet_reorder_more_options, null)
        moreOptionsDialog.setContentView(view)

        val dialogOptions = ArrayList<LinearLayout>()
        dialogOptions.add(view.findViewById(R.id.saveOption))
        dialogOptions.add(view.findViewById(R.id.reverseOption))
        dialogOptions.add(view.findViewById(R.id.copyOption))
        initDialogOptions(moreOptionsDialog, dialogOptions)

        if(isReversed) dialogOptions[1].visibility = View.GONE

        fun clickOption(option: LinearLayout) {
            when(option) {
                dialogOptions[0] -> saveOrder()
                dialogOptions[1] -> reverseOrder()
                dialogOptions[2] -> copyListToClipboard()
            }

            moreOptionsDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        moreOptionsDialog.show()
    }

    private fun reverseOrder() {
        val listId = intent.getIntExtra(listIdRef, SENTINEL)
        val listToReorder = intent.getStringExtra(listToReorderRef)

        val reorderPage = Intent(this, ReorderListActivity::class.java)
        reorderPage.putExtra(listTypeRef, listType)
        reorderPage.putExtra(listIdRef, listId)
        if(listToReorder != null)
            reorderPage.putExtra(listToReorderRef, listToReorder)
        reorderPage.putExtra(orderReversedRef, true)
        startActivityForResult(reorderPage, REORDER_PAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REORDER_PAGE_CODE) {
            val listReordered = data?.getBooleanExtra(
                listReorderedRef, false)?: false
            val intentData = Intent()
            intentData.putExtra(listReorderedRef, listReordered)
            setResult(Activity.RESULT_OK, intentData)
            finish()
        }
    }

    private fun copyListToClipboard() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("note", getListAsString())
        clipboard.setPrimaryClip(clip)
        displayToast(getString(R.string.listCopiedToClipboardMessage))
    }

    override fun onBackPressed() {
        if(originalOrder != stringListToReorder.toString())
            showConfirmCloseDialog()
        else super.onBackPressed()
    }
}
