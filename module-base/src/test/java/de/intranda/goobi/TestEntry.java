/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

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
        assertEquals(new EntryAuthor("Frank Alkatiri").getFullName(), authors.get(1).getFullName());
        assertEquals(new EntryAuthor("Tom Albert Nguyen").getFullName(), authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorSplitSemicolonPica() {
        Entry entry = new Entry("", "Maik Folger; Frank Alkatiri; Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Folger, Maik").getFullName(), authors.get(0).getPicaName());
        assertEquals(new EntryAuthor("Alkatiri, Frank").getFullName(), authors.get(1).getPicaName());
        assertEquals(new EntryAuthor("Nguyen, Tom Albert").getFullName(), authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorSplitSemicolonColon() {
        Entry entry = new Entry("", "Folger, M.;Alkatiri, F.;Nguyen, T.A.", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Folger, M.").getFullName(), authors.get(0).getFullName());
        assertEquals(new EntryAuthor("Alkatiri, F.").getFullName(), authors.get(1).getFullName());
        assertEquals(new EntryAuthor("Nguyen, T.A.").getFullName(), authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorSplitSemicolonColonPica() {
        Entry entry = new Entry("", "Folger, M.;Alkatiri, F.;Nguyen, T.A.", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Folger, M.").getFullName(), authors.get(0).getPicaName());
        assertEquals(new EntryAuthor("Alkatiri, F.").getFullName(), authors.get(1).getPicaName());
        assertEquals(new EntryAuthor("Nguyen, T.A.").getFullName(), authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorSplitColon() {
        Entry entry = new Entry("", "Maik Folger, Frank Alkatiri, Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Maik Folger").getFullName(), authors.get(0).getFullName());
        assertEquals(new EntryAuthor("Frank Alkatiri").getFullName(), authors.get(1).getFullName());
        assertEquals(new EntryAuthor("Tom Albert Nguyen").getFullName(), authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorSplitColonPica() {
        Entry entry = new Entry("", "Maik Folger, Frank Alkatiri, Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Folger, Maik").getFullName(), authors.get(0).getPicaName());
        assertEquals(new EntryAuthor("Alkatiri, Frank").getFullName(), authors.get(1).getPicaName());
        assertEquals(new EntryAuthor("Nguyen, Tom Albert").getFullName(), authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorSplitAnd() {
        Entry entry = new Entry("", "Maik Folger and Frank Alkatiri and Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Maik Folger").getFullName(), authors.get(0).getFullName());
        assertEquals(new EntryAuthor("Frank Alkatiri").getFullName(), authors.get(1).getFullName());
        assertEquals(new EntryAuthor("Tom Albert Nguyen").getFullName(), authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorSplitAndPica() {
        Entry entry = new Entry("", "Maik Folger and Frank Alkatiri and Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Folger, Maik").getFullName(), authors.get(0).getPicaName());
        assertEquals(new EntryAuthor("Alkatiri, Frank").getFullName(), authors.get(1).getPicaName());
        assertEquals(new EntryAuthor("Nguyen, Tom Albert").getFullName(), authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorSplitAndColon() {
        Entry entry = new Entry("", "Folger, M and Alkatiri, F and Nguyen T.A.", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Folger, M").getFullName(), authors.get(0).getFullName());
        assertEquals(new EntryAuthor("Alkatiri, F").getFullName(), authors.get(1).getFullName());
        assertEquals(new EntryAuthor("Nguyen T.A.").getFullName(), authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorSplitAndColonPica() {
        Entry entry = new Entry("", "Folger, M and Alkatiri, F and Nguyen T.A.", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Folger, M").getFullName(), authors.get(0).getPicaName());
        assertEquals(new EntryAuthor("Alkatiri, F").getFullName(), authors.get(1).getPicaName());
        assertEquals(new EntryAuthor("Nguyen, T.A.").getFullName(), authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorSplitComma() {
        Entry entry = new Entry("", "M. Folger, F. Alkatiri, T.A. Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("M. Folger").getFullName(), authors.get(0).getFullName());
        assertEquals(new EntryAuthor("F. Alkatiri").getFullName(), authors.get(1).getFullName());
        assertEquals(new EntryAuthor("T.A. Nguyen").getFullName(), authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorSplitCommaPica() {
        Entry entry = new Entry("", "M. Folger, F. Alkatiri, T.A. Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals(new EntryAuthor("Folger, M.").getFullName(), authors.get(0).getPicaName());
        assertEquals(new EntryAuthor("Alkatiri, F.").getFullName(), authors.get(1).getPicaName());
        assertEquals(new EntryAuthor("Nguyen, T.A.").getFullName(), authors.get(2).getPicaName());
    }
}
