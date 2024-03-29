package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.common.IntUtils;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.ReflectionUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SimpleSheetElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.AdjustableSizeImage;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.IntegerSpinner;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptableTouch;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptableTouch.InterceptTouchDelegate;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LinedSheetElementView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.IntegerSpinner.*;

public class TimeStepDialog extends DialogFragment {
	
	public static interface OnPromptResult {
		void onResult(TimeStep enteredValue);
	}
	
	private static LogUtils log = new LogUtils(TimeStepDialog.class);
	private static AdjustableSizeImage commonTime;
	private static AdjustableSizeImage cutCommonTime;
	
	private int selected;
	private Point spinners = new Point(1, 1);
	
	private static void setupResources(Context ctx) throws LoadingSvgException {
		if(commonTime == null)
			commonTime = NotePartFactory.prepareAdujstableImage(ctx, R.array.svg_timesignature_commontime, false);		
		if(cutCommonTime == null)
			cutCommonTime = NotePartFactory.prepareAdujstableImage(ctx, R.array.svg_timesignature_cutcommontime, false);		
	}

	public static TimeStepDialog newInstance(Context ctx, TimeStep value) throws LoadingSvgException {
		log.d("::newInstance()");
		setupResources(ctx);
		TimeStepDialog f = new TimeStepDialog();
		int selected;
		if(value == null) {
			selected = R.id.EDIT_dialog_timestep_noneSet;
		} else if(value == TimeStep.commonTime) {
			selected = R.id.EDIT_dialog_timestep_commonTime;
		} else if(value == TimeStep.cutCommonTime) { 
			selected = R.id.EDIT_dialog_timestep_cutCommonTime;
		} else {
			selected = R.id.EDIT_dialog_timestep_custom;
			f.spinners.set(value.getBaseMultiplier(), 1 << value.getTempoBaseLength());
		}
		f.selected = selected;
        return f;
    }
	
	private static final String KEY_SELECTED = "selectedContainerId";
	private static final String KEY_SPINNERS_X = "spinnersX";
	private static final String KEY_SPINNERS_Y = "spinnersY";
	
	private void onRestoreInstanceState(Bundle bundle) {
		log.d("::onRestoreInstanceState()");
		selected = bundle.getInt(KEY_SELECTED, selected);
		spinners.x = bundle.getInt(KEY_SPINNERS_X, spinners.x);
		spinners.y = bundle.getInt(KEY_SPINNERS_Y, spinners.y);
	}
	
	@Override
	public void onSaveInstanceState(Bundle bundle) {
		log.d("::onSaveInstanceState()");
		bundle.putInt(KEY_SELECTED, getSelectedContainer());
		bundle.putInt(KEY_SPINNERS_X, 
			getController(getDialog(), R.id.EDIT_dialog_timestep_spinnertop).getValue());
		bundle.putInt(KEY_SPINNERS_Y, 
				getController(getDialog(), R.id.EDIT_dialog_timestep_spinnerbottom).getValue());
		super.onSaveInstanceState(bundle);
	}
	
	private int getSelectedContainer() {
		for (int i = 0; i < RadioGroup.containers.length; i++) {
			int id = RadioGroup.containers[i];
			View container = getDialog().findViewById(id);
			if(radioButton(container).isChecked()) {
				switch(id) {
				case R.id.EDIT_dialog_timestep_noneSet:
				case R.id.EDIT_dialog_timestep_commonTime:
				case R.id.EDIT_dialog_timestep_cutCommonTime:
				case R.id.EDIT_dialog_timestep_custom:
					return id;
				default:
						break;
				}
			}
		}
		log.w("Unable to find selected radio button, returning none TimeStep");
		return R.id.EDIT_dialog_timestep_noneSet;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		log.d("::onCreateDialog()");
		if(savedInstanceState != null)
			onRestoreInstanceState(savedInstanceState);
		
		Activity ctx = getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		LayoutInflater inflater = ctx.getLayoutInflater();
		log.v("Inflating editscreen_dialog_timestep");
		View wrapper = inflater.inflate(R.layout.editscreen_dialog_timestep, null);
		prepare((ViewGroup) wrapper);
		builder
			.setView(wrapper)
			.setTitle(R.string.EDIT_timestepdialog_title)
			.setCancelable(true)
			.setPositiveButton(android.R.string.ok,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Activity act = getActivity();
						if(act == null || !(act instanceof OnPromptResult)) {
							log.w(
								"[OK] received when no activity implementing result interface is bound: %s",
								act
							);
						} else {
							((OnPromptResult) act).onResult(getValue(getDialog()));
						}
					}
				}
			)
			.setNegativeButton(android.R.string.cancel, null);
		AlertDialog dialog = builder.create();
		return dialog;
	}
	
	private void prepare(final ViewGroup wrapper) {
		log.v("::prepare()");
		// custom code for RadioGroup to scroll to reveal whole selected container when selection changes
		RadioGroup group = new RadioGroup(wrapper)  {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				super.onCheckedChanged(buttonView, isChecked);
				if(!isChecked) {
					return;
				}
				ViewGroup scroll = (ViewGroup) wrapper.findViewById(R.id.EDIT_dialog_timestep_scroll);
				// find container
				View container = buttonView;
				while(inArray(RadioGroup.containers, container.getId()) < 0 && container != scroll
						&& container.getParent() instanceof View) {
					container = (View) container.getParent();
				}
				ScrollToOne scrollToOne = new ScrollToOne(scroll, container) {
					@Override
					protected void handle(Point dOffset) {
						// ensure that decendant is visible
						dOffset.offset(-scroll.getScrollX(), -scroll.getScrollY());
						int dx = 0, dy = 0;
						int rightOutside = dOffset.x + scrollDescendant.getWidth() - scroll.getWidth();
						if(dOffset.x < 0) {
							dx = dOffset.x;
						} else if(rightOutside > 0) {
							dx = rightOutside;
						}
						int bottomOutside = dOffset.y + scrollDescendant.getHeight() - scroll.getHeight();
						if(dOffset.y < 0) {
							dy = dOffset.y;
						} else if(bottomOutside > 0) {
							dy = bottomOutside;
						}
						if(scroll instanceof ScrollView && dy != 0) {
							((ScrollView) scroll).smoothScrollBy(dx, dy);
						} else if(scroll instanceof HorizontalScrollView && dx != 0) {
							((HorizontalScrollView) scroll).smoothScrollBy(dx, dy);
						} else if(dx != 0 && dy != 0) {
							scroll.scrollBy(dx, dy);
						}
					}
				};
				scrollToOne.run();
			}

			private int inArray(int[] ids, int id) {
				for (int i = 0; i < ids.length; i++) {
					int curr = ids[i];
					if(curr == id) {
						return i;
					}
				}
				return -1;
			}
		};
		
		for (int i = 0; i < RadioGroup.containers.length; i++) {
			int id = RadioGroup.containers[i];
			View container = wrapper.findViewById(id);
			if(container instanceof InterceptableTouch) {
				((InterceptableTouch) container).setInterceptTouchDelegate(group);
			}
			container.setOnClickListener(group);
			radioButton(container).setOnCheckedChangeListener(group);
		}
		
		Context ctx = wrapper.getContext();
		Resources resources = ctx.getResources();
		SheetParams params = new SheetParams(
			resources.getInteger(R.integer.lineThickness),
			resources.getInteger(R.integer.linespaceThickness)
		);
		int destLineThickness = resources.getDimensionPixelSize(R.dimen.dialog_timestep_icon_linethickness);
		params.setScale(params.getScale()*destLineThickness/params.getLineThickness());
		PaintSetup paint = ExtendedResourcesFactory.createPaintSetup(
			ExtendedResourcesFactory.styleResolver(ctx.getResources()),
			R.style.dialog_timestep_iconPaint
		);
		setupSheetElement(wrapper, paint, params, 
			R.id.EDIT_dialog_timestep_commonTime,
			commonTime
		);
		setupSheetElement(wrapper, paint, params, 
			R.id.EDIT_dialog_timestep_cutCommonTime,
			cutCommonTime
		);
		
		setupNumberSpinner(
			wrapper.findViewById(R.id.EDIT_dialog_timestep_spinnertop),
			new IncrementModel(1).setValue(1).setMinValue(1)
		);
		setupNumberSpinner(
			wrapper.findViewById(R.id.EDIT_dialog_timestep_spinnerbottom),
			new MultiplyModel(2).setValue(1).setMinValue(1).setMaxValue(
				1 << resources.getInteger(R.integer.minNotePossibleValue)
			)
		);
		
		getController(wrapper, R.id.EDIT_dialog_timestep_spinnertop)
		.setValue(spinners.x);
		getController(wrapper, R.id.EDIT_dialog_timestep_spinnerbottom)
		.setValue(spinners.y);
		radioButton(wrapper.findViewById(selected)).setChecked(true);
		
		// scroll to current value view
		final ViewGroup scroll = (ViewGroup) wrapper.findViewById(R.id.EDIT_dialog_timestep_scroll);
		if(scroll != null) {
			scroll.getViewTreeObserver().addOnGlobalLayoutListener(new ScrollToOne(
				scroll, wrapper.findViewById(selected)
			));
		}
	}
	
	private TimeStep getValue(Dialog dialog) {
		for (int i = 0; i < RadioGroup.containers.length; i++) {
			int id = RadioGroup.containers[i];
			View container = dialog.findViewById(id);
			if(radioButton(container).isChecked()) {
				switch(id) {
				case R.id.EDIT_dialog_timestep_noneSet:
					return null;
				case R.id.EDIT_dialog_timestep_commonTime:
					return TimeStep.commonTime;
				case R.id.EDIT_dialog_timestep_cutCommonTime:
					return TimeStep.cutCommonTime;
				case R.id.EDIT_dialog_timestep_custom:
					return new TimeStep(
						getController(container, R.id.EDIT_dialog_timestep_spinnertop)
						.getValue(),
						IntUtils.log2(
						getController(container, R.id.EDIT_dialog_timestep_spinnerbottom)
						.getValue()
						)
					);
				default:
					break;
				}
			}
		}
		log.w("Unable to find selected radio button, returning none TimeStep");
		return null;
	}	
	
	private static class ScrollToOne implements OnGlobalLayoutListener {
		protected ViewGroup scroll;
		protected View scrollDescendant;
		
		public ScrollToOne(ViewGroup scroll, View scrollDecendant) {
			super();
			this.scroll = scroll;
			this.scrollDescendant = scrollDecendant;
		}

		@Override
		public void onGlobalLayout() {
			scroll.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			run();
		}
		
		public void run() {			
			View current = scrollDescendant;
			Point offset = new Point(current.getLeft(), current.getTop());
			while(current.getParent() != scroll) {
				ViewParent parent = current.getParent();
				if(!(parent instanceof View))
					break;
				current = (View) parent;
				offset.offset(current.getLeft(), current.getTop());
			}
			if(current.getParent() != scroll) {
				log.w("%s not a descendant of %s",
					ReflectionUtils.findConstName(R.id.class, "", scrollDescendant.getId()),
					ReflectionUtils.findConstName(R.id.class, "", scroll.getId())
				);
			} else {
				handle(offset);
				log.v("scrolled to "+offset);
			}
		}

		protected void handle(Point descendantOffset) {
			scroll.scrollTo(descendantOffset.x, descendantOffset.y);
		}
	}

	private static void setupSheetElement(ViewGroup wrapper, PaintSetup paint, SheetParams params, int containerId, AdjustableSizeImage image) {
		LinedSheetElementView view = (LinedSheetElementView) (wrapper.findViewById(containerId)
			.findViewById(R.id.EDIT_dialog_timestep_special_sheetelement));
		view.setPaint(paint.paint, paint.drawRadius);
		String padding = wrapper.getContext().getResources().getString(R.string.dialog_timestep_icon_horizontalpadding);
		view.setLinesHorizontalPadding((int) params.readParametrizedValue(padding));
		view.setSheetParams(params);
		view.setFrontModel(new SimpleSheetElement(
			image
		));
	}
	
	static private void setupNumberSpinner(View wrapper, IntegerSpinnerModel model) {
		IntegerSpinnerController ctl = new IntegerSpinnerController(model, wrapper);
		wrapper.setTag(ctl);
	}
	
	static private IntegerSpinnerController getController(View wrapper, int spinnerViewId) {
		return (IntegerSpinnerController) wrapper.findViewById(spinnerViewId).getTag();
	}
	static private IntegerSpinnerController getController(Dialog wrapper, int spinnerViewId) {
		return (IntegerSpinnerController) wrapper.findViewById(spinnerViewId).getTag();
	}
	
	private static class IntegerSpinnerController extends IntegerSpinner.IntegerSpinnerController {
		private TextView label;

		IntegerSpinnerController(IntegerSpinnerModel model, View wrapper) {
			super(model, wrapper, R.id.numberspinner_button_more, R.id.numberspinner_button_less);
			this.label = (TextView) wrapper.findViewById(R.id.numberspinner_label);
			updateViews();
		}

		public int getValue() {
			return model.getValue();
		}

		public void setValue(int value) {
			model.setValue(value);
			updateViews();
		}

		@Override
		protected void updateViews() {
			if(model != null && model.getValue() != null && label != null) {
				label.setText(Integer.toString(model.getValue()));
			}
			super.updateViews();
		}

	}

	static CompoundButton radioButton(Context ctx, View container) {
		return (CompoundButton) container.findViewWithTag(ctx.getString(R.string.dialog_timestep_radiotag));
	}
	private CompoundButton radioButton(View container) {
		return radioButton(getActivity(), container);
	}
	
	private static class RadioGroup implements View.OnClickListener, OnCheckedChangeListener, InterceptTouchDelegate {
		static int[] containers = new int[] {
			R.id.EDIT_dialog_timestep_noneSet,
			R.id.EDIT_dialog_timestep_commonTime,
			R.id.EDIT_dialog_timestep_cutCommonTime,
			R.id.EDIT_dialog_timestep_custom
		};
		private ViewGroup wrapper;
		public RadioGroup(ViewGroup wrapper) {
			this.wrapper = wrapper;
		}
		@Override
		public void onClick(View v) {
			radioButton(v).setChecked(true);
		}
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(!isChecked)
				return;
			for (int i = 0; i < containers.length; i++) {
				int id = containers[i];
				CompoundButton radioB = radioButton(wrapper.findViewById(id));
				if(buttonView != radioB) {
					radioB.setChecked(false);
				}
			}
		}
		
		private CompoundButton radioButton(View container) {
			return TimeStepDialog.radioButton(wrapper.getContext(), container);
		}
		
		@Override
		public boolean onInterceptTouchEvent(View interceptableView, MotionEvent ev) {
			if(ev.getActionMasked() == MotionEvent.ACTION_UP) {
				onClick(interceptableView);
			}
			return false;
		}
	}
	
}
