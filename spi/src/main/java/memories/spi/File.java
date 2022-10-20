package memories.spi;

public interface File {

  /**
   * Map specified seek of file into memory,
   *
   * @param offset offset.
   * @param length length.
   * @return returns mapped {@link Memory}.
   */
  MappedMemory map(long offset, long length);

  /**
   * Get file details.
   *
   * @return returns file details.
   */
  Status status();

  /** Close file handler. */
  void close();

  /** File details. */
  interface Status {

    /**
     * Get file size in bytes.
     *
     * @return returns file size in bytes.
     */
    long size();
  }
}
