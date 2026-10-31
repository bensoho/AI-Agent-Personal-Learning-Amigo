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
| 练习 | 单选、多选、代码阅读、代码补全、Debug 等题型，带答案解析 |
| 复习 | 错题本 + 基础间隔复习调度 |
| 统计 | 正确率、学习时长、每日记录等 |
| 设置 | 偏好设置、本地数据备份与恢复 |

### 知识点 → 学习计划 Tab

学习计划文档会被拆分为：

- **计划指南**：计划总览、学习方法、代码环境准备、资源与建议
- **Month 1–4**：每月显示月度目标，下列该月的 4 周
- **Week 01–16**：每周详情页合并展示文档中对应的三部分内容
  - 课程安排（第三章：理论学习、实战练习、练习题、交付物）
  - 学习讲义（第四章：概念讲解、跟做步骤、课后题参考答案）
  - 代码跟做示例（第五章：可复制的代码片段）

详情页支持标题、列表、引用、表格、代码块（带复制按钮）及粗体、行内代码等常用 Markdown 语法，文本可选中复制。

## 技术栈

- Kotlin 2.0、Jetpack Compose（Material 3）、Navigation Compose
- Hilt 依赖注入、Room（SQLite）、DataStore
- kotlinx.serialization（题库 JSON）、Coroutines / Flow
- 单元测试：JUnit 4、kotlinx-coroutines-test、Turbine
- minSdk 26 / targetSdk 34，JDK 17

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
