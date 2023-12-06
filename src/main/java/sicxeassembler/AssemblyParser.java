package sicxeassembler;

import java.text.ParseException;
import java.util.Objects;
import java.util.Set;

/** Parses individual lines of assembly code. */
public class AssemblyParser {
  private final OpTable opTable;
  private final Set<String> directives;
  private final Set<String> allowedArgFlags = Set.of("@", "#");
  private final String extFlagSymbol = "+";

  /**
   * Creates a new AssemblyParser
   *
   * @param opTable Table of valid instructions
   * @param directives Set of valid directives
   */
  public AssemblyParser(OpTable opTable, Set<String> directives) {
    this.opTable = Objects.requireNonNull(opTable);
    this.directives = Objects.requireNonNull(directives);
  }

  /**
   * Parses the input.
   *
   * @param source The source string to parse
   * @return Parsed source object
   * @throws ParseException if there was an error in parsing.
   */
  public SourceLine parse(String source) throws ParseException {
    // First, lets get rid of any blank lines
    if (source.isBlank()) {
      return new SourceLine.Empty();
    }

    // split around the first occurrence of "." surrounded on both sides by any amount of same-line
    // whitespace or the start or end of the line
    var codeAndComment = source.split("(^|\\h+)\\.($|\\h+)", 2);
    var code = codeAndComment[0];
    var comment = "";
    if (codeAndComment.length > 1) {
      comment = codeAndComment[1];
    }
    // Handle comment-only lines
    if (code.isBlank()) {
      return new SourceLine.Comment(comment);
    }

    // code[0]: label
    // code[1]: mnemonic or directive
    // code[2]: args

    // Split code around whitespace
    var fields = code.split("[\t ]+");

    String label = "";
    boolean extFlag = false;
    String mnemonic = "";
    String args = "";

    // this check should be redundant since we know the line is not blank
    if (fields.length > 0) {
      label = fields[0];
    }
    if (fields.length > 1) {
      mnemonic = fields[1];
      if (mnemonic.startsWith(extFlagSymbol)) {
        extFlag = true;
        mnemonic = mnemonic.substring(extFlagSymbol.length());
      }
    }
    if (fields.length > 2) {
      args = fields[2];
    }

    // Now parse the args
    String argFlag = "";
    String argOne = "";
    String argTwo = "";
    if (!args.isEmpty()) {
      var splitArgs = args.split(",");
      argOne = splitArgs[0];
      if (splitArgs.length > 1) {
        // Make sure argOne actually has something (in the case of args = ",argTwo")
        if (argOne.isEmpty()) {
          throw new ParseException("Missing argument before ','", source.indexOf(args));
        }
        argTwo = splitArgs[1];
      }
      // handle arg flag if it exists
      if (allowedArgFlags.contains(String.valueOf(argOne.charAt(0)))) {
        argFlag = String.valueOf(argOne.charAt(0));
        argOne = argOne.substring(1);
        // Make sure argOne doesn't just contain the arg flag
        if (argOne.isEmpty()) {
          throw new ParseException(
              "Flag '" + argFlag + "' found without argument.", source.indexOf(args));
        }
      }
    }

    // Handle final errors and return
    // This could be moved to pass one if desired
    if (opTable.contains(mnemonic)) {
      // extended format only allowed on 3/4 instructions
      if (extFlag && !opTable.get(mnemonic).get().format().equals(Operation.Format.THREE_FOUR)) {
        throw new ParseException(
            "Extended format not allowed on instruction '" + mnemonic + "'",
            source.indexOf(mnemonic) - 1);
      }
      return new SourceLine.Instruction(label, extFlag, mnemonic, argFlag, argOne, argTwo, comment);
    } else if (directives.contains(mnemonic)) {
      if (!argTwo.isEmpty()) {
        throw new ParseException(
            "Invalid operand for directive '"
                + mnemonic
                + "': '"
                + argTwo
                + "'. Directives may not have 2 operands.",
            source.indexOf(argTwo));
      }
      if (extFlag) {
        throw new ParseException(
            "Extended format not allowed on directives.", source.indexOf(mnemonic) - 1);
      }
      if (!argFlag.isEmpty()) {
        throw new ParseException(
            "Operand flags not allowed for directives.", source.indexOf(argFlag));
      }
      return new SourceLine.Directive(label, mnemonic, argOne, comment);
    } else {
      throw new ParseException(
          "Invalid mnemonic or directive: '" + mnemonic + "'", source.indexOf(mnemonic));
    }
  }
}
