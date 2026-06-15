package com.textgenerator.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 句子模板解析器
 * 负责解析用户定义的模板规则，提取出每个位置的约束条件
 * 
 * 模板格式示例：
 * [分类:自然][词性:形容词] + [分类:自然][词性:名词] + [词性:动词] + [词性:副词] + [词性:动词]
 * 
 * 解析后会生成一个 TokenSlot 列表，每个 TokenSlot 包含该位置需要的词性和分类约束
 */
public class TemplateParser {

    /**
     * 内部类：表示模板中的一个词语槽位
     * 存储该位置需要的词性和分类约束
     */
    public static class TokenSlot {
        private String requiredPos;       // 需要的词性
        private String requiredCategory;  // 需要的分类

        public TokenSlot(String requiredPos, String requiredCategory) {
            this.requiredPos = requiredPos;
            this.requiredCategory = requiredCategory;
        }

        public String getRequiredPos() {
            return requiredPos;
        }

        public String getRequiredCategory() {
            return requiredCategory;
        }

        /**
         * 判断该槽位是否有分类约束
         */
        public boolean hasCategoryConstraint() {
            return requiredCategory != null && !requiredCategory.isEmpty();
        }

        /**
         * 判断该槽位是否有词性约束
         */
        public boolean hasPosConstraint() {
            return requiredPos != null && !requiredPos.isEmpty();
        }
    }

    /**
     * 解析模板字符串，生成 TokenSlot 列表
     * 
     * @param template 模板字符串，格式如：[分类:自然][词性:形容词] + [词性:名词]
     * @return TokenSlot 列表，每个元素代表一个词语位置及其约束
     */
    public List<TokenSlot> parse(String template) {
        List<TokenSlot> slots = new ArrayList<>();
        
        if (template == null || template.trim().isEmpty()) {
            return slots;
        }

        // 正则表达式匹配 [分类:xxx] 或 [词性:xxx] 的模式
        // 分组 1: 约束类型（"分类"或"词性"）
        // 分组 2: 约束值
        Pattern slotPattern = Pattern.compile("\\[(分类 | 词性):([^\\]]+)\\]");
        Matcher matcher = slotPattern.matcher(template);

        // 当前正在构建的 TokenSlot 的临时变量
        String currentPos = null;
        String currentCategory = null;
        boolean hasCurrentSlot = false;

        while (matcher.find()) {
            String type = matcher.group(1);  // "分类" 或 "词性"
            String value = matcher.group(2).trim();  // 具体的值

            if ("词性".equals(type)) {
                currentPos = value;
                hasCurrentSlot = true;
            } else if ("分类".equals(type)) {
                currentCategory = value;
                hasCurrentSlot = true;
            }

            // 检查是否到达一个完整槽位的结束
            // 判断依据：下一个匹配位置与当前位置不连续，或者已经是最后一个匹配
            int nextMatchStart = matcher.end();
            boolean isLastMatch = !matcher.find();
            
            if (!isLastMatch) {
                // 重置 matcher 位置继续循环
                matcher.region(matcher.start(), template.length());
            }

            // 如果已经收集了至少一个约束，且下一个匹配不连续或是最后一个匹配，则创建一个槽位
            if (hasCurrentSlot) {
                // 检查下一个字符是否是分隔符或空白，如果是则创建槽位
                boolean shouldCreateSlot = isLastMatch;
                if (!isLastMatch) {
                    // 检查两个匹配之间是否有非约束内容（如空格、+ 号等）
                    String betweenText = template.substring(nextMatchStart, matcher.start()).trim();
                    // 如果中间有非约束内容，说明前一个槽位结束了
                    shouldCreateSlot = !betweenText.isEmpty() || 
                                       !template.substring(matcher.end() - value.length() - 4, matcher.end()).endsWith("]");
                }

                if (shouldCreateSlot || isLastMatch) {
                    slots.add(new TokenSlot(currentPos, currentCategory));
                    // 重置临时变量
                    currentPos = null;
                    currentCategory = null;
                    hasCurrentSlot = false;
                }
            }
        }

        // 处理最后一个槽位（如果上面的循环没有处理）
        if (hasCurrentSlot) {
            slots.add(new TokenSlot(currentPos, currentCategory));
        }

        return slots;
    }

    /**
     * 简化的解析方法：按顺序提取所有约束组合
     * 更直观地处理连续的 [分类:...][词性:...] 组合
     */
    public List<TokenSlot> parseSimple(String template) {
        List<TokenSlot> slots = new ArrayList<>();
        
        if (template == null || template.trim().isEmpty()) {
            return slots;
        }

        // 先提取所有的约束标记
        // 正则表达式匹配 [xxx:yyy] 的模式，其中 xxx 是类型（分类或词性），yyy 是值
        Pattern pattern = Pattern.compile("\\[([^:\\]]+):([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(template);

        List<String> types = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();

        while (matcher.find()) {
            types.add(matcher.group(1).trim());
            values.add(matcher.group(2).trim());
            positions.add(matcher.start());
            ends.add(matcher.end());
        }

        if (types.isEmpty()) {
            return slots;
        }

        // 遍历所有匹配，将连续的约束组合成一个槽位
        int i = 0;
        while (i < types.size()) {
            String pos = null;
            String category = null;

            // 收集连续的约束（直到遇到间隔或结束）
            while (i < types.size()) {
                if ("词性".equals(types.get(i))) {
                    pos = values.get(i);
                } else if ("分类".equals(types.get(i))) {
                    category = values.get(i);
                }

                // 检查下一个约束是否与当前约束连续（中间没有其他文本）
                if (i + 1 < types.size()) {
                    int currentEnd = ends.get(i);
                    int nextStart = positions.get(i + 1);
                    
                    // 如果下一个约束不是紧挨着的，说明当前槽位结束
                    if (nextStart > currentEnd) {
                        i++;
                        break;
                    }
                }
                i++;
            }

            // 只有当至少有一个约束时才添加槽位
            if (pos != null || category != null) {
                slots.add(new TokenSlot(pos, category));
            }
        }

        return slots;
    }
}
