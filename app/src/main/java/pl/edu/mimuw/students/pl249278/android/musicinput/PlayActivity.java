package pl.edu.mimuw.students.pl249278.android.musicinput;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pl.edu.mimuw.students.pl249278.android.async.AsyncHelper;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.PaintBuilder;
import pl.edu.mimuw.students.pl249278.android.common.ReflectionUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ScoreHelper.InsertDivided;
import pl.edu.mimuw.students.pl249278.android.musicinput.ScorePositioningStrategy.PositioningEnv;
import pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.mixin.ShowScoreActivityWithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy.ActivityStrategyChainRoot;
import pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy.ErrorDialogStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy.InitialProgressDialogStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy.ManagedReceiverStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.LengthSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.PauseSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.PlayingConfiguration;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score.ParcelableScore;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreContentElem;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfig;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.SerializationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.ContentService;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.WorkerService;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InfoDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.PlayPreferencesDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ProgressDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.ElementType;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetAlignedElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.HackedScrollViewChild;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.Sheet5LinesView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.SheetAlignedElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.SheetElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewUtils.OnLayoutListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptsScaleGesture;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.LazyScrolling;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.OnInterceptTouchObservable;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.OnInterceptTouchObservable.OnInterceptListener;
import pl.edu.mimuw.students.pl249278.midi.MidiFile;
import pl.edu.mimuw.students.pl249278.midi.MidiFormatException;

import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
import static pl.edu.mimuw.students.pl249278.android.common.Macros.ifNotNull;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ScoreHelper.elementSpecNN;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ScoreHelper.middleX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ScoreHelper.timeCapacity;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.LINE0_ABSINDEX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.LINE4_ABSINDEX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.overallLength;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutParamsHelper.updateSize;

public class PlayActivity extends ShowScoreActivityWithMixin implements
		OnLayoutListener, InfoDialog.InfoDialogListener,
		ProgressDialog.ProgressDialogListener, AudioManager.OnAudioFocusChangeListener,
		PlayPreferencesDialog.PlayPreferencesDialogListener {
	private static LogUtils log = new LogUtils(PlayActivity.class);
	
	/** of type long */
	public static final String STARTINTENT_EXTRAS_SCORE_ID = "score_id";
	/**
	 * Allows caller to specify score content that is different from state in DB, 
	 * by providing Score object directly. It must have valid identifier that matches 
	 * existing Score id DB. Value Type: {@link ParcelableScore} 
	 */
	public static final String STARTINTENT_EXTRAS_SCORE = null;
	private static final String INSTANCE_STATE_SCORE = "score";
	private static final String INSTANCE_STATE_VISCONF = "visual_conf";
	private static final String INSTANCE_STATE_PLAYCONF = "play_conf";
	private static final String INSTANCE_STATE_LISTENER_POS = "listener_pos";
	/** of type float */
	private static final String INSTANCE_STATE_SCALE = "scale";
	private static final String CALLBACK_ACTION_GET = PlayActivity.class.getName()+".get_score";

	private Score score;
	private List<ScoreContentElem> content;
	private ScoreVisualizationConfig visualConf;
	private PlayingConfiguration playConf;
	
	private MediaPlayer player;
	/** If the last message sent to MediaPlayer was play() */
	private boolean playerIsPlaying = false;
	/** position to seek before next {@link #player} .start() call */
	private Integer lazySeek = null;
	/** Path to temporary generated MIDI file for MediaPlayer playback purpose */
	private File midiLocalFile;
	/** If play configuration (besides .loop field) has changed since last {@link #resumePlaying()} call */
	private boolean configurationUpdated = false;
	
	/** Maps given Score element to View that represents it */
	private Map<ScoreContentElem, SheetAlignedElementView> modelToViewsMapping = new HashMap<ScoreContentElem, SheetAlignedElementView>();
	private ViewGroup sheet;
	private ScrollView vertscroll;
	private Paint normalPaint = PaintBuilder.init().antialias(true).build();
	private Paint highlightPaint;
	private float highlightShadowFactor;
	private int NOTE_DRAW_PADDING;
	private int notesAreaX;
	/** If the Activity is in playing mode (controls hidden) or paused mode */
	private boolean isPlayingState = false;
	/** Is being run on UI Thread when MediaPlayer is playing to follow it's progress */
	private OnPlayerPositionChanged listener = new OnPlayerPositionChanged();
	private int listenerSavedPosition = 0;
	private boolean isScaleValid = false;

	private final ErrorDialogStrategy errorDialogStrategy;
	private final InitialProgressDialogStrategy initialProgressDialogStrategy;
	private final ManagedReceiverStrategy managedReceiverStrategy;

	public PlayActivity() {
		ActivityStrategyChainRoot root = new ActivityStrategyChainRoot(this);
		errorDialogStrategy = new ErrorDialogStrategy(root);
		initialProgressDialogStrategy = new InitialProgressDialogStrategy(errorDialogStrategy);
		managedReceiverStrategy = new ManagedReceiverStrategy(initialProgressDialogStrategy);
		initMixin(managedReceiverStrategy);
	}

	@Override
	public void onDismiss(InfoDialog.InfoDialogDismissalEvent dismissalEvent) {
		mixin.onCustomEvent(dismissalEvent);
	}

	@Override
	public void onCancel(ProgressDialog.ProgressDialogCanceledEvent event) {
		mixin.onCustomEvent(event);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WorkerService.scheduleCleanOldFiles(getApplicationContext());
		setContentView(R.layout.playscreen);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		sheet = (ViewGroup) findViewById(R.id.PLAY_sheet_container);
		lines = (Sheet5LinesView) findViewById(R.id.PLAY_sheet_5lines);
		((OnInterceptTouchObservable) hscroll).setListener(new OnInterceptListener() {
			@Override
			public void onTouchIntercepted() {
				exitPlayingState();
			}
		});
		vertscroll = (ScrollView) findViewById(R.id.PLAY_vertscrollview);
		((HackedScrollViewChild) vertscroll.getChildAt(0)).setRuler(lines);
		highlightPaint = PaintBuilder.init().antialias(true)
		.color(getResources().getColor(R.color.highlightColor)).build();
		highlightShadowFactor = readParametrizedFactor(R.string.noteShadow);
		findViewById(R.id.PLAY_barbutton_play).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(enterPlayingState()) {
					resumePlaying();
				}
			}
		});
		findViewById(R.id.PLAY_barbutton_replay).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(enterPlayingState()) {
					// rewind
					listener.seek(0);
					resumePlaying();
				}
			}
		});
		findViewById(R.id.PLAY_barbutton_configure).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isPlayingState) return;
				PlayPreferencesDialog.showNew(getSupportFragmentManager(), playConf);
			}
		});
		((View) findViewById(R.id.PLAY_barbutton_loop)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setSelected(!v.isSelected());
				playConf.setLoop(v.isSelected());
			}
		});
		View mainView = ((ViewGroup) findViewById(R.id.PLAY_vertscrollview)).getChildAt(0);
		mainView.setClickable(true);
		mainView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exitPlayingState();
			}
		});
		sheet.setOnTouchListener(new NoteOrPauseOnClickListener());
		
		if(savedInstanceState != null) {
			listenerSavedPosition = savedInstanceState.getInt(INSTANCE_STATE_LISTENER_POS, 0);
			if(savedInstanceState.containsKey(INSTANCE_STATE_SCALE)) {
				sheetParams.setScale(savedInstanceState.getFloat(INSTANCE_STATE_SCALE, 1));
				isScaleValid = true;
			}
		}
		if(savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_STATE_PLAYCONF)) {
			// everything is already loaded
			ParcelableScore parcelable = savedInstanceState.getParcelable(INSTANCE_STATE_SCORE);
			ScoreVisualizationConfig conf = savedInstanceState.getParcelable(INSTANCE_STATE_VISCONF);
			PlayingConfiguration pconf = savedInstanceState.getParcelable(INSTANCE_STATE_PLAYCONF);
			onModelLoaded(parcelable.getSource(), conf, pconf);
		} else {
			// load required model objects first
			Intent startIntent = getIntent();
			long scoreId = startIntent.getLongExtra(STARTINTENT_EXTRAS_SCORE_ID, -1);
			ParcelableScore pScore = startIntent.getParcelableExtra(STARTINTENT_EXTRAS_SCORE);
			if(pScore != null) {
				this.score = pScore.getSource();
				scoreId = this.score.getId();
			} else if(scoreId == -1) {
				log.e("Activity started without score id in intent");
				showErrorDialog(R.string.errormsg_unrecoverable, null, true);
				return;
			}
			GetScoreReceiver getScoreReceiver = new GetScoreReceiver();	
			Intent requestIntent = AsyncHelper.prepareServiceIntent(
				this, 
				ContentService.class, 
				ContentService.ACTIONS.GET_SCORE_BY_ID, 
				getScoreReceiver.getCurrentRequestId(), 
				AsyncHelper.getBroadcastCallback(CALLBACK_ACTION_GET), 
				true
			);
			requestIntent.putExtra(ContentService.ACTIONS.EXTRAS_ENTITY_ID, scoreId);
			requestIntent.putExtra(ContentService.ACTIONS.EXTRAS_ATTACH_SCORE_VISUAL_CONF, true);
			requestIntent.putExtra(ContentService.ACTIONS.EXTRAS_ATTACH_SCORE_PLAY_CONF, true);
			managedReceiverStrategy.registerManagedReceiver(getScoreReceiver, CALLBACK_ACTION_GET);
	    	log.v("Sending "+CALLBACK_ACTION_GET+" for id "+scoreId);
	    	startService(requestIntent);
	    	initialProgressDialogStrategy.showProgressDialog();
		}
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		if (focusChange == AUDIOFOCUS_LOSS || focusChange == AUDIOFOCUS_LOSS_TRANSIENT) {
			exitPlayingState();
		}
	}

	@Override
	public void onDialogResult(PlayPreferencesDialog.PlayPreferencesDialogClosedEvent event) {
		PlayingConfiguration newConfiguration = event.getPlayingConfiguration();
		if (!newConfiguration.equals(playConf)) {
			playConf = newConfiguration;
			configurationUpdated = true;
		}
	}

	/**
	 * Detects clicks on Note/Pause views inside "sheet" View, 
	 * fires {@link #listener#seek(LengthSpec)}, unless isPlayingState
	 */
	private final class NoteOrPauseOnClickListener implements OnTouchListener {
		private int selectedIndex = -1;
		private Paint prevPaint;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(isPlayingState)
				return false;
			switch(event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				// check if some note was clicked
				int i = findPressedElementIndex(event);
				if(i == -1 || !(specAt(i).getType() == ElementType.NOTE
				  || specAt(i).getType() == ElementType.PAUSE)) {
					break;
				}
				selectedIndex = i;
				SheetAlignedElementView view = elementViews.get(selectedIndex);
				prevPaint = view.getPaint();
				view.setPaint(highlightPaint, NOTE_DRAW_PADDING);
				return true;
			case MotionEvent.ACTION_MOVE:
				 return selectedIndex != -1;
			case MotionEvent.ACTION_UP:
				if(selectedIndex != -1) {
					elementViews.get(selectedIndex).setPaint(prevPaint, NOTE_DRAW_PADDING);
					ElementSpec spec = specAt(selectedIndex);
					LengthSpec elem;
					switch(spec.getType()) {
					case NOTE:
						elem = ((ElementSpec.NormalNote) spec).noteSpec();
						break;
					case  PAUSE:
						elem = ((ElementSpec.Pause) spec).pauseSpec();
						break;
					default:
						throw new CodeLogicError("Not expected type of clicked element "+spec.getType());
					}
					listener.seek(elem);
					return true;
				}
				break;
			default:
				if(selectedIndex != -1) {
					elementViews.get(selectedIndex).setPaint(prevPaint, NOTE_DRAW_PADDING);
				}
			}
			selectedIndex = -1;
			return false;
		}
	}

	private class OnPlayerPositionChanged implements Runnable {
		/** Used for pull MediaPlayer position changes */
		private Handler mHandler = new Handler();
		private static final int REFRESH_DELAY = 50;
		
		private int prevPlayerPosition = 0;
		private ScoreIterator scoreIterator;
		private LengthSpec currentElement;
		/** in base units */
		private int currentElementOffset;
		/** in base units */
		private int currentElemLength;
		/** duration of minPossibleValue (in miliseconds) according to current tempo */
		private float baseUnitDuration;
		/** the configuration this listener is currently using */
		private PlayingConfiguration playConfiguration;
		
		void onConfigurationChanged(PlayingConfiguration newPlayConf) {
			float newUnitDuration = ((float) 60*1000)/((1 << (minPossibleValue - NoteConstants.LEN_QUATERNOTE)) * newPlayConf.getTempo());
			if(playConfiguration == null) {
				baseUnitDuration = newUnitDuration;
				playConfiguration = new PlayingConfiguration(newPlayConf);
			} else {
				if(currentElement != null) {
					modelToViewsMapping.get(currentElement).setPaint(normalPaint, NOTE_DRAW_PADDING);
				}
				int position = getPosition();
				int unitPassed = (int) (position/baseUnitDuration);
				unitPassed -= MidiBuilder.initialDelay(content, minPossibleValue, playConfiguration);
				if(unitPassed <= 0) {
					// if onConfigurationChanged() happened before playing first actual note/rest rewind to start (before intro)
					unitPassed = 0;
				} else {
					unitPassed += MidiBuilder.initialDelay(content, minPossibleValue, newPlayConf);
				}
				position = (int) (unitPassed * newUnitDuration);
				lazySeek = position;
				baseUnitDuration = newUnitDuration;
				playConfiguration = new PlayingConfiguration(newPlayConf);
				resetIterator();
				seek(position);
			}
		}
		
		public void run() {
			if(onPositionChanged(player.getCurrentPosition())) {
				mHandler.postDelayed(this, REFRESH_DELAY);
			}
		}
		
		private boolean onPositionChanged(int currentPosition) {
			int baseUnitPassed = (int) Math.round(currentPosition/baseUnitDuration);
			LengthSpec prevElement = currentElement;
			if(currentPosition < prevPlayerPosition || scoreIterator == null) {
				resetIterator();
			}
			prevPlayerPosition = currentPosition;
			// seek forward iterator to find element matching current position
			while(currentElementOffset + currentElemLength <= baseUnitPassed) {
				if(!scoreIterator.hasNext()) {
					log.v("No element found for position %d (%d baseUnit)", currentPosition, baseUnitPassed);
					// treat this as float precision issue of baseUnitDuration, stay at last element
					return true;
				}
				currentElementOffset += currentElemLength;
				ScoreContentElem elem = scoreIterator.next();
				if(elem instanceof PauseSpec) {
					currentElement = (LengthSpec) elem;
					currentElemLength = overallLength(currentElement, minPossibleValue);
				} else if(elem instanceof TimeSpec) {
					currentElemLength = 0;
					continue;
				} else {
					throw new UnsupportedOperationException();
				}
			}
			if(currentElement != prevElement) {
				if(prevElement != null) {
					modelToViewsMapping.get(prevElement).setPaint(normalPaint, NOTE_DRAW_PADDING);
				}
				if(currentElement != null) {
					// mark current
					modelToViewsMapping.get(currentElement).setPaint(highlightPaint, NOTE_DRAW_PADDING);
				}
			}
			// scroll to make current middle visible
			hscroll.scrollTo(computeHorizontalScroll(), 0);
			return true;
		}
		
		public int computeHorizontalScroll() {
			if(scoreIterator == null) {
				resetIterator();
			}
			if(currentElement == null) {
				return 0;
			}
			ScoreContentElem next;
			while((next = scoreIterator.previewNext()) != null && !(next instanceof PauseSpec)) {
				scoreIterator.next();
			}
			int currMiddleX = middleAbsoluteX(modelToViewsMapping.get(currentElement));
			int absX;
			if(next != null) {
				// scroll in between current and next
				float progress = (prevPlayerPosition - (currentElementOffset * baseUnitDuration))/(currentElemLength * baseUnitDuration);
				int nextMiddleX = middleAbsoluteX(modelToViewsMapping.get(next));
				absX = (int) (currMiddleX + (nextMiddleX - currMiddleX) * progress);
			}
			else {
				absX = currMiddleX;
			}
			return absX - hscroll.getWidth()/3;
		}

		private void resetIterator() {
			scoreIterator = new ScoreIterator(content);
			currentElementOffset = 0;
			currentElement = null;
			currentElemLength = MidiBuilder.initialDelay(content, minPossibleValue, playConfiguration);
		}

		/** Called when user (in pause mode) clicks some note to rewind to it */
		public void seek(LengthSpec elem) {
			SheetAlignedElementView view = modelToViewsMapping.get(elem);
			view.setPaint(highlightPaint, NOTE_DRAW_PADDING);
			if(currentElement == elem) {
				return;
			} else if(currentElement != null) {
				// remove previous note selection
				modelToViewsMapping.get(currentElement).setPaint(normalPaint, NOTE_DRAW_PADDING);
			}
			if(scoreIterator == null) {
				resetIterator();
			}
			if(!forwardIteratorTo(elem)) {
				// must rewind iterator and search from beginning
				resetIterator();
				if(!forwardIteratorTo(elem)) {
					throw new InvalidParameterException(elem + "couldn't be found in ScoreIterator");
				}
			}
			// seek MediaPlayer to current element start
			lazySeek = (int) (baseUnitDuration * currentElementOffset);
		}

		private boolean forwardIteratorTo(LengthSpec elem) {
			while(scoreIterator.hasNext()) {
				currentElementOffset += currentElemLength;
				ScoreContentElem next = scoreIterator.next();
				if(!(next instanceof LengthSpec)) {
					currentElemLength = 0;
					continue;
				}
				currentElement = (LengthSpec) next;
				currentElemLength = overallLength(currentElement, minPossibleValue);
				if(currentElement == elem) {
					return true;
				}
			}
			return false;
		}
		
		public void seek(int position) {
			onPositionChanged(position);
			lazySeek = position;
		}
		
		public void startListening() {
			mHandler.postDelayed(this, REFRESH_DELAY);
		}

		public void stopListening() {
			mHandler.removeCallbacks(this);
		}

		public int getPosition() {
			if(lazySeek != null) {
				return lazySeek;
			} else {
				return prevPlayerPosition;
			}
		}
	};

	private BuildMidiTask buildMidiTask = new BuildMidiTask();
	
	private class BuildMidiTask extends AsyncTask<Void, Void, File> {
		protected File doInBackground(Void... params) {
			try {
				MidiFile file = MidiBuilder.build(content, minPossibleValue, playConf);
				File tempFile = TemporaryFilesFactory.getUniqueName(PlayActivity.this, ".mid");
				FileOutputStream out = new FileOutputStream(tempFile);
				file.writeTo(new FileOutputWrapper(out));
				out.close();
				return tempFile;
			} catch (MidiFormatException e) {
				log.e("Failed to create midi file", e);
				errorDialogStrategy.showErrorDialogOnUiThread(R.string.errormsg_unrecoverable, e, true);
			} catch (IOException e) {
				log.e("Failed to create midi file", e);
				errorDialogStrategy.showErrorDialogOnUiThread(R.string.errormsg_exception_try_later, e, true);
			}
			return null;
		}
		
		protected void onPostExecute(File result) {
			if(!isCancelled()) {
				midiLocalFile = result;
				resumePlaying();
			}
		}
	};
	
	private void resumePlaying() {
		if(configurationUpdated) {
			configurationUpdated = false;
			listener.onConfigurationChanged(playConf);
			// invalidate previously generated midi file
			buildMidiTask.cancel(true);
			buildMidiTask = new BuildMidiTask();
			midiLocalFile = null;
			clearPlayerInstance();
		}
		if(midiLocalFile != null) {
			play(midiLocalFile);
		} else if(buildMidiTask.getStatus() == AsyncTask.Status.RUNNING && !buildMidiTask.isCancelled()) {
			// wait until task is done, it will call resumePlaying() then
		} else {
			buildMidiTask.execute();
		}
	}
	
	private void play(File midiFile) {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int focusRequestResult = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		if (focusRequestResult != AUDIOFOCUS_REQUEST_GRANTED) {
			log.i("requestAudioFocus() failed");
			return;
		}
		if(player == null) {
			player = MediaPlayer.create(this, Uri.fromFile(midiFile));
			if(player == null) {
				showErrorDialog(R.string.errormsg_exception_try_later, null, true);
				return;
			}
			player.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					String errorLabel = ReflectionUtils.findConstName(MediaPlayer.class, "MEDIA_ERROR", what, null);
					if(errorLabel == null && !playerIsPlaying) {
						/*
						 * A workaround for issue with calling player.pause() at beginning.
						 * It causes onError(..., -3, 0) but music keeps playing.
						 * We stop it and force to recreate the Player.
						 */
						log.d("Received strange MediaPlayer.onError(, %d, %d) when !playerIsPlaying", what, extra);
						try {
							player.stop();
							player.release();
							player = null;
						} catch (IllegalStateException e) {
						}
						return true;
					} else {
						log.w("MediaPlayer.onError(, %s, %d)", errorLabel, extra);
						showErrorDialog(R.string.PLAY_errormsg_playingfailed_tryagain, null, false);
						listener.stopListening();
						playerIsPlaying = false;
						try {
							player.release();
						} catch (Exception e) {
						}
						player = null;
						return false;
					}
				}
			});
			player.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					playerIsPlaying = false;
					listener.stopListening();
					lazySeek = 0;
					exitPlayingState();
				}
			});
		}
		player.setLooping(playConf.isLoop());
		playerIsPlaying = true;
		if(lazySeek != null) {
			player.seekTo(lazySeek);
			lazySeek = null;
		}
		player.start();
		listener.startListening();
	}
	
	/**
	 * If player is playing ({@link #playerIsPlaying}) then pause it or cancel task that was preparing MIDI file
	 */
	private void pausePlaying() {
		if(buildMidiTask.getStatus() == Status.RUNNING) {
			buildMidiTask.cancel(true);
		}
		if(player != null && playerIsPlaying) {
			playerIsPlaying = false;
			player.pause();
			listener.stopListening();
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audioManager.abandonAudioFocus(this);
		}
	}
	
	@Override
	protected void onPause() {
		exitPlayingState();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setUiHidden(false);
	}
	
	@Override
	protected void onStop() {
		clearPlayerInstance();
		/** save player's current position in case of onStart() and "play" command */
		lazySeek = listener.getPosition();
		if(playConf != null) {
			// save configuration for feature use
			Intent request = AsyncHelper.prepareServiceIntent(
				this, ContentService.class, 
				ContentService.ACTIONS.SAVE_PLAY_CONF, 
				null, null, false);
			request.putExtra(ContentService.ACTIONS.EXTRAS_ENTITY_ID, score.getId());
			request.putExtra(ContentService.ACTIONS.EXTRAS_SCORE_PLAY_CONF, playConf);
			log.v("Sending request SAVE_PLAY_CONF of Score#%d", score.getId());
			startService(request);
		}
		
		super.onStop();
	}

	private void clearPlayerInstance() {
		if(player != null) {
			player.setOnErrorListener(null);
			player.stop();
			player.release();
			player = null;
		}
	}
		
	private class GetScoreReceiver extends ManagedReceiverStrategy.SingleManagedReceiver {

		@Override
		protected void onFailureReceived(Intent response) {
			log.e("Failed to get score: " + AsyncHelper.getError(response));
			initialProgressDialogStrategy.hideProgressDialog();
			showErrorDialog(R.string.errormsg_unrecoverable, null, true);
		}
		
		@Override
		protected void onSuccessReceived(Intent response) {
			initialProgressDialogStrategy.hideProgressDialog();
			ParcelableScore parcelable = response.getParcelableExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_ENTITY);
			ScoreVisualizationConfig config = response.getParcelableExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_VISUAL_CONF);
			PlayingConfiguration playConf = response.getParcelableExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_PLAY_CONF);
			if(playConf == null) {
				playConf = new PlayingConfiguration(
					getResources().getInteger(R.integer.defaultPlayTempoBPM),
					false,
					false,
					false
				);
			}
			// take into account that Score object may have already been provided by in STARTINTENT_EXTRAS_SCORE
			onModelLoaded(ifNotNull(score, parcelable.getSource()), config, playConf);
		}
	}

	public void onModelLoaded(Score score, ScoreVisualizationConfig config, PlayingConfiguration playConf) {
		this.score = score;
		this.visualConf = config;
		this.playConf = playConf;
		try {
			 content = score.getContent();
		} catch (SerializationException e) {
			log.e("Failed to create midi file", e);
			showErrorDialog(R.string.errormsg_unrecoverable, e, true);
			return;
		}
		listener.onConfigurationChanged(playConf);
		try {
			fillUpWithPauses(content, minPossibleValue);
			createViews(content);
			int lastEl = elementViews.size()-1;
			buildNoteGroups(0, lastEl, sheet, normalPaint, 0);
			buildJoinArcs(0, lastEl, sheet, normalPaint, 0);
			ViewUtils.addActivityOnLayout(this, this);
			String scoreTitle = score.getTitle();
			if(scoreTitle == null) {
				scoreTitle = getString(android.R.string.untitled);
			}
			((TextView) findViewById(R.id.title)).setText(getString(R.string.PLAY_title, scoreTitle));
			sheet.requestLayout();			
		} catch (CreationException e) {
			log.e("Exception while creating drawing model", e);
			showErrorDialog(R.string.errormsg_unrecoverable, e, true);
			return;
		}
		// setup UI according to loaded model
		findViewById(R.id.PLAY_barbutton_loop).setSelected(playConf.isLoop());
	}
	
	private InterceptsScaleGesture.OnScaleListener scaleListener = new InterceptsScaleGesture.OnScaleListener() {
		@Override
		public void onScale(float scaleFactor, PointF focusPoint) {
			float oldScale = sheetParams.getScale();
			float newScale = oldScale*scaleFactor;
			sheetParams.setScale(newScale);
			if(sheetParams.getLineThickness() < 1) {
				sheetParams.setScale(oldScale);
				return;
			}
			int fpNewRelX = (int) ((visible2absX((int) focusPoint.x)-notesAreaX)*scaleFactor);
			int line0NewVisibleY = (int) (focusPoint.y + (abs2visibleY(line0Top()) - focusPoint.y)*scaleFactor);
			onScaleChanged();
			hscroll.scrollTo((int) (fpNewRelX+notesAreaX-focusPoint.x), 0);
			fixLine0VisibleY(line0NewVisibleY);
			isScaleValid = true;			
		}
		
		private int abs2visibleY(int absoluteY) {
			return absoluteY + sheet.getTop() - vertscroll.getScrollY();
		}
		
		private int visible2absX(int visibleX) {
			return visibleX + hscroll.getScrollX();
		}

		@Override
		public void onScaleBegin() {
		}

		@Override
		public void onScaleEnd() {
		}
	};

	private PositioningEnv positioningEnv;

	private void fixLine0VisibleY(int visY) {
		((HackedScrollViewChild) vertscroll.getChildAt(0)).fixRulerVisibleY(visY - lines.getPaddingTop());
	}
	
	/** fired when model was loaded, views created so now we need to position them. */
	@Override
	public void onLayoutPassed() {
		int visibleHeight = hscroll.getHeight();
		sheet.setVisibility(View.INVISIBLE);
		findViewById(R.id.PLAY_barAnimator).setVisibility(View.INVISIBLE);
		
		((InterceptsScaleGesture) findViewById(R.id.PLAY_scaleInterceptor)).setOnScaleListener(scaleListener);
		if(!isScaleValid) {
			// try to fit every View into available height
			sheetParams.setScale(1);
			int minTop = Integer.MAX_VALUE, maxBottom = Integer.MIN_VALUE;
			for (SheetAlignedElementView view : elementViews) {
				view.setSheetParams(sheetParams);
				int top = view.model().getOffsetToAnchor(LINE0_ABSINDEX, AnchorPart.TOP_EDGE)
				- view.getPaddingTop();
				int bottom = top + view.measureHeight();
				minTop = Math.min(minTop, top);
				maxBottom = Math.max(maxBottom, bottom);
			}
			sheetParams.setScale(visibleHeight / (float) (maxBottom - minTop));
			// make sure that line is at least 1 pixel thick
			if(sheetParams.getLineThickness() < 1) {
				sheetParams.setScale(1f / sheetParams.getLineFactor());
			}
			int max = getResources().getDimensionPixelSize(R.dimen.playscreen_lines_maxInitialHeight);
			int basicLinesHeight = sheetParams.anchorOffset(LINE4_ABSINDEX, AnchorPart.BOTTOM_EDGE);
			if(basicLinesHeight > max) {
				sheetParams.setScale(sheetParams.getScale() * max / basicLinesHeight);
			}
		}
		onScaleChanged();
		int linesHalf = sheetParams.anchorOffset(NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE), AnchorPart.MIDDLE);
		fixLine0VisibleY((visibleHeight/2) - linesHalf);
		listener.seek(listenerSavedPosition);
		((LazyScrolling) hscroll).postLayoutScrollTo(listener.computeHorizontalScroll(), 0);
		ViewUtils.addActivityOnLayout(this, new OnLayoutListener() {
			@Override
			public void onLayoutPassed() {
				sheet.setVisibility(View.VISIBLE);
				findViewById(R.id.PLAY_barAnimator).setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	protected void onScaleChanged() {
		super.onScaleChanged();
		float highlightShadow = highlightShadowFactor*sheetParams.getScale();
		highlightPaint.setShadowLayer(highlightShadow, highlightShadow/2, highlightShadow, Color.BLACK);		
		NOTE_DRAW_PADDING = (int) Math.floor(2*highlightShadow);
		lines.setParams(sheetParams, 0, 0);
		lines.setPaddingTop(sheetParams.getLinespacingThickness());
		lines.setPaddingBottom(sheetParams.getLinespacingThickness());
		if(positioningEnv == null) {
			positioningEnv = new PositioningEnv() {
				@Override
				public void notesAreaPaddingLeftChanged(int paddingLeft) {
					lines.setPaddingLeft(paddingLeft);
					notesAreaX = paddingLeft;
				}
				@Override
				public int middleX(int index) {
					SheetAlignedElementView v = elementViews.get(index);
					return ScoreHelper.middleX(v);
				}
				@Override
				public void preVisit(int index) {
					elementViews.get(index).updateDrawRadius(NOTE_DRAW_PADDING);
				}			
				@Override
				public void onPositionChanged(int index, int x) {
					SheetElementView<?> v = elementViews.get(index);
					int ypos = sheetElementY(v);
					updatePosition(
						v, 
						x,
						ypos
					);
				}			
			};
		}		
		for(int i = 0; i < overlaysViews.size(); i++) {
			overlaysViews.get(i).updateDrawRadius(NOTE_DRAW_PADDING);
		}
		positioningStrategy.calculatePositions(positioningEnv, spacingEnv, sheetParams, true);
		correctSheetWidth();
	}
	
	/**
	 * Resize sheet accordingly to last element position.
	 */
	private void correctSheetWidth() {
		int lastElIndex = elementViews.size()-1;
		SheetAlignedElementView lastView = elementViews.get(lastElIndex);
		int hPadding = (int) (positioningStrategy.getNotesAreaHorizontalPaddingFactor() * sheetParams.getScale());
		lines.setPaddingRight(hPadding + middleX(lastView));
		int linesWidth = left(lastView) + lastView.measureWidth() + hPadding;
		int newSheetWidth = Math.max(linesWidth, 
				hscroll.getWidth());
		updateSize(sheet, newSheetWidth, null);
		updateSize(lines, linesWidth, null);
	}

	/** create views for Score elements */
	private void createViews(final List<ScoreContentElem> content)
			throws CreationException {
		TimeSpec prevTime = null;
		for (Iterator<ScoreContentElem> iterator = content.iterator(); iterator.hasNext();) {
			ScoreContentElem elem = iterator.next();
			ElementSpec spec;
			if(elem instanceof NoteSpec) {
				spec = elementSpecNN((NoteSpec) elem, visualConf.getDisplayMode());
			} else if(elem instanceof PauseSpec) {
				spec = new ElementSpec.Pause((PauseSpec) elem);
			} else if(elem instanceof TimeSpec) {
				if(!iterator.hasNext() && prevTime != null) {
					// skip last bar definition when it has no content, unless it's also the first one
					break;
				}
				TimeSpec currTime = (TimeSpec) elem;
				spec = new ElementSpec.TimeDivider(prevTime, currTime);
				prevTime = currTime;
			} else {
				throw new UnsupportedOperationException();
			}
			SheetAlignedElement drawingModel = DrawingModelFactory.createDrawingModel(this, spec);
			SheetAlignedElementView view = addElementView(drawingModel);
			modelToViewsMapping.put(elem, view);
		}
		ElementSpec.TimeDivider closingTimeDivider = new ElementSpec.TimeDivider(prevTime, null);
		addElementView(DrawingModelFactory.createDrawingModel(this, closingTimeDivider));
	}

	private SheetAlignedElementView addElementView(SheetAlignedElement drawingModel) {
		SheetAlignedElementView elementView = new SheetAlignedElementView(this, drawingModel);
		elementView.setPaint(normalPaint, NOTE_DRAW_PADDING);
		elementView.setSheetParams(sheetParams);
		elementViews.add(elementView);
		sheet.addView(elementView);
		return elementView;
	}

	/** fill empty space in bars (except last one) with rests */
	private static void fillUpWithPauses(final List<ScoreContentElem> content, int baseUnit) throws CreationException {
		int capacityLeft = 0;
		TimeStep currentTS = null;
		for(int i = 0; i < content.size(); i++) {
			ScoreContentElem elem = content.get(i);
			if(elem instanceof LengthSpec) {
				capacityLeft -= ElementSpec.overallLength((LengthSpec) elem, baseUnit);
			} else if(elem instanceof TimeSpec) {
				if(currentTS != null && capacityLeft > 0) { 
					// fill empty space in previous bar with rests
					InsertDivided strategy = new InsertDivided() {
						@Override
						protected void handle(int atIndex, int baseLength, int dotExt) throws CreationException {
							PauseSpec pause = new PauseSpec(baseLength, dotExt);
							content.add(atIndex, pause);
						}
					};
					strategy.insertDivided(i, capacityLeft, true, baseUnit);
					i += strategy.getTotal();
				}
				TimeSpec time = (TimeSpec) elem;
				currentTS = ifNotNull(time.getTimeStep(), currentTS);
				capacityLeft = timeCapacity(currentTS, baseUnit);
			} else {
				throw new UnsupportedOperationException();
			}
		}
	}	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(playConf != null) {
			try {
				outState.putParcelable(INSTANCE_STATE_SCORE, score.prepareParcelable());
				outState.putParcelable(INSTANCE_STATE_VISCONF, visualConf);
				outState.putParcelable(INSTANCE_STATE_PLAYCONF, playConf);
				outState.putInt(INSTANCE_STATE_LISTENER_POS, listener.getPosition());
			} catch (SerializationException e) {
				// this shouldn't happen because we didn't change anything
				throw new RuntimeException(e);
			}
		}
		if(isScaleValid) {
			outState.putFloat(INSTANCE_STATE_SCALE, sheetParams.getScale());
		}
	}
	
	private void setUiHidden(boolean hide) {
		setAnimatorContentHidden(R.id.PLAY_barAnimator, R.id.PLAY_actionsBar, hide);
	}
	
	private void setAnimatorContentHidden(int animatorId, int contentId, boolean b) {
		View cotent = findViewById(contentId);
		int i = 0;
		ViewAnimator animator = (ViewAnimator) findViewById(animatorId);
		for(; i < animator.getChildCount(); i++) {
			if(animator.getChildAt(i) == cotent)
				break;
		}
		animator.setDisplayedChild(b ? i+1 : i);
	}

	/**
	 * If not in playing state ({@link #isPlayingState}), enter it by hiding toolbar and setting {@link #isPlayingState} to true.
	 * @return whether change happened (so return false if already in playing state)
	 */
	private boolean enterPlayingState() {
		if(!isPlayingState) {
			setUiHidden(true);
			isPlayingState = true;
			hscroll.setKeepScreenOn(true);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * If {@link #isPlayingState}, set it to false, show toolbar and call {@link #pausePlaying()}
	 */
	private void exitPlayingState() {
		if(isPlayingState) {
			setUiHidden(false);
			isPlayingState = false;
			hscroll.setKeepScreenOn(false);
			pausePlaying();
		}
	}

	private void showErrorDialog(int messageStringId, Throwable e, boolean lazyFinish) {
		errorDialogStrategy.showErrorDialog(messageStringId, e, lazyFinish);
	}

}
