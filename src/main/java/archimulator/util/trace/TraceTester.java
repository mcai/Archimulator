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

import archimulator.util.action.Action1;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author Min Cai
 * @param <TraceRecordT>
 */
public abstract class TraceTester<TraceRecordT> {
    private String fileName;

    /**
     *
     */
    protected RandomAccessFile buffer;

    /**
     *
     * @param fileName
     */
    public TraceTester(String fileName) {
        this.fileName = fileName;
    }

    /**
     *
     * @param action
     */
    public void run(Action1<TraceRecordT> action) {
        try {
            this.buffer = new RandomAccessFile(fileName, "r");

            for (; ; ) {
                TraceRecordT traceRecord = this.readNext();

                if (traceRecord == null) {
                    break;
                }

                action.apply(traceRecord);
            }

            this.buffer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return
     */
    protected abstract TraceRecordT readNext();

    /**
     *
     * @return
     */
    public String getFileName() {
        return fileName;
    }
}
