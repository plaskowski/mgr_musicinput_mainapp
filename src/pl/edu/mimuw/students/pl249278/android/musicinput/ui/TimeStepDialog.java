package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.ReflectionUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SimpleSheetElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.AdjustableSizeImage;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.InterceptableView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.InterceptableView.InterceptTouchDelegate;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LinedSheetElementView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

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
			commonTime = NotePartFactory.prepareAdujstableImage(ctx, R.xml.timesignature_commontime, false);		
		if(cutCommonTime == null)
			cutCommonTime = NotePartFactory.prepareAdujstableImage(ctx, R.xml.timesignature_cutcommontime, false);		
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
			.setPositiveButton(R.string.dialog_button_ok,
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
			.setNegativeButton(R.string.dialog_button_cancel, null);
		AlertDialog dialog = builder.create();
		return dialog;
	}
	
	private void prepare(ViewGroup wrapper) {
		log.v("::prepare()");
		
		RadioGroup group = new RadioGroup(wrapper);
		for (int i = 0; i < RadioGroup.containers.length; i++) {
			int id = RadioGroup.containers[i];
			View container = wrapper.findViewById(id);
			if(container instanceof InterceptableView) {
				((InterceptableView) container).setInterceptTouchDelegate(group);
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
		Paint paint = ExtendedResourcesFactory.createPaint(
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
			new MultiplyModel(2).setValue(1).setMinValue(1)
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
						log2(
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
	
	private static int log2(int value) {
		return 31 - Integer.numberOfLeadingZeros(value);
	}
	
	private static class ScrollToOne implements OnGlobalLayoutListener {
		private ViewGroup scroll;
		private View scrollDecendant;
		
		public ScrollToOne(ViewGroup scroll, View scrollDecendant) {
			super();
			this.scroll = scroll;
			this.scrollDecendant = scrollDecendant;
		}

		@Override
		public void onGlobalLayout() {
			scroll.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			
			View current = scrollDecendant;
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
					ReflectionUtils.findConstName(R.id.class, "", scrollDecendant.getId()),
					ReflectionUtils.findConstName(R.id.class, "", scroll.getId())
				);
			} else {
				scroll.scrollTo(offset.x, offset.y);
				log.v("scrolled to "+offset);
			}
		}
	}
	

	private static void setupSheetElement(ViewGroup wrapper, Paint paint, SheetParams params, int containerId, AdjustableSizeImage image) {
		LinedSheetElementView view = (LinedSheetElementView) (wrapper.findViewById(containerId)
			.findViewById(R.id.EDIT_dialog_timestep_special_sheetelement));
		view.setPaint(paint);
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
	
	private static class MultiplyModel extends IntegerSpinnerModel {
		private int factor;

		public MultiplyModel(int factor) {
			this.factor = factor;
		}

		@Override
		protected Integer prevValue(int value) {
			return value/factor;
		}

		@Override
		protected Integer nextValue(int value) {
			return value*factor;
		}
		
	}
	
	private static class IncrementModel extends IntegerSpinnerModel {
		private int step;

		public IncrementModel(int step) {
			this.step = step;
		}

		@Override
		protected Integer prevValue(int value) {
			return value-step;
		}

		@Override
		protected Integer nextValue(int value) {
			return value+step;
		}
	}
	
	private static abstract class IntegerSpinnerModel {
		private int value;
		private Integer minValue;

		protected abstract Integer prevValue(int value);
		protected abstract Integer nextValue(int value);
		
		public void nextValue() {
			value = nextValue(value);
		}
		
		public void prevValue() {
			if(hasPrev())
				value = prevValue(value);
		}
		public boolean hasPrev() {
			return minValue == null || prevValue(value) >= minValue;
		}
		
		/**
		 * @return self
		 */
		public IntegerSpinnerModel setMinValue(Integer minValue) {
			this.minValue = minValue;
			if(minValue != null)
				value = Math.max(minValue, value);
			return this;
		}
		
		/**
		 * @return self
		 */
		public IntegerSpinnerModel setValue(int value) {
			this.value = value;
			if(minValue != null) 
				this.value = Math.max(minValue, value);
			return this;
		}
		
		public int getValue() {
			return value;
		}
	}
	
	private static class IntegerSpinnerController implements View.OnClickListener {
		private TextView label;
		private View less, more;
		private IntegerSpinnerModel model;

		IntegerSpinnerController(IntegerSpinnerModel model, View wrapper) {
			this.model = model;
			less = wrapper.findViewById(R.id.numberspinner_button_less);
			less.setOnClickListener(this);
			more = wrapper.findViewById(R.id.numberspinner_button_more);
			more.setOnClickListener(this);
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
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.numberspinner_button_less:
				model.prevValue();
				break;
			case R.id.numberspinner_button_more:
				model.nextValue();
				break;
			default:
				throw new RuntimeException();
			}
			updateViews();
		}

		private void updateViews() {
			label.setText(Integer.toString(model.getValue()));
			less.setEnabled(model.hasPrev());
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
