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
package spade.query.quickstep.parser;

import spade.query.quickstep.utility.TreeStringSerializable;

import java.util.ArrayList;

public class ParseName extends spade.query.quickstep.parser.ParseExpression
{
	private spade.query.quickstep.parser.ParseString name;

	public ParseName(int lineNumber, int columnNumber, spade.query.quickstep.parser.ParseString name)
	{
		super(lineNumber, columnNumber, ParseExpression.ExpressionType.kName);
		this.name = name;
	}

	@Override
	public String getLabel()
	{
		return "Name";
	}

	public ParseString getName()
	{
		return name;
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
		inline_field_names.add("name");
		inline_field_values.add(name.getValue());
	}
}
