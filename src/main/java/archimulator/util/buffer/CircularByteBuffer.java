/**
 * ****************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the PickaPack library.
 * <p>
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.util.buffer;

import java.nio.ByteBuffer;

/**
 * Circular byte buffer.
 *
 * @author Min Cai
 */
public class CircularByteBuffer {
    private ByteBuffer buffer;
    private int readIndex = 0;
    private int writeIndex = 0;

    /**
     * Create a circular byte buffer.
     *
     * @param capacity the capacity
     */
    public CircularByteBuffer(int capacity) {
        buffer = ByteBuffer.allocate(capacity);
    }

    /**
     * Reset the circular byte buffer.
     */
    public void reset() {
        readIndex = 0;
        writeIndex = 0;
    }

    /**
     * Read data from the circular byte buffer into the specified destination byte array.
     *
     * @param dest the destination byte array
     * @param offset the offset
     * @param length the length
     * @return the number of data read from the circular byte buffer
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
     * Write data from the specified byte array into the specified circular byte buffer.
     *
     * @param src the source byte array
     * @param offset the offset
     * @param length the length of the data written
     * @return a boolean value indicating whether the specified number of bytes has been written successfully or not
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
     * Get a boolean value indicating whether the circular byte buffer is empty or not.
     *
     * @return a boolean value indicating whether the circular byte buffer is empty or not
     */
    public boolean isEmpty() {
        return writeIndex == readIndex;
    }

    /**
     * Get a boolean value indicating whether the circular byte buffer is full or not
     *
     * @return a boolean value indicating whether the circular byte buffer is full or not
     */
    public boolean isFull() {
        return writeIndex + 1 <= buffer.capacity() && writeIndex + 1 == readIndex || writeIndex == buffer.capacity() - 1 && readIndex == 0;
    }

    /**
     * Get the supporting byte buffer.
     *
     * @return the supporting byte buffer
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Get the read index.
     *
     * @return the read index
     */
    public int getReadIndex() {
        return readIndex;
    }

    /**
     * Get the write index.
     *
     * @return the write index
     */
    public int getWriteIndex() {
        return writeIndex;
    }
}

