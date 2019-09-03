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
package spade.query.quickgrail.neo4j.utility;

import org.apache.commons.collections.CollectionUtils;
import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.kernel.AbstractEnvironment;
import spade.query.quickgrail.core.utility.TreeStringSerializable;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.neo4j.entities.Neo4jGraphMetadata;
import spade.storage.neo4j.Neo4jExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spade.query.quickgrail.neo4j.utility.CommonVariables.EDGE_ALIAS;
import static spade.query.quickgrail.neo4j.utility.CommonVariables.NodeTypes.VERTEX;
import static spade.query.quickgrail.neo4j.utility.CommonVariables.RelationshipTypes.EDGE;
import static spade.query.quickgrail.neo4j.utility.CommonVariables.VERTEX_ALIAS;
import static spade.query.quickgrail.neo4j.utility.Neo4jUtil.removeDollar;

/**
 * QuickGrail compile-time environment (also used in runtime) mainly for
 * managing symbols (e.g. mapping from graph variables to underlying Neo4j).
 */
public class Environment extends TreeStringSerializable implements AbstractEnvironment
{
	private final static Neo4jGraph kBaseGraph = new Neo4jGraph("$base");
	private HashMap<String, String> symbols;
	private Neo4jExecutor executor;
	// for access in query classes
	private static String resultGraphName;
	private static final Logger logger = Logger.getLogger(Environment.class.getName());

	public Environment(Neo4jExecutor ns)
	{
		this.executor = ns;
		this.symbols = new HashMap<>();

		// Get the count of different graph variables/tables represented as labels.
		Set<String> tableNames = Neo4jUtil.GetAllTableNames(ns);
		if(CollectionUtils.isNotEmpty(tableNames))
		{
			for(Object table : tableNames)
			{
				String tableName = (String) table;
				symbols.put("$" + tableName, tableName);
			}
		}
	}

	@Override
	public boolean IsBaseGraph(Graph graph)
	{
		return graph.getName().equals(kBaseGraph.getName());
	}

	@Override
	public void clear()
	{
		if(symbols.size() > 0)
		{
			// remove all labels from all nodes
			StringBuilder removeLabelsQuery = new StringBuilder(100);
			removeLabelsQuery.append("MATCH (" + VERTEX_ALIAS + ":");
			removeLabelsQuery.append(VERTEX.toString());
			removeLabelsQuery.append(")");
			removeLabelsQuery.append("REMOVE ");
			removeLabelsQuery.append(VERTEX_ALIAS);
			for(String symbol : symbols.keySet())
			{
				removeLabelsQuery.append(":").append(removeDollar(symbol));
			}
			executor.executeQuery(removeLabelsQuery.toString());

			// remove all symbols from all relationships
			String removeSymbolsQuery = "MATCH ()-[" + EDGE_ALIAS + ":" + EDGE.toString() + "]->()" +
					"REMOVE " + EDGE_ALIAS + ".quickgrail_symbol";
			executor.executeQuery(removeSymbolsQuery);

			symbols.clear();
		}
	}

	@Override
	public Neo4jGraph allocateGraph()
	{
		return new Neo4jGraph(resultGraphName);
	}

	@Override
	public Neo4jGraph allocateGraph(String graphName)
	{
		return new Neo4jGraph(graphName);
	}

	@Override
	public Neo4jGraphMetadata allocateGraphMetadata()
	{
		logger.log(Level.WARNING, "GraphMetadata operations not supported in SPADE yet");
		return new Neo4jGraphMetadata(resultGraphName);
	}

	@Override
	public Neo4jGraphMetadata allocateGraphMetadata(String graphName)
	{
		logger.log(Level.WARNING, "GraphMetadata operations not supported in SPADE yet");
		return new Neo4jGraphMetadata(graphName);
	}

	@Override
	public String lookup(String symbol)
	{
		switch(symbol)
		{
			case "$base":
				return kBaseGraph.getName();
		}
		if(symbols.containsKey(symbol))
		{
			return symbol;
		}
		return null;
	}

	public void setResultGraphName(String resultGraphName)
	{
		Environment.resultGraphName = resultGraphName;
	}

	@Override
	public void addSymbol(String symbol, String value)
	{
		switch(symbol)
		{
			case "$base":
				throw new RuntimeException("Cannot reassign reserved variables.");
		}
		symbols.put(symbol, removeDollar(symbol));
	}

	@Override
	public void eraseSymbol(String symbol)
	{
		switch(symbol)
		{
			case "$base":
				logger.log(Level.WARNING, "Cannot erase reserved symbols.");
				throw new RuntimeException("Cannot erase reserved symbols.");
		}
		if(symbols.containsKey(symbol))
		{
			// remove label from all nodes
			String removeLabelQuery = "MATCH (" + VERTEX_ALIAS + ":" + VERTEX.toString() + ")" +
					"REMOVE " + VERTEX_ALIAS + ":" + removeDollar(symbol);
			executor.executeQuery(removeLabelQuery);
			// remove label from all relationships
			String removeSymbolQuery = "MATCH ()-[" + EDGE_ALIAS + ":" + EDGE.toString() + "]->() " +
					"WHERE " + EDGE_ALIAS + ".quickgrail_symbol CONTAINS '," + removeDollar(symbol) + ",'" +
					"SET " + EDGE_ALIAS + ".quickgrail_symbol = " +
					"replace(" + EDGE_ALIAS + ".quickgrail_symbol, '," + removeDollar(symbol) + ",', '')";
			executor.executeQuery(removeSymbolQuery);
			symbols.remove(symbol);
		}
	}

	@Override
	public final Map<String, String> getSymbols()
	{
		return symbols;
	}

	@Override
	public void gc()
	{
	}

	@Override
	public String getLabel()
	{
		return "Environment";
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
		for(Entry<String, String> entry : symbols.entrySet())
		{
			inline_field_names.add(entry.getKey());
			inline_field_values.add(entry.getValue());
		}
	}
}
