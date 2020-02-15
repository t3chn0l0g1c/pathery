package solver.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import solver.input.JSONReader;
import solver.input.MazeDescriptor;
import solver.util.Site;

public class PTabbedPane extends JTabbedPane {
	
	//TODO add changeListener to notify tabs at change for button active state or check periodically
//	.
	// TODO use singleton object again for that (also singleton running action thread)
	
	private final List<MazePanel> mazePanelList = new ArrayList<MazePanel>();
	
	public void initTabsWithCurrentDate() {
		initTabs(createCurrentDateString());
	}
	
	public void initTabs(String dateString) {
		for(MazePanel m : mazePanelList) {
			m.cancel();
		}
		mazePanelList.clear();
		removeAll();
		createTabs(Site.YELLOW, dateString);
		createTabs(Site.RED, dateString);
		
		revalidate();
	}
	
	public static String createCurrentDateString() {
		GregorianCalendar c = new GregorianCalendar();
//		c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH)+1);
		return c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + (c.get(Calendar.DAY_OF_MONTH));
	}
	
	private void createTabs(Site site, String dateString) {
		String url = site.urlStub + "/a/mapsbydate/" + dateString + ".js";
		System.out.println(url);
		String[] normal = JSONReader.readMapIDsForDay(url);
		if(normal==null) {
			return;
		}
		for(int i = 0; i<normal.length; i++) {
			String str = normal[i];
			if(str.equals("null")) {
				System.err.println("Ignored a map at " + site.urlStub);
				continue;
			}
			MazeDescriptor md = JSONReader.readJSON(site, str);
			if(md==null) {
				continue;
			}
			MazePanel mp = new MazePanel(md);
			mazePanelList.add(mp);
			JLabel label = new JLabel(str);
			label.setBackground(site.color);
			label.setForeground(site.color);
			addTab(null, mp);
			setTabComponentAt(getTabCount()-1, label);
		}
	}

}
