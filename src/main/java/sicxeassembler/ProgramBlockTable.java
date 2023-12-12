package sicxeassembler;

import java.util.*;
import java.util.stream.Collectors;

public class ProgramBlockTable {
  private final Map<String, ProgramBlock> blocksNew = new LinkedHashMap<>();

  /** Maps program block name to block ID */
  private final Map<String, Integer> blockIds = new LinkedHashMap<>();

  /** List of block counters- index is block ID */
  private final List<Integer> blocks = new ArrayList<>();

  public ProgramBlock getBlock(String blockName) {
    return blocksNew.computeIfAbsent(blockName, (name) -> new ProgramBlock(blocksNew.size(), name));
  }

  /**
   * Adds count to the counter for the given block, and returns the previously stored value. If the
   * table does not exist yet, this function will return 0, and the counter will be set to count.
   *
   * @param block The program block to get and add.
   * @param count Count to add to counter for the given program block
   * @return Counter value before adding count
   */
  @Deprecated
  int getAndAddCount(String block, int count) {
    var blockO = getBlock(block);
    // If the program block doesn't exist yet, start at 0
    return blockO.getAndAdd(count);
  }

  /**
   * Gets the block id. If the block is not in the table, the block is added.
   *
   * @param name The block name
   * @return The block number.
   */
  @Deprecated
  int getBlockId(String name) {
    return getBlock(name).getId();
  }

  /**
   * Returns the current value of the counter for the given program block. Returns 0 if block does
   * not exist.
   *
   * @param block the program block
   * @return Current value of the counter for the given program block.
   */
  @Deprecated
  int getCount(String block) {
    return getBlock(block).get();
  }

  void set(String block, int value) {
    getBlock(block).set(value);
  }

  /**
   * Returns the backing map of block names to block counts
   *
   * @return the map
   */
  Map<String, Integer> getMap() {
    return blocksNew.entrySet().stream()
        .collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue().get()));
  }

  /**
   * Calculates the starting location of each program block and returns the results as a Map
   *
   * @return List of starting locations of each block
   */
  List<Integer> makeAbsolutePositions() {
    int counter = 0;
    var temp = new ArrayList<Integer>();
    for (var entry : blocksNew.values()) {
      // Block starts at current location
      temp.add(counter);
      // Add length of block
      counter += entry.get();
    }
    return temp;
  }
}
