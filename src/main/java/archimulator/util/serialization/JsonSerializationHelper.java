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
package archimulator.util.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * JSON serialization helper.
 *
 * @author Min Cai
 */
public class JsonSerializationHelper {
    private static Gson gson;
    private static Gson gsonPrettyPrint;

    /**
     * Static constructor.
     */
    static {
        gson = new GsonBuilder().serializeNulls().serializeSpecialFloatingPointValues().create();
        gsonPrettyPrint = new GsonBuilder().setPrettyPrinting().serializeNulls().serializeSpecialFloatingPointValues().create();
    }

    /**
     * Deserialize.
     *
     * @param <T> the type
     * @param clz the class
     * @param str the string
     * @return the object deserialized from the specified string
     */
    public static <T> T deserialize(Class<T> clz, String str) {
        return fromJson(clz, str);
    }

    /**
     * Deserialize.
     *
     * @param <T> the type
     * @param clz the class
     * @param str the string
     * @return the object deserialized from the specified string
     */
    public static <T> T deserialize(Type clz, String str) {
        return fromJson(clz, str);
    }

    /**
     * Serialize.
     *
     * @param obj the object
     * @return the text serialized from the specified object
     */
    public static String serialize(Object obj) {
        return toJson(obj);
    }

    /**
     * Serialize.
     *
     * @param obj the object
     * @param type the type
     * @return the text serialized from the specified object
     */
    public static String serialize(Object obj, Type type) {
        return toJson(obj, type);
    }

    /**
     * Deserialize the object from the specified JSON string.
     *
     * @param <T> the type
     * @param clz the class
     * @param str the JSON string
     * @return the object deserialized from the specified JSON string
     */
    public static <T> T fromJson(Class<T> clz, String str) {
        return gson.fromJson(str, clz);
    }

    /**
     * Deserialize the object from the specified JSON string.
     *
     * @param <T> the type
     * @param clz the class
     * @param str the JSON string
     * @return the object deserialized from the specified JSON string
     */
    public static <T> T fromJson(Type clz, String str) {
        return gson.fromJson(str, clz);
    }

    /**
     * Serialize the specified object to a JSON string.
     *
     * @param obj the object
     * @return the JSON string serialized from the specified object
     */
    public static String toJson(Object obj) {
        return toJson(obj, false);
    }

    /**
     * Serialize the specified object to a JSON string.
     *
     * @param obj the object
     * @param type the type
     * @return the JSON string serialized from the specified object
     */
    public static String toJson(Object obj, Type type) {
        return toJson(obj, type, false);
    }

    /**
     * Serialize the specified object to a JSON string.
     *
     * @param obj the object
     * @param prettyPrint a value indicating whether pretty print is enabled or not
     * @return the JSON string serialized from the specified object
     */
    public static String toJson(Object obj, boolean prettyPrint) {
        return prettyPrint ? gsonPrettyPrint.toJson(obj) : gson.toJson(obj);
    }

    /**
     * Serialize the specified object to a JSON string.
     *
     * @param obj the object
     * @param type the type
     * @param prettyPrint a value indicating whether pretty print is enabled or not
     * @return the JSON string serialized from the specified object
     */
    public static String toJson(Object obj, Type type, boolean prettyPrint) {
        return prettyPrint ? gsonPrettyPrint.toJson(obj, type) : gson.toJson(obj, type);
    }

    /**
     * Pretty print.
     *
     * @param json the JSON string
     * @return the pretty printed string of the specified string
     */
    public static String prettyPrint(String json) {
        return gsonPrettyPrint.toJson(gsonPrettyPrint.fromJson(json, Object.class));
    }

    /**
     * The object wrapper.
     */
    public static class ObjectWrapper {
        private String className;
        private String str;

        /**
         * Create an object wrapper.
         *
         * @param className the class name
         * @param obj the object
         */
        public ObjectWrapper(String className, Object obj) {
            this.className = className;
            this.str = serialize(obj);
        }

        /**
         * Get the object.
         *
         * @param <T> the type
         * @return the object
         */
        @SuppressWarnings("unchecked")
        public <T> T getObj() {
            try {
                return (T) deserialize(Class.forName(this.className), this.str);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}