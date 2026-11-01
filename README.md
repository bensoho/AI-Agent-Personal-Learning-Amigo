# AI Agent Personal Learning Amigo

一个**离线优先**的 Android 学习 App（应用名：Agent Learning），配合
[《LangChain + LangGraph + LangSmith：4个月 AI Agent 系统化学习计划》](./LangChain_LangGraph_LangSmith_4个月学习计划.md)
使用：按月、按周浏览课程与讲义，用内置题库做练习，并通过错题本和间隔复习巩固知识点。

## 功能

| 模块 | 说明 |
|---|---|
| 首页 | 今日任务、学习进度概览，快速开始练习 |
| 课程 | 4 个月 / 16 周课程结构，查看每周知识点与练习入口 |
| 知识点 | 两个 Tab：**题库知识点**（题库自带笔记）与 **学习计划**（按月、周浏览完整学习计划文档），共用关键词搜索 |
| 练习 | 多种题型与答案解析、固定操作栏、结果汇总及本轮错题复习 |
| 复习 | 错题本 + 基础间隔复习调度 |
| 统计 | 本周学习、优先巩固知识点、近期答题记录与课程进度 |
| 设置 | 主题（跟随系统/浅色/深色）、字体、音效等偏好设置，本地数据备份与恢复 |

首页的「开始今日任务」位于任务卡内，复习入口位于待复习数量下方。
课程目录默认展开当前学习周（与首页一致，取仍有未答题目的最早一周），其他周可点击展开或收起；
所有题目均已作答时默认全部收起。知识点与学习计划卡片将阅读状态放在标题下方，优先保留标题空间。
课程顶部提供「定位当前周」「全部展开」「全部收起」，滚动到后续月份时也可直接定位回当前周。
最近 7 天无答题记录时显示紧凑提示，有记录时显示完整柱状图。

### 练习与结果

提交、下一题和查看结果固定在屏幕底部，长题目、代码与答案解析在上方独立滚动。
保存期间不可重复提交或跳题；保存失败会明确提示并允许重试，同一轮同一道题的相同答案重试不会重复计数。
结果页显示本轮正确率、答对和错题数量；「复习本轮错题」仅包含刚结束这一轮答错的题目，
不混入历史错题，并创建独立练习轮次。复习作答会正常更新答题记录与复习进度；全部答对时不显示错题复习入口。

### 学习统计

「本周学习」统计本地日期周一至今的答题数、学习天数及正确率，与「最近 7 天」图表区分。
「优先巩固」仅从已经练习过的知识点中，按「学习中」题目数量降序推荐最多 3 项，可直接进入该知识点练习；
未练习不等于薄弱，不会据此生成推荐。存在待复习或当前错题时提供对应练习入口，历史周统计默认收起。

累计答题数包含重复练习；当前错题按最近一次答错的题目去重。课程进度以已练习题目占比展示，
掌握度另标明为按复习状态估算，不与完成率混用。没有答题记录时显示提示，不生成正确率或趋势结论。

### 知识点 → 学习计划 Tab

学习计划文档会被拆分为：

- **计划指南**：计划总览、学习方法、代码环境准备、资源与建议
- **Month 1–4**：每月显示月度目标，下列该月的 4 周
- **Week 01–16**：每周详情页合并展示文档中对应的三部分内容
  - 课程安排（第三章：理论学习、实战练习、练习题、交付物）
  - 学习讲义（第四章：概念讲解、跟做步骤、课后题参考答案）
  - 代码跟做示例（第五章：可复制的代码片段）

搜索框常驻显示，不会在进入页面时自动弹出键盘。两个 Tab 共用关键词及月份、周次筛选；
切换月份会重置周次，切换 Tab 保留筛选条件。筛选选项始终来自完整目录，不会随搜索结果缩减。
搜索结果高亮命中词；仅正文命中时补充正文摘要。按月份或周次筛选学习计划时不显示「计划指南」，
选择全部月份、全部周次后可重新查看；无匹配结果时可用「重置筛选」同时清除关键词与范围限制。

详情页支持标题、列表、引用、表格、代码块（带复制按钮）及粗体、行内代码等常用 Markdown 语法，文本可选中复制。

### 阅读次数

题库知识点卡片与学习计划目录卡片右上角显示阅读次数标签（`未读` / `已读 N 次`）。
进入详情页后在前台累计停留满 30 秒，退出详情页时记为一次阅读（切到后台或进入练习页的时间不计入）。
阅读次数保存在 Room 的 `read_counts` 表中，随本地数据一起备份、恢复和重置。

## 技术栈

- Kotlin 2.0、Jetpack Compose（Material 3）、Navigation Compose
- Hilt 依赖注入、Room（SQLite）、DataStore
- kotlinx.serialization（题库 JSON）、Coroutines / Flow
- 系统 TTS（Android TextToSpeech，使用手机内置语音引擎）朗读
- 单元测试：JUnit 4、kotlinx-coroutines-test、Turbine
- minSdk 26 / targetSdk 34，JDK 17

### 语音朗读

知识点和学习计划详情页的朗读按钮通过 Android `TextToSpeech` 调用手机当前的
**系统默认语音引擎**（例如 vivo 的 AIService TTS、Google TTS）。当系统默认引擎
发生变化时，App 会在下次朗读前自动重新绑定，无需重启。

朗读时：**短按暂停/继续**（从暂停处接着读，不会重头开始），**长按停止**（下次从头朗读）。
实现方式是把每个句子合成为临时音频文件后用 `MediaPlayer` 播放，因为系统 TTS
本身不支持原位暂停。

## 项目结构

```text
.
├── LangChain_LangGraph_LangSmith_4个月学习计划.md   # 学习计划原文（学习计划 Tab 的数据源）
├── Agent_Learning_Android_Room_SQLite_开发文档.md   # App 设计与开发文档
├── app/
│   ├── schemas/                                      # Room 数据库 Schema 导出
│   └── src/
│       ├── main/
│       │   ├── assets/
│       │   │   ├── docs/study_plan.md                # 打包进 App 的学习计划副本
│       │   │   └── questions/                        # 固定题库：manifest.json + weekNN(_more).json
│       │   └── java/com/agentlearning/
│       │       ├── data/        # Room、题库导入、Repository 实现
│       │       ├── domain/      # 领域模型、判题、进度、复习调度
│       │       ├── feature/     # 各页面（home/curriculum/knowledge/quiz/review/statistics/settings）
│       │       │   └── knowledge/plan/   # 学习计划解析、Markdown 渲染与页面
│       │       ├── navigation/  # 路由与底部导航
│       │       └── ui/          # 通用组件与主题
│       └── test/                # JVM 单元测试
└── tools/
    ├── question-bank-guide.txt        # 题库编写规范
    ├── validate-question-bank.ps1     # 题库校验脚本
    └── sync-study-plan.ps1            # 同步学习计划文档到 assets
```

## 构建与运行

需要 Android Studio（内置 JDK 17）及 Android SDK 34。

1. 用 Android Studio 打开仓库根目录，等待 Gradle 同步完成。
   仓库未提交 `gradlew` 脚本与 wrapper jar；Android Studio 会根据
   `gradle/wrapper/gradle-wrapper.properties` 自动处理，如需命令行构建，可先执行 `gradle wrapper` 生成。
2. 选择 `app` 运行配置，在模拟器或真机上运行。
3. 命令行常用任务：

```bash
./gradlew assembleDebug        # 构建 Debug APK
./gradlew testDebugUnitTest    # 运行单元测试
```

首次启动时 App 会把 `assets/questions` 中的题库导入本地 Room 数据库，之后完全离线可用。

## 内容维护

### 更新学习计划

修改根目录的 `LangChain_LangGraph_LangSmith_4个月学习计划.md` 后，同步到 App 资源：

```powershell
./tools/sync-study-plan.ps1
```

解析规则（见 `app/src/main/java/com/agentlearning/feature/knowledge/plan/StudyPlan.kt`）依赖以下标题格式，请保持不变：

- 一级标题 `# 三、详细课程安排`、`# 四、逐周学习讲义…`、`# 五、逐周代码…` 用于识别章节
- 第三章中的 `## 第N个月：标题`、`### 第N周：标题`，以及 `**月度目标：**`
- 第四、五章中的 `## 第N周：标题`

### 更新题库

题库位于 `app/src/main/assets/questions/`，编写规范见 [tools/question-bank-guide.txt](./tools/question-bank-guide.txt)。修改后先校验：

```powershell
./tools/validate-question-bank.ps1          # 校验全部周
./tools/validate-question-bank.ps1 -Week 3  # 只校验第 3 周
```

题库内容变更时，请递增 `manifest.json` 中的 `contentVersion`，App 会在下次启动时重新导入。

## 相关文档

- [4个月学习计划](./LangChain_LangGraph_LangSmith_4个月学习计划.md)
- [Android App 开发文档](./Agent_Learning_Android_Room_SQLite_开发文档.md)
