/*
 --------------------------------------------------------------------------------
 SPADE - Support for Provenance Auditing in Distributed Environments.
 Copyright (C) 2017 SRI International
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
package spade.query.neo4j;

import org.apache.commons.collections.CollectionUtils;
import spade.core.AbstractEdge;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static spade.storage.Neo4j.RelationshipTypes;

/**
 * @author raza
 */
public class GetEdge extends Neo4j<Set<AbstractEdge>>
{
	@Override
	public Set<AbstractEdge> execute(String argument_string)
	{
		Pattern argument_pattern = Pattern.compile(",");
		String[] arguments = argument_pattern.split(argument_string);
		String constraints = arguments[0].trim();
		Map<String, List<String>> parameters = parseConstraints(constraints);
		Integer limit = null;
		if(arguments.length > 1)
			limit = Integer.parseInt(arguments[1].trim());

		return execute(parameters, limit);
	}

	@Override
	public Set<AbstractEdge> execute(Map<String, List<String>> parameters, Integer limit)
	{
		Set<AbstractEdge> edgeSet = null;
		try
		{
			StringBuilder query = new StringBuilder(100);
			query.append("MATCH ()-[").append(EDGE_ALIAS).append(":").append(RelationshipTypes.EDGE).append("]->()");
			if(parameters.size() > 0)
			{
				query.append(" WHERE ");
				for(Map.Entry<String, List<String>> entry : parameters.entrySet())
				{
					String colName = entry.getKey();
					List<String> values = entry.getValue();
					query.append(EDGE_ALIAS).append(".");
					query.append(colName);
					query.append(values.get(COMPARISON_OPERATOR));
					query.append("'");
					query.append(values.get(COL_VALUE));
					query.append("'");
					query.append(" ");
					String boolOperator = values.get(BOOLEAN_OPERATOR);
					if(boolOperator != null)
						query.append(boolOperator);
				}
			}
			query.append("RETURN ").append(EDGE_ALIAS);
			if(limit != null)
				query.append(" LIMIT ").append(limit);

			edgeSet = prepareEdgeSetFromNeo4jResult(query.toString());
			if(!CollectionUtils.isEmpty(edgeSet))
				return edgeSet;
		}
		catch(Exception ex)
		{
			Logger.getLogger(GetVertex.class.getName()).log(Level.SEVERE, "Error creating vertex set!", ex);
		}

		return edgeSet;
	}
}
