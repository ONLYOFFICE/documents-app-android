package app.editors.manager.viewModels.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.viewModels.main.SetPasscodeViewModel

class SetPasscodeViewModelFactory(val preferenceTool: PreferenceTool): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SetPasscodeViewModel(preferenceTool) as T
    }
}