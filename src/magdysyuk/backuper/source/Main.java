package magdysyuk.backuper.source;

import java.util.Locale;

import magdysyuk.backuper.source.cli.CLIRunner;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;


public class Main {

	public static void main(String[] args) {
		/*
		 * Switch locale to english.
		 * args4j contains in *.jar localization resources for german and russian languages.
		 * In result, if you are running this program on the operation system with russian locale,
		 * language of args4j will be switched to russian, and in the console you couldn't read nothing,
		 * because there is a troubles with encoding (encoding of Windows console is Cp866),
		 * article about it (on russian): 
		 * http://skipy.ru/technics/encodings_console_comp.html
		 * After this switchng of locale, all output will be on english. 
		 */
		Locale locale = new Locale("en");
		Locale.setDefault(locale);

		CLIRunner cliRunner = new CLIRunner();
		CmdLineParser cmdLineParser = new CmdLineParser(cliRunner);
		
		try {
			cmdLineParser.parseArgument(args);
			cliRunner.runLogic();
		} catch(CmdLineException ex) {
			System.out.println(ex.getMessage());
			System.out.println();
			System.out.println("Please check that all options have acceptable values");
			System.out.println();
			cmdLineParser.printUsage(System.out);
		}
		
	}

}
