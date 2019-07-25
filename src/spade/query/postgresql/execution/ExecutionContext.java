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
package spade.query.postgresql.execution;

import spade.storage.postgresql.PostgresExecutor;

import java.util.ArrayList;

/**
 * QuickGrail runtime environment.
 */
public class ExecutionContext
{
	private PostgresExecutor ps;
	private ArrayList<Object> responses;

	public ExecutionContext(PostgresExecutor ps)
	{
		this.ps = ps;
		this.responses = new ArrayList<>();
	}

	public PostgresExecutor getExecutor()
	{
		return ps;
	}

	public void addResponse(Object response)
	{
		responses.add(response);
	}

	public ArrayList<Object> getResponses()
	{
		return responses;
	}
}
