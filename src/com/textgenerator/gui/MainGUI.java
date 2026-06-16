package com.textgenerator.gui;

import com.textgenerator.engine.TextGenerator;
import com.textgenerator.library.WordLibrary;
import com.textgenerator.model.Word;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * 主 GUI 界面类
 * 使用 JTabbedPane 组织三个功能页面：词库管理、模板管理、句子生成
 */
public class MainGUI extends JFrame {
    private WordLibrary wordLibrary;
    private TextGenerator textGenerator;
    
    // 组件引用
    private JTable wordTable;
    private DefaultTableModel tableModel;
    private JTextArea templateArea;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> templateCombo;
    private JTextArea resultArea;
    
    private static final String WORD_LIBRARY_FILE = "resources/word_library.json";
    private static final String TEMPLATE_FILE = "resources/templates.txt";

    /**
     * 构造函数，初始化 GUI
     */
    public MainGUI() {
        super("文本生成器");
        
        // 初始化词库和生成器
        wordLibrary = new WordLibrary(WORD_LIBRARY_FILE);
        textGenerator = new TextGenerator(wordLibrary);
        
        // 设置窗口基本属性
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // 居中显示
        
        // 创建标签页面板
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("词库管理", createWordLibraryPanel());
        tabbedPane.addTab("模板管理", createTemplatePanel());
        tabbedPane.addTab("句子生成", createGeneratorPanel());
        
        add(tabbedPane);
        
        // 加载保存的模板
        loadTemplates();
    }

    /**
     * 创建词库管理面板
     */
    private JPanel createWordLibraryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 表格模型：列名为"词语"、"词性"、"分类"
        String[] columnNames = {"词语", "词性", "分类"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // 表格只读
            }
        };
        
        wordTable = new JTable(tableModel);
        wordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(wordTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        JButton addButton = new JButton("添加词语");
        addButton.addActionListener(e -> showAddWordDialog());
        buttonPanel.add(addButton);
        
        JButton deleteButton = new JButton("删除选中");
        deleteButton.addActionListener(e -> deleteSelectedWord());
        buttonPanel.add(deleteButton);
        
        JButton saveButton = new JButton("保存到文件");
        saveButton.addActionListener(e -> saveWordLibrary());
        buttonPanel.add(saveButton);
        
        JButton refreshButton = new JButton("刷新列表");
        refreshButton.addActionListener(e -> refreshWordTable());
        buttonPanel.add(refreshButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 初始加载表格数据
        refreshWordTable();
        
        return panel;
    }

    /**
     * 创建模板管理面板
     */
    private JPanel createTemplatePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel instructionLabel = new JLabel(
            "<html>请输入句子模板，格式示例：<br>" +
            "[分类:自然][词性:形容词] + [分类:自然][词性:名词] + [词性:动词]<br><br>" +
            "说明：[分类:xxx]指定词语分类，[词性:xxx]指定词性，连续的约束会被组合成一个词语槽位</html>"
        );
        panel.add(instructionLabel, BorderLayout.NORTH);
        
        templateArea = new JTextArea(15, 50);
        templateArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(templateArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        JButton saveTemplateButton = new JButton("保存模板");
        saveTemplateButton.addActionListener(e -> saveTemplate());
        buttonPanel.add(saveTemplateButton);
        
        JButton loadTemplateButton = new JButton("加载模板");
        loadTemplateButton.addActionListener(e -> loadTemplates());
        buttonPanel.add(loadTemplateButton);
        
        JButton clearButton = new JButton("清空");
        clearButton.addActionListener(e -> templateArea.setText(""));
        buttonPanel.add(clearButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * 创建句子生成面板
     */
    private JPanel createGeneratorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 第一行：分类选择
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("选择分类:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        categoryCombo = new JComboBox<>();
        updateCategoryCombo();
        panel.add(categoryCombo, gbc);
        
        // 第二行：模板选择
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("选择模板:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        templateCombo = new JComboBox<>();
        updateTemplateCombo();
        panel.add(templateCombo, gbc);
        
        // 第三行：生成按钮
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        JButton generateButton = new JButton("生成句子");
        generateButton.setFont(new Font("Arial", Font.BOLD, 16));
        generateButton.setPreferredSize(new Dimension(200, 40));
        generateButton.addActionListener(e -> generateSentence());
        panel.add(generateButton, gbc);
        
        // 第四行：结果显示
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        resultArea = new JTextArea(10, 50);
        resultArea.setFont(new Font("宋体", Font.PLAIN, 16));
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(245, 245, 245));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        panel.add(resultScroll, gbc);
        
        return panel;
    }

    /**
     * 显示添加词语对话框
     */
    private void showAddWordDialog() {
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        
        JTextField textField = new JTextField(15);
        JTextField posField = new JTextField(15);
        JTextField categoryField = new JTextField(15);
        
        inputPanel.add(new JLabel("词语:"));
        inputPanel.add(textField);
        inputPanel.add(new JLabel("词性:"));
        inputPanel.add(posField);
        inputPanel.add(new JLabel("分类:"));
        inputPanel.add(categoryField);
        
        int result = JOptionPane.showConfirmDialog(
            this, 
            inputPanel, 
            "添加新词语",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            String text = textField.getText().trim();
            String pos = posField.getText().trim();
            String category = categoryField.getText().trim();
            
            if (text.isEmpty() || pos.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "所有字段都不能为空！", 
                    "输入错误", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            wordLibrary.addWord(new Word(text, pos, category));
            refreshWordTable();
            updateCategoryCombo();  // 更新分类下拉框
            JOptionPane.showMessageDialog(this, "词语添加成功！");
        }
    }

    /**
     * 删除选中的词语
     */
    private void deleteSelectedWord() {
        int selectedRow = wordTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "请先选择要删除的词语！", 
                "提示", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "确定要删除选中的词语吗？", 
            "确认删除",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            wordLibrary.removeWord(selectedRow);
            refreshWordTable();
            updateCategoryCombo();
            JOptionPane.showMessageDialog(this, "删除成功！");
        }
    }

    /**
     * 保存词库到文件
     */
    private void saveWordLibrary() {
        wordLibrary.saveToFile();
        JOptionPane.showMessageDialog(this, "词库已保存到文件！");
    }

    /**
     * 刷新词库表格
     */
    private void refreshWordTable() {
        tableModel.setRowCount(0);  // 清空现有行
        List<Word> words = wordLibrary.getAllWords();
        for (Word word : words) {
            tableModel.addRow(new Object[]{word.getText(), word.getPos(), word.getCategory()});
        }
    }

    /**
     * 更新分类下拉框选项
     */
    private void updateCategoryCombo() {
        categoryCombo.removeAllItems();
        categoryCombo.addItem("全部");
        List<String> categories = wordLibrary.getUniqueCategoryList();
        for (String category : categories) {
            categoryCombo.addItem(category);
        }
    }

    /**
     * 保存模板到文件
     */
    private void saveTemplate() {
        String content = templateArea.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "模板内容不能为空！", 
                "提示", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            File file = new File(TEMPLATE_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(content);
            writer.close();
            
            updateTemplateCombo();  // 更新模板下拉框
            JOptionPane.showMessageDialog(this, "模板保存成功！");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "保存模板失败：" + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 从文件加载模板
     */
    private void loadTemplates() {
        File file = new File(TEMPLATE_FILE);
        if (!file.exists()) {
            // 如果文件不存在，创建一个默认模板
            createDefaultTemplate();
            return;
        }
        
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            templateArea.setText(content.toString().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "加载模板失败：" + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 创建默认模板文件
     */
    private void createDefaultTemplate() {
        String defaultTemplate = "[分类:自然][词性:形容词] + [分类:自然][词性:名词] + [词性:动词] + [词性:副词] + [词性:动词]";
        templateArea.setText(defaultTemplate);
        saveTemplate();
    }

    /**
     * 更新模板下拉框
     */
    private void updateTemplateCombo() {
        templateCombo.removeAllItems();
        templateCombo.addItem("自定义模板");
        // 可以添加预定义模板
        templateCombo.addItem("[分类:自然][词性:形容词] + [分类:自然][词性:名词]");
        templateCombo.addItem("[词性:名词] + [词性:动词] + [词性:名词]");
        templateCombo.addItem("[分类:情感][词性:形容词] + [词性:名词] + [词性:动词]");
    }

    /**
     * 生成句子
     */
    private void generateSentence() {
        String template = templateArea.getText().trim();
        if (template.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "请先输入或加载模板！", 
                "提示", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String result = textGenerator.generate(template);
        resultArea.setText(result);
    }

    /**
     * 程序入口点
     */
    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 在事件调度线程中启动 GUI
        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            gui.setVisible(true);
        });
    }
}
