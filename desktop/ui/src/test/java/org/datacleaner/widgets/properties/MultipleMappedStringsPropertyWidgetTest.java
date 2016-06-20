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
package org.datacleaner.widgets.properties;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.datastructures.BuildMapTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.extension.output.CreateStagingTableAnalyzer;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.widgets.DCCheckBox;

import junit.framework.TestCase;

public class MultipleMappedStringsPropertyWidgetTest extends TestCase {

	public void testRemoveColumnFromTransformer() throws Exception {
	        DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();

	        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
	            ajb.addSourceColumn(new MutableColumn("foo", ColumnType.VARCHAR));
	            final MutableColumn barColumn = new MutableColumn("bar", ColumnType.VARCHAR);
	            ajb.addSourceColumn(barColumn);
	            ajb.addSourceColumn(new MutableColumn("baz", ColumnType.VARCHAR));
	            final MutableColumn column = new MutableColumn("foobar", ColumnType.VARCHAR);
	            // add another available column
				ajb.addSourceColumn(column);
	            
	            final TransformerComponentBuilder<BuildMapTransformer> tjb = ajb.addTransformer(BuildMapTransformer.class);
	            final TransformerDescriptor<BuildMapTransformer> descriptor = tjb.getDescriptor();
	            final ConfiguredPropertyDescriptor valuesProperty = descriptor.getConfiguredProperty("Values");
	            final ConfiguredPropertyDescriptor keysProperty = descriptor.getConfiguredProperty("Keys");
	            
	            final MultipleMappedStringsPropertyWidget widget = new MultipleMappedStringsPropertyWidget(tjb,
	                    valuesProperty, keysProperty) {
	                @Override
	                protected String getDefaultMappedString(InputColumn<?> inputColumn) {
	                    return inputColumn.getName();
	                }
	            };
	            widget.onPanelAdd();

	            // initialize with null
	            widget.initialize(null);
	            widget.selectAll();
	            final List<InputColumn<?>> inputColumns = tjb.getInputColumns();
				assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[bar], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]",inputColumns.toString()); 
	            
	            InputColumn<?>[] value = widget.getValue();
	            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[bar], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]", Arrays.toString(value));
	            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[bar], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]",Arrays.asList(widget.getColumns()).toString());
	            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[bar], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]", widget.getSelectedInputColumns().toString());
	            assertEquals("foo,bar,baz,foobar", getAvailableCheckBoxValues(widget));

	            ajb.removeSourceColumn(barColumn); 
	            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]",tjb.getInputColumns().toString()); 
	            
	            widget.onPanelRemove();
	            value = widget.getValue();
	            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]", Arrays.toString(value));
	            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]",Arrays.asList(widget.getColumns()).toString());
	            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]", widget.getSelectedInputColumns().toString()); 
	            assertEquals("bar", widget.getDefaultMappedString(inputColumns.get(1))); 
	            assertEquals("foo,baz,foobar", getAvailableCheckBoxValues(widget));

	            // remove a column
	            ajb.removeSourceColumn(barColumn);

	            value = widget.getValue();
	            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]", Arrays.toString(value));
	            assertEquals("foo,baz,foobar", getAvailableCheckBoxValues(widget));
	            
	            widget.onPanelRemove();
	        }
	    }
	
	public void testRemoveColumnFromAnalyzer() throws Exception {
        DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();

        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
            ajb.addSourceColumn(new MutableColumn("foo", ColumnType.VARCHAR));
            final MutableColumn barColumn = new MutableColumn("bar", ColumnType.VARCHAR);
            ajb.addSourceColumn(barColumn);
            ajb.addSourceColumn(new MutableColumn("baz", ColumnType.VARCHAR));
            final MutableColumn column = new MutableColumn("foobar", ColumnType.VARCHAR);
            // add another available column
			ajb.addSourceColumn(column);
            
            final AnalyzerComponentBuilder<CreateStagingTableAnalyzer> analyzerJobBuilder = ajb.addAnalyzer(CreateStagingTableAnalyzer.class);
            ajb.addAnalyzer(analyzerJobBuilder); 
            

            final ConfiguredPropertyDescriptor valuesProperty = analyzerJobBuilder.getDescriptor().getConfiguredProperty("Columns");
            final ConfiguredPropertyDescriptor keysProperty = analyzerJobBuilder.getDescriptor().getConfiguredProperty("Fields");
            
            final MultipleMappedStringsPropertyWidget widget = new MultipleMappedStringsPropertyWidget(analyzerJobBuilder,
                    valuesProperty, keysProperty) {
                @Override
                protected String getDefaultMappedString(InputColumn<?> inputColumn) {
                    return inputColumn.getName();
                }
            };
            widget.onPanelAdd();

            // initialize with null
            widget.initialize(null);
            widget.selectAll();
            final List<InputColumn<?>> inputColumns = analyzerJobBuilder.getInputColumns();
			assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[bar], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]",inputColumns.toString()); 
            
            InputColumn<?>[] value = widget.getValue();
            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[bar], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]", Arrays.toString(value));
            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[bar], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]",Arrays.asList(widget.getColumns()).toString());
            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[bar], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]", widget.getSelectedInputColumns().toString());
            assertEquals("foo,bar,baz,foobar", getAvailableCheckBoxValues(widget));

            ajb.removeSourceColumn(barColumn); 
            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]",analyzerJobBuilder.getInputColumns().toString()); 
            
            widget.onPanelRemove();
            value = widget.getValue();
            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]", Arrays.toString(value));
            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]",Arrays.asList(widget.getColumns()).toString());
            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[baz], MetaModelInputColumn[foobar]]", widget.getSelectedInputColumns().toString()); 
            assertEquals("bar", widget.getDefaultMappedString(inputColumns.get(1))); 
            assertEquals("foo,baz,foobar", getAvailableCheckBoxValues(widget));
            
            widget.onPanelRemove();
        }
    }

	
	/**
	 * Helper method to determine which checkboxes are shown
	 * 
	 * @param widget
	 * @return
	 */
	private String getAvailableCheckBoxValues(MultipleInputColumnsPropertyWidget widget) {
		StringBuilder sb = new StringBuilder();
		Collection<DCCheckBox<InputColumn<?>>> values = widget.getCheckBoxes().values(); 
		for (DCCheckBox<InputColumn<?>> dcCheckBox : values) {
			if (dcCheckBox instanceof DCCheckBox) {
				@SuppressWarnings("unchecked")
				DCCheckBox<InputColumn<?>> checkBox = (DCCheckBox<InputColumn<?>>) dcCheckBox;
				String name = checkBox.getValue().getName();
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(name);
			}
		}
		return sb.toString();
	}
}
