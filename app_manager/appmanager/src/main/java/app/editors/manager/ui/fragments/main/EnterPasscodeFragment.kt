package app.editors.manager.ui.fragments.main

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.compose.fragments.main.PasscodeOperationCompose
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.viewModels.main.PasscodeLockState
import app.editors.manager.viewModels.main.SetPasscodeViewModel

class EnterPasscodeFragment: BaseAppFragment() {

    companion object {
        val TAG = EnterPasscodeFragment::class.java.simpleName


        fun newInstance(): EnterPasscodeFragment = EnterPasscodeFragment()
    }

    private val viewModel by viewModels<SetPasscodeViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireContext().appComponent.inject(viewModel)
    }


    @ExperimentalFoundationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setActionBarTitle(getString(R.string.app_settings_passcode))
        return ComposeView(requireContext()).apply {
            setContent {
                PasscodeOperationCompose.PasscodeOperation(viewModel,"Enter passcode to unlock app", "", { codeDigit ->
                    viewModel.checkConfirmPasscode(codeDigit.toString(), "Incorrect passcode entered")
                }, {
                    viewModel.codeBackspace()
                })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        viewModel.getData()
    }

    private fun init() {
        viewModel.passcodeLockState.observe(viewLifecycleOwner) { state ->
            when(state) {

                is PasscodeLockState.ConfirmPasscode -> {
                    MainActivity.show(requireContext())
                    requireActivity().finish()
                }
                is PasscodeLockState.Error -> {
                    Handler(Looper.getMainLooper()).postDelayed( {
                        showFragment(newInstance(), TAG, false)
                    }, 1500)
                }

            }
        }
    }

}