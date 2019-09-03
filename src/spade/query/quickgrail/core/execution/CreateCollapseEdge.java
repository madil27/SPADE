package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;

import java.util.ArrayList;

public class CreateCollapseEdge extends InstructionFactory
{
	private Graph targetGraph;
	private Graph sourceGraph;
	private ArrayList<String> fields;

	public CreateCollapseEdge(Graph targetGraph, Graph sourceGraph, ArrayList<String> fields)
	{
		super();
		this.targetGraph = targetGraph;
		this.sourceGraph = sourceGraph;
		this.fields = fields;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.CollapseEdge((Neo4jGraph) targetGraph,
					(Neo4jGraph) sourceGraph, fields);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.CollapseEdge((PostgreSQLGraph) targetGraph,
					(PostgreSQLGraph) sourceGraph, fields);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.CollapseEdge((QuickstepGraph) targetGraph,
					(QuickstepGraph) sourceGraph, fields);
		}

		return instruction;
	}
}
