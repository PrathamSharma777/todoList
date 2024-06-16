package com.example.todolist

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: TodoDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = TodoDatabaseHelper(this)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(this, taskList)
        recyclerView.adapter = taskAdapter

        fab = findViewById(R.id.fab)
        fab.setOnClickListener { showAddTaskDialog() }

        loadTasks()
    }

    private fun loadTasks() {
        taskList.clear()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            TodoDatabaseHelper.TABLE_TASKS,
            null, null, null, null, null,
            "${TodoDatabaseHelper.COLUMN_TIMESTAMP} DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_ID))
                val taskName = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TASK_NAME))
                val timestamp = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TIMESTAMP))
                taskList.add(Task(id, taskName, timestamp))
            } while (cursor.moveToNext())
        }
        cursor.close()
        taskAdapter.notifyDataSetChanged()
    }

    private fun showAddTaskDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Task")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Add") { dialog, _ ->
            val taskName = input.text.toString().trim()
            if (taskName.isNotEmpty()) {
                addTask(taskName)
            } else {
                Toast.makeText(this, "Task cannot be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun addTask(taskName: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(TodoDatabaseHelper.COLUMN_TASK_NAME, taskName)
        }
        val id = db.insert(TodoDatabaseHelper.TABLE_TASKS, null, values)

        if (id != -1L) {
            taskList.add(Task(id, taskName, null))
            taskAdapter.notifyDataSetChanged()
        }
    }
}
