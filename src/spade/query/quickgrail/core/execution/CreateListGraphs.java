package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;

public class CreateListGraphs extends InstructionFactory
{
	private String style;

	public CreateListGraphs(String style)
	{
		super();
		this.style = style;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.ListGraphs(style);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.ListGraphs(style);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.ListGraphs(style);
		}

		return instruction;
	}
}
