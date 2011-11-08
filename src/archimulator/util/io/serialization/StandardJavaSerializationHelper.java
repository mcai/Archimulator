/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.util.io.serialization;

import java.io.*;
import java.nio.ByteBuffer;

public class StandardJavaSerializationHelper {
    public static <T> void serialize(T obj, String fileName) {
        try {
            FileOutputStream fileOut = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(obj);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(Class<? extends T> clz, String fileName) {
        try {
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            T obj = (T) in.readObject();
            in.close();
            fileIn.close();
            return obj;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeDirectByteBuffer(ObjectOutputStream oos, ByteBuffer buffer) throws IOException {
        byte[] bufferData = new byte[buffer.capacity()];
        buffer.position(0);
        buffer.get(bufferData);

        oos.writeObject(bufferData);
    }

    public static ByteBuffer readDirectByteBuffer(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        byte[] bufferData = (byte[]) ois.readObject();

        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferData.length);
        buffer.position(0);
        buffer.put(bufferData);
        return buffer;
    }
}
