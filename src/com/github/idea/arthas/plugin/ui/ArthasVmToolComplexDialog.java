package com.github.idea.arthas.plugin.ui;

import com.github.idea.arthas.plugin.common.command.CommandContext;
import com.github.idea.arthas.plugin.utils.ActionLinkUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.ui.components.ActionLink;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;

public class ArthasVmToolComplexDialog extends JDialog {
    private static final Logger LOG = Logger.getInstance(ArthasVmToolComplexDialog.class);
    private JPanel contentPane;
    private ActionLink vmtoolHelpLabel;
    private JTextField vmToolExpressTextField;
    private ActionLink classloaderHelpLabel;
    private JTextField classloaderHashValueTextField;
    private JButton clearCacheButton;
    private JButton copyScCommandButton;
    private JButton instancesCommandButton;
    private JButton copyCommandButton;
    private JTable table1;

    private CommandContext commandContext;


    public ArthasVmToolComplexDialog(CommandContext commandContext) {
        this.commandContext = commandContext;
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(false);
        //getRootPane().setDefaultButton(buttonOK);

//        buttonOK.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                onOK();
//            }
//        });

//        buttonCancel.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                onCancel();
//            }
//        });

        if ((commandContext.getPsiElement() instanceof PsiMethod)) {
            PsiMethod psiMethod = (PsiMethod) commandContext.getPsiElement();
            PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
            if (parameters.length > 0) {
                table1.setVisible(true);
                DefaultTableModel model = new DefaultTableModel(parameters.length, 2);
                table1.setModel(model);
                table1.setRowHeight(30);

                // 创建并设置 JTextArea 渲染器
                JTextArea textAreaRenderer = new JTextArea();
                textAreaRenderer.setLineWrap(true);
                textAreaRenderer.setWrapStyleWord(true);
                //textAreaRenderer.setRows(3);
                textAreaRenderer.setEditable(true);
                TableColumn column1 = table1.getColumnModel().getColumn(1);
                // 设置渲染器
                column1.setCellRenderer(new TableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                        textAreaRenderer.setText((String) value);
                        textAreaRenderer.setFont(table.getFont());
                        return textAreaRenderer;
                    }
                });

                JLabel jLabel = new JLabel();
                TableColumn column0 = table1.getColumnModel().getColumn(0);
                column0.setMaxWidth(180);
                column0.setMinWidth(150);
                column0.setResizable(true);
                column0.setCellRenderer(new TableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        jLabel.setText((String) value);
                        jLabel.setFont(table.getFont());
                        return jLabel;
                    }
                });


                //table1.setDragEnabled(true);
                //table1.setFillsViewportHeight(true);
                int index = 0;
                for (PsiParameter parameter : parameters) {
                    //PsiElement declarationScope = parameter.getDeclarationScope();
                    String jsonString = "";
                    try {
                        // jsonString = PsiParserToJson.getInstance().toJSONString(parameter);
                    } catch (Exception e) {
                        LOG.error("error", e);
                    }
                    // String json = this.pojo2JSONParser.uElementToJSONString(uElement);
                    // String defaultParamValue = OgnlPsUtils.getDefaultString(parameter.getType(), parameter.getProject());
                    model.setValueAt(parameter.getName(), index, 0);
                    model.setValueAt(jsonString, index, 1);
                    index++;
                }
            }

        } else {
            table1.setVisible(false);
        }


        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        classloaderHelpLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/sc.html");
        vmtoolHelpLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/vmtool.html");
    }

    /**
     * 打开窗口
     */
    public void open(String title) {
        setTitle(title);
        pack();
        //两个屏幕处理出现问题，跳到主屏幕去了 https://blog.csdn.net/weixin_33919941/article/details/88129513
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.commandContext.getProject()));
        setVisible(true);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(5, 4, new Insets(10, 10, 10, 10), -1, -1));
        vmtoolHelpLabel.setText("vmtool invoke express");
        contentPane.add(vmtoolHelpLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        vmToolExpressTextField = new JTextField();
        contentPane.add(vmToolExpressTextField, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        classloaderHelpLabel.setText("sc -d get classloader ");
        classloaderHelpLabel.setToolTipText("sc -d get classloader hash value");
        contentPane.add(classloaderHelpLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        contentPane.add(spacer1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        classloaderHashValueTextField = new JTextField();
        contentPane.add(classloaderHashValueTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        clearCacheButton = new JButton();
        clearCacheButton.setText("clear cache");
        contentPane.add(clearCacheButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        copyScCommandButton = new JButton();
        copyScCommandButton.setText("copy sc command");
        contentPane.add(copyScCommandButton, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        instancesCommandButton = new JButton();
        instancesCommandButton.setText("copy instantces command");
        instancesCommandButton.setToolTipText("get class instances");
        contentPane.add(instancesCommandButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        copyCommandButton = new JButton();
        copyCommandButton.setText("copy invoke command");
        copyCommandButton.setToolTipText("invoke first class instances method or field");
        contentPane.add(copyCommandButton, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        table1 = new JTable();
        contentPane.add(table1, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
