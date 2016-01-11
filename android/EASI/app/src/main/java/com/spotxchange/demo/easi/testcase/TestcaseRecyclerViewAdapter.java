package com.spotxchange.demo.easi.testcase;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spotxchange.demo.easi.R;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Testcase} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class TestcaseRecyclerViewAdapter extends RecyclerView.Adapter<TestcaseRecyclerViewAdapter.ViewHolder> {

    private final List<Testcase> _values = new ArrayList<>();
    private final OnListFragmentInteractionListener _listener;

    public TestcaseRecyclerViewAdapter(OnListFragmentInteractionListener listener) {
        _listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_testcase, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = _values.get(position);
        holder.mIdView.setText(String.valueOf(position));
        holder.mContentView.setText(_values.get(position).scriptlet);

        if (_values.get(position).state == Testcase.PASSED)
        {
            holder.mContentView.setBackgroundResource(R.color.successful);
        } else if (_values.get(position).state == Testcase.FAILED)
        {
            holder.mContentView.setBackgroundResource(R.color.failed);
        }
        else {
            holder.mContentView.setBackgroundResource(R.color.neutral);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != _listener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    _listener.onListFragmentInteraction(position, holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return _values.size();
    }

    public void addTestcases(List<Testcase> newCases)
    {
        int currentPos = _values.size();

        _values.addAll(newCases);
        notifyItemRangeInserted(currentPos, newCases.size());
    }

    public void markTestCaseResult(int position, int result)
    {
        _values.get(position).state = result;
        notifyItemChanged(position);
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
