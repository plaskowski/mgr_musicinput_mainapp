package pl.edu.mimuw.students.pl249278.android.musicinput;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.SystemClock;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.Clef;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.KeySignature;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreContentFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfigFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.ScoreRepository;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LinearLayout_ExtendedBackground;

@RunWith(AndroidJUnit4.class)
public class MainActivitySmokeTest {
    private static final String SMOKE_TITLE = "Smoke Score";

    private Context targetContext;

    @Before
    public void setUp() throws Exception {
        targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ScoreRepository.clearStorage(targetContext);
        seedSmokeScore();
    }

    @After
    public void tearDown() {
        ScoreRepository.clearStorage(targetContext);
    }

    @Test
    public void mainScreen_opensSeededScoreInPlayMode() {
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            waitForDisplayed(withText(SMOKE_TITLE));

            onView(withId(R.id.MAIN_msg_on_empty)).check(matches(not(isDisplayed())));
            onView(withText(SMOKE_TITLE)).check(matches(isDisplayed()));

            onView(allOf(
                isAssignableFrom(LinearLayout_ExtendedBackground.class),
                hasDescendant(withText(SMOKE_TITLE))
            )).perform(click());

            waitForDisplayed(withId(R.id.button_play));
            onView(withId(R.id.button_edit)).check(matches(isDisplayed()));
            onView(withId(R.id.button_rename)).check(matches(isDisplayed()));

            onView(withId(R.id.button_play)).perform(click());

            waitForDisplayed(withId(R.id.PLAY_scaleInterceptor));
        }
    }

    private void seedSmokeScore() throws Exception {
        Score score = new Score(
            SMOKE_TITLE,
            ScoreContentFactory.initialContent(Clef.VIOLIN, KeySignature.C_DUR, TimeStep.commonTime)
        );
        try (ScoreRepository repository = new ScoreRepository(targetContext)) {
            long id = repository.insertScore(
                score,
                ScoreVisualizationConfigFactory.createWithDefaults(targetContext)
            );
            assertTrue(id != Score.NO_ID);
        }
    }

    private static void waitForDisplayed(Matcher<View> matcher) {
        AssertionError lastAssertion = null;
        NoMatchingViewException lastNoMatch = null;
        long deadline = SystemClock.uptimeMillis() + 10000;
        do {
            try {
                onView(matcher).check(matches(isDisplayed()));
                return;
            } catch (AssertionError e) {
                lastAssertion = e;
            } catch (NoMatchingViewException e) {
                lastNoMatch = e;
            }
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() < deadline);

        if(lastAssertion != null) {
            throw lastAssertion;
        }
        throw lastNoMatch;
    }
}
