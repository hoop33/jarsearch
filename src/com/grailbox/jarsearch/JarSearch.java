package com.grailbox.jarsearch;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class JarSearch {
  public String className = "";
  public String fileOrFolder = "";
  public Button searchButton;
  public Label status;
  public List output;
  private Searcher searcher;

  public void run() {
    Display display = new Display();
    Shell shell = new Shell(display);
    shell.setText("Jar Search");
    createContents(shell);
    shell.open();
    while (!shell.isDisposed())
      if (!display.readAndDispatch())
        display.sleep();
    display.dispose();
  }

  public void completeSearch() {
    searcher = null;
  }

  protected void createContents(Shell shell) {
    GridLayout layout = new GridLayout(1, false);
    shell.setLayout(layout);
    createForm(shell);
    createButtons(shell);
    createStatus(shell);
    createOutput(shell);
    shell.pack();
  }

  protected void createForm(final Shell shell) {
    Composite composite = new Composite(shell, SWT.NONE);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    composite.setLayout(new GridLayout(6, true));

    new Label(composite, SWT.RIGHT).setText("Class Name:");
    final Text classText = new Text(composite, SWT.SINGLE | SWT.BORDER);
    classText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5,
        1));
    classText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent arg0) {
        className = classText.getText();
        searchButton.setEnabled(className.length() > 0
            && fileOrFolder.length() > 0);
      }
    });

    new Label(composite, SWT.RIGHT).setText("File or Folder:");
    final Text fileOrFolderText = new Text(composite, SWT.SINGLE | SWT.BORDER);
    fileOrFolderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
        false, 3, 1));
    fileOrFolderText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent arg0) {
        fileOrFolder = fileOrFolderText.getText();
        searchButton.setEnabled(className.length() > 0
            && fileOrFolder.length() > 0);
      }
    });

    final Button browseFile = new Button(composite, SWT.PUSH);
    browseFile.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    browseFile.setText("Browse File...");
    browseFile.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        FileDialog open = new FileDialog(shell, SWT.OPEN);
        open.setFilterNames(new String[] { "Jar files (*.jar)",
            "Zip files (*.zip)", "All Files (*)" });
        open.setFilterExtensions(new String[] { "*.jar", "*.zip", "*" });
        String file = open.open();
        if (file != null) {
          fileOrFolderText.setText(file);
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent arg0) {
        this.widgetSelected(arg0);
      }
    });

    final Button browseFolder = new Button(composite, SWT.PUSH);
    browseFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    browseFolder.setText("Browse Folder...");
    browseFolder.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        DirectoryDialog open = new DirectoryDialog(shell);
        String folder = open.open();
        if (folder != null) {
          fileOrFolderText.setText(folder);
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent arg0) {
        this.widgetSelected(arg0);
      }
    });
  }

  protected void createButtons(final Shell shell) {
    Composite composite = new Composite(shell, SWT.NONE);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    composite.setLayout(new GridLayout(1, false));

    searchButton = new Button(composite, SWT.PUSH);
    searchButton
        .setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
    searchButton.setText("Search");
    final JarSearch self = this;
    searchButton.addSelectionListener(new SelectionListener() {
      @Override
      public synchronized void widgetSelected(SelectionEvent arg0) {
        if (searcher == null) {
          searcher = new Searcher(self);
          searchButton.setText("Cancel");
          new Thread(searcher).start();
        } else {
          searchButton.setText("...");
          searchButton.setEnabled(false);
          searcher.stop();
          searcher = null;
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent arg0) {
        this.widgetSelected(arg0);
      }
    });
    searchButton.setEnabled(false);
    shell.setDefaultButton(searchButton);
  }

  protected void createStatus(final Shell shell) {
    Composite composite = new Composite(shell, SWT.NONE);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    composite.setLayout(new GridLayout(1, false));

    status = new Label(composite, SWT.NONE);
    status.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    status.setText("Ready");
  }

  protected void createOutput(final Shell shell) {
    Composite composite = new Composite(shell, SWT.NONE);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    composite.setLayout(new GridLayout(1, true));

    output = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL
        | SWT.H_SCROLL);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
    gridData.heightHint = 10 * output.getItemHeight();
    output.setLayoutData(gridData);
  }

  public static void main(String[] args) {
    new JarSearch().run();
  }
}
