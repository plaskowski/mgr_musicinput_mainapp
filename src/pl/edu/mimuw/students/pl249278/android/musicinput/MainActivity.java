package pl.edu.mimuw.students.pl249278.android.musicinput;

import java.util.ArrayList;

import pl.edu.mimuw.students.pl249278.android.async.AsyncHelper;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.Macros;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score.ParcelableScore;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.SerializationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.ContentService;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.FilterByRequestIdReceiver;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TextInputDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TextInputDialog.TextInputDialogListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.component.activity.FragmentActivity_ErrorDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutAnimator;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewHeightAnimation;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity_ErrorDialog implements TextInputDialogListener {
	private static final String CALLBACK_ACTION_GET = MainActivity.class.getName()+".callback_get";
	private static final String STATE_REQUEST_ID = "request_id";	
	private static final String STATE_SCORES = "scores";
	protected static final int INPUT_NEW_TITLE = 1;
	protected static final String DIALOG_NEW_TITLE = "dialog_newtitle";
	private static final String STATE_EXPANDED_ENTRY_SCOREID = "expanded_scoreid";	
	private static LogUtils log = new LogUtils(MainActivity.class);
	
	private ArrayList<ParcelableScore> scores = null;
	
	private ProgressDialog progressDialog;
	/**
	 * Receives response from {@link ContentService}. Not null means request was sent and response haven't been received yet.
	 */
	private ContentReceiver receiver;
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
		} else {
			PendingIntent callbackIntent = PendingIntent.getBroadcast(this, 0, new Intent(CALLBACK_ACTION_GET), 0);
			Intent requestIntent;
			if(savedState != null && savedState.containsKey(STATE_REQUEST_ID)) {
				String requestId = savedState.getString(STATE_REQUEST_ID);
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
	    	progressDialog = ProgressDialog.show(this, "", 
				getString(R.string.msg_loading_please_wait), true);
		}
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
			unregisterReceiver(this);
			receiver = null;
			progressDialog.dismiss();
			progressDialog = null;
			showErrorDialog(R.string.errormsg_unrecoverable, null, true);
		}
		
		@Override
		protected void onSuccess(Intent response) {
			unregisterReceiver(this);
			receiver = null;
			progressDialog.dismiss();
			progressDialog = null;
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
			View entry = getLayoutInflater().inflate(R.layout.mainscreen_entry, container, false);
			populateEntryTextViews(score, entry);
			container.addView(entry);
			entry.setOnClickListener(entryClickListener);
			entry.setTag(score);
		}
	}

	private void populateEntryTextViews(Score score, View entry) {
		((TextView) entry.findViewById(R.id.title)).setText(
			Macros.ifNotNull(score.getTitle(), getString(android.R.string.untitled)));
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
				animator.startAnimation(new ViewHeightAnimation.ExpandAnimation<MainActivity>(toolbar, 300) {
					protected void apply(MainActivity ctx, float state) {
						super.apply(ctx, state);
						ensureViewIsVisible(view, true);
					}
				});
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
					INPUT_NEW_TITLE, score.getId(), 
					getString(R.string.popup_title_change_title), android.R.string.ok, android.R.string.cancel, initialValue)
				.show(getSupportFragmentManager(), DIALOG_NEW_TITLE);
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
	}
	
	@Override
	public void onDismiss(TextInputDialog dialog, int valueId, long listenerArg) {
	}
	
	@Override
	public void onValueEntered(TextInputDialog dialog, int valueId,
			long listenerArg, String value) {
		switch(valueId) {
		case INPUT_NEW_TITLE:
			Score score;
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
		}		
	}
	
	private Score findScoreById(long scoreId) {
		for (ParcelableScore score : scores) {
			if(score.getSource().getId() == scoreId)
				return score.getSource();
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
		if(receiver != null) {
			outState.putString(STATE_REQUEST_ID, receiver.getUniqueRequestID(false));
			unregisterReceiver(receiver);
			receiver = null;
		} else if(scores != null) {
			outState.putParcelableArrayList(STATE_SCORES, scores);
			if(expandedEntry != null) {
				outState.putLong(STATE_EXPANDED_ENTRY_SCOREID, ((Score) expandedEntry.getTag()).getId());
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		if(receiver != null) {
			unregisterReceiver(receiver);
			sendCleanRequest(receiver.getUniqueRequestID(false));
			receiver = null;
		}
		if(progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		super.onDestroy();
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
	
}
