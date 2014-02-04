package com.grailbox.jarsearch;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.swt.widgets.Display;

public class Searcher implements Runnable {
  private JarSearch jarSearch;
  private volatile boolean isStopped;

  public Searcher() {
    this(null);
  }

  public Searcher(JarSearch jarSearch) {
    this.jarSearch = jarSearch;
  }

  public void stop() {
    isStopped = true;
  }

  @Override
  public void run() {
    if (jarSearch == null) {
      updateStatus("Jar Search must be specified");
    } else if (jarSearch.className == null || jarSearch.className.length() == 0) {
      updateStatus("Class name to search for must be specified");
    } else if (jarSearch.fileOrFolder == null
        || jarSearch.fileOrFolder.length() == 0) {
      updateStatus("File or folder to search in must be specified");
    } else {
      File file = new File(jarSearch.fileOrFolder);
      if (!file.exists()) {
        updateStatus("File or folder " + jarSearch.fileOrFolder
            + " does not exist");
      } else {
        if (file.isDirectory()) {
          searchDirectory(file);
        } else {
          searchFile(file);
        }
        updateStatus("Done");
      }
    }
    updateButton();
  }

  protected void searchDirectory(File directory) {
    if (!isStopped) {
      try {
        updateStatus("Searching directory " + directory.getCanonicalPath());
      } catch (IOException e) {
        updateStatus("Searching some directory");
      }
      File[] matches = directory.listFiles(new FileFilter() {
        @Override
        public boolean accept(File file) {
          String name = null;
          try {
            name = file.getCanonicalPath();
          } catch (IOException e) {
            name = "";
          }
          return file.isDirectory() || name.endsWith(".jar")
              || name.endsWith(".zip");
        }
      });

      for (File file : matches) {
        if (isStopped) {
          break;
        } else if (file.isDirectory()) {
          searchDirectory(file);
        } else {
          searchFile(file);
        }
      }
    }
  }

  protected void searchFile(File file) {
    if (!isStopped) {
      try {
        updateStatus("Searching file " + file.getCanonicalPath());
      } catch (IOException e) {
        updateStatus("Searching some file");
      }
      try {
        ZipFile zip = new ZipFile(file);
        for (Enumeration<? extends ZipEntry> files = zip.entries(); files
            .hasMoreElements() && !isStopped;) {
          ZipEntry entry = files.nextElement();
          if (entry.getName().indexOf(jarSearch.className) > -1) {
            jarSearch.output.add(entry.getName());
          }
        }
      } catch (Exception e) {
        String name = null;
        try {
          name = file.getCanonicalPath();
        } catch (IOException exception) {
          name = "Unknown file";
        }
        updateStatus(name + " : " + e.getLocalizedMessage());
      }
    }
  }

  protected void updateDisplay(Runnable runnable) {
    final Display display = Display.getDefault();
    if (!display.isDisposed() && !isStopped) {
      display.asyncExec(runnable);
    }
  }

  protected void updateStatus(final String message) {
    updateDisplay(new Runnable() {
      @Override
      public void run() {
        if (!isStopped)
          jarSearch.status.setText(message);
      }
    });
  }

  protected void removeResults() {
    updateDisplay(new Runnable() {
      @Override
      public void run() {
        if (!isStopped)
          jarSearch.output.removeAll();
      }
    });
  }

  protected void addResult(final String result) {
    updateDisplay(new Runnable() {
      @Override
      public void run() {
        if (!isStopped)
          jarSearch.output.add(result);
      }
    });
  }

  protected void updateButton() {
    final Display display = Display.getDefault();
    if (!display.isDisposed()) {
      display.asyncExec(new Runnable() {
        @Override
        public void run() {
          jarSearch.searchButton.setText("Search");
          jarSearch.searchButton.setEnabled(true);
          if (isStopped)
            jarSearch.status.setText("Cancelled");
        }
      });
    }
  }

}
