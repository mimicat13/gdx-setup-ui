package aurelienribon.libgdx.ui.panels;

import aurelienribon.libgdx.Helper;
import aurelienribon.libgdx.Helper.ClasspathEntry;
import aurelienribon.libgdx.Helper.GwtModule;
import aurelienribon.libgdx.ui.Ctx;
import aurelienribon.libgdx.ui.MainPanel;
import aurelienribon.ui.css.Style;
import aurelienribon.utils.notifications.AutoListModel;
import aurelienribon.utils.notifications.ObservableList;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.apache.commons.io.FileUtils;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class ClasspathsPanel extends javax.swing.JPanel {
	private final ObservableList<ClasspathEntry> coreClasspath = Ctx.cfgUpdate.coreClasspath;
	private final ObservableList<ClasspathEntry> androidClasspath = Ctx.cfgUpdate.androidClasspath;
	private final ObservableList<ClasspathEntry> desktopClasspath = Ctx.cfgUpdate.desktopClasspath;
	private final ObservableList<ClasspathEntry> htmlClasspath = Ctx.cfgUpdate.htmlClasspath;
	private final ObservableList<GwtModule> gwtModules = Ctx.cfgUpdate.gwtModules;

    public ClasspathsPanel(final MainPanel mainPanel) {
        initComponents();

		Style.registerCssClasses(jScrollPane2, ".frame");
		Style.registerCssClasses(jScrollPane6, ".frame");
		Style.registerCssClasses(jScrollPane4, ".frame");
		Style.registerCssClasses(jScrollPane5, ".frame");
		Style.registerCssClasses(jScrollPane7, ".frame");
		Style.registerCssClasses(paintedPanel1, ".optionGroupPanel");

		coreList.setModel(new AutoListModel<ClasspathEntry>(coreClasspath));
		androidList.setModel(new AutoListModel<ClasspathEntry>(androidClasspath));
		desktopList.setModel(new AutoListModel<ClasspathEntry>(desktopClasspath));
		htmlList.setModel(new AutoListModel<ClasspathEntry>(htmlClasspath));
		gwtList.setModel(new AutoListModel<GwtModule>(gwtModules));

		coreList.setCellRenderer(classpathListCellRenderer);
		androidList.setCellRenderer(classpathListCellRenderer);
		desktopList.setCellRenderer(classpathListCellRenderer);
		htmlList.setCellRenderer(classpathListCellRenderer);
		gwtList.setCellRenderer(modulesListCellRenderer);

		Ctx.listeners.add(new Ctx.Listener() {
			@Override public void cfgUpdateChanged() {update();}
		});

		backBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {mainPanel.hideGenerationUpdatePanel();}});
 		deleteBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {delete();}});
 		validateBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {mainPanel.launchUpdateProcess();}});
  }

	private void update() {
		File coreDir = new File(Helper.getCorePrjPath(Ctx.cfgUpdate));
		File androidDir = new File(Helper.getAndroidPrjPath(Ctx.cfgUpdate));
		File desktopDir = new File(Helper.getDesktopPrjPath(Ctx.cfgUpdate));
		File htmlDir = new File(Helper.getHtmlPrjPath(Ctx.cfgUpdate));

		if (!coreDir.isDirectory()) return;

		coreClasspath.clear();
		androidClasspath.clear();
		desktopClasspath.clear();
		htmlClasspath.clear();
		gwtModules.clear();

		coreClasspath.addAll(Helper.getClasspathEntries(new File(coreDir, ".classpath")));
		androidClasspath.addAll(Helper.getClasspathEntries(new File(androidDir, ".classpath")));
		desktopClasspath.addAll(Helper.getClasspathEntries(new File(desktopDir, ".classpath")));
		htmlClasspath.addAll(Helper.getClasspathEntries(new File(htmlDir, ".classpath")));

		if (Ctx.cfgUpdate.isHtmlIncluded) {
			for (File file : FileUtils.listFiles(htmlDir, new String[] {"gwt.xml"}, true)) {
				if (file.getName().equals("GwtDefinition.gwt.xml"))
					gwtModules.addAll(Helper.getGwtModules(file));
			}
		}

		List<ClasspathEntry> newCoreClasspath = Helper.getCoreClasspathEntries(Ctx.cfgUpdate, Ctx.libs);
		List<ClasspathEntry> newAndroidClasspath = Helper.getAndroidClasspathEntries(Ctx.cfgUpdate, Ctx.libs);
		List<ClasspathEntry> newDesktopClasspath = Helper.getDesktopClasspathEntries(Ctx.cfgUpdate, Ctx.libs);
		List<ClasspathEntry> newHtmlClasspath = Helper.getHtmlClasspathEntries(Ctx.cfgUpdate, Ctx.libs);
		List<GwtModule> newGwtModules = Helper.getGwtModules(Ctx.cfgUpdate, Ctx.libs);

		for (ClasspathEntry e : coreClasspath) e.testOverwritten(newCoreClasspath);
		for (ClasspathEntry e : androidClasspath) e.testOverwritten(newAndroidClasspath);
		for (ClasspathEntry e : desktopClasspath) e.testOverwritten(newDesktopClasspath);
		for (ClasspathEntry e : htmlClasspath) e.testOverwritten(newHtmlClasspath);
		for (GwtModule m : gwtModules) m.testOverwritten(newGwtModules);

		for (ClasspathEntry e : newCoreClasspath) if (e.testAdded(coreClasspath)) coreClasspath.add(e);
		for (ClasspathEntry e : newAndroidClasspath) if (e.testAdded(androidClasspath)) androidClasspath.add(e);
		for (ClasspathEntry e : newDesktopClasspath) if (e.testAdded(desktopClasspath)) desktopClasspath.add(e);
		for (ClasspathEntry e : newHtmlClasspath) if (e.testAdded(htmlClasspath)) htmlClasspath.add(e);
		for (GwtModule m : newGwtModules) if (m.testAdded(gwtModules)) gwtModules.add(m);

		Collections.sort(coreClasspath);
		Collections.sort(androidClasspath);
		Collections.sort(desktopClasspath);
		Collections.sort(htmlClasspath);
		Collections.sort(gwtModules);
	}

	private void delete() {
		for (Object o : coreList.getSelectedValues()) {
			ClasspathEntry e = (ClasspathEntry) o;
			if (!e.added && !e.overwritten) coreClasspath.remove(e);
		}

		for (Object o : androidList.getSelectedValues()) {
			ClasspathEntry e = (ClasspathEntry) o;
			if (!e.added && !e.overwritten) androidClasspath.remove(e);
		}

		for (Object o : desktopList.getSelectedValues()) {
			ClasspathEntry e = (ClasspathEntry) o;
			if (!e.added && !e.overwritten) desktopClasspath.remove(e);
		}

		for (Object o : htmlList.getSelectedValues()) {
			ClasspathEntry e = (ClasspathEntry) o;
			if (!e.added && !e.overwritten) htmlClasspath.remove(e);
		}

		for (Object o : gwtList.getSelectedValues()) {
			GwtModule m = (GwtModule) o;
			if (!m.added && !m.overwritten) gwtModules.remove(m);
		}

		coreList.clearSelection();
		androidList.clearSelection();
		desktopList.clearSelection();
		htmlList.clearSelection();
		gwtList.clearSelection();
	}

	// -------------------------------------------------------------------------
	// List renderer
	// -------------------------------------------------------------------------

	private final ListCellRenderer classpathListCellRenderer = new DefaultListCellRenderer() {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			ClasspathEntry entryPath = (ClasspathEntry) value;

			label.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
			label.setText(entryPath.path);

			if (entryPath.overwritten) {
				label.setForeground(new Color(0x3D5277));
			} else if (entryPath.added) {
				label.setForeground(new Color(0x008800));
			} else {
				label.setForeground(new Color(0xD1B40F));
			}

			return label;
		}
	};

	private final ListCellRenderer modulesListCellRenderer = new DefaultListCellRenderer() {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			GwtModule module = (GwtModule) value;

			label.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
			label.setText(module.name);

			if (module.overwritten) {
				label.setForeground(new Color(0x3D5277));
			} else if (module.added) {
				label.setForeground(new Color(0x008800));
			} else {
				label.setForeground(new Color(0xD1B40F));
			}

			return label;
		}
	};

	// -------------------------------------------------------------------------
	// Generated stuff
	// -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backBtn = new javax.swing.JButton();
        validateBtn = new javax.swing.JButton();
        paintedPanel1 = new aurelienribon.ui.components.PaintedPanel();
        jLabel2 = new javax.swing.JLabel();
        deleteBtn = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        coreList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        htmlList = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        androidList = new javax.swing.JList();
        jLabel5 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        desktopList = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        gwtList = new javax.swing.JList();
        jLabel7 = new javax.swing.JLabel();

        backBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_back.png"))); // NOI18N
        backBtn.setText("Go back");

        validateBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_ok.png"))); // NOI18N
        validateBtn.setText("Validate");

        jLabel2.setText("<html><b>Legend</b>\n<br/>\n<font color=\"#3D5277\"><b>Blue</b></font> is an element that will be updated, <font color=\"#008800\"><b>green</b></font> is a new element (you selected a new library), and <font color=\"#D1B40F\"><b>orange</b></font> is an element that is not updated, or that is unknown.\n<br/><br/>\nPlease review your classpaths before proceeding. Specifically, you should look at the orange entries, and remove those that are not needed in your project. When updating a project, some libraries may have changed their names, leaving old entries undesirable.");
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout paintedPanel1Layout = new javax.swing.GroupLayout(paintedPanel1);
        paintedPanel1.setLayout(paintedPanel1Layout);
        paintedPanel1Layout.setHorizontalGroup(
            paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paintedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        paintedPanel1Layout.setVerticalGroup(
            paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paintedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap())
        );

        deleteBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_delete.png"))); // NOI18N
        deleteBtn.setText("<html>\nDelete selected <font color=\"#D1B40F\"><b>unknown</b></font> element(s)");

        jPanel7.setOpaque(false);
        jPanel7.setLayout(new java.awt.GridLayout(1, 2, 10, 0));

        jPanel6.setOpaque(false);
        jPanel6.setLayout(new java.awt.GridLayout(2, 1, 0, 10));

        jPanel1.setOpaque(false);

        coreList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(coreList);

        jLabel1.setText("Core project classpath");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addContainerGap(187, Short.MAX_VALUE))
            .addComponent(jScrollPane2)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
        );

        jPanel6.add(jPanel1);

        jPanel4.setOpaque(false);

        htmlList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane6.setViewportView(htmlList);

        jLabel6.setText("Html project classpath");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addContainerGap())
            .addComponent(jScrollPane6)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
        );

        jPanel6.add(jPanel4);

        jPanel7.add(jPanel6);

        jPanel5.setOpaque(false);
        jPanel5.setLayout(new java.awt.GridLayout(3, 1, 0, 10));

        jPanel3.setOpaque(false);

        androidList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane5.setViewportView(androidList);

        jLabel5.setText("Android project classpath");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addContainerGap())
            .addComponent(jScrollPane5)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE))
        );

        jPanel5.add(jPanel3);

        jPanel2.setOpaque(false);

        desktopList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane4.setViewportView(desktopList);

        jLabel4.setText("Desktop project classpath");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addContainerGap())
            .addComponent(jScrollPane4)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE))
        );

        jPanel5.add(jPanel2);

        jPanel8.setOpaque(false);

        gwtList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane7.setViewportView(gwtList);

        jLabel7.setText("GWT modules");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addGap(210, 230, Short.MAX_VALUE))
            .addComponent(jScrollPane7)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE))
        );

        jPanel5.add(jPanel8);

        jPanel7.add(jPanel5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(paintedPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(validateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(backBtn))
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {backBtn, validateBtn});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(backBtn)
                    .addComponent(validateBtn)
                    .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(paintedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList androidList;
    private javax.swing.JButton backBtn;
    private javax.swing.JList coreList;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JList desktopList;
    private javax.swing.JList gwtList;
    private javax.swing.JList htmlList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private aurelienribon.ui.components.PaintedPanel paintedPanel1;
    private javax.swing.JButton validateBtn;
    // End of variables declaration//GEN-END:variables

}