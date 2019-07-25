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
package spade.query.postgresql.utility;

import spade.query.postgresql.entities.Graph;
import spade.query.postgresql.entities.GraphMetadata;
import spade.storage.postgresql.PostgresExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spade.query.postgresql.utility.CommonVariables.PRIMARY_KEY;

/**
 * Convenient functions.
 */
public class PostgresUtil
{
	private static final Logger logger = Logger.getLogger(PostgresUtil.class.getName());

	public static void CreateEmptyGraph(PostgresExecutor ps, Graph graph)
	{
		String vertexTable = graph.getVertexTableName();
		String edgeTable = graph.getEdgeTableName();

		StringBuilder sb = new StringBuilder();
		sb.append("DROP TABLE IF EXISTS ").append(vertexTable).append(";");
		sb.append("DROP TABLE IF EXISTS ").append(edgeTable).append(";");
		sb.append("CREATE TABLE ").append(vertexTable).append(" (").append(PRIMARY_KEY).append(" UUID);");
		sb.append("CREATE TABLE ").append(edgeTable).append(" (").append(PRIMARY_KEY).append(" UUID); ");
		ps.executeQuery(sb.toString());
	}

	// Note: GraphMetadata operations are not supported in SPADE anywhere
	public static void CreateEmptyGraphMetadata(PostgresExecutor ps, GraphMetadata metadata)
	{
		logger.log(Level.SEVERE, "GraphMetadata operations are not supported.");
		throw new UnsupportedOperationException("GraphMetadata operations are not supported.");
	}

	public static ArrayList<String> GetAllTableNames(PostgresExecutor ps)
	{
		String output = ps.executeCopy("COPY (SELECT table_name " +
				"FROM information_schema.tables " +
				"WHERE table_type='BASE TABLE' " +
				"AND table_schema='public') TO stdout;");
		return new ArrayList<>(Arrays.asList(output.split("\n")));
	}

	public static long GetNumVertices(PostgresExecutor ps, Graph graph)
	{
		return ps.executeQueryForLongResult(
				"COPY (SELECT COUNT(*) FROM " + graph.getVertexTableName() + ") TO stdout;");
	}

	public static long GetNumEdges(PostgresExecutor ps, Graph graph)
	{
		return ps.executeQueryForLongResult(
				"COPY (SELECT COUNT(*) FROM " + graph.getEdgeTableName() + ") TO stdout;");
	}

	public static long GetNumTimestamps(PostgresExecutor ps, Graph graph)
	{
		logger.log(Level.SEVERE, "GetNumTimestamps is not supported.");
		throw new UnsupportedOperationException("GetNumTimestamps is not supported.");
	}

	public static Long[] GetTimestampRange(PostgresExecutor ps, Graph graph)
	{
		logger.log(Level.SEVERE, "GetTimestampRange is not supported.");
		throw new UnsupportedOperationException("GetTimestampRange is not supported.");
	}
}
