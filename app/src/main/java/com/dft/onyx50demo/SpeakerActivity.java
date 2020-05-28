package com.dft.onyx50demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

public class SpeakerActivity extends Activity {
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaker);
    }

    @Override
    public void onResume() {
        super.onResume();
        activity = this;
        final AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        Button okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainApplication.setSpeaker(autoCompleteTextView.getText().toString());
                startActivity(new Intent(activity, OnyxSetupActivity.class));
            }
        });
    }
}
