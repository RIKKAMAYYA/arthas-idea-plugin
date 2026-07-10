package com.github.idea.arthas.plugin.ui;

import com.github.idea.arthas.plugin.constants.ArthasCommandConstants;
import com.github.idea.arthas.plugin.utils.ActionLinkUtils;
import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.github.idea.arthas.plugin.utils.PropertiesComponentUtils;
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

public class ArthasVmToolDialog extends JDialog {
    private JPanel contentPane;

    private JTextField vmToolExpressTextField;

    private ActionLink classloaderHelpLabel;

    private JTextField classloaderHashValueTextField;

    private JButton clearCacheButton;

    private JButton copyScCommandButton;

    private JButton copyCommandButton;

    private ActionLink vmtoolHelpLabel;

    private JButton instancesCommandButton;

    private Project project;


    public ArthasVmToolDialog(Project project, String className, String invokeCommand, String instancesCommand) {
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(copyCommandButton);
        this.project = project;
        String classloaderHash = PropertiesComponentUtils.getValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE);
        classloaderHashValueTextField.setText(classloaderHash);

        vmToolExpressTextField.setText(invokeCommand);
        copyCommandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        copyScCommandButton.addActionListener(e -> {
            String scCommand = String.join(" ", "sc -d", className);
            ClipboardUtils.setClipboardString(scCommand);
            NotifyUtils.notifyMessageDefault(project);

        });

        if (StringUtils.isNotBlank(instancesCommand)) {
            instancesCommandButton.addActionListener(e -> {
                String hashClassloader = classloaderHashValueTextField.getText();
                String vmtoolInstanceCommand = instancesCommand;
                if (StringUtils.isNotBlank(hashClassloader) && vmtoolInstanceCommand != null && !vmtoolInstanceCommand.contains("-c ")) {
                    vmtoolInstanceCommand = vmtoolInstanceCommand + " -c " + hashClassloader;
                    PropertiesComponentUtils.setValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE, hashClassloader);
                }
                if (StringUtils.isNotBlank(vmtoolInstanceCommand)) {
                    ClipboardUtils.setClipboardString(vmtoolInstanceCommand);
                    NotifyUtils.notifyMessageDefault(project);
                }
            });
        } else {
            // 没有获取实例的表达式直接关闭
            instancesCommandButton.setVisible(false);
        }


        clearCacheButton.addActionListener(e -> {
            classloaderHashValueTextField.setText("");
            PropertiesComponentUtils.setValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE, "");
        });

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
    }

    private void onOK() {
        String hashClassloader = classloaderHashValueTextField.getText();
        String vmtoolExpress = vmToolExpressTextField.getText();
        if (StringUtils.isNotBlank(hashClassloader) && vmtoolExpress != null && !vmtoolExpress.contains("-c ")) {
            vmtoolExpress = vmtoolExpress + " -c " + hashClassloader;
            PropertiesComponentUtils.setValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE, hashClassloader);
        }
        if (StringUtils.isNotBlank(vmtoolExpress)) {
            ClipboardUtils.setClipboardString(vmtoolExpress);
            NotifyUtils.notifyMessageDefault(project);
        }
        dispose();
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
        //两个屏幕处理出现问题，跳到主屏幕去了 https://blog.csdn.net/weixin_33919941/article/details/88129513
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        vmToolExpressTextField.requestFocus();
        setVisible(true);
    }


    private void createUIComponents() {
        classloaderHelpLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/sc.html");
        vmtoolHelpLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/vmtool.html");
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
        contentPane.setLayout(new GridLayoutManager(4, 4, new Insets(10, 10, 10, 10), -1, -1));
        vmtoolHelpLabel.setText("vmtool invoke express");
        contentPane.add(vmtoolHelpLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        contentPane.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        vmToolExpressTextField = new JTextField();
        contentPane.add(vmToolExpressTextField, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        classloaderHelpLabel.setText("sc -d get classloader ");
        classloaderHelpLabel.setToolTipText("sc -d get classloader hash value");
        contentPane.add(classloaderHelpLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        classloaderHashValueTextField = new JTextField();
        contentPane.add(classloaderHashValueTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        clearCacheButton = new JButton();
        clearCacheButton.setText("clear cache");
        contentPane.add(clearCacheButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        copyScCommandButton = new JButton();
        copyScCommandButton.setText("copy sc command");
        contentPane.add(copyScCommandButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        copyCommandButton = new JButton();
        copyCommandButton.setText("copy invoke command");
        copyCommandButton.setToolTipText("invoke first class instances method or field");
        contentPane.add(copyCommandButton, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        instancesCommandButton = new JButton();
        instancesCommandButton.setText("copy instantces command");
        instancesCommandButton.setToolTipText("get class instances");
        contentPane.add(instancesCommandButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
