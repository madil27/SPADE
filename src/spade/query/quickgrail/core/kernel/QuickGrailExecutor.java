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
package spade.query.quickgrail.core.kernel;

import org.apache.commons.lang3.exception.ExceptionUtils;
import spade.core.AbstractQuery;
import spade.query.quickgrail.core.parser.DSLParserWrapper;
import spade.query.quickgrail.core.parser.ParseProgram;
import spade.query.quickgrail.core.utility.StorageExecutor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Top level class for the QuickGrail graph query storageExecutor.
 */
public class QuickGrailExecutor extends AbstractQuery<String>
{
	private StorageExecutor storageExecutor;
	private AbstractEnvironment env;
	private static Logger logger = Logger.getLogger(QuickGrailExecutor.class.getName());

	public void createEnvironment(String currentStorageName)
	{
		this.storageExecutor = StorageExecutor.getExecutor(currentStorageName);
		this.env = EnvironmentFactory.createEnvironment(currentStorageName, this.storageExecutor);
	}

	@Override
	public String execute(String query)
	{
		ArrayList<Object> responses;
		try
		{
			DSLParserWrapper parserWrapper = new DSLParserWrapper();
			ParseProgram parseProgram = parserWrapper.fromText(query);

			logger.log(Level.INFO, "Parse tree:\n" + parseProgram.toString());

			Resolver resolver = new Resolver();
			Program program = resolver.resolveProgram(parseProgram, this.env);

			logger.log(Level.INFO, "Execution plan:\n" + program.toString());

			try
			{
				responses = program.execute(storageExecutor);
			}
			finally
			{
				env.gc();
			}
		}
		catch(Exception ex)
		{
			responses = new ArrayList<>();
			StringWriter stackTrace = new StringWriter();
			PrintWriter pw = new PrintWriter(stackTrace);
			pw.println("Error evaluating QuickGrail command:");
			pw.println("------------------------------------------------------------");
			logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(ex));
			pw.println(ex.getMessage());
			pw.println("------------------------------------------------------------");
			responses.add(stackTrace.toString());
		}

		if(responses == null || responses.isEmpty())
		{
			return "OK";
		}
		else
		{
			// Currently only return the last response.
			Object response = responses.get(responses.size() - 1);
			return response == null ? "" : response.toString();
		}
	}

	@Override
	public String execute(Map<String, List<String>> parameters, Integer limit)
	{
		throw new RuntimeException("Not supported");
	}
}
