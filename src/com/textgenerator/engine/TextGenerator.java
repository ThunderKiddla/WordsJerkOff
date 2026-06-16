package com.textgenerator.engine;

import com.textgenerator.library.WordLibrary;
import com.textgenerator.model.Word;
import com.textgenerator.parser.TemplateParser;

import java.util.List;
import java.util.Random;

/**
 * 文本生成引擎
 * 负责根据模板和词库生成随机句子
 */
public class TextGenerator {
    private WordLibrary wordLibrary;  // 词库引用
    private TemplateParser parser;    // 模板解析器
    private Random random;            // 随机数生成器

    /**
     * 构造函数
     * @param wordLibrary 词库实例
     */
    public TextGenerator(WordLibrary wordLibrary) {
        this.wordLibrary = wordLibrary;
        this.parser = new TemplateParser();
        this.random = new Random();
    }

    /**
     * 根据模板生成句子
     * 
     * 工作流程：
     * 1. 使用 TemplateParser 解析模板，得到 TokenSlot 列表
     * 2. 对每个 TokenSlot，从词库中筛选符合条件的词语
     * 3. 从筛选结果中随机选择一个词语
     * 4. 将所有选中的词语拼接成完整句子
     * 
     * @param template 模板字符串，如 "[分类:自然][词性:形容词] + [词性:名词]"
     * @return 生成的句子，如果某个槽位没有匹配的词语则返回错误信息
     */
    public String generate(String template) {
        if (template == null || template.trim().isEmpty()) {
            return "错误：模板不能为空";
        }

        // 解析模板，获取所有词语槽位
        List<TemplateParser.TokenSlot> slots = parser.parseSimple(template);
        
        if (slots.isEmpty()) {
            return "错误：模板格式不正确，未找到有效的约束条件";
        }

        StringBuilder result = new StringBuilder();
        
        // 遍历每个槽位，从词库中抽取匹配的词语
        for (int i = 0; i < slots.size(); i++) {
            TemplateParser.TokenSlot slot = slots.get(i);
            
            // 根据槽位的约束条件筛选词语
            List<Word> candidates = wordLibrary.filterWords(
                slot.getRequiredPos(), 
                slot.getRequiredCategory()
            );

            // 如果没有找到匹配的词语，返回错误信息
            if (candidates.isEmpty()) {
                String constraintDesc = buildConstraintDescription(slot);
                return "错误：词库中没有符合" + constraintDesc + "的词语";
            }

            // 从候选词语中随机选择一个
            Word selectedWord = candidates.get(random.nextInt(candidates.size()));
            
            // 添加到结果中
            result.append(selectedWord.getText());
            
            // 如果不是最后一个词语，添加空格分隔（可根据需要调整）
            if (i < slots.size() - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }

    /**
     * 构建约束条件的描述信息，用于错误提示
     * @param slot 词语槽位
     * @return 约束描述字符串
     */
    private String buildConstraintDescription(TemplateParser.TokenSlot slot) {
        StringBuilder desc = new StringBuilder();
        if (slot.hasCategoryConstraint()) {
            desc.append("分类=").append(slot.getRequiredCategory());
        }
        if (slot.hasPosConstraint()) {
            if (desc.length() > 0) {
                desc.append("且");
            }
            desc.append("词性=").append(slot.getRequiredPos());
        }
        return desc.toString();
    }

    /**
     * 批量生成多个句子
     * @param template 模板字符串
     * @param count 生成数量
     * @return 生成的句子列表
     */
    public String[] generateMultiple(String template, int count) {
        String[] results = new String[count];
        for (int i = 0; i < count; i++) {
            results[i] = generate(template);
        }
        return results;
    }
}
