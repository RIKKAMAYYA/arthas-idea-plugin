package com.github.idea.arthas.plugin.ui;

import com.github.idea.arthas.plugin.action.arthas.ArthasOgnlSpringAllPropertySourceCommandAction;
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

public class ArthasActionWatchSpringContextDialog extends JDialog {

    /**
     * 构造前缀
     */
    private static final String WATCH_FOR_SPRING_PROPERTY_PRE = "watch -x 3 -n 1  org.springframework.web.servlet.DispatcherServlet doDispatch";
    /**
     * 构造获取spring 表达式的信息 参考 {@link ArthasOgnlSpringAllPropertySourceCommandAction}
     */
    private static final String WATCH_FOR_SPRING_PROPERTY_CONTEXT = ArthasCommandConstants.SPRING_CONTEXT_PARAM + "=@org.springframework.web.context.support.WebApplicationContextUtils@getWebApplicationContext(params[0].getServletContext())";

    //    public static void main(String[] args) {
//        String command = String.format(ArthasCommandConstants.SPRING_ALL_PROPERTY, WATCH_FOR_SPRING_PROPERTY_PRE, WATCH_FOR_SPRING_PROPERTY_CONTEXT, ArthasCommandConstants.SPRING_CONTEXT_PARAM);
//        System.out.println(command);
//    }
    private JButton closeButton;

    private JTextField ognlExpressionEditor;

    private JPanel contentPane;
    private ActionLink ognlOfficeActionLink;
    private ActionLink ognlDemoLink;
    private ActionLink watchHelpLink;
    /**
     * spring 所有环境配置项信息获取
     */
    private JButton springPropertyButton;
    /**
     * 代理对象的原始对象的信息
     */
    private JButton springNonProxyTargetButton;


    private String className;

    private String staticOgnlExpression;

    private Project project;

    private String aopTargetOgnlExpression;


    public ArthasActionWatchSpringContextDialog(Project project, String className, String staticOgnlExpression, String aopTargetOgnlExpression) {
        this.project = project;
        $$$setupUI$$$();
        setContentPane(this.contentPane);
        setModal(true);
        getRootPane().setDefaultButton(closeButton);
        this.className = className;
        this.staticOgnlExpression = staticOgnlExpression;
        this.aopTargetOgnlExpression = aopTargetOgnlExpression;

        closeButton.addActionListener(e -> onOK());


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
        springPropertyButton.addActionListener((e -> {
            String command = String.format(ArthasCommandConstants.SPRING_ALL_PROPERTY, WATCH_FOR_SPRING_PROPERTY_PRE, WATCH_FOR_SPRING_PROPERTY_CONTEXT, ArthasCommandConstants.SPRING_CONTEXT_PARAM);
            ClipboardUtils.setClipboardString(command);
            NotifyUtils.notifyMessage(project, "由于使用watch 触发ognl的调用，必须要触发一次Mvc接口的调用，Static Spring Context 调用不同,获取指定项的值可以可以参考Ognl get selected spring property");
        }));

        springNonProxyTargetButton.addActionListener(e -> {
            ClipboardUtils.setClipboardString(aopTargetOgnlExpression);
            NotifyUtils.notifyMessage(project, "Bean 名称可能不正确可以手动修改,由于使用watch 触发ognl的调用，必须要触发一次Mvc接口的调用，Static Spring Context 调用不同");
        });
    }


    /**
     * 取人按钮回调
     */
    private void onOK() {
        String ognCurrentExpression = ognlExpressionEditor.getText();
        if (StringUtils.isNotBlank(ognCurrentExpression)) {
            ClipboardUtils.setClipboardString(ognCurrentExpression);
            NotifyUtils.notifyMessage(project, "Bean 名称可能不正确可以手动修改,由于使用watch 触发ognl的调用，必须要触发一次Mvc接口的调用，Static Spring Context 调用不同");
        }
        dispose();
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
        //两个屏幕处理出现问题，跳到主屏幕去了
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        ognlExpressionEditor.requestFocus();
        setVisible(true);

    }


    private void createUIComponents() {
        ognlOfficeActionLink = ActionLinkUtils.newActionLink("https://commons.apache.org/dormant/commons-ognl/language-guide.html");
        ognlDemoLink = ActionLinkUtils.newActionLink("https://github.com/WangJi92/arthas-idea-plugin/issues/5");

        //https://github.com/WangJi92/arthas-idea-plugin/issues/5

        watchHelpLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/arthas/watch");
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
        contentPane.setLayout(new GridLayoutManager(2, 7, new Insets(10, 10, 10, 10), -1, -1));
        closeButton = new JButton();
        closeButton.setText("copy command");
        contentPane.add(closeButton, new GridConstraints(1, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(101, 27), null, 0, false));
        watchHelpLink.setEnabled(true);
        watchHelpLink.setText("watch ognl expressions");
        contentPane.add(watchHelpLink, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(111, 16), null, 0, false));
        ognlExpressionEditor = new JTextField();
        ognlExpressionEditor.setEditable(true);
        ognlExpressionEditor.setHorizontalAlignment(4);
        contentPane.add(ognlExpressionEditor, new GridConstraints(0, 1, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, 16), null, 0, false));
        ognlOfficeActionLink.setText("ognl help");
        contentPane.add(ognlOfficeActionLink, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ognlDemoLink.setText("best example");
        contentPane.add(ognlDemoLink, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        springPropertyButton = new JButton();
        springPropertyButton.setHorizontalTextPosition(0);
        springPropertyButton.setText("spring env variables");
        springPropertyButton.setToolTipText("获取当前spring 环境变量信息");
        contentPane.add(springPropertyButton, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        springNonProxyTargetButton = new JButton();
        springNonProxyTargetButton.setText(" non proxy target");
        springNonProxyTargetButton.setToolTipText("代理对象的原始对象信息");
        contentPane.add(springNonProxyTargetButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
