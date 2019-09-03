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
package spade.query.quickgrail.core.utility;

import spade.query.quickgrail.core.kernel.QuickGrailExecutor;
import spade.storage.Quickstep;

/**
 * Interface for a storage-specific executor.
 */
public abstract class StorageExecutor
{
	private static StorageExecutor executor;

	public abstract Object executeQuery(String query);

	public static StorageExecutor getExecutor(String storageName)
	{
		if(storageName.equalsIgnoreCase("neo4j"))
		{
			executor = new spade.storage.neo4j.Neo4jExecutor();
		}
		else if(storageName.equalsIgnoreCase("postgresql"))
		{
			executor = new spade.storage.postgresql.PostgresExecutor();
		}
		else if(storageName.equalsIgnoreCase("quickstep"))
		{
			Quickstep storage = (Quickstep) QuickGrailExecutor.getCurrentStorage();
			executor = storage.getExecutor();
		}
		// add a condition above to add support for a new storage executor
		return executor;
	}
}
