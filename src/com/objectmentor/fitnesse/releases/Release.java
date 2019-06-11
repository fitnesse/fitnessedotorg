package com.objectmentor.fitnesse.releases;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Release {
  private static final File releaseHome = new File("releases");

  private final File releaseDir;
  private final Map<String, ReleaseFile> releaseFiles;
  private final File infoFile;
  private final String name;

  public Release(String name) throws IOException {
    this.name = name;
    releaseDir = new File(releaseHome, name);
    infoFile = new File(releaseDir, ".releaseInfo");
    releaseFiles = new HashMap<>(4);
    if (exists())
      load();
  }

  public String getName() {
    return name;
  }

  public boolean exists() {
    return releaseDir.exists();
  }

  private void load() throws IOException {
    loadRecordedFiles();
    loadLocalFiles();
  }

  private void loadLocalFiles() {
    for (File file : releaseDir.listFiles()) {
      String filename = file.getName();
      if (!releaseFiles.containsKey(filename) && !filename.startsWith(".") &&
        !file.isDirectory()) {
        releaseFiles.put(filename, new ReleaseFile(file.getAbsolutePath()));
      }
    }
  }

  private void loadRecordedFiles() throws IOException {
    if (infoFile.exists()) {
      for (String row : Files.readAllLines(infoFile.toPath())) {
        ReleaseFile releaseFile = ReleaseFile.parse(
          releaseDir.getAbsolutePath(), row
        );
        if (releaseFile.exists())
          releaseFiles.put(releaseFile.getFilename(), releaseFile);
      }
    }
  }

  public int fileCount() {
    return releaseFiles.size();
  }

  public void saveInfo()  {
	try (FileWriter writer = new FileWriter(infoFile)){
		for (Iterator iterator = getFiles().iterator(); iterator.hasNext();)
		  writer.write(iterator.next().toString() + "\n");
		writer.flush();
	} catch (IOException e) {
		e.printStackTrace();
	}
  }

  public List<ReleaseFile> getFiles() {
    LinkedList<ReleaseFile> files = new LinkedList<>(releaseFiles.values());
    Collections.sort(files);
    return files;
  }

  public ReleaseFile getFile(String filename) {
    return releaseFiles.get(filename);
  }

  public boolean isCorrupted()  {
    try {
		if (infoFile == null)
		  return true;
		else if (!infoFile.exists())
		  return false;

        List<String> fileContent = Files.readAllLines(infoFile.toPath());
        if (fileContent.size() == 1 && fileContent.get(0).equals(""))
		  return true;
	} catch (IOException e) {
		return false;
	}

    return false;
  }
}
