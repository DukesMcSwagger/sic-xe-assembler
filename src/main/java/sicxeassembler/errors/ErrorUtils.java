package sicxeassembler.errors;

import java.text.ParseException;

public class ErrorUtils {
  /**
   * Prints a formatted error
   *
   * @param fileName The file the error is from
   * @param lineNumber The line number the error occurs on
   * @param inputString The specific text that caused the error
   * @param error The ParseException that contains the message and location
   */
  public static void error(
      String fileName, int lineNumber, String inputString, ParseException error) {
    String output =
        fileName
            + ":"
            + lineNumber
            + ":"
            + "error: "
            + error.getMessage()
            + "\n"
            + inputString
            + "\n"
            + " ".repeat(Math.max(0, error.getErrorOffset()))
            + "^";
    System.err.println(output);
  }

  public static void error(String fileName, AssemblerException error) {
    String output =
        fileName
            + ":"
            + error.getLineNumber()
            + ":error: "
            + error.getMessage()
            + "\n"
            + error.getCauseLine();
    System.err.println(output);
  }
}
