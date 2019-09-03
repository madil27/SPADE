package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;

public class CreateCreateEmptyGraph extends InstructionFactory
{
	private Graph outputGraph;

	public CreateCreateEmptyGraph(Graph outputGraph)
	{
		super();
		this.outputGraph = outputGraph;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.CreateEmptyGraph((Neo4jGraph) outputGraph);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.CreateEmptyGraph((PostgreSQLGraph) outputGraph);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.CreateEmptyGraph((QuickstepGraph) outputGraph);
		}

		return instruction;
	}
}
