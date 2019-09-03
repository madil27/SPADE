/*
 --------------------------------------------------------------------------------
 SPADE - Support for Provenance Auditing in Distributed Environments.
 Copyright (C) 2018 SRI International

 This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
 --------------------------------------------------------------------------------
 */
package spade.query.quickgrail.quickstep.execution;

import spade.query.quickgrail.core.entities.Graph.GraphComponent;
import spade.query.quickgrail.core.kernel.AbstractEnvironment;
import spade.query.quickgrail.core.kernel.ExecutionContext;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.utility.TreeStringSerializable;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;
import spade.storage.quickstep.QuickstepExecutor;

import java.util.ArrayList;

/**
 * Subtract one graph from the other.
 */
public class SubtractGraph extends Instruction
{
	// Output graph.
	private QuickstepGraph outputGraph;
	// Minuend graph.
	private QuickstepGraph minuendGraph;
	// Subtrahend graph.
	private QuickstepGraph subtrahendGraph;
	// Graph components that should be involved in the operation (vertices / edges, or both).
	private GraphComponent component;

	public SubtractGraph(QuickstepGraph outputGraph, QuickstepGraph minuendGraph,
						 QuickstepGraph subtrahendGraph, GraphComponent component)
	{
		this.outputGraph = outputGraph;
		this.minuendGraph = minuendGraph;
		this.subtrahendGraph = subtrahendGraph;
		this.component = component;
	}

	@Override
	public void execute(AbstractEnvironment env, ExecutionContext ctx)
	{
		String outputVertexTable = outputGraph.getVertexTableName();
		String outputEdgeTable = outputGraph.getEdgeTableName();
		String minuendVertexTable = minuendGraph.getVertexTableName();
		String minuendEdgeTable = minuendGraph.getEdgeTableName();
		String subtrahendVertexTable = subtrahendGraph.getVertexTableName();
		String subtrahendEdgeTable = subtrahendGraph.getEdgeTableName();

		QuickstepExecutor qs = (QuickstepExecutor) ctx.getExecutor();
		if(component == null || component == GraphComponent.kVertex)
		{
			qs.executeQuery("\\analyzerange " + subtrahendVertexTable + "\n");
			qs.executeQuery("INSERT INTO " + outputVertexTable +
					" SELECT id FROM " + minuendVertexTable +
					" WHERE id NOT IN (SELECT id FROM " + subtrahendVertexTable + ");");
		}
		if(component == null || component == GraphComponent.kEdge)
		{
			qs.executeQuery("\\analyzerange " + subtrahendEdgeTable + "\n");
			qs.executeQuery("INSERT INTO " + outputEdgeTable +
					" SELECT id FROM " + minuendEdgeTable +
					" WHERE id NOT IN (SELECT id FROM " + subtrahendEdgeTable + ");");
		}
	}

	@Override
	public String getLabel()
	{
		return "SubtractGraph";
	}

	@Override
	protected void getFieldStringItems(ArrayList<String> inline_field_names, ArrayList<String> inline_field_values,
									   ArrayList<String> non_container_child_field_names, ArrayList<TreeStringSerializable> non_container_child_fields,
									   ArrayList<String> container_child_field_names,
									   ArrayList<ArrayList<? extends TreeStringSerializable>> container_child_fields)
	{
		inline_field_names.add("outputGraph");
		inline_field_values.add(outputGraph.getName());
		inline_field_names.add("minuendGraph");
		inline_field_values.add(minuendGraph.getName());
		inline_field_names.add("subtrahendGraph");
		inline_field_values.add(subtrahendGraph.getName());
		if(this.component != null)
		{
			inline_field_names.add("component");
			inline_field_values.add(component.name());
		}
	}
}
