/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.util.io.appender;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class JTextPaneOutputAppender implements OutputAppender {
    private StyledDocument doc;
    private Style styleCurrentCycle;
    private Style styleStdOut;
    private Style styleStdErr;

    public JTextPaneOutputAppender(JTextPane textPane) {
        this.doc = (StyledDocument) textPane.getDocument();

        this.styleCurrentCycle = this.doc.addStyle("CurrentCycle", null);
        StyleConstants.setForeground(this.styleCurrentCycle, Color.BLACK);
        StyleConstants.setBold(this.styleCurrentCycle, true);

        this.styleStdOut = this.doc.addStyle("StdOut", null);
        StyleConstants.setForeground(this.styleStdOut, Color.BLACK);

        this.styleStdErr = this.doc.addStyle("StdErr", null);
        StyleConstants.setForeground(this.styleStdErr, Color.RED);
    }

    public void appendStdOutLine(final long currentCycle, final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    doc.insertString(doc.getLength(), "[" + currentCycle + "]", styleCurrentCycle);
                    doc.insertString(doc.getLength(), " " + text + '\n', styleStdOut);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void appendStdErrLine(final long currentCycle, final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    doc.insertString(doc.getLength(), "[" + currentCycle + "]", styleCurrentCycle);
                    doc.insertString(doc.getLength(), " " + text + '\n', styleStdErr);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
