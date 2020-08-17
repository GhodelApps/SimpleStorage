package com.anggrayudi.storage.extension

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * Created on 17/08/20
 * @author Anggrayudi H
 */
fun Context.getAppDirectory(type: String? = null) = "${getExternalFilesDir(type)}"

fun Intent?.hasActivityHandler(context: Context) =
    this?.resolveActivity(context.packageManager) != null

fun Context.startActivitySafely(intent: Intent) {
    if (intent.hasActivityHandler(this)) {
        startActivity(intent)
    }
}

fun Activity.startActivityForResultSafely(requestCode: Int, intent: Intent) {
    if (intent.hasActivityHandler(this)) {
        startActivityForResult(intent, requestCode)
    }
}