package pl.edu.mimuw.students.pl249278.android.ui.view;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.View.MeasureSpec.getMode;
import static android.view.View.MeasureSpec.getSize;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static android.view.ViewGroup.LayoutParams.FILL_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import java.util.ArrayList;
import java.util.Iterator;

import pl.edu.mimuw.students.pl249278.android.flow.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class FlowLayout extends ViewGroup {
	private int verticalSpacing = 0;
	private ArrayList<Integer> rowsHeight = new ArrayList<Integer>();

	public FlowLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
		verticalSpacing = styledAttrs.getDimensionPixelSize(R.styleable.FlowLayout_android_verticalSpacing, verticalSpacing);
		styledAttrs.recycle();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = getMode(widthMeasureSpec);
		int heightMode = getMode(heightMeasureSpec);
		int width = (widthMode == UNSPECIFIED ? Integer.MAX_VALUE : getSize(widthMeasureSpec))
			- getPaddingLeft() - getPaddingRight();
		int height = getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
		int childWidthMode = widthMode == EXACTLY ? AT_MOST : widthMode;
		int childHeightMode = heightMode == EXACTLY ? AT_MOST : heightMode;
		rowsHeight.clear();
		
		int maxRowWidth = 0, totalHeight = 0;
		int total = getChildCount();
		int rowWidth = 0, rowHeight = 0;
		for(int i = 0; i < total; i++) {
			View child = getChildAt(i);
			if(child.getVisibility() == GONE) {
				continue;
			}
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
			int childWidthSpec;
			if(lp.width == FILL_PARENT) {
				childWidthSpec = makeMeasureSpec(width, widthMode == UNSPECIFIED ? UNSPECIFIED : EXACTLY);
			} else if(lp.width == WRAP_CONTENT) {
				childWidthSpec = makeMeasureSpec(width, childWidthMode);
			} else {
				childWidthSpec = makeMeasureSpec(lp.width, EXACTLY);
			}
			int childHeightSpec;
			if(lp.height != FILL_PARENT && lp.height != WRAP_CONTENT) {
				childHeightSpec = makeMeasureSpec(lp.height, EXACTLY);
			} else {
				childHeightSpec = makeMeasureSpec(height, childHeightMode);
			}
			child.measure(childWidthSpec, childHeightSpec);
			int childTotalWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
			rowWidth += childTotalWidth;
			if(rowWidth > width && i > 0) {
				// break line first
				totalHeight += rowHeight + verticalSpacing;
				rowsHeight.add(rowHeight);
				rowHeight = 0;
				rowWidth = childTotalWidth;
			}
			maxRowWidth = Math.max(rowWidth, maxRowWidth);
			rowHeight = Math.max(child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin, rowHeight);
			if((lp.gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT) {
				// current child will be moved to the right so we force an end of line
				rowWidth = width;
			}
		}
		if(rowHeight != 0) {
			totalHeight += rowHeight;
			rowsHeight.add(rowHeight);
		}
		setMeasuredDimension(
			Math.max(
				getPaddingLeft() + maxRowWidth + getPaddingRight(),
				widthMode != UNSPECIFIED ? getSize(widthMeasureSpec) : 0
			),
			getPaddingTop() + totalHeight + getPaddingBottom()
		);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int total = getChildCount();
		int xstart = getPaddingLeft();
		int xpos = xstart, rowY = getPaddingTop();
		int xmax = r - l - getPaddingRight();
		Iterator<Integer> rowHeightIt = rowsHeight.iterator();
		int rowHeight = rowHeightIt.hasNext() ? rowHeightIt.next() : 0;
		for(int i = 0; i < total; i++) {
			View child = getChildAt(i);
			if(child.getVisibility() == GONE) {
				continue;
			}
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
			int childWidth = child.getMeasuredWidth();
			int childHeight = child.getMeasuredHeight();
			if(xpos > xstart && xpos + childWidth + lp.leftMargin + lp.rightMargin > xmax) {
				// break line
				xpos = xstart;
				rowY += rowHeight + verticalSpacing;
				rowHeight = rowHeightIt.next();
			}
			xpos += lp.leftMargin;
			int childY;
			int vertGravity = lp.gravity & Gravity.VERTICAL_GRAVITY_MASK;
			if(vertGravity == Gravity.CENTER_VERTICAL) {
				childY = rowY + lp.topMargin + (rowHeight - lp.topMargin - lp.bottomMargin - childHeight)/2;
			} else if(vertGravity == Gravity.BOTTOM) {
				childY = rowY + rowHeight - lp.bottomMargin - childHeight;
			} else {
				childY = rowY + lp.topMargin;				
			}
			if((lp.gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT) {
				int xspaceLeft = xmax - xpos - childWidth - lp.rightMargin;
				xpos += xspaceLeft;
			}
			child.layout(xpos, childY, xpos + childWidth, childY + childHeight);
			xpos += childWidth + lp.rightMargin;
		}
	}
	
	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}
	
	@Override
	protected boolean checkLayoutParams(LayoutParams p) {
		return (p instanceof FrameLayout.LayoutParams);
	}
	
	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new FrameLayout.LayoutParams(getContext(), attrs);
	}
}
