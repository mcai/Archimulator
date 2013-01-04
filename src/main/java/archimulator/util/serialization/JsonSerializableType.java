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

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;
import net.pickapack.JsonSerializationHelper;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.SQLException;

/**
 * JSON serializable type.
 *
 * @author Min Cai
 */
public abstract class JsonSerializableType extends BaseDataType {
    private Type clz;

	/**
     * Create a JSON serializable type.
     *
     * @param type the type
     */
    protected JsonSerializableType(Type type) {
		super(SqlType.LONG_STRING, new Class<?>[0]);
        this.clz = type;
    }

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		throw new SQLException("Default values for serializable types are not supported");
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return results.getString(columnPos);
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        return JsonSerializationHelper.deserialize(clz, (String) sqlArg);
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj) throws SQLException {
        return JsonSerializationHelper.serialize(obj, clz);
	}

	@Override
	public boolean isValidForField(Field field) {
		return Serializable.class.isAssignableFrom(field.getType());
	}

	@Override
	public boolean isStreamType() {
		return true;
	}

	@Override
	public boolean isComparable() {
		return false;
	}

	@Override
	public boolean isAppropriateId() {
		return false;
	}

	@Override
	public boolean isArgumentHolderRequired() {
		return true;
	}

	@Override
	public Object resultStringToJava(FieldType fieldType, String stringValue, int columnPos) throws SQLException {
		throw new SQLException("Serializable type cannot be converted from json to Java");
	}
}
