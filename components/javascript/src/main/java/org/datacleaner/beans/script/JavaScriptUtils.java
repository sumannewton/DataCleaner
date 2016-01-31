/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.beans.script;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.api.scripting.ScriptUtils;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.util.ReflectionUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import javax.script.*;

/**
 * Various utility methods for dealing with JavaScript (Mozilla Rhino)
 * 
 * 
 */
final class JavaScriptUtils {

    public static final String SCRIPTTYPE = "nashorn";

    private JavaScriptUtils() {
		// prevent instantiation
	}

	/**
	 * Adds an object to the JavaScript scope with a set of variable names
	 * 
	 * @param scope
	 * @param object
	 * @param names
	 */
	public static void addToScope(Bindings scope, Object object, String... names) {
		for (String name : names) {
			name = name.replaceAll(" ", "_");
			scope.put(name, object);
		}
	}

	/**
	 * Adds the values of a row to the JavaScript scope
	 * 
	 * @param scope
	 * @param inputRow
	 * @param columns
	 * @param arrayName
	 */
	public static void addToScope(Bindings scope, InputRow inputRow, InputColumn<?>[] columns, String arrayName) {
        Object[] values = new Object[columns.length];

		for (int i = 0; i < columns.length; i++) {
			InputColumn<?> column = columns[i];
			Object value = inputRow.getValue(column);

			values[i] = value;

			// TODO: Ew! Why?
			addToScope(scope, value, column.getName(), column.getName().toLowerCase(), column.getName().toUpperCase());
		}

		addToScope(scope, values, arrayName);
	}

    public static CompiledScript compileScript(ScriptEngine scriptEngine, String sourceCode) {
        Compilable compilableScriptEngine = (Compilable) scriptEngine;
        try {
            return compilableScriptEngine.compile(sourceCode);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("Script is not proper JavaScript", e);
        }
    }
}
