/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the PickaPack library.
 *
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.util.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

/**
 *
 * @author Min Cai
 */
public class IterableBigTextFile implements Iterable<String> {
    private BufferedReader reader;

    /**
     *
     * @param reader
     */
    public IterableBigTextFile(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    /**
     *
     */
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterator<String> iterator() {
        return new FileIterator();
    }

    private class FileIterator implements Iterator<String> {
        private String currentLine;

        public boolean hasNext() {
            try {
                currentLine = reader.readLine();
            } catch (Exception ex) {
                currentLine = null;
                ex.printStackTrace();
            }

            return currentLine != null;
        }

        public String next() {
            return currentLine;
        }

        public void remove() {
        }
    }
}
