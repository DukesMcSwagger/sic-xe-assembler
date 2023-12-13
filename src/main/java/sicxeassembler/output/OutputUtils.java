package sicxeassembler.output;

public class OutputUtils {
  public static String bytesToHexString(int[] bytes) {
    if (bytes == null) return "";
    StringBuilder builder = new StringBuilder();
    for (var b : bytes) {
      b &= 0xff;
      builder.append(Integer.toHexString(b).toUpperCase());
    }
    return builder.toString();
  }
}
