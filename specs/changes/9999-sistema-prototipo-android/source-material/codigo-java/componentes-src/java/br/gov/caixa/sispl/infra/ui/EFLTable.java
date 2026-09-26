/*
 * Created on 20/04/2005 To change the template for this generated file go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 * Comments
 */
package br.gov.caixa.sispl.infra.ui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import br.gov.caixa.sispl.infra.skin.SkinPainter;
import br.gov.caixa.sispl.infra.skin.SkinRender;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.SkinState;
import br.gov.caixa.sispl.infra.skin.component.table.TableHeaderSkin;
import br.gov.caixa.sispl.infra.skin.component.table.TableSkin;

/**
 * @author Achilles e Clarissa To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code
 *         Generation&gt;Code and Comments
 */
public class EFLTable extends JTable implements SkinPainter {

    private TableSkin component;
    private TableHeaderSkin componentHeader;

    /**
     * @param string
     * @param model
     */
    public EFLTable(String tableSkin) {
        super();
        this.setTableSkin(tableSkin);
    }

    /**
     * @param string
     * @param model
     */
    public EFLTable(String tableSkin, TableModel model) {
        super(model);
        this.setTableSkin(tableSkin);
    }

    /**
     * @param tableSkin
     * @param arg0
     * @param arg1
     */
    public EFLTable(String tableSkin, Object[][] arg0, Object[] arg1) {
        super(arg0, arg1);
        this.setTableSkin(tableSkin);
    }

    /**
     * @param tableSkin
     */
    public void setTableSkin(String tableSkin) {

        if (tableSkin != null) {
            component = (TableSkin) SkinRender.renderComponent(ComponentSkin.TABLE, tableSkin);
            componentHeader = component.getTableHeaderSkin();
        }

        EFLTableCellRenderer cellRender = new EFLTableCellRenderer(component);
        EFLTableHeaderRenderer headerRender = new EFLTableHeaderRenderer(componentHeader);

        int columns = getColumnCount();
        for (int i = 0; i < columns; i++) {
            TableColumn col = this.getColumnModel().getColumn(i);
            col.setCellRenderer(cellRender);
            col.setHeaderRenderer(headerRender);
        }

        // Definindo o Background Default
        setBackground(component.getDefaultState().getBackgroundColor());
        // this.setTableHeader(null);

    }

    /**
     * 
     */
    public EFLTable() {
        this((String) null, null, null);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public EFLTable(Object[][] arg0, Object[] arg1) {
        this(null, arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#paintBorder(java.awt.Graphics)
     */
    protected void paintBorder(Graphics g) {}

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
        if (component != null)
            super.paint(g);
        else {
            super.paintComponent(g);
            super.paintBorder(g);
            super.paintChildren(g);
        }
    }

    public boolean isCellEditable(int colunas, int dados) {
        return false;
    }

    public class EFLTableCellRenderer extends JLabel implements TableCellRenderer {

        private TableSkin tableSkin;

        public EFLTableCellRenderer(TableSkin tableSkin) {
            this.tableSkin = tableSkin;
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean,
         *      int, int)
         */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            SkinState state = null;

            setOpaque(true);
            // Definindo o estado a ser pintado
            if (isSelected)
                state = tableSkin.getTouchedState();
            else if (!this.isEnabled() && tableSkin.getDisabledState() != null)
                state = tableSkin.getDisabledState();
            else
                state = tableSkin.getDefaultState();

            setEnabled(table.isEnabled());
            setForeground(state.getFontColor());
            setBackground(state.getBackgroundColor());
            setFont(state.getFont());
            setText(value.toString());

            return this;
        }
    }

    public class EFLTableHeaderRenderer extends JLabel implements TableCellRenderer {

        private TableHeaderSkin tableHeaderSkin;

        public EFLTableHeaderRenderer(TableHeaderSkin tableHeaderSkin) {
            this.tableHeaderSkin = tableHeaderSkin;
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean,
         *      int, int)
         */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            SkinState state = null;

            setOpaque(true);

            // Definindo o estado a ser pintado
            state = tableHeaderSkin.getDefaultState();

            // setEnabled(table.isEnabled());
            setForeground(state.getFontColor());
            setBackground(state.getBackgroundColor());
            setFont(state.getFont());
            setText(value.toString());

            return this;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.SkinPainter#drawSkin(java.awt.Graphics)
     */
    public void drawSkin(Graphics g) {
        // TODO Auto-generated method stub
    }
}