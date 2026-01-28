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
        assertEquals("Maik Folger", authors.get(0).getFullName());
        assertEquals("Frank Alkatiri", authors.get(1).getFullName());
        assertEquals("Tom Albert Nguyen", authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorSplitSemicolonPica() {
        Entry entry = new Entry("", "Maik Folger; Frank Alkatiri; Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Folger, Maik", authors.get(0).getPicaName());
        assertEquals("Alkatiri, Frank", authors.get(1).getPicaName());
        assertEquals("Nguyen, Tom Albert", authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorSplitSemicolonColon() {
        Entry entry = new Entry("", "Folger, M.;Alkatiri, F.;Nguyen, T.A.", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Folger, M.", authors.get(0).getFullName());
        assertEquals("Alkatiri, F.", authors.get(1).getFullName());
        assertEquals("Nguyen, T. A.", authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorSplitSemicolonColonPica() {
        Entry entry = new Entry("", "Folger, M.;Alkatiri, F.;Nguyen, T.A.", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Folger, M.", authors.get(0).getPicaName());
        assertEquals("Alkatiri, F.", authors.get(1).getPicaName());
        assertEquals("Nguyen, T. A.", authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorSplitColon() {
        Entry entry = new Entry("", "Maik Folger, Frank Alkatiri, Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Maik Folger", authors.get(0).getFullName());
        assertEquals("Frank Alkatiri", authors.get(1).getFullName());
        assertEquals("Tom Albert Nguyen", authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorSplitColonPica() {
        Entry entry = new Entry("", "Maik Folger, Frank Alkatiri, Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Folger, Maik", authors.get(0).getPicaName());
        assertEquals("Alkatiri, Frank", authors.get(1).getPicaName());
        assertEquals("Nguyen, Tom Albert", authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorSplitAnd() {
        Entry entry = new Entry("", "Maik Folger and Frank Alkatiri and Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Maik Folger", authors.get(0).getFullName());
        assertEquals("Frank Alkatiri", authors.get(1).getFullName());
        assertEquals("Tom Albert Nguyen", authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorSplitAndPica() {
        Entry entry = new Entry("", "Maik Folger and Frank Alkatiri and Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Folger, Maik", authors.get(0).getPicaName());
        assertEquals("Alkatiri, Frank", authors.get(1).getPicaName());
        assertEquals("Nguyen, Tom Albert", authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorSplitAndColon() {
        Entry entry = new Entry("", "Folger, M and Alkatiri, F and Nguyen T.A.", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Folger, M", authors.get(0).getFullName());
        assertEquals("Alkatiri, F", authors.get(1).getFullName());
        assertEquals("Nguyen T. A.", authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorSplitAndColonPica() {
        Entry entry = new Entry("", "Folger, M and Alkatiri, F and Nguyen T.A.", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Folger, M", authors.get(0).getPicaName());
        assertEquals("Alkatiri, F", authors.get(1).getPicaName());
        assertEquals("Nguyen, T. A.", authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorSplitComma() {
        Entry entry = new Entry("", "M. Folger, F. Alkatiri, T.A. Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("M. Folger", authors.get(0).getFullName());
        assertEquals("F. Alkatiri", authors.get(1).getFullName());
        assertEquals("T. A. Nguyen", authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorSplitCommaPica() {
        Entry entry = new Entry("", "M. Folger, F. Alkatiri, T.A. Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Folger, M.", authors.get(0).getPicaName());
        assertEquals("Alkatiri, F.", authors.get(1).getPicaName());
        assertEquals("Nguyen, T. A.", authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorAbbreviation() {
        Entry entry = new Entry("", "M. F., F. A., T.A. N.", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("M. F.", authors.get(0).getFullName());
        assertEquals("F. A.", authors.get(1).getFullName());
        assertEquals("T. A. N.", authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorAbbreviationPica() {
        Entry entry = new Entry("", "M. F., F. A., T.A. N.", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("F., M.", authors.get(0).getPicaName());
        assertEquals("A., F.", authors.get(1).getPicaName());
        assertEquals("N., T. A.", authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorAbbreviationNoWhitespace() {
        Entry entry = new Entry("", "M.F., F.A., T.A.N.", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("M. F.", authors.get(0).getFullName());
        assertEquals("F. A.", authors.get(1).getFullName());
        assertEquals("T. A. N.", authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorAbbreviationPicaNoWhitespace() {
        Entry entry = new Entry("", "M.F., F.A., T.A.N.", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("F., M.", authors.get(0).getPicaName());
        assertEquals("A., F.", authors.get(1).getPicaName());
        assertEquals("N., T. A.", authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorMixed() {
        Entry entry = new Entry("", "M. Folger, F. Alkatiri and T.A. Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("M. Folger", authors.get(0).getFullName());
        assertEquals("F. Alkatiri", authors.get(1).getFullName());
        assertEquals("T. A. Nguyen", authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorMixedPica() {
        Entry entry = new Entry("", "M. Folger, F. Alkatiri and T.A. Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Folger, M.", authors.get(0).getPicaName());
        assertEquals("Alkatiri, F.", authors.get(1).getPicaName());
        assertEquals("Nguyen, T. A.", authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorMixedInitials() {
        Entry entry = new Entry("", "Maik M. Folger, F. Frank Alkatiri, Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Maik M. Folger", authors.get(0).getFullName());
        assertEquals("F. Frank Alkatiri", authors.get(1).getFullName());
        assertEquals("Tom Albert Nguyen", authors.get(2).getFullName());
    }

    @Test
    public void TestAuthorMixedInitialsPica() {
        Entry entry = new Entry("", "Maik M. Folger, F. Frank Alkatiri, Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Folger, Maik M.", authors.get(0).getPicaName());
        assertEquals("Alkatiri, F. Frank", authors.get(1).getPicaName());
        assertEquals("Nguyen, Tom Albert", authors.get(2).getPicaName());
    }

    @Test
    public void TestAuthorShortList() {
        Entry entry = new Entry("", "Maik Folger, Tom Albert Nguyen", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Maik Folger", authors.get(0).getFullName());
        assertEquals("Tom Albert Nguyen", authors.get(1).getFullName());
        assertEquals("Folger, Maik", authors.get(0).getPicaName());
        assertEquals("Nguyen, Tom Albert", authors.get(1).getPicaName());
    }

    @Test
    public void TestAuthorShortList2() {
        Entry entry = new Entry("", "Tom Albert Nguyen, Maik Folger", "", "", new ArrayList<Box>(), false);
        List<EntryAuthor> authors = entry.getAuthorList();
        assertEquals("Tom Albert Nguyen", authors.get(0).getFullName());
        assertEquals("Maik Folger", authors.get(1).getFullName());
        assertEquals("Nguyen, Tom Albert", authors.get(0).getPicaName());
        assertEquals("Folger, Maik", authors.get(1).getPicaName());
    }

}
