package app.editors.manager.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.editors.manager.R;
import app.editors.manager.managers.tools.CountriesCodesTool;
import app.editors.manager.ui.adapters.base.BaseAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CountriesCodesAdapter extends BaseAdapter<CountriesCodesTool.Codes> implements Filterable {

    private final Context mContext;
    private AdapterFilter mAdapterFilter;
    private List<CountriesCodesTool.Codes> mDefaultList;

    public CountriesCodesAdapter(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int typeHolder) {
        final View viewItem = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_countries_codes_item, viewGroup, false);
        return new ViewHolderItem(viewItem);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final ViewHolderItem mViewHolder = (ViewHolderItem) viewHolder;
        final CountriesCodesTool.Codes codesBefore = getItem(position - 1);
        final CountriesCodesTool.Codes codesCurrent = getItem(position);

        mViewHolder.mCountriesCodesCodeText.setText("+" + codesCurrent.mNumber);
        mViewHolder.mCountriesCodesNameText.setText(codesCurrent.mName);

        if (codesBefore == null || (codesBefore != null && codesCurrent.mName.charAt(0) != codesBefore.mName.charAt(0))) {
            mViewHolder.mCountriesCodesAlphaText.setVisibility(View.VISIBLE);
            mViewHolder.mCountriesCodesAlphaText.setText(String.valueOf(codesCurrent.mName.charAt(0)));
        } else {
            mViewHolder.mCountriesCodesAlphaText.setVisibility(View.GONE);
        }
    }

    @Override
    public Filter getFilter() {
        if (mAdapterFilter == null) {
            mDefaultList = new ArrayList<>(mList);
            mAdapterFilter = new AdapterFilter();
        }
        return mAdapterFilter;
    }

    public void setDefault() {
        mList = mDefaultList;
        notifyDataSetChanged();
    }

    protected class ViewHolderItem extends RecyclerView.ViewHolder {

        @BindView(R.id.countries_codes_alpha_text)
        TextView mCountriesCodesAlphaText;
        @BindView(R.id.countries_codes_name_text)
        TextView mCountriesCodesNameText;
        @BindView(R.id.countries_codes_code_text)
        TextView mCountriesCodesCodeText;

        public ViewHolderItem(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, getLayoutPosition());
                }
            });
        }

    }

    private class AdapterFilter extends Filter {

        private final FilterResults mResults;
        private final List<CountriesCodesTool.Codes> mFilteredList;

        public AdapterFilter() {
            mResults = new FilterResults();
            mFilteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            mList = mDefaultList;
            mResults.count = 0;
            mResults.values = null;
            mFilteredList.clear();

            final String upperSymbols = constraint.toString().toUpperCase();
            for (int i = 0; i < mList.size(); i++) {
                final CountriesCodesTool.Codes codes = mList.get(i);
                if (codes.mName.toUpperCase().startsWith(upperSymbols))  {
                    mFilteredList.add(codes);
                }
            }

            mResults.count = mFilteredList.size();
            mResults.values = mFilteredList;
            return mResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (List<CountriesCodesTool.Codes>) results.values;
            notifyDataSetChanged();
        }

    }

}
