package app.editors.manager.viewModels.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import app.documents.core.storage.recent.Recent
import app.editors.manager.app.appComponent

class RecentViewModel(context: Application): AndroidViewModel(context) {

    val isRecent: LiveData<List<Recent>> = checkNotNull(context.appComponent.recentDao?.getRecentsLiveData())
}