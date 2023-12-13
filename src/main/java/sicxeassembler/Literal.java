package sicxeassembler;

public class Literal {
  private int[] data;
  private int address = -1;
  private int block;

  public Literal(int[] data) {
    this.data = data;
  }

  public int[] getData() {
    return data;
  }

  public void setData(int[] data) {
    this.data = data;
  }

  public int getAddress() {
    return address;
  }

  public void setAddress(int address) {
    this.address = address;
  }

  public int getBlock() {
    return block;
  }

  public void setBlock(int block) {
    this.block = block;
  }

  public int getSize() {
    return data.length;
  }
}
