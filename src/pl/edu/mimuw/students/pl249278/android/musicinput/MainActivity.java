package pl.edu.mimuw.students.pl249278.android.musicinput;

import static pl.edu.mimuw.students.pl249278.android.async.AsyncHelper.getBroadcastCallback;
import static pl.edu.mimuw.students.pl249278.android.common.Macros.ifNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfig;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.SerializationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.ContentService;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.FilterByRequestIdReceiver;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.WorkerService;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog.ConfirmDialogBuilder;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog.ConfirmDialogListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.DateRelativeFormatHelper;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InfoDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ParcelablePrimitives.ParcelableLong;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TextInputDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TextInputDialog.TextInputDialogListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.WorkerThread;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.component.activity.FragmentActivity_ErrorDialog_TipDialog_ProgressDialog_ManagedReceiver;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawable.ScoreThumbnailDrawable;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutAnimator;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutAnimator.LayoutAnimation;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewHeightAnimation;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewHeightAnimation.ExpandAnimation;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.DrawingChildOnTop;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
	private static final String CALLBACK_ACTION_GET_EDITED = MainActivity.class.getName()+".callback_get_edited";
	
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
	private static final String STATE_VISCONFS = "configs";
	private static final String STATE_EXPANDED_ENTRY_SCOREID = "expanded_scoreid";
	private static final String STATE_RECEIVERS_STATES = "receivers_states";
	/** key for persisting {@link #editedScoreId} field */
	private static final String STATE_EDITED_SCOREID = "edited_score";
	
	protected static final int REQUEST_NEW_SCORE = 1;
	protected static final int REQUEST_EDIT = 2;
	
	/**
	 * Not null means whole model has been successfully loaded.
	 */
	private ArrayList<ParcelableScore> scores = null;
	private Map<Long, ScoreVisualizationConfig> scoreConfig = null;
	
	private List<EnqueuedReceiver> receivers = new ArrayList<MainActivity.EnqueuedReceiver>();
	private LayoutAnimator<MainActivity> animator = new LayoutAnimator<MainActivity>(this, 25);
	protected View expandedEntry;
	private Handler uiHandler;
	/** Id of {@link Score} on which "EDIT" action was requested most lately. */
	protected long editedScoreId = -1;
	private WorkerThread thumbnailsThread = new WorkerThread("Score thumbnails");
	
	static enum ReceiverType {
		SCORE_DELETED(ContentService.class),
		SCORE_DUPLICATED(ContentService.class),
		SCORE_EXPORTED(WorkerService.class), 
		GET_CREATED(ContentService.class), 
		GET_EDITED(ContentService.class);
		
		final Class<AsynchronousRequestsService> serviceClass;
		
		@SuppressWarnings("unchecked")
		private <T extends AsynchronousRequestsService> ReceiverType(Class<T> serviceClass) {
			this.serviceClass = (Class<AsynchronousRequestsService>) serviceClass;
		}
	};
	
	private abstract class EnqueuedReceiver extends ManagedReceiver {		
		protected final ReceiverType type;
		
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
			ArrayList<ScoreVisualizationConfig> confs = savedState.getParcelableArrayList(STATE_VISCONFS);
			Map<Long, ScoreVisualizationConfig> scoreConfigMapping = new HashMap<Long, ScoreVisualizationConfig>();
			for(int i = 0; i < scores.size(); i++) {
				scoreConfigMapping.put(scores.get(i).getSource().getId(), confs.get(i));
			}
			Collections.sort(scores, new Comparator<ParcelableScore>() {
				@Override
				public int compare(ParcelableScore lhs, ParcelableScore rhs) {
					long diff = lhs.getSource().getModificationUtcStamp() - rhs.getSource().getModificationUtcStamp();
					if(diff == 0) {
						return 0;
					} else {
						return diff > 0 ? -1 : 1;
					}
				}
			});
			onModelLoaded(scores, scoreConfigMapping);
			editedScoreId = savedState.getLong(STATE_EDITED_SCOREID, -1);
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
				case GET_EDITED:
					long scoreId = ((ByScoreIdRequest) state).scoreId;
					entryView = findEntryView(scoreId);
					if(entryView == null) {
						log.w("Couldn't find entry view for edited Score#%d", scoreId);
					} else {
						addProgressLock(entryView);
						registerEnqueueAndRequestRepeat(
							new GetEditedReceiver((ByScoreIdRequest) state), 
							CALLBACK_ACTION_GET_EDITED);
					}
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
			Parcelable[] configsArr = response.getParcelableArrayExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_VISUAL_CONFS);
			Map<Long, ScoreVisualizationConfig> configMapping = new HashMap<Long, ScoreVisualizationConfig>();
			for (int i = 0; i < scoresArr.length; i++) {
				ParcelableScore pScore = (ParcelableScore) scoresArr[i];
				scores.add(pScore);
				configMapping.put(pScore.getSource().getId(), (ScoreVisualizationConfig) configsArr[i]);
			}
			onModelLoaded(scores, configMapping);
		}
	}
	
	private void onModelLoaded(ArrayList<ParcelableScore> scores, Map<Long, ScoreVisualizationConfig> scoreConfigMapping) {
		this.scores = scores;
		this.scoreConfig = scoreConfigMapping;
		ViewGroup container = (ViewGroup) findViewById(R.id.entries_container);
		for(ParcelableScore pScore: scores) {
			Score score = pScore.getSource();
			View entry = inflateAndPopulateEntry(score, container);
			container.addView(entry);
		}
		updateMsgOnEmptyState();
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
	
	private void updateMsgOnEmptyState() {
		ViewGroup container = (ViewGroup) findViewById(R.id.entries_container);
		boolean notEmpty = false;
		int childCount = container.getChildCount();
		for(int i = 0; i < childCount; i++) {
			View child = container.getChildAt(i);
			if(notEmpty |= (child.getVisibility() != View.GONE))
				break;
		}
		findViewById(R.id.MAIN_msg_on_empty).setVisibility(notEmpty ? View.GONE : View.VISIBLE);
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
					recv.getCurrentRequestId(), getBroadcastCallback(CALLBACK_ACTION_GET_CREATED),
					true
				);
				i.putExtra(ContentService.ACTIONS.EXTRAS_ENTITY_ID, scoreId);
				i.putExtra(ContentService.ACTIONS.EXTRAS_ATTACH_SCORE_VISUAL_CONF, true);
				startService(i);
			}
			break;
		case REQUEST_EDIT:
			long scoreId = editedScoreId;
			Score score = findScoreById(scoreId);
			if(score == null) {
				log.w("Couldn't find Score#%d that was edited most lately", editedScoreId);
			} else {
				// refresh edited score data
				GetEditedReceiver recv = new GetEditedReceiver(scoreId);
				registerAndEnqueue(recv, CALLBACK_ACTION_GET_EDITED);
				Intent i = AsyncHelper.prepareServiceIntent(this, ContentService.class, 
					ContentService.ACTIONS.GET_SCORE_BY_ID, 
					recv.getCurrentRequestId(), getBroadcastCallback(CALLBACK_ACTION_GET_EDITED),
					true
				);
				i.putExtra(ContentService.ACTIONS.EXTRAS_ENTITY_ID, scoreId);
				i.putExtra(ContentService.ACTIONS.EXTRAS_ATTACH_SCORE_VISUAL_CONF, true);
				View entry = findEntryView(scoreId);
				if(entry == null) {
					log.w("Couldn't find entry View for Score#%d that was edited", scoreId);
				} else {
					addProgressLock(entry);
				}
				startService(i);
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	private abstract class GetScoreReceiver extends EnqueuedReceiver {
		long scoreId;

		public GetScoreReceiver(ReceiverType type, long scoreId) {
			super(type);
			this.scoreId = scoreId;
		}
		
		public GetScoreReceiver(ReceiverType type, ByScoreIdRequest state) {
			super(state.requestId, type);
			this.scoreId = state.scoreId;
		}
		
		@Override
		protected void onFailureReceived(Intent response) {
			sendCleanSilently();
			dismissReceiversByType(type);
			showErrorDialog(R.string.errormsg_failed_to_refresh, ERRORDIALOG_CALLBACKARG_RELOAD);
		}
		
		@Override
		protected void onSuccessReceived(Intent response) {
			sendCleanSilently();
			ParcelableScore pScore = response.getParcelableExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_ENTITY);
			ScoreVisualizationConfig config = response.getParcelableExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_VISUAL_CONF);
			onScoreReceived(pScore, config);
		}
		
		protected abstract void onScoreReceived(ParcelableScore pScore, ScoreVisualizationConfig config);

		@Override
		public ReceiverState getState() {
			return new ByScoreIdRequest(type, scoreId, getCurrentRequestId());
		}
	}
	
	private class GetCreatedReceiver extends GetScoreReceiver {		
		private GetCreatedReceiver(ByScoreIdRequest state) {
			super(ReceiverType.GET_CREATED, state);
		}

		private GetCreatedReceiver(long scoreId) {
			super(ReceiverType.GET_CREATED, scoreId);
		}

		@Override
		protected void onScoreReceived(ParcelableScore pScore, ScoreVisualizationConfig config) {
			long stamp = pScore.getSource().getModificationUtcStamp();
			// find index in list ordered by modification stamp
			int i = 0;
			for(; i < scores.size(); i++) {
				if(stamp >= scores.get(i).getSource().getModificationUtcStamp()) {
					break;
				}
			}
			onNewScoreArrived(i, pScore, config, new AnimatedMoveUp(pScore.getSource().getId(), true));
		}
	}
	
	private class GetEditedReceiver extends GetScoreReceiver {
		private GetEditedReceiver(ByScoreIdRequest state) {
			super(ReceiverType.GET_EDITED, state);
		}

		private GetEditedReceiver(long scoreId) {
			super(ReceiverType.GET_EDITED, scoreId);
		}
		
		@Override
		protected void onScoreReceived(ParcelableScore pScore, ScoreVisualizationConfig config) {
			long scoreId = pScore.getSource().getId();
			View entryView = findEntryView(scoreId);
			if(entryView == null) {
				log.w("Couldn't find a view for edited Score#%d so can't refresh it", scoreId);
			} else {
				removeProgressLock(entryView);
				int index = scores.indexOf(findParcelableScoreById(scoreId));
				if(index < 0) {
					log.w("Couldn't find an original ParcelableScore#%d object to swap with", scoreId);
				} else {
					Score prevVersion = scores.get(index).getSource();
					if(pScore.getSource().getModificationUtcStamp() <= prevVersion.getModificationUtcStamp()) {
						// no modification saved on this Score so there is nothing to refresh
						return;
					}
					// update object in model
					scores.set(index, pScore);
					scoreConfig.put(scoreId, config);
					// update view with new data
					populateEntry(pScore.getSource(), entryView);
					AnimatedMoveUp moveUpAnim = new AnimatedMoveUp(scoreId, true);
					if(moveUpAnim.isRequired() && expandedEntry == entryView) {
						entryView.setSelected(true);
						// collapse first
						entryClickListener.collapseExpanded(moveUpAnim);
					} else {
						moveUpAnim.run();
					}
				}
			}
		}
	}

	private View inflateAndPopulateEntry(Score score, ViewGroup container) {
		View entry = getLayoutInflater().inflate(R.layout.mainscreen_entry, container, false);
		entry.setOnClickListener(entryClickListener);
		populateEntry(score, entry);
		return entry;
	}
	
	private void populateEntry(Score score, View entry) {
		populateEntryTextViews(score, entry);
		entry.setTag(score);
		// prepare background (under content, without toolbar) drawable with Score preview
		try {			
			ScoreThumbnailDrawable thumb = new ScoreThumbnailDrawable(getApplicationContext(), 
				thumbnailsThread, getResources().getColorStateList(R.color.main_entry_thumbnail));
			thumb.setModel(score.getContent(), scoreConfig.get(score.getId()).getDisplayMode());
			thumb.setLoadingIcon(getResources().getDrawable(R.drawable.spinner_16dp));
			entry.findViewById(R.id.mainscreen_entry_content).setBackgroundDrawable(thumb);
		} catch (SerializationException e) {
			log.e("Failed to deserialize score content", e);
			showErrorDialog(R.string.errormsg_unrecoverable, e, true);
		}
	}

	private void populateEntryTextViews(Score score, View entry) {
		((TextView) entry.findViewById(R.id.title)).setText(
			title(score));
		((TextView) entry.findViewById(R.id.created)).setText(
			formatDate(score.getCreationUtcStamp()));
		((TextView) entry.findViewById(R.id.modified)).setText(
				formatDate(score.getModificationUtcStamp()));
	}
	
	private static class ChangeVisibility implements Runnable {
		private View view;
		private int visibility;
		
		private ChangeVisibility(View view, int visibility) {
			this.view = view;
			this.visibility = visibility;
		}

		@Override
		public void run() {
			view.setVisibility(visibility);
		}
	}
	
	private ToggleEntryToolbar entryClickListener = new ToggleEntryToolbar();
	
	private class ToggleEntryToolbar implements OnClickListener {
		private View prev = null;
		
		@Override
		public void onClick(View view) {
			ViewGroup entry = (ViewGroup) view;
			View toolbar = setupEntryToolbar(entry);
			if(prev != null) {
				ViewHeightAnimation.CollapseAnimation<MainActivity> anim = new ViewHeightAnimation.CollapseAnimation<MainActivity>(prev, 300);
				anim.setOnAnimationEndListener(new ChangeVisibility(prev, View.GONE));
				stopAndStart(anim);
			}
			if(toolbar != prev) {
				toolbar.setVisibility(View.VISIBLE);
				stopAndStart(new ExpandKeepVisibleAnimation(toolbar, 300));
				prev = toolbar;
				expandedEntry = view;
			} else {
				prev = null;
				expandedEntry = null;
			}
		}
		
		public void collapseExpanded(Runnable onAnimationEnd) {
			if(expandedEntry != null) {
				ViewHeightAnimation.CollapseAnimation<MainActivity> anim = new ViewHeightAnimation.CollapseAnimation<MainActivity>(prev, 300);
				anim.setOnAnimationEndListener(onAnimationEnd);
				stopAndStart(anim);
				prev = null;
				expandedEntry = null;
			}
		}
		
		private void stopAndStart(LayoutAnimation<MainActivity, ?> anim) {
			LayoutAnimation<MainActivity, ?> oldAnim = animator.getAnimation(anim.getView());
			if(oldAnim != null) {
				animator.stopAnimation(oldAnim);
			}
			animator.startAnimation(anim);
		}
	};
	
	private DateRelativeFormatHelper dateHelper;
	
	private CharSequence formatDate(long utcStamp) {
		if(dateHelper == null) {
			dateHelper = new DateRelativeFormatHelper(this);
		}
		return dateHelper.formatDate(utcStamp);
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
				editedScoreId = score.getId();
				startActivityForResult(intent, REQUEST_EDIT);
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
	public void onDialogResult(ConfirmDialog dialog, int dialogId,
			DialogAction action, Parcelable state) {
		switch(dialogId) {
		case CONFIRMDIALOG_CALLBACKARG_DELETESCORE:
			switch(action) {
			case BUTTON_POSITIVE:
				Long scoreId = ((ParcelableLong) state).value;
				Score score = findScoreById(scoreId);
				if(score != null) {
					deleteScore(score);
				} else {
					log.w("Received delete confirmation for non-existient Score#%d", scoreId);
				}
				break;
			}
			break;
		case CONFIRMDIALOG_CALLBACKARG_MIDIFILE_OVERWRITE:
			switch(action) {
			case BUTTON_POSITIVE:
				// user chose to overwrite existing MIDI file
				sendExportMidiRequest((ExportMidiRequest) state);
				break;
			case BUTTON_NEUTRAL:
				ExportMidiRequest request = (ExportMidiRequest) state;
				showExportMidiDialog(findScoreById(request.scoreId), request.filename);
				break;
			}
			break;
		default:
			super.onDialogResult(dialog, dialogId, action, state);
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
    		fadeOut.setDuration(getResources().getInteger(R.integer.mainscreen_deleted_fadeout_duration));
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
				    		updateMsgOnEmptyState();
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
    	ParcelableScore pScore = findParcelableScoreById(score.getId());
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
			ScoreVisualizationConfig visConfig = response.getParcelableExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_VISUAL_CONF);
			int insertIndex = Math.max(0, scores.indexOf(findParcelableScoreById(originalScoreId)));
			onNewScoreArrived(insertIndex, pScore, visConfig, new AnimatedMoveUp(pScore.getSource().getId()));
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
	
	private static class AnimatedMoveUpConsts {
		int initialDuration, decreaseStep, minDuration, notanimDuration, horizontalDeviation, highlightOffDelay;
		Handler uiHandler;
		
		public AnimatedMoveUpConsts(Resources res) {
			initialDuration = res.getInteger(R.integer.mainscreen_moveup_duration);
			decreaseStep = res.getInteger(R.integer.mainscreen_moveup_decrease_step);
			minDuration = res.getInteger(R.integer.mainscreen_moveup_min_duration);
			notanimDuration = res.getInteger(R.integer.mainscreen_moveup_notanim_duration);
			horizontalDeviation = res.getDimensionPixelOffset(R.dimen.mainscreen_moveup_deviation);
			highlightOffDelay = res.getInteger(R.integer.mainscreen_moveup_highlight_off_delay);
			uiHandler = new Handler();
		}
	}
	
	private AnimatedMoveUpConsts consts;
	
	/** 
	 * Utility class that checks if given Score is at its right place in model ({@link MainActivity#scores}) that is ordered by modification stamp.
	 * If not it starts an {@link LayoutAnimation} of swapping given Score with the one above 
	 * and updates immediately {@link ParcelableScore} object position in model. 
	 * This task is repeated until Score object reaches its correct position in model. 
	 */
	private class AnimatedMoveUp implements Runnable {
		long scoreId;
		boolean keepVisible;
		private int duration;
		private int loopNo = -1;
		private boolean wasTopVisible;
		
		private AnimatedMoveUp(long scoreId) {
			this(scoreId, false);
		}
		
		private AnimatedMoveUp(long scoreId, boolean keepVisible) {
			this.scoreId = scoreId;
			this.keepVisible = keepVisible;
			if(consts == null) {
				consts = new AnimatedMoveUpConsts(getResources());
			}
			this.duration = consts.initialDuration;
		}

		public boolean isRequired() {
			ParcelableScore pScore = findParcelableScoreById(scoreId);
			int indexInModel;
			if(pScore == null || (indexInModel = scores.indexOf(pScore)) < 0) {
				log.v("Score#%d to move up not found in model", scoreId);
				return false;
			}
			long stamp = pScore.getSource().getModificationUtcStamp();
			return indexInModel > 0 && scores.get(indexInModel-1).getSource().getModificationUtcStamp() <= stamp;
		}
		
		@Override
		public void run() {
			loopNo++;
			if(loopNo == 0) {
				wasTopVisible = isTopVisible();
			}
			ViewGroup container = (ViewGroup) findViewById(R.id.entries_container);
			int entryViewIndex = findEntryViewIndex(scoreId);
			ParcelableScore pScore = findParcelableScoreById(scoreId);
			int indexInModel = scores.indexOf(pScore);
			boolean isRequired = isRequired();
			final View entryView = container.getChildAt(entryViewIndex);
			if(isRequired) {
				entryView.setSelected(true);
				ParcelableScore upper = scores.get(indexInModel-1);
				scores.remove(pScore);
				scores.add(indexInModel-1, pScore);
				int upperViewIndex = findEntryViewIndex(upper.getSource().getId());
				if(duration >= consts.minDuration) {
					moveEntryUpAnimation(
						entryViewIndex,
						upperViewIndex,
						duration
					);
					duration -= consts.decreaseStep;
				} else {
					if(keepVisible) {
						ensureViewIsVisible(container.getChildAt(upperViewIndex), true);
					}
					View lowerView = entryView;
					container.removeView(lowerView);
					container.addView(lowerView, upperViewIndex);
					consts.uiHandler.postDelayed(this, consts.notanimDuration);
				}
			} else if(keepVisible && indexInModel == 0 && !wasTopVisible && loopNo > 0) {
				ScrollView scrollView = (ScrollView) findViewById(R.id.main_scrollview);
				LayoutAnimation<MainActivity, ?> scrollAnim = new LayoutAnimation<MainActivity, ScrollView>(scrollView, 
						scrollView.getScrollY(), -scrollView.getScrollY(), 300) {
					@Override
					protected void apply(MainActivity ctx, float state) {
						view.scrollTo(0, start_value + (int) (state*delta));
					}
				};
				scrollAnim.setOnAnimationEndListener(new Runnable() {
					@Override
					public void run() {
						consts.uiHandler.postDelayed(new Deselect(entryView), consts.highlightOffDelay);
					}
				});
				animator.startAnimation(scrollAnim);
			} else {
				consts.uiHandler.postDelayed(new Deselect(entryView), consts.highlightOffDelay);
			}
		}

		private boolean isTopVisible() {
			ScrollView scrollView = (ScrollView) findViewById(R.id.main_scrollview);
			View topView = findViewById(R.id.MAIN_entry_addnew);
			return scrollView.getScrollY() <= topView.getHeight()/2;
		}

		private void moveEntryUpAnimation(int lowerEntryIndex, int upperEntryIndex, final int time) {
			ViewGroup container = (ViewGroup) findViewById(R.id.entries_container);
			View upperView = container.getChildAt(upperEntryIndex);
			final int upperH = viewHeight(upperView);
			View lowerView = container.getChildAt(lowerEntryIndex);
			final int lowerH = viewHeight(lowerView);
			animator.startAnimation(new LayoutAnimation<MainActivity, View>(upperView, 0, 0, time) {
				@Override
				protected void apply(MainActivity ctx, float state) {
					float revState = 1-state;
					int top = (int) (-upperH*revState) + (int) (-lowerH*revState);
					int bottom = (int) (lowerH*revState);
					setVMargins(view, top, bottom);
					int hdiff = (int) (-4 * state * (state -1) * consts.horizontalDeviation);
					setHMargins(view, -hdiff, hdiff);
				}
			});
			animator.startAnimation(new LayoutAnimation<MainActivity, View>(lowerView, 0, 0, time, this) {
				@Override
				protected void apply(MainActivity ctx, float state) {
					float revState = 1-state;
					int top = (int) (upperH*revState);
					setVMargins(view, 
						top, 
						0);
					int hdiff = (int) (-4 * state * (state -1) * consts.horizontalDeviation);
					setHMargins(view, hdiff, -hdiff);
					if(keepVisible) {
						ensureViewIsVisible(view, true);
					}
				}
			});
			container.removeView(lowerView);
			container.addView(lowerView, upperEntryIndex);
			setVMargins(upperView, -upperH-lowerH, lowerH);
			setVMargins(lowerView, upperH, 0);
			((DrawingChildOnTop) container).setFrontChildView(lowerView);
		}
		
		private int viewHeight(View view) {
			if(view.getHeight() != 0) {
				return view.getHeight();
			} else {
				int unspc = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
				view.measure(unspc, unspc);
				return view.getMeasuredHeight();
			}
		}
		
		private void setHMargins(View view, int left, int right) {
			ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
			params.leftMargin = left;
			params.rightMargin = right;
			view.setLayoutParams(params);
		}
		
		private void setVMargins(View view, int top, int bottom) {
			ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
			params.topMargin = top;
			params.bottomMargin = bottom;
			view.setLayoutParams(params);
		}
	}
	
	private static class Deselect implements Runnable {
		private View view;

		private Deselect(View view) {
			this.view = view;
		}
		
		@Override
		public void run() {
			view.setSelected(false);
		}
	}

	/**
	 * Add object to {@link MainActivity#scores}, inflates new entry view and start animation to reveal it.
	 * Add :selected to entry view.
	 * @param insertAt index in model
	 * @param onAnimFinish task to run on "reveal" animation end
	 */
	private void onNewScoreArrived(int insertAt, ParcelableScore pScore, ScoreVisualizationConfig visConfig, final Runnable onAnimFinish) {
		insertAt = Math.max(insertAt, 0);
		int viewInsertIndex = 0;
		if(insertAt < scores.size()) {
			viewInsertIndex = findEntryViewIndex(scores.get(insertAt).getSource().getId());
		}
		scores.add(insertAt, pScore);
		scoreConfig.put(pScore.getSource().getId(), visConfig);
		final ViewGroup container = (ViewGroup) findViewById(R.id.entries_container);
		final View entryView = inflateAndPopulateEntry(pScore.getSource(), container);
		container.addView(entryView, viewInsertIndex);
		entryView.setVisibility(View.INVISIBLE);
		final int height = entryView.getLayoutParams().height;
		ExpandAnimation<MainActivity> anim = new ViewHeightAnimation.ExpandAnimation<MainActivity>(entryView, 200);
		ExpandAnimation.fillBefore(entryView);
		anim.setOnAnimationEndListener(new Runnable() {
			@Override
			public void run() {
				updateMsgOnEmptyState();
				Animation inAnim = AnimationUtils.makeInAnimation(MainActivity.this, true);
				inAnim.setDuration(500);
				inAnim.setFillBefore(true);
				inAnim.setFillAfter(true);
				entryView.setVisibility(View.VISIBLE);
				entryView.getLayoutParams().height = height;
				entryView.requestLayout();
				entryView.setAnimation(inAnim);
				inAnim.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}
					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					@Override
					public void onAnimationEnd(Animation animation) {
						entryView.setAnimation(null);
						if(onAnimFinish != null) {
							onAnimFinish.run();
						}
					}
				});
				inAnim.startNow();		
				entryView.setSelected(true);
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
		request.putExtra(ContentService.ACTIONS.EXTRAS_ATTACH_SCORE_VISUAL_CONF, true);
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
			ArrayList<ScoreVisualizationConfig> confs = new ArrayList<ScoreVisualizationConfig>(scores.size());
			for(int i = 0; i < scores.size(); i++) {
				confs.add(scoreConfig.get(scores.get(i).getSource().getId()));
			}
			outState.putParcelableArrayList(STATE_VISCONFS, confs);
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
			outState.putLong(STATE_EDITED_SCOREID, editedScoreId);
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
