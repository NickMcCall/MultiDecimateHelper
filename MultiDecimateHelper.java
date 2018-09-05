import java.util.Scanner;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.text.DefaultCaret;
import static javax.swing.ScrollPaneConstants.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;


public class MultiDecimateHelper extends JFrame{
	
	private static final long serialVersionUID = 1L;
	private final DefaultListModel listModel;
	private JList jlist;
	private int[] selection;
	protected JTextArea output;
	private Frame[] frames;
	private JButton btnOpen = new JButton("Open");
	private JButton btnSave = new JButton("Save");
	private JButton btnHelp = new JButton("Help");
	private JButton btnReload = new JButton("Reload");
	private JFileChooser fileChooser = new JFileChooser(new File("."));
	private String firstLine;
	private int oldCount;
	private int newCount;
	private String dir;
	private String path;
	private OpenFile openFile;
	private ListCellRenderer renderer = new MDHRenderer();
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				MultiDecimateHelper gui  = new MultiDecimateHelper();
				gui.setSize(445,845);
				gui.setTitle("MultiDecimate Helper");
				gui.setLocationRelativeTo(null);
				gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				gui.setVisible(true);
			}
		});
	}

	public MultiDecimateHelper() {
		
		listModel = new DefaultListModel();

		jlist = new JList(listModel);
		jlist.setCellRenderer(renderer);
		jlist.setFixedCellWidth(185);
		jlist.setVisibleRowCount(36);
		jlist.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e){
				int[] indices = jlist.getSelectedIndices();
				if(!e.getValueIsAdjusting()){
						setSelection(indices);
				}
			}
		});
		jlist.setFocusable(true);
		jlist.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent e){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					AlterFrames a = new AlterFrames();
					a.execute();
				}
			}
		});
		jlist.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(e.getClickCount() == 2){
					AlterFrames a = new AlterFrames();
					a.execute();
				}
			}				
		});
		
		JScrollPane spList = new JScrollPane(jlist);
		spList.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(spList);
		rightPanel.setBorder(new EmptyBorder(10,0,10,0));
		add(rightPanel, BorderLayout.LINE_END);
		
		output = new JTextArea(10,15);
		output.setFont(output.getFont().deriveFont(16f));
		output.setEditable(false);
		output.setWrapStyleWord(true);
		DefaultCaret caret = (DefaultCaret)output.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane scrollPane = new JScrollPane(output);
		scrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);

		btnOpen.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				output.append(open());
			}
		});
		btnReload.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				output.append(reload());
			}
		});
		btnSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				output.append(save());
			}
		});
		btnHelp.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				String msg = "<html>MultiDecimate Helper by Nicholas McCall, 2018<br><br>"
						+ "To use, run Pass 1 of MultiDecimate and then MultiDecimate.exe, as usual.<br><br>"
						+ "In MultiDecimate Helper:<br><br>"
						+ "1. Open cfile.txt.<br><br>"
						+ "2. To mark/unmark frames for deletion, double-click a frame number, or select and press ENTER.<br>"
						+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Frames in red will be deleted.<br><br>"
						+ "When Finished, save and exit MultiDecimate Helper. Then, run Pass 2 of MultiDecimate.</html>";
				JOptionPane.showMessageDialog(null, msg, "Help", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		JPanel buttonTopPanel = new JPanel();
		buttonTopPanel.setLayout(new BorderLayout());
		buttonTopPanel.setBorder(new EmptyBorder(10,0,10,0));
		buttonTopPanel.add(btnOpen, BorderLayout.LINE_START);
		buttonTopPanel.add(btnReload, BorderLayout.LINE_END);

		JPanel buttonBtmPanel = new JPanel();
		buttonBtmPanel.add(btnHelp);
		buttonBtmPanel.add(btnSave);
					
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.setBorder(new EmptyBorder(0,10,0,0));
		leftPanel.add(buttonBtmPanel, BorderLayout.PAGE_END);
		leftPanel.add(buttonTopPanel, BorderLayout.PAGE_START);
		leftPanel.add(scrollPane, BorderLayout.CENTER);			
		add(leftPanel, BorderLayout.LINE_START);
		
		pack();
	}
	
	private String open(){
		String result = "";
		if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			open(fileChooser.getSelectedFile());
		
		return result;
	}
	
	// If not all frames load, then there is an error in the cfile.txt
	private void open(File file){
		String s;
		
		if(!listModel.isEmpty())
			listModel.clear();
		try{
			dir = file.getParent();
			path = file.getPath();
			Scanner input = new Scanner(file);
			firstLine = input.nextLine();
			for(int i=0;i<4;i++){
				input.next();
			}
			s = input.next();
			s = s.substring(0,s.length()-1);
			oldCount = Integer.parseInt(s);
			newCount = 0;
			frames = new Frame[oldCount];
			input.nextLine();
			
			openFile = new OpenFile(input);
			openFile.execute();

		}
		catch(IOException e){
			JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			output.append("Open cfile failed\n");
		}		
		
		return;
	}

	private String reload(){
		File f = new File(path);
		
		open(f);
		
		return("Reload\n");
	}
	
	private String save(){
		int c, d;
		String result = "Saved\n";
		
		// Make new cfile.txt
		try{
			PrintWriter newC = new PrintWriter(dir + "\\cfile.txt");
			newC.println(firstLine);
			newC.println("#original frame count = " + oldCount + ", decimated frame count = " + newCount);
			for(int i=0;i<oldCount;i++){
				newC.println(frames[i].toString());
			}
			newC.close();
		}
		catch(FileNotFoundException e){
			JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			result = "Save failed: cfile.txt\n";
		}
		
		// make new dfile.txt
		try{
			PrintWriter newD = new PrintWriter(dir + "\\dfile.txt");
			newD.println("0 3 1 " + oldCount + " " + newCount);
			c = 0;
			d = 0;
			for(int i=0;i<oldCount;i++){
				switch(frames[i].status()){
				// good frame- assign frame number
				case 0:	newD.println(d + " " + c); // assign frame number
						c++;
						d++;
						break;
				// dupe frame- "delete" this frame
				case 1: c++;
						break;
				}
			}			
			newD.close();
		}
		catch(FileNotFoundException e){
			JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			result = "Save failed: dfile.txt\n";
		}
		
		return result;
	}
	
	private void setSelection(int[] items){
		selection = new int[items.length];
		System.arraycopy(items, 0, selection, 0, items.length);
	}
	
	private class AlterFrames extends SwingWorker<String, String>{
		private String s = "";
		private String g;
		private String result = "";
		
		AlterFrames(){
		}
		
		protected String doInBackground(){
			for(int i=0;i<selection.length;i++){
				switch(frames[selection[i]].status()){
					case 0: frames[selection[i]].setStatus(1);
							s += "Delete " + frames[selection[i]].number() + "\n";
							publish(s);
							s = "";
							g = frames[selection[i]].toString();
							listModel.set(selection[i], g);
							newCount--;
							break;
					case 1: frames[selection[i]].setStatus(0);
							s += "Keep " + frames[selection[i]].number() + "\n";
							publish(s);
							s = "";
							g = frames[selection[i]].toString();
							listModel.set(selection[i], g);
							newCount++;
							break;
				}
			}
			return null;
		}
		
		protected void process(List<String> chunks){
			for(String text : chunks){
				result += text;
			}
		}
		
		protected void done(){
			output.append(result);
		}
	}
	
	private class OpenFile extends SwingWorker<String, String>{
		private Scanner input;
		
		OpenFile(Scanner in){
			input = in;
		}
		
		@Override
		protected String doInBackground() throws Exception {
			output.append("Loading...\n");
			// Read cfile into array and make JLabels
			for(int i=0;i<oldCount;i++){
				frames[i] = new Frame();
				frames[i].setFrame(Integer.parseInt(input.next()));
				frames[i].setStatus(Integer.parseInt(input.next()));
				frames[i].setDiff(Float.parseFloat(input.next()));
				String g = frames[i].toString();
				publish(g);
				if(frames[i].status() == 0)
					newCount++;
			}
			return null;
		}
		
		@Override
        protected void process(List<String> chunks) {
            for (String text : chunks) {
                listModel.addElement(text);
            }
        }
		
		@Override
		protected void done(){
			output.append("Opened cfile\n");
		}
		
	}
	
	private class MDHRenderer extends JLabel implements ListCellRenderer{
		private static final long serialVersionUID = 1L;
		Border lineBorder = BorderFactory.createLineBorder(Color.BLACK, 3);
	    Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
	    
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			setFont(jlist.getFont().deriveFont(16f));
			setText(value.toString());
			
			if(isSelected){
				if(frames[index].status() == 1)
					setBackground(Color.PINK);
				else
					setBackground(Color.GRAY);
			}
			else if(frames[index].status() == 1)
				setBackground(Color.RED);
			else
				setBackground(Color.WHITE);
			
			setOpaque(true);
			setBorder(cellHasFocus ? lineBorder : emptyBorder);
			
			return this;
		}		
	}
}
