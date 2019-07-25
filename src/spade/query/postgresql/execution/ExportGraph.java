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
package spade.query.postgresql.execution;

import spade.core.AbstractEdge;
import spade.core.AbstractVertex;
import spade.core.Edge;
import spade.core.Vertex;
import spade.query.postgresql.entities.Graph;
import spade.query.postgresql.kernel.Environment;
import spade.query.postgresql.utility.PostgresUtil;
import spade.query.postgresql.utility.TreeStringSerializable;
import spade.storage.postgresql.PostgresExecutor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spade.query.postgresql.utility.CommonVariables.CHILD_VERTEX_KEY;
import static spade.query.postgresql.utility.CommonVariables.EDGE_TABLE;
import static spade.query.postgresql.utility.CommonVariables.PARENT_VERTEX_KEY;
import static spade.query.postgresql.utility.CommonVariables.PRIMARY_KEY;
import static spade.query.postgresql.utility.CommonVariables.VERTEX_TABLE;

/**
 * Export a QuickGrail graph to spade.core.Graph or to DOT representation.
 */
public class ExportGraph extends Instruction
{
	private static final int kNonForceVisualizeLimit = 10000;
	private static final int kNonForceDumpLimit = 100;
	private static final Logger logger = Logger.getLogger(ExportGraph.class.getName());
	private Graph targetGraph;
	private Format format;
	private boolean force;

	public ExportGraph(Graph targetGraph, Format format, boolean force)
	{
		this.targetGraph = targetGraph;
		this.format = format;
		this.force = force;
	}

	@Override
	public void execute(Environment env, ExecutionContext ctx)
	{
		PostgresExecutor ps = ctx.getExecutor();

		if(!force)
		{
			long numVertices = PostgresUtil.GetNumVertices(ps, targetGraph);
			long numEdges = PostgresUtil.GetNumEdges(ps, targetGraph);
			long graphsize = numVertices + numEdges;
			if(format == Format.kNormal && (graphsize > kNonForceDumpLimit))
			{
				ctx.addResponse("It may take a long time to print the result data due to " +
						"too many vertices/edges: " + numVertices + "/" + numEdges + "" +
						"Please use 'force dump ...' to force the print");
				return;
			}
			else if(format == Format.kDot && (graphsize > kNonForceVisualizeLimit))
			{
				ctx.addResponse("It may take a long time to transfer the result data due to " +
						"too many vertices/edges: " + numVertices + "/" + numEdges + "" +
						"Please use 'force visualize ...' to force the transfer");
				return;
			}
		}

		ps.executeQuery("DROP TABLE IF EXISTS m_vertex;" +
				"DROP TABLE IF EXISTS m_edge;" +
				"CREATE TABLE m_vertex(" + PRIMARY_KEY + " UUID);" +
				"CREATE TABLE m_edge(" + PRIMARY_KEY + " UUID);");

		String targetVertexTable = targetGraph.getVertexTableName();
		String targetEdgeTable = targetGraph.getEdgeTableName();
		ps.executeQuery("INSERT INTO m_vertex SELECT " + PRIMARY_KEY + " FROM " + targetVertexTable + " GROUP BY " +
				PRIMARY_KEY + ";" +
				"INSERT INTO m_edge SELECT " + PRIMARY_KEY + " FROM " + targetEdgeTable + " GROUP BY " +
				PRIMARY_KEY + ";");

		HashMap<String, AbstractVertex> vertices = exportVertices(ps, "m_vertex");
		Set<AbstractEdge> edges = exportEdges(ps, "m_edge", vertices);

		ps.executeQuery("DROP TABLE IF EXISTS m_vertex;" +
				"DROP TABLE IF EXISTS m_edge;");

		spade.core.Graph graph = new spade.core.Graph();
		graph.vertexSet().addAll(vertices.values());
		graph.edgeSet().addAll(edges);

		if(format == Format.kNormal)
		{
			ctx.addResponse(graph.toString());
		}
		else
		{
			ctx.addResponse(graph.exportGraph());
		}
	}

	private HashMap<String, AbstractVertex> exportVertices(
			PostgresExecutor ps, String targetVertexTable)
	{
		HashMap<String, AbstractVertex> vertices = new HashMap<>();
		long numVertices = ps.executeQueryForLongResult(
				"COPY (SELECT COUNT(*) FROM " + targetVertexTable + ") TO stdout;");
		if(numVertices == 0)
		{
			return vertices;
		}

		try
		{
			ResultSet result = ps.executeQuery(
					"SELECT * FROM " + VERTEX_TABLE + " WHERE " + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM " +
							targetVertexTable + ");");
			ResultSetMetaData metadata = result.getMetaData();
			int columnCount = metadata.getColumnCount();
			Map<Integer, String> columnLabels = new HashMap<>();
			for(int i = 1; i <= columnCount; i++)
			{
				columnLabels.put(i, metadata.getColumnName(i));
			}
			while(result.next())
			{
				String hash = null;
				AbstractVertex vertex = new Vertex();
				for(int i = 1; i <= columnCount; i++)
				{
					String colName = columnLabels.get(i);
					String value = result.getString(i);
					if(value != null & colName != null)
					{
						if(!colName.equals(PRIMARY_KEY))
						{
							vertex.addAnnotation(colName, value);
						}
						else
						{
							hash = value;
						}
					}
				}
				vertices.put(hash, vertex);
			}
		}
		catch(Exception ex)
		{
			logger.log(Level.SEVERE, "Error fetching vertices from the database!", ex);
		}
		return vertices;
	}

	private Set<AbstractEdge> exportEdges(
			PostgresExecutor ps, String targetEdgeTable, HashMap<String, AbstractVertex> vertices)
	{
		Set<AbstractEdge> edges = new HashSet<>();
		long numEdges = ps.executeQueryForLongResult(
				"COPY (SELECT COUNT(*) FROM " + targetEdgeTable + ") TO stdout;");
		if(numEdges == 0)
		{
			return edges;
		}

		try
		{
			ResultSet result = ps.executeQuery(
					"SELECT * FROM " + EDGE_TABLE + " WHERE " + PRIMARY_KEY + " IN (SELECT " + PRIMARY_KEY + " FROM " +
							targetEdgeTable + ");");
			ResultSetMetaData metadata = result.getMetaData();
			int columnCount = metadata.getColumnCount();

			Map<Integer, String> columnLabels = new HashMap<>();
			for(int i = 1; i <= columnCount; i++)
			{
				columnLabels.put(i, metadata.getColumnName(i));
			}
			while(result.next())
			{
				AbstractEdge edge = new Edge(null, null);
				for(int i = 1; i <= columnCount; i++)
				{
					String colName = columnLabels.get(i);
					String value = result.getString(i);
					if(value != null & colName != null)
					{
						if(!colName.equals(PRIMARY_KEY))
						{
							if(colName.equals(CHILD_VERTEX_KEY))
							{
								AbstractVertex childVertex = vertices.get(value);
								edge.setChildVertex(childVertex);
							}
							if(colName.equals(PARENT_VERTEX_KEY))
							{
								AbstractVertex parentVertex = vertices.get(value);
								edge.setParentVertex(parentVertex);
							}
							edge.addAnnotation(colName, value);
						}
					}
				}
				edges.add(edge);
			}
		}
		catch(Exception ex)
		{
			logger.log(Level.SEVERE, "Error fetching edges from the database!", ex);
		}

		return edges;
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

	public enum Format
	{
		kNormal,
		kDot
	}
}
