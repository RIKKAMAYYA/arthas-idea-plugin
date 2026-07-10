package com.github.idea.arthas.plugin.ui;

import com.github.idea.arthas.plugin.utils.ActionLinkUtils;
import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.github.idea.arthas.plugin.utils.StringUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.ActionLink;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ArthasAsyncProfileDialog extends JDialog {
    private JPanel contentPane;
    private JComboBox eventComboBox;

    private JComboBox eventModeComboBox;
    /**
     * 开始
     */
    private JButton startCommandButton;
    /**
     * 停止
     */
    private JButton stopCommandButton;
    /**
     * 其他的命令
     */
    private JComboBox otherCommandComboBox;
    /**
     * 输出格式
     */
    private JComboBox outputFileFormatComboBox;
    /**
     * 其他的命令
     */
    private JButton otherCommandButton;
    /**
     * 获取所有的样本
     */
    private JButton getSampleCommandButton;
    private ActionLink help;
    private JButton closeButton;
    private JRadioButton differentThreadsSeparatelyRadioButton;
    private ActionLink asyncExample;
    /**
     * 持续时间
     */
    private JTextField durationTextField;

    /**
     * 持续时间自动结束的命令
     */
    private JButton autoStopCommandButton;

    private Project project;

    public ArthasAsyncProfileDialog(Project project) {
        this.project = project;
        $$$setupUI$$$();
        setContentPane(this.contentPane);
        setModal(false);
        getRootPane().setDefaultButton(closeButton);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        initEvent();
    }

    private void initEvent() {

        // https://github.com/jvm-profiling-tools/async-profiler
        // Wall-clock profiler is most useful in per-thread mode: -t
        eventComboBox.addItemListener(e -> {
            if ("wall".equals(e.getItem().toString())) {
                differentThreadsSeparatelyRadioButton.setSelected(true);
            } else {
                differentThreadsSeparatelyRadioButton.setSelected(false);
            }
        });

        startCommandButton.addActionListener((event) -> {
            startCommand();

        });
        autoStopCommandButton.addActionListener((event) -> {
            autoStopCommand();
        });
        stopCommandButton.addActionListener((event) -> {
            stopCommand();
        });
        getSampleCommandButton.addActionListener((event) -> {
            getSample();
        });
        otherCommandButton.addActionListener((event) -> {
            otherCommand();

        });
    }

    private void getSample() {
        List<String> commands = new ArrayList<>();
        commands.add("profiler");
        commands.add("getSamples");
        String commandFinal = String.join(" ", commands);
        ClipboardUtils.setClipboardString(commandFinal);
        NotifyUtils.notifyMessage(project, "获取当前profiler 收集的样本的数量");
    }

    private void otherCommand() {
        List<String> commands = new ArrayList<>();
        commands.add("profiler");
        String otherCommandComboBoxStr = otherCommandComboBox.getSelectedItem() != null ? otherCommandComboBox.getSelectedItem().toString() : "";
        if (StringUtils.isNotBlank(otherCommandComboBoxStr)) {
            commands.add(otherCommandComboBoxStr);
        }
        String commandFinal = String.join(" ", commands);
        ClipboardUtils.setClipboardString(commandFinal);
        NotifyUtils.notifyMessage(project, "list(all supported events),actions(all supported actions)");
    }

    private void stopCommand() {
        List<String> commands = new ArrayList<>();
        commands.add("profiler");
        commands.add("stop");
        String outputFileFormatComboBoxStr = outputFileFormatComboBox.getSelectedItem() != null ? outputFileFormatComboBox.getSelectedItem().toString() : "";
        if (StringUtils.isNotBlank(outputFileFormatComboBoxStr)) {
            commands.add("--format");
            commands.add(outputFileFormatComboBoxStr);
        }
        String commandFinal = String.join(" ", commands);
        ClipboardUtils.setClipboardString(commandFinal);
        NotifyUtils.notifyMessage(project, "分析火焰图X轴越长,代表用的越多(重点关注),Y轴是调用堆栈信息,和颜色无关 eg cpu占用率高");
    }

    private void startCommand() {
        List<String> commands = getStartCommandList();
        String commandFinal = String.join(" ", commands);
        ClipboardUtils.setClipboardString(commandFinal);
        NotifyUtils.notifyMessage(project, "默认收集cpu  alluser(user-mode events) allkernel(kernel-mode events) 频率单位纳秒");
    }

    /**
     * 完成后自动关闭
     */
    private void autoStopCommand() {
        List<String> commands = getStartCommandList();

        String outputFileFormatComboBoxStr = outputFileFormatComboBox.getSelectedItem() != null ? outputFileFormatComboBox.getSelectedItem().toString() : "";
        if (StringUtils.isNotBlank(outputFileFormatComboBoxStr)) {
            commands.add("--format");
            commands.add(outputFileFormatComboBoxStr);
        }

        String durationTextFieldText = durationTextField.getText();
        if (StringUtils.isNotBlank(durationTextFieldText)) {
            commands.add("--duration");
            commands.add(durationTextFieldText);
        }
        if (differentThreadsSeparatelyRadioButton.isSelected()) {
            commands.add("--threads");
        }
        String commandFinal = String.join(" ", commands);
        ClipboardUtils.setClipboardString(commandFinal);
        NotifyUtils.notifyMessage(project, " 自动完成后自动Stop 详情见 Arthas special Use link");

    }

    /**
     * 获取启动命令的构造 List
     *
     * @return
     */
    @NotNull
    private List<String> getStartCommandList() {
        List<String> commands = new ArrayList<>();
        commands.add("profiler");
        commands.add("start");
        commands.add("--event");
        String eventComboBoxStr = eventComboBox.getSelectedItem() != null ? eventComboBox.getSelectedItem().toString() : "";
        commands.add(eventComboBoxStr);
        // 具体的参数规则 https://github.com/jvm-profiling-tools/async-profiler/blob/v1.6/src/arguments.cpp#L34 allkernel alluser 虽然  ./profiler.sh help 不一样 由于脚本处理了
        String eventModeComboBoxStr = eventModeComboBox.getSelectedItem() != null ? eventModeComboBox.getSelectedItem().toString() : "";
        if (StringUtils.isNotBlank(eventModeComboBoxStr)) {
            commands.add("--" + eventModeComboBoxStr);
        }
        commands.add("--interval");
        commands.add("10000000");

        if (differentThreadsSeparatelyRadioButton.isSelected()) {
            commands.add("--threads");
        }
        return commands;
    }


    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    /**
     * 打开窗口
     */
    public void open() {
        setTitle("async-profiler you can use --include/--exclude filter stack traces,more read doc");
        pack();
        //两个屏幕处理出现问题，跳到主屏幕去了 https://blog.csdn.net/weixin_33919941/article/details/88129513
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        setVisible(true);
    }

    private void createUIComponents() {
        help = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/profiler.html");
        asyncExample = ActionLinkUtils.newActionLink("https://www.cnblogs.com/leihuazhe/p/11630466.html");
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
        contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(6, 7, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("profiler start");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        eventComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("cpu");
        defaultComboBoxModel1.addElement("alloc");
        defaultComboBoxModel1.addElement("lock");
        defaultComboBoxModel1.addElement("wall");
        defaultComboBoxModel1.addElement("itimer");
        defaultComboBoxModel1.addElement("");
        defaultComboBoxModel1.addElement("page-faults");
        defaultComboBoxModel1.addElement("context-switches");
        defaultComboBoxModel1.addElement("cycles");
        defaultComboBoxModel1.addElement("instructions");
        defaultComboBoxModel1.addElement("cache-references");
        defaultComboBoxModel1.addElement("cache-misses");
        defaultComboBoxModel1.addElement("branches");
        defaultComboBoxModel1.addElement("branch-misses");
        defaultComboBoxModel1.addElement("bus-cycles");
        defaultComboBoxModel1.addElement("L1-dcache-load-misses");
        defaultComboBoxModel1.addElement("LLC-load-misses");
        defaultComboBoxModel1.addElement("dTLB-load-misses");
        defaultComboBoxModel1.addElement("mem:breakpoint");
        defaultComboBoxModel1.addElement("trace:tracepoint");
        eventComboBox.setModel(defaultComboBoxModel1);
        panel1.add(eventComboBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("profiler stop");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startCommandButton = new JButton();
        startCommandButton.setText("copy start command");
        startCommandButton.setToolTipText("need to execute stop command after execte start command");
        panel1.add(startCommandButton, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("ohter command");
        panel1.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        otherCommandComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("resume");
        defaultComboBoxModel2.addElement("status");
        defaultComboBoxModel2.addElement("list");
        defaultComboBoxModel2.addElement("actions");
        defaultComboBoxModel2.addElement("version");
        defaultComboBoxModel2.addElement("dumpFlat");
        defaultComboBoxModel2.addElement("dumpTraces");
        defaultComboBoxModel2.addElement("dumpCollapsed");
        otherCommandComboBox.setModel(defaultComboBoxModel2);
        panel1.add(otherCommandComboBox, new GridConstraints(3, 1, 1, 5, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        otherCommandButton = new JButton();
        otherCommandButton.setText("copy other command");
        panel1.add(otherCommandButton, new GridConstraints(3, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("profiler getSamples");
        label4.setToolTipText("获取当前profiler 收集的样本的数量");
        panel1.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        getSampleCommandButton = new JButton();
        getSampleCommandButton.setText("copy samples command");
        panel1.add(getSampleCommandButton, new GridConstraints(2, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("profiling event");
        label5.setToolTipText("profiler list 获取");
        panel1.add(label5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("format");
        label6.setToolTipText("导出文件支持的格式");
        panel1.add(label6, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        outputFileFormatComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        defaultComboBoxModel3.addElement("html");
        defaultComboBoxModel3.addElement("jfr");
        outputFileFormatComboBox.setModel(defaultComboBoxModel3);
        panel1.add(outputFileFormatComboBox, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        closeButton = new JButton();
        closeButton.setText("close");
        panel1.add(closeButton, new GridConstraints(4, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        help.setText("arthas help");
        help.setToolTipText("火焰图使用帮助文档");
        panel1.add(help, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("火焰图X轴越长,代表用的越多(重点关注),Y轴是调用堆栈信息,和颜色无关");
        panel1.add(label7, new GridConstraints(4, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventModeComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel4 = new DefaultComboBoxModel();
        defaultComboBoxModel4.addElement("");
        defaultComboBoxModel4.addElement("alluser");
        defaultComboBoxModel4.addElement("allkernel");
        eventModeComboBox.setModel(defaultComboBoxModel4);
        eventModeComboBox.setToolTipText("events mode 注意: macOS 分析仅限于用户空间");
        panel1.add(eventModeComboBox, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        differentThreadsSeparatelyRadioButton = new JRadioButton();
        differentThreadsSeparatelyRadioButton.setSelected(false);
        differentThreadsSeparatelyRadioButton.setText("不同线程分开统计");
        differentThreadsSeparatelyRadioButton.setToolTipText("wall profiler is most useful in per-thread mode");
        panel1.add(differentThreadsSeparatelyRadioButton, new GridConstraints(0, 4, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        asyncExample.setText("async-profiler example");
        panel1.add(asyncExample, new GridConstraints(4, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        stopCommandButton = new JButton();
        stopCommandButton.setText("copy stop command");
        panel1.add(stopCommandButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("duration(seconds) ");
        label8.setToolTipText("持续时间 自动结束 单位秒");
        panel1.add(label8, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        durationTextField = new JTextField();
        durationTextField.setText("30");
        durationTextField.setToolTipText("持续多久自动结束");
        panel1.add(durationTextField, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        autoStopCommandButton = new JButton();
        autoStopCommandButton.setText("copy duration(start|stop) command");
        autoStopCommandButton.setToolTipText("auto stop in duration");
        panel1.add(autoStopCommandButton, new GridConstraints(1, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
