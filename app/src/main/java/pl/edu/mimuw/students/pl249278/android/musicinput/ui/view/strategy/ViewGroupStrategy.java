package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewInflationContext;

public interface ViewGroupStrategy {

    void initStrategy(ViewInflationContext viewInflationContext);

    <T> T extractNature(Class<T> natureClass);

    ViewGroupInternals internals();

    /**
     * See {@link View#onLayout(boolean, int, int, int, int)}.
     */
    void onLayout(boolean changed, int l, int t, int r, int b, OnLayoutSuperCall superCall);

    interface OnLayoutSuperCall {
        void onLayout(boolean changed, int l, int t, int r, int b);
    }

    /**
     * See {@link View#onSizeChanged(int, int, int, int)}.
     */
    void onSizeChanged(int w, int h, int oldw, int oldh, OnSizeChangedSuperCall superCall);

    interface OnSizeChangedSuperCall {
        void onSizeChanged(int w, int h, int oldw, int oldh);
    }

    /**
     * See {@link android.view.ViewGroup#onInterceptTouchEvent(MotionEvent)}.
     */
    boolean onInterceptTouchEvent(MotionEvent ev, OnInterceptTouchEventSuperCall superCall);

    interface OnInterceptTouchEventSuperCall {
        boolean onInterceptTouchEvent(MotionEvent ev);
    }

    int getChildDrawingOrder(int childCount, int i, GetChildDrawingOrderSuperCall superCall);

    interface GetChildDrawingOrderSuperCall {
        int getChildDrawingOrder(int childCount, int i);
    }

    void onDraw(Canvas canvas, OnDrawSuperCall superCall);

    interface OnDrawSuperCall {
        void onDraw(Canvas canvas);
    }

    void buildDrawingCache(BuildDrawingCacheSuperCall superCall);

    void buildDrawingCache(boolean autoScale, BuildDrawingCacheSuperCall superCall);

    interface BuildDrawingCacheSuperCall {
        void buildDrawingCache();
        void buildDrawingCache(boolean autoScale);
    }

    void onMeasure(int widthMeasureSpec, int heightMeasureSpec, OnMeasureSuperCall superCall);

    interface OnMeasureSuperCall {
        void onMeasure(int widthMeasureSpec, int heightMeasureSpec);
    }

    void onScrollChanged(int l, int t, int oldl, int oldt, OnScrollChangedSuperCall superCall);

    interface OnScrollChangedSuperCall {
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }

    boolean dispatchTouchEvent(MotionEvent ev, DispatchTouchEventSuperCall superCall);

    interface DispatchTouchEventSuperCall {
        boolean dispatchTouchEvent(MotionEvent ev);
    }

    void setPadding(int left, int top, int right, int bottom, SetPaddingSuperCall superCall);

    interface SetPaddingSuperCall {
        void setPadding(int left, int top, int right, int bottom);
    }

    void onFinishInflate(OnFinishInflateSuperCall superCall);

    interface OnFinishInflateSuperCall {
        void onFinishInflate();
    }

}