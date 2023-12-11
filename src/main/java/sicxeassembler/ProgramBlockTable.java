package sicxeassembler;

import java.util.*;

public class ProgramBlockTable {
  /** Maps program block name to block ID */
  private final Map<String, Integer> blockIds = new LinkedHashMap<>();

  /** List of block counters- index is block ID */
  private final List<Integer> blocks = new ArrayList<>();

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
    set(block, current + count);
    return current;
  }

  /**
   * Gets the block id. If the block is not in the table, the block is added.
   *
   * @param name The block name
   * @return The block number.
   */
  int getBlockId(String name) {
    return blockIds.computeIfAbsent(
        name,
        (unused) -> {
          blocks.add(0);
          return blocks.size() - 1;
        });
  }

  /**
   * Returns the current value of the counter for the given program block. Returns 0 if block does
   * not exist.
   *
   * @param block the program block
   * @return Current value of the counter for the given program block.
   */
  int get(String block) {
    return blocks.get(getBlockId(block));
  }

  void set(String block, int value) {
    blocks.set(getBlockId(block), value);
  }

  /**
   * Returns the backing map of block names to block IDs
   *
   * @return the map
   */
  Map<String, Integer> getMap() {
    return blockIds;
  }

  /**
   * Calculates the starting location of each program block and returns the results as a Map
   *
   * @return List of starting locations of each block
   */
  List<Integer> makeAbsolutePositions() {
    int counter = 0;
    var temp = new ArrayList<Integer>();
    for (var entry : blocks) {
      // Block starts at current location
      temp.add(counter);
      // Add length of block
      counter += entry;
    }
    return temp;
  }
}
