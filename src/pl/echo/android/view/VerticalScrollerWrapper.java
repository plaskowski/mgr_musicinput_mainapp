package pl.echo.android.view;

import android.content.Context;
import android.widget.Scroller;

/**
 * Aborts animation when boundary is reached.
 */
public class VerticalScrollerWrapper {
	private final Scroller scroller;
	private int lastVelocityY = 0;
	private int lastMaxY, lastMinY;
	
	public VerticalScrollerWrapper(Context context) {
		this.scroller = new Scroller(context);
	}

	public boolean isFinished() {
		return scroller.isFinished();
	}

	public void abortAnimation() {
		scroller.abortAnimation();
	}

	public boolean computeScrollOffset() {
		if(!isFinished()) {
			if((lastVelocityY > 0 && scroller.getCurrY() == lastMaxY)
					|| (lastVelocityY < 0 && scroller.getCurrY() == lastMinY)) {
				scroller.abortAnimation();
				return false;
			}
		}
		return scroller.computeScrollOffset();
	}

	public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX,
			int minY, int maxY) {
		scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
	}

	public int getCurrX() {
		return scroller.getCurrX();
	}

	public int getCurrY() {
		return scroller.getCurrY();
	}

	public int getFinalY() {
		return scroller.getFinalY();
	}

	public void startScroll(int startX, int startY, int dx, int dy) {
		scroller.startScroll(startX, startY, dx, dy);
	}
}
