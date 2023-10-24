package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.app.Activity
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


class ReorderListTitlesActivity : AppCompatActivity() {

    private lateinit var database: ListsDatabase

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter

    private lateinit var listIdsToReorder: ArrayList<Int>

    private lateinit var originalOrder: String

    private var stringListToReorder = ArrayList<String>()
    private var selectedListLayout: LinearLayout? = null
    private var isReversed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reorder_list_titles)

        database = ListsDatabase(this)
        isReversed = intent.getBooleanExtra(
            orderReversedRef, false)
        initListToReorder()

        handleItemTouchHelper()

        initButtonAnimationListener(exitButton)
        initButtonAnimationListener(saveButton)
        initButtonAnimationListener(moreOptions)
        exitButton.setOnClickListener { finish() }
        saveButton.setOnClickListener { saveOrder() }
        moreOptions.setOnClickListener { showMoreOptionsDialog() }

        if(isReversed) moreOptions.visibility = View.GONE
    }

    private fun saveOrder() {
        database.updateListTitlesOrder(listIdsToReorder)
        val intentData = Intent()
        intentData.putExtra(listReorderedRef, true)
        setResult(Activity.RESULT_OK, intentData)
        finish()
    }

    private fun reverseList(listIds: ArrayList<Int>) {
        val tempList = ArrayList<Int>()
        for(listId in listIds)
            tempList.add(0, listId)
        listIds.clear()
        for(listId in tempList)
            listIds.add(listId)
    }

    private fun initListToReorder() {
        listIdsToReorder = database.getListIds()
        if(isReversed) reverseList(listIdsToReorder)

        for(listId in listIdsToReorder) {
            val list = database.getList(listId)
            stringListToReorder.add("${list.getTitle()}\t${list.getListType()}")
        }

        originalOrder = if(isReversed)
            "" else stringListToReorder.toString()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerAdapter = RecyclerAdapter(stringListToReorder, this, false)
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
                if(actionState != 0) selectedListLayout =
                    viewHolder?.itemView?.findViewById(R.id.taskLayout)
                selectedListLayout?.let { animateButton(it, actionState != 0) }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                Collections.swap(stringListToReorder, fromPosition, toPosition)
                Collections.swap(listIdsToReorder, fromPosition, toPosition)

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

        dialogOptions[2].visibility = View.GONE

        fun clickOption(option: LinearLayout) {
            when(option) {
                dialogOptions[0] -> saveOrder()
                dialogOptions[1] -> reverseOrder()
            }

            moreOptionsDialog.dismiss()
        }

        for(option in dialogOptions)
            option.setOnClickListener { clickOption(option) }

        moreOptionsDialog.show()
    }

    private fun reverseOrder() {
        val reorderPage = Intent(this, ReorderListTitlesActivity::class.java)
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

    override fun onBackPressed() {
        if(originalOrder != stringListToReorder.toString())
            showConfirmCloseDialog()
        else super.onBackPressed()
    }
}
