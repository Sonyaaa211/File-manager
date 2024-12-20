package com.example.file_manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class FileContentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_content)

        val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: ""
        val fileContent = intent.getStringExtra(EXTRA_FILE_CONTENT) ?: ""

        val titleTextView: TextView = findViewById(R.id.titleTextView)
        val contentTextView: TextView = findViewById(R.id.contentTextView)

        titleTextView.text = fileName
        contentTextView.text = fileContent
    }

    companion object {
        private const val EXTRA_FILE_NAME = "file_name"
        private const val EXTRA_FILE_CONTENT = "file_content"

        fun createIntent(context: Context, fileName: String, fileContent: String): Intent {
            return Intent(context, FileContentActivity::class.java).apply {
                putExtra(EXTRA_FILE_NAME, fileName)
                putExtra(EXTRA_FILE_CONTENT, fileContent)
            }
        }
    }
}