package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;

public class CreateEvaluateQuery extends InstructionFactory
{
	private String query;

	public CreateEvaluateQuery(String query)
	{
		super();
		this.query = query;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.EvaluateQuery(query);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.EvaluateQuery(query);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.EvaluateQuery(query);
		}

		return instruction;
	}
}
