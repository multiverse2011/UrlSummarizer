package com.ayushio.urlsummarizer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.text.StringBuilder

class MainActivity : AppCompatActivity() {

    private val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        val taskList = readAll()

        val adapter =
            TaskAdapter(
                this, taskList,
                object : TaskAdapter.OnItemClickListener {
                    override fun onItemClick(item: Task) {
                    }
                },
                object : TaskAdapter.OnItemLongClickListener {
                    override fun onItemLongClick(item: Task) {
                        Toast.makeText(
                            applicationContext,
                            item.content + "\ndeleted!",
                            Toast.LENGTH_SHORT
                        ).show()
                        delete(item.id)
                    }
                }, true
            )

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_export -> {
            val realmResults = exportAll()
            val urlBuilder = StringBuilder()
            for (i in realmResults) {
                urlBuilder.append(i.content)
                urlBuilder.append("\n")
            }
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("URL list", urlBuilder.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                applicationContext,
                "Urls are copied!",
                Toast.LENGTH_SHORT
            ).show()

            true
        }
        R.id.action_clear_all -> {
            AlertDialog.Builder(this) // FragmentではActivityを取得して生成
                .setTitle("Notice")
                .setMessage("Are you sure to delete all data?")
                .setPositiveButton("OK") { dialog, which ->
                    deleteAll()
                }
                .setNegativeButton("No") { dialog, which ->

                }
                .show()
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }


    private fun createDummyData() {
        for (i in 0..10) {
            create("やること $i")
        }
    }

    private fun update(id: String, content: String) {
        realm.executeTransaction {
            val task = realm.where(Task::class.java).equalTo("id", id).findFirst()
                ?: return@executeTransaction
            task.content = content
        }
    }

    private fun create(content: String) {
        realm.executeTransaction {
            val task = it.createObject(Task::class.java, UUID.randomUUID().toString())
            task.content = content
        }
    }

    private fun readAll(): RealmResults<Task> {
        return realm.where(Task::class.java).findAll().sort("createdAt", Sort.DESCENDING)
    }

    private fun exportAll(): RealmResults<Task> {
        return realm.where(Task::class.java).distinct("content").findAll()
    }

    private fun delete(id: String) {
        realm.executeTransaction {
            val task = realm.where(Task::class.java).equalTo("id", id).findFirst()
                ?: return@executeTransaction
            task.deleteFromRealm()
        }
    }

    private fun deleteAll() {
        realm.executeTransaction {
            realm.deleteAll()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }


}