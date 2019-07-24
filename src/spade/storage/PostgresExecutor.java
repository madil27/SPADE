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
package spade.storage;

import com.mysql.jdbc.StringUtils;
import org.postgresql.copy.PGCopyInputStream;
import spade.core.AbstractStorage;

import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spade.core.AbstractQuery.getCurrentStorage;

/**
 * Helper class for encapsulation of related methods and asynchronous execution.
 */
public class PostgresExecutor
{
	private static final Logger logger = Logger.getLogger(PostgresExecutor.class.getName());

    /**
     * Submit a query and wait for the result.
     */
	public ResultSet executeQuery(String query)
    {
        AbstractStorage currentStorage = getCurrentStorage();
		return ((PostgreSQL) currentStorage).executeQuery(query.trim());
    }

    /**
	 * Submit a copy query and wait for the result.
     */
	public String executeCopy(String query)
    {
		if(!StringUtils.isNullOrEmpty(query))
		{
			AbstractStorage currentStorage = getCurrentStorage();
			String result = ((PostgreSQL) currentStorage).executeCopy(query.trim());
			if(result != null)
				return result.trim();
        }
		return null;
    }

    /**
     * Submit a query and cast result as long type.
     */
    public long executeQueryForLongResult(String query)
    {
		String resultStr = executeCopy(query);
        try
        {
            return Long.parseLong(resultStr);
        }
		catch(Exception ex)
        {
            throw new RuntimeException(
					"Unexpected result \"" + resultStr + "\" from Postgres: expecting a long value");
		}
    }
}
