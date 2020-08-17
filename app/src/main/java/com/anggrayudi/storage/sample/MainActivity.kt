package com.anggrayudi.storage.sample

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.afollestad.materialdialogs.MaterialDialog
import com.anggrayudi.storage.DocumentFileCompat
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.StorageType
import com.anggrayudi.storage.callback.StoragePermissionCallback
import com.anggrayudi.storage.extension.storageId
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var storage: SimpleStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupSimpleStorage()

        btnRequestStoragePermission.setOnClickListener {
            Dexter.withContext(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (!report.areAllPermissionsGranted()) {
                            Toast.makeText(this@MainActivity, "Please grant storage permissions", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                        // no-op
                    }
                }).check()
        }

        btnSelectFolder.setOnClickListener {
//            startActivityForResult(storage.requireExternalStorageRootAccess(), 100)
        }
    }

    private fun setupSimpleStorage() {
        storage = SimpleStorage(this)
        storage.storageAccessCallback = object : StoragePermissionCallback {
            override fun onRootPathNotSelected(rootPath: String) {
                MaterialDialog(this@MainActivity)
                    .message(text = "Please select $rootPath")
                    .negativeButton(android.R.string.cancel)
                    .positiveButton {
                        val initialRoot = if (rootPath == DocumentFileCompat.PRIMARY) StorageType.EXTERNAL else StorageType.SD_CARD
                        storage.requestStorageAccess(REQUEST_CODE_STORAGE_ACCESS, initialRoot)
                    }
                    .show()
            }

            override fun onStoragePermissionDenied() {
                Dexter.withContext(this@MainActivity)
                    .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            if (report.areAllPermissionsGranted()) {
                                storage.requestStorageAccess(REQUEST_CODE_STORAGE_ACCESS)
                            } else {
                                Toast.makeText(baseContext, "Please grant storage permissions", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                            // no-op
                        }
                    }).check()
            }

            override fun onRootPathPermissionGranted(root: DocumentFile) {
                Toast.makeText(baseContext, "Storage access has been granted for ${root.storageId}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        storage.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        storage.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        storage.onRestoreInstanceState(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        btnSelectFolder.isEnabled = SimpleStorage.hasStoragePermission(this)
    }

    companion object {
        const val REQUEST_CODE_STORAGE_ACCESS = 1
    }
}