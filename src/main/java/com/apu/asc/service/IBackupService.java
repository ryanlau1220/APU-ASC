package com.apu.asc.service;

import java.io.IOException;
import java.nio.file.Path;

public interface IBackupService {
  public Path backup() throws IOException;
  public void restore(Path zipPath) throws IOException;
}
