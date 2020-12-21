package app.editors.manager.ui.fragments.operations;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class DocsOperationSectionFragment extends BaseAppFragment {

    public static final String TAG = DocsOperationSectionFragment.class.getSimpleName();

    private Unbinder mUnbinder;
    @BindView(R.id.operation_sections_my)
    protected LinearLayout mOperationSectionsMy;
    @BindView(R.id.operation_sections_share)
    protected LinearLayout mOperationSectionsShare;
    @BindView(R.id.operation_sections_common)
    protected LinearLayout mOperationSectionsCommon;
    @BindView(R.id.operation_sections_projects)
    protected LinearLayout mOperationSectionsProjects;

    @Inject
    protected PreferenceTool mPreferenceTool;

    public static DocsOperationSectionFragment newInstance() {
        return new DocsOperationSectionFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_operation_section, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        if (mPreferenceTool.isProjectDisable()) {
            mOperationSectionsProjects.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @OnClick({R.id.operation_sections_my,
            R.id.operation_sections_share,
            R.id.operation_sections_common,
            R.id.operation_sections_projects})
    public void onSectionClick(View view) {
        int sectionType = Api.SectionType.CLOUD_USER;
        switch (view.getId()) {
            case R.id.operation_sections_my:
                sectionType = Api.SectionType.CLOUD_USER;
                break;
            case R.id.operation_sections_share:
                sectionType = Api.SectionType.CLOUD_SHARE;
                break;
            case R.id.operation_sections_common:
                sectionType = Api.SectionType.CLOUD_COMMON;
                break;
            case R.id.operation_sections_projects:
                sectionType = Api.SectionType.CLOUD_PROJECTS;
                break;
        }

        showFragment(DocsCloudOperationFragment.newInstance(sectionType),
                DocsCloudOperationFragment.TAG, false);
    }

    private void init(final Bundle savedInstanceState) {
        setActionBarTitle(getString(R.string.operation_choose_section));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (mPreferenceTool.getIsVisitor()) {
            mOperationSectionsMy.setVisibility(View.GONE);
        }
    }

}
