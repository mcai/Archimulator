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
package archimulator.util;

import ch.lambdaj.function.convert.Converter;
import org.jaxen.JaxenException;
import org.jaxen.javabean.Element;
import org.jaxen.javabean.JavaBeanXPath;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.Lambda.convert;

public class JaxenHelper {
    @SuppressWarnings("unchecked")
    public static <T> List<T> selectNodes(Object obj, String expr) {
        try {
            List<Element> result = new JavaBeanXPath(expr).selectNodes(obj);
            return result != null ? convert(result, new Converter<Element, T>() {
                @Override
                public T convert(Element from) {
                    return (T) from.getObject();
                }
            }) : null;
        } catch (JaxenException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T selectSingleNode(Object obj, String expr) {
        try {
            Element result = (Element) new JavaBeanXPath(expr).selectSingleNode(obj);
            return (T) (result != null ? result.getObject() : null);
        } catch (JaxenException e) {
            throw new RuntimeException(e);
        }
    }

    public static void dumpValueFromXPath(Map<String, String> stats, Object obj, String expr) {
        Object resultObj = selectSingleNode(obj, expr);
        if (resultObj != null) {
            if (resultObj instanceof Map) {
                Map resultMap = (Map) resultObj;

                for (Object key : resultMap.keySet()) {
                    stats.put(escape(expr) + "[" + key + "]", toString(resultMap.get(key)));
                }
            } else {
                stats.put(escape(expr), toString(resultObj));
            }
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    public static void dumpValuesFromXPath(Map<String, String> stats, Object obj, String expr) {
        List<Object> result = selectNodes(obj, expr);
        if (result != null) {
            for (int i = 0; i < result.size(); i++) {
                Object resultObj = result.get(i);
                stats.put(escape(expr) + "[" + i + "]", toString(resultObj));
            }
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    private static String escape(String str) {
        return str.replaceAll("'", "").replaceAll("\\[", "\\[").replaceAll("\\]", "\\]");
    }

    private static String toString(Object resultObj) {
        if (resultObj instanceof Integer || resultObj instanceof Long || resultObj instanceof Float || resultObj instanceof Double) {
            return MessageFormat.format("{0}", resultObj);
        }
        return escape(resultObj + "");
    }
}
