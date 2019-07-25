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

public class ParseOperation extends spade.query.quickstep.parser.ParseExpression
{
	private spade.query.quickstep.parser.ParseExpression subject;
	private spade.query.quickstep.parser.ParseString operator;
	private ArrayList<spade.query.quickstep.parser.ParseExpression> operands;

	public ParseOperation(int lineNumber, int columnNumber,
						  spade.query.quickstep.parser.ParseExpression subject, spade.query.quickstep.parser.ParseString operator)
	{
		super(lineNumber, columnNumber, spade.query.quickstep.parser.ParseExpression.ExpressionType.kOperation);
		this.subject = subject;
		this.operator = operator;
		this.operands = new ArrayList<spade.query.quickstep.parser.ParseExpression>();
	}

	public void addOperand(spade.query.quickstep.parser.ParseExpression operand)
	{
		operands.add(operand);
	}

	public spade.query.quickstep.parser.ParseExpression getSubject()
	{
		return subject;
	}

	public ParseString getOperator()
	{
		return operator;
	}

	public ArrayList<ParseExpression> getOperands()
	{
		return operands;
	}

	@Override
	public String getLabel()
	{
		return "Operation";
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
		if(subject != null)
		{
			non_container_child_field_names.add("subject");
			non_container_child_fields.add(subject);
		}
		inline_field_names.add("operator");
		inline_field_values.add(operator.getValue());
		container_child_field_names.add("operands");
		container_child_fields.add(operands);
	}
}
