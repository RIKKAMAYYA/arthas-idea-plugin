package com.github.idea.arthas.plugin.ui;

import com.github.idea.arthas.plugin.action.arthas.ArthasOgnlSpringAllPropertySourceCommandAction;
import com.github.idea.arthas.plugin.setting.AppSettingsState;
import com.github.idea.arthas.plugin.constants.ArthasCommandConstants;
import com.github.idea.arthas.plugin.utils.ActionLinkUtils;
import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.ActionLink;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ArthasTimeTunnelSpringContextDialog extends JDialog {
    /**
     * 构造前缀
     */
    private static final String TT_FOR_SPRING_PROPERTY_PRE = "tt -w";
    /**
     * 构造获取spring 表达式的信息 参考 {@link ArthasOgnlSpringAllPropertySourceCommandAction}
     */
    private static final String TT_FOR_SPRING_PROPERTY_CONTEXT = ArthasCommandConstants.SPRING_CONTEXT_PARAM + "=target.getApplicationContext()";

//    public static void main(String[] args) {
//        String command = String.format(ArthasCommandConstants.SPRING_ALL_PROPERTY, TT_FOR_SPRING_PROPERTY_PRE, TT_FOR_SPRING_PROPERTY_CONTEXT, ArthasCommandConstants.SPRING_CONTEXT_PARAM);
//        System.out.println(command);
//    }

    private JButton closeButton;

    /**
     * 全量表达式
     */
    private JTextField ognlExpressionEditor;

    private JPanel contentPane;
    private ActionLink ognlOfficeActionLink;
    private ActionLink oglSpecialLink;
    private ActionLink ttInvokeAfterLink;
    private JTextField ttRequestMappingHandlerAdapterInvokeField;
    private JTextField timeTunnelIndexField;
    private JButton ttBeginButton;
    private ActionLink ttInvokeBeforeHelp;
    private ActionLink ttIndexLabel;
    /**
     * spring all 环境变量信息
     */
    private JButton springPropertyButton;

    /**
     * 获取目标对象的表达式
     */
    private JTextField aopTargetTextField;

    /**
     * 获取目标对象
     */
    private JButton aopTargetCommandButton;


    private String className;

    private String staticOgnlExpression;

    /**
     * aop 获取目标对象的表达式
     */
    private String aopTargetOgnlExpression;

    private Project project;


    public ArthasTimeTunnelSpringContextDialog(Project project, String className, String staticOgnlExpression, String aopTargetOgnlExpression) {
        this.project = project;
        $$$setupUI$$$();
        setContentPane(this.contentPane);
        setModal(true);
        getRootPane().setDefaultButton(null);
        this.className = className;
        this.staticOgnlExpression = staticOgnlExpression;
        this.aopTargetOgnlExpression = aopTargetOgnlExpression;


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
        ognlExpressionEditor.setText(this.staticOgnlExpression);
        // aop 获取目标对象 https://github.com/alibaba/arthas/issues/482
        // tt -i 1000 -w '#userServers=target.getApplicationContext().getBean("userService"),@org.springframework.aop.support.AopUtils@getTargetClass(#userServers)'
        aopTargetTextField.setText(this.aopTargetOgnlExpression);
        ttBeginButton.addActionListener(e -> {
            String text = ttRequestMappingHandlerAdapterInvokeField.getText();
            ClipboardUtils.setClipboardString(text);
            NotifyUtils.notifyMessage(project, "通过tt 获取spring context的命令可以多次使用,第一次使用需要触发一下一个接口的调用");
        });
        springPropertyButton.addActionListener((e -> {
            String timeTunnelIndex = timeTunnelIndexField.getText();
            if (StringUtils.isBlank(timeTunnelIndex)) {
                timeTunnelIndex = "1000";
            }
            String command = String.format(ArthasCommandConstants.SPRING_ALL_PROPERTY, TT_FOR_SPRING_PROPERTY_PRE, TT_FOR_SPRING_PROPERTY_CONTEXT, ArthasCommandConstants.SPRING_CONTEXT_PARAM);
            String invokeCommand = String.join(" ", command, "-x", "3", "-i", timeTunnelIndex);
            ClipboardUtils.setClipboardString(invokeCommand);
            NotifyUtils.notifyMessage(project, "这里的-i 参数必须是通过tt 获取spring context的命令的tt index的值,获取指定项的值可以可以参考Ognl get selected spring property");
        }));

        // aop target 对象的信息
        aopTargetCommandButton.addActionListener(e -> onOK(aopTargetTextField.getText(), true));

        // 原始的获取方法的数据
        closeButton.addActionListener(e -> onOK(ognlExpressionEditor.getText(), false));

        //初始化数据
        AppSettingsState instance = AppSettingsState.getInstance(project);
        String invokeCount = instance.invokeCount;
        String conditionExpressDisplay = instance.conditionExpressDisplay ? ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS : "";
        String ttSpringContextBeginPrefix = "tt -t org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter invokeHandlerMethod";
        String ttSpringContextBegin = String.join(" ", ttSpringContextBeginPrefix, "-n", invokeCount, conditionExpressDisplay);
        ttRequestMappingHandlerAdapterInvokeField.setText(ttSpringContextBegin);
    }


    /**
     * 关闭按钮回调
     */
    private void onOK(String ognCurrentExpression, boolean isAop) {
        String timeTunnelIndex = timeTunnelIndexField.getText();
        if (StringUtils.isBlank(timeTunnelIndex)) {
            timeTunnelIndex = "1000";
        }
        if (StringUtils.isNotBlank(ognCurrentExpression)) {
            AppSettingsState instance = AppSettingsState.getInstance(project);
            String depthPrintPropertyX = instance.depthPrintProperty;
            if (isAop) {
                depthPrintPropertyX = "1";
            }
            String invokeCommand = String.join(" ", ognCurrentExpression, "-x", depthPrintPropertyX, "-i", timeTunnelIndex);
            ClipboardUtils.setClipboardString(invokeCommand);
            NotifyUtils.notifyMessage(project, "这里的-i 参数必须是通过tt 获取spring context的命令的tt index的值，bean 名称可能不正确，可以手动修改");
        }
    }


    /**
     * 关闭
     */
    private void onCancel() {
        dispose();
    }

    /**
     * 打开窗口
     */
    public void open(String title) {
        setTitle(title);
        pack();
        setMinimumSize(new Dimension(854, 200));
        //两个屏幕处理出现问题，跳到主屏幕去了
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        ttRequestMappingHandlerAdapterInvokeField.requestFocus();
        setVisible(true);

    }


    private void createUIComponents() {
        ognlOfficeActionLink = ActionLinkUtils.newActionLink("https://commons.apache.org/dormant/commons-ognl/language-guide.html");

        oglSpecialLink = ActionLinkUtils.newActionLink("https://github.com/alibaba/arthas/issues/71");

        //https://github.com/WangJi92/arthas-idea-plugin/issues/5
        ttInvokeBeforeHelp = ActionLinkUtils.newActionLink("https://github.com/alibaba/arthas/issues/482");


        ttIndexLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/tt");

        ttInvokeAfterLink = ActionLinkUtils.newActionLink("https://github.com/WangJi92/arthas-idea-plugin/issues/4");
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
        contentPane.setLayout(new GridLayoutManager(5, 3, new Insets(10, 10, 10, 10), -1, -1));
        ttInvokeAfterLink.setEnabled(true);
        ttInvokeAfterLink.setText("2、Time Tunnel Invoke Method Field");
        ttInvokeAfterLink.setToolTipText("bean的名称、方法参数值可以自己手动修改");
        contentPane.add(ttInvokeAfterLink, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(111, 22), null, 0, false));
        ognlExpressionEditor = new JTextField();
        ognlExpressionEditor.setEditable(true);
        ognlExpressionEditor.setHorizontalAlignment(2);
        contentPane.add(ognlExpressionEditor, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, 22), null, 0, false));
        ognlOfficeActionLink.setText("ognl help");
        contentPane.add(ognlOfficeActionLink, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        oglSpecialLink.setText("ognl special");
        contentPane.add(oglSpecialLink, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(178, 16), null, 0, false));
        ttInvokeBeforeHelp.setText("1、Time Tunnel Get Spring Context");
        ttInvokeBeforeHelp.setToolTipText("获取spring context 时间隧道只需执行一次，记录下indx 下次调用任意的bean的方法不用再次执行");
        contentPane.add(ttInvokeBeforeHelp, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ttBeginButton = new JButton();
        ttBeginButton.setText("copy command");
        contentPane.add(ttBeginButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(101, 27), null, 0, false));
        ttRequestMappingHandlerAdapterInvokeField = new JTextField();
        ttRequestMappingHandlerAdapterInvokeField.setEditable(true);
        ttRequestMappingHandlerAdapterInvokeField.setText("tt -t org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter invokeHandlerMethod");
        contentPane.add(ttRequestMappingHandlerAdapterInvokeField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        closeButton = new JButton();
        closeButton.setEnabled(true);
        closeButton.setText("copy command");
        closeButton.setToolTipText("bean的名称可能不正确，可以手动修改");
        contentPane.add(closeButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(101, 27), null, 0, false));
        ttIndexLabel.setText("3、Time Tunnel Index");
        ttIndexLabel.setToolTipText("时间隧道的index值，由第一步获取 可以通过tt -l 查看");
        contentPane.add(ttIndexLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        timeTunnelIndexField = new JTextField();
        timeTunnelIndexField.setText("1000");
        contentPane.add(timeTunnelIndexField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        springPropertyButton = new JButton();
        springPropertyButton.setText("spring env variables");
        springPropertyButton.setToolTipText("获取当前spring 环境变量信息");
        contentPane.add(springPropertyButton, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText(" non proxy target");
        label1.setToolTipText("获取spring 代理对象的原始对象信息");
        contentPane.add(label1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        aopTargetTextField = new JTextField();
        contentPane.add(aopTargetTextField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        aopTargetCommandButton = new JButton();
        aopTargetCommandButton.setText("copy command");
        contentPane.add(aopTargetCommandButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
