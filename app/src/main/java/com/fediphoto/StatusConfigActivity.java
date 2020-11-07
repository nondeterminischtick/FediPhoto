package com.fediphoto;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.QuickContactBadge;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class StatusConfigActivity extends AppCompatActivity {
    private final Context context = this;
    private final String TAG = this.getClass().getCanonicalName();

    private RadioButton radioVisibilityDirect;
    private RadioButton radioVisibilityUnlisted;
    private RadioButton radioVisibilityFollowers;
    private RadioButton radioVisibilityPublic;
    private RadioButton radioThreadingNever;
    private RadioButton radioThreadingAlways;
    private RadioButton radioThreadingDaily;
    private EditText editTextLabel;
    private EditText editTextText;
    private EditText editTextDateFormat;
    private EditText editTextGpsCoordinatesFormat;
    private CheckBox checkBoxActiveStatus;
    private int statusIndexSelected;
    private JsonObject settings;
    private JsonObject status;
    private Button buttonStartNewThread;

    private final String DEFAULT_GPS_COORDINATES_FORMAT = "https://www.openstreetmap.org/?mlat=%.5f&mlon=%.5f&zoom=17#layers=m";
    private final String DEFAULT_DATE_FORMAT = "EEEE MMMM dd, yyyy hh:mm:ss a z";
    private final double EIFFEL_TOWER_LATITUDE = 48.85827;
    private final double EIFFEL_TOWER_LONGITUDE = 2.29443;

    private void setup() {
        setContentView(R.layout.activity_status_config);
        settings = Utils.getSettings(context);
        Log.i(TAG, String.format("Settings in StatusConfigActivity %s", settings.toString()));
        status = Utils.getStatusSelectedFromSettings(context);
        if (status == null) {
            Log.i(TAG, "Status from settings is null. Creating blank JsonObject.");
            status = new JsonObject();
            status.addProperty(MainActivity.Literals.gpsCoordinatesFormat.name(), DEFAULT_GPS_COORDINATES_FORMAT);
            status.addProperty(MainActivity.Literals.dateFormat.name(), DEFAULT_DATE_FORMAT);
        }
        radioVisibilityDirect = findViewById(R.id.radioVisibilityDirect);
        radioVisibilityUnlisted = findViewById(R.id.radioVisibilityUnlisted);
        radioVisibilityFollowers = findViewById(R.id.radioVisibilityFollowers);
        radioVisibilityPublic = findViewById(R.id.radioVisibilityPublic);
        radioThreadingAlways = findViewById(R.id.radioThreadingAlways);
        radioThreadingNever = findViewById(R.id.radioThreadingNever);
        radioThreadingDaily = findViewById(R.id.radioThreadingDaily);
        checkBoxActiveStatus = findViewById(R.id.checkBoxStatusActive);
        int statusIndexActive = Utils.getInt(Utils.getProperty(settings, MainActivity.Literals.statusIndexActive.name()));
        statusIndexSelected = Utils.getInt(Utils.getProperty(settings, MainActivity.Literals.statusIndexSelected.name()));
        checkBoxActiveStatus.setChecked(statusIndexActive == statusIndexSelected);
        if (MainActivity.Literals.direct.name().equals(Utils.getProperty(status, MainActivity.Literals.visibility.name()))) {
            radioVisibilityDirect.setChecked(true);
        }
        if ("private".equals(Utils.getProperty(status, MainActivity.Literals.visibility.name()))) {
            radioVisibilityFollowers.setChecked(true);
        }
        if (MainActivity.Literals.unlisted.name().equals(Utils.getProperty(status, MainActivity.Literals.visibility.name()))) {
            radioVisibilityUnlisted.setChecked(true);
        }
        if ("public".equals(Utils.getProperty(status, MainActivity.Literals.visibility.name()))) {
            radioVisibilityPublic.setChecked(true);
        }
        buttonStartNewThread = findViewById(R.id.buttonClearThread);
        buttonStartNewThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status.remove(MainActivity.Literals.threadingId.name());
                status.remove(MainActivity.Literals.threadingDate.name());
                Log.i(TAG, "Threading ID cleared.");
                Toast.makeText(context, R.string.threading_id_cleared, Toast.LENGTH_LONG).show();
            }
        });
        RadioGroup radioGroupThreading = findViewById(R.id.radioGroupThreading);
        radioGroupThreading.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                buttonStartNewThread.setEnabled(!radioThreadingNever.isChecked());
            }
        });
        if (MainActivity.Literals.never.name().equals(Utils.getProperty(status, MainActivity.Literals.threading.name()))) {
            radioThreadingNever.setChecked(true);
        }
        if (MainActivity.Literals.always.name().equals(Utils.getProperty(status, MainActivity.Literals.threading.name()))) {
            radioThreadingAlways.setChecked(true);
        }
        if (MainActivity.Literals.daily.name().equals(Utils.getProperty(status, MainActivity.Literals.threading.name()))) {
            radioThreadingDaily.setChecked(true);
        }
        editTextLabel = findViewById(R.id.editTextLabel);
        editTextLabel.setText(Utils.getProperty(status, MainActivity.Literals.label.name()));
        editTextText = findViewById(R.id.editTextText);
        String text = Utils.getProperty(status, MainActivity.Literals.text.name());
        Log.i(TAG, String.format("Text \"%s\" Status \"%s\"", text, status));
        editTextText.setText(text);
        editTextDateFormat = findViewById(R.id.editTextDateFormat);
        editTextDateFormat.setText(Utils.getProperty(status, MainActivity.Literals.dateFormat.name()));
        editTextGpsCoordinatesFormat = findViewById(R.id.editTextGpsCoordinatesFormat);
        editTextGpsCoordinatesFormat.setText(Utils.getProperty(status, MainActivity.Literals.gpsCoordinatesFormat.name()));
        final StringBuilder dateFormat = new StringBuilder();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setup();
    }

    private void save() {
        if (status == null) {
            return;
        }
        if (editTextLabel.getText().toString().trim().length() == 0) {
            Toast.makeText(context, R.string.label_can_not_be_blank, Toast.LENGTH_LONG).show();
            return;
        }
        Log.i(TAG, String.format("Settings at start of save() %s", settings.toString()));
        JsonObject statusJsonObject = status.getAsJsonObject();
        statusJsonObject.addProperty(MainActivity.Literals.label.name(), editTextLabel.getText().toString());
        statusJsonObject.addProperty(MainActivity.Literals.text.name(), editTextText.getText().toString());
        statusJsonObject.addProperty(MainActivity.Literals.dateFormat.name(), editTextDateFormat.getText().toString());
        statusJsonObject.addProperty(MainActivity.Literals.gpsCoordinatesFormat.name(), editTextGpsCoordinatesFormat.getText().toString());
        if (radioVisibilityDirect.isChecked()) {
            statusJsonObject.addProperty(MainActivity.Literals.visibility.name(), MainActivity.Literals.direct.name());
        }
        if (radioVisibilityFollowers.isChecked()) {
            statusJsonObject.addProperty(MainActivity.Literals.visibility.name(), "private");
        }
        if (radioVisibilityPublic.isChecked()) {
            statusJsonObject.addProperty(MainActivity.Literals.visibility.name(), "public");
        }
        if (radioVisibilityUnlisted.isChecked()) {
            statusJsonObject.addProperty(MainActivity.Literals.visibility.name(), MainActivity.Literals.unlisted.name());
        }
        if (radioThreadingNever.isChecked()) {
            statusJsonObject.addProperty(MainActivity.Literals.threading.name(), MainActivity.Literals.never.name());
        }
        if (radioThreadingAlways.isChecked()) {
            statusJsonObject.addProperty(MainActivity.Literals.threading.name(), MainActivity.Literals.always.name());
        }
        if (radioThreadingDaily.isChecked()) {
            statusJsonObject.addProperty(MainActivity.Literals.threading.name(), MainActivity.Literals.daily.name());
        }
        if (checkBoxActiveStatus.isChecked()) {
            settings.addProperty(MainActivity.Literals.statusIndexActive.name(), statusIndexSelected);
        }
        JsonArray statuses = settings.getAsJsonArray(MainActivity.Literals.statuses.name());
        if (statuses == null || statuses.isJsonNull() || statuses.size() == 0) {
            statuses = new JsonArray();
            statuses.add(statusJsonObject);
            settings.add(MainActivity.Literals.statuses.name(), statuses);
        } else {
            Log.i(TAG, String.format("Save status at selected index: %d", statusIndexSelected));
            statuses.set(statusIndexSelected, statusJsonObject);
        }
        Log.i(TAG, String.format("Settings before write in save() %s", settings.toString()));
        Utils.writeSettings(context, settings);
        finish();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Resume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        save();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        save();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_status, menu);
        return true;
    }

    private void addNewStatus() {
        Log.i(TAG, "Add status.");
        JsonArray statuses = settings.getAsJsonArray(MainActivity.Literals.statuses.name());
        JsonObject newStatus = new JsonObject();
        newStatus.addProperty(MainActivity.Literals.gpsCoordinatesFormat.name(), DEFAULT_GPS_COORDINATES_FORMAT);
        newStatus.addProperty(MainActivity.Literals.dateFormat.name(), DEFAULT_DATE_FORMAT);
        statuses.add(newStatus);
        settings.addProperty(MainActivity.Literals.statusIndexSelected.name(), statuses.size() - 1);
        Utils.writeSettings(context, settings);
        setup();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(context, MainActivity.class);
        JsonArray statuses;
        switch (item.getItemId()) {
            case R.id.add_status:
                addNewStatus();
                return true;
            case R.id.remove_status:
                Log.i(TAG, "Remove status config.");
                statuses = settings.getAsJsonArray(MainActivity.Literals.statuses.name());
                if (statuses == null || statuses.isJsonNull() || statuses.size() == 0) {
                    Log.i(TAG, "No status to remove.");
                    Toast.makeText(context, R.string.no_status_config_to_remove, Toast.LENGTH_LONG).show();
                } else {
                    statuses.remove(statusIndexSelected);
                    if (statuses.size() > 0) {
                        settings.add(MainActivity.Literals.statuses.name(), statuses);
                        settings.addProperty(MainActivity.Literals.statusIndexActive.name(), 0);
                        settings.addProperty(MainActivity.Literals.statusIndexSelected.name(), 0);
                        status = null;
                        Toast.makeText(context, R.string.status_config_removed_status_0_active, Toast.LENGTH_LONG).show();
                    } else {
                        addNewStatus();
                        return true;
                    }
                    Utils.writeSettings(context, settings);
                }
                setResult(MainActivity.RESULT_OK, intent);
                finish();
                return true;
            case R.id.restore_defaults:
                radioVisibilityPublic.setChecked(true);
                radioThreadingDaily.setChecked(true);
                editTextDateFormat.setText(DEFAULT_DATE_FORMAT);
                editTextGpsCoordinatesFormat.setText(DEFAULT_GPS_COORDINATES_FORMAT);
                return true;
            case R.id.test_date_format:
                String dateDisplay;
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(editTextDateFormat.getText().toString());
                    dateDisplay = sdf.format(new Date());
                } catch (IllegalArgumentException e) {
                    dateDisplay = getString(R.string.invalid_date_format);
                }
                Toast.makeText(context, dateDisplay, Toast.LENGTH_LONG).show();
                return true;
            case R.id.test_gps_coordinates_format:
                save();
                intent = new Intent(Intent.ACTION_VIEW);
                String urlString = String.format(editTextGpsCoordinatesFormat.getText().toString(), EIFFEL_TOWER_LATITUDE, EIFFEL_TOWER_LONGITUDE);
                intent.setData(Uri.parse(urlString));
                startActivity(intent);
                return true;
            default:
                Log.i(TAG, "Default menu option.");
                return super.onContextItemSelected(item);

        }

    }
}
