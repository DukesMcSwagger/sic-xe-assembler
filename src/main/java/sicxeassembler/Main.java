package sicxeassembler;

import static java.lang.System.exit;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import sicxeassembler.errors.AssemblerException;
import sicxeassembler.errors.ErrorUtils;
import sicxeassembler.output.AssemblyListingGenerator;

public class Main {
  public static void main(String[] args) throws FileNotFoundException, ParseException {

    if (args.length < 1) {
      System.out.println("No file name provided!");
      exit(0);
    }
    File inputFile = new File(args[0]);
    Scanner scanner = new Scanner(inputFile);
    OpTable optable = OpTable.loadFromResourceCsv("instructions.csv");
    List<SourceLine> lines = new LinkedList<>();
    int lineNum = 1;
    while (scanner.hasNextLine()) {
      var line = scanner.nextLine();
      try {
        lines.add(SourceLine.parseLine(line));
      } catch (ParseException e) {
        ErrorUtils.error(inputFile.getName(), lineNum, line, e);
      }
      lineNum++;
    }
    PassOne passOne = new PassOne(optable, lines);
    try {
      passOne.process();
    } catch (AssemblerException e) {
      ErrorUtils.error(inputFile.getName(), e);
      System.exit(1);
    }
    PassTwo passTwo =
        new PassTwo(
            optable,
            passOne.getStartAddress(),
            passOne.getSymbolTable(),
            passOne.getLiteralTable(),
            passOne.getBlockAddresses(),
            passOne.getOutput());
    passTwo.addOutputGenerator(
        new AssemblyListingGenerator(new File(inputFile.getName().split("\\.")[0] + "_sol.txt")));
    try {
      passTwo.process();
    } catch (AssemblerException e) {
      ErrorUtils.error(inputFile.getName(), e);
      System.exit(0);
    }
  }
}
