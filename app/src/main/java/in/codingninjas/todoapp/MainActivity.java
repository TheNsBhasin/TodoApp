package in.codingninjas.todoapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateUtils;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static in.codingninjas.todoapp.IntentConstants.REQUEST_ID_TODO_ITEM;
import static in.codingninjas.todoapp.TodoOpenHelper.TODO_CATEGORY;
import static in.codingninjas.todoapp.TodoOpenHelper.TODO_DATE;
import static in.codingninjas.todoapp.TodoOpenHelper.TODO_LABEL;
import static in.codingninjas.todoapp.TodoOpenHelper.TODO_PRIORITY;
import static in.codingninjas.todoapp.TodoOpenHelper.TODO_REMINDER;
import static in.codingninjas.todoapp.TodoOpenHelper.TODO_TITLE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ArrayList<Todo> todoArrayList = new ArrayList<>();
    private TodoRecyclerViewAdapter todoRecyclerViewAdapter;
    public static CoordinatorLayout todoCoordinatorLayout;
    private TodoOpenHelper todoOpenHelper;
    private AlarmClass alarmClass;
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        todoOpenHelper = TodoOpenHelper.getInstance(MainActivity.this);
        alarmClass = AlarmClass.getInstance(MainActivity.this);
        todoCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.todoCoordinatorLayout);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
                Todo todo = new Todo("", "", null, -1, "", false);
                intent.putExtra(IntentConstants.TODO_ITEM, todo);
                startActivityForResult(intent, REQUEST_ID_TODO_ITEM);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);

        todoRecyclerViewAdapter = new TodoRecyclerViewAdapter(MainActivity.this, todoArrayList);
        TodoRecyclerView recyclerView = (TodoRecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setEmptyView(findViewById(R.id.todoEmptyView));
        ItemTouchHelper.Callback callback = new ItemTouchHelperClass(todoRecyclerViewAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this));
        updateTodoList();
        setAlarm();
        recyclerView.setAdapter(todoRecyclerViewAdapter);
    }

    private void updateTodoList() {
        todoArrayList.clear();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM, yyyy h:mm a", Locale.getDefault());

        SQLiteDatabase database = todoOpenHelper.getReadableDatabase();
        Cursor cursor = database.query(TodoOpenHelper.TODO_TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {

            long id = cursor.getLong(cursor.getColumnIndex(TodoOpenHelper.TODO_ID));
            String todoTitle = cursor.getString(cursor.getColumnIndex(TODO_TITLE));
            String dateString = cursor.getString(cursor.getColumnIndex(TODO_DATE));
            int reminder = cursor.getInt(cursor.getColumnIndex(TODO_REMINDER));
            boolean hasReminder = (reminder == 1);
            Date todoDate = null;
            if (dateString != null) {
                try {
                    todoDate = simpleDateFormat.parse(dateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            String todoCategory = cursor.getString(cursor.getColumnIndex(TODO_CATEGORY));
            int todoPriority = cursor.getInt(cursor.getColumnIndex(TODO_PRIORITY));
            String todoLabel = cursor.getString(cursor.getColumnIndex(TODO_LABEL));
            Todo todo = new Todo(id, todoTitle, todoCategory, todoDate, todoPriority, todoLabel, hasReminder);
            todoArrayList.add(todo);
        }
        cursor.close();
        todoRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void setAlarm() {
        if (todoArrayList != null) {
            for (Todo todo : todoArrayList) {
                if (todo.hasReminder() && todo.getDate() != null) {
                    if (todo.getDate().before(new Date())) {
                        todo.setDate(null);
                        continue;
                    }
                    Intent i = new Intent(MainActivity.this, AlarmReceiver.class);
                    i.putExtra(IntentConstants.TODO_ITEM, todo);
                    alarmClass.createAlarm(i, (int) todo.getId(), todo.getDate().getTime());
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED && requestCode == REQUEST_ID_TODO_ITEM) {
            long id = data.getLongExtra(IntentConstants.TODO_ID, -1);
            Todo todo = todoOpenHelper.getTodo(id);
            if (todo.getTitle().length() <= 0) {
                return;
            }

            boolean existed = false;

            if (todo.hasReminder() && todo.getDate() != null) {
                Intent i = new Intent(this, AlarmReceiver.class);
                i.putExtra(IntentConstants.TODO_ITEM, todo);
                alarmClass.createAlarm(i, (int) todo.getId(), todo.getDate().getTime());
            }

            for (int i = 0; i < todoArrayList.size(); i++) {
                if (todo.getId() == (todoArrayList.get(i).getId())) {
                    todoArrayList.set(i, todo);
                    existed = true;
                    todoRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                }
            }
            if (!existed) {
                todoArrayList.add(todo);
                todoRecyclerViewAdapter.notifyItemInserted(todoArrayList.size() - 1);
            }
            if (!navigationView.getMenu().getItem(0).isChecked()) {
                Snackbar.make(todoCoordinatorLayout, "Added to \"Inbox\"", Snackbar.LENGTH_SHORT)
                        .setAction("SHOW", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showInbox();
                            }
                        }).show();
            }
            if (navigationView.getMenu().getItem(0).isChecked()) {
                showInbox();
            } else if (navigationView.getMenu().getItem(1).isChecked()) {
                showToday();
            } else if (navigationView.getMenu().getItem(2).isChecked()) {
                showNext7Days();
            } else {
                showInbox();
            }
        }
    }

    private void showInbox() {
        toolbar.setTitle("Inbox");
        navigationView.getMenu().getItem(0).setChecked(true);
        updateTodoList();
    }

    private void showToday() {
        toolbar.setTitle("Today");
        navigationView.getMenu().getItem(1).setChecked(true);
        todoArrayList.clear();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM, yyyy h:mm a", Locale.getDefault());
        SQLiteDatabase database = todoOpenHelper.getReadableDatabase();
        Cursor cursor = database.query(TodoOpenHelper.TODO_TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String dateString = cursor.getString(cursor.getColumnIndex(TODO_DATE));
            Date todoDate = null;
            if (dateString != null) {
                try {
                    todoDate = simpleDateFormat.parse(dateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                continue;
            }
            if (todoDate != null && !DateUtils.isToday(todoDate.getTime()))
                continue;
            long id = cursor.getLong(cursor.getColumnIndex(TodoOpenHelper.TODO_ID));
            String todoTitle = cursor.getString(cursor.getColumnIndex(TODO_TITLE));
            int reminder = cursor.getInt(cursor.getColumnIndex(TODO_REMINDER));
            boolean hasReminder = (reminder == 1);

            String todoCategory = cursor.getString(cursor.getColumnIndex(TODO_CATEGORY));
            int todoPriority = cursor.getInt(cursor.getColumnIndex(TODO_PRIORITY));
            String todoLabel = cursor.getString(cursor.getColumnIndex(TODO_LABEL));
            Todo todo = new Todo(id, todoTitle, todoCategory, todoDate, todoPriority, todoLabel, hasReminder);
            todoArrayList.add(todo);
        }
        cursor.close();
        todoRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void showNext7Days() {
        toolbar.setTitle("Next 7 days");
        navigationView.getMenu().getItem(2).setChecked(true);
        todoArrayList.clear();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM, yyyy h:mm a", Locale.getDefault());
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 7);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        SQLiteDatabase database = todoOpenHelper.getReadableDatabase();
        Cursor cursor = database.query(TodoOpenHelper.TODO_TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String dateString = cursor.getString(cursor.getColumnIndex(TODO_DATE));
            Date todoDate = null;
            if (dateString != null) {
                try {
                    todoDate = simpleDateFormat.parse(dateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                continue;
            }
            if (todoDate != null && todoDate.before(c.getTime()) && todoDate.after(new Date())) {
                long id = cursor.getLong(cursor.getColumnIndex(TodoOpenHelper.TODO_ID));
                String todoTitle = cursor.getString(cursor.getColumnIndex(TODO_TITLE));
                int reminder = cursor.getInt(cursor.getColumnIndex(TODO_REMINDER));
                boolean hasReminder = (reminder == 1);

                String todoCategory = cursor.getString(cursor.getColumnIndex(TODO_CATEGORY));
                int todoPriority = cursor.getInt(cursor.getColumnIndex(TODO_PRIORITY));
                String todoLabel = cursor.getString(cursor.getColumnIndex(TODO_LABEL));
                Todo todo = new Todo(id, todoTitle, todoCategory, todoDate, todoPriority, todoLabel, hasReminder);
                todoArrayList.add(todo);
            }
        }
        cursor.close();
        todoRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void showProjects() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        builder.setTitle("Project")
                .setItems(R.array.project, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedCategory = getResources().getStringArray(R.array.project)[i];
                        showCategory(selectedCategory);
                        dialogInterface.dismiss();
                    }
                });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showInbox();
                dialogInterface.dismiss();
            }
        });
        Dialog projectDialog = builder.create();
        projectDialog.show();
    }

    private void showCategory(String selectedCategory) {
        toolbar.setTitle(selectedCategory);
        navigationView.getMenu().getItem(3).setChecked(true);
        todoArrayList.clear();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM, yyyy h:mm a", Locale.getDefault());
        SQLiteDatabase database = todoOpenHelper.getReadableDatabase();
        Cursor cursor = database.query(TodoOpenHelper.TODO_TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String todoTitle = cursor.getString(cursor.getColumnIndex(TODO_TITLE));
            String todoCategory = cursor.getString(cursor.getColumnIndex(TODO_CATEGORY));
            if (todoCategory.equals(selectedCategory)) {
                long id = cursor.getLong(cursor.getColumnIndex(TodoOpenHelper.TODO_ID));
                String dateString = cursor.getString(cursor.getColumnIndex(TODO_DATE));
                Date todoDate = null;
                if (dateString != null) {
                    try {
                        todoDate = simpleDateFormat.parse(dateString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                int reminder = cursor.getInt(cursor.getColumnIndex(TODO_REMINDER));
                boolean hasReminder = (reminder == 1);
                int todoPriority = cursor.getInt(cursor.getColumnIndex(TODO_PRIORITY));
                String todoLabel = cursor.getString(cursor.getColumnIndex(TODO_LABEL));
                Todo todo = new Todo(id, todoTitle, todoCategory, todoDate, todoPriority, todoLabel, hasReminder);
                todoArrayList.add(todo);
            }
        }
        cursor.close();
        todoRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_inbox) {
            showInbox();
        } else if (id == R.id.nav_today) {
            showToday();
        } else if (id == R.id.nav_next7days) {
            showNext7Days();
        } else if (id == R.id.nav_projects) {
            showProjects();
        } else if (id == R.id.nav_labels) {
            Snackbar.make(todoCoordinatorLayout, "Feature unavailable", Snackbar.LENGTH_SHORT).show();
        } else if (id == R.id.nav_filters) {
            Snackbar.make(todoCoordinatorLayout, "Feature unavailable", Snackbar.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private final int[] ATTRS = new int[]{android.R.attr.listDivider};

        private Drawable divider;

        DividerItemDecoration(Context context) {
            final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
            divider = styledAttributes.getDrawable(0);
            styledAttributes.recycle();
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount - 1; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + divider.getIntrinsicHeight();

                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }
    }
}
