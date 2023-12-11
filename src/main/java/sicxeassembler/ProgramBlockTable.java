package sicxeassembler;

import java.util.*;

public class ProgramBlockTable {
  /** Maps program block name with current location counter */
  private final Map<String, Integer> blocks = new LinkedHashMap<>();

  /**
   * Adds count to the counter for the given block, and returns the previously stored value. If the
   * table does not exist yet, this function will return 0, and the counter will be set to count.
   *
   * @param block The program block to get and add.
   * @param count Count to add to counter for the given program block
   * @return Counter value before adding count
   */
  int getAndAdd(String block, int count) {
    // If the program block doesn't exist yet, start at 0
    int current = get(block);
    blocks.put(block, current + count);
    return current;
  }

  /**
   * Returns the current value of the counter for the given program block. Returns 0 if block does
   * not exist.
   *
   * @param block the program block
   * @return Current value of the counter for the given program block.
   */
  int get(String block) {
    return blocks.getOrDefault(block, 0);
  }

  Set<String> getTables() {
    return Collections.unmodifiableSet(blocks.keySet());
  }

  /**
   * Returns the backing map
   *
   * @return the map
   */
  Map<String, Integer> getMap() {
    return blocks;
  }

  /**
   * Calculates the starting location of each program block and returns the results as a Map
   *
   * @return Map of program block names to starting locations
   */
  Map<String, Integer> makeAbsolutePositions() {
    int counter = 0;
    var temp = new HashMap<String, Integer>();
    for (var entry : blocks.keySet()) {
      temp.put(entry, counter);
      counter += blocks.get(entry);
    }
    return temp;
  }
}
