package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.graphics.Canvas;
import android.view.MotionEvent;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewInflationContext;

public class ViewGroupStrategyChainRoot implements ViewGroupStrategy {

    private final ViewGroupInternals viewGroupInternals;

    public ViewGroupStrategyChainRoot(ViewGroupInternals viewGroupInternals) {
        this.viewGroupInternals = viewGroupInternals;
    }

    @Override
    public void initStrategy(ViewInflationContext viewInflationContext) {}

    @Override
    public <T> T extractNature(Class<T> natureClass) {
        throw new IllegalStateException("Nature " + natureClass.getSimpleName() + " not found in strategy chain");
    }

    @Override
    public ViewGroupInternals internals() {
        return viewGroupInternals;
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b, OnLayoutSuperCall superCall) {
        superCall.onLayout(changed, l, t, r, b);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh, OnSizeChangedSuperCall superCall) {
        superCall.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev, OnInterceptTouchEventSuperCall superCall) {
        return superCall.onInterceptTouchEvent(ev);
    }

    @Override
    public int getChildDrawingOrder(int childCount, int i, GetChildDrawingOrderSuperCall superCall) {
        return superCall.getChildDrawingOrder(childCount, i);
    }

    @Override
    public void onDraw(Canvas canvas, OnDrawSuperCall superCall) {
        superCall.onDraw(canvas);
    }

    @Override
    public void buildDrawingCache(BuildDrawingCacheSuperCall superCall) {
        superCall.buildDrawingCache();
    }

    @Override
    public void buildDrawingCache(boolean autoScale, BuildDrawingCacheSuperCall superCall) {
        superCall.buildDrawingCache(autoScale);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec, OnMeasureSuperCall superCall) {
        superCall.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt, OnScrollChangedSuperCall superCall) {
        superCall.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev, DispatchTouchEventSuperCall superCall) {
        return superCall.dispatchTouchEvent(ev);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom, SetPaddingSuperCall superCall) {
        superCall.setPadding(left, top, right, bottom);
    }

    @Override
    public void onFinishInflate(OnFinishInflateSuperCall superCall) {
        superCall.onFinishInflate();
    }
}
