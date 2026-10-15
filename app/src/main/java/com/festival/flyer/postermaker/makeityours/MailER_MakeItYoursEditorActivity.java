package com.festival.flyer.postermaker.makeityours;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.festival.flyer.postermaker.R;

public class MailER_MakeItYoursEditorActivity extends AppCompatActivity {
    private float dX, dY;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_it_yours_editor);
        
        // Mock drag implementation
        // findViewById(R.id.canvas).setOnTouchListener(...)
    }
}
