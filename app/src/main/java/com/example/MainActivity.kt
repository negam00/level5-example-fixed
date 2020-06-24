package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.model.Reminder
import com.example.reminder.*
import com.example.vm.MainActivityViewModel
import androidx.lifecycle.Observer
import com.example.adapter.ReminderAdapter

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    private val reminders = arrayListOf<Reminder>()
    private val reminderAdapter = ReminderAdapter(reminders)
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        setTitle(R.string.app_name)

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        // Initialize the recycler view with a linear layout manager, adapter
        rvReminders.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvReminders.adapter = reminderAdapter

        rvReminders.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        createItemTouchHelper().attachToRecyclerView(rvReminders)

        fab.setOnClickListener { startAddActivity() }
    }

    private fun observeViewModel() {
        viewModel.reminders.observe(this, Observer { reminders ->
            this@MainActivity.reminders.clear()
            this@MainActivity.reminders.addAll(reminders)
            reminderAdapter.notifyDataSetChanged()
        })
    }

    private fun startAddActivity() {
        val intent = Intent(this, AddReminderActivity::class.java)
        startActivityForResult(intent,
            ADD_REMINDER_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ADD_REMINDER_REQUEST_CODE -> {
                    data?.let {safeData ->
                        val reminder = safeData.getParcelableExtra<Reminder>(EXTRA_REMINDER)
                        reminder?.let { safeReminder ->
                            viewModel.insertReminder(safeReminder)
                        } ?: run {
                            Log.e("MainActivity", "Reminder is null")
                        }
                    } ?: run { Log.e("MainActivity", "Empty intent daata receivd") }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Create a touch helper to recognize when a user swipes an item from a recycler view.
     * An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
     * and uses callbacks to signal when a user is performing these actions.
     */
    private fun createItemTouchHelper(): ItemTouchHelper {

        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            // Delete reminder once user swipes left
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val reminderToDelete = reminders[position]

                viewModel.deleteReminder(reminderToDelete)
            }
        }
        return ItemTouchHelper(callback)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        const val ADD_REMINDER_REQUEST_CODE = 100
    }
}
