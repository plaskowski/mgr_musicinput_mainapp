package pl.edu.mimuw.students.pl249278.android.musicinput;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.async.AsyncHelper;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.Macros;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score.ParcelableScore;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.SerializationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.ContentService;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.FilterByRequestIdReceiver;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ErrorDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.FragmentUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ProgressDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TextInputDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog.ConfirmDialogListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ProgressDialog.ProgressDialogListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TextInputDialog.TextInputDialogListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.component.activity.FragmentActivity_ErrorDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutAnimator;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutAnimator.LayoutAnimation;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewHeightAnimation;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity_ErrorDialog implements TextInputDialogListener, ProgressDialogListener, ConfirmDialogListener {
	private static LogUtils log = new LogUtils(MainActivity.class);
	private static final String CALLBACK_ACTION_GET = MainActivity.class.getName()+".callback_get";
	protected static final String CALLBACK_ACTION_DELETE = MainActivity.class.getName()+".callback_delete";
	private static final String CALLBACK_ACTION_DUPLICATE = MainActivity.class.getName()+".callback_duplicate";
	
	protected static final String DIALOGTAG_NEW_TITLE = "dialog_newtitle";
	protected static final String DIALOGTAG_COPY_TITLE = "dialog_copytitle";
	protected static final String DIALOGTAG_PROGRESS = "dialog_progress";
	protected static final String DIALOGTAG_CONFIRM_DELETE = "dialog_confirm_delete";
	private static final int ERRORDIALOG_CALLBACKARG_RELOAD = ERRORDIALOG_CALLBACKARG_DO_FINISH+1;
	private static final int ERRORDIALOG_CALLBACKARG_DUPLICATE = ERRORDIALOG_CALLBACKARG_RELOAD+1;
	protected static final int INPUTDIALOG_CALLBACKARG_NEW_TITLE = 1;
	protected static final int CONFIRMDIALOG_CALLBACKARG_DELETESCORE = 1;
	protected static final int INPUTDIALOG_CALLBACKARG_COPY_TITLE = 2;
	
	private static final String STATE_REQUEST_ID = "request_id";
	private static final String STATE_SCORES = "scores";
	private static final String STATE_EXPANDED_ENTRY_SCOREID = "expanded_scoreid";
	private static final String STATE_DELETE_REQUESTS = "delete_requests";
	private static final String STATE_DUPLICATE_REQUESTS = "duplicate_requests";
	
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
	private LayoutAnimator<MainActivity> animator = new LayoutAnimator<MainActivity>(this);
	protected View expandedEntry;

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.mainscreen);
		
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
				receiver.inflateAndAddProgressView();
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
			receiver.getUniqueRequestID(false), callbackIntent(callbackAction)
		));
	}

	private void requestModel(String requestId) {
		PendingIntent callbackIntent = PendingIntent.getBroadcast(this, 0, new Intent(CALLBACK_ACTION_GET), 0);
		Intent requestIntent;
		if(requestId != null) {
			receiver = new ContentReceiver(requestId);
			requestIntent = AsyncHelper.prepareRepeatCallbackIntent(
				this, 
				ContentService.class, 
				requestId, 
				callbackIntent
			);
		} else {
			receiver = new ContentReceiver();
			requestIntent = AsyncHelper.prepareServiceIntent(
				this, 
				ContentService.class, 
				ContentService.ACTIONS.LIST_SCORES, 
				receiver.getUniqueRequestID(true), 
				callbackIntent, 
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
			Toast.makeText(MainActivity.this, "Loaded", Toast.LENGTH_SHORT).show();
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
			View toolbar = entry.findViewById(R.id.entry_toolbar);
			if(toolbar == null) {
				toolbar = getLayoutInflater().inflate(R.layout.mainscreen_entry_toolbar, entry, false);
				entry.addView(toolbar);
				setupEntryToolbarCallbacks((Score) entry.getTag(), toolbar);
			}
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
				ConfirmDialog.newInstance(
					MainActivity.this, CONFIRMDIALOG_CALLBACKARG_DELETESCORE, score.getId(), 
					R.string.confirmmsg_delete, new String[] { title(score) }, 
					android.R.string.ok, android.R.string.cancel)
				.show(getSupportFragmentManager(), DIALOGTAG_CONFIRM_DELETE);
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
	}
	
	@Override
	public void onConfirm(ConfirmDialog dialog, int dialogId, long callbackParam) {
		switch(dialogId) {
		case CONFIRMDIALOG_CALLBACKARG_DELETESCORE:
			Score score = findScoreById(callbackParam);
			if(score != null) {
				deleteScore(score);
			} else {
				log.w("Received delete confirmation for non-existient Score#%d", callbackParam);
			}
			break;
		}
	}
	
	/**
	 * Send DELETE request to {@link ContentService}, removes view and updates {@link #scores}
	 */
	private void deleteScore(Score score) {
		PendingIntent callbackIntent = callbackIntent(CALLBACK_ACTION_DELETE);
		DeleteScoreReceiver receiver = new DeleteScoreReceiver();
		Intent requestIntent = AsyncHelper.prepareServiceIntent(
			MainActivity.this, 
			ContentService.class, 
			ContentService.ACTIONS.DELETE_SCORE, 
			receiver.getUniqueRequestID(true), 
			callbackIntent, 
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

	private PendingIntent callbackIntent(String callbackAction) {
		return PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(callbackAction), 0);
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
	public void onDismiss(ErrorDialog dialog, int arg) {
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
		View progressView;
		
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
				if(progressView != null) {
					LayoutAnimation<MainActivity, ?> animation = animator.getAnimation(progressView);
					if(animation != null) {
						animator.stopAnimation(animation);
					}
					animator.startAnimation(new ViewHeightAnimation.CollapseAnimation<MainActivity>(progressView, 150));
				}
				// create and reveal entry with received copy
				ParcelableScore pScore = response.getParcelableExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_ENTITY);
				scores.add(scores.indexOf(findParcelableScoreById(originalScoreId))+1, pScore);
				ViewGroup container = (ViewGroup) findViewById(R.id.entries_container);
				final View entryView = inflateAndPopulateEntry(pScore.getSource(), container);
				container.addView(entryView, ViewUtils.indexOf(container, progressView));
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
				if(progressView != null) {
					progressView.setVisibility(View.GONE);
					progressView = null;
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
		
		View inflateAndAddProgressView() {
			ViewGroup container = (ViewGroup) findViewById(R.id.entries_container);
			View progressView = getLayoutInflater().inflate(R.layout.mainscreen_progress_stub, container, false);
			container.addView(progressView, ViewUtils.indexOf(container, findEntryView(originalScoreId))+1);
			this.progressView = progressView;
			return progressView;
		}
	}
	
	public static class DuplicateRequest implements Parcelable {
		private long scoreId;
		private String requestId;
		
		public DuplicateRequest(long scoreId, String requestId) {
			this.scoreId = scoreId;
			this.requestId = requestId;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeLong(scoreId);
			out.writeString(requestId);
		}
		
		private DuplicateRequest(Parcel in) {
			scoreId = in.readLong();
			requestId = in.readString();
		}
		
		public static final Parcelable.Creator<DuplicateRequest> CREATOR = new Parcelable.Creator<DuplicateRequest>() {
			public DuplicateRequest createFromParcel(Parcel in) {
				return new DuplicateRequest(in);
			}
			
			public DuplicateRequest[] newArray(int size) {
				return new DuplicateRequest[size];
			}
		};
		
		public int describeContents() {
			return 0;
		}
	}	
	
	private void sendCreateDuplicate(Score score, String newTitle) {
		DuplicateReceiver receiver = new DuplicateReceiver(null, score.getId());
		registerReceiver(receiver, new IntentFilter(CALLBACK_ACTION_DUPLICATE));
		duplicateReceivers.add(receiver);
		Intent request = AsyncHelper.prepareServiceIntent(MainActivity.this, 
			ContentService.class, ContentService.ACTIONS.DUPLICATE_SCORE, 
			receiver.getUniqueRequestID(true), callbackIntent(CALLBACK_ACTION_DUPLICATE), true);
		request.putExtra(ContentService.ACTIONS.EXTRAS_ENTITY_ID, score.getId());
		request.putExtra(ContentService.ACTIONS.EXTRAS_NEW_TITLE, newTitle);
		log.v("Sending request DUPLICATE of Score#%d", score.getId());
		startService(request);
		// show progress indicator
		View progressView = receiver.inflateAndAddProgressView();
		ViewHeightAnimation.ExpandAnimation.fillBefore(progressView);
		animator.startAnimation(new ExpandKeepVisibleAnimation(progressView, 150));
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

	private String title(Score score) {
		return Macros.ifNotNull(score.getTitle(), getString(android.R.string.untitled));
	}
	
	@Override
	public void onCancel(ConfirmDialog dialog, int dialogId, long callbackParam) {
	}
	
	@Override
	public void onDismiss(TextInputDialog dialog, int valueId, long listenerArg) {
	}
	
}
