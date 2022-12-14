package app.editors.manager.ui.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.databinding.FragmentStorageWebDavBinding
import app.editors.manager.mvp.views.base.BaseView
import app.editors.manager.ui.interfaces.WebDavInterface
import app.editors.manager.ui.views.edits.BaseWatcher
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

abstract class WebDavBaseFragment : BaseAppFragment(), BaseView {

    protected var viewBinding: FragmentStorageWebDavBinding? = null
    protected var parentActivity: WebDavInterface? = null
    protected var webDavProvider: WebDavApi.Providers? = null

    private var textWatcher: FieldsWatcher? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentStorageWebDavBinding.inflate(layoutInflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        parentActivity?.showConnectButton(false)
        viewBinding = null
        parentActivity = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (parentFragmentManager.fragments.size > 1) {
                parentFragmentManager.popBackStack()
            } else {
                onBackPressed()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onError(message: String?) {
        hideDialog()
        viewBinding?.let { binding ->
            binding.storageWebDavLoginLayout.error()
            when (webDavProvider) {
                WebDavApi.Providers.NextCloud -> onServerError()
                WebDavApi.Providers.KDrive -> {
                    binding.storageWebDavPasswordLayout.error(R.string.errors_webdav_username_password)
                }
                else -> {
                    if (binding.storageWebDavServerLayout.isVisible) {
                        binding.storageWebDavServerLayout.error()
                        binding.storageWebDavPasswordLayout.error(R.string.errors_webdav_sign_in)
                    } else {
                        binding.storageWebDavPasswordLayout.error(R.string.errors_webdav_username_password)
                    }
                }
            }
        }
    }

    protected fun TextInputEditText.setActionDoneListener(listener: () -> Unit) {
        imeOptions = EditorInfo.IME_ACTION_DONE
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && !text.isNullOrEmpty()) {
                listener()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun TextInputLayout?.error(resId: Int? = null) {
        this?.error = resId?.let(requireContext()::getString) ?: " "
    }

    fun onServerError() {
        viewBinding?.storageWebDavServerLayout?.error(R.string.errors_webdav_server)
    }

    open fun initViews(isNextCloud: Boolean = false) {
        textWatcher = FieldsWatcher(isNextCloud)
        parentActivity?.showConnectButton(true)
        parentActivity?.enableConnectButton(false)
        viewBinding?.let { binding ->
            val errorTextAppearance = lib.toolkit.base.R.style.TextInputEmptyErrorMessage
            binding.storageWebDavLoginEdit.addTextChangedListener(textWatcher)
            binding.storageWebDavPasswordEdit.addTextChangedListener(textWatcher)
            binding.storageWebDavServerEdit.addTextChangedListener(textWatcher)
            binding.storageWebDavLoginLayout.setErrorTextAppearance(errorTextAppearance)
            if (!isNextCloud) binding.storageWebDavServerLayout.setErrorTextAppearance(errorTextAppearance)
        }
    }

    inner class FieldsWatcher(private val isNextCloud: Boolean) : BaseWatcher() {
        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            viewBinding?.let {
                it.storageWebDavServerLayout.error = null
                it.storageWebDavLoginLayout.error = null
                it.storageWebDavPasswordLayout.error = null
                parentActivity?.enableConnectButton(it.checkFields())
            }
        }

        private fun FragmentStorageWebDavBinding.checkFields(): Boolean {
            return isNextCloud && storageWebDavServerEdit.isNotEmpty() ||
                    storageWebDavLoginEdit.isNotEmpty() &&
                    storageWebDavPasswordEdit.isNotEmpty() &&
                    storageWebDavServerEdit.isNotEmpty()
        }

        private fun TextInputEditText.isNotEmpty(): Boolean = text?.trim()?.isNotEmpty() == true
    }
}
