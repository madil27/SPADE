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

import java.util.ArrayList;

import spade.query.graph.execution.ExecutionContext;
import spade.query.graph.kernel.Environment;
import spade.query.graph.utility.TreeStringSerializable;

/**
 * Evaluate an arbitrary SQL query (that is supported by Quickstep).
 */
public class EvaluateQuery extends Instruction
{
    private String sqlQuery;

    public EvaluateQuery(String sqlQuery)
    {
        this.sqlQuery = sqlQuery;
    }

    @Override
    public String getLabel()
    {
        return "EvaluateQuery";
    }

    @Override
    public void execute(Environment env, ExecutionContext ctx)
    {
        ctx.getExecutor().executeQuery(sqlQuery);
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
        inline_field_names.add("sqlQuery");
        inline_field_values.add(sqlQuery);
    }
}
