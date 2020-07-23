package com.ayushio.urlsummarizer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import java.util.*

class ReceiveActivity : AppCompatActivity(){
    private val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val url = when (intent.action) {
            Intent.ACTION_SEND -> {
                intent.extras?.getCharSequence(Intent.EXTRA_TEXT)
            }
            else -> null
        }


        create(url as String)

        Toast.makeText(
            applicationContext,
            url as String + "\nAdded!",
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }


    private fun create(content: String) {
        realm.executeTransaction {
            val task = it.createObject(Task::class.java, UUID.randomUUID().toString())
            task.content = content
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}