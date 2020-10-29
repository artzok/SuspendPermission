package com.artzok.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CompletableDeferred

suspend fun FragmentActivity.requestPermissions(permissions: List<String>): IntArray {
    val granted = PackageManager.PERMISSION_GRANTED
    if (permissions.map {
            ActivityCompat.checkSelfPermission(this, it)
        }.all {
            it == granted
        }) {
        return IntArray(permissions.size) { granted }
    }

    val tag = PermissionFragment.FRAGMENT_TAG
    var fragment = supportFragmentManager.findFragmentByTag(tag)
    if (fragment !is PermissionFragment) {
        fragment = PermissionFragment()
        supportFragmentManager
            .beginTransaction()
            .add(fragment, tag)
            .commitAllowingStateLoss()
    }
    val def = CompletableDeferred<IntArray>()
    fragment.requestPermissions(def, permissions)
    return def.await()
}

suspend fun Fragment.requestPermissions(permissions: List<String>): IntArray? {
    return activity?.requestPermissions(permissions)
}

open class PermissionFragment : Fragment() {

    companion object {
        const val FRAGMENT_TAG = "com.artzok.permission.fragment.tag"
    }

    private var requestCode = 10
    private val readyPermissionsArray = SparseArray<List<String>>()
    private val readyDeferredArray = SparseArray<CompletableDeferred<IntArray>>()

    private var requestingRequestCode: Int = 0
    private var requestingDeferred: CompletableDeferred<IntArray>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        tryRequestPermission()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        tryRequestPermission()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tryRequestPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancelAllRequests()
    }

    override fun onDetach() {
        super.onDetach()
        cancelAllRequests()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAllRequests()
    }

    fun requestPermissions(deferred: CompletableDeferred<IntArray>, permissions: List<String>) {
        // check current has pending
        repeat(readyPermissionsArray.size()) {
            val value = readyPermissionsArray[readyPermissionsArray.keyAt(it)]
            if (value == permissions) return
        }

        readyDeferredArray.put(++requestCode, deferred)
        readyPermissionsArray.put(requestCode, permissions)
        tryRequestPermission()
    }

    private fun tryRequestPermission() {
        if (!isRemoving && activity != null &&
            !isDetached && isAdded && requestingDeferred == null
        ) {
            val requestCode = readyPermissionsArray.keyAt(0)
            val def = readyDeferredArray[requestCode]
            val permissions = readyPermissionsArray[requestCode]

            if (def != null && permissions != null) {
                requestingDeferred = def
                requestingRequestCode = requestCode
                readyDeferredArray.remove(requestCode)
                readyPermissionsArray.remove(requestCode)
                requestPermissions(permissions.toTypedArray(), requestCode)
            }
        }
    }

    private fun cancelAllRequests() {
        requestingDeferred?.cancel()
        requestingRequestCode = 0
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        val def = requestingDeferred
        if (requestingRequestCode == requestCode && def != null) {
            def.complete(grantResults)
            requestingDeferred = null
            requestingRequestCode = 0
            tryRequestPermission()
        }
    }
}