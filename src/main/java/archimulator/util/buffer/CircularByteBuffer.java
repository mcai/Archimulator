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
package archimulator.util.buffer;

import java.nio.ByteBuffer;

/**
 *
 * @author Min Cai
 */
public class CircularByteBuffer {
    private ByteBuffer buffer;
    private int readIndex = 0;
    private int writeIndex = 0;

    /**
     *
     * @param capacity
     */
    public CircularByteBuffer(int capacity) {
        buffer = ByteBuffer.allocate(capacity);
    }

    /**
     *
     */
    public void reset() {
        readIndex = 0;
        writeIndex = 0;
    }

    /**
     *
     * @param dest
     * @param offset
     * @param length
     * @return
     */
    public int read(byte[] dest, int offset, int length) {
        assert length < buffer.capacity() : "The requested read is bigger than the buffer";

        if (writeIndex == readIndex) {
            return 0;
        }

        buffer.position(readIndex);
        if (writeIndex < readIndex) {
            int remainder = buffer.remaining();
            if (remainder < length) {
                buffer.get(dest, offset, remainder);

                offset += remainder;
                length -= remainder;

                readIndex = 0;
                buffer.position(readIndex);

                int space = writeIndex - readIndex;
                if (space <= length) {
                    length = space;
                }

                buffer.get(dest, offset, length);
                readIndex += length;

                return remainder + length;
            } else {
                buffer.get(dest, offset, remainder);
                readIndex += remainder;
                return remainder;
            }
        } else {
            int space = writeIndex - readIndex;
            if (space <= length) {
                length = space;
            }

            buffer.get(dest, offset, length);
            readIndex += length;
            return length;
        }
    }

    /**
     *
     * @param src
     * @param offset
     * @param length
     * @return
     */
    public boolean write(byte[] src, int offset, int length) {
        assert length < buffer.capacity() : "The requested write is bigger than the buffer";

        buffer.position(writeIndex);

        if ((readIndex <= writeIndex && writeIndex + length < buffer.capacity()) ||
                (writeIndex < readIndex && length < readIndex - writeIndex)) {
            // source fits in the remainder of the buffer
            buffer.put(src, offset, length);
            writeIndex += length;
            return true;
        } else {
            // the source don't fit in the buffer without wrapping
            int remainder = buffer.remaining();

            if (readIndex < writeIndex && length > readIndex + remainder) {
                return false;
            }
            if (writeIndex < readIndex && length > readIndex - writeIndex) {
                return false;
            }


            buffer.put(src, offset, remainder);

            offset += remainder;
            length -= remainder;

            writeIndex = 0;
            buffer.position(writeIndex);

            assert length < readIndex : "There is not enough room for this write operation";
            buffer.put(src, offset, length);
            writeIndex += length;

            return true;
        }
    }

    /**
     *
     * @return
     */
    public boolean isEmpty() {
        return writeIndex == readIndex;
    }

    /**
     *
     * @return
     */
    public boolean isFull() {
        return writeIndex + 1 <= buffer.capacity() && writeIndex + 1 == readIndex || writeIndex == buffer.capacity() - 1 && readIndex == 0;
    }

    /**
     *
     * @return
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     *
     * @return
     */
    public int getReadIndex() {
        return readIndex;
    }

    /**
     *
     * @return
     */
    public int getWriteIndex() {
        return writeIndex;
    }
}

