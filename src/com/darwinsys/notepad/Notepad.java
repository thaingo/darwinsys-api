package notepad;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import com.darwinsys.swingui.UtilGUI;

/**
 * Simple text editor, making Swing do the work.
 */
public class Notepad {
	
	JFrame jf;
	
	JTextArea ta;

	private static List<Notepad> windows = new ArrayList<Notepad>();
	
	public Notepad() {
		jf = new JFrame();
		jf.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeThisWindow();
			}		
		});

		ta = new JTextArea(30,70);
		jf.setContentPane(ta);
		jf.pack();
		
		createMenus();
		
		UtilGUI.centre(jf);
		Point loc = jf.getLocation();
		synchronized(windows) {
			int windowsCreated = windows.size();
			loc.x += windowsCreated * 20;
			loc.y += windowsCreated * 20;		
			windows.add(this);
		}
		jf.setLocation(loc);
		jf.setVisible(true);
	}
	
	private void closeThisWindow() {
		if (!okToClose()) {
			return;
		}
		jf.setVisible(false);
		jf.dispose();
		synchronized(windows) {
			windows.remove(this);
			if (windows.size() == 0) {
				System.exit(0);
			}
		}
	}
	
	Action openAction = new OpenAction();
	class OpenAction extends AbstractAction {
		private static final long serialVersionUID = 134810980912890L;
		OpenAction() {
			super("Open");
		}
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(jf, 
				"OPEN");
		}		
	};
	Action newAction = new NewAction();
	class NewAction extends AbstractAction {
		private static final long serialVersionUID = 134810980912890L;
		NewAction() {
			super("New");
		}
		public void actionPerformed(ActionEvent e) {
			new Notepad();
		}		
	};
	Action saveAction = new SaveAction();
	class SaveAction extends AbstractAction {
		private static final long serialVersionUID = 134810980912890L;
		SaveAction() {
			super("Save");
		}
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(jf, 
				"SAVE");
		}		
	};
	Action closeAction = new CloseAction();
	class CloseAction extends AbstractAction {
		private static final long serialVersionUID = -5036606682572726640L;
		CloseAction() {
			super("Close");
		}
		public void actionPerformed(ActionEvent e) {
			closeThisWindow();
		}		
	};
	Action printAction = new PrintAction();
	class PrintAction extends AbstractAction {
		private static final long serialVersionUID = 42434480912890L;
		PrintAction() {
			super("Print");
		}
		public void actionPerformed(ActionEvent e) {
			try {
				doPrint();
			} catch (IOException e1) {
				error("Print failure", e1);
			} catch (PrintException e1) {
				error("Print failure", e1);
			}
		}
	};
	
	private void error(String message, Exception e) {
		JOptionPane.showMessageDialog(jf, message + "\n" + e);
		e.printStackTrace();
	}	

	Action exitAction = new ExitAction();
	class ExitAction extends AbstractAction {
		private static final long serialVersionUID = 31310980912890L;
		ExitAction() {
			super("Exit");
		}
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}		
	};

	Action helpAboutAction = new HelpAboutAction();
	class HelpAboutAction extends AbstractAction {
		HelpAboutAction() {
			super("About");
		}
		private static final long serialVersionUID = 20191980912890L;
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(jf, 
				"Notepad 0.0");
		}		
	};

	private void createMenus() {
		JMenuBar mb = new JMenuBar();
		/** File, Help */
		JMenu fm, em, hm;
		
		jf.setJMenuBar(mb);

		// The File Menu...
		fm = new JMenu("File");
		fm.add(openAction);
		fm.add(closeAction);
		fm.add(newAction);
		fm.addSeparator();
		fm.add(printAction);
		fm.addSeparator();
		fm.add(exitAction);
		mb.add(fm);

		// The Edit Menu...
		em = new JMenu("Edit");
		em.add(new JMenuItem("Cut"));
		em.add(new JMenuItem("Copy"));
		em.add(new JMenuItem("Paste"));
		em.addSeparator();
		JMenuItem insertMenu = new JMenu("Insert");
		em.add(insertMenu);
		insertMenu.add(new JMenuItem("Date"));
		insertMenu.add(new JMenuItem(".signature"));
		insertMenu.add(new JMenuItem("File..."));
		mb.add(em);
		
		// The Help Menu...
		hm = new JMenu("Help");
		hm.add(helpAboutAction);
		mb.add(hm);
	}

	private boolean okToClose() {
		// TODO if unsaved changes
		// confirm via JOptionPane
		return true;
	}
	
	/** Print a file by name 
	 * @throws IOException
	 * @throws PrintException 
	 */
	public void doPrint() throws IOException, PrintException {
	
		System.out.println("Printing ");
		DocFlavor flavor = DocFlavor.CHAR_ARRAY.TEXT_PLAIN;
		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
		//aset.add(MediaSizeName.NA_LETTER);
		PrintService[] pservices = PrintServiceLookup.lookupPrintServices(
				flavor, aset);
		int i;
		switch(pservices.length) {
		case 0:
			JOptionPane.showMessageDialog(jf,
					"Error: No PrintService Found", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		case 1:
			i = 1;
			break;
		default:
			i = JOptionPane.showOptionDialog(jf, 
					"Pick a printer", "Choice", 
					JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, 
					null, pservices, pservices[0]);
			break;
		}
		DocPrintJob pj = pservices[i].createPrintJob();
		Doc doc = new MyDocFlavor(flavor);

		pj.print(doc, aset);

	}
	
	/**
	 * Simple holder for document flavor.
	 */
	final class MyDocFlavor implements Doc {
		
		private DocFlavor flavor;
		public MyDocFlavor(DocFlavor flavor) {
			this.flavor = flavor;
		}

		public DocFlavor getDocFlavor() {
			return flavor;
		}

		public Object getPrintData() throws IOException {
			return ta.getText();
		}

		public DocAttributeSet getAttributes() {
			return null;
		}

		public Reader getReaderForText() throws IOException {
			return new StringReader(ta.getText());
		}

		public InputStream getStreamForBytes() throws IOException {
			return null;
		}
	}	
	
	public void doLoad(String fileName) throws IOException {		
		if (fileName == null) {
			throw new NullPointerException("filename is null");
		}
		BufferedReader is = new BufferedReader(new FileReader(fileName));
		String line;
		while ((line = is.readLine()) != null) {
			ta.append(line);
			ta.append("\n");
		}
		is.close();		
	}
}
