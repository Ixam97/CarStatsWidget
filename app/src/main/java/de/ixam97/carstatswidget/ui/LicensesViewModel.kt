package de.ixam97.carstatswidget.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext

class LicensesViewModel(application: Application) : AndroidViewModel(application = application) {
    val applicationContext = application

    val libs = Libs.Builder()
        .withContext(applicationContext)
        .build()

    // val librariesList = mutableListOf<(() -> Unit)>()

}