package com.github.idea.arthas.plugin.ui;

import com.github.idea.arthas.plugin.setting.AppSettingsState;
import com.github.idea.arthas.plugin.constants.ArthasCommandConstants;
import com.github.idea.arthas.plugin.utils.ActionLinkUtils;
import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.google.common.collect.Sets;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.ActionLink;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;

/**
 * 支持trace -E
 *
 * @author 汪小哥
 * @date 3-1-2020
 */
public class ArthasTraceMultipleCommandDialog extends JDialog {
    private JPanel contentPane;
    /**
     * 完成命令
     */
    private JButton commandOk;
    /**
     * 继续添加命令
     */
    private JButton addTraceButton;
    /**
     * 展示数据命令
     */
    private JTextField traceCommandTextField;
    /**
     * 清除命令
     */
    private JButton clearButton;

    /**
     * 帮助命令
     */
    private ActionLink traceHelp;

    /**
     * 当前工程命令
     */
    private Project project;

    /**
     * 类名称
     */
    private static Set<String> CLASS_SET = Sets.newConcurrentHashSet();
    /**
     * 方法名称
     */
    private static Set<String> METHOD_SET = Sets.newConcurrentHashSet();


    public ArthasTraceMultipleCommandDialog(Project project) {
        this.project = project;
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(commandOk);
        initEvent();
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        commandOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        addTraceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                continueToAdd();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                destroyTraceData(project);
            }
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

    /**
     * 完成处理
     */
    private void onOK() {
        String traceECommand = traceCommandTextField.getText();
        if (StringUtils.isNotBlank(traceECommand)) {
            AppSettingsState instance = AppSettingsState.getInstance(project);
            boolean skipJdkMethod = instance.traceSkipJdk;
            String skpJdkMethodCommand = skipJdkMethod ? "" : ArthasCommandConstants.DEFAULT_SKIP_JDK_FALSE;
            String printConditionExpress = instance.printConditionExpress ? "-v" : "";
            String command = String.join(" ", traceECommand, printConditionExpress, skpJdkMethodCommand, ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS);
            ClipboardUtils.setClipboardString(command);
            NotifyUtils.notifyMessage(project, NotifyUtils.COMMAND_COPIED + "<a href=\"https://arthas.aliyun.com/doc/trace.html\">trace -E help</a>");
        }
        // modify by wangji 同事意见 多次trace 可能需要增加其他的 最好是自己手动清除
        // this.destroyTraceData(project);
        dispose();
    }

    /**
     * 继续添加
     */
    private void continueToAdd() {
        dispose();
    }

    private void onCancel() {
        dispose();
        //清除数据
        // this.destroyTraceData(project);
    }

    /**
     * 展示命令信息
     */
    private void showTraceCommand() {
        String classNames = String.join("|", CLASS_SET);
        String methodNames = String.join("|", METHOD_SET);

        // java.util.regex.PatternSyntaxException: Dangling meta character '*' near index
        // eg trace -E com.common.A |com.common.B  *| list  will be error before
        String replaceMethodName = methodNames.replace("*", "\\\\*");

        AppSettingsState instance = AppSettingsState.getInstance(project);
        String invokeCount = instance.invokeCount;
        String command = String.join(" ", "trace -E", classNames, replaceMethodName, "-n", invokeCount);
        traceCommandTextField.setText(command);
    }


    /**
     * 添加方法和参数信息
     *
     * @param className
     * @param methodName
     */
    public void continueAddTrace(String className, String methodName) {
        CLASS_SET.add(className);
        METHOD_SET.add(methodName);
    }

    /**
     * 清除数据
     *
     * @param project
     */
    private void destroyTraceData(Project project) {
        this.project = project;
        CLASS_SET.clear();
        METHOD_SET.clear();
        this.traceCommandTextField.setText("trace -E ");
    }

    /**
     * 展示对话框
     */
    public void showDialog() {
        this.showTraceCommand();
        this.open();
        this.setVisible(true);
    }

    /**
     * 打开窗口
     */
    public void open() {
        setTitle("arthas trace -E");
        pack();
        //两个屏幕处理出现问题，跳到主屏幕去了 https://blog.csdn.net/weixin_33919941/article/details/88129513
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        setVisible(false);
    }

    /**
     * 自定义UI 系统自动调用
     */
    private void createUIComponents() {
        traceHelp = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/trace.html");
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
        contentPane.setLayout(new GridLayoutManager(2, 5, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setMinimumSize(new Dimension(800, 83));
        traceCommandTextField = new JTextField();
        contentPane.add(traceCommandTextField, new GridConstraints(0, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("trace -E");
        contentPane.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        clearButton = new JButton();
        clearButton.setText("clear");
        contentPane.add(clearButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addTraceButton = new JButton();
        addTraceButton.setText("continue  add");
        addTraceButton.setToolTipText("");
        contentPane.add(addTraceButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        commandOk = new JButton();
        commandOk.setText("copy command");
        commandOk.setToolTipText("追踪方法中的方法、追踪多个方法的trace");
        contentPane.add(commandOk, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        traceHelp.setText("help");
        contentPane.add(traceHelp, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
