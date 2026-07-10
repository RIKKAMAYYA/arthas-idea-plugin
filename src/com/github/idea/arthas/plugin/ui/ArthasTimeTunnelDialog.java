package com.github.idea.arthas.plugin.ui;

import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.github.idea.arthas.plugin.common.enums.TimeTunnelCommandEnum;
import com.github.idea.arthas.plugin.utils.ActionLinkUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.ActionLink;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 方法执行数据的时空隧道，记录下指定方法每次调用的入参和返回信息，并能对这些不同的时间下调用进行观测
 *
 * @author 汪小哥
 * @date 11-01-2020
 */
@SuppressWarnings("unchecked")
public class ArthasTimeTunnelDialog extends JDialog {
    private JPanel contentPane;
    /**
     * tt -t 表达式构造
     */
    private JTextField ttTextField;
    /**
     * 获取tt 命令行Button
     */
    private JButton ttButton;
    /**
     * 常用命令下拉框
     */
    private JComboBox comboBox;

    /**
     * 获取命令的信息
     */
    private JButton comboBoxValueGetButton;
    /**
     * 关闭按钮
     */
    private JButton closeButton;

    /**
     * 帮助链接
     */
    private ActionLink helpLink;

    /**
     * 最佳案列
     */
    private ActionLink ttBestLink;

    /**
     * 表达式
     */
    private String timeTunnelExpression;
    /**
     * 工程信息
     */
    private Project project;


    public ArthasTimeTunnelDialog(Project project, String timeTunnelExpression) {
        this.project = project;
        $$$setupUI$$$();
        setContentPane(this.contentPane);
        setModal(true);
        getRootPane().setDefaultButton(closeButton);
        this.timeTunnelExpression = timeTunnelExpression;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        init();
    }

    private void init() {
        ttTextField.setText(this.timeTunnelExpression);
        ttButton.addActionListener(e -> {
            String text = ttTextField.getText();
            if (StringUtils.isNotBlank(text)) {
                ClipboardUtils.setClipboardString(text);
                NotifyUtils.notifyMessageDefault(project);
            }

        });

        comboBoxValueGetButton.addActionListener(e -> {
            Object selectedItem = comboBox.getSelectedItem();
            String selectedItemStr = selectedItem.toString();
            if (selectedItem instanceof TimeTunnelCommandEnum) {
                selectedItemStr = ((TimeTunnelCommandEnum) selectedItem).getCode();
            }
            if (StringUtils.isNotBlank(selectedItemStr)) {
                ClipboardUtils.setClipboardString(selectedItemStr);
                NotifyUtils.notifyMessageDefault(project);
            }
        });
        //值太长展示不全处理 https://www.java-forums.org/awt-swing/16196-item-too-big-jcombobox.html

        for (TimeTunnelCommandEnum value : TimeTunnelCommandEnum.values()) {
            comboBox.addItem(value);
        }

        comboBox.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);

                String tipText = value.toString();
                String codeValue = value.toString();
                if (value instanceof TimeTunnelCommandEnum) {
                    tipText = ((TimeTunnelCommandEnum) value).getEnumMsg();
                    codeValue = ((TimeTunnelCommandEnum) value).getCode();
                }
                if (isSelected) {
                    comboBox.setToolTipText(tipText);
                }

                setToolTipText(tipText);
                Rectangle textRect =
                        new Rectangle(comboBox.getSize().width,
                                getPreferredSize().height);
                String shortText = SwingUtilities.layoutCompoundLabel(this,
                        getFontMetrics(getFont()),
                        codeValue, null,
                        getVerticalAlignment(), getHorizontalAlignment(),
                        getHorizontalTextPosition(), getVerticalTextPosition(),
                        textRect, new Rectangle(), textRect,
                        getIconTextGap());
                setText(shortText);
                return this;
            }

        });
        closeButton.addActionListener(e -> onCancel());
    }

    /**
     * 关闭
     */
    private void onCancel() {
        dispose();
    }


    private void createUIComponents() {
        ttBestLink = ActionLinkUtils.newActionLink("https://github.com/alibaba/arthas/issues/482");
        helpLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/tt.html");
    }

    /**
     * 打开窗口
     */
    public void open(String title) {
        setTitle(title);
        pack();
        //两个屏幕处理出现问题，跳到主屏幕去了
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
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
        contentPane.setLayout(new FormLayout("fill:78px:noGrow,left:57dlu:noGrow,left:3dlu:noGrow,fill:271px:noGrow,fill:max(d;4px):noGrow", "center:28px:noGrow,top:16dlu:noGrow,top:21dlu:noGrow"));
        final JLabel label1 = new JLabel();
        label1.setText("tt -t ");
        label1.setToolTipText("可以通过help tt 获取帮助信息");
        CellConstraints cc = new CellConstraints();
        contentPane.add(label1, cc.xy(1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
        ttTextField = new JTextField();
        contentPane.add(ttTextField, cc.xyw(2, 1, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        ttButton = new JButton();
        ttButton.setText("copy command");
        contentPane.add(ttButton, cc.xy(5, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
        comboBox = new JComboBox();
        comboBox.setEditable(true);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        comboBox.setModel(defaultComboBoxModel1);
        contentPane.add(comboBox, cc.xyw(2, 2, 3, CellConstraints.FILL, CellConstraints.FILL));
        final JLabel label2 = new JLabel();
        label2.setText(" list c");
        label2.setToolTipText("-i 参数需要根据运行时进行修改");
        contentPane.add(label2, cc.xy(1, 2, CellConstraints.CENTER, CellConstraints.CENTER));
        comboBoxValueGetButton = new JButton();
        comboBoxValueGetButton.setText("copy command");
        comboBoxValueGetButton.setToolTipText("-i 参数需要根据运行时进行修改");
        contentPane.add(comboBoxValueGetButton, cc.xy(5, 2, CellConstraints.CENTER, CellConstraints.CENTER));
        helpLink.setText("help");
        helpLink.setToolTipText("可以通过help tt 获取帮助信息");
        contentPane.add(helpLink, cc.xy(1, 3, CellConstraints.CENTER, CellConstraints.CENTER));
        ttBestLink.setText("tt best example");
        contentPane.add(ttBestLink, cc.xy(2, 3, CellConstraints.LEFT, CellConstraints.CENTER));
        closeButton = new JButton();
        closeButton.setText("close");
        contentPane.add(closeButton, cc.xy(5, 3, CellConstraints.CENTER, CellConstraints.CENTER));
        final JLabel label3 = new JLabel();
        label3.setText(" -i parameter needs changed at runtime");
        contentPane.add(label3, cc.xy(4, 3, CellConstraints.CENTER, CellConstraints.CENTER));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
