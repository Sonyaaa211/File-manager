package com.example.file_manager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter

    private var currentDirectory: File = Environment.getExternalStorageDirectory()
    private val directoryStack = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (isStoragePermissionGranted()) {
            displayFiles(currentDirectory)
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
                false
            } else {
                true
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
                false
            } else {
                true
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayFiles(currentDirectory)
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun displayFiles(directory: File) {
        currentDirectory = directory
        val files = directory.listFiles()
        if (files != null) {
            fileAdapter = FileAdapter(files.toList()) { file ->
                if (file.isDirectory) {
                    directoryStack.add(currentDirectory)
                    displayFiles(file)
                } else if (file.isFile && file.extension in listOf("txt", "log", "csv", "json", "xml", "html", "md"))  {
                    openFile(file)
                } else {
                    Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show()
                }
            }
            recyclerView.adapter = fileAdapter
        }
    }

    override fun onBackPressed() {
        if (directoryStack.isNotEmpty()) {
            val previousDirectory = directoryStack.removeAt(directoryStack.lastIndex)
            displayFiles(previousDirectory)
        } else {
            super.onBackPressed()
        }
    }

    private fun openFile(file: File) {
        try {
            // Kiểm tra file có tồn tại và có thể đọc được không
            if (file.exists() && file.canRead()) {
                val fileContent = file.readText() // Đọc nội dung file bằng cách sử dụng readText()

                // Tạo intent để mở activity hiển thị nội dung file
                val intent = FileContentActivity.createIntent(this, file.name, fileContent)
                startActivity(intent)
            } else {
                Toast.makeText(this, "File không thể đọc được hoặc không tồn tại", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // In ra thông báo chi tiết lỗi để dễ dàng debug
            e.printStackTrace()
            Toast.makeText(this, "Không thể mở file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                displayFiles(currentDirectory)
            }
        }
    }

}