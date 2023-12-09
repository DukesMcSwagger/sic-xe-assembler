package sicxeassembler;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class OpTable {

  private Map<String, Operation> table;

  public OpTable(Collection<Operation> instructions) {
    this(instructions.stream().collect(Collectors.toMap(Operation::mnemonic, op -> op)));
  }

  public OpTable(Map<String, Operation> instructions) {
    table = new HashMap<>(instructions);
  }

  /**
   * Determine whether the mnemonic maps to a valid operation
   *
   * @param mnemonic the mnemonic
   * @return true if the mnemonic is valid, otherwise false
   */
  public boolean contains(String mnemonic) {
    return getMap().containsKey(mnemonic);
  }

  /**
   * Get the {@link Operation} for the given mnemonic
   *
   * @param mnemonic the mnemonic
   * @return If the mnemonic is valid, an optional containing the Operation; otherwise an empty
   *     optional.
   */
  public Optional<Operation> getOptional(String mnemonic) {
    return Optional.ofNullable(getMap().get(mnemonic));
  }

  public Operation get(String mnemonic) {
    return getMap().get(mnemonic);
  }

  /**
   * Get the backing map of the OpTable.
   *
   * @return the map.
   */
  public Map<String, Operation> getMap() {
    return table;
  }

  /**
   * Constructs an operation from a string containing comma separated values. The CSV string must be
   * in the form {mnemonic}, {format}, {opcode}
   *
   * @param csvLine String containing comma separated values
   * @return
   */
  private static Operation operationFromCSVLine(String csvLine) {
    var values = csvLine.split(",");
    if (values.length < 3) {
      throw new IllegalArgumentException(
          "CSV record found with " + values.length + " entries, expected 3: '" + csvLine + "'");
    }
    Operation op;
    try {
      var mnemonic = values[0].trim();
      var format = Operation.Format.fromValue(Integer.parseInt(values[1].trim()));
      var opcode = Integer.parseInt(values[2].trim(), 16);
      op = new Operation(mnemonic, opcode, format);
    } catch (Exception e) {
      throw new RuntimeException("Could not parse CSV record '" + csvLine + "'", e);
    }
    return op;
  }

  public static OpTable loadFromResourceCsv(String resource) {
    HashMap<String, Operation> map = new HashMap<>();
    try (InputStream instream = OpTable.class.getClassLoader().getResourceAsStream(resource)) {
      if (instream == null) {
        throw new IOException("Could not open " + resource + " from resources!");
      }
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(instream))) {
        String line;
        while ((line = reader.readLine()) != null) {
          // Ignore empty or comment lines
          if (!(line.isBlank() || line.trim().startsWith("#"))) {
            Operation op = operationFromCSVLine(line);
            map.put(op.mnemonic(), op);
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return new OpTable(map);
  }
}
