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
package spade.query.quickgrail.neo4j.execution;

import spade.core.AbstractEdge;
import spade.core.AbstractVertex;
import spade.query.quickgrail.core.entities.Graph.ExportFormat;
import spade.query.quickgrail.core.kernel.AbstractEnvironment;
import spade.query.quickgrail.core.kernel.ExecutionContext;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.utility.TreeStringSerializable;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.neo4j.utility.Neo4jUtil;
import spade.storage.neo4j.Neo4jExecutor;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spade.query.quickgrail.core.entities.Graph.kNonForceDumpLimit;
import static spade.query.quickgrail.core.entities.Graph.kNonForceVisualizeLimit;
import static spade.query.quickgrail.neo4j.utility.CommonVariables.EDGE_ALIAS;
import static spade.query.quickgrail.neo4j.utility.CommonVariables.RelationshipTypes.EDGE;
import static spade.query.quickgrail.neo4j.utility.CommonVariables.VERTEX_ALIAS;

/**
 * Export a QuickGrail graph to spade.core.Graph.
 */
public class ExportGraph extends Instruction
{
	private Neo4jGraph targetGraph;
	private ExportFormat format;
	private boolean force;
	private static final Logger logger = Logger.getLogger(ExportGraph.class.getName());

	public ExportGraph(Neo4jGraph targetGraph, ExportFormat format, boolean force)
	{
		this.targetGraph = targetGraph;
		this.format = format;
		this.force = force;
	}

	@Override
	public void execute(AbstractEnvironment env, ExecutionContext ctx)
	{
		Neo4jExecutor ns = (Neo4jExecutor) ctx.getExecutor();
		String targetVertexTable = targetGraph.getVertexTableName();
		String targetEdgeTable = targetGraph.getEdgeTableName();
		long numVertices = Neo4jUtil.GetNumVertices(ns, targetVertexTable);
		long numEdges = Neo4jUtil.GetNumEdges(ns, targetEdgeTable);
		long graphsize = numVertices + numEdges;

		if(!force)
		{
			if(format == ExportFormat.kNormal && (graphsize > kNonForceDumpLimit))
			{
				ctx.addResponse("It may take a long time to print the result data due to " +
						"too many vertices/edges: " + numVertices + "/" + numEdges + "" +
						"Please use 'force dump ...' to force the print");
				return;
			}
			else if(format == ExportFormat.kDot && (graphsize > kNonForceVisualizeLimit))
			{
				ctx.addResponse("It may take a long time to transfer the result data due to " +
						"too many vertices/edges: " + numVertices + "/" + numEdges + "" +
						"Please use 'force visualize ...' to force the transfer");
				return;
			}
		}

		spade.core.Graph resultGraph = new spade.core.Graph();
		if(numVertices > 0)
		{
			String vertexQuery = "MATCH (" + VERTEX_ALIAS + ":" + targetVertexTable + ") RETURN " + VERTEX_ALIAS;
			Map<String, AbstractVertex> vertices = Neo4jUtil.prepareVertexMapFromNeo4jResult(ns, vertexQuery);

			String edgeQuery;
			if(env.IsBaseGraph(targetGraph))
			{
				edgeQuery = "MATCH ()-[" + EDGE_ALIAS + ":" + EDGE.toString() + "]->() RETURN " + EDGE_ALIAS;
			}
			else
			{
				edgeQuery = "MATCH ()-[" + EDGE_ALIAS + ":" + EDGE.toString() + "]->() " +
						" WHERE " + EDGE_ALIAS + ".quickgrail_symbol CONTAINS '," + targetEdgeTable + ",' " +
						" RETURN " + EDGE_ALIAS;
			}
			Set<AbstractEdge> edges = Neo4jUtil.prepareEdgeSetFromNeo4jResult(ns, edgeQuery, vertices);
			logger.log(Level.INFO, "vertices: " + vertices.values());
			logger.log(Level.INFO, "edges: " + edges);
			resultGraph.vertexSet().addAll(vertices.values());
			resultGraph.edgeSet().addAll(edges);
		}

		if(format == ExportFormat.kNormal)
		{
			ctx.addResponse(resultGraph.prettyPrint());
		}
		else
		{
			ctx.addResponse(resultGraph.exportGraph());
		}
	}

	@Override
	public String getLabel()
	{
		return "ExportGraph";
	}

	@Override
	protected void getFieldStringItems(
			ArrayList<String> inline_field_names,
			ArrayList<String> inline_field_values,
			ArrayList<String> non_container_child_field_names,
			ArrayList<TreeStringSerializable> non_container_child_fields,
			ArrayList<String> container_child_field_names,
			ArrayList<ArrayList<? extends TreeStringSerializable>> container_child_fields)
	{
		inline_field_names.add("targetGraph");
		inline_field_values.add(targetGraph.getName());
		inline_field_names.add("format");
		inline_field_values.add(format.name());
		inline_field_names.add("force");
		inline_field_values.add(String.valueOf(force));
	}
}
