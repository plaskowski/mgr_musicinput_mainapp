package pl.edu.mimuw.students.pl249278.android.musicinput.test;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class SvgTestActivity extends Activity {
	int zeroLineYOffset = 100;
	int lineThickness = 20;
	int linespacingThickness = 80;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.test_svgtest);
        
		RelativeLayout sheet = (RelativeLayout) findViewById(R.id.sheet);
		/*
        View imgV = findViewById(R.id.sheet_bg);
        Bitmap b = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawColor(Color.WHITE);
        ((ImageView) imgV).setImageBitmap(b);
        
        // draw visible anchors
        for(int i = 0; i < 5; i++) {
        	float yoffset = zeroLineYOffset+i*(linespacingThickness+lineThickness);
        	c.drawRect(0, yoffset, 500, yoffset+lineThickness, new Paint());
        }
        */

        try {
        	Paint paint = new Paint();
//			paint.setColor(0xFFFF9933);
			paint.setAntiAlias(true);
			
			NoteView note = new NoteView(this, 2, 8);
			note.setSheetParams(new SheetParams(lineThickness, linespacingThickness));
			note.setPaint(paint);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.leftMargin = 50;
			params.topMargin = 50;
			sheet.addView(note, params);
        	
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
	}


}
