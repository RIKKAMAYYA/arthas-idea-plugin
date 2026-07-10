package com.github.idea.arthas.plugin.ui;

import com.github.idea.arthas.plugin.common.combox.CustomComboBoxItem;
import com.github.idea.arthas.plugin.common.combox.CustomDefaultListCellRenderer;
import com.github.idea.arthas.plugin.common.command.CommandContext;
import com.github.idea.arthas.plugin.common.enums.ShellScriptCommandEnum;
import com.github.idea.arthas.plugin.common.enums.ShellScriptConstantEnum;
import com.github.idea.arthas.plugin.common.enums.ShellScriptVariableEnum;
import com.github.idea.arthas.plugin.constants.ArthasCommandConstants;
import com.github.idea.arthas.plugin.setting.AppSettingsState;
import com.github.idea.arthas.plugin.utils.*;
import com.google.common.collect.Sets;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.ActionLink;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.idea.arthas.plugin.utils.NotifyUtils.COMMAND_COPIED;

/**
 * 快捷命令
 *
 * @author 汪小哥
 * @date 05-05-2021
 */
public class ArthasShellScriptCommandDialog extends JDialog {
    private JPanel contentPane;
    private JComboBox shellScriptComboBox;
    private JButton shellScriptCommandButton;
    /**
     * 通用脚本
     */
    private JComboBox commonShellScriptComboBox;
    /**
     * 通用脚本点击
     */
    private JButton commonShellScriptCommandButton;
    private JButton closeScriptButton;
    private JRadioButton selectCommandCloseDialogRadioButton;
    private JButton dyCopyCommandButton;
    private JButton commonCopyCommandButton;
    private JLabel dyTipLabel;
    private JLabel constantLabel;
    private JTextField dyClassloaderHashTextField;

    private JButton dyCopyScCommandButton;

    private JButton dyClearCacheButton;


    private ActionLink dyScHelpLink;

    private CommandContext commandContext;

    private Project project;

    /**
     * 默认的 sc -d xxxClass
     */
    private String selectClassName = "";

    /**
     * 当前选择的动态脚本
     */
    private CustomComboBoxItem currentSelectDyScriptVariableEnum;

    /**
     * 当前选中的 静态文本信息
     */
    private CustomComboBoxItem currentSelectConstantScriptVariableEnum;


    /**
     * 防止手动修改 恶意添加了 classloader
     */
    public static Set<String> NEED_CLASSLOADER_COMMAND = Sets.newHashSet("logger --name", "dump", "jad", "vmtool", "ognl", "${CLASSLOADER_HASH_VALUE}");


    public ArthasShellScriptCommandDialog(AnActionEvent event) {
        this.commandContext = new CommandContext(event);
        $$$setupUI$$$();
        this.project = commandContext.getProject();
        this.selectClassName = commandContext.getKeyValue(ShellScriptVariableEnum.CLASS_NAME);
        setContentPane(contentPane);
        setMinimumSize(new Dimension(800, 340));
        setModal(true);
        getRootPane().setDefaultButton(closeScriptButton);
        closeScriptButton.addActionListener(e -> onOK());
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


    @SuppressWarnings("unchecked")
    private void init() {
        this.initDynamicShellScript();
        this.commonScriptInit();
        this.initSelectCommandCloseDialogSetting();
        this.initScClassloaderMethod();

    }

    /**
     * 初始化 classloader的逻辑
     */
    private void initScClassloaderMethod() {

        String classloaderHash = PropertiesComponentUtils.getValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE);
        dyClassloaderHashTextField.setText(classloaderHash);

        //clear cache的classloader的hash值的信息
        dyClearCacheButton.addActionListener(e -> onClearClassLoaderHashValue());

        //获取 sc -d classloader的命令
        dyCopyScCommandButton.addActionListener(e -> {
            String currentScCommand = "";
            if (this.currentSelectDyScriptVariableEnum != null) {
                ShellScriptCommandEnum scriptCommandEnum = (ShellScriptCommandEnum) this.currentSelectDyScriptVariableEnum.getContentObject();
                currentScCommand = scriptCommandEnum.getScCommand(commandContext);
            }
            if (StringUtils.isBlank(currentScCommand)) {
                if (StringUtils.isBlank(selectClassName)) {
                    NotifyUtils.notifyMessage(project, "sc -d class name is empty");
                    return;
                }
                currentScCommand = String.join(" ", "sc", "-d", this.selectClassName);
            }
            ClipboardUtils.setClipboardString(currentScCommand);
            NotifyUtils.notifyMessageDefault(project);
        });
    }

    /**
     * 删除之前的缓存classloader的信息
     */
    private void onClearClassLoaderHashValue() {
        dyClassloaderHashTextField.setText("");
        PropertiesComponentUtils.setValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE, "");
    }

    private void initSelectCommandCloseDialogSetting() {
        selectCommandCloseDialogRadioButton.addItemListener(e -> {
            if (e.getSource().equals(selectCommandCloseDialogRadioButton)) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    PropertiesComponentUtils.setValue("scriptDialogCloseWhenSelectedCommand", "n");
                } else if (e.getStateChange() == ItemEvent.SELECTED) {
                    PropertiesComponentUtils.setValue("scriptDialogCloseWhenSelectedCommand", "y");
                }
            }
        });
        AppSettingsState instance = AppSettingsState.getInstance(project);
        if ("y".equalsIgnoreCase(instance.scriptDialogCloseWhenSelectedCommand)) {
            selectCommandCloseDialogRadioButton.setSelected(true);
        }
    }

    /**
     * 初始化动态执行脚本
     */
    @SuppressWarnings("unchecked")
    private void initDynamicShellScript() {
        shellScriptCommandButton.addActionListener(e -> {
            Object selectedItem = shellScriptComboBox.getSelectedItem();
            assert selectedItem != null;
            String selectedItemStr = selectedItem.toString();
            String scCommand = "";
            if (this.currentSelectDyScriptVariableEnum != null) {
                ShellScriptCommandEnum shellScriptCommandEnum = (ShellScriptCommandEnum) currentSelectDyScriptVariableEnum.getContentObject();
                scCommand = shellScriptCommandEnum.getScCommand(this.commandContext);
                // 这里要处理一下 手动修改了comboBox的值 获取不到 是否需要classloader
                if (StringUtils.isNotBlank(scCommand)) {
                    final String selectedItemStrFinal = selectedItemStr;
                    AtomicBoolean containSc = new AtomicBoolean(false);
                    NEED_CLASSLOADER_COMMAND.forEach(needSc -> {
                        if (selectedItemStrFinal.contains(needSc)) {
                            containSc.set(true);
                        }
                    });
                    if (containSc.get()) {
                        selectedItemStr = String.join(" ", selectedItemStr, "-c", ShellScriptVariableEnum.CLASSLOADER_HASH_VALUE.getCode());
                    } else {
                        scCommand = "";
                    }
                }
            }
            // 这里再次处理一下上下文信息
            String finalStr = commandContext.getCommandCode(selectedItemStr);
            if (StringUtils.isNotBlank(finalStr)) {
                CommonExecuteScriptUtils.executeCommonScript(project, scCommand, finalStr, "");
            }
            this.doCloseDialog();
        });

        dyCopyCommandButton.addActionListener(e -> {
            Object selectedItem = shellScriptComboBox.getSelectedItem();
            assert selectedItem != null;
            String copyCommand = selectedItem.toString();
            if (this.currentSelectDyScriptVariableEnum != null) {
                ShellScriptCommandEnum shellScriptCommandEnum = (ShellScriptCommandEnum) currentSelectDyScriptVariableEnum.getContentObject();
                String scCommand = shellScriptCommandEnum.getScCommand(this.commandContext);
                if (StringUtils.isNotBlank(scCommand)) {
                    String hashClassloader = dyClassloaderHashTextField.getText();
                    if (StringUtils.isNotBlank(hashClassloader)) {
                        copyCommand = String.join(" ", copyCommand, "-c", hashClassloader);
                        PropertiesComponentUtils.setValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE, hashClassloader);
                    }
                }
            }
            ClipboardUtils.setClipboardString(copyCommand);
            NotifyUtils.notifyMessage(project, COMMAND_COPIED + "(Some commands need classloader hash value to be executed directly)");
            this.doCloseDialog();
        });
        shellScriptComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem() instanceof CustomComboBoxItem) {
                    CustomComboBoxItem item = (CustomComboBoxItem) e.getItem();
                    dyTipLabel.setText(item.getTipText());
                    this.currentSelectDyScriptVariableEnum = item;
                    ShellScriptCommandEnum shellScriptCommandEnum = (ShellScriptCommandEnum) item.getContentObject();
                    // 控制是否展示 sc command,比如watch 这种命令不需要sc
                    if (StringUtils.isBlank(shellScriptCommandEnum.getScCommand(commandContext))) {
                        dyCopyScCommandButton.setEnabled(false);
                    } else {
                        dyCopyScCommandButton.setEnabled(true);
                    }
                }
            }
        });
        shellScriptComboBox.setRenderer(new CustomDefaultListCellRenderer(shellScriptComboBox, dyTipLabel));
        // 增加点击事件 打开链接
        this.dyTipLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentSelectDyScriptVariableEnum != null) {
                    ShellScriptCommandEnum contentObject = (ShellScriptCommandEnum) currentSelectDyScriptVariableEnum.getContentObject();
                    if (contentObject != null && contentObject.getRefLink() != null) {
                        BrowserUtil.browse(contentObject.getRefLink());
                    }
                }
            }
        });

        for (ShellScriptCommandEnum shellScript : ShellScriptCommandEnum.values()) {
            if (!shellScript.support(this.commandContext)) {
                continue;
            }
            String displayCode = shellScript.getArthasCommand(commandContext);
            CustomComboBoxItem<ShellScriptCommandEnum> boxItem = new CustomComboBoxItem<ShellScriptCommandEnum>();
            boxItem.setContentObject(shellScript);
            boxItem.setDisplay(displayCode);
            boxItem.setTipText(shellScript.getEnumMsg());
            if (StringUtils.isNotBlank(shellScript.getRefLink())) {
                boxItem.setTipText(String.format(ArthasCommandConstants.LABEL_HTML_FORMAT_AND_LINK, shellScript.getRefLink(), shellScript.getEnumMsg()));
            }
            shellScriptComboBox.addItem(boxItem);
        }
    }

    /**
     * 关闭窗口
     */
    private void doCloseDialog() {
        AppSettingsState instance = AppSettingsState.getInstance(project);
        if ("y".equalsIgnoreCase(instance.scriptDialogCloseWhenSelectedCommand)) {
            dispose();
        }
    }

    /**
     * 常用脚本添加
     */
    @SuppressWarnings("unchecked")
    private void commonScriptInit() {
        boolean constantLabel = false;
        for (ShellScriptConstantEnum scriptConstantEnum : ShellScriptConstantEnum.values()) {
            CustomComboBoxItem<ShellScriptConstantEnum> boxItem = new CustomComboBoxItem<ShellScriptConstantEnum>();
            boxItem.setContentObject(scriptConstantEnum);
            boxItem.setDisplay(scriptConstantEnum.getCode());
            boxItem.setTipText(scriptConstantEnum.getEnumMsg());
            if (StringUtils.isNotBlank(scriptConstantEnum.getUrl())) {
                boxItem.setTipText(String.format(ArthasCommandConstants.LABEL_HTML_FORMAT_AND_LINK, scriptConstantEnum.getUrl(), scriptConstantEnum.getEnumMsg()));
            }
            commonShellScriptComboBox.addItem(boxItem);
            if (!constantLabel) {
                constantLabel = true;
                this.constantLabel.setText(boxItem.getTipText());
                this.currentSelectConstantScriptVariableEnum = boxItem;
            }

        }
        commonShellScriptComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem() instanceof CustomComboBoxItem) {
                    CustomComboBoxItem item = (CustomComboBoxItem) e.getItem();
                    this.constantLabel.setText(item.getTipText());
                    this.currentSelectConstantScriptVariableEnum = item;
                }
            }
        });
        // 监听文字的变化 根据是否有;号判断是否能够执行copy command
        try {
            ComboBoxEditor editor = commonShellScriptComboBox.getEditor();
            if (editor != null) {
                final JTextComponent tc = (JTextComponent) editor.getEditorComponent();
                tc.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        try {
                            String text = e.getDocument().getText(0, e.getDocument().getLength());
                            checkStaticCommandChange(text);
                        } catch (Exception ex) {
                            //
                        }
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        try {
                            String text = e.getDocument().getText(0, e.getDocument().getLength());
                            checkStaticCommandChange(text);
                        } catch (Exception ex) {
                            //
                        }
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        try {
                            String text = e.getDocument().getText(0, e.getDocument().getLength());
                            checkStaticCommandChange(text);
                        } catch (Exception ex) {
                            //
                        }
                    }
                });
            }
        } catch (Exception e) {
            //
        }

        commonShellScriptComboBox.setRenderer(new CustomDefaultListCellRenderer(commonShellScriptComboBox, this.constantLabel));
        commonShellScriptCommandButton.addActionListener(e -> {
            Object selectedItem = commonShellScriptComboBox.getSelectedItem();
            assert selectedItem != null;
            String selectedItemStr = selectedItem.toString();
            if (StringUtils.isNotBlank(selectedItemStr)) {
                CommonExecuteScriptUtils.executeCommonScript(project, "", selectedItemStr, "");
            }
            this.doCloseDialog();
        });
        commonCopyCommandButton.addActionListener(e -> {
            Object selectedItem = commonShellScriptComboBox.getSelectedItem();
            assert selectedItem != null;
            String selectedItemStr = selectedItem.toString();
            ClipboardUtils.setClipboardString(selectedItemStr);
            NotifyUtils.notifyMessage(project, COMMAND_COPIED + "(some of the batch scripts cannot be executed need to be modified manually)");
            this.doCloseDialog();
        });
        // 增加点击事件 打开链接
        this.constantLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentSelectConstantScriptVariableEnum != null) {
                    ShellScriptConstantEnum contentObject = (ShellScriptConstantEnum) currentSelectConstantScriptVariableEnum.getContentObject();
                    if (contentObject != null && contentObject.getUrl() != null) {
                        BrowserUtil.browse(contentObject.getUrl());
                    }
                }
            }
        });

    }

    private void checkStaticCommandChange(String currentText) {
        if (currentText != null && currentText.indexOf(";") > 0) {
            commonCopyCommandButton.setEnabled(false);
        } else {
            commonCopyCommandButton.setEnabled(true);
        }
    }

    private void onOK() {
        // add your code here
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
        //两个屏幕处理出现问题，跳到主屏幕去了
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        setVisible(true);

    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    private void createUIComponents() {
        this.dyScHelpLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/sc.html");
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
        contentPane.setLayout(new GridLayoutManager(11, 3, new Insets(5, 5, 5, 5), -1, -1));
        contentPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        dyTipLabel = new JLabel();
        dyTipLabel.setText("");
        contentPane.add(dyTipLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        constantLabel = new JLabel();
        constantLabel.setEnabled(true);
        constantLabel.setText("");
        contentPane.add(constantLabel, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(500, -1), null, 0, false));
        selectCommandCloseDialogRadioButton = new JRadioButton();
        selectCommandCloseDialogRadioButton.setText("close this window after seleced one command");
        contentPane.add(selectCommandCloseDialogRadioButton, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(438, 22), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("dymastic commands(show only the commands available at the current mouse position)");
        contentPane.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        dyScHelpLink.setText("sc -d get classloader");
        panel1.add(dyScHelpLink, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dyClassloaderHashTextField = new JTextField();
        panel1.add(dyClassloaderHashTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FormLayout("fill:846px:grow,fill:118px:noGrow", "center:d:grow"));
        contentPane.add(panel2, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        shellScriptComboBox = new JComboBox();
        shellScriptComboBox.setEditable(true);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        shellScriptComboBox.setModel(defaultComboBoxModel1);
        CellConstraints cc = new CellConstraints();
        panel2.add(shellScriptComboBox, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.CENTER));
        shellScriptCommandButton = new JButton();
        shellScriptCommandButton.setText("shell command");
        shellScriptCommandButton.setToolTipText("直接执行脚本无需打开arthas");
        panel2.add(shellScriptCommandButton, cc.xy(2, 1, CellConstraints.RIGHT, CellConstraints.CENTER));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new FormLayout("fill:846px:grow,fill:118px:noGrow", "center:d:grow"));
        contentPane.add(panel3, new GridConstraints(8, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        commonShellScriptComboBox = new JComboBox();
        commonShellScriptComboBox.setEditable(true);
        panel3.add(commonShellScriptComboBox, cc.xy(1, 1));
        commonShellScriptCommandButton = new JButton();
        commonShellScriptCommandButton.setText("shell command");
        commonShellScriptCommandButton.setToolTipText("直接执行脚本无需打开arthas");
        panel3.add(commonShellScriptCommandButton, cc.xy(2, 1, CellConstraints.RIGHT, CellConstraints.CENTER));
        dyCopyCommandButton = new JButton();
        dyCopyCommandButton.setText("copy  command");
        dyCopyCommandButton.setToolTipText("命令复制到剪切板 到服务器启动arhtas 执行命令 部分脚本可能不支持哦,需要classloader hashcode");
        contentPane.add(dyCopyCommandButton, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        dyCopyScCommandButton = new JButton();
        dyCopyScCommandButton.setText("copy sc command");
        contentPane.add(dyCopyScCommandButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        closeScriptButton = new JButton();
        closeScriptButton.setText("close  command");
        contentPane.add(closeScriptButton, new GridConstraints(10, 1, 1, 2, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        commonCopyCommandButton = new JButton();
        commonCopyCommandButton.setText("copy command");
        commonCopyCommandButton.setToolTipText("命令复制到剪切板 到服务器启动arhtas 执行命令 部分为批量脚本 不能执行哦");
        contentPane.add(commonCopyCommandButton, new GridConstraints(9, 1, 1, 2, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("static commands(multiple commands can only be used with shell command)");
        contentPane.add(label2, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        contentPane.add(separator1, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        dyClearCacheButton = new JButton();
        dyClearCacheButton.setText("clear cache");
        dyClearCacheButton.setToolTipText("clear  cache of previously used classloaders ");
        contentPane.add(dyClearCacheButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
