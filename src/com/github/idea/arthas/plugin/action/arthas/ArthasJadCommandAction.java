package com.github.idea.arthas.plugin.action.arthas;

import com.github.idea.arthas.plugin.utils.CommonExecuteScriptUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * 反编译查看类文件的信息  Decompile class
 * jad java.lang.String
 * jad java.lang.String toString
 * jad --source-only java.lang.String
 * jad -c 39eb305e org/apache/log4j/Logger
 * jad -c 39eb305e -E org\\.apache\\.*\\.StringUtils
 *
 * @author 汪小哥
 * @date 28-02-2020
 */
public class ArthasJadCommandAction extends BaseArthasPluginAction {

    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement, Editor editor) {
        if ("*".equals(methodName)) {
            // 查看当前类的反编译结果
            methodName = "";
        }
        // The common script assigns this command in double quotes, so retain an
        // inner-class separator as a literal instead of expanding it as a shell variable.
        String command = String.join(" ", "jad --source-only", className.replace("$", "\\$"), methodName).trim();
        CommonExecuteScriptUtils.executeCommonScript(project, "", command,
                " (Decompile the target method or class without opening Arthas)");
    }
}
