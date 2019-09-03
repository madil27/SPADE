package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.entities.Graph;
import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;
import spade.query.quickgrail.neo4j.entities.Neo4jGraph;
import spade.query.quickgrail.postgresql.entities.PostgreSQLGraph;
import spade.query.quickgrail.quickstep.entities.QuickstepGraph;

public class CreateGetVertex extends InstructionFactory
{
	private Graph targetGraph;
	private Graph subjectGraph;
	private String field;
	private String operation;
	private String value;

	public CreateGetVertex(Graph targetGraph, Graph subjectGraph, String field, String operation, String value)
	{
		super();
		this.targetGraph = targetGraph;
		this.subjectGraph = subjectGraph;
		this.field = field;
		this.operation = operation;
		this.value = value;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.GetVertex((Neo4jGraph) targetGraph,
					(Neo4jGraph) subjectGraph, field, operation, value);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.GetVertex((PostgreSQLGraph) targetGraph,
					(PostgreSQLGraph) subjectGraph, field, operation, value);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.GetVertex((QuickstepGraph) targetGraph,
					(QuickstepGraph) subjectGraph, field, operation, value);
		}

		return instruction;
	}
}
