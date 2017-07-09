package in.codingninjas.todoapp;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by nsbhasin on 09/07/17.
 */

public class TodoRecyclerView extends RecyclerView {
    private View emptyView;

    public TodoRecyclerView(Context context) {
        super(context);
    }

    public TodoRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TodoRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            showEmptyView();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            showEmptyView();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            showEmptyView();
        }
    };

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        super.setAdapter(adapter);

        if (adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
            emptyObserver.onChanged();
        }
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }

    public void showEmptyView() {
        Adapter<?> adapter = getAdapter();
        if (adapter != null && emptyView != null) {
            if (adapter.getItemCount() == 0) {
                emptyView.setVisibility(View.VISIBLE);
                TodoRecyclerView.this.setVisibility(View.GONE);
            }  else {
                emptyView.setVisibility(View.GONE);
                TodoRecyclerView.this.setVisibility(View.VISIBLE);
            }
        }
    }
}
