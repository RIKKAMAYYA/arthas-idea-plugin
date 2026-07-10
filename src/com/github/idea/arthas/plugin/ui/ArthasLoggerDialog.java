package com.github.idea.arthas.plugin.ui;

import com.github.idea.arthas.plugin.utils.ActionLinkUtils;
import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.CommonExecuteScriptUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.ActionLink;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * 动态更新日志等级
 * logger --name sample.mybatis.SampleXmlApplication --l warn
 */
public class ArthasLoggerDialog extends JDialog {
    private JPanel contentPane;
    private JButton updateLevelButton;
    private JTextField loggerExpressionEditor;
    private JButton scCommandButton;
    private JTextField classloaderHashEditor;
    private JComboBox logLevelComboBox;
    private JButton closeButton;
    private ActionLink helpLink;
    private ActionLink loggerBestLink;
    private JButton shellScriptCommandButton;


    private Project project;

    private String className;

    public ArthasLoggerDialog(Project project, String className) {
        this.project = project;
        this.className = className;
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(closeButton);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        init();
    }

    private void onOK() {
        // add your code here
        List<String> commands = Lists.newArrayList();

        String loggerName = loggerExpressionEditor.getText();
        commands.add(loggerName);

        //更新level
        String currentLoggerLevel = (String) logLevelComboBox.getSelectedItem();
        if (StringUtils.isNotBlank(currentLoggerLevel)) {
            commands.add("--level");
            commands.add(currentLoggerLevel);
        }

        String hashClassloader = classloaderHashEditor.getText();
        if (StringUtils.isNotBlank(hashClassloader)) {
            commands.add("-c");
            commands.add(hashClassloader);
        }
        String joinCommands = String.join(" ", commands);
        ClipboardUtils.setClipboardString(joinCommands);
        NotifyUtils.notifyMessage(project, NotifyUtils.COMMAND_COPIED + "(logger level trace>debug>info>warn>error,-c classloader hash value,--l logger level");
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
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

    private void init() {
        this.scCommandButton.addActionListener(e -> getLoggerClassHashLoader());
        this.updateLevelButton.addActionListener(e -> onOK());
        this.closeButton.addActionListener(e -> onCancel());
        String loggerEx = String.join(" ", "logger", "--name", this.className);
        this.loggerExpressionEditor.setText(loggerEx);
        shellScriptCommandButton.addActionListener(e -> {
            List<String> commands = Lists.newArrayList();
            String loggerName = loggerExpressionEditor.getText();
            commands.add(loggerName);
            //更新level
            String currentLoggerLevel = (String) logLevelComboBox.getSelectedItem();
            if (StringUtils.isNotBlank(currentLoggerLevel)) {
                commands.add("--level");
                commands.add(currentLoggerLevel);
            }
            String joinCommands = String.join(" ", commands);
            // logger --name xxx class 这里的表达式不是 classLoaderHash   18b4aac2 统一一下格式
            String scCommand = String.join(" ", "logger", "--name", className);
            CommonExecuteScriptUtils.executeCommonScript(project, scCommand, joinCommands, "");
            dispose();
        });
    }

    private void getLoggerClassHashLoader() {
        String loggerName = loggerExpressionEditor.getText();
        ClipboardUtils.setClipboardString(loggerName);
        NotifyUtils.notifyMessage(project, NotifyUtils.COMMAND_COPIED + "(Get classloader hash value of class through logger - name)");
    }

    private void createUIComponents() {
        loggerBestLink = ActionLinkUtils.newActionLink("https://github.com/WangJi92/arthas-idea-plugin/issues/7");
        helpLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/logger.html");
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
        contentPane.setLayout(new GridLayoutManager(6, 3, new Insets(10, 10, 10, 10), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setEnabled(true);
        label1.setText("logger expressions");
        label1.setToolTipText("获取logger的详情");
        contentPane.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loggerExpressionEditor = new JTextField();
        loggerExpressionEditor.setEditable(true);
        loggerExpressionEditor.setEnabled(true);
        loggerExpressionEditor.setHorizontalAlignment(4);
        contentPane.add(loggerExpressionEditor, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, -1), null, 0, false));
        scCommandButton = new JButton();
        scCommandButton.setText("copy logger sc ");
        scCommandButton.setToolTipText("");
        contentPane.add(scCommandButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        classloaderHashEditor = new JTextField();
        contentPane.add(classloaderHashEditor, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        logLevelComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("");
        defaultComboBoxModel1.addElement("trace");
        defaultComboBoxModel1.addElement("debug");
        defaultComboBoxModel1.addElement("info");
        defaultComboBoxModel1.addElement("warn");
        defaultComboBoxModel1.addElement("error");
        logLevelComboBox.setModel(defaultComboBoxModel1);
        logLevelComboBox.setToolTipText("修改日志等级");
        contentPane.add(logLevelComboBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateLevelButton = new JButton();
        updateLevelButton.setText("copy command");
        updateLevelButton.setToolTipText("更新logger的日志级别");
        contentPane.add(updateLevelButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(101, 27), null, 0, false));
        closeButton = new JButton();
        closeButton.setText("close");
        contentPane.add(closeButton, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setEnabled(true);
        label2.setText("logger level");
        label2.setToolTipText("logger 的日志级别");
        contentPane.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("-c classloader");
        label3.setToolTipText("当前logger -name对应的classloder的hash 值");
        contentPane.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        helpLink.setText("help");
        helpLink.setToolTipText("可以通过help tt 获取帮助信息");
        contentPane.add(helpLink, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loggerBestLink.setText("logger best example");
        loggerBestLink.setToolTipText("logger 实践例子");
        contentPane.add(loggerBestLink, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        contentPane.add(spacer1, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        shellScriptCommandButton = new JButton();
        shellScriptCommandButton.setText("shell command");
        shellScriptCommandButton.setToolTipText("直接执行脚本 无需打开arthas【先选择更新的等级】");
        contentPane.add(shellScriptCommandButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
