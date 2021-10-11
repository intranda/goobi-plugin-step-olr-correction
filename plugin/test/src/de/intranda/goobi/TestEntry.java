package de.intranda.goobi;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.intranda.goobi.plugins.Box;
import de.intranda.goobi.plugins.Entry;
import de.intranda.goobi.plugins.EntryAuthor;

public class TestEntry {

    @Test
    public void TestAuthorSplitSemicolon() {
        Entry entry = new Entry("", "Maik Folger; Frank Alkatiri; Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Maik Folger").getFullName(), authors.get(0).getFullName());
    }

    @Test
    public void TestAuthorSplitSemicolonColon() {
        Entry entry = new Entry("", "Folger, M.;Alkatiri, F.;Nguyen, T.A.", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Folger, M.").getFullName(), authors.get(0).getFullName());
    }

    @Test
    public void TestAuthorSplitColon() {
        Entry entry = new Entry("", "Maik Folger, Frank Alkatiri, Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Maik Folger").getFullName(), authors.get(0).getFullName());
    }

    @Test
    public void TestAuthorSplitAnd() {
        Entry entry = new Entry("", "Maik Folger and Frank Alkatiri and Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Maik Folger").getFullName(), authors.get(0).getFullName());
    }

    @Test
    public void TestAuthorSplitAndColon() {
        Entry entry = new Entry("", "Folger, M and Alkatiri, F and Nguyen T.A.", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Folger, M").getFullName(), authors.get(0).getFullName());
    }
}
