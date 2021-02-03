package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.viewgroup;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewInflationContext;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupInternals;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategy;

public class HorizontalScrollView_WithMixin extends HorizontalScrollView {

    protected ViewGroupStrategy mixin;

    public HorizontalScrollView_WithMixin(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScrollView_WithMixin(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void initMixin(ViewGroupStrategy mixin, ViewInflationContext viewInflationContext) {
        this.mixin = mixin;
        mixin.initStrategy(viewInflationContext);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mixin.onLayout(changed, l, t, r, b, new ViewGroupStrategy.OnLayoutSuperCall() {
            @Override
            public void onLayout(boolean changed, int l, int t, int r, int b) {
                HorizontalScrollView_WithMixin.super.onLayout(changed, l, t, r, b);
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mixin.onSizeChanged(w, h, oldw, oldh, new ViewGroupStrategy.OnSizeChangedSuperCall() {
            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                HorizontalScrollView_WithMixin.super.onSizeChanged(w, h, oldw, oldh);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mixin.onInterceptTouchEvent(ev, new ViewGroupStrategy.OnInterceptTouchEventSuperCall() {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return HorizontalScrollView_WithMixin.super.onInterceptTouchEvent(ev);
            }
        });
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        return mixin.getChildDrawingOrder(childCount, i, new ViewGroupStrategy.GetChildDrawingOrderSuperCall() {
            @Override
            public int getChildDrawingOrder(int childCount, int i) {
                return HorizontalScrollView_WithMixin.super.getChildDrawingOrder(childCount, i);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mixin.onDraw(canvas, new ViewGroupStrategy.OnDrawSuperCall() {
            @Override
            public void onDraw(Canvas canvas) {
                HorizontalScrollView_WithMixin.super.onDraw(canvas);
            }
        });
    }

    @Override
    public void buildDrawingCache() {
        mixin.buildDrawingCache(buildDrawingCacheSuperCall());
    }

    @Override
    public void buildDrawingCache(boolean autoScale) {
        mixin.buildDrawingCache(autoScale, buildDrawingCacheSuperCall());
    }

    private ViewGroupStrategy.BuildDrawingCacheSuperCall buildDrawingCacheSuperCall() {
        return new ViewGroupStrategy.BuildDrawingCacheSuperCall() {
            @Override
            public void buildDrawingCache() {
                HorizontalScrollView_WithMixin.super.buildDrawingCache();
            }

            @Override
            public void buildDrawingCache(boolean autoScale) {
                HorizontalScrollView_WithMixin.super.buildDrawingCache(autoScale);
            }
        };
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mixin.onMeasure(widthMeasureSpec, heightMeasureSpec, new ViewGroupStrategy.OnMeasureSuperCall() {
            @Override
            public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                HorizontalScrollView_WithMixin.super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        });
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        mixin.onScrollChanged(l, t, oldl, oldt, new ViewGroupStrategy.OnScrollChangedSuperCall() {
            @Override
            public void onScrollChanged(int l, int t, int oldl, int oldt) {
                HorizontalScrollView_WithMixin.super.onScrollChanged(l, t, oldl, oldt);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mixin.dispatchTouchEvent(ev, new ViewGroupStrategy.DispatchTouchEventSuperCall() {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                return HorizontalScrollView_WithMixin.super.dispatchTouchEvent(ev);
            }
        });
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        mixin.setPadding(left, top, right, bottom, new ViewGroupStrategy.SetPaddingSuperCall() {
            @Override
            public void setPadding(int left, int top, int right, int bottom) {
                HorizontalScrollView_WithMixin.super.setPadding(left, top, right, bottom);
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        mixin.onFinishInflate(new ViewGroupStrategy.OnFinishInflateSuperCall() {
            @Override
            public void onFinishInflate() {
                HorizontalScrollView_WithMixin.super.onFinishInflate();
            }
        });
    }

    public class Internals implements ViewGroupInternals {
        @Override
        public void setChildrenDrawingOrderEnabled(boolean enabled) {
            HorizontalScrollView_WithMixin.super.setChildrenDrawingOrderEnabled(enabled);
        }

        @Override
        public View viewObject() {
            return HorizontalScrollView_WithMixin.this;
        }

        @Override
        public boolean super_dispatchTouchEvent(MotionEvent ev) {
            return HorizontalScrollView_WithMixin.super.dispatchTouchEvent(ev);
        }

        @Override
        public void super_setPadding(int left, int top, int right, int bottom) {
            HorizontalScrollView_WithMixin.super.setPadding(left, top, right, bottom);
        }
    }

}
