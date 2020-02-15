package solver.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class PatheryUI {

    
	
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		
		PTabbedPane tabbedPane = new PTabbedPane();
		
		tabbedPane.setPreferredSize(new Dimension(1400, 750));
		
		frame.add(tabbedPane);
		
		
		tabbedPane.initTabsWithCurrentDate();
		
		JPanel datePanel = new JPanel(new GridLayout(1, 0));
		datePanel.setPreferredSize(new Dimension(200, 25));
		datePanel.setMaximumSize(datePanel.getPreferredSize());
		
		JFormattedTextField textField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
		textField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		
		textField.setPreferredSize(new Dimension(75, 20));
		
		textField.setText(PTabbedPane.createCurrentDateString());
		
		datePanel.add(textField);
		
		JButton changeButton = new JButton("Change");
		changeButton.addActionListener((e)-> tabbedPane.initTabs(textField.getText()));
		
		datePanel.add(changeButton);

		frame.add(datePanel);

		frame.setVisible(true);
		
		frame.pack();
//		frame.setSize(1210, 760);
		frame.setResizable(false);		
		
	}
}
