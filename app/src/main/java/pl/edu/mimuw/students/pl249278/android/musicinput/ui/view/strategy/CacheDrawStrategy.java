package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewInflationContext;

public class CacheDrawStrategy extends ViewGroupStrategyBase {

	public CacheDrawStrategy(ViewGroupStrategy parent) {
		super(parent);
	}

	@Override
	public void initStrategy(ViewInflationContext viewInflationContext) {
		super.initStrategy(viewInflationContext);
		internals().viewObject().setDrawingCacheEnabled(true);
	}

    private static Paint mPaint = new Paint();
	private boolean inCacheContext = false;
    
	@Override
	public void buildDrawingCache(BuildDrawingCacheSuperCall superCall) {
		inCacheContext = true;
		super.buildDrawingCache(superCall);
		inCacheContext = false;
	}
	
	@Override
	public void buildDrawingCache(boolean autoScale, BuildDrawingCacheSuperCall superCall) {
		inCacheContext = true;
		super.buildDrawingCache(autoScale, superCall);
		inCacheContext = false;
	}
	
	@Override
	public void onDraw(Canvas canvas, OnDrawSuperCall superCall) {
		if(!inCacheContext) {
			Bitmap cache = internals().viewObject().getDrawingCache();
			if(cache != null) {
				canvas.drawBitmap(cache, 0, 0, mPaint);
				return;
			}
		}
		super.onDraw(canvas, superCall);
	}	
}
