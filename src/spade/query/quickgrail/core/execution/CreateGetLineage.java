package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.entities.Graph.Direction;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;

public class CreateGetLineage extends InstructionFactory
{
	private Graph targetGraph;
	private Graph subjectGraph;
	private Graph startGraph;
	private Integer depth;
	private Direction direction;

	public CreateGetLineage(Graph targetGraph, Graph subjectGraph,
							Graph startGraph, Integer depth, Direction direction)
	{
		super();
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.startGraph = startGraph;
		this.depth = depth;
		this.direction = direction;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.GetLineage((Neo4jGraph) targetGraph,
					(Neo4jGraph) subjectGraph, (Neo4jGraph) startGraph, depth, direction);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.GetLineage((PostgreSQLGraph) targetGraph,
					(PostgreSQLGraph) subjectGraph, (PostgreSQLGraph) startGraph, depth, direction);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.GetLineage((QuickstepGraph) targetGraph,
					(QuickstepGraph) subjectGraph, (QuickstepGraph) startGraph, depth, direction);
		}

		return instruction;
	}
}
