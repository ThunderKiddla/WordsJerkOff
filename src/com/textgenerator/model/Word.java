package com.textgenerator.model;

/**
 * 词语数据模型类
 * 表示词库中的单个词语，包含词语文本、词性和分类信息
 */
public class Word {
    private String text;      // 词语文本
    private String pos;       // 词性（名词、动词、形容词等）
    private String category;  // 分类（自然、科技、情感等）

    /**
     * 构造函数
     * @param text 词语文本
     * @param pos 词性
     * @param category 分类
     */
    public Word(String text, String pos, String category) {
        this.text = text;
        this.pos = pos;
        this.category = category;
    }

    // Getter 和 Setter 方法
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 重写 toString 方法，便于在 JTable 中显示
     */
    @Override
    public String toString() {
        return text + " (" + pos + ", " + category + ")";
    }
}
