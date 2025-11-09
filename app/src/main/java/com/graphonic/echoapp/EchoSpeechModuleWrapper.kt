package com.graphonic.echoapp

import android.content.Context
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class EchoSpeechModuleWrapper {

    val MODULE_NAME = "test";

    private var py: Python
    private var module: PyObject

    constructor(context: Context) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }

        py = Python.getInstance()
        module = py.getModule(MODULE_NAME)
    }

    fun HelloPython() {
        val result = module.callAttr("hello_python")

        Log.d("PYTHON", "$result")
    }

}