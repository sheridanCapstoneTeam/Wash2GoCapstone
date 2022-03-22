package project.sheridancollege.wash2goproject.util

import android.view.View
import android.widget.Button

object ExtensionFunctions {
    fun View.show(){
        this.visibility = View.VISIBLE
    }

    fun View.hide(){
        this.visibility = View.INVISIBLE
    }

    fun Button.enable(){
        this.isEnabled = true
    }

    fun Button.disabled(){
        this.isEnabled = false
    }
}