package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;

import java.util.ArrayList;

public class CreateInsertLiteralEdge extends InstructionFactory
{
	private Graph targetGraph;
	private ArrayList<String> edges;

	public CreateInsertLiteralEdge(Graph targetGraph, ArrayList<String> edges)
	{
		super();
		this.targetGraph = targetGraph;
		this.edges = edges;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.InsertLiteralEdge((Neo4jGraph) targetGraph, edges);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.InsertLiteralEdge((PostgreSQLGraph) targetGraph, edges);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.InsertLiteralEdge((QuickstepGraph) targetGraph, edges);
		}

		return instruction;
	}
}
