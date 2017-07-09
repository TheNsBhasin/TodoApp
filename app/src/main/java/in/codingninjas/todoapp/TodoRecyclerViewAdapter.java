package in.codingninjas.todoapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import static in.codingninjas.todoapp.IntentConstants.REQUEST_ID_TODO_ITEM;
import static in.codingninjas.todoapp.IntentConstants.TODO_ITEM;
import static in.codingninjas.todoapp.MainActivity.todoCoordinatorLayout;

/**
 * Created by nsbhasin on 09/07/17.
 */

class TodoRecyclerViewAdapter extends RecyclerView.Adapter<TodoRecyclerViewAdapter.TodoViewHolder> implements ItemTouchHelperClass.ItemTouchHelperAdapter {
    private LayoutInflater layoutInflater;
    private ArrayList<Todo> todoArrayList = new ArrayList<>();
    private TodoOpenHelper todoOpenHelper;
    private Todo mJustDeletedTodoItem;
    private int mIndexOfDeletedTodoItem;
    private AlarmClass alarmClass;
    private Context context;

    TodoRecyclerViewAdapter(Context context, ArrayList<Todo> todoArrayList) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.todoArrayList = todoArrayList;
        alarmClass = AlarmClass.getInstance(context);
    }

    @Override
    public TodoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.list_item, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TodoViewHolder holder, int position) {
        Todo todo = todoArrayList.get(position);
        holder.todoTitleTextView.setText(todo.getTitle());
        holder.todoCategoryTextView.setText(todo.getCategory());
        if (todo.getDate() != null) {
            String timeToShow;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.getDefault());
            timeToShow = simpleDateFormat.format(todo.getDate());
            holder.todoDateTextView.setText(timeToShow);
        } else {
            holder.todoDateTextView.setText("No due date");
        }
    }

    @Override
    public int getItemCount() {
        return todoArrayList.size();
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(todoArrayList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(todoArrayList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemRemoved(int position) {
        mJustDeletedTodoItem = todoArrayList.remove(position);
        long mIdOfDeletedTodoItem = mJustDeletedTodoItem.getId();
        mIndexOfDeletedTodoItem = position;
        Intent i = new Intent(context,AlarmReceiver.class);
        alarmClass.deleteAlarm(i, (int)mJustDeletedTodoItem.getId());
        notifyItemRemoved(position);

        todoOpenHelper = TodoOpenHelper.getInstance(context);
        todoOpenHelper.deleteTodo(mIdOfDeletedTodoItem);

        String toShow = "Todo";
        Snackbar.make(todoCoordinatorLayout , "Deleted " + toShow, Snackbar.LENGTH_SHORT)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        todoArrayList.add(mIndexOfDeletedTodoItem, mJustDeletedTodoItem);
                        todoOpenHelper.addDeletedTodo(mJustDeletedTodoItem);
                        if (mJustDeletedTodoItem.getDate() != null && mJustDeletedTodoItem.hasReminder()) {
                            Intent i = new Intent(context, AlarmReceiver.class);
                            i.putExtra(TODO_ITEM, mJustDeletedTodoItem);
                            alarmClass.createAlarm(i, (int)mJustDeletedTodoItem.getId(), mJustDeletedTodoItem.getDate().getTime());
                        }
                        notifyItemInserted(mIndexOfDeletedTodoItem);
                    }
                }).show();
    }

    class TodoViewHolder extends TodoRecyclerView.ViewHolder {
        TextView todoTitleTextView;
        TextView todoDateTextView;
        TextView todoCategoryTextView;

        TodoViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Todo todo = todoArrayList.get(TodoViewHolder.this.getAdapterPosition());
                    Intent intent = new Intent(context, AddTodoActivity.class);
                    intent.putExtra(TODO_ITEM, todo);
                    ((Activity)context).startActivityForResult(intent, REQUEST_ID_TODO_ITEM);
                }
            });

            todoTitleTextView = itemView.findViewById(R.id.todoTitleTextView);
            todoDateTextView = itemView.findViewById(R.id.todoDateTextView);
            todoCategoryTextView = itemView.findViewById(R.id.todoCategoryTextView);
        }
    }


}
