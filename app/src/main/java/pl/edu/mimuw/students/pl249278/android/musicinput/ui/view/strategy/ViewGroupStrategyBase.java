package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;

import com.google.common.base.Preconditions;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewInflationContext;

public class ViewGroupStrategyBase implements ViewGroupStrategy {

    private final ViewGroupStrategy parent;

    public ViewGroupStrategyBase(ViewGroupStrategy parent) {
        this.parent = parent;
    }

    @CallSuper
    @Override
    public void initStrategy(ViewInflationContext viewInflationContext) {
        parent.initStrategy(viewInflationContext);
    }

    protected void checkThatViewImplements(Class<?> interfaceClass) {
        View viewObject = internals().viewObject();
        Preconditions.checkState(interfaceClass.isInstance(viewObject),
                "View %s has to implement %s to use %s",
                viewObject.getClass().getSimpleName(),
                interfaceClass.getSimpleName(),
                this.getClass().getSimpleName());
    }

    @Override
    public ViewGroupInternals internals() {
        return parent.internals();
    }

    protected int getChildCount() {
        return ((ViewGroup) internals().viewObject()).getChildCount();
    }

    protected View getChildAt(int index) {
        return ((ViewGroup) internals().viewObject()).getChildAt(index);
    }

    protected Context getContext() {
        return internals().viewObject().getContext();
    }

    @Override
    public <T> T extractNature(Class<T> natureClass) {
        if (natureClass.isInstance(this)) {
            return natureClass.cast(this);
        } else {
            return parent.extractNature(natureClass);
        }
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b, OnLayoutSuperCall superCall) {
        parent.onLayout(changed, l, t, r, b, superCall);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh, OnSizeChangedSuperCall superCall) {
        parent.onSizeChanged(w, h, oldw, oldh, superCall);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev, OnInterceptTouchEventSuperCall superCall) {
        return parent.onInterceptTouchEvent(ev, superCall);
    }

    @Override
    public int getChildDrawingOrder(int childCount, int i, GetChildDrawingOrderSuperCall superCall) {
        return parent.getChildDrawingOrder(childCount, i, superCall);
    }

    @Override
    public void onDraw(Canvas canvas, OnDrawSuperCall superCall) {
        parent.onDraw(canvas, superCall);
    }

    @Override
    public void buildDrawingCache(BuildDrawingCacheSuperCall superCall) {
        parent.buildDrawingCache(superCall);
    }

    @Override
    public void buildDrawingCache(boolean autoScale, BuildDrawingCacheSuperCall superCall) {
        parent.buildDrawingCache(autoScale, superCall);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec, OnMeasureSuperCall superCall) {
        parent.onMeasure(widthMeasureSpec, heightMeasureSpec, superCall);
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt, OnScrollChangedSuperCall superCall) {
        parent.onScrollChanged(l, t, oldl, oldt, superCall);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev, DispatchTouchEventSuperCall superCall) {
        return parent.dispatchTouchEvent(ev, superCall);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom, SetPaddingSuperCall superCall) {
        parent.setPadding(left, top, right, bottom, superCall);
    }

    @Override
    public void onFinishInflate(OnFinishInflateSuperCall superCall) {
        parent.onFinishInflate(superCall);
    }
}
