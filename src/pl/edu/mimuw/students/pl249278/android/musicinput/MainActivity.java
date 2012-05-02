package pl.edu.mimuw.students.pl249278.android.musicinput;

import static pl.edu.mimuw.students.pl249278.android.async.AsyncHelper.getBroadcastCallback;
import static pl.edu.mimuw.students.pl249278.android.common.Macros.ifNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.async.AsyncHelper;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.Macros;
import pl.edu.mimuw.students.pl249278.android.musicinput.MainActivityHelper.DuplicateRequest;
import pl.edu.mimuw.students.pl249278.android.musicinput.MainActivityHelper.ExportMidiRequest;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score.ParcelableScore;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.SerializationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.ContentService;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.FilterByRequestIdReceiver;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.WorkerService;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog.ConfirmDialogBuilder;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog.ConfirmDialogListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.FragmentUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InfoDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ParcelablePrimitives.ParcelableLong;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ProgressDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ProgressDialog.ProgressDialogListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TextInputDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TextInputDialog.TextInputDialogListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.component.activity.FragmentActivity_ErrorDialog_TipDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutAnimator;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewHeightAnimation;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewUtils;
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

public class MainActivity extends FragmentActivity_ErrorDialog_TipDialog implements TextInputDialogListener, ProgressDialogListener, ConfirmDialogListener {
	private static LogUtils log = new LogUtils(MainActivity.class);
	private static final String CALLBACK_ACTION_GET = MainActivity.class.getName()+".callback_get";
	protected static final String CALLBACK_ACTION_DELETE = MainActivity.class.getName()+".callback_delete";
	private static final String CALLBACK_ACTION_DUPLICATE = MainActivity.class.getName()+".callback_duplicate";
	private static final String CALLBACK_ACTION_EXPORTMIDI = MainActivity.class.getName()+".callback_export";
	
	protected static final String DIALOGTAG_NEW_TITLE = "dialog_newtitle";
	protected static final String DIALOGTAG_COPY_TITLE = "dialog_copytitle";
	protected static final String DIALOGTAG_PROGRESS = "dialog_progress";
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
	
	private static final String STATE_REQUEST_ID = "request_id";
	private static final String STATE_SCORES = "scores";
	private static final String STATE_EXPANDED_ENTRY_SCOREID = "expanded_scoreid";
	private static final String STATE_DELETE_REQUESTS = "delete_requests";
	private static final String STATE_DUPLICATE_REQUESTS = "duplicate_requests";
	private static final String STATE_EXPORT_REQUESTS = "export_requests";
	
	/**
	 * Not null means whole model has been successfully loaded.
	 */
	private ArrayList<ParcelableScore> scores = null;
	
	/**
	 * Receives response from {@link ContentService}. 
	 * Not null means request was sent and response haven't been received yet.
	 * Not null also means it is registered (assumption valid on UIThread only).
	 */
	private ContentReceiver receiver;
	private List<FilterByRequestIdReceiver> scoreDeletedReceivers = new ArrayList<FilterByRequestIdReceiver>();
	private List<DuplicateReceiver> duplicateReceivers = new ArrayList<DuplicateReceiver>();
	private List<ExportMidiReceiver> exportMidiReceivers = new ArrayList<MainActivity.ExportMidiReceiver>();
	private LayoutAnimator<MainActivity> animator = new LayoutAnimator<MainActivity>(this);
	protected View expandedEntry;
	private Handler uiHandler;

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
			ArrayList<String> deleteRequests = savedState.getStringArrayList(STATE_DELETE_REQUESTS);
			if(deleteRequests != null) for(String requestId: deleteRequests) {
				FilterByRequestIdReceiver receiver = new DeleteScoreReceiver(requestId);
				registerAndRequestRepeat(receiver, CALLBACK_ACTION_DELETE, scoreDeletedReceivers);
			}
			ArrayList<DuplicateRequest> duplicateRequests = savedState.getParcelableArrayList(STATE_DUPLICATE_REQUESTS);
			if(duplicateRequests != null) for(DuplicateRequest state: duplicateRequests) {
				DuplicateReceiver receiver = new DuplicateReceiver(state);
				registerAndRequestRepeat(receiver, CALLBACK_ACTION_DUPLICATE, duplicateReceivers);
				View entryView = findEntryView(receiver.originalScoreId);
				addProgressLock(entryView);
				addLock(entryView, R.id.button_duplicate);
			}
			ArrayList<ExportMidiRequest> exportRequests = savedState.getParcelableArrayList(STATE_EXPORT_REQUESTS);
			if(exportRequests != null) for(ExportMidiRequest request: exportRequests) {
				ExportMidiReceiver receiver = new ExportMidiReceiver(request);
				registerAndRequestRepeat(receiver, CALLBACK_ACTION_EXPORTMIDI, exportMidiReceivers);
				// show progress indicator
				View entryView = findEntryView(request.scoreId);
				addProgressLock(entryView);
				addLock(entryView, R.id.button_exportmidi);
			}
		} else {
			requestModel(savedState == null ? null : savedState.getString(STATE_REQUEST_ID));
		}
	}
	
	private <Receiver extends FilterByRequestIdReceiver> void registerAndRequestRepeat(Receiver receiver, String callbackAction, List<Receiver> collection) {
		registerReceiver(receiver, new IntentFilter(callbackAction));
		collection.add(receiver);
		startService(AsyncHelper.prepareRepeatCallbackIntent(
			this, ContentService.class, 
			receiver.getUniqueRequestID(false), getBroadcastCallback(callbackAction)
		));
	}

	private void requestModel(String requestId) {
		Intent requestIntent;
		if(requestId != null) {
			receiver = new ContentReceiver(requestId);
			requestIntent = AsyncHelper.prepareRepeatCallbackIntent(
				this, 
				ContentService.class, 
				requestId, 
				getBroadcastCallback(CALLBACK_ACTION_GET)
			);
		} else {
			receiver = new ContentReceiver();
			requestIntent = AsyncHelper.prepareServiceIntent(
				this, 
				ContentService.class, 
				ContentService.ACTIONS.LIST_SCORES, 
				receiver.getUniqueRequestID(true), 
				getBroadcastCallback(CALLBACK_ACTION_GET),
				true
			);
			requestIntent.putExtra(ContentService.ACTIONS.EXTRAS_ATTACH_SCORE_VISUAL_CONF, true);
		}
		registerReceiver(receiver, new IntentFilter(CALLBACK_ACTION_GET));
		startService(requestIntent);
		FragmentUtils.showDialogFragment(this, DIALOGTAG_PROGRESS, 
			ProgressDialog.newInstance(this, R.string.msg_loading_please_wait, true));
	}
	
	private class ContentReceiver extends FilterByRequestIdReceiver {
		
		public ContentReceiver() {
		}

		public ContentReceiver(String currentRequestId) {
			super(currentRequestId);
		}

		@Override
		protected void onFailure(Intent response) {
			log.e("Failed to list scores: " + AsyncHelper.getError(response));
			if(receiver == null) {
				// we received response before unregisterReceiver() took effect. Ignore.
				return;
			}
			unregisterReceiver(this);
			receiver = null;
			FragmentUtils.dismissDialogFragment(MainActivity.this, DIALOGTAG_PROGRESS);
			showErrorDialog(R.string.errormsg_unrecoverable, null, true);
		}
		
		@Override
		protected void onSuccess(Intent response) {
			if(receiver == null) {
				// we received response before unregisterReceiver() took effect. Ignore.
				return;
			}
			unregisterReceiver(this);
			receiver = null;
			FragmentUtils.dismissDialogFragment(MainActivity.this, DIALOGTAG_PROGRESS);
			Parcelable[] scoresArr = response.getParcelableArrayExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_SCORES);
			ArrayList<ParcelableScore> scores = new ArrayList<Score.ParcelableScore>(scoresArr.length);
			for (int i = 0; i < scoresArr.length; i++) {
				Parcelable parcelable = scoresArr[i];
				scores.add((ParcelableScore) parcelable);
			}
			onModelLoaded(scores);
			// TODO handle vis confs
			response.getParcelableArrayExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_VISUAL_CONFS);
			sendCleanRequest(getUniqueRequestID(false));
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
				startActivity(i);
			}
		});
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
			receiver.getUniqueRequestID(true), 
			getBroadcastCallback(CALLBACK_ACTION_DELETE), 
			true
		);
		requestIntent.putExtra(ContentService.ACTIONS.EXTRAS_ENTITY_ID, score.getId());
		registerReceiver(receiver, new IntentFilter(CALLBACK_ACTION_DELETE));
		scoreDeletedReceivers.add(receiver);
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
    	}
	}

	private class DeleteScoreReceiver extends FilterByRequestIdReceiver {
		
		public DeleteScoreReceiver() {
		}

		public DeleteScoreReceiver(String currentRequestId) {
			super(currentRequestId);
		}

		@Override
		protected void onFailure(Intent response) {
			if(!unregister())
				return;
			// failed to delete, so we will refresh all
			dismissReceivers(scoreDeletedReceivers);
			showErrorDialog(R.string.errormsg_failed_to_delete, ERRORDIALOG_CALLBACKARG_RELOAD);
		}

		@Override
		protected void onSuccess(Intent response) {
			if(unregister()) {
				log.v("DELETE request -> onSuccess()");
			}
		}
		
		private boolean unregister() {
			if(scoreDeletedReceivers.remove(this)) {
				unregisterReceiver(this);
				return true;
			} else {
				return false;
			}
		}
	}
	
	@Override
	public void onDismiss(InfoDialog dialog, int arg) {
		if(arg == ERRORDIALOG_CALLBACKARG_RELOAD) {			
			// clear entry views
			((ViewGroup) findViewById(R.id.entries_container)).removeAllViews();
			// read model once again
			scores = null;
			requestModel(null);
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
		ViewGroup wrapper = (ViewGroup) findViewById(R.id.entries_container);
		int total = wrapper.getChildCount();
		for (int i = 0; i < total; i++) {
			View child = wrapper.getChildAt(i);
			Object tag = child.getTag();
			if(tag != null && tag instanceof Score && ((Score) tag).getId() == scoreId)
				return child;
		}
		return null;
	}

	private class DuplicateReceiver extends FilterByRequestIdReceiver {
		private long originalScoreId;
		
		public DuplicateReceiver(DuplicateRequest request) {
			this(request.requestId, request.scoreId);
		}
		
		public DuplicateReceiver(String currentRequestId, long originalScoreId) {
			super(currentRequestId);
			this.originalScoreId = originalScoreId;
		}

		@Override
		protected void onSuccess(Intent response) {
			if(unregister()) {
				// hide progress
				View originalEntryView = findEntryView(originalScoreId);
				if(originalEntryView != null) {
					removeProgressLock(originalEntryView);
					removeLock(originalEntryView, R.id.button_duplicate);
				}
				// create and reveal entry with received copy
				ParcelableScore pScore = response.getParcelableExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_ENTITY);
				scores.add(scores.indexOf(findParcelableScoreById(originalScoreId))+1, pScore);
				ViewGroup container = (ViewGroup) findViewById(R.id.entries_container);
				final View entryView = inflateAndPopulateEntry(pScore.getSource(), container);
				container.addView(entryView, ViewUtils.indexOf(container, originalEntryView)+1);
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
		}

		@Override
		protected void onFailure(Intent response) {
			if(unregister()) {
				showErrorDialog(R.string.errormsg_failed_to_duplicate, ERRORDIALOG_CALLBACKARG_DUPLICATE);
				View originalEntryView = findEntryView(originalScoreId);
				if(originalEntryView != null) {
					removeProgressLock(originalEntryView);
					removeLock(originalEntryView, R.id.button_duplicate);
				}
			}
		}

		private boolean unregister() {
			if(duplicateReceivers.remove(this)) {
				unregisterReceiver(this);
				return true;
			} else {
				return false;
			}
		}

		public Parcelable getState() {
			return new DuplicateRequest(originalScoreId, getUniqueRequestID(false));
		}
		
	}
	
	private void sendCreateDuplicate(Score score, String newTitle) {
		DuplicateReceiver receiver = new DuplicateReceiver(null, score.getId());
		registerReceiver(receiver, new IntentFilter(CALLBACK_ACTION_DUPLICATE));
		duplicateReceivers.add(receiver);
		Intent request = AsyncHelper.prepareServiceIntent(MainActivity.this, 
			ContentService.class, ContentService.ACTIONS.DUPLICATE_SCORE, 
			receiver.getUniqueRequestID(true), getBroadcastCallback(CALLBACK_ACTION_DUPLICATE), true);
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
		try {
			Intent request = AsyncHelper.prepareServiceIntent(
				MainActivity.this, ContentService.class, 
				ContentService.ACTIONS.UPDATE_SCORE, 
				null, null, false);
			request.putExtra(ContentService.ACTIONS.EXTRAS_SCORE, score.prepareParcelable());
			request.putExtra(ContentService.ACTIONS.EXTRAS_KEEP_BACKUP, true);
			log.v("Sending request UPDATE of Score#%d", score.getId());
			startService(request);
			// update view
			View entry = findEntryView(score.getId());
			if(entry == null) {
				log.w("Couldn't find entry View for Score %d#%s", score.getId(), score.getTitle());
			} else {
				populateEntryTextViews(score, entry);
				ensureViewIsVisible(entry, true);
			}
		} catch (SerializationException e) {
			showErrorDialog(R.string.errormsg_unrecoverable, e, true);
			log.e("Failed to serialize model", e);
		}
	}
	
	private class ExportMidiReceiver extends FilterByRequestIdReceiver {
		private ExportMidiRequest state;

		public ExportMidiReceiver(ExportMidiRequest state) {
			super(state.requestId);
			this.state = state;
		}

		@Override
		protected void onFailure(Intent response) {
			if(unregister()) {
				hideProgress();
				showErrorDialog(R.string.errormsg_failed_to_export_midi, ERRORDIALOG_CALLBACKARG_INFO);
			}
		}

		@Override
		protected void onSuccess(Intent response) {
			if(unregister()) {
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
		}
		
		private boolean unregister() {
			if(exportMidiReceivers.remove(this)) {
				unregisterReceiver(this);
				return true;
			} else {
				return false;
			}
		}
		
		private void hideProgress() {
			View entryView = findEntryView(state.scoreId);
			if(entryView != null) {
				removeProgressLock(entryView);
				removeLock(entryView, R.id.button_exportmidi);
			}
		}
		
		public ExportMidiRequest getState() {
			state.requestId = getUniqueRequestID(false);
			return state;
		}
	}
		
	private void sendExportMidiRequest(ExportMidiRequest state) {
		ExportMidiReceiver receiver = new ExportMidiReceiver(state);
		registerReceiver(receiver, new IntentFilter(CALLBACK_ACTION_EXPORTMIDI));
		exportMidiReceivers.add(receiver);
		String requestId = receiver.getUniqueRequestID(state.requestId == null);
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
		FragmentUtils.dismissDialogFragment(MainActivity.this, DIALOGTAG_PROGRESS);
		if(receiver != null) {
			outState.putString(STATE_REQUEST_ID, receiver.getUniqueRequestID(false));
			unregisterReceiver(receiver);
			receiver = null;
		} else if(scores != null) {
			outState.putParcelableArrayList(STATE_SCORES, scores);
			if(expandedEntry != null) {
				outState.putLong(STATE_EXPANDED_ENTRY_SCOREID, ((Score) expandedEntry.getTag()).getId());
			}
			ArrayList<String> deleteRequests = new ArrayList<String>();
			for(FilterByRequestIdReceiver receiver: scoreDeletedReceivers) {
				deleteRequests.add(receiver.getUniqueRequestID(false));
				unregisterReceiver(receiver);
			}
			scoreDeletedReceivers.clear();
			outState.putStringArrayList(STATE_DELETE_REQUESTS, deleteRequests);
			ArrayList<Parcelable> duplicateRequests = new ArrayList<Parcelable>();
			for(DuplicateReceiver receiver: duplicateReceivers) {
				duplicateRequests.add(receiver.getState());
				unregisterReceiver(receiver);
			}
			duplicateReceivers.clear();
			outState.putParcelableArrayList(STATE_DUPLICATE_REQUESTS, duplicateRequests);
			ArrayList<Parcelable> exportRequests = new ArrayList<Parcelable>();
			for(ExportMidiReceiver receiver: exportMidiReceivers) {
				exportRequests.add(receiver.getState());
				unregisterReceiver(receiver);
			}
			exportMidiReceivers.clear();
			outState.putParcelableArrayList(STATE_EXPORT_REQUESTS, exportRequests);
		}
	}
	
	@Override
	protected void onDestroy() {
		if(receiver != null) {
			unregisterReceiver(receiver);
			sendCleanRequest(receiver.getUniqueRequestID(false));
			receiver = null;
		}
		dismissReceivers(scoreDeletedReceivers);
		dismissReceivers(duplicateReceivers);
		dismissReceivers(exportMidiReceivers);
		super.onDestroy();
	}

	private <Receiver extends FilterByRequestIdReceiver> void dismissReceivers(List<Receiver> receivers) {
		for(Receiver receiver: receivers) {
			unregisterReceiver(receiver);
		}
		receivers.clear();
	}
	
	@Override
	public void onCancel(ProgressDialog dialog) {
		// user dismissed "loading ..." dialog so we exit
		finish();
	}

	private void sendCleanRequest(String requestId) {
		Intent cleanRequest = AsyncHelper.prepareCleanCallbackIntent(MainActivity.this, ContentService.class, requestId);
		startService(cleanRequest);
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
	
}
