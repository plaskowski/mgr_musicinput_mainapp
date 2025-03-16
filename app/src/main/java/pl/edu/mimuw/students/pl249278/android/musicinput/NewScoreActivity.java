package pl.edu.mimuw.students.pl249278.android.musicinput;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import pl.edu.mimuw.students.pl249278.android.async.AsyncHelper;
import pl.edu.mimuw.students.pl249278.android.common.IntUtils;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.mixin.AppCompatActivityWithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy.ActivityStrategyChainRoot;
import pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy.ErrorDialogStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.Clef;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.KeySignature;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreContentFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfig;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfigFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.SerializationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.ContentService;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.FilterByRequestIdReceiver;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InfoDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InfoDialog.InfoDialogListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.KeySignatureElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SimpleSheetElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.Tempo;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.AdjustableSizeImage;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.IntegerSpinner;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.IntegerSpinner.IncrementModel;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.IntegerSpinner.IntegerSpinnerModel;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.IntegerSpinner.MultiplyModel;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LinedSheetElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewUtils.OnLayoutListener;

public class NewScoreActivity extends AppCompatActivityWithMixin implements InfoDialogListener {
	private static LogUtils log = new LogUtils(NewScoreActivity.class);
	private static final Clef defaultClef = Clef.VIOLIN;
	private static final KeySignature defaultKeySign = KeySignature.C_DUR;
	private static final TimeStep defaultCustomTimeSign = new TimeStep(1 << NoteConstants.LEN_QUATERNOTE, NoteConstants.LEN_QUATERNOTE);
	private static final TimeSignatureType defaultTimeSignatureType = TimeSignatureType.CUSTOM;
	protected static final int REQUEST_CODE_VISCONF = 1;
	private InsertRequestReceiver insertRequestReceiver;
	private ScoreVisualizationConfig visConf;
	private static final KeySignature[] KeySignature_values = KeySignature.values();
	private final ErrorDialogStrategy errorDialogStrategy;

	public NewScoreActivity() {
		errorDialogStrategy = new ErrorDialogStrategy(new ActivityStrategyChainRoot(this));
		initMixin(errorDialogStrategy);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newscore);
		visConf = ScoreVisualizationConfigFactory.createWithDefaults(this);
		
		Resources res = getResources();
		SheetParams params = new SheetParams(
			res.getInteger(R.integer.lineThickness), res.getInteger(R.integer.linespaceThickness)
		);
		float destLineThickness = res.getDimensionPixelSize(R.dimen.NEWSCORE_thumbs_lineThickness);
		params.setScale(destLineThickness / params.getLineThickness());
		RadioGroup radioGroup = new RadioGroup(), clefsRadioGroup = new RadioGroup() {
			@Override
			public void onClick(View radioView) {
				super.onClick(radioView);
				updateKeySignatureModels();
			}
		};
		
		// fill clefs images
		ViewGroup clefsContainer = (ViewGroup) findViewById(R.id.NEWSCORE_clefs_container);
		for (int i = 0; i < Clef.values().length; i++) {
			Clef clef = Clef.values()[i];
			try {
				AdjustableSizeImage img = NotePartFactory.prepareClefImage(this, clef);
				SheetElement model = new SimpleSheetElement(img);
				LinedSheetElementView sheetElementView = setupThumbnailView(params, clefsContainer, model);
				sheetElementView.setOnClickListener(clefsRadioGroup);
				sheetElementView.setTag(clef);
			} catch (LoadingSvgException e) {
				log.e("Failed to prepare clef "+clef.name(), e);
				showErrorDialog(R.string.errormsg_unrecoverable, e, true);
				return;
			}
		}
		alignVertically(clefsContainer);
		RadioGroup.select(findViewByEnumTag(clefsContainer, defaultClef));
		
		// fill key signature images
		ViewGroup keysContainer = (ViewGroup) findViewById(R.id.NEWSCORE_keys_container);
		for (int i = 0; i < KeySignature_values.length; i++) {
			KeySignature key = KeySignature_values[i];
			try {
				SheetElement model = new KeySignatureElement(this, defaultClef, key);
				LinedSheetElementView sheetElementView = setupThumbnailView(params, keysContainer, model);
				sheetElementView.setTag(key);
				sheetElementView.setOnClickListener(radioGroup);
			} catch (LoadingSvgException e) {
				log.e("Failed to prepare key signature "+key.name(), e);
				showErrorDialog(R.string.errormsg_unrecoverable, e, true);
				return;
			}
		}
		alignVertically(keysContainer);
		RadioGroup.select(findViewByEnumTag(keysContainer, defaultKeySign));
		((View) keysContainer.getParent()).setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
		
		// setup meter signature widgets
		ViewGroup timeSignContainer = (ViewGroup) findViewById(R.id.NEWSCORE_meter_container);		
		try {
			LinedSheetElementView sheetElementView;
			sheetElementView = setupThumbnailView(params, timeSignContainer, new EmptyElement());
			sheetElementView.setOnClickListener(radioGroup);
			sheetElementView.setTag(TimeSignatureType.NONE);
			sheetElementView = setupThumbnailView(params, timeSignContainer, R.array.svg_timesignature_commontime);
			sheetElementView.setOnClickListener(radioGroup);
			sheetElementView.setTag(TimeSignatureType.COMMON_TIME);
			sheetElementView = setupThumbnailView(params, timeSignContainer, R.array.svg_timesignature_cutcommontime);
			sheetElementView.setOnClickListener(radioGroup);
			sheetElementView.setTag(TimeSignatureType.CUT_COMMON_TIME);
			alignVertically(timeSignContainer);
			timeSignature = setupCustomTimeSignature(params, radioGroup, timeSignContainer);
			timeSignature.setTag(TimeSignatureType.CUSTOM);
			RadioGroup.select(findViewByEnumTag(timeSignContainer, defaultTimeSignatureType));
		} catch (LoadingSvgException e) {
			log.e("Failed to prepare image.", e);
			showErrorDialog(R.string.errormsg_unrecoverable, e, true);
			return;
		}
		
		View okButton = findViewById(R.id.NEWSCORE_okbutton);
		okButton.setOnClickListener(new OkListener());
		findViewById(R.id.NEWSCORE_button_editor_prefs).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), VisualPreferencesActivity.class);
				i.putExtra(VisualPreferencesActivity.START_EXTRAS_VISCONF, visConf);
				startActivityForResult(i, REQUEST_CODE_VISCONF);
			}
		});
	}
	
	private void updateKeySignatureModels() {
		View selectedClef = findSelected((ViewGroup) findViewById(R.id.NEWSCORE_clefs_container));
		if(selectedClef == null) {
			return;
		}
		Clef currentClef = (Clef) selectedClef.getTag();
		ViewGroup keysContainer = (ViewGroup) findViewById(R.id.NEWSCORE_keys_container);
		for (int i = 0; i < KeySignature_values.length; i++) {
			KeySignature key = KeySignature_values[i];
			try {
				SheetElement model = new KeySignatureElement(this, currentClef, key);
				LinedSheetElementView sheetElementView = (LinedSheetElementView) keysContainer.findViewWithTag(key);
				sheetElementView.setFrontModel(model);
			} catch (LoadingSvgException e) {
				log.e("Failed to prepare key signature "+key.name(), e);
				showErrorDialog(R.string.errormsg_unrecoverable, e, true);
				return;
			}
		}
		alignVertically(keysContainer);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case REQUEST_CODE_VISCONF:
			if(resultCode == RESULT_OK) {
				visConf = data.getParcelableExtra(VisualPreferencesActivity.RESULT_EXTRAS_VISCONF);
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		updateKeySignatureModels();
		ViewUtils.addActivityOnLayout(this, new OnLayoutListener() {
			@Override
			public void onLayoutPassed() {
				scrollToSelected((ViewGroup) findViewById(R.id.NEWSCORE_clefs_container));
				scrollToSelected((ViewGroup) findViewById(R.id.NEWSCORE_keys_container), 0.5f, -0.5f);
				scrollToSelected((ViewGroup) findViewById(R.id.NEWSCORE_meter_container));
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		unregisterInsertReceiver();
		super.onDestroy();
	}

	@Override
	public void onDismiss(InfoDialog.InfoDialogDismissalEvent dismissalEvent) {
		mixin.onCustomEvent(dismissalEvent);
	}

	private void unregisterInsertReceiver() {
		if(insertRequestReceiver != null) {
			unregisterReceiver(insertRequestReceiver);
			insertRequestReceiver = null;
		}
	}
	
	private static ThreadLocal<Rect> threadLocal = new ThreadLocal<Rect>() { protected Rect initialValue() { return new Rect(); } };
	private void scrollToSelected(ViewGroup wrapper, float childWidthFactor, float scrollWidthFactor) {
		View selectedChild = findSelected(wrapper);
		if(selectedChild == null)
			return;
		Rect rectangle = threadLocal.get();
		selectedChild.getHitRect(rectangle);
		rectangle.offset((int) (selectedChild.getWidth()*childWidthFactor), 0);
		rectangle.offset(wrapper.getPaddingLeft(), wrapper.getPaddingTop());
		View scroll = (View) wrapper.getParent();
		rectangle.offset(-scroll.getHorizontalFadingEdgeLength(), -scroll.getVerticalFadingEdgeLength());
		rectangle.offset((int) (scroll.getWidth()*scrollWidthFactor), 0);
		scroll.scrollTo(rectangle.left, rectangle.top);
	}
	
	private void scrollToSelected(ViewGroup wrapper) {
		scrollToSelected(wrapper, 0, 0);
	}
	
	private static final String INSTANCE_EXTRA_BASE = "metrum_base";
	private static final String INSTANCE_EXTRA_MULTIPLIER = "metrum_multiplier";
	private static final String INSTANCE_EXTRA_CLEF = "clef";
	private static final String INSTANCE_EXTRA_KEY = "key";
	private static final String INSTANCE_EXTRA_METRUM_TYPE = "metrum_signature_type";
	private static final String INSTANCE_EXTRA_INSERT_REQUEST_ID = "insert_requestid";
	private static final String INSTANCE_VISCONF = "visconf";
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(INSTANCE_VISCONF, visConf);
		outState.putInt(INSTANCE_EXTRA_BASE, meterSignBaseCtrl.getValue());
		outState.putInt(INSTANCE_EXTRA_MULTIPLIER, meterSignMultipierCtrl.getValue());
		saveSelectedViewEnumTag(outState, R.id.NEWSCORE_clefs_container, INSTANCE_EXTRA_CLEF);
		saveSelectedViewEnumTag(outState, R.id.NEWSCORE_keys_container, INSTANCE_EXTRA_KEY);
		saveSelectedViewEnumTag(outState, R.id.NEWSCORE_meter_container, INSTANCE_EXTRA_METRUM_TYPE);
    	if(insertRequestReceiver != null) {
    		// save information about insert request we've already sent
    		outState.putString(
				INSTANCE_EXTRA_INSERT_REQUEST_ID, 
				insertRequestReceiver.getCurrentRequestId()
			);
    		unregisterInsertReceiver();
    	}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if(savedInstanceState == null) return;
		visConf = savedInstanceState.getParcelable(INSTANCE_VISCONF);
		meterSignBaseCtrl.setValue(savedInstanceState.getInt(INSTANCE_EXTRA_BASE, meterSignBaseCtrl.getValue()));
		meterSignMultipierCtrl.setValue(savedInstanceState.getInt(INSTANCE_EXTRA_MULTIPLIER, meterSignMultipierCtrl.getValue()));
		selectRadioViewByEnumTag(savedInstanceState, INSTANCE_EXTRA_CLEF, R.id.NEWSCORE_clefs_container);
		selectRadioViewByEnumTag(savedInstanceState, INSTANCE_EXTRA_KEY, R.id.NEWSCORE_keys_container);
		selectRadioViewByEnumTag(savedInstanceState, INSTANCE_EXTRA_METRUM_TYPE, R.id.NEWSCORE_meter_container);
		
		if(savedInstanceState.containsKey(INSTANCE_EXTRA_INSERT_REQUEST_ID)) {
        	// we requested INSERT but got restarted before receiving response
        	String callbackId = savedInstanceState.getString(INSTANCE_EXTRA_INSERT_REQUEST_ID);
			insertRequestReceiver = new InsertRequestReceiver(callbackId);
			Intent i = AsyncHelper.prepareRepeatCallbackIntent(
				this, 
				ContentService.class, 
				callbackId,
				AsyncHelper.getBroadcastCallback(CALLBACK_ACTION_INSERT)
			);
	        registerReceiver(insertRequestReceiver, new IntentFilter(CALLBACK_ACTION_INSERT));
        	log.v("Sending REPEAT_CALLBACK "+callbackId);
        	startService(i);
        	lockUiOnProgress();
        	
        	ViewUtils.addActivityOnLayout(this, new OnLayoutListener() {
				@Override
				public void onLayoutPassed() {
					View button = findViewById(R.id.NEWSCORE_okbutton);
					Rect rect = new Rect(0, 0, button.getWidth(), button.getHeight());
					button.requestRectangleOnScreen(
						rect,
						true
					);
				}
			});
        }
	}

	private void selectRadioViewByEnumTag(Bundle state, String stateKey, int containerId) {
		String enumName = state.getString(stateKey);
		View view;
		if(enumName != null && (view = findViewByEnumTag(containerId, enumName)) != null) {
			RadioGroup.select(view);
		}
	}
	
	private View findViewByEnumTag(ViewGroup wrapper, Enum<?> enumValue) {
		for(int i = 0; i < wrapper.getChildCount(); i++) {
			View child = wrapper.getChildAt(i);
			if(enumValue == child.getTag()) {
				return child;
			}
		}
		return null;
	}
	
	private View findViewByEnumTag(int containerId, String enumName) {
		ViewGroup wrapper = (ViewGroup) findViewById(containerId);
		for(int i = 0; i < wrapper.getChildCount(); i++) {
			View child = wrapper.getChildAt(i);
			Object tagValue = child.getTag();
			if(tagValue != null && ((Enum<?>) tagValue).name().equals(enumName)) {
				return child;
			}
		}
		return null;
	}

	private void saveSelectedViewEnumTag(Bundle outState, int containerViewGroupId, String extrasKey) {
		View selectedView;
		if((selectedView = findSelected((ViewGroup) findViewById(containerViewGroupId))) != null) {
			outState.putString(extrasKey, ((Enum<?>) selectedView.getTag()).name());
		}
	}
	
	protected View findSelected(ViewGroup wrapper) {
		for(int i = 0; i < wrapper.getChildCount(); i++) {
			View child = wrapper.getChildAt(i);
			if(child.isSelected())
				return child;
		}
		return null;
	}
	
	private final class OkListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			lockUiOnProgress();
			String title = ((TextView) findViewById(R.id.NEWSCORE_textfield_title)).getText().toString();
			if(title.trim().length() < 1) {
				title = null;
			}
			Clef clef = (Clef) getSelectedViewTag(R.id.NEWSCORE_clefs_container);
			KeySignature key = (KeySignature) getSelectedViewTag(R.id.NEWSCORE_keys_container);
			TimeSignatureType signType = (TimeSignatureType) getSelectedViewTag(R.id.NEWSCORE_meter_container);
			TimeStep timeSignature = null;
			switch(signType) {
			case COMMON_TIME:
				timeSignature = TimeStep.commonTime;
				break;
			case CUT_COMMON_TIME:
				timeSignature = TimeStep.cutCommonTime;
				break;
			case CUSTOM:
				timeSignature = new TimeStep(
					meterSignMultipierCtrl.getValue(), 
					IntUtils.log2(meterSignBaseCtrl.getValue())
				);
				break;
			}
			Score score = new Score(
				title,
				ScoreContentFactory.initialContent(
					clef, key, timeSignature
				)
			);
			insertRequestReceiver = new InsertRequestReceiver();
			Intent requestIntent = AsyncHelper.prepareServiceIntent(
				NewScoreActivity.this, 
				ContentService.class, 
				ContentService.ACTIONS.INSERT_SCORE, 
				insertRequestReceiver.getCurrentRequestId(), 
				AsyncHelper.getBroadcastCallback(CALLBACK_ACTION_INSERT),
				true
			);
			try {
				requestIntent.putExtra(ContentService.ACTIONS.EXTRAS_SCORE, score.prepareParcelable());
			} catch (SerializationException e) {
				log.e("failed to serialize Score", e);
				showErrorDialog(R.string.errormsg_unrecoverable, e, true);
				return;
			}
			requestIntent.putExtra(ContentService.ACTIONS.EXTRAS_SCORE_VISUAL_CONF, visConf);
			ContextCompat.registerReceiver(NewScoreActivity.this, insertRequestReceiver, new IntentFilter(CALLBACK_ACTION_INSERT), ContextCompat.RECEIVER_EXPORTED);
        	log.v("Sending "+CALLBACK_ACTION_INSERT);
        	startService(requestIntent);
		}

		private Object getSelectedViewTag(int containerId) {
			return findSelected((ViewGroup) findViewById(containerId)).getTag();
		}
	}
	
	private static final String CALLBACK_ACTION_INSERT = NewScoreActivity.class.getName()+ ".insert";
	public static final String RESULT_CREATED_SCORE_ID = "createdScoreId";
	
	private class InsertRequestReceiver extends FilterByRequestIdReceiver {
		
		public InsertRequestReceiver() {
		}

		public InsertRequestReceiver(String currentRequestId) {
			super(currentRequestId);
		}

		@Override
		protected void onFailure(Intent response) {
			unregisterInsertReceiver();
			sendCleanup();
			showErrorDialog(R.string.NEWSCORE_errormsg_gonewrong, null, false);
			unlockUi();
		}

		@Override
		protected void onSuccess(Intent response) {
			unregisterInsertReceiver();
			sendCleanup();
			log.v("INSERT_SCORE onSuccess()");
			long scoreId = response.getLongExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_ENTITY_ID, -1);
			if(scoreId == -1) {
				log.e("INSERT_SCORE onSuccess() didn't contain entity id");
				showErrorDialog(R.string.errormsg_unrecoverable, null, true);
				return;
			}
			Intent i = new Intent(getApplicationContext(), EditActivity.class);
			i.putExtra(EditActivity.STARTINTENT_EXTRAS_SCORE_ID, scoreId);
			Intent result = new Intent();
			result.putExtra(RESULT_CREATED_SCORE_ID, scoreId);
			NewScoreActivity.this.setResult(RESULT_OK, result);
			startActivity(i);
			finish();
		}
		
		private void sendCleanup() {
			Intent i = AsyncHelper.prepareCleanCallbackIntent(
				NewScoreActivity.this, 
				ContentService.class,
				getCurrentRequestId()
			);
			startService(i);
		}
	}

	private static enum TimeSignatureType {
		NONE,
		COMMON_TIME,
		CUT_COMMON_TIME,
		CUSTOM;
	};
	private View setupCustomTimeSignature(SheetParams params, RadioGroup radioGroup, ViewGroup container) {
		View timeSignature = getLayoutInflater().inflate(R.layout.newscore_timesignature_custom, container, false);
		container.addView(timeSignature);
		tempoLabel = (LinedSheetElementView) timeSignature.findViewById(R.id.NEWSCORE_tempo);
		tempoLabel.setSheetParams(params);
		timeSignature.setOnClickListener(radioGroup);
		meterSignMultipierCtrl = new IntegerSpinnerController(
			new IncrementModel(1).setValue(defaultCustomTimeSign.getBaseMultiplier()).setMinValue(1),
			timeSignature, 
			R.id.upper_increment_button, R.id.upper_decrement_button,
			tempoLabel
		);
		Integer minNote = getResources().getInteger(R.integer.minNotePossibleValue);
		meterSignBaseCtrl = new IntegerSpinnerController(
			new MultiplyModel(2).setValue(1 << defaultCustomTimeSign.getTempoBaseLength()).setMinValue(1).setMaxValue(1 << minNote),
			timeSignature, 
			R.id.lower_increment_button, R.id.lower_decrement_button,
			tempoLabel
		);
		updateTempoLabel();
		return timeSignature;
	}
	View timeSignature;
	LinedSheetElementView tempoLabel;
	private IntegerSpinnerController meterSignMultipierCtrl, meterSignBaseCtrl;
	
	private void updateTempoLabel() {
		if(meterSignBaseCtrl != null && meterSignMultipierCtrl != null) {
			tempoLabel.setFrontModel(new Tempo(
				meterSignMultipierCtrl.getValue(),
				meterSignBaseCtrl.getValue()
			));
		}
	}
	
	private class IntegerSpinnerController extends IntegerSpinner.IntegerSpinnerController {
		public IntegerSpinnerController(IntegerSpinnerModel model,
				View wrapper, int incrementButtonId, int decrementButtonId,
				LinedSheetElementView label) {
			super(model, wrapper, incrementButtonId, decrementButtonId);
		}
		
		@Override
		protected void updateViews() {
			super.updateViews();
			updateTempoLabel();
		}
		
		@Override
		public void onClick(View v) {
			super.onClick(v);
			RadioGroup.select(timeSignature);
		}
	}
	
	private static class RadioGroup implements View.OnClickListener {
		@Override
		public void onClick(View radioView) {
			select(radioView);
		}
		
		public static void select(View radioView) {
			ViewGroup parent = (ViewGroup) radioView.getParent();
			for(int i = 0; i < parent.getChildCount(); i++) {
				View child = parent.getChildAt(i);
				child.setSelected(child == radioView);
			}
		}
	}

	private LinedSheetElementView setupThumbnailView(SheetParams params,
			ViewGroup container, int svgResId) throws LoadingSvgException {
		AdjustableSizeImage img = NotePartFactory.prepareAdujstableImage(this, svgResId, null);
		SheetElement model = new SimpleSheetElement(img);
		LinedSheetElementView sheetElementView = setupThumbnailView(params, container, model);
		return sheetElementView;
	}

	private LinedSheetElementView setupThumbnailView(SheetParams params, ViewGroup container, SheetElement model) {
		LinedSheetElementView sheetElementView = 
			(LinedSheetElementView) getLayoutInflater().inflate(R.layout.newscore_thumbnail, container, false);
		sheetElementView.setFrontModel(model);
		sheetElementView.setSheetParams(params);
		container.addView(sheetElementView);
		return sheetElementView;
	}

	private void alignVertically(ViewGroup container) {
		// align in vertical
		int maxOffset = Integer.MIN_VALUE;
		int maxToBottom = Integer.MIN_VALUE;
		for(int i = 0; i < container.getChildCount(); i++) {
			LinedSheetElementView sheetElementView = (LinedSheetElementView) container.getChildAt(i);
			sheetElementView.setPadding(0, 0, 0, 0, false);
			int offsetToAnchor = sheetElementView.getOffsetToAnchor(NoteConstants.LINE0_ABSINDEX, AnchorPart.TOP_EDGE);
			maxOffset = Math.max(maxOffset, offsetToAnchor);
			int toBottom = sheetElementView.measureHeight() - offsetToAnchor;
			maxToBottom = Math.max(maxToBottom, toBottom);
		}
		for(int i = 0; i < container.getChildCount(); i++) {
			LinedSheetElementView sheetElementView = (LinedSheetElementView) container.getChildAt(i);
			int offset = sheetElementView.getOffsetToAnchor(NoteConstants.LINE0_ABSINDEX, AnchorPart.TOP_EDGE);
			int toBottom = sheetElementView.measureHeight() - offset;
			sheetElementView.setPadding(0, maxOffset - offset, 0, maxToBottom - toBottom, false);
		}
	}
	
	private void lockUiOnProgress() {
		Button okButton = (Button) findViewById(R.id.NEWSCORE_okbutton);
		AnimationDrawable dr = (AnimationDrawable) getResources().getDrawable(R.drawable.spinner_16dp);
		okButton.setCompoundDrawablesWithIntrinsicBounds(dr, null, null, null);
		dr.start();
		okButton.setEnabled(false);
	}
	
	private void unlockUi() {
		Button okButton = (Button) findViewById(R.id.NEWSCORE_okbutton);
		AnimationDrawable dr = (AnimationDrawable) getResources().getDrawable(R.drawable.spinner_16dp);
		okButton.setCompoundDrawablesWithIntrinsicBounds(dr, null, null, null);
		dr.stop();
		okButton.setEnabled(true);
	}

	private void showErrorDialog(int messageStringId, Throwable e, boolean lazyFinish) {
		errorDialogStrategy.showErrorDialog(messageStringId, e, lazyFinish);
	}

	private static class EmptyElement extends SheetElement {

		@Override
		public int measureWidth() {
			return 0;
		}

		@Override
		public int measureHeight() {
			return 0;
		}

		@Override
		public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
			return 0;
		}

		@Override
		public void onDraw(Canvas canvas, Paint paint) {
		}
		
		@Override
		public void getCollisionRegions(ArrayList<Rect> areas,
				ArrayList<Rect> rectsPool) {
			// there are none
		}
	}
}
