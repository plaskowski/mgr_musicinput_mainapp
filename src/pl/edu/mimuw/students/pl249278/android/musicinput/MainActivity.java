package pl.edu.mimuw.students.pl249278.android.musicinput;

import static pl.edu.mimuw.students.pl249278.android.async.AsyncHelper.getBroadcastCallback;
import static pl.edu.mimuw.students.pl249278.android.common.Macros.ifNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.async.AsyncHelper;
import pl.edu.mimuw.students.pl249278.android.async.AsynchronousRequestsService;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.Macros;
import pl.edu.mimuw.students.pl249278.android.musicinput.MainActivityHelper.ByScoreIdRequest;
import pl.edu.mimuw.students.pl249278.android.musicinput.MainActivityHelper.ExportMidiRequest;
import pl.edu.mimuw.students.pl249278.android.musicinput.MainActivityHelper.ReceiverState;
import pl.edu.mimuw.students.pl249278.android.musicinput.component.ManagedReceiver;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score.ParcelableScore;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.ContentService;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.FilterByRequestIdReceiver;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.WorkerService;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog.ConfirmDialogBuilder;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog.ConfirmDialogListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InfoDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ParcelablePrimitives.ParcelableLong;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TextInputDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TextInputDialog.TextInputDialogListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.component.activity.FragmentActivity_ErrorDialog_TipDialog_ProgressDialog_ManagedReceiver;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutAnimator;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewHeightAnimation;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity_ErrorDialog_TipDialog_ProgressDialog_ManagedReceiver implements TextInputDialogListener, ConfirmDialogListener {
	private static LogUtils log = new LogUtils(MainActivity.class);
	private static final String CALLBACK_ACTION_GET = MainActivity.class.getName()+".callback_get";
	protected static final String CALLBACK_ACTION_DELETE = MainActivity.class.getName()+".callback_delete";
	private static final String CALLBACK_ACTION_DUPLICATE = MainActivity.class.getName()+".callback_duplicate";
	private static final String CALLBACK_ACTION_EXPORTMIDI = MainActivity.class.getName()+".callback_export";
	private static final String CALLBACK_ACTION_GET_CREATED = MainActivity.class.getName()+".callback_get_created";
	
	protected static final String DIALOGTAG_NEW_TITLE = "dialog_newtitle";
	protected static final String DIALOGTAG_COPY_TITLE = "dialog_copytitle";
	protected static final String DIALOGTAG_CONFIRM_DELETE = "dialog_confirm_delete";
	protected static final String DIALOGTAG_EXPORT_MIDI = "dialog_export_midi";
	protected static final String DIALOGTAG_INFO = "dialog_info";
	private static final String DIALOGTAG_CONFIRM_OVERWRITE = "dialog_overwrite_file";
	private static final int ERRORDIALOG_CALLBACKARG_RELOAD = ERRORDIALOG_CALLBACKARG_DO_FINISH+1;
	private static final int ERRORDIALOG_CALLBACKARG_DUPLICATE = ERRORDIALOG_CALLBACKARG_RELOAD+1;
	private static final int ERRORDIALOG_CALLBACKARG_INFO = ERRORDIALOG_CALLBACKARG_DUPLICATE+1;
	protected static final int INPUTDIALOG_CALLBACKARG_NEW_TITLE = 1;
	protected static final int INPUTDIALOG_CALLBACKARG_COPY_TITLE = 2;
	protected static final int INPUTDIALOG_CALLBACKARG_MIDIFILE = 3;
	protected static final int CONFIRMDIALOG_CALLBACKARG_DELETESCORE = CONFIRMDIALOG_CALLBACKARG_TIP+1;
	private static final int CONFIRMDIALOG_CALLBACKARG_MIDIFILE_OVERWRITE = CONFIRMDIALOG_CALLBACKARG_DELETESCORE+1;
	protected static final String TIP_MIDI_ON_STORAGE = MainActivity.class.getCanonicalName()+".midi_exported_to_storage";
	
	private static final String STATE_SCORES = "scores";
	private static final String STATE_EXPANDED_ENTRY_SCOREID = "expanded_scoreid";
	private static final String STATE_RECEIVERS_STATES = "receivers_states";
	
	protected static final int REQUEST_NEW_SCORE = 1;
	
	/**
	 * Not null means whole model has been successfully loaded.
	 */
	private ArrayList<ParcelableScore> scores = null;
	
	private List<EnqueuedReceiver> receivers = new ArrayList<MainActivity.EnqueuedReceiver>();
	private LayoutAnimator<MainActivity> animator = new LayoutAnimator<MainActivity>(this);
	protected View expandedEntry;
	private Handler uiHandler;
	
	static enum ReceiverType {
		SCORE_DELETED(ContentService.class),
		SCORE_DUPLICATED(ContentService.class),
		SCORE_EXPORTED(WorkerService.class), 
		GET_CREATED(ContentService.class);
		
		final Class<AsynchronousRequestsService> serviceClass;
		
		@SuppressWarnings("unchecked")
		private <T extends AsynchronousRequestsService> ReceiverType(Class<T> serviceClass) {
			this.serviceClass = (Class<AsynchronousRequestsService>) serviceClass;
		}
	};
	
	private abstract class EnqueuedReceiver extends ManagedReceiver {		
		private ReceiverType type;
		
		public EnqueuedReceiver(ReceiverType type) {
			this.type = type;
		}

		public EnqueuedReceiver(String currentRequestId, ReceiverType type) {
			super(currentRequestId);
			this.type = type;
		}

		@Override
		protected boolean unregister() {
			if(receivers.remove(this)) {
				unregisterReceiver(this);
				return true;
			} else {
				return false;
			}
		}

		public abstract ReceiverState getState();
		
		protected void sendCleanSilently() {
			try {
				Intent i = AsyncHelper.prepareCleanCallbackIntent(getApplicationContext(), type.serviceClass, getCurrentRequestId());
				startService(i);
			} catch(Exception e) {
				log.e("Failed to send CLEAN, ignoring.", e);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.mainscreen);
		uiHandler = new Handler();
		
		if(savedState != null && savedState.containsKey(STATE_SCORES)) {
			ArrayList<ParcelableScore> scores = savedState.getParcelableArrayList(STATE_SCORES);
			onModelLoaded(scores);
			if(savedState.containsKey(STATE_EXPANDED_ENTRY_SCOREID)) {
				View entry = findEntryView(savedState.getLong(STATE_EXPANDED_ENTRY_SCOREID));
				if(entry != null) {
					entry.performClick();
				}
			}
			ArrayList<ReceiverState> receiverStates = savedState.getParcelableArrayList(STATE_RECEIVERS_STATES);
			if(receiverStates != null) for(ReceiverState state: receiverStates) {
				View entryView;
				switch(state.type) {
				case SCORE_DELETED:
					EnqueuedReceiver receiver = new DeleteScoreReceiver(state.requestId);
					registerEnqueueAndRequestRepeat(receiver, CALLBACK_ACTION_DELETE);
					break;
				case SCORE_DUPLICATED:
					DuplicateReceiver duplReceiver = new DuplicateReceiver((ByScoreIdRequest) state);
					registerEnqueueAndRequestRepeat(duplReceiver, CALLBACK_ACTION_DUPLICATE);
					entryView = findEntryView(duplReceiver.originalScoreId);
					addProgressLock(entryView);
					addLock(entryView, R.id.button_duplicate);
					break;
				case SCORE_EXPORTED:
					ExportMidiRequest exportState = (ExportMidiRequest) state;
					ExportMidiReceiver exportReceiver = new ExportMidiReceiver((ExportMidiRequest) exportState);
					registerEnqueueAndRequestRepeat(exportReceiver, CALLBACK_ACTION_EXPORTMIDI);
					// show progress indicator
					entryView = findEntryView(exportState.scoreId);
					addProgressLock(entryView);
					addLock(entryView, R.id.button_exportmidi);
					break;
				case GET_CREATED:
					registerEnqueueAndRequestRepeat(
						new GetCreatedReceiver((ByScoreIdRequest) state), 
						CALLBACK_ACTION_GET_CREATED);
					break;
				}
			}
		} else {
			requestModel();
		}
	}
	
	private <Receiver extends FilterByRequestIdReceiver> void registerEnqueueAndRequestRepeat(EnqueuedReceiver receiver, String callbackAction) {
		registerAndEnqueue(receiver, callbackAction);
		startService(AsyncHelper.prepareRepeatCallbackIntent(
			this, receiver.type.serviceClass, 
			receiver.getCurrentRequestId(), getBroadcastCallback(callbackAction)
		));
	}

	private void registerAndEnqueue(EnqueuedReceiver receiver, String callbackAction) {
		registerReceiver(receiver, new IntentFilter(callbackAction));
		receivers.add(receiver);
	}

	private void requestModel() {
		Intent requestIntent;
		ContentReceiver receiver = new ContentReceiver();
		requestIntent = AsyncHelper.prepareServiceIntent(
			this, 
			ContentService.class, 
			ContentService.ACTIONS.LIST_SCORES, 
			receiver.getCurrentRequestId(), 
			getBroadcastCallback(CALLBACK_ACTION_GET),
			false
		);
		requestIntent.putExtra(ContentService.ACTIONS.EXTRAS_ATTACH_SCORE_VISUAL_CONF, true);
		registerManagedReceiver(receiver, CALLBACK_ACTION_GET);
		startService(requestIntent);
		showProgressDialog();
	}
	
	private class ContentReceiver extends SingleManagedReceiver {
		@Override
		protected void onFailureReceived(Intent response) {
			log.e("Failed to list scores: " + AsyncHelper.getError(response));
			hideProgressDialog();
			showErrorDialog(R.string.errormsg_unrecoverable, null, true);
		}
		
		@Override
		protected void onSuccessReceived(Intent response) {
			hideProgressDialog();
			Parcelable[] scoresArr = response.getParcelableArrayExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_SCORES);
			ArrayList<ParcelableScore> scores = new ArrayList<Score.ParcelableScore>(scoresArr.length);
			for (int i = 0; i < scoresArr.length; i++) {
				Parcelable parcelable = scoresArr[i];
				scores.add((ParcelableScore) parcelable);
			}
			onModelLoaded(scores);
			// TODO handle vis confs
			response.getParcelableArrayExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_VISUAL_CONFS);
		}
	}
	
	private void onModelLoaded(ArrayList<ParcelableScore> scores) {
		this.scores = scores;
		ViewGroup container = (ViewGroup) findViewById(R.id.entries_container);
		for(ParcelableScore pScore: scores) {
			Score score = pScore.getSource();
			View entry = inflateAndPopulateEntry(score, container);
			container.addView(entry);
		}
		// FIXME ugly hack to force loading of SVG icons used in entry toolbar
		getLayoutInflater().inflate(R.layout.mainscreen_entry_toolbar, null);
		findViewById(R.id.MAIN_entry_addnew).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), NewScoreActivity.class);
				startActivityForResult(i, REQUEST_NEW_SCORE);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case REQUEST_NEW_SCORE:
			if(resultCode == RESULT_OK) {
				long scoreId = data.getLongExtra(NewScoreActivity.RESULT_CREATED_SCORE_ID, -1);
				GetCreatedReceiver recv = new GetCreatedReceiver(scoreId);
				registerAndEnqueue(recv, CALLBACK_ACTION_GET_CREATED);
				Intent i = AsyncHelper.prepareServiceIntent(this, ContentService.class, 
					ContentService.ACTIONS.GET_SCORE_BY_ID, 
					recv.getCurrentRequestId(), AsyncHelper.getBroadcastCallback(CALLBACK_ACTION_GET_CREATED),
					true
				);
				i.putExtra(ContentService.ACTIONS.EXTRAS_ENTITY_ID, scoreId);
				// TODO if request visConf
				startService(i);
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	private class GetCreatedReceiver extends EnqueuedReceiver {
		long scoreId;

		private GetCreatedReceiver(long scoreId) {
			super(ReceiverType.GET_CREATED);
			this.scoreId = scoreId;
		}
		
		public GetCreatedReceiver(ByScoreIdRequest state) {
			super(state.requestId, ReceiverType.GET_CREATED);
			this.scoreId = state.scoreId;
		}
		
		@Override
		protected void onFailureReceived(Intent response) {
			sendCleanSilently();
			dismissReceiversByType(ReceiverType.GET_CREATED);
			showErrorDialog(R.string.errormsg_failed_to_refresh, ERRORDIALOG_CALLBACKARG_RELOAD);
		}
		
		@Override
		protected void onSuccessReceived(Intent response) {
			sendCleanSilently();
			ParcelableScore pScore = response.getParcelableExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_ENTITY);
			long stamp = pScore.getSource().getModificationUtcStamp();
			// find index in list ordered by modification stamp
			int i = 0;
			for(; i < scores.size(); i++) {
				if(stamp >= scores.get(i).getSource().getModificationUtcStamp()) {
					break;
				}
			}
			onNewScoreArrived(i, pScore);
		}
		
		@Override
		public ReceiverState getState() {
			return new ByScoreIdRequest(ReceiverType.GET_CREATED, scoreId, getCurrentRequestId());
		}
	}

	private View inflateAndPopulateEntry(Score score, ViewGroup container) {
		View entry = getLayoutInflater().inflate(R.layout.mainscreen_entry, container, false);
		populateEntryTextViews(score, entry);
		entry.setOnClickListener(entryClickListener);
		entry.setTag(score);
		return entry;
	}

	private void populateEntryTextViews(Score score, View entry) {
		((TextView) entry.findViewById(R.id.title)).setText(
			title(score));
		((TextView) entry.findViewById(R.id.created)).setText(
			formatDate(score.getCreationUtcStamp()));
		((TextView) entry.findViewById(R.id.modified)).setText(
				formatDate(score.getModificationUtcStamp()));
	}
	
	private OnClickListener entryClickListener = new OnClickListener() {
		private View prev = null;
		@Override
		public void onClick(View view) {
			ViewGroup entry = (ViewGroup) view;
			View toolbar = setupEntryToolbar(entry);
			if(prev != null) {
				animator.startAnimation(new ViewHeightAnimation.CollapseAnimation<MainActivity>(prev, 300));
			}
			if(toolbar != prev) {
				animator.startAnimation(new ExpandKeepVisibleAnimation(toolbar, 300));
				prev = toolbar;
				expandedEntry = view;
			} else {
				prev = null;
				expandedEntry = null;
			}
		}
	};
	
	private CharSequence formatDate(long UtcStamp) {
		return DateUtils.getRelativeDateTimeString(this, UtcStamp, 
			DateUtils.MINUTE_IN_MILLIS, 2*DateUtils.DAY_IN_MILLIS, 0);
	}
	
	/**
	 * Inflate and set up "entry" View toolbar, unless it was already done.
	 * @return "toolbar" View of given "entry" View
	 */
	private View setupEntryToolbar(ViewGroup entry) {
		View toolbar = entry.findViewById(R.id.entry_toolbar);
		if(toolbar == null) {
			toolbar = getLayoutInflater().inflate(R.layout.mainscreen_entry_toolbar, entry, false);
			entry.addView(toolbar);
			setupEntryToolbarCallbacks((Score) entry.getTag(), toolbar);
		}
		return toolbar;
	}	
	
	protected void setupEntryToolbarCallbacks(final Score score, View toolbar) {
		toolbar.findViewById(R.id.button_rename).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String initialValue = score.getTitle();
				TextInputDialog.newInstance(MainActivity.this, 
					INPUTDIALOG_CALLBACKARG_NEW_TITLE, score.getId(), 
					getString(R.string.popup_title_change_title), android.R.string.ok, android.R.string.cancel, initialValue)
				.show(getSupportFragmentManager(), DIALOGTAG_NEW_TITLE);
			}
		});
		toolbar.findViewById(R.id.button_edit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), EditActivity.class);
				intent.putExtra(EditActivity.STARTINTENT_EXTRAS_SCORE_ID, score.getId());
				startActivity(intent);
			}
		});
		toolbar.findViewById(R.id.button_play).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
				intent.putExtra(PlayActivity.STARTINTENT_EXTRAS_SCORE_ID, score.getId());
				startActivity(intent);
			}
		});
		toolbar.findViewById(R.id.button_delete).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new ConfirmDialogBuilder(CONFIRMDIALOG_CALLBACKARG_DELETESCORE)
				.setState(new ParcelableLong(score.getId()))
				.setMsg(R.string.confirmmsg_delete, new String[] { title(score) })
				.setPositiveNegative(android.R.string.ok, android.R.string.cancel)
				.showNew(getSupportFragmentManager(), DIALOGTAG_CONFIRM_DELETE);
			}
		});
		toolbar.findViewById(R.id.button_duplicate).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String initialValue = getString(R.string.template_copy_title, title(score));
				TextInputDialog.newInstance(MainActivity.this, 
					INPUTDIALOG_CALLBACKARG_COPY_TITLE, score.getId(), 
					getString(R.string.popup_title_copytitle), android.R.string.ok, android.R.string.cancel, initialValue)
				.show(getSupportFragmentManager(), DIALOGTAG_COPY_TITLE);
			}
		});
		toolbar.findViewById(R.id.button_exportmidi).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				String title = ifNotNull(score.getTitle(), getString(android.R.string.untitled));
				String initValue = title.replaceAll("[^A-Za-z \\(\\)0-9]", "_") + ".midi";
				showExportMidiDialog(score, initValue);
			}
		});
	}
	
	@Override
	public void onConfirm(ConfirmDialog dialog, int dialogId, Parcelable state) {
		switch(dialogId) {
		case CONFIRMDIALOG_CALLBACKARG_DELETESCORE:
			Long scoreId = ((ParcelableLong) state).value;
			Score score = findScoreById(scoreId);
			if(score != null) {
				deleteScore(score);
			} else {
				log.w("Received delete confirmation for non-existient Score#%d", scoreId);
			}
			break;
		case CONFIRMDIALOG_CALLBACKARG_MIDIFILE_OVERWRITE:
			// user chose to overwrite existing MIDI file
			sendExportMidiRequest((ExportMidiRequest) state);
			break;
		default:
			super.onConfirm(dialog, dialogId, state);
		}
	}
	
	@Override
	public void onNeutral(ConfirmDialog dialog, int dialogId, Parcelable state) {
		switch(dialogId) {
		case CONFIRMDIALOG_CALLBACKARG_MIDIFILE_OVERWRITE:
			ExportMidiRequest request = (ExportMidiRequest) state;
			showExportMidiDialog(findScoreById(request.scoreId), request.filename);
			break;
		default:
			super.onNeutral(dialog, dialogId, state);
		}
	}
	
	/**
	 * Send DELETE request to {@link ContentService}, removes view and updates {@link #scores}
	 */
	private void deleteScore(Score score) {
		DeleteScoreReceiver receiver = new DeleteScoreReceiver();
		Intent requestIntent = AsyncHelper.prepareServiceIntent(
			MainActivity.this, 
			ContentService.class, 
			ContentService.ACTIONS.DELETE_SCORE, 
			receiver.getCurrentRequestId(), 
			getBroadcastCallback(CALLBACK_ACTION_DELETE), 
			true
		);
		requestIntent.putExtra(ContentService.ACTIONS.EXTRAS_ENTITY_ID, score.getId());
		registerAndEnqueue(receiver, CALLBACK_ACTION_DELETE);
    	startService(requestIntent);
    	// hide view
    	final View entryView = findEntryView(score.getId());
    	if(entryView == null) {
    		log.w("No entryView to hide for deleted Score#%d", score.getId());
    	} else {
    		Animation fadeOut = AnimationUtils.makeOutAnimation(MainActivity.this, true);
    		fadeOut.setDuration(200);
    		fadeOut.setFillAfter(true);
    		fadeOut.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				@Override
				public void onAnimationEnd(Animation animation) {
		    		ViewHeightAnimation.CollapseAnimation<MainActivity> anim = new ViewHeightAnimation.CollapseAnimation<MainActivity>(entryView, 200);
		    		anim.setOnAnimationEndListener(new Runnable() {
						@Override
						public void run() {
				    		entryView.setVisibility(View.GONE);
						}
					});
					animator.startAnimation(anim);
				}
			});
    		entryView.setAnimation(fadeOut);
    		fadeOut.startNow();
    		entryView.invalidate();
    		entryView.setTag(null);
    	}
    	// remove Score from model
    	ParcelableScore pScore = null;
    	for(ParcelableScore obj: scores) {
    		if(obj.getSource() == score) {
    			pScore = obj;
    			break;
    		}
    	}
    	if(pScore != null) {
    		scores.remove(pScore);
    	} else {
    		log.w("Failed to remove Score#%d from model", score.getId());
    	}
	}

	private class DeleteScoreReceiver extends EnqueuedReceiver {
		public DeleteScoreReceiver() {
			super(ReceiverType.SCORE_DELETED);
		}

		public DeleteScoreReceiver(String currentRequestId) {
			super(currentRequestId, ReceiverType.SCORE_DELETED);
		}

		@Override
		protected void onFailureReceived(Intent response) {
			// failed to delete, so we will refresh all
			dismissReceiversByType(ReceiverType.SCORE_DELETED);
			showErrorDialog(R.string.errormsg_failed_to_delete, ERRORDIALOG_CALLBACKARG_RELOAD);
		}

		@Override
		protected void onSuccessReceived(Intent response) {
			log.v("DELETE request -> onSuccess()");
		}
		
		@Override
		public ReceiverState getState() {
			return new ReceiverState(ReceiverType.SCORE_DELETED, getCurrentRequestId());
		}
	}
	
	@Override
	public void onDismiss(InfoDialog dialog, int arg) {
		if(arg == ERRORDIALOG_CALLBACKARG_RELOAD) {			
			// clear entry views
			((ViewGroup) findViewById(R.id.entries_container)).removeAllViews();
			// read model once again
			scores = null;
			requestModel();
		} else {
			super.onDismiss(dialog, arg);
		}
	}
	
	@Override
	public void onValueEntered(TextInputDialog dialog, int valueId,
			long listenerArg, String value) {
		Score score;
		switch(valueId) {
		case INPUTDIALOG_CALLBACKARG_MIDIFILE:
			if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				showErrorDialog(R.string.errormsg_external_storage_not_present, ERRORDIALOG_CALLBACKARG_INFO);
			} else {
				File dir = WorkerService.getExportDir();
				File destFile = new File(dir, value);
				if(destFile.exists()) {
					new ConfirmDialogBuilder(CONFIRMDIALOG_CALLBACKARG_MIDIFILE_OVERWRITE)
					.setState(new ExportMidiRequest(listenerArg, value))
					.setMsg(R.string.popup_msg_file_already_exists, new String[] { value })
					.setButtons(R.string.overwrite, R.string.change, android.R.string.cancel)
					.showNew(getSupportFragmentManager(), DIALOGTAG_CONFIRM_OVERWRITE);
				} else {
					sendExportMidiRequest(new ExportMidiRequest(listenerArg, value));
				}
			}
			break;
		case INPUTDIALOG_CALLBACKARG_NEW_TITLE:
			if((score = findScoreById(listenerArg)) == null) {
				log.w("onValueEntered::new title for non-existient score %d", listenerArg);
				break;
			}
			if(value == null || value.equals("")) {
				// clear title
				if(score.getTitle() != null) {
					score.setTitle(null);
					sendUpdate(score);
				}
			} else if(!value.equals(score.getTitle())) {
				score.setTitle(value);
				sendUpdate(score);
			}
			break;
		case INPUTDIALOG_CALLBACKARG_COPY_TITLE:
			if((score = findScoreById(listenerArg)) == null) {
				log.w("onValueEntered::copy for non-existient score %d", listenerArg);
				break;
			}
			sendCreateDuplicate(score, value);
			break;
		}		
	}
	
	private Score findScoreById(long scoreId) {
		ParcelableScore pScore = findParcelableScoreById(scoreId);
		return pScore == null ? null : pScore.getSource();
	}
	
	private ParcelableScore findParcelableScoreById(long scoreId) {
		for (ParcelableScore score : scores) {
			if(score.getSource().getId() == scoreId)
				return score;
		}
		return null;
	}
	
	private View findEntryView(long scoreId) {
		int index = findEntryViewIndex(scoreId);
		if(index >= 0) {
			return ((ViewGroup) findViewById(R.id.entries_container)).getChildAt(index);
		} else {
			return null;
		}
	}
	
	private int findEntryViewIndex(long scoreId) {
		ViewGroup wrapper = (ViewGroup) findViewById(R.id.entries_container);
		int total = wrapper.getChildCount();
		for (int i = 0; i < total; i++) {
			View child = wrapper.getChildAt(i);
			Object tag = child.getTag();
			if(tag != null && tag instanceof Score && ((Score) tag).getId() == scoreId)
				return i;
		}
		return -1;
	}

	private class DuplicateReceiver extends EnqueuedReceiver {
		private long originalScoreId;
		
		public DuplicateReceiver(ByScoreIdRequest request) {
			this(request.requestId, request.scoreId);
		}
		
		public DuplicateReceiver(String currentRequestId, long originalScoreId) {
			super(currentRequestId, ReceiverType.SCORE_DUPLICATED);
			this.originalScoreId = originalScoreId;
		}

		@Override
		protected void onSuccessReceived(Intent response) {
			sendCleanSilently();
			// hide progress
			View originalEntryView = findEntryView(originalScoreId);
			if(originalEntryView != null) {
				removeProgressLock(originalEntryView);
				removeLock(originalEntryView, R.id.button_duplicate);
			}
			// create and reveal entry with received copy
			ParcelableScore pScore = response.getParcelableExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_ENTITY);
			onNewScoreArrived(scores.indexOf(findParcelableScoreById(originalScoreId))+1, pScore);
		}

		@Override
		protected void onFailureReceived(Intent response) {
			sendCleanSilently();
			showErrorDialog(R.string.errormsg_failed_to_duplicate, ERRORDIALOG_CALLBACKARG_DUPLICATE);
			View originalEntryView = findEntryView(originalScoreId);
			if(originalEntryView != null) {
				removeProgressLock(originalEntryView);
				removeLock(originalEntryView, R.id.button_duplicate);
			}
		}

		@Override
		public ByScoreIdRequest getState() {
			return new ByScoreIdRequest(ReceiverType.SCORE_DUPLICATED, originalScoreId, getCurrentRequestId());
		}
		
	}

	/**
	 * Add object to {@link MainActivity#scores}, inflates new entry view and start animation to reveal it
	 * @param insertAt index in model
	 */
	private void onNewScoreArrived(int insertAt, ParcelableScore pScore) {
		insertAt = Math.max(insertAt, 0);
		int viewInsertIndex = 0;
		if(insertAt < scores.size()) {
			viewInsertIndex = findEntryViewIndex(scores.get(insertAt).getSource().getId());
		}
		scores.add(insertAt, pScore);
		ViewGroup container = (ViewGroup) findViewById(R.id.entries_container);
		final View entryView = inflateAndPopulateEntry(pScore.getSource(), container);
		container.addView(entryView, viewInsertIndex);
		ViewHeightAnimation.ExpandAnimation.fillBefore(entryView);
		ExpandKeepVisibleAnimation anim = new ExpandKeepVisibleAnimation(entryView, 200);
		anim.setOnAnimationEndListener(new Runnable() {
			@Override
			public void run() {
				entryView.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
			}
		});
		animator.startAnimation(anim);
	}
	
	private void sendCreateDuplicate(Score score, String newTitle) {
		DuplicateReceiver receiver = new DuplicateReceiver(null, score.getId());
		registerAndEnqueue(receiver, CALLBACK_ACTION_DUPLICATE);
		Intent request = AsyncHelper.prepareServiceIntent(MainActivity.this, 
			ContentService.class, ContentService.ACTIONS.DUPLICATE_SCORE, 
			receiver.getCurrentRequestId(), getBroadcastCallback(CALLBACK_ACTION_DUPLICATE), true);
		request.putExtra(ContentService.ACTIONS.EXTRAS_ENTITY_ID, score.getId());
		request.putExtra(ContentService.ACTIONS.EXTRAS_NEW_TITLE, newTitle);
		log.v("Sending request DUPLICATE of Score#%d", score.getId());
		startService(request);
		// show progress indicator
		View entryView = findEntryView(score.getId());
		if(entryView != null) {
			addProgressLock(entryView);
			addLock(entryView, R.id.button_duplicate);
		}
	}
	
	private void sendUpdate(Score score) {
		Intent request = AsyncHelper.prepareServiceIntent(
			MainActivity.this, ContentService.class, 
			ContentService.ACTIONS.CHANGE_SCORE_TITLE, 
			null, null, false);
		request.putExtra(ContentService.ACTIONS.EXTRAS_ENTITY_ID, score.getId());
		request.putExtra(ContentService.ACTIONS.EXTRAS_NEW_TITLE, score.getTitle());
		log.v("Sending request CHANGE_TITLE of Score#%d", score.getId());
		startService(request);
		// update view
		View entry = findEntryView(score.getId());
		if(entry == null) {
			log.w("Couldn't find entry View for Score %d#%s", score.getId(), score.getTitle());
		} else {
			populateEntryTextViews(score, entry);
			ensureViewIsVisible(entry, true);
		}
	}
	
	private class ExportMidiReceiver extends EnqueuedReceiver {
		private ExportMidiRequest state;

		public ExportMidiReceiver(ExportMidiRequest state) {
			super(state.requestId, ReceiverType.SCORE_EXPORTED);
			this.state = state;
		}

		@Override
		protected void onFailureReceived(Intent response) {
			hideProgress();
			showErrorDialog(R.string.errormsg_failed_to_export_midi, ERRORDIALOG_CALLBACKARG_INFO);
		}

		@Override
		protected void onSuccessReceived(Intent response) {
			// delay so user can notice progress indicator and how it fades away
			uiHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					hideProgress();
					Toast.makeText(getApplicationContext(), R.string.toast_midi_export_finished, Toast.LENGTH_SHORT).show();
					String relDir = WorkerService.getExportDir().getAbsolutePath();
					relDir = relDir.replace(Environment.getExternalStorageDirectory().getAbsolutePath(), "");
					showTipDialog(TIP_MIDI_ON_STORAGE, 
						R.string.tip_midifile_exported, new String[] {
						state.filename, getString(R.string.midiArtist), getString(R.string.midiAlbum), relDir
					});
				}
			}, 500);
		}
		
		private void hideProgress() {
			View entryView = findEntryView(state.scoreId);
			if(entryView != null) {
				removeProgressLock(entryView);
				removeLock(entryView, R.id.button_exportmidi);
			}
		}
		
		public ExportMidiRequest getState() {
			state.requestId = getCurrentRequestId();
			return state;
		}
	}
		
	private void sendExportMidiRequest(ExportMidiRequest state) {
		ExportMidiReceiver receiver = new ExportMidiReceiver(state);
		registerAndEnqueue(receiver, CALLBACK_ACTION_EXPORTMIDI);
		String requestId = receiver.getCurrentRequestId();
		Intent request = AsyncHelper.prepareServiceIntent(MainActivity.this, 
			WorkerService.class, WorkerService.ACTIONS.EXPORT_TO_MIDI, 
			requestId, 
			getBroadcastCallback(CALLBACK_ACTION_EXPORTMIDI), true);
		request.putExtra(WorkerService.ACTIONS.EXTRAS_SCORE_ID, state.scoreId);
		request.putExtra(WorkerService.ACTIONS.EXTRAS_DEST_FILE, state.filename);
		log.v("Sending request(%s) EXPORT_MIDI of Score#%d", requestId, state.scoreId);
		startService(request);
		// show progress indicator
		View entryView = findEntryView(state.scoreId);
		if(entryView != null) {
			addProgressLock(entryView);
			addLock(entryView, R.id.button_exportmidi);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(scores != null) {
			outState.putParcelableArrayList(STATE_SCORES, scores);
			if(expandedEntry != null && expandedEntry.getTag() != null) {
				outState.putLong(STATE_EXPANDED_ENTRY_SCOREID, ((Score) expandedEntry.getTag()).getId());
			}
			ArrayList<ReceiverState> states = new ArrayList<ReceiverState>(receivers.size());
			for(EnqueuedReceiver receiver: receivers) {
				states.add(receiver.getState());
				unregisterReceiver(receiver);
			}
			receivers.clear();
			outState.putParcelableArrayList(STATE_RECEIVERS_STATES, states);			
		}
	}
	
	@Override
	protected void onDestroy() {
		dismissReceivers(receivers);
		super.onDestroy();
	}

	private <Receiver extends FilterByRequestIdReceiver> void dismissReceivers(List<Receiver> receivers) {
		for(Receiver receiver: receivers) {
			unregisterReceiver(receiver);
		}
		receivers.clear();
	}
	
	private static int[] point = new int[2];
	private static Rect rectangle = new Rect();
	
	private void ensureViewIsVisible(View view, boolean scrollImmediate) {
		rectangle.set(0, 0, view.getWidth(), view.getHeight());
		view.getLocationOnScreen(point);
		rectangle.offsetTo(point[0], point[1]);
		ScrollView scrollView = (ScrollView) findViewById(R.id.main_scrollview);
		scrollView.getChildAt(0).getLocationOnScreen(point);
		rectangle.offset(-point[0], -point[1]);
		scrollView.requestChildRectangleOnScreen(scrollView.getChildAt(0), rectangle, scrollImmediate);
	}

	private class ExpandKeepVisibleAnimation extends
			ViewHeightAnimation.ExpandAnimation<MainActivity> {
		private ExpandKeepVisibleAnimation(View view, long duration) {
			super(view, duration);
		}

		protected void apply(MainActivity ctx, float state) {
			super.apply(ctx, state);
			ensureViewIsVisible(view, true);
		}
	}
	
	private void addLock(View entryView, int buttonId) {
		changeLock(entryView, buttonId, true);
	}

	private void changeLock(View entryView, int buttonId, boolean incr) {
		View button = setupEntryToolbar((ViewGroup) entryView).findViewById(buttonId);
		button.setEnabled(changeLockTag(button, incr) == 0);
	}

	/**
	 * @return locks count after modification
	 */
	private int changeLockTag(View view, boolean incr) {
		int locks = ifNotNull((Integer) view.getTag(R.id.viewtag_locks), 0);
		locks = Math.max(locks + (incr ? 1 : -1), 0);
		view.setTag(R.id.viewtag_locks, locks);
		return locks;
	}

	private void addProgressLock(View entryView) {
		changeProgressLock(entryView, true);		
	}
	
	public void removeLock(View entryView, int buttonId) {
		changeLock(entryView, buttonId, false);
	}

	public void removeProgressLock(View entryView) {
		changeProgressLock(entryView, false);
		
	}

	private void changeProgressLock(View entryView, boolean incr) {
		int locks = changeLockTag(entryView, incr);
		TextView textView = (TextView) entryView.findViewById(R.id.title);
		Drawable[] compoundDrawables = textView.getCompoundDrawables();
		compoundDrawables[2] = locks > 0 ? getResources().getDrawable(R.drawable.spinner_16dp) : null;
		textView.setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[0], compoundDrawables[1], compoundDrawables[2], compoundDrawables[3]);
	}
	
	private void showErrorDialog(int msgId, int infoDialogCallbackId) {
		InfoDialog.newInstance(this, R.string.errordialog_title, msgId, R.string.errordialog_button, null, infoDialogCallbackId)
		.show(getSupportFragmentManager(), DIALOGTAG_INFO);
	}

	private String title(Score score) {
		return Macros.ifNotNull(score.getTitle(), getString(android.R.string.untitled));
	}
	
	@Override
	public void onDismiss(TextInputDialog dialog, int valueId, long listenerArg) {
	}

	private void showExportMidiDialog(final Score score, String initValue) {
		TextInputDialog.newInstance(MainActivity.this, 
			INPUTDIALOG_CALLBACKARG_MIDIFILE, score.getId(),
			R.string.popup_title_export_as_midi, getString(R.string.popup_msg_exportmidi),
			android.R.string.ok, android.R.string.cancel, initValue
		).show(getSupportFragmentManager(), DIALOGTAG_EXPORT_MIDI);
	}

	private void dismissReceiversByType(ReceiverType type) {
		List<EnqueuedReceiver> filtered = new LinkedList<EnqueuedReceiver>();
		for(EnqueuedReceiver recv: receivers) {
			if(recv.type == type) {
				filtered.add(recv);
			}
		}
		receivers.removeAll(filtered);
		dismissReceivers(filtered);
	}	
}
