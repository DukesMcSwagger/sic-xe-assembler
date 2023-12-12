package sicxeassembler;

public class ProgramBlock {
  private final int id;
  private final String name;
  private int counter = 0;

  public ProgramBlock(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int get() {
    return counter;
  }

  public void set(int value) {
    counter = value;
  }

  /**
   * Adds count to the counter for this block, and returns the previously stored value.
   *
   * @param count Count to add to counter for the given program block
   * @return Counter value before adding count
   */
  public int getAndAdd(int count) {
    int prevCount = counter;
    counter += count;
    return prevCount;
  }

  @Override
  public String toString() {
    return "ProgramBlock{" + "id=" + id + ", name='" + name + '\'' + ", counter=" + counter + '}';
  }
}
