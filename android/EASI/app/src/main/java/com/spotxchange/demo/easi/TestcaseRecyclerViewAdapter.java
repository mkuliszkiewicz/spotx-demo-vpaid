package com.spotxchange.demo.easi;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spotxchange.demo.easi.TestcaseListFragment.OnListFragmentInteractionListener;
import com.spotxchange.demo.easi.testcases.Testcase;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Testcase} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class TestcaseRecyclerViewAdapter extends RecyclerView.Adapter<TestcaseRecyclerViewAdapter.ViewHolder> {

    private final List<Testcase> mValues = new ArrayList<>();
    private final OnListFragmentInteractionListener mListener;

    public TestcaseRecyclerViewAdapter(OnListFragmentInteractionListener listener) {
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_testcase, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(String.valueOf(position));
        holder.mContentView.setText(mValues.get(position).scriptlet);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void addTestcases(List<Testcase> newCases)
    {
        int currentPos = mValues.size();

        mValues.addAll(newCases);
        notifyItemRangeInserted(currentPos, newCases.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Testcase mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.scriptlet);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
