package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CacheDrawStrategy extends View {

	public CacheDrawStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDrawingCacheEnabled(true);
	}

	public CacheDrawStrategy(Context context) {
		super(context);
		setDrawingCacheEnabled(true);
	}
	
    private static Paint mPaint = new Paint();
	private boolean inCacheContext = false;
    
	@Override
	public void buildDrawingCache() {
		inCacheContext = true;
		super.buildDrawingCache();
		inCacheContext = false;
	}
	
	@Override
	public void buildDrawingCache(boolean autoScale) {
		inCacheContext = true;
		super.buildDrawingCache(autoScale);
		inCacheContext = false;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(!inCacheContext) {
			Bitmap cache = getDrawingCache();
			if(cache != null) {
				canvas.drawBitmap(cache, 0, 0, mPaint);
				return;
			}
		}
		super.onDraw(canvas);
	}	
}
