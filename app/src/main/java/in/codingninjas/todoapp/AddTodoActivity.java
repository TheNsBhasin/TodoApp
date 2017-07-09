package in.codingninjas.todoapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTodoActivity extends AppCompatActivity {
    private Todo mUserTodoItem;
    private String mUserTodoTitle;
    private String mUserTodoCategory;
    private Date mUserTodoDate;
    private int mUserTodoPriority;
    private String mUserTodoLabel;
    private boolean mUserHasReminder;

    private EditText todoTitleEditText;
    private EditText todoProjectEditText;
    private TextView todoDateTextView;
    private TextView todoPriorityTextView;
    private TextView todoLabelTextView;
    private TextView todoReminderTextView;

    private static final String DEFAULT_TITLE = "";
    private final String DEFAULT_CATEGORY =  "Inbox";
    private final String DEFAULT_LABEL = "No label";
    private final String DEFAULT_REMINDER = "No reminders";
    private final int DEFAULT_PRIORITY = 4;
    private final String DEFAULT_DATE = "No due date";

    private TodoOpenHelper todoOpenerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        todoOpenerHelper = TodoOpenHelper.getInstance(AddTodoActivity.this);

        mUserTodoItem = (Todo) getIntent().getSerializableExtra(IntentConstants.TODO_ITEM);

        mUserTodoTitle = mUserTodoItem.getTitle();
        mUserTodoCategory = mUserTodoItem.getCategory();
        mUserTodoDate = mUserTodoItem.getDate();
        mUserTodoPriority = mUserTodoItem.getPriority();
        mUserTodoLabel = mUserTodoItem.getLabel();
        mUserHasReminder = mUserTodoItem.hasReminder();

        LinearLayout todoProperyLinearLayout = (LinearLayout) findViewById(R.id.todoProperyLinearLayout);
        todoTitleEditText = (EditText) findViewById(R.id.todoTitleEditText);
        todoProjectEditText = (EditText) findViewById(R.id.todoProjectEditText);
        ConstraintLayout todoDateConstraintLayout = (ConstraintLayout) findViewById(R.id.todoDateConstraintLayout);
        todoDateTextView = (TextView) findViewById(R.id.todoDateTextView);
        ConstraintLayout todoPriorityConstraintLayout = (ConstraintLayout) findViewById(R.id.todoPriorityConstraintLayout);
        todoPriorityTextView = (TextView) findViewById(R.id.todoPriorityTextView);
        todoLabelTextView = (TextView) findViewById(R.id.todoLabelTextView);
        todoReminderTextView = (TextView) findViewById(R.id.todoReminderTextView);
        SwitchCompat todoReminderSwitchCombat = (SwitchCompat) findViewById(R.id.todoReminderSwitchCombat);

        todoProperyLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(todoTitleEditText);
            }
        });

        if (mUserHasReminder && mUserTodoDate != null) {
            setReminderTextView();
        }
        if (mUserTodoDate == null) {
            todoDateTextView.setText(DEFAULT_DATE);
            todoReminderTextView.setText(DEFAULT_REMINDER);
        } else {
            setDateAndTimeEditText();
        }

        todoTitleEditText.requestFocus();
        todoTitleEditText.setText((mUserTodoTitle == null || mUserTodoTitle.isEmpty()) ? DEFAULT_TITLE : mUserTodoTitle);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        todoTitleEditText.setSelection(todoTitleEditText.length());

        todoTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mUserTodoTitle = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        todoReminderSwitchCombat.setChecked(mUserHasReminder && (mUserTodoDate != null));
        todoReminderSwitchCombat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mUserHasReminder = isChecked;
                setReminderTextView();
                hideKeyboard(todoTitleEditText);
            }
        });

        todoDateConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                todoDatePicker();
                hideKeyboard(todoTitleEditText);
            }
        });

        todoProjectEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddTodoActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
                builder.setTitle("Project")
                        .setItems(R.array.project, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mUserTodoCategory = getResources().getStringArray(R.array.project)[i];
                                setProjectEditText();
                                dialogInterface.dismiss();
                            }
                        });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                Dialog projectDialog = builder.create();
                hideKeyboard(todoTitleEditText);
                projectDialog.show();
            }
        });

        todoPriorityConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddTodoActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
                builder.setTitle("Priority")
                        .setItems(R.array.priority, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mUserTodoPriority = 4 - i;
                                setPriorityTextView();
                                dialogInterface.dismiss();
                            }
                        });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                Dialog priorityDialog = builder.create();
                hideKeyboard(todoTitleEditText);
                priorityDialog.show();
            }
        });

        setDateAndTimeEditText();
        setProjectEditText();
        setDateAndTimeEditText();
        setProjectEditText();
        setPriorityTextView();
        setLabelTextView();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                todoTitleEditText.setText(todoTitleEditText.getText().toString().trim());
                if (todoTitleEditText.length() <= 0) {
                    todoTitleEditText.setError(getString(R.string.todo_error));
                } else if (mUserTodoDate != null && mUserTodoDate.before(new Date())) {
                    todoDateTextView.setError(getResources().getString(R.string.date_error_check_again));
                } else {
                    makeResult(RESULT_OK);
                    finish();
                }
                hideKeyboard(todoTitleEditText);
            }
        });


    }

    private void setProjectEditText() {
        if (mUserTodoCategory == null || mUserTodoCategory.isEmpty()) {
            mUserTodoCategory = DEFAULT_CATEGORY;
        }
        todoProjectEditText.setText(mUserTodoCategory);
    }

    private void setPriorityTextView() {
        if (mUserTodoPriority == -1) {
            mUserTodoPriority = DEFAULT_PRIORITY;
        }
        todoPriorityTextView.setText("Priority " + mUserTodoPriority);
    }

    private void setLabelTextView() {
        if (mUserTodoLabel == null || mUserTodoLabel.isEmpty()) {
            mUserTodoLabel = DEFAULT_LABEL;
        }
        todoLabelTextView.setText(mUserTodoLabel);
    }

    private void setDateAndTimeEditText() {
        if (mUserTodoDate != null) {
            String userDate = formatDate("d MMM, yyyy h:mm a", mUserTodoDate);
            todoDateTextView.setText(userDate);
        } else {
            todoDateTextView.setText(DEFAULT_DATE);
        }
        checkError();
    }

    private void checkError() {
        if (mUserTodoDate != null && mUserTodoDate.before(new Date())) {
            todoDateTextView.setError(getResources().getString(R.string.date_error_check_again));
        } else {
            todoDateTextView.setError(null);
        }
        if (mUserHasReminder && mUserTodoDate != null && mUserTodoDate.before(new Date()) || mUserHasReminder && mUserTodoDate == null) {
            todoReminderTextView.setError(getResources().getString(R.string.todo_reminder_error));
        } else {
            todoReminderTextView.setError(null);
        }
    }

    public void setTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        if (mUserTodoDate != null) {
            calendar.setTime(mUserTodoDate);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, hour, minute, 0);
        mUserTodoDate = calendar.getTime();

        setDateAndTimeEditText();
        setReminderTextView();
    }

    public void setDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        int hour, minute;

        Calendar reminderCalendar = Calendar.getInstance();
        reminderCalendar.set(year, month, day);

        if (reminderCalendar.before(calendar)) {
            Toast.makeText(this, getString(R.string.todo_reminder_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mUserTodoDate != null) {
            calendar.setTime(mUserTodoDate);
        }

        if (DateFormat.is24HourFormat(this)) {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
        } else {

            hour = calendar.get(Calendar.HOUR);
        }
        minute = calendar.get(Calendar.MINUTE);

        calendar.set(year, month, day, hour, minute);
        mUserTodoDate = calendar.getTime();
        setReminderTextView();
        setDateEditText();
    }

    public void setDateEditText() {
        String dateFormat = "d MMM, yyyy";
        todoDateTextView.setText(formatDate(dateFormat, mUserTodoDate));
    }

    private void setReminderTextView() {
        if (mUserHasReminder && mUserTodoDate != null) {
            if (mUserTodoDate.before(new Date())) {
                todoReminderTextView.setText(getString(R.string.todo_reminder_error));
                todoReminderTextView.setTextColor(Color.RED);
                return;
            }
            Date date = mUserTodoDate;
            String dateString = formatDate("d MMM, yyyy", date);
            String timeString;
            String amPmString = "";

            if (DateFormat.is24HourFormat(this)) {
                timeString = formatDate("k:mm", date);
            } else {
                timeString = formatDate("h:mm", date);
                amPmString = formatDate("a", date);
            }
            String finalString = String.format(getResources().getString(R.string.remind_date_and_time), dateString, timeString, amPmString);
            todoReminderTextView.setTextColor(ContextCompat.getColor(this, R.color.colorSecondaryText));
            todoReminderTextView.setText(finalString);
        } else {
            todoReminderTextView.setText(DEFAULT_REMINDER);
            todoReminderTextView.setTextColor(ContextCompat.getColor(this, R.color.colorSecondaryText));
        }
        checkError();
    }

    private void todoDatePicker() {
        Date date;
        hideKeyboard(todoTitleEditText);
        final Calendar calendar = Calendar.getInstance();
        if (mUserTodoItem.getDate() != null) {
            date = mUserTodoDate;
        } else {
            date = calendar.getTime();
        }
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(AddTodoActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                setDate(year, month, day);
                todoTimePicker();
            }
        }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void todoTimePicker() {
        Date date;
        hideKeyboard(todoTitleEditText);
        if (mUserTodoItem.getDate() != null) {
            date = mUserTodoDate;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            calendar.add(Calendar.MINUTE, 0);
            date = calendar.getTime();
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(AddTodoActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                calendar.set(Calendar.HOUR, hour);
                calendar.set(Calendar.MINUTE, minute);
                setTime(hour, minute);
            }
        }, hour, minute, DateFormat.is24HourFormat(AddTodoActivity.this));
        timePickerDialog.show();
    }

    public static String formatDate(String formatString, Date dateToFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatString, Locale.getDefault());
        return simpleDateFormat.format(dateToFormat);
    }

    private void hideKeyboard(EditText todoTitleEditText) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(todoTitleEditText.getWindowToken(), 0);
    }

    private void makeResult(int result) {
        Intent intent = new Intent();
        if (mUserTodoTitle.length() > 0) {
            String capitalizedString = Character.toUpperCase(mUserTodoTitle.charAt(0)) + mUserTodoTitle.substring(1);
            mUserTodoItem.setTitle(capitalizedString);
        } else {
            mUserTodoItem.setTitle(mUserTodoTitle);
        }

        if (mUserTodoDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mUserTodoDate);
            calendar.set(Calendar.SECOND, 0);
            mUserTodoDate = calendar.getTime();
        }
        mUserTodoItem.setReminder(mUserHasReminder);
        mUserTodoItem.setDate(mUserTodoDate);
        mUserTodoItem.setCategory(mUserTodoCategory);
        mUserTodoItem.setPriority(mUserTodoPriority);
        mUserTodoItem.setLabel(mUserTodoLabel);
        long id = mUserTodoItem.getId();
        if (id != 0) {
            todoOpenerHelper.updateTodo(mUserTodoItem);
        } else {
            id = todoOpenerHelper.addTodo(mUserTodoItem);
        }
        intent.putExtra(IntentConstants.TODO_ID, id);
        setResult(result, intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null) {
                    setResult(RESULT_CANCELED);
                    NavUtils.navigateUpFromSameTask(this);
                }
                hideKeyboard(todoTitleEditText);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (NavUtils.getParentActivityName(this) != null) {
            setResult(RESULT_CANCELED);
            NavUtils.navigateUpFromSameTask(this);
        }
        hideKeyboard(todoTitleEditText);
    }
}
