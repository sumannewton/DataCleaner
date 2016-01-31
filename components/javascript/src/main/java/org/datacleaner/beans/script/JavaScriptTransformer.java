/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 * <p>
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.beans.script;

import jdk.nashorn.api.scripting.ScriptUtils;
import org.datacleaner.api.*;
import org.datacleaner.components.categories.ScriptingCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.script.*;

/**
 * A transformer that uses user-written JavaScript to generate a value
 */
@Named("JavaScript transformer (simple)")
@Alias("JavaScript transformer")
@Description("Supply your own piece of JavaScript to do a custom transformation")
@Categorized(ScriptingCategory.class)
public class JavaScriptTransformer implements Transformer {

    private static final Logger logger = LoggerFactory
            .getLogger(JavaScriptTransformer.class);

    public enum ReturnType {STRING, NUMBER, BOOLEAN}

    @Provided
    ComponentContext componentContext;

    @Configured
    InputColumn<?>[] columns;

    @Configured
    ReturnType returnType = ReturnType.STRING;

    @Configured
    @Description("Available variables:\nvalues[0..]: Array of values\nvalues[\"my_col\"]: Map of values\nmy_col: Each column value has it's own variable\nout: Print to console using out.println('hello')\nlog: Print to log using log.info(...), log.warn(...), log.error(...)")
    @StringProperty(multiline = true, mimeType = {"text/javascript",
            "application/x-javascript"})
    String sourceCode = "function eval() {\n\treturn \"hello \" + values[0];\n}\n\neval();";

    private ScriptEngine _scriptEngine;
    private CompiledScript _script;

    public JavaScriptTransformer() {
        ScriptEngineManager _scriptEngineManager = new ScriptEngineManager();
        _scriptEngine = _scriptEngineManager.getEngineByName(JavaScriptUtils.SCRIPTTYPE);
    }

    @Override
    public OutputColumns getOutputColumns() {
        OutputColumns outputColumns = new OutputColumns(Object.class, "JavaScript output");
        if (returnType == ReturnType.NUMBER) {
            outputColumns.setColumnType(0, Number.class);
        } else if (returnType == ReturnType.BOOLEAN) {
            outputColumns.setColumnType(0, Boolean.class);
        } else {
            outputColumns.setColumnType(0, String.class);
        }
        return outputColumns;
    }

    @Validate
    void validate() {
        JavaScriptUtils.compileScript(_scriptEngine, sourceCode);
    }

    @Initialize
    public void init() {
        _scriptEngine.getContext().setAttribute("logger", logger, ScriptContext.GLOBAL_SCOPE);
        _scriptEngine.getContext().setAttribute("log", logger, ScriptContext.GLOBAL_SCOPE);
        _scriptEngine.getContext().setAttribute("out", System.out, ScriptContext.GLOBAL_SCOPE);
        _script = JavaScriptUtils.compileScript(_scriptEngine, sourceCode);
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        ScriptContext context = new SimpleScriptContext();
        context.setBindings(_scriptEngine.createBindings(), ScriptContext.ENGINE_SCOPE);
        context.setBindings(_scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE);
        Bindings localScope = context.getBindings(ScriptContext.ENGINE_SCOPE);

        JavaScriptUtils.addToScope(localScope, inputRow, columns, "values");

        Object result = null;
        try {
            result = _script.eval(context);
        } catch (ScriptException e) {
            componentContext.publishMessage(new ExecutionLogMessage("Error occurred while transforming row " + inputRow.getId()));
        }

        if (returnType == ReturnType.NUMBER) {
            result = ScriptUtils.convert(result, Number.class);
        } else if (returnType == ReturnType.BOOLEAN) {
            result = ScriptUtils.convert(result, Boolean.class);
        } else {
            result = ScriptUtils.convert(result, String.class);
        }
        return new Object[]{result};
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public void setColumns(InputColumn<?>[] columns) {
        this.columns = columns;
    }
}
