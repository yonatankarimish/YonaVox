package org.yonavox.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import org.yonavox.R;
import org.yonavox.services.RecordingService;
import org.yonavox.util.Constants;
import org.yonavox.util.SensiboUtils;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final int RECORD_PERMISSION_CODE = 1;
    private boolean currentlyTyping = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        final EditText apiKey = findViewById(R.id.apiKey);
        final EditText deviceId = findViewById(R.id.deviceId);
        final ImageButton playBtn = findViewById(R.id.playBtn);
        final ImageButton invokeBtn = findViewById(R.id.invokeBtn);

        initSensiboApi(apiKey, deviceId);
        registerKeyEvents(apiKey, deviceId);
        registerTapEvents(playBtn, invokeBtn);
        startRecording();
    }

    private void initSensiboApi(EditText apiKey, EditText deviceId) {
        String apiKeyText = sharedPreferences.getString(Constants.SENSIBO_API_KEY, "");
        String deviceIdText = sharedPreferences.getString(Constants.SENSIBO_DEVICE_ID, "");

        apiKey.setText(apiKeyText);
        deviceId.setText(deviceIdText);
        SensiboUtils.apiKey = apiKeyText;
        SensiboUtils.deviceId = deviceIdText;
    }

    private void registerKeyEvents(EditText apiKey, EditText deviceId) {
        apiKey.setOnKeyListener((view, keyCode, keyEvent) -> {
            String apiKeyText = getTextFromView(apiKey);
            handleKeyPress(keyCode, keyEvent, "New api key: " + apiKeyText);
            SensiboUtils.apiKey = apiKeyText;
            saveValue(Constants.SENSIBO_API_KEY, apiKeyText);
            return false;
        });

        apiKey.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus && currentlyTyping) {
                String apiKeyText = getTextFromView(apiKey);
                displayMessage("New api key: " + apiKeyText);
                currentlyTyping = false;
            }
        });

        deviceId.setOnKeyListener((view, keyCode, keyEvent) -> {
            String deviceIdText = getTextFromView(deviceId);
            handleKeyPress(keyCode, keyEvent, "New device id: " + deviceIdText);
            SensiboUtils.deviceId = deviceIdText;
            saveValue(Constants.SENSIBO_DEVICE_ID, deviceIdText);
            return false;
        });

        deviceId.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus && currentlyTyping) {
                String deviceIdText = getTextFromView(deviceId);
                displayMessage("New device id: " + deviceIdText);
                currentlyTyping = false;
            }
        });
    }

    private void registerTapEvents(ImageButton playBtn, ImageButton invokeBtn) {
        playBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                RecordingService.invokeTranscription();
            }
            view.performClick();
            return true;
        });

        invokeBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                testApiInvoke();
            }
            view.performClick();
            return true;
        });
    }

    private void handleKeyPress(int keyCode, KeyEvent event, String message) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
            displayMessage(message);
            currentlyTyping = false;
        } else {
            currentlyTyping = true;
        }
    }

    private String getTextFromView(EditText editText) {
        return editText.getText().toString().length() > 0 ? editText.getText().toString() : "";
    }

    private void displayMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void saveValue(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private boolean obtainRecordingPermissions() {
        Context appContext = getApplicationContext();
        boolean hasRecordPermission = ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        if (!hasRecordPermission) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_PERMISSION_CODE);
        }

        return hasRecordPermission;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RECORD_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                displayMessage("Cannot record audio without granting permission");
            }
        }
    }

    private void startRecording() {
        if (obtainRecordingPermissions()) {
            try {
                Intent intent = new Intent(this, RecordingService.class);
                startService(intent);
            } catch (Exception e) {
                System.err.println("Failed to start recording audio. Caused by: " + e.getMessage());
            }
        }
    }

    private void testApiInvoke() {
        SensiboUtils.cool();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SensiboUtils.heat();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SensiboUtils.turnOff();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent stopRecordingIntent = new Intent(this, RecordingService.class);
        stopService(stopRecordingIntent);
    }
}