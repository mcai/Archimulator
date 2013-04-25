/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.util.serialization;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * XML serialization helper.
 *
 * @author Min Cai
 */
public class XMLSerializationHelper {
    private static Serializer serializer;

    /**
     * Static constructor.
     */
    static {
        serializer = new Persister();
    }

    /**
     * Deserialize.
     *
     * @param clz the class
     * @param str the XML string
     * @param <T> the type
     * @return the object deserialized from the specified XML string
     */
    public static <T> T deserialize(Class<T> clz, String str) {
        try {
            StringReader stringReader = new StringReader(str);
            return serializer.read(clz, stringReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Serialize.
     *
     * @param obj the object
     * @return the XML serialization from of the specified object
     */
    public static String serialize(Object obj) {
        try {
            StringWriter stringWriter = new StringWriter();
            serializer.write(obj, stringWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
