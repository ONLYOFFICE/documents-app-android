package app.editors.manager.viewModels.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.documents.core.model.cloud.Recent

class RecentViewModel(context: Application): AndroidViewModel(context) {

    // TODO: recent datasource
    val isRecent: LiveData<List<Recent>> = MutableLiveData(emptyList())
}