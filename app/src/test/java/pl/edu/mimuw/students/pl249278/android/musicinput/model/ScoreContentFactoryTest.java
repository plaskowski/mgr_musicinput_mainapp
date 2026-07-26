package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.Test;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.Clef;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.KeySignature;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.NoteModifier;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.AdditionalMark;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;

public class ScoreContentFactoryTest {
    @Test
    public void serializeAndDeserialize_preservesScoreElements() throws Exception {
        List<ScoreContentElem> content = Arrays.asList(
                new TimeSpec(
                        new TimeStep(3, NoteConstants.LEN_EIGHTNOTE),
                        Clef.VIOLIN,
                        KeySignature.D_DUR,
                        EnumSet.of(AdditionalMark.BEGIN_REPEAT)),
                new NoteSpec(
                        NoteConstants.LEN_QUATERNOTE,
                        1,
                        4,
                        NoteModifier.SHARP,
                        true,
                        false),
                new PauseSpec(NoteConstants.LEN_HALFNOTE, 2));

        List<ScoreContentElem> restored = ScoreContentFactory.deserialize(
                ScoreContentFactory.serialize(content));

        assertEquals(3, restored.size());

        TimeSpec restoredTime = (TimeSpec) restored.get(0);
        assertEquals(3, restoredTime.getTimeStep().getBaseMultiplier());
        assertEquals(NoteConstants.LEN_EIGHTNOTE.intValue(), restoredTime.getTimeStep().getTempoBaseLength());
        assertEquals(Clef.VIOLIN, restoredTime.getClef());
        assertEquals(KeySignature.D_DUR, restoredTime.getKeySignature());
        assertTrue(restoredTime.hasMark(AdditionalMark.BEGIN_REPEAT));

        NoteSpec restoredNote = (NoteSpec) restored.get(1);
        assertEquals(NoteConstants.LEN_QUATERNOTE, restoredNote.length());
        assertEquals(1, restoredNote.dotExtension());
        assertEquals(4, restoredNote.positon());
        assertEquals(NoteModifier.SHARP, restoredNote.getToneModifier());
        assertTrue(restoredNote.hasJoinArc());

        PauseSpec restoredPause = (PauseSpec) restored.get(2);
        assertEquals(NoteConstants.LEN_HALFNOTE, restoredPause.length());
        assertEquals(2, restoredPause.dotExtension());
    }
}
