package com.graphonic.echoapp

import android.content.Context
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class EchoSpeechModuleWrapper {

    val MODULE_NAME = "echo-speech-module";

    private var py: Python
    private var module: PyObject

    constructor(context: Context) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }

        py = Python.getInstance()
        module = py.getModule(MODULE_NAME)
    }

    fun help() {
        val result = module.callAttr("main", "--help")
    }

}