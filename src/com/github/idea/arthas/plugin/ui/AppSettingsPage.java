package com.github.idea.arthas.plugin.ui;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSS;
import com.amazonaws.services.s3.AmazonS3;
import com.github.idea.arthas.plugin.common.pojo.TunnelServerInfo;
import com.github.idea.arthas.plugin.constants.ArthasCommandConstants;
import com.github.idea.arthas.plugin.setting.AppSettingsState;
import com.github.idea.arthas.plugin.utils.ActionLinkUtils;
import com.github.idea.arthas.plugin.utils.AliyunOssUtils;
import com.github.idea.arthas.plugin.utils.JedisUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.github.idea.arthas.plugin.utils.OsS3Utils;
import com.github.idea.arthas.plugin.utils.PropertiesComponentUtils;
import com.github.idea.arthas.plugin.utils.StringUtils;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Vector;

import static com.github.idea.arthas.plugin.constants.ArthasCommandConstants.AT;
import static com.github.idea.arthas.plugin.utils.OpenConfigDialogUtils.OPEN_CONFIG_TAB;

/**
 * https://jetbrains.org/intellij/sdk/docs/reference_guide/settings_guide.html 属性配置 参考
 * https://github.com/pwielgolaski/shellcheck-plugin
 *
 * @author 汪小哥
 * @date 15-08-2020
 */
public class AppSettingsPage implements Configurable {
    /**
     * arthas 的设置
     */
    private JTextField springContextStaticOgnlExpressionTextFiled;
    /**
     * arthas -n
     */
    private JSpinner invokeCountField;
    private ActionLink springContextProviderLink;

    private JPanel contentPane;
    /**
     * 跳过jdk trace
     */
    private JRadioButton traceSkipJdkRadio;
    /**
     * 调用次数
     */
    private JSpinner invokeMonitorCountField;
    /**
     * 时间间隔
     */
    private JSpinner invokeMonitorIntervalField;

    /**
     * 打印属性的深度
     */
    private JSpinner depthPrintPropertyField;
    /**
     * 是否展示默认的条件表达式
     */
    private JRadioButton conditionExpressDisplayRadio;

    private JTextField selectProjectNameTextField;

    private ActionLink selectLink;
    private ActionLink batchSupportLink;
    private ActionLink redefineHelpActionLink;
    private ActionLink ossHelpLink;
    /**
     * 主pane
     */
    private JTabbedPane settingTabPane;
    /**
     * 基础设置pane
     */
    private JPanel basicSettingPane;
    /**
     * 热更新 面板
     */
    private JPanel hotRedefineSettingPane;
    /**
     * 设置选中 剪切板
     */
    private JRadioButton clipboardRadioButton;
    /**
     * 设置选中 阿里云
     */
    private JRadioButton aliYunOssRadioButton;
    /**
     * oss 配置信息 Endpoint
     */
    private JTextField ossEndpointTextField;
    /**
     * oss 配置信息 AccessKeyId
     */
    private JTextField ossAccessKeyIdPasswordField;
    /**
     * oss 配置信息 AccessKeySecret
     */
    private JTextField ossAccessKeySecretPasswordField;
    /**
     * oss 配置信息 DirectoryPrefix
     */
    private JTextField ossDirectoryPrefixTextField;
    /**
     * oss 配置信息 BucketName
     */
    private JTextField ossBucketNameTextField;
    /**
     * 检测 oss 配置是否正确 button
     */
    private JButton ossSettingCheckButton;

    /**
     * 检测异常的信息
     */
    private JLabel ossCheckMsgLabel;

    /**
     * 阿里云Oss Setting Pane
     */
    private JPanel aliyunOssSettingPane;
    /**
     * 全局spring context 开关
     */
    private JRadioButton springContextGlobalSettingRadioButton;
    /**
     * oss 全局开关
     */
    private JRadioButton ossGlobalSettingRadioButton;
    /**
     * 热更新完成后删除文件
     */
    private JRadioButton hotRedefineDeleteFileRadioButton;
    /**
     * watch/trace/monitor support verbose option, print ConditionExpress result #1348
     */
    private JRadioButton printConditionExpressRadioButton;

    private ActionLink printConditionExpressLink;
    /**
     * 热更新之前先编译
     */
    private JRadioButton redefineBeforeCompileRadioButton;
    private JRadioButton manualSelectPidRadioButton;
    private JRadioButton preConfigurationSelectPidRadioButton;
    private JPanel preConfigurationSelectPidPanel;
    /**
     * redis 选择 按钮
     */
    private JRadioButton redisRadioButton;
    private JPanel redisSettingPane;
    /**
     * redis 地址
     */
    private JTextField redisAddressTextField;
    /**
     * redis 端口
     */
    private JSpinner redisPortField;
    /**
     * redis 密码
     */
    private JTextField redisPasswordField;
    /**
     * redis 检测 button
     */
    private JButton redisCheckConfigButton;
    /**
     * 错误信息
     */
    private JLabel redisMessageLabel;
    /**
     * cache key
     */
    private JTextField redisCacheKeyTextField;
    /**
     * cache key ttl
     */
    private JSpinner redisCacheKeyTtl;

    /**
     * arthas zip 信息的地址
     */
    private JTextField arthasPackageZipDownloadUrlTextField;
    /**
     * spring service bean 的名称
     */
    private JTextField mybatisMapperReloadServiceBeanNameTextField;
    /**
     * spring bean 方法的名称
     */
    private JTextField mybatisMapperReloadMethodNameTextField;
    /**
     * 更多链接
     */
    private ActionLink mybatisMapperReloadHelpLink;
    /**
     * retransform 帮助文档
     */
    private ActionLink retransformHelpLink;
    /**
     * arthas retransformer 热更新功能浅析
     */
    private ActionLink analysisRetransformerLink;
    /**
     * github 地址
     */
    private ActionLink arthasIdeaGithubLink;
    /**
     * demo 地址
     */
    private ActionLink arthasIdeaDemoLink;
    /**
     * 语雀知识库链接
     */
    private ActionLink arthasYuQueDocumentLink;
    /**
     * 自动转换为中文编码
     */
    private JRadioButton autoToUnicodeRadioButton;
    /**
     * 自动将中文转换为 unicode 编码
     */
    private ActionLink howToInputChineseParamLink;

    private JRadioButton s3RadioButton;

    private JTextField s3EndPointField;

    private JTextField s3AkField;

    private JTextField s3SkField;

    private JTextField s3BucketNameField;

    private JTextField s3DirPrefixField;

    private JRadioButton s3GlobalConfigField;

    private JTextField s3RegionField;

    private JPanel s3Panel;

    private JLabel s3CheckMessageLabel;
    private JButton s3CheckButton;
    private JPanel tunnelServerSettingPanel;
    private JButton addButton;
    private JButton deleteButton;
    private JTable tunnelTable;
    private JPanel tablePanel;
    public static DefaultTableModel tableModel;
    private boolean tableModify = false;
    /**
     * 自动打开Arthas终端
     */
    private JRadioButton autoOpenArthasTerminalRadioButton;
    private ActionLink tunnelServerLabel;


    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Arthas Idea Plugin";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return springContextStaticOgnlExpressionTextFiled;
    }

    private Project project;

    /**
     * 设置信息
     */
    private AppSettingsState settings;

    /**
     * 自动构造  idea 会携带当前的project 参数信息
     *
     * @param project
     */
    public AppSettingsPage(Project project) {
        this.project = project;
        $$$setupUI$$$();
        settings = AppSettingsState.getInstance(this.project);
        tableModel = new DefaultTableModel(new String[]{"Name", "TunnelAddress", "WsAddress"}, 0);
    }

    private void createUIComponents() {
        this.springContextProviderLink = ActionLinkUtils.newActionLink("https://github.com/WangJi92/arthas-plugin-demo/blob/master/src/main/java/com/wangji92/arthas/plugin/demo/common/ApplicationContextProvider.java");
        this.selectLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/advanced-use.html");
        this.batchSupportLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/batch-support.html");
        this.redefineHelpActionLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/redefine.html#");
        this.ossHelpLink = ActionLinkUtils.newActionLink("https://helpcdn.aliyun.com/document_detail/84781.html?spm=a2c4g.11186623.6.823.148d1144LOadRS");
        this.printConditionExpressLink = ActionLinkUtils.newActionLink("https://github.com/alibaba/arthas/issues/1348");
        this.mybatisMapperReloadHelpLink = ActionLinkUtils.newActionLink("https://github.com/WangJi92/mybatis-mapper-reload-spring-boot-start");
        this.retransformHelpLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/retransform.html");
        this.analysisRetransformerLink = ActionLinkUtils.newActionLink("https://www.yuque.com/arthas-idea-plugin/help/lyevb2");
        this.arthasIdeaGithubLink = ActionLinkUtils.newActionLink("https://github.com/WangJi92/arthas-idea-plugin");
        this.arthasIdeaDemoLink = ActionLinkUtils.newActionLink("https://github.com/WangJi92/arthas-plugin-demo");
        this.arthasYuQueDocumentLink = ActionLinkUtils.newActionLink("https://www.yuque.com/arthas-idea-plugin");
        this.howToInputChineseParamLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/faq.html#%E8%BE%93%E5%85%A5%E4%B8%AD%E6%96%87-unicode-%E5%AD%97%E7%AC%A6");
        this.tunnelServerLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/tunnel.html#%E6%9C%80%E4%BD%B3%E5%AE%9E%E8%B7%B5");

    }

    @Nullable
    @Override
    public JComponent createComponent() {
        int openConfigTab = PropertiesComponent.getInstance().getInt(OPEN_CONFIG_TAB, 0);
        settingTabPane.setSelectedIndex(openConfigTab);
        return contentPane;
    }

    @Override
    public boolean isModified() {
        // IntelliJ may call isModified() before createComponent() in newer IDE versions (2024.x+)
        if (contentPane == null) {
            return false;
        }
        boolean modify = !springContextStaticOgnlExpressionTextFiled.getText().equals(settings.staticSpringContextOgnl)
                                 || !invokeCountField.getValue().toString().equals(settings.invokeCount)
                                 || !invokeMonitorCountField.getValue().toString().equals(settings.invokeMonitorCount)
                                 || !invokeMonitorIntervalField.getValue().toString().equals(settings.invokeMonitorInterval)
                                 || !depthPrintPropertyField.getValue().toString().equals(settings.depthPrintProperty)
                                 || !selectProjectNameTextField.getText().equals(settings.selectProjectName)
                                 || traceSkipJdkRadio.isSelected() != settings.traceSkipJdk
                                 || conditionExpressDisplayRadio.isSelected() != settings.conditionExpressDisplay
                                 || ossGlobalSettingRadioButton.isSelected() != settings.ossGlobalSetting
                                 || springContextGlobalSettingRadioButton.isSelected() != settings.springContextGlobalSetting
                                 || aliYunOssRadioButton.isSelected() != settings.aliYunOss
                                 || redisRadioButton.isSelected() != settings.hotRedefineRedis
                                 || s3RadioButton.isSelected() != settings.awsS3
                                 || hotRedefineDeleteFileRadioButton.isSelected() != settings.hotRedefineDelete
                                 || redefineBeforeCompileRadioButton.isSelected() != settings.redefineBeforeCompile
                                 || printConditionExpressRadioButton.isSelected() != settings.printConditionExpress
                                 || manualSelectPidRadioButton.isSelected() != settings.manualSelectPid
                                 || !arthasPackageZipDownloadUrlTextField.getText().equalsIgnoreCase(settings.arthasPackageZipDownloadUrl)
                                 || !mybatisMapperReloadMethodNameTextField.getText().equalsIgnoreCase(settings.mybatisMapperReloadMethodName)
                                 || !mybatisMapperReloadServiceBeanNameTextField.getText().equalsIgnoreCase(settings.mybatisMapperReloadServiceBeanName)
                                 || autoToUnicodeRadioButton.isSelected() != settings.autoToUnicode
                                 || autoOpenArthasTerminalRadioButton.isSelected() != settings.autoOpenArthasTerminal
                                 || tableModify
                                 || tunnelTable.getRowCount() != (settings.tunnelServerList == null ? 0 : settings.tunnelServerList.size());

        if (modify) {
            return modify;
        }
        if (aliYunOssRadioButton.isSelected()) {
            modify = !settings.endpoint.equals(ossEndpointTextField.getText())
                             || !settings.accessKeyId.equals(String.valueOf(ossAccessKeyIdPasswordField.getText()))
                             || !settings.accessKeySecret.equals(String.valueOf(ossAccessKeySecretPasswordField.getText()))
                             || !settings.bucketName.equals(ossBucketNameTextField.getText())
                             || !settings.directoryPrefix.equals(ossDirectoryPrefixTextField.getText());
        }

        if (s3RadioButton.isSelected()) {
            modify = !settings.s3Endpoint.equals(s3EndPointField.getText())
                             || !settings.s3AccessKeyId.equals(String.valueOf(s3AkField.getText()))
                             || !settings.s3AccessKeySecret.equals(String.valueOf(s3SkField.getText()))
                             || !settings.s3BucketName.equals(s3BucketNameField.getText())
                             || !settings.s3Region.equals(s3RegionField.getText())
                             || !settings.s3DirectoryPrefix.equals(s3DirPrefixField.getText());
        }

        if (redisRadioButton.isSelected()) {
            modify = !settings.redisAddress.equals(redisAddressTextField.getText())
                             || !settings.redisPort.equals((redisPortField.getValue()))
                             || !settings.redisAuth.equals(String.valueOf(redisPasswordField.getText()))
                             || !settings.redisCacheKey.equals(redisCacheKeyTextField.getText())
                             || !settings.redisCacheKeyTtl.equals(redisCacheKeyTtl.getValue());
        }
        return modify;
    }

    @Override
    public void apply() {
        saveSettings();
    }

    @Override
    public void reset() {
        loadSettings();
    }

    /**
     * 保存配置
     */
    private void saveSettings() {
        StringBuilder error = new StringBuilder();
        this.saveStaticSpringContext(error);
        this.saveMybatisMapperSetting();
        if (((int) invokeCountField.getValue()) <= 0) {
            error.append("invokeCountField <= 0 ");
        } else {
            settings.invokeCount = invokeCountField.getValue().toString();
        }
        if (((int) invokeMonitorCountField.getValue()) <= 0) {
            error.append("invokeMonitorCount <= 0 ");
        } else {
            settings.invokeMonitorCount = invokeMonitorCountField.getValue().toString();
        }
        if (((int) invokeMonitorIntervalField.getValue()) <= 0) {
            error.append("invokeMonitorCount <= 0 ");
        } else {
            settings.invokeMonitorInterval = invokeMonitorIntervalField.getValue().toString();
        }
        if (((int) depthPrintPropertyField.getValue()) <= 0) {
            error.append("invokeMonitorCount <= 0 ");
        } else {
            settings.depthPrintProperty = depthPrintPropertyField.getValue().toString();
        }
        settings.traceSkipJdk = traceSkipJdkRadio.isSelected();
        settings.conditionExpressDisplay = conditionExpressDisplayRadio.isSelected();
        settings.selectProjectName = selectProjectNameTextField.getText();
        settings.manualSelectPid = manualSelectPidRadioButton.isSelected();
        settings.hotRedefineDelete = hotRedefineDeleteFileRadioButton.isSelected();
        settings.redefineBeforeCompile = redefineBeforeCompileRadioButton.isSelected();
        settings.printConditionExpress = printConditionExpressRadioButton.isSelected();
        settings.arthasPackageZipDownloadUrl = arthasPackageZipDownloadUrlTextField.getText();
        settings.autoToUnicode = autoToUnicodeRadioButton.isSelected();
        settings.autoOpenArthasTerminal = autoOpenArthasTerminalRadioButton.isSelected();
        PropertiesComponentUtils.setValue("autoToUnicode", autoToUnicodeRadioButton.isSelected() ? "y" : "n");
        PropertiesComponentUtils.setValue("autoOpenArthasTerminal", autoOpenArthasTerminalRadioButton.isSelected() ? "y" : "n");
        // 设置到全局
        PropertiesComponentUtils.setValue("arthasPackageZipDownloadUrl", arthasPackageZipDownloadUrlTextField.getText());
        List<TunnelServerInfo> tunnelServerList = this.getTunnelServerInfoList();
        settings.tunnelServerList = tunnelServerList;
        PropertiesComponentUtils.setValue("ArthasTunnelServerList", JSON.toJSONString(tunnelServerList));

        if (clipboardRadioButton.isSelected()) {
            settings.hotRedefineClipboard = true;
            settings.aliYunOss = false;
            settings.hotRedefineRedis = false;
            settings.awsS3 = false;
            PropertiesComponentUtils.setValue("storageType", "hotRedefineClipboard");
        } else if (aliYunOssRadioButton.isSelected()) {
            this.saveAliyunOssConfig(error);
        } else if (redisRadioButton.isSelected()) {
            this.saveRedisConfig(error);
        } else if (s3RadioButton.isSelected()) {
            this.saveS3Config(error);
        }

        if (StringUtils.isNotBlank(error)) {
            NotifyUtils.notifyMessage(project, error.toString(), NotificationType.ERROR);
        }

    }

    @NotNull
    private List<TunnelServerInfo> getTunnelServerInfoList() {
        Vector<Vector> dataVector = ((DefaultTableModel) tunnelTable.getModel()).getDataVector();
        return dataVector.stream()
                       .map(vector -> new TunnelServerInfo(vector.get(0).toString(), vector.get(1).toString(), StringUtils.toString(vector.get(2))))
                       .toList();
    }

    /**
     * 保存mybatis mapper reload 配置
     */
    private void saveMybatisMapperSetting() {
        String mybatisMapperReloadServiceBeanNameTex = mybatisMapperReloadServiceBeanNameTextField.getText();
        String mybatisMapperReloadMethodNameText = mybatisMapperReloadMethodNameTextField.getText();
        if (StringUtils.isBlank(mybatisMapperReloadMethodNameText) || StringUtils.isBlank(mybatisMapperReloadServiceBeanNameTex)) {
            return;
        }
        settings.mybatisMapperReloadMethodName = mybatisMapperReloadMethodNameText;
        settings.mybatisMapperReloadServiceBeanName = mybatisMapperReloadServiceBeanNameTex;
        PropertiesComponentUtils.setValue("mybatisMapperReloadMethodName", mybatisMapperReloadMethodNameText);
        PropertiesComponentUtils.setValue("mybatisMapperReloadServiceBeanName", mybatisMapperReloadServiceBeanNameTex);
    }

    /**
     * 保存spring static 配置信息
     *
     * @param error
     */
    private void saveStaticSpringContext(StringBuilder error) {
        String staticOgnlExpressionTextFiledText = springContextStaticOgnlExpressionTextFiled.getText();
        if (StringUtils.isBlank(staticOgnlExpressionTextFiledText)) {
            settings.staticSpringContextOgnl = "";
            //全局设置
            if (springContextGlobalSettingRadioButton.isSelected() && !ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING.equals(settings.staticSpringContextOgnl)) {
                PropertiesComponentUtils.setValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION, staticOgnlExpressionTextFiledText);
            }
            return;
        }

        if (!staticOgnlExpressionTextFiledText.contains(AT)) {
            error.append("配置静态spring context 错误");
            return;
        }
        settings.staticSpringContextOgnl = staticOgnlExpressionTextFiledText;
        settings.springContextGlobalSetting = springContextGlobalSettingRadioButton.isSelected();
        //全局设置
        if (springContextGlobalSettingRadioButton.isSelected() && !ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING.equals(settings.staticSpringContextOgnl)) {
            PropertiesComponentUtils.setValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION, staticOgnlExpressionTextFiledText);
        }
    }

    /**
     * 保存redis的配置信息
     *
     * @param error
     */
    private void saveRedisConfig(StringBuilder error) {
        if (((int) redisCacheKeyTtl.getValue()) <= 0) {
            error.append("redisCacheKeyTtl <= 0 ");
        } else {
            settings.redisCacheKeyTtl = (Integer) redisCacheKeyTtl.getValue();
        }
        if (StringUtils.isBlank(redisCacheKeyTextField.getText())) {
            settings.redisCacheKey = "arthasIdeaPluginRedefineCacheKey";
        }
        try (Jedis jedis = JedisUtils.buildJedisClient(redisAddressTextField.getText(), (Integer) redisPortField.getValue(), 5000, String.valueOf(redisPasswordField.getText()));) {
            JedisUtils.checkRedisClient(jedis);
            settings.redisAddress = redisAddressTextField.getText();
            settings.redisPort = (Integer) redisPortField.getValue();
            settings.redisAuth = String.valueOf(redisPasswordField.getText());
            settings.hotRedefineRedis = true;
            settings.aliYunOss = false;
            settings.awsS3 = false;
            settings.hotRedefineClipboard = false;
            PropertiesComponentUtils.setValue("storageType", "hotRedefineRedis");
            PropertiesComponentUtils.setValue("redisAddress", settings.redisAddress);
            PropertiesComponentUtils.setValue("redisPort", "" + settings.redisPort);
            PropertiesComponentUtils.setValue("redisAuth", settings.redisAuth);
            PropertiesComponentUtils.setValue("redisCacheKey", settings.redisCacheKey);
            PropertiesComponentUtils.setValue("redisCacheKeyTtl", "" + settings.redisCacheKeyTtl);
        } catch (Exception ex) {
            error.append(ex.getMessage());
        }
    }

    /**
     * 保存 阿里云oss的配置
     *
     * @param error
     */
    private void saveAliyunOssConfig(StringBuilder error) {
        OSS oss = null;
        try {
            oss = AliyunOssUtils.buildOssClient(ossEndpointTextField.getText(), String.valueOf(ossAccessKeyIdPasswordField.getText()), String.valueOf(ossAccessKeySecretPasswordField.getText()), ossBucketNameTextField.getText(), ossDirectoryPrefixTextField.getText());
            AliyunOssUtils.checkBuckNameExist(ossBucketNameTextField.getText(), oss);
            settings.endpoint = ossEndpointTextField.getText();
            settings.accessKeyId = String.valueOf(ossAccessKeyIdPasswordField.getText());
            settings.accessKeySecret = String.valueOf(ossAccessKeySecretPasswordField.getText());
            settings.bucketName = ossBucketNameTextField.getText();
            settings.directoryPrefix = ossDirectoryPrefixTextField.getText();
            settings.aliYunOss = true;
            settings.hotRedefineRedis = false;
            settings.awsS3 = false;
            settings.hotRedefineClipboard = false;
            settings.ossGlobalSetting = ossGlobalSettingRadioButton.isSelected();
            if (ossGlobalSettingRadioButton.isSelected()) {
                PropertiesComponentUtils.setValue("storageType", "aliYunOss");
                PropertiesComponentUtils.setValue("endpoint", settings.endpoint);
                PropertiesComponentUtils.setValue("accessKeyId", settings.accessKeyId);
                PropertiesComponentUtils.setValue("accessKeySecret", settings.accessKeySecret);
                PropertiesComponentUtils.setValue("bucketName", settings.bucketName);
                PropertiesComponentUtils.setValue("directoryPrefix", settings.directoryPrefix);
            }
            oss.shutdown();
        } catch (Exception e) {
            error.append(e.getMessage());
        } finally {
            if (oss != null) {
                oss.shutdown();
            }
        }
    }

    /**
     * aws3
     *
     * @param error
     */
    private void saveS3Config(StringBuilder error) {
        AmazonS3 s3 = null;
        try {
            s3 = OsS3Utils.buildS3Client(s3EndPointField.getText(),
                    String.valueOf(s3AkField.getText()),
                    String.valueOf(s3SkField.getText()),
                    s3BucketNameField.getText(),
                    s3RegionField.getText(),
                    s3DirPrefixField.getText());
            OsS3Utils.checkBuckNameExist(s3BucketNameField.getText(), s3);
            settings.s3Endpoint = s3EndPointField.getText();
            settings.s3AccessKeyId = String.valueOf(s3AkField.getText());
            settings.s3AccessKeySecret = String.valueOf(s3SkField.getText());
            settings.s3BucketName = s3BucketNameField.getText();
            settings.s3DirectoryPrefix = s3DirPrefixField.getText();
            settings.s3Region = s3RegionField.getText();
            settings.awsS3 = true;
            settings.hotRedefineRedis = false;
            settings.aliYunOss = false;
            settings.hotRedefineClipboard = false;
            settings.s3GlobalConfig = s3GlobalConfigField.isSelected();
            if (s3GlobalConfigField.isSelected()) {
                PropertiesComponentUtils.setValue("storageType", "awsS3");
                PropertiesComponentUtils.setValue("s3Endpoint", settings.s3Endpoint);
                PropertiesComponentUtils.setValue("s3AccessKeyId", settings.s3AccessKeyId);
                PropertiesComponentUtils.setValue("s3AccessKeySecret", settings.s3AccessKeySecret);
                PropertiesComponentUtils.setValue("s3BucketName", settings.s3BucketName);
                PropertiesComponentUtils.setValue("s3DirectoryPrefix", settings.s3DirectoryPrefix);
                PropertiesComponentUtils.setValue("s3Region", settings.s3Region);
            }
        } catch (Exception e) {
            error.append(e.getMessage());
        } finally {
            if (s3 != null) {
                s3.shutdown();
            }
        }
    }

    /**
     * 加载配置
     */
    private void loadSettings() {
        springContextStaticOgnlExpressionTextFiled.setText(settings.staticSpringContextOgnl);
        invokeCountField.setValue(Integer.parseInt(settings.invokeCount));
        invokeMonitorCountField.setValue(Integer.parseInt(settings.invokeMonitorCount));
        invokeMonitorIntervalField.setValue(Integer.parseInt(settings.invokeMonitorInterval));
        depthPrintPropertyField.setValue(Integer.parseInt(settings.depthPrintProperty));
        traceSkipJdkRadio.setSelected(settings.traceSkipJdk);
        conditionExpressDisplayRadio.setSelected(settings.conditionExpressDisplay);
        hotRedefineDeleteFileRadioButton.setSelected(settings.hotRedefineDelete);
        redefineBeforeCompileRadioButton.setSelected(settings.redefineBeforeCompile);
        printConditionExpressRadioButton.setSelected(settings.printConditionExpress);
        autoToUnicodeRadioButton.setSelected(settings.autoToUnicode);
        autoOpenArthasTerminalRadioButton.setSelected(settings.autoOpenArthasTerminal);
        selectProjectNameTextField.setText(settings.selectProjectName);

        ossEndpointTextField.setText(settings.endpoint);
        ossAccessKeyIdPasswordField.setText(settings.accessKeyId);
        ossAccessKeySecretPasswordField.setText(settings.accessKeySecret);
        ossBucketNameTextField.setText(settings.bucketName);
        ossDirectoryPrefixTextField.setText(settings.directoryPrefix);
        redisAddressTextField.setText(settings.redisAddress);
        redisPortField.setValue(settings.redisPort);
        redisPasswordField.setText(settings.redisAuth);
        redisCacheKeyTtl.setValue(settings.redisCacheKeyTtl);
        redisCacheKeyTextField.setText(settings.redisCacheKey);

        s3EndPointField.setText(settings.s3Endpoint);
        s3AkField.setText(settings.s3AccessKeyId);
        s3SkField.setText(settings.s3AccessKeySecret);
        s3BucketNameField.setText(settings.s3BucketName);
        s3DirPrefixField.setText(settings.s3DirectoryPrefix);
        s3RegionField.setText(settings.s3Region);
        s3GlobalConfigField.setSelected(settings.s3GlobalConfig);

        if (settings.aliYunOss) {
            // 阿里云oss
            aliYunOssRadioButton.setSelected(true);
            redisSettingPane.setVisible(false);
            s3Panel.setVisible(false);
            aliyunOssSettingPane.setVisible(true);
        } else if (settings.hotRedefineRedis) {
            // redis
            aliyunOssSettingPane.setVisible(false);
            redisRadioButton.setSelected(true);
            s3Panel.setVisible(false);
            redisSettingPane.setVisible(true);
        } else if (settings.awsS3) {
            s3RadioButton.setSelected(true);
            redisSettingPane.setVisible(false);
            s3Panel.setVisible(true);
            aliyunOssSettingPane.setVisible(false);
        } else {
            // 剪切板
            clipboardRadioButton.setSelected(true);
            aliyunOssSettingPane.setVisible(false);
            redisSettingPane.setVisible(false);
            s3Panel.setVisible(false);
        }
        if (settings.manualSelectPid) {
            preConfigurationSelectPidPanel.setVisible(false);
            preConfigurationSelectPidRadioButton.setSelected(false);
            manualSelectPidRadioButton.setSelected(true);
        } else {
            preConfigurationSelectPidPanel.setVisible(true);
            preConfigurationSelectPidRadioButton.setSelected(true);
            manualSelectPidRadioButton.setSelected(false);
        }
        springContextGlobalSettingRadioButton.setSelected(settings.springContextGlobalSetting);
        ossGlobalSettingRadioButton.setSelected(settings.ossGlobalSetting);

        // 设置远程的下载地址
        arthasPackageZipDownloadUrlTextField.setText(settings.arthasPackageZipDownloadUrl);
        mybatisMapperReloadMethodNameTextField.setText(settings.mybatisMapperReloadMethodName);
        mybatisMapperReloadServiceBeanNameTextField.setText(settings.mybatisMapperReloadServiceBeanName);
        initEvent();
    }

    private void initEvent() {
        ossCheckMsgLabel.setText("");
        ossCheckMsgLabel.setForeground(JBColor.BLACK);
        ossSettingCheckButton.addActionListener(e -> {
            OSS oss = null;
            try {
                oss = AliyunOssUtils.buildOssClient(ossEndpointTextField.getText(), String.valueOf(ossAccessKeyIdPasswordField.getText()), String.valueOf(ossAccessKeySecretPasswordField.getText()), ossBucketNameTextField.getText(), ossDirectoryPrefixTextField.getText());
                AliyunOssUtils.checkBuckNameExist(ossBucketNameTextField.getText(), oss);
                oss.shutdown();
                ossCheckMsgLabel.setText("oss setting check success");
                ossCheckMsgLabel.setForeground(JBColor.BLACK);
            } catch (Exception ex) {
                ossCheckMsgLabel.setText(ex.getMessage());
                ossCheckMsgLabel.setForeground(JBColor.RED);
            } finally {
                if (oss != null) {
                    oss.shutdown();
                }
            }
        });

        s3CheckButton.addActionListener(e -> {
            AmazonS3 s3 = null;
            try {
                s3 = OsS3Utils.buildS3Client(s3EndPointField.getText(),
                        String.valueOf(s3AkField.getText()),
                        String.valueOf(s3SkField.getText()),
                        s3BucketNameField.getText(),
                        s3RegionField.getText(),
                        s3DirPrefixField.getText());
                OsS3Utils.checkBuckNameExist(s3BucketNameField.getText(), s3);
                s3.shutdown();
                s3CheckMessageLabel.setText("s3 setting check success");
                s3CheckMessageLabel.setForeground(JBColor.BLACK);
            } catch (Exception ex) {
                s3CheckMessageLabel.setText(ex.getMessage());
                s3CheckMessageLabel.setForeground(JBColor.RED);
            } finally {
                if (s3 != null) {
                    s3.shutdown();
                }
            }
        });

        redisCheckConfigButton.addActionListener(e -> {
            try (Jedis jedis = JedisUtils.buildJedisClient(redisAddressTextField.getText(), (Integer) redisPortField.getValue(), 5000, String.valueOf(redisPasswordField.getText()));) {
                JedisUtils.checkRedisClient(jedis);
                redisMessageLabel.setText("redis setting check success");
                redisMessageLabel.setForeground(JBColor.BLACK);
            } catch (Exception ex) {
                redisMessageLabel.setText(ex.getMessage());
                redisMessageLabel.setForeground(JBColor.RED);
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(aliYunOssRadioButton);
        group.add(clipboardRadioButton);
        group.add(redisRadioButton);
        group.add(s3RadioButton);
        ItemListener itemListener = e -> {
            if (e.getSource().equals(aliYunOssRadioButton) && e.getStateChange() == ItemEvent.SELECTED) {
                aliyunOssSettingPane.setVisible(true);
                redisSettingPane.setVisible(false);
                s3Panel.setVisible(false);
            } else if (e.getSource().equals(clipboardRadioButton) && e.getStateChange() == ItemEvent.SELECTED) {
                aliyunOssSettingPane.setVisible(false);
                redisSettingPane.setVisible(false);
                s3Panel.setVisible(false);
            } else if (e.getSource().equals(redisRadioButton) && e.getStateChange() == ItemEvent.SELECTED) {
                aliyunOssSettingPane.setVisible(false);
                redisSettingPane.setVisible(true);
                s3Panel.setVisible(false);
            } else if (e.getSource().equals(s3RadioButton) && e.getStateChange() == ItemEvent.SELECTED) {
                aliyunOssSettingPane.setVisible(false);
                redisSettingPane.setVisible(false);
                s3Panel.setVisible(true);
            }
            ossCheckMsgLabel.setText("");
            redisMessageLabel.setText("");
            s3CheckMessageLabel.setText("");
        };
        aliYunOssRadioButton.addItemListener(itemListener);
        clipboardRadioButton.addItemListener(itemListener);
        redisRadioButton.addItemListener(itemListener);
        s3RadioButton.addItemListener(itemListener);

        // 设置是否手动选择pid
        ItemListener itemListenerSelectPid = e -> {
            if (e.getSource().equals(manualSelectPidRadioButton) && e.getStateChange() == ItemEvent.SELECTED || e.getSource().equals(preConfigurationSelectPidRadioButton) && e.getStateChange() == ItemEvent.DESELECTED) {
                preConfigurationSelectPidPanel.setVisible(false);
                preConfigurationSelectPidRadioButton.setSelected(false);
                manualSelectPidRadioButton.setSelected(true);
            } else if (e.getSource().equals(preConfigurationSelectPidRadioButton) && e.getStateChange() == ItemEvent.SELECTED || e.getSource().equals(manualSelectPidRadioButton) && e.getStateChange() == ItemEvent.DESELECTED) {
                preConfigurationSelectPidPanel.setVisible(true);
                preConfigurationSelectPidRadioButton.setSelected(true);
                manualSelectPidRadioButton.setSelected(false);
            }
        };
        manualSelectPidRadioButton.addItemListener(itemListenerSelectPid);
        preConfigurationSelectPidRadioButton.addItemListener(itemListenerSelectPid);

        addButton.addActionListener(e -> new AddTunnelServer(project).open());
        deleteButton.addActionListener(e -> {
            int selectedRow = tunnelTable.getSelectedRow();
            if (selectedRow != -1) {
                tableModel.removeRow(selectedRow);
            }
        });
        tunnelTable = new JBTable(tableModel);
        addTableModelListener(tableModel);
        tunnelTable.setRowHeight(30);
        tunnelTable.getColumnModel().getColumn(0).setMaxWidth(200);
        tunnelTable.getColumnModel().getColumn(1).setMaxWidth(400);
        tunnelTable.getColumnModel().getColumn(2).setMaxWidth(400);
        Optional.ofNullable(settings.tunnelServerList).stream()
                .flatMap(Collection::stream).map(TunnelServerInfo::toObjArr).forEach(tableModel::addRow);
        JScrollPane scrollPane = new JBScrollPane(tunnelTable);
        BorderLayout borderLayout = new BorderLayout();
        tablePanel.setLayout(borderLayout);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

    }

    @Override
    public void disposeUIResources() {
        contentPane = null;
    }

    private void addTableModelListener(DefaultTableModel model) {
        model.addTableModelListener(e -> {
            int type = e.getType();
            if (type == TableModelEvent.UPDATE) {
                if (e.getColumn() != TableModelEvent.ALL_COLUMNS) {
                    tableModify = true;
                }
            }
        });
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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        settingTabPane = new JTabbedPane();
        settingTabPane.setToolTipText("");
        contentPane.add(settingTabPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        settingTabPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        basicSettingPane = new JPanel();
        basicSettingPane.setLayout(new GridLayoutManager(13, 4, new Insets(10, 10, 10, 10), -1, -1));
        settingTabPane.addTab("Basic Setting", basicSettingPane);
        basicSettingPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setFocusable(false);
        label1.setText("Spring Static Context Setting Example");
        basicSettingPane.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(99, 16), null, 0, false));
        final Spacer spacer1 = new Spacer();
        basicSettingPane.add(spacer1, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        springContextProviderLink.setFocusable(false);
        springContextProviderLink.setText("Spring Context eg:@applicationContextProvider@context");
        basicSettingPane.add(springContextProviderLink, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setFocusable(false);
        label2.setText("Spring Static  Context Ognl Setting");
        label2.setToolTipText("@XXXCLass@xxStaticField 静态的spring context完整路径");
        basicSettingPane.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        springContextStaticOgnlExpressionTextFiled = new JTextField();
        basicSettingPane.add(springContextStaticOgnlExpressionTextFiled, new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setFocusable(false);
        label3.setText("Invoke Count(-n)");
        label3.setToolTipText("watch trace 调用次数");
        basicSettingPane.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        invokeCountField = new JSpinner();
        basicSettingPane.add(invokeCountField, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setFocusable(false);
        label4.setText("Invoke Monitor Count(-n)");
        label4.setToolTipText("监控次数");
        basicSettingPane.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        invokeMonitorCountField = new JSpinner();
        basicSettingPane.add(invokeMonitorCountField, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setFocusable(false);
        label5.setText("Invoke MoInitor Interval(--cycle)");
        label5.setToolTipText("调用监控的时间间隔");
        basicSettingPane.add(label5, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        invokeMonitorIntervalField = new JSpinner();
        basicSettingPane.add(invokeMonitorIntervalField, new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setFocusable(false);
        label6.setText("Depth  Print Property (-x)");
        label6.setToolTipText("指定输出结果的属性遍历深度");
        basicSettingPane.add(label6, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        depthPrintPropertyField = new JSpinner();
        basicSettingPane.add(depthPrintPropertyField, new GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setFocusable(false);
        label7.setText("Trace skipJDKMethod(--skipJDKMethod)");
        label7.setToolTipText("默认是否跳过jdk方法");
        basicSettingPane.add(label7, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        traceSkipJdkRadio = new JRadioButton();
        traceSkipJdkRadio.setText("Skip Jdk method");
        basicSettingPane.add(traceSkipJdkRadio, new GridConstraints(7, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setFocusable(false);
        label8.setText("Default Condition Express Display");
        label8.setToolTipText("默认条件表达式展示否");
        basicSettingPane.add(label8, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        conditionExpressDisplayRadio = new JRadioButton();
        conditionExpressDisplayRadio.setText("Show the default conditional expression 1==1 ");
        basicSettingPane.add(conditionExpressDisplayRadio, new GridConstraints(8, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        springContextGlobalSettingRadioButton = new JRadioButton();
        springContextGlobalSettingRadioButton.setFocusable(false);
        springContextGlobalSettingRadioButton.setText("Spring static context global configuration");
        springContextGlobalSettingRadioButton.setToolTipText("当前配置设置为全局默认配置");
        basicSettingPane.add(springContextGlobalSettingRadioButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setFocusable(false);
        label9.setText("Print ConditionExpress Result(-v)");
        basicSettingPane.add(label9, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        printConditionExpressRadioButton = new JRadioButton();
        printConditionExpressRadioButton.setText("Print the result of the execution of a conditional expression");
        basicSettingPane.add(printConditionExpressRadioButton, new GridConstraints(9, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        printConditionExpressLink.setText("ConditionExpress Issue Link");
        basicSettingPane.add(printConditionExpressLink, new GridConstraints(9, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        arthasIdeaGithubLink.setFocusable(false);
        arthasIdeaGithubLink.setText("arthas idea plugin");
        basicSettingPane.add(arthasIdeaGithubLink, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        arthasIdeaDemoLink.setFocusable(false);
        arthasIdeaDemoLink.setText("arthas idea plugin demo");
        basicSettingPane.add(arthasIdeaDemoLink, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        arthasYuQueDocumentLink.setFocusable(false);
        arthasYuQueDocumentLink.setText("arthas idea plugin yuque document");
        basicSettingPane.add(arthasYuQueDocumentLink, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setFocusable(false);
        label10.setText("Auto Convert Chinese To Unicode");
        label10.setToolTipText("输入参数为中文自动转换为unicode 编码");
        basicSettingPane.add(label10, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        howToInputChineseParamLink.setText("How To Input Chinese Param Link");
        basicSettingPane.add(howToInputChineseParamLink, new GridConstraints(10, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        autoToUnicodeRadioButton = new JRadioButton();
        autoToUnicodeRadioButton.setText("Auto To Unicode");
        basicSettingPane.add(autoToUnicodeRadioButton, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setFocusable(false);
        label11.setText("Auto Open Arthas Terminal");
        label11.setToolTipText("选择指令后自动打开Arthas Terminal");
        basicSettingPane.add(label11, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        autoOpenArthasTerminalRadioButton = new JRadioButton();
        autoOpenArthasTerminalRadioButton.setText("Auto Open Arthas Terminal");
        basicSettingPane.add(autoOpenArthasTerminalRadioButton, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hotRedefineSettingPane = new JPanel();
        hotRedefineSettingPane.setLayout(new GridLayoutManager(8, 6, new Insets(10, 10, 10, 10), -1, -1));
        hotRedefineSettingPane.setToolTipText("Hot swap to store shell files");
        settingTabPane.addTab("Storage And Script Setting", hotRedefineSettingPane);
        final JLabel label12 = new JLabel();
        label12.setFocusable(false);
        label12.setText("Storage configuration options");
        label12.setToolTipText("redefine 的时候需要将更新的class 上传到目标服务器");
        hotRedefineSettingPane.add(label12, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        hotRedefineSettingPane.add(spacer2, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        clipboardRadioButton = new JRadioButton();
        clipboardRadioButton.setText("Clipboard");
        clipboardRadioButton.setToolTipText("直接使用剪切板class文件比较大剪切板的就比较长");
        hotRedefineSettingPane.add(clipboardRadioButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        hotRedefineSettingPane.add(spacer3, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        aliYunOssRadioButton = new JRadioButton();
        aliYunOssRadioButton.setText("Ali Cloud oss");
        aliYunOssRadioButton.setToolTipText("使用阿里云oss 存储，本地将构造好的脚本上传到阿里云oss");
        hotRedefineSettingPane.add(aliYunOssRadioButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        aliyunOssSettingPane = new JPanel();
        aliyunOssSettingPane.setLayout(new GridLayoutManager(8, 5, new Insets(5, 5, 5, 5), -1, -1));
        hotRedefineSettingPane.add(aliyunOssSettingPane, new GridConstraints(1, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(0, 51), null, 0, false));
        aliyunOssSettingPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label13 = new JLabel();
        label13.setText("Oss Endpoint");
        label13.setToolTipText("eg: http://oss-cn-hangzhou.aliyuncs.com");
        aliyunOssSettingPane.add(label13, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        aliyunOssSettingPane.add(spacer4, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        ossEndpointTextField = new JTextField();
        aliyunOssSettingPane.add(ossEndpointTextField, new GridConstraints(0, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("Oss AccessKeyId");
        label14.setToolTipText("yourAccessKeyId");
        aliyunOssSettingPane.add(label14, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ossAccessKeyIdPasswordField = new JTextField();
        aliyunOssSettingPane.add(ossAccessKeyIdPasswordField, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setText("Oss AccessKeySecret");
        label15.setToolTipText("yourAccessKeySecret");
        aliyunOssSettingPane.add(label15, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ossAccessKeySecretPasswordField = new JTextField();
        aliyunOssSettingPane.add(ossAccessKeySecretPasswordField, new GridConstraints(2, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label16 = new JLabel();
        label16.setText("Oss BucketName");
        label16.setToolTipText("yourBucketName");
        aliyunOssSettingPane.add(label16, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ossBucketNameTextField = new JTextField();
        aliyunOssSettingPane.add(ossBucketNameTextField, new GridConstraints(3, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label17 = new JLabel();
        label17.setText("Oss Directory Prefix");
        label17.setToolTipText("oss 存储文件空间的地址前缀可以为空( abc/efg/ 为前缀  abc/efg/123.jpg)");
        aliyunOssSettingPane.add(label17, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ossDirectoryPrefixTextField = new JTextField();
        aliyunOssSettingPane.add(ossDirectoryPrefixTextField, new GridConstraints(4, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer5 = new Spacer();
        aliyunOssSettingPane.add(spacer5, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        ossSettingCheckButton = new JButton();
        ossSettingCheckButton.setText("Oss config detection");
        aliyunOssSettingPane.add(ossSettingCheckButton, new GridConstraints(6, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ossHelpLink.setText("Oss Help");
        aliyunOssSettingPane.add(ossHelpLink, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ossCheckMsgLabel = new JLabel();
        ossCheckMsgLabel.setText("");
        aliyunOssSettingPane.add(ossCheckMsgLabel, new GridConstraints(6, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label18 = new JLabel();
        label18.setText("Oss Setting Scope");
        label18.setToolTipText("Oss 设置作用范围");
        aliyunOssSettingPane.add(label18, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        aliyunOssSettingPane.add(spacer6, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        ossGlobalSettingRadioButton = new JRadioButton();
        ossGlobalSettingRadioButton.setText("Oss global configuration");
        ossGlobalSettingRadioButton.setToolTipText("是否当前配置作为全局默认配置");
        aliyunOssSettingPane.add(ossGlobalSettingRadioButton, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        preConfigurationSelectPidPanel = new JPanel();
        preConfigurationSelectPidPanel.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:grow"));
        hotRedefineSettingPane.add(preConfigurationSelectPidPanel, new GridConstraints(5, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        selectLink.setText("as.sh --select project name");
        selectLink.setToolTipText("as.sh --select -c '命令' auto redfine");
        CellConstraints cc = new CellConstraints();
        preConfigurationSelectPidPanel.add(selectLink, cc.xy(1, 1));
        batchSupportLink.setText("-c batch support");
        preConfigurationSelectPidPanel.add(batchSupportLink, cc.xy(5, 1));
        selectProjectNameTextField = new JTextField();
        preConfigurationSelectPidPanel.add(selectProjectNameTextField, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label19 = new JLabel();
        label19.setText("Script select process configuration");
        hotRedefineSettingPane.add(label19, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        manualSelectPidRadioButton = new JRadioButton();
        manualSelectPidRadioButton.setText("Manual selection process");
        hotRedefineSettingPane.add(manualSelectPidRadioButton, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        preConfigurationSelectPidRadioButton = new JRadioButton();
        preConfigurationSelectPidRadioButton.setText("Pre-configured project name  --select  project name (jps -l)");
        hotRedefineSettingPane.add(preConfigurationSelectPidRadioButton, new GridConstraints(4, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        redisRadioButton = new JRadioButton();
        redisRadioButton.setText("Redis");
        redisRadioButton.setToolTipText("使用redis 存储构造的脚本");
        hotRedefineSettingPane.add(redisRadioButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        redisSettingPane = new JPanel();
        redisSettingPane.setLayout(new GridLayoutManager(5, 6, new Insets(5, 5, 5, 5), -1, -1));
        hotRedefineSettingPane.add(redisSettingPane, new GridConstraints(3, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(0, 50), null, 0, false));
        redisSettingPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label20 = new JLabel();
        label20.setText("Address");
        label20.setToolTipText("redis server address");
        redisSettingPane.add(label20, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        redisSettingPane.add(spacer7, new GridConstraints(0, 4, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label21 = new JLabel();
        label21.setEnabled(true);
        label21.setText(":");
        redisSettingPane.add(label21, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label22 = new JLabel();
        label22.setText("Auth");
        label22.setToolTipText("redis password");
        redisSettingPane.add(label22, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        redisPasswordField = new JTextField();
        redisPasswordField.setToolTipText("Password to use when connecting to the server.");
        redisSettingPane.add(redisPasswordField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer8 = new Spacer();
        redisSettingPane.add(spacer8, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        redisCheckConfigButton = new JButton();
        redisCheckConfigButton.setText("redis config detection");
        redisSettingPane.add(redisCheckConfigButton, new GridConstraints(3, 5, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        redisAddressTextField = new JTextField();
        redisAddressTextField.setToolTipText("Server hostname (eg : 127.0.0.1).");
        redisSettingPane.add(redisAddressTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        redisPortField = new JSpinner();
        redisSettingPane.add(redisPortField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(-1, 30), 0, false));
        redisMessageLabel = new JLabel();
        redisMessageLabel.setText("");
        redisSettingPane.add(redisMessageLabel, new GridConstraints(3, 1, 1, 4, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label23 = new JLabel();
        label23.setText("Cache Key");
        redisSettingPane.add(label23, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        redisCacheKeyTextField = new JTextField();
        redisCacheKeyTextField.setToolTipText("reids 缓存key 的信息");
        redisSettingPane.add(redisCacheKeyTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        redisCacheKeyTtl = new JSpinner();
        redisCacheKeyTtl.setToolTipText("time to live for a key(s)");
        redisSettingPane.add(redisCacheKeyTtl, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label24 = new JLabel();
        label24.setText("ttl");
        label24.setToolTipText("time to live for a key");
        redisSettingPane.add(label24, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label25 = new JLabel();
        label25.setText("Arthas Package Zip Download Url");
        label25.setToolTipText("如果内网无法访问阿里云的地址 可以手动指定 完整包的地址");
        hotRedefineSettingPane.add(label25, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        arthasPackageZipDownloadUrlTextField = new JTextField();
        arthasPackageZipDownloadUrlTextField.setText("");
        hotRedefineSettingPane.add(arthasPackageZipDownloadUrlTextField, new GridConstraints(6, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label26 = new JLabel();
        label26.setText("Server cannot access arthas download address can be specified manually");
        hotRedefineSettingPane.add(label26, new GridConstraints(6, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        s3RadioButton = new JRadioButton();
        s3RadioButton.setText(" Object Storage S3");
        s3RadioButton.setToolTipText("aws s3 对象存储");
        hotRedefineSettingPane.add(s3RadioButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        s3Panel = new JPanel();
        s3Panel.setLayout(new GridLayoutManager(9, 5, new Insets(5, 5, 5, 5), -1, -1));
        hotRedefineSettingPane.add(s3Panel, new GridConstraints(2, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(0, 51), null, 0, false));
        s3Panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label27 = new JLabel();
        label27.setText("S3 Endpoint");
        label27.setToolTipText("eg: http://oss-cn-hangzhou.aliyuncs.com");
        s3Panel.add(label27, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        s3Panel.add(spacer9, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        s3EndPointField = new JTextField();
        s3Panel.add(s3EndPointField, new GridConstraints(0, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label28 = new JLabel();
        label28.setText("S3 AccessKeyId");
        label28.setToolTipText("yourAccessKeyId");
        s3Panel.add(label28, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        s3AkField = new JTextField();
        s3Panel.add(s3AkField, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label29 = new JLabel();
        label29.setText("S3 AccessKeySecret");
        label29.setToolTipText("yourAccessKeySecret");
        s3Panel.add(label29, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        s3SkField = new JTextField();
        s3Panel.add(s3SkField, new GridConstraints(2, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label30 = new JLabel();
        label30.setText("S3 BucketName");
        label30.setToolTipText("yourBucketName");
        s3Panel.add(label30, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        s3BucketNameField = new JTextField();
        s3Panel.add(s3BucketNameField, new GridConstraints(3, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label31 = new JLabel();
        label31.setText("S3 Directory Prefix");
        label31.setToolTipText("oss 存储文件空间的地址前缀可以为空( abc/efg/ 为前缀  abc/efg/123.jpg)");
        s3Panel.add(label31, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        s3DirPrefixField = new JTextField();
        s3Panel.add(s3DirPrefixField, new GridConstraints(5, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer10 = new Spacer();
        s3Panel.add(spacer10, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        s3CheckButton = new JButton();
        s3CheckButton.setText("S3 config detection");
        s3Panel.add(s3CheckButton, new GridConstraints(7, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        s3CheckMessageLabel = new JLabel();
        s3CheckMessageLabel.setText("");
        s3Panel.add(s3CheckMessageLabel, new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label32 = new JLabel();
        label32.setText("S3 Setting Scope");
        label32.setToolTipText("Oss 设置作用范围");
        s3Panel.add(label32, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer11 = new Spacer();
        s3Panel.add(spacer11, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        s3GlobalConfigField = new JRadioButton();
        s3GlobalConfigField.setText("S3 global configuration");
        s3GlobalConfigField.setToolTipText("是否当前配置作为全局默认配置");
        s3Panel.add(s3GlobalConfigField, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label33 = new JLabel();
        label33.setText("S3 Region");
        label33.setToolTipText("yourBucketName");
        s3Panel.add(label33, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        s3RegionField = new JTextField();
        s3Panel.add(s3RegionField, new GridConstraints(4, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(5, 2, new Insets(10, 10, 10, 10), -1, -1));
        settingTabPane.addTab("Class File Hot Swap", panel1);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label34 = new JLabel();
        label34.setText("Hot Swap Delete Class File");
        panel1.add(label34, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer12 = new Spacer();
        panel1.add(spacer12, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        hotRedefineDeleteFileRadioButton = new JRadioButton();
        hotRedefineDeleteFileRadioButton.setText("Delete class files after the hot swap is complete");
        hotRedefineDeleteFileRadioButton.setToolTipText("热更新完成后删除文件");
        panel1.add(hotRedefineDeleteFileRadioButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label35 = new JLabel();
        label35.setText("Hot Swap Before Compile");
        panel1.add(label35, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        redefineBeforeCompileRadioButton = new JRadioButton();
        redefineBeforeCompileRadioButton.setText("Auto compile before build  hot  swap script");
        panel1.add(redefineBeforeCompileRadioButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        redefineHelpActionLink.setFocusable(false);
        redefineHelpActionLink.setText("redefine help");
        panel1.add(redefineHelpActionLink, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        retransformHelpLink.setFocusable(false);
        retransformHelpLink.setText("retransform help");
        panel1.add(retransformHelpLink, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        analysisRetransformerLink.setFocusable(false);
        analysisRetransformerLink.setText("Analysis of Arthas retransformer hot swap function");
        analysisRetransformerLink.setToolTipText("retransformer 实现原理浅析");
        panel1.add(analysisRetransformerLink, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(6, 3, new Insets(10, 10, 10, 10), -1, -1));
        settingTabPane.addTab("Mybatis Mapper Xml Reload", panel2);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label36 = new JLabel();
        label36.setText("Mybatis Mapper Reload Spring Service Bean Name");
        panel2.add(label36, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer13 = new Spacer();
        panel2.add(spacer13, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        mybatisMapperReloadServiceBeanNameTextField = new JTextField();
        panel2.add(mybatisMapperReloadServiceBeanNameTextField, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label37 = new JLabel();
        label37.setText("Mybatis Mapper Reload Spring Service Method Name");
        panel2.add(label37, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mybatisMapperReloadMethodNameTextField = new JTextField();
        panel2.add(mybatisMapperReloadMethodNameTextField, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label38 = new JLabel();
        label38.setFocusable(false);
        Font label38Font = this.$$$getFont$$$(null, -1, -1, label38.getFont());
        if (label38Font != null) label38.setFont(label38Font);
        label38.setText("* The reload  mybatis xml requires  configuration of a spring bean service");
        panel2.add(label38, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mybatisMapperReloadHelpLink.setFocusable(false);
        mybatisMapperReloadHelpLink.setText("Mybatis Mapper Xml  Reload Know More");
        panel2.add(mybatisMapperReloadHelpLink, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label39 = new JLabel();
        label39.setFocusable(false);
        label39.setText(" eg spring.getBean(\"mybatisMapperXmlFileReloadService\").reloadAllSqlSessionFactoryMapper(\"mapperXmlPath\")  ");
        panel2.add(label39, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label40 = new JLabel();
        label40.setFocusable(false);
        label40.setText("* Service Method only one parameter and that is  mapper xml path");
        panel2.add(label40, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tunnelServerSettingPanel = new JPanel();
        tunnelServerSettingPanel.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        settingTabPane.addTab("Tunnel Server", tunnelServerSettingPanel);
        addButton = new JButton();
        addButton.setText("Add");
        tunnelServerSettingPanel.add(addButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer14 = new Spacer();
        tunnelServerSettingPanel.add(spacer14, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        deleteButton = new JButton();
        deleteButton.setText("Delete");
        tunnelServerSettingPanel.add(deleteButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer15 = new Spacer();
        tunnelServerSettingPanel.add(spacer15, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        tablePanel = new JPanel();
        tablePanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        tunnelServerSettingPanel.add(tablePanel, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        tablePanel.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        tunnelTable = new JTable();
        tablePanel.add(tunnelTable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final Spacer spacer16 = new Spacer();
        tablePanel.add(spacer16, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        tunnelServerLabel.setText("Tunnel Server doc");
        tunnelServerSettingPanel.add(tunnelServerLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer17 = new Spacer();
        contentPane.add(spacer17, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
