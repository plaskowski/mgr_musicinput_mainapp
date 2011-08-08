package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

public abstract class ElementsOverlay extends SheetElement {
	public abstract void positionChanged(SheetAlignedElement element, int newX, int newY);
	
	private Observer observer = null;
	
	public interface Observer {
		public void onMeasureInvalidated();
	}
	
	public void setObserver(Observer observer) {
		this.observer = observer;
	}
	
	protected void onMeasureInvalidated() {
		if(observer != null) observer.onMeasureInvalidated();
	}
	
	protected void setMeasured(int measuredWidth, int measuredHeight) {
		this.measuredWidth = measuredWidth;
		this.measuredHeight = measuredHeight;
	}
	
	protected void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	protected void makeEmpty() {
		setMeasured(0, 0);
	}
	
	private int x, y, measuredWidth, measuredHeight;
	
	@Override
	public int measureWidth() {
		return measuredWidth;
	}
	@Override
	public int measureHeight() {
		return measuredHeight;
	}
	
	public int left() {
		return x;
	}
	
	public int top() {
		return y;
	}

}
