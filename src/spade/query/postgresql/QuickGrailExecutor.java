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
package spade.query.postgresql;

import org.apache.commons.lang3.exception.ExceptionUtils;
import spade.core.AbstractQuery;
import spade.query.postgresql.kernel.Environment;
import spade.query.postgresql.kernel.Program;
import spade.query.postgresql.kernel.Resolver;
import spade.query.postgresql.parser.DSLParserWrapper;
import spade.query.postgresql.parser.ParseProgram;
import spade.storage.postgresql.PostgresExecutor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Top level class for the QuickGrail graph query executor.
 */
public class QuickGrailExecutor extends AbstractQuery<String>
{
	private final Logger logger = Logger.getLogger(QuickGrailExecutor.class.getName());
	private PostgresExecutor ps;
	private Environment env = null;

	public QuickGrailExecutor()
	{
		this.ps = new PostgresExecutor();
	}

	public void createEnvironment()
	{
		if(this.env == null)
		{
			this.env = new Environment(this.ps);
		}
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
				responses = program.execute(this.ps);
			}
			finally
			{
				this.env.gc();
			}
		}
		catch(Exception ex)
		{
			responses = new ArrayList<>();
			StringWriter stackTrace = new StringWriter();
			PrintWriter pw = new PrintWriter(stackTrace);
			pw.println("Error evaluating QuickGrail command:");
			pw.println("------------------------------------------------------------");
			// e.printStackTrace(pw);
			logger.log(Level.INFO, ExceptionUtils.getStackTrace(ex));
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
