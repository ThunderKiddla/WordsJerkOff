# 文本生成器项目说明

## 项目概述
这是一个基于规则和词库的文本生成器，使用 Java Swing 构建 GUI 界面。用户可以管理词库、定义句子模板，并根据模板随机生成句子。

## 类结构设计

### 1. Word (com.textgenerator.model.Word)
**职责**: 词语数据模型类
- 存储单个词语的信息：词语文本、词性、分类
- 提供 getter/setter 方法访问和修改属性

### 2. WordLibrary (com.textgenerator.library.WordLibrary)
**职责**: 词库管理类
- 从 JSON 文件加载词库数据
- 将词库保存到 JSON 文件
- 支持添加、删除词语
- 根据词性和分类筛选词语
- 获取唯一的词性列表和分类列表

### 3. TemplateParser (com.textgenerator.parser.TemplateParser)
**职责**: 句子模板解析器
- 解析用户定义的模板字符串
- 提取每个词语槽位的约束条件（词性、分类）
- 内部类 TokenSlot 表示一个词语槽位及其约束

### 4. TextGenerator (com.textgenerator.engine.TextGenerator)
**职责**: 文本生成引擎
- 使用 TemplateParser 解析模板
- 根据约束条件从词库中筛选候选词语
- 随机选择词语并拼接成完整句子
- 提供错误处理机制

### 5. MainGUI (com.textgenerator.gui.MainGUI)
**职责**: 主 GUI 界面类
- 使用 JTabbedPane 组织三个功能页面
- **词库管理页**: JTable 展示词库，支持添加、删除、保存操作
- **模板管理页**: 文本域输入和保存句子模板
- **句子生成页**: 下拉框选择分类和模板，生成按钮，结果显示区

## 运行方法

### 环境要求
- Java 17 或更高版本
- JSON 库 (org.json)

### 编译项目
```bash
cd /workspace
mkdir -p out
javac -d out -cp "resources/json.jar" \
    src/com/textgenerator/model/Word.java \
    src/com/textgenerator/library/WordLibrary.java \
    src/com/textgenerator/parser/TemplateParser.java \
    src/com/textgenerator/engine/TextGenerator.java \
    src/com/textgenerator/gui/MainGUI.java
```

### 运行 GUI 程序
```bash
# 需要 X11 显示环境
java -cp "out:resources/json.jar" com.textgenerator.gui.MainGUI
```

### 运行命令行测试（无 GUI 环境）
```bash
java -cp "out:resources/json.jar" com.textgenerator.TextGeneratorTest
```

## 本地词库文件格式示例

词库文件采用 JSON 格式，存储在 `resources/word_library.json`：

```json
[
  {
    "text": "青山",
    "pos": "名词",
    "category": "自然"
  },
  {
    "text": "蔚蓝",
    "pos": "形容词",
    "category": "自然"
  },
  {
    "text": "奔跑",
    "pos": "动词",
    "category": "动作"
  },
  {
    "text": "快速地",
    "pos": "副词",
    "category": "状态"
  }
]
```

### 字段说明
- `text`: 词语文本（必填）
- `pos`: 词性，如"名词"、"动词"、"形容词"、"副词"等（必填）
- `category`: 分类，如"自然"、"科技"、"情感"、"动作"等（必填）

## 模板格式说明

模板用于定义句子的结构规则，格式如下：

```
[分类:自然][词性:形容词] + [分类:自然][词性:名词] + [词性:动词] + [词性:副词] + [词性:动词]
```

### 语法规则
- `[分类:xxx]`: 指定词语必须属于 xxx 分类
- `[词性:xxx]`: 指定词语必须是 xxx 词性
- 连续的约束（中间无空格或其他字符）会被组合成一个词语槽位
- 槽位之间可以用任意分隔符（如空格、+ 号等）

### 模板示例
1. **简单模板**: `[词性:名词] + [词性:动词] + [词性:名词]`
   - 生成结果示例："阳光 奔跑 青山"

2. **带分类约束**: `[分类:自然][词性:形容词] + [分类:自然][词性:名词]`
   - 生成结果示例："蔚蓝 微风"

3. **复杂模板**: `[分类:情感][词性:形容词] + [词性:名词] + [词性:动词]`
   - 生成结果示例："温暖 梦想 思念"

## 核心逻辑说明

### 模板解析流程
1. 使用正则表达式 `\[([^:\]]+):([^\]]+)\]` 匹配所有约束标记
2. 遍历匹配结果，将连续的约束组合成一个 TokenSlot
3. 每个 TokenSlot 包含该位置需要的词性和/或分类约束

### 词语抽取流程
1. 遍历所有 TokenSlot
2. 对每个槽位，调用 `WordLibrary.filterWords(pos, category)` 筛选候选词语
3. 如果没有匹配的词语，返回错误信息
4. 从候选词语中随机选择一个
5. 将所有选中的词语用空格拼接成完整句子

## 项目文件结构
```
/workspace
├── src/com/textgenerator/
│   ├── model/
│   │   └── Word.java           # 词语模型
│   ├── library/
│   │   └── WordLibrary.java    # 词库管理
│   ├── parser/
│   │   └── TemplateParser.java # 模板解析
│   ├── engine/
│   │   └── TextGenerator.java  # 生成引擎
│   ├── gui/
│   │   └── MainGUI.java        # GUI 界面
│   └── TextGeneratorTest.java  # 命令行测试
├── resources/
│   ├── json.jar                # JSON 库
│   └── word_library.json       # 词库数据文件
├── out/                        # 编译输出目录
└── README.md                   # 项目说明
```
