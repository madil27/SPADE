package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;

import java.util.ArrayList;

public class CreateInsertLiteralVertex extends InstructionFactory
{
	private Graph targetGraph;
	private ArrayList<String> vertices;

	public CreateInsertLiteralVertex(Graph targetGraph, ArrayList<String> vertices)
	{
		super();
		this.targetGraph = targetGraph;
		this.vertices = vertices;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.InsertLiteralVertex((Neo4jGraph) targetGraph, vertices);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.InsertLiteralVertex((PostgreSQLGraph) targetGraph, vertices);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.InsertLiteralVertex((QuickstepGraph) targetGraph, vertices);
		}

		return instruction;
	}
}
