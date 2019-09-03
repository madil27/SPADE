package spade.query.quickgrail.core.execution;

import spade.query.quickgrail.core.kernel.Instruction;
import spade.query.quickgrail.core.kernel.InstructionFactory;

import java.util.List;

public class CreateEraseSymbols extends InstructionFactory
{
	private List<String> symbols;

	public CreateEraseSymbols(List<String> symbols)
	{
		super();
		this.symbols = symbols;
	}

	@Override
	public Instruction createInstruction()
	{
		Instruction instruction = null;
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			instruction = new spade.query.quickgrail.neo4j.execution.EraseSymbols(symbols);
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			instruction = new spade.query.quickgrail.postgresql.execution.EraseSymbols(symbols);
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			instruction = new spade.query.quickgrail.quickstep.execution.EraseSymbols(symbols);
		}

		return instruction;
	}
}
