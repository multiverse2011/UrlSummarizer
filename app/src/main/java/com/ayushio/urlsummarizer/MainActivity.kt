package com.ayushio.urlsummarizer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                            item.content + " deleted!",
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

    private fun createDummyData() {
        for (i in 0..10) {
            create("やること")
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
        return realm.where(Task::class.java).findAll().sort("createdAt", Sort.ASCENDING)
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