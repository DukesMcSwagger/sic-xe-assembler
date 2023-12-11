package sicxeassembler.output;

import sicxeassembler.AssemblyPair;
import sicxeassembler.ObjectCode;

/**
 * Outputs assembled code. {@link #initialize(String, int, int)} will be called to prepare the
 * generator for output. Then, {@link #accept(ObjectCode)} will be called repeatedly as many times
 * as needed. Finally, {@link #finalizeOutput()} will be called to allow for cleanup or other
 * post-output tasks.
 */
public interface OutputGenerator {
  /**
   * Initializes the generator. Called once before
   *
   * @param name
   * @param start
   * @param length
   */
  void initialize(String name, int start, int length);

  /**
   * Accept more output.
   *
   * @param output the ObjectCode to output
   */
  void accept(AssemblyPair output);

  /** Finish and clean up. Called once after all code has been accepted. */
  void finalizeOutput();
}
