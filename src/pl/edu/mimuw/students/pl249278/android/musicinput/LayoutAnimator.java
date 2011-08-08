package pl.edu.mimuw.students.pl249278.android.musicinput;

import java.util.ArrayList;
import java.util.Iterator;

import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class LayoutAnimator<ContextType> implements Runnable {
	private static Interpolator interpolator = new AccelerateDecelerateInterpolator();
	private ContextType ctx = null;
	
	public LayoutAnimator(ContextType ctx) {
		super();
		this.ctx = ctx;
	}
	protected static abstract class LayoutAnimation<ContextType, ViewType extends View> {
		protected int start_value;
		protected int delta;
		private long duration;
		private long elapsed = 0;
		protected ViewType view;
		protected Runnable onAnimationEndListener = null;
		
		public LayoutAnimation(ViewType view, int start_value, int delta, long duration, Runnable onAnimationEndListener) {
			this(view, start_value, delta, duration);
			this.onAnimationEndListener = onAnimationEndListener;
		}
		public LayoutAnimation(ViewType view, int start_value, int delta, long duration) {
			this.view = view;
			this.start_value = start_value;
			this.delta = delta;
			this.duration = duration;
		}
		public void apply(ContextType ctx) {
			apply(ctx, interpolator.getInterpolation(((float) elapsed)/duration));
		}
		protected abstract void apply(ContextType ctx, float state);
		public int startValue() {
			return start_value;
		}
		public int destValue() {
			return start_value+delta;
		}
		public boolean isFinished() {
			return elapsed == duration;
		}
	}
	
	private ArrayList<LayoutAnimation<ContextType, ?>> animations = new ArrayList<LayoutAnimator.LayoutAnimation<ContextType, ?>>();
	private Handler mHandler = new Handler();
	private boolean mIsRunning = false;
	private long lastticktime;
	
	protected void mStartAnimation(LayoutAnimation<ContextType, ?> anim) {
		animations.add(anim);
		if(!mIsRunning) {
			lastticktime = System.currentTimeMillis(); 
			mHandler.post(this);
			mIsRunning = true;
		}
	}
	
	public void forceFinishAll() {
		if(mIsRunning) {
			mHandler.removeCallbacks(this);
			mIsRunning = false;
		}
		if(!animations.isEmpty()) {
			for (LayoutAnimation<ContextType, ?> anim: animations) {
				anim.elapsed = anim.duration;
				anim.apply(ctx);
			}
			animations.clear();
		}
	}

	public void stopAnimation(LayoutAnimation<ContextType, ?> anim) {
		animations.remove(anim);
		if(animations.isEmpty()) {
			mHandler.removeCallbacks(this);
			mIsRunning = false;
		}
	}

	public LayoutAnimation<ContextType, ?> getAnimation(View view) {
		for (LayoutAnimation<ContextType, ?> anim : animations) {
			if(anim.view == view) return anim;
		}
		return null;
	}

	private ArrayList<LayoutAnimation<ContextType, ?>> temp = new ArrayList<LayoutAnimation<ContextType, ?>>();
	@Override
	public void run() {
		long currTime = System.currentTimeMillis();
		long tick = currTime-lastticktime;
		lastticktime = currTime;
		for (Iterator<LayoutAnimation<ContextType, ?>> it = animations.iterator(); it.hasNext();) {
			LayoutAnimation<ContextType, ?> anim = (LayoutAnimation<ContextType, ?>) it.next();
			anim.elapsed = Math.min(anim.elapsed+tick, anim.duration);
			anim.apply(ctx);
			if(anim.isFinished()) {
				it.remove();
				temp.add(anim);
			}
		}
		mIsRunning = !animations.isEmpty();
		if(mIsRunning) {
			mHandler.post(this);
		}
		for(LayoutAnimation<ContextType, ?> anim: temp) {
			if(anim.onAnimationEndListener != null) {
				anim.onAnimationEndListener.run();
			}
		}
		temp.clear();
	}
	
}
