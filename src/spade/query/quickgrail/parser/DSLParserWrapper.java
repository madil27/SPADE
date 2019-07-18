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
package spade.query.quickgrail.parser;

import java.io.IOException;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import spade.query.quickgrail.parser.DSLLexer;
import spade.query.quickgrail.parser.DSLParser;
import spade.query.quickgrail.parser.ParseProgram;

public class DSLParserWrapper {
    public spade.query.quickgrail.parser.ParseProgram fromText(String text)
    {
    CharStream input = CharStreams.fromString(text);
    return fromCharStream(input);
  }

    public spade.query.quickgrail.parser.ParseProgram fromFile(String filename) throws IOException
    {
    CharStream input = CharStreams.fromFileName(filename);
    return fromCharStream(input);
  }

    public spade.query.quickgrail.parser.ParseProgram fromStdin() throws IOException
    {
    CharStream input = CharStreams.fromStream(System.in);
    return fromCharStream(input);
  }

  private ParseProgram fromCharStream(CharStream input) {
      spade.query.quickgrail.parser.DSLLexer lexer = new DSLLexer(input);
      spade.query.quickgrail.parser.DSLParser parser = new DSLParser(new CommonTokenStream(lexer));
    parser.setErrorHandler(new BailErrorStrategy());
    return parser.program().r;
  }
}
