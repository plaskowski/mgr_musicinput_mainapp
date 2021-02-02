package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutParamsHelper.setVerticalPadding;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;

@SuppressWarnings("deprecation")
public class HackedScrollViewChild extends LinearLayout {
	@SuppressWarnings("unused")
	private static final LogUtils log = new LogUtils(HackedScrollViewChild.class);

	public HackedScrollViewChild(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOrientation(HORIZONTAL);
	}
	
	private View ruler;
	private int scrollViewportHeight = 0;
	private int rulerVisibleY;
	private boolean rulerVisibleYFixed = false;
	
	public void fixRulerVisibleY(int y) {
		rulerVisibleY = y;
		rulerVisibleYFixed = true;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(!rulerVisibleYFixed) {
			rulerVisibleY = rulerVisibleY();
		}
//		log.d("onMeasure():: rulerVisibleY = %d, vPadding = %d, %d",
//			rulerVisibleY,
//			getPaddingTop(),
//			getPaddingBottom()
//		);
		setVerticalPadding(this, 0, 0);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		computePadding(
			rulerVisibleY,
			((AbsoluteLayout.LayoutParams) ruler.getLayoutParams()).y,
			ruler.getMeasuredHeight(),
			ruler.getPaddingTop() + (ruler.getMeasuredHeight() - ruler.getPaddingTop() - ruler.getPaddingBottom())/2,
			this.getMeasuredHeight()
		);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		rulerVisibleYFixed = false;
		super.onLayout(changed, l, t, r, b);
//		log.d("post super::onLayout() ruler.height = %d, ruler.parent.height = %d, wrapper.height = %d, wrapper.padding = %d %d",
//			ruler.getHeight(),
//			((View) ruler.getParent()).getHeight(),
//			getHeight(),
//			getPaddingTop(), getPaddingBottom()
//		);
	}
	
	private int rulerVisibleY() {
		int result = - ((View) getParent()).getScrollY();
		for(View view = ruler; view != this; view = (View) view.getParent()) {
			result += view.getTop();
		}
		return result;
	}

	/**
	 * @param rulerDestY destination visible Y position of ruler view
	 * @param rulerRelY ruler relative Y position in it's parent coordinates
	 * @param rulerHeight height of ruler view
	 * @param rulerMiddleY vertical middle of ruler that must be able to reach vertical middle of visible estate
	 * @param rulerContainerHeight height of ruler parent
	 */
	public void computePadding(int rulerDestY, int rulerRelY, int rulerHeight, int rulerMiddleY, int rulerContainerHeight) {
		int topSpacerHeight = Math.max(
			0, Math.max(
			// assure we can scroll to forced position
			rulerDestY - rulerRelY,
			// assure we can scroll ruler middle to middle
			scrollViewportHeight/2 - rulerRelY - rulerMiddleY
		));		
		int scrollTo = Math.max(0, topSpacerHeight + rulerRelY - rulerDestY);
		int bottomHeight = Math.max(
			0, Math.max( 
			// assure we can scroll to forced position
			scrollViewportHeight - (topSpacerHeight + rulerContainerHeight - scrollTo),
			// assure we can scroll ruler middle to middle
			scrollViewportHeight/2 - (rulerContainerHeight - rulerRelY - rulerMiddleY)
		));
//		log.d("compute(rDestY: %d, rRelY: %d, rH: %d, rContH: %d, rMidY: %d, sVP.height: %d) spacers %d %d, scroll %d", 
//				rulerDestY, rulerRelY, rulerHeight, rulerContainerHeight, rulerMiddleY, scrollViewportHeight
//				,topSpacerHeight, bottomHeight, scrollTo);
		setVerticalPadding(this, topSpacerHeight, bottomHeight);
		HackedScrollView parent = (HackedScrollView) getParent();
		parent.postLayoutScrollTo(0, scrollTo);
	}

	public void setRuler(View ruler) {
		this.ruler = ruler;
	}

	void setScrollViewportHeight(int scrollViewportHeight) {
		this.scrollViewportHeight = scrollViewportHeight;
	}
}
