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
package archimulator.util.trace;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.function.Consumer;

/**
 * Trace tester.
 *
 * @author Min Cai
 * @param <TraceRecordT>
 */
public abstract class TraceTester<TraceRecordT> {
    private String fileName;

    /**
     * The file backed buffer.
     */
    protected RandomAccessFile buffer;

    /**
     * Create a trace tester.
     *
     * @param fileName the file name
     */
    public TraceTester(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Run the specified action.
     *
     * @param action the action
     */
    public void run(Consumer<TraceRecordT> action) {
        try {
            this.buffer = new RandomAccessFile(fileName, "r");

            for (; ; ) {
                TraceRecordT traceRecord = this.readNext();

                if (traceRecord == null) {
                    break;
                }

                action.accept(traceRecord);
            }

            this.buffer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read the next trace record.
     *
     * @return the next trace record if exists
     */
    protected abstract TraceRecordT readNext();

    /**
     * Get the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }
}
