package app.editors.manager.ui.fragments.login;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.CountriesCodesTool;
import app.editors.manager.ui.adapters.CountriesCodesAdapter;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.ui.adapters.BaseAdapter;

public class CountriesCodesFragment extends BaseAppFragment implements SearchView.OnQueryTextListener,
        BaseAdapter.OnItemClickListener {

    public static final String TAG = CountriesCodesFragment.class.getSimpleName();

    protected Unbinder mUnbinder;
    @BindView(R.id.fragment_countries_codes_list)
    protected RecyclerView mFragmentCountriesCodesList;

    private SearchView mSearchView;
    private ImageView mCloseButton;
    private MenuItem mSearchItem;
    private CountriesCodesAdapter mCountriesCodesAdapter;

    @Inject
    protected CountriesCodesTool mCountriesCodesTool;

    public static CountriesCodesFragment newInstance() {
        return new CountriesCodesFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_countries_codes, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        init(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.countries_codes, menu);
        mSearchItem = menu.findItem(R.id.menu_countries_search);
        mSearchView = (SearchView) mSearchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setOnCloseListener(() -> {
            if (mSearchView.getQuery().length() > 0) {
                mSearchView.setQuery("", true);
                return true;
            }
            return false;
        });

        mCloseButton = mSearchView.findViewById(androidx.appcompat.R.id.search_close_btn);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mCountriesCodesAdapter.getFilter().filter(newText);
        return false;
    }

    @Override
    public boolean onBackPressed() {
        if (!mSearchView.isIconified()) {
            mSearchView.setQuery("", false);
            mSearchView.setIconified(true);
            return true;
        }

        return super.onBackPressed();
    }

    @Override
    public void onItemClick(View view, int position) {
        final CountriesCodesTool.Codes codes = mCountriesCodesAdapter.getItem(position);
        getFragmentManager().popBackStack(EnterprisePhoneFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getFragmentManager().popBackStack(CountriesCodesFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        showFragment(EnterprisePhoneFragment.newInstance(codes.mNumber, codes.mName, codes.mCode), EnterprisePhoneFragment.TAG, false);
    }

    private void init(final Bundle savedInstanceState) {
        setActionBarTitle(getString(R.string.countries_codes_title));
        mFragmentCountriesCodesList.setLayoutManager(new LinearLayoutManager(getContext()));
        mCountriesCodesAdapter = new CountriesCodesAdapter(getContext());
        mCountriesCodesAdapter.setItems(mCountriesCodesTool.getCodes());
        mCountriesCodesAdapter.setOnItemClickListener(this);
        mFragmentCountriesCodesList.setAdapter(mCountriesCodesAdapter);
    }

}
