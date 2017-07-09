package in.codingninjas.todoapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by nsbhasin on 09/07/17.
 */

class TodoOpenHelper extends SQLiteOpenHelper {
    final static String TODO_TABLE_NAME = "table_name";
    final static String TODO_ID = "todo_id";
    final static String TODO_TITLE = "todo_title";
    final static String TODO_CATEGORY = "todo_category";
    final static String TODO_DATE = "todo_date";
    final static String TODO_PRIORITY = "todo_priority";
    final static String TODO_LABEL = "todo_label";
    final static String TODO_REMINDER = "todo_remainder";

    private static TodoOpenHelper todoOpenHelper;

    static TodoOpenHelper getInstance(Context context) {
        if (todoOpenHelper == null) {
            todoOpenHelper = new TodoOpenHelper(context);
        }
        return todoOpenHelper;
    }

    private TodoOpenHelper(Context context) {
        super(context, "todoapp_final.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TODO_TABLE_NAME + " ( " + TODO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TODO_TITLE + " TEXT, " + TODO_CATEGORY + " TEXT, " + TODO_DATE + " TEXT, " + TODO_PRIORITY + " INTEGER, " + TODO_LABEL + " TEXT, " + TODO_REMINDER + " integer );";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    Todo getTodo(long id) {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(TodoOpenHelper.TODO_TABLE_NAME, null, TodoOpenHelper.TODO_ID + " = " + id, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM, yyyy h:mm a", Locale.getDefault());

        assert cursor != null;
        String todoTitle = cursor.getString(cursor.getColumnIndex(TODO_TITLE));
        String dateString = cursor.getString(cursor.getColumnIndex(TODO_DATE));
        int reminder = cursor.getInt(cursor.getColumnIndex(TODO_REMINDER));
        boolean hasReminder = (reminder==1);
        Date todoDate = null;
        if(dateString != null) {
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
        Log.i("addTodo",TODO_TITLE + " = " + todo.getTitle() + " " + TODO_DATE + " = " + dateString + " " + TODO_REMINDER + " = " + (todo.hasReminder()?1:0) + TODO_CATEGORY + " = " + todo.getCategory() + TODO_PRIORITY + " = " + todo.getPriority() + TODO_LABEL + " = " + todo.getLabel());
        cursor.close();

        return todo;
    }

    long addTodo(Todo todo) {
        SQLiteDatabase database = this.getWritableDatabase();

        String dateString = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM, yyyy h:mm a", Locale.getDefault());
        if (todo.getDate() != null) {
            dateString = simpleDateFormat.format(todo.getDate());
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(TodoOpenHelper.TODO_TITLE, todo.getTitle());
        contentValues.put(TodoOpenHelper.TODO_DATE, dateString);
        contentValues.put(TODO_REMINDER, (todo.hasReminder()?1:0));
        contentValues.put(TODO_CATEGORY, todo.getCategory());
        contentValues.put(TODO_PRIORITY, todo.getPriority());
        contentValues.put(TODO_LABEL, todo.getLabel());
        Log.i("addTodo",TODO_TITLE + " = " + todo.getTitle() + " " + TODO_DATE + " = " + dateString + " " + TODO_REMINDER + " = " + (todo.hasReminder()?1:0) + TODO_CATEGORY + " = " + todo.getCategory() + TODO_PRIORITY + " = " + todo.getPriority() + TODO_LABEL + " = " + todo.getLabel());
        long id = database.insert(TodoOpenHelper.TODO_TABLE_NAME, null, contentValues);
        Log.i("addTodo ID","" + id);
        return id;
    }

    void updateTodo(Todo todo) {
        SQLiteDatabase database = this.getWritableDatabase();

        long id = todo.getId();
        String dateString = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM, yyyy h:mm a", Locale.getDefault());
        if (todo.getDate() != null) {
            dateString = simpleDateFormat.format(todo.getDate());
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(TodoOpenHelper.TODO_TITLE, todo.getTitle());
        contentValues.put(TodoOpenHelper.TODO_DATE, dateString);
        contentValues.put(TODO_REMINDER, (todo.hasReminder()?1:0));
        contentValues.put(TODO_CATEGORY, todo.getCategory());
        contentValues.put(TODO_PRIORITY, todo.getPriority());
        contentValues.put(TODO_LABEL, todo.getLabel());
        database.update(TodoOpenHelper.TODO_TABLE_NAME, contentValues, TodoOpenHelper.TODO_ID + " = " + id, null);
    }

    void deleteTodo(long id) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TodoOpenHelper.TODO_TABLE_NAME, TodoOpenHelper.TODO_ID + " = " + id, null);
    }

    void addDeletedTodo(Todo todo) {
        SQLiteDatabase database = this.getWritableDatabase();

        String dateString = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM, yyyy h:mm a", Locale.getDefault());
        if (todo.hasReminder() && todo.getDate() != null) {
            dateString = simpleDateFormat.format(todo.getDate());
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(TodoOpenHelper.TODO_ID, todo.getId());
        contentValues.put(TodoOpenHelper.TODO_TITLE, todo.getTitle());
        contentValues.put(TodoOpenHelper.TODO_DATE, dateString);
        contentValues.put(TODO_REMINDER, (todo.hasReminder()?1:0));
        contentValues.put(TODO_CATEGORY, todo.getCategory());
        contentValues.put(TODO_PRIORITY, todo.getPriority());
        contentValues.put(TODO_LABEL, todo.getLabel());
        database.insert(TodoOpenHelper.TODO_TABLE_NAME, null, contentValues);
    }
}
