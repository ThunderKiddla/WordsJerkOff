package com.textgenerator;

import com.textgenerator.engine.TextGenerator;
import com.textgenerator.library.WordLibrary;

/**
 * 命令行测试类
 * 用于在无 GUI 环境下测试文本生成器的核心功能
 */
public class TextGeneratorTest {
    public static void main(String[] args) {
        System.out.println("=== 文本生成器核心功能测试 ===\n");
        
        // 初始化词库和生成器
        WordLibrary wordLibrary = new WordLibrary("resources/word_library.json");
        TextGenerator generator = new TextGenerator(wordLibrary);
        
        // 显示词库信息
        System.out.println("【词库信息】");
        System.out.println("词语总数：" + wordLibrary.size());
        System.out.println("可用分类：" + String.join(", ", wordLibrary.getUniqueCategoryList()));
        System.out.println("可用词性：" + String.join(", ", wordLibrary.getUniquePosList()));
        System.out.println();
        
        // 测试模板 1：简单模板
        String template1 = "[分类:自然][词性:形容词] + [分类:自然][词性:名词]";
        System.out.println("【测试模板 1】: " + template1);
        for (int i = 0; i < 3; i++) {
            System.out.println("  生成结果 " + (i+1) + ": " + generator.generate(template1));
        }
        System.out.println();
        
        // 测试模板 2：复杂模板
        String template2 = "[分类:自然][词性:形容词] + [分类:自然][词性:名词] + [词性:动词] + [词性:副词] + [词性:动词]";
        System.out.println("【测试模板 2】: " + template2);
        for (int i = 0; i < 3; i++) {
            System.out.println("  生成结果 " + (i+1) + ": " + generator.generate(template2));
        }
        System.out.println();
        
        // 测试模板 3：只有词性约束
        String template3 = "[词性:名词] + [词性:动词] + [词性:名词]";
        System.out.println("【测试模板 3】: " + template3);
        for (int i = 0; i < 3; i++) {
            System.out.println("  生成结果 " + (i+1) + ": " + generator.generate(template3));
        }
        System.out.println();
        
        // 测试模板 4：情感类
        String template4 = "[分类:情感][词性:形容词] + [词性:名词] + [词性:动词]";
        System.out.println("【测试模板 4】: " + template4);
        for (int i = 0; i < 3; i++) {
            System.out.println("  生成结果 " + (i+1) + ": " + generator.generate(template4));
        }
        System.out.println();
        
        // 测试错误处理：不存在的约束
        String template5 = "[分类:不存在的分类][词性:名词]";
        System.out.println("【测试错误处理】: " + template5);
        System.out.println("  生成结果：" + generator.generate(template5));
        System.out.println();
        
        // 测试筛选功能
        System.out.println("【测试筛选功能】");
        System.out.println("筛选条件 - 分类=自然，词性=名词:");
        wordLibrary.filterWords("名词", "自然").forEach(w -> 
            System.out.println("  - " + w.getText() + " (" + w.getPos() + ", " + w.getCategory() + ")")
        );
        
        System.out.println("\n=== 测试完成 ===");
    }
}
