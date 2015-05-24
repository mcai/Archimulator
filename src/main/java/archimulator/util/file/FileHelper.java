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

import java.io.*;

/**
 *
 * @author Min Cai
 */
public class FileHelper {
    /**
     *
     * @param dir
     * @return
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (String child : dir.list()) {
                if (!deleteDir(new File(dir, child))) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    /**
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] load(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        byte[] content = load(in, (int) file.length());
        in.close();
        return content;
    }

    /**
     *
     * @param in
     * @param length
     * @return
     * @throws IOException
     */
    public static byte[] load(InputStream in, int length) throws IOException {
        byte[] buffer = new byte[length];
        int offset = 0;
        for (; ; ) {
            int remain = length - offset;
            if (remain <= 0) {
                break;
            }
            int numRead = in.read(buffer, offset, remain);
            if (numRead == -1) {
                throw new IOException("Reached EOF, read " + offset + " expecting " + length);
            }
            offset += numRead;
        }
        return buffer;
    }

    /**
     *
     * @param file
     * @param content
     * @throws IOException
     */
    public static void save(File file, byte[] content) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        save(out, content);
        out.close();
    }

    /**
     *
     * @param out
     * @param content
     * @throws IOException
     */
    public static void save(OutputStream out, byte[] content) throws IOException {
        out.write(content);
    }
}
