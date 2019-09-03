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
package spade.query.quickgrail.postgresql.execution;

import spade.query.quickgrail.core.entities.GraphMetadata.GraphMetadataComponent;
import spade.query.quickgrail.core.kernel.AbstractEnvironment;
import spade.query.quickgrail.core.kernel.ExecutionContext;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.utility.TreeStringSerializable;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraphMetadata;

import java.util.ArrayList;

/**
 * This class is not yet used in the SPADE integrated QuickGrail.
 */
public class SetGraphMetadata extends Instruction
{
	private static final String kDigits = "0123456789ABCDEF";
	private PostgreSQLGraphMetadata targetMetadata;
	private GraphMetadataComponent component;
	private PostgreSQLGraph sourceGraph;
	private String name;
	private String value;

	public SetGraphMetadata(PostgreSQLGraphMetadata targetMetadata,
							GraphMetadataComponent component,
							PostgreSQLGraph sourceGraph,
							String name,
							String value)
	{
		this.targetMetadata = targetMetadata;
		this.component = component;
		this.sourceGraph = sourceGraph;
		this.name = name;
		this.value = value;
	}

	private static String FormatStringLiteral(String input)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("e'");
		for(int i = 0; i < input.length(); ++i)
		{
			char c = input.charAt(i);
			if(c >= 32)
			{
				if(c == '\\' || c == '\'')
				{
					sb.append(c);
				}
				sb.append(c);
				continue;
			}
			switch(c)
			{
				case '\b':
					sb.append("\\b");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				default:
					// Use hexidecimal representation.
					sb.append("\\x");
					sb.append(kDigits.charAt(c >> 4));
					sb.append(kDigits.charAt(c & 0xF));
					break;
			}
		}
		sb.append("'");
		return sb.toString();
	}

	@Override
	public void execute(AbstractEnvironment env, ExecutionContext ctx)
	{
	}

	@Override
	public String getLabel()
	{
		return "SetGraphMetadata";
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
		inline_field_names.add("targetMetadata");
		inline_field_values.add(targetMetadata.getName());
		inline_field_names.add("component");
		inline_field_values.add(component.name().substring(1));
		inline_field_names.add("sourceGraph");
		inline_field_values.add(sourceGraph.getName());
		inline_field_names.add("name");
		inline_field_values.add(name);
		inline_field_names.add("value");
		inline_field_values.add(value);
	}
}
