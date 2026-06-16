package com.textgenerator.library;

import com.textgenerator.model.Word;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 词库管理类
 * 负责词语的加载、保存、添加、删除以及按条件查询
 */
public class WordLibrary {
    private List<Word> words;  // 存储所有词语的列表
    private String filePath;   // 词库文件路径

    /**
     * 构造函数，初始化词库
     * @param filePath 词库文件路径
     */
    public WordLibrary(String filePath) {
        this.words = new ArrayList<>();
        this.filePath = filePath;
        loadFromFile();  // 构造时自动加载文件
    }

    /**
     * 从 JSON 文件加载词库
     * 文件格式示例：
     * [
     *   {"text": "青山", "pos": "名词", "category": "自然"},
     *   {"text": "奔跑", "pos": "动词", "category": "动作"}
     * ]
     */
    public void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            // 文件不存在时创建空文件
            saveToFile();
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            JSONArray jsonArray = new JSONArray(jsonContent.toString());
            words.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String text = obj.getString("text");
                String pos = obj.getString("pos");
                String category = obj.getString("category");
                words.add(new Word(text, pos, category));
            }
        } catch (IOException e) {
            System.err.println("加载词库文件失败：" + e.getMessage());
        }
    }

    /**
     * 将词库保存到 JSON 文件
     */
    public void saveToFile() {
        JSONArray jsonArray = new JSONArray();
        for (Word word : words) {
            JSONObject obj = new JSONObject();
            obj.put("text", word.getText());
            obj.put("pos", word.getPos());
            obj.put("category", word.getCategory());
            jsonArray.put(obj);
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.write(jsonArray.toString(2));  // 格式化输出，缩进为 2 空格
        } catch (IOException e) {
            System.err.println("保存词库文件失败：" + e.getMessage());
        }
    }

    /**
     * 添加词语到词库
     * @param word 要添加的词语对象
     */
    public void addWord(Word word) {
        words.add(word);
    }

    /**
     * 从词库删除指定索引的词语
     * @param index 词语在列表中的索引
     * @return 是否删除成功
     */
    public boolean removeWord(int index) {
        if (index >= 0 && index < words.size()) {
            words.remove(index);
            return true;
        }
        return false;
    }

    /**
     * 获取所有词语列表
     * @return 词语列表
     */
    public List<Word> getAllWords() {
        return new ArrayList<>(words);
    }

    /**
     * 根据词性和分类筛选词语
     * 支持模糊匹配：如果 pos 或 category 为 null 或空字符串，则不限制该条件
     * @param pos 词性（可为 null 或空）
     * @param category 分类（可为 null 或空）
     * @return 匹配的词语列表
     */
    public List<Word> filterWords(String pos, String category) {
        return words.stream()
                .filter(word -> {
                    // 检查词性是否匹配（如果 pos 为空则跳过此条件）
                    boolean posMatch = (pos == null || pos.isEmpty() || word.getPos().equals(pos));
                    // 检查分类是否匹配（如果 category 为空则跳过此条件）
                    boolean categoryMatch = (category == null || category.isEmpty() || word.getCategory().equals(category));
                    return posMatch && categoryMatch;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取所有唯一的词性列表
     * @return 词性列表
     */
    public List<String> getUniquePosList() {
        return words.stream()
                .map(Word::getPos)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 获取所有唯一的分类列表
     * @return 分类列表
     */
    public List<String> getUniqueCategoryList() {
        return words.stream()
                .map(Word::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 获取词库中词语总数
     * @return 词语数量
     */
    public int size() {
        return words.size();
    }
}
