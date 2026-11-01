# LangChain + LangGraph + LangSmith：4个月 AI Agent 系统化学习计划

**学习目标：**
从传统全栈软件工程师成长为能够独立设计、开发、测试、部署和监控 AI Agent
应用的 AI Engineer。

**学习周期：** 16周（4个月）\
**建议投入：** 每周10--15小时，总计约160--240小时\
**技术主线：** LangChain → LangGraph → LangSmith → Production AI Agent

> 版本提示：学习时以官方文档当前稳定版本为准。LangChain、LangGraph
> 和相关集成更新较快，避免照搬旧版教程中的弃用 API。

------------------------------------------------------------------------

## 一、最终需要掌握的技术体系

### 1. LangChain：AI 应用开发基础

-   模型调用、Prompt、Messages
-   Structured Output、Pydantic
-   Tools、Tool Calling
-   Embeddings、Document Loaders、Text Splitters
-   Retriever、Vector Store、RAG
-   Agent 基础

### 2. LangGraph：Agent 工作流与复杂智能体

-   State、Nodes、Edges
-   Conditional Routing
-   Tool Calling Agent
-   Persistence、Memory、Checkpoint
-   Human-in-the-loop
-   Parallel Execution、Subgraph、Multi-Agent

### 3. LangSmith：Agent 工程化

-   Tracing、Runs、Trace Tree
-   Dataset、Evaluation
-   Prompt Management
-   Experiment Comparison
-   Monitoring、Debugging、成本与性能分析

### 4. 最终交付能力

-   独立设计和开发 AI Agent 项目
-   构建 RAG 和 Multi-Agent 系统
-   完成测试、评估、调试和优化
-   将 Agent 部署到生产环境并持续维护

------------------------------------------------------------------------

## 二、16周课程总览

  ------------------------------------------------------------------------------------------------
  阶段                              周次 学习主题                             阶段项目
  ---------------- --------------------- ------------------------------------ --------------------
  第1个月                           1--4 LangChain 基础、RAG、Advanced RAG    企业知识库问答系统

  第2个月                           5--8 LangGraph、Tools、Memory、Workflow   个人 AI 工作助手

  第3个月                          9--12 Multi-Agent、Agentic RAG、LangSmith  AI Software
                                                                              Development
                                                                              Assistant

  第4个月                         13--16 系统架构、安全、测试、部署           企业级 AI Agent 平台
  ------------------------------------------------------------------------------------------------

------------------------------------------------------------------------

# 三、详细课程安排

## 第1个月：LangChain 基础与 AI 应用开发

**月度目标：** 能够独立使用 LangChain 开发具有实际业务价值的 LLM 应用。

### 第1周：LLM 基础与开发环境

**理论学习** - LLM、Transformer、Token、Embedding - Temperature、Max
Tokens、Context Window - Prompt Engineering：System Prompt、User
Prompt、Few-shot - Chat Model、Model Provider - LangChain
核心架构与当前稳定版 API

**实战练习** 1. 使用 Python 调用一个模型 API。 2. 使用 LangChain
创建简单 Chatbot。 3. 实现多轮对话。 4. 实现不同 System Prompt
的角色切换。 5. 记录请求的 Token 使用量。

**练习题** 1. LLM 中 Token 和 Word 有什么区别？ 2. Temperature
对模型输出有什么影响？ 3. System Message 和 Human Message 有什么区别？
4. Chat Model 与直接调用普通 HTTP API 有什么区别？ 5. Context Window
超出限制时应该如何处理？

**本周交付物：** 支持多轮对话、角色切换和 Token 统计的 Chatbot。

### 第2周：LangChain 核心组件

**理论学习** -
Messages：SystemMessage、HumanMessage、AIMessage、ToolMessage - Prompt
Templates：ChatPromptTemplate、MessagesPlaceholder - Output
Parser：JSON、Pydantic Structured Output -
Runnable、RunnableLambda、RunnablePassthrough - Chat Model 与模型配置 -
Tool 定义、参数 Schema、工具调用

**实战练习** 1. 创建邮件生成助手。 2. 创建 JSON 数据提取程序。 3. 使用
Pydantic 定义输出结构。 4. 创建一个天气查询 Tool（可先使用模拟数据）。
5. 让模型根据用户输入决定是否调用 Tool。

**练习题** 1. Runnable 接口解决了什么问题？ 2. Structured Output 与普通
JSON Prompt 有什么区别？ 3. Tool Description 为什么重要？ 4.
模型如何判断是否需要调用 Tool？ 5. 如何避免模型返回不符合业务 Schema
的数据？

**本周交付物：** 能理解用户意图、调用工具并返回结构化 JSON 的 AI 助手。

### 第3周：RAG 基础

**理论学习** - Embedding 与文本向量化 - Vector Database - Document
Loader：PDF、Word、TXT - Text Splitter：Chunking、Overlap -
Retriever：Similarity Search、Top-K - Vector
Store：Chroma、FAISS、PGVector - RAG
Pipeline：Indexing、Retrieval、Generation

**实战练习** 1. 读取 PDF 和 Word 文档。 2. 将文档拆分为合理的 Chunk。 3.
使用 Embedding 将文本转换为向量。 4. 将向量存储到 Chroma。 5.
实现语义相似度搜索。 6. 实现基于企业文档的 RAG 问答。 7.
在回答中返回原始文档来源。

**练习题** 1. Embedding 的作用是什么？ 2. 为什么需要 Chunking？ 3. Chunk
Size 太大或太小分别有什么问题？ 4. Similarity Search 和 Keyword Search
有什么区别？ 5. RAG 为什么能降低模型幻觉？它能否完全消除幻觉？ 6. Top-K
设置为3和10分别有什么优缺点？

**本周交付物：** 企业 PDF/Word 知识库问答系统。

### 第4周：Advanced RAG 与综合项目

**理论学习** - Semantic Search、Hybrid Search - Metadata Filtering -
Reranking - Query Transformation、Multi-Query Retrieval - Context
Compression - RAG Evaluation

**实战练习** 1. 实现关键词与向量混合搜索。 2. 实现 Metadata Filter。 3.
实现多个 Query 自动生成。 4. 实现 Reranker。 5. 对比普通 RAG 和优化后的
RAG。 6. 为回答增加引用和来源信息。

**月度项目：企业知识库 AI 助手**

功能要求： - 支持 PDF、Word 文档上传。 - 自动建立知识库。 -
支持自然语言提问。 - 返回答案和文档引用。 - 支持多轮对话。 -
支持不同知识库分类。 - 对无依据的问题明确说明无法确定。

**验收标准：**
自行准备至少30个问题，测试检索准确性、回答正确性和引用准确性，并整理问题及改进记录。

------------------------------------------------------------------------

## 第2个月：LangGraph 核心与 Agent 工作流

**月度目标：** 从简单 LLM 应用升级到能够执行任务、管理状态和控制工作流的
Agent。

### 第5周：Agent 与 LangGraph 基础

**理论学习** - Agent 与普通 Chatbot 的区别 -
Workflow：固定流程与动态决策 - State、Graph State、状态更新 -
Node、Edge - Conditional Edge - START、END

**实战练习** 1. 使用 LangGraph 创建最简单的 Graph。 2.
创建接收问题、处理问题、输出结果三个 Node。 3. 使用 State
在节点之间传递数据。 4. 使用 Conditional Edge 实现条件路由。 5.
根据问题类型选择不同处理节点。

**练习题** 1. LangChain Agent 和 LangGraph 有什么关系？ 2. State 在
Graph 中起什么作用？ 3. Node 和 Edge 分别负责什么？ 4. 什么时候应该使用
Conditional Edge？ 5. 为什么复杂业务不适合全部交给一个 Prompt 处理？

**本周交付物：** 具有条件分支的 AI 工作流。

### 第6周：Tool Calling Agent 与执行控制

**理论学习** - Tool Calling、Agent Loop - 当前推荐的 Agent 创建方式 -
Tool Schema、Tool Execution - Middleware - Retry、Error Handling -
Structured Response

**实战练习**

创建具有以下工具的 Agent： 1. Search Tool：搜索信息。 2. Calculator
Tool：数学计算。 3. Database Tool：查询数据库。 4. File Tool：读取文件。
5. Custom API Tool：调用外部 REST API。

让 Agent 判断应该使用哪个工具，以及是否需要组合多个工具。

**练习题** 1. Tool Calling 是模型直接执行函数吗？ 2. Tool Calling
和普通函数调用有什么区别？ 3. 如何处理 Tool 执行失败？ 4. 如何限制 Agent
的最大执行次数？ 5. 如何防止 Agent 重复调用同一个 Tool？

**本周交付物：** 能调用多个工具完成复杂任务的 AI Agent。

### 第7周：Memory、Persistence 与 Human-in-the-loop

**理论学习** - Short-term Memory：当前对话线程状态 - Long-term
Memory：跨线程长期记忆 - Checkpointer、Persistence - Thread ID -
Interrupt - Human-in-the-loop

**实战练习** 1. 使用 SQLite Checkpointer 保存对话状态。 2. 使用 Thread
ID 区分不同用户。 3. 实现对话中断后继续执行。 4. 实现长期记忆存储。 5.
为敏感操作增加人工审批。 6. 实现用户修改 Agent 执行计划后继续运行。

**练习题** 1. Checkpointer 和 Store 有什么区别？ 2. Thread ID
的作用是什么？ 3. 为什么 Agent 需要 Persistence？ 4. Human-in-the-loop
适合哪些业务场景？ 5. Agent 被 Interrupt 后如何恢复执行？

### 第8周：复杂 Workflow 与状态管理

**理论学习** - Graph API、Functional API - Parallel Execution、Fan-out /
Fan-in - Command、Send - Subgraph - Streaming - Error Recovery - Time
Travel

**实战练习：智能数据分析 Agent** 1. 用户提交数据分析需求。 2. Agent
分析需求。 3. 自动拆分任务。 4. 并行执行多个分析节点。 5. 汇总结果。 6.
生成分析报告。 7. 敏感操作暂停并等待审批。

**月度项目：个人 AI 工作助手**

要求： - 理解用户任务。 - 调用多个 Tools。 - 具备多轮对话记忆。 -
执行多步骤工作流。 - 保存执行状态。 - 支持人工审批。 -
支持任务失败后的恢复。

**验收标准：**
至少设计10个业务场景，覆盖正常执行、工具失败、条件分支、人工审批和恢复执行。

------------------------------------------------------------------------

## 第3个月：高级 Agent 架构与 LangSmith

**月度目标：** 掌握复杂 Agent 架构，并能够追踪、测试、评估和优化 Agent。

### 第9周：Multi-Agent Architecture

**理论学习** - Multi-Agent - Supervisor、Sub-Agent - Handoff - Agent
Collaboration - Subgraph - Shared State

**需要掌握的架构模式** 1. Supervisor Pattern：主管 Agent 管理多个子
Agent。 2. Hierarchical Agent：多层级 Agent。 3. Handoff Pattern：Agent
之间交接任务。 4. Parallel Agent：多个 Agent 并行工作。 5. Reflection
Pattern：生成结果后由另一个 Agent 评审。

**实战练习**

创建三个 Agent： - Research Agent：信息收集。 - Analysis Agent：分析。 -
Writer Agent：生成报告。

再创建 Supervisor
Agent，负责分配任务、控制执行顺序、收集结果、检查完整性和决定是否重新执行。

**练习题** 1. 单 Agent 和 Multi-Agent 各自适合什么场景？ 2. Supervisor
Pattern 有什么优点？ 3. 多个 Agent 如何共享 State？ 4. Multi-Agent
是否一定比 Single-Agent 更好？ 5. 如何避免 Agent 之间无限交接任务？

### 第10周：Advanced RAG 与 Agentic RAG

**理论学习** - Agentic RAG、Adaptive RAG - Corrective RAG、Self-RAG -
Query Planning、Retrieval Routing - Document Grading、Answer Grading -
Reflection、Hallucination Detection

**普通 RAG 与 Agentic RAG** \| 普通 RAG \| Agentic RAG \| \|---\|---\|
\| 固定检索流程 \| Agent 决定如何检索 \| \| 通常一次查询 \| 可以多次查询
\| \| 固定 Retriever \| 可以选择不同 Retriever \| \| 通常直接生成 \|
可以检查检索和答案质量 \| \| 流程较简单 \| 可以动态规划和调整 \|

**实战练习：企业智能研发知识库** - 判断问题是否需要检索。 -
根据问题类型选择不同知识库。 - 检查检索结果相关性。 -
结果不充分时自动改写 Query 并重新检索。 - 对最终答案进行质量检查。 -
返回引用和必要的依据说明。

**练习题** 1. Agentic RAG 与普通 RAG 最大的区别是什么？ 2.
什么情况下需要 Query Rewrite？ 3. 为什么需要 Reranking？ 4.
如何判断检索结果是否足够回答问题？ 5. 如何减少 RAG 中的幻觉？

### 第11周：LangSmith 核心功能

**理论学习** - Tracing、Run、Trace Tree - Metadata - Dataset -
Evaluation - Experiment - Prompt Management - Monitoring

**实战练习** 1. 注册 LangSmith 并创建 Project。 2. 为 LangChain 应用开启
Tracing。 3. 为 LangGraph 应用开启 Tracing。 4. 查看一次 Agent
执行的完整 Trace Tree。 5. 分析模型调用耗时和 Token。 6. 追踪 Tool
调用失败。 7. 添加用户 ID、业务 ID 等 Metadata。 8. 管理和版本化
Prompt。

**练习题** 1. Tracing 和普通 Application Logging 有什么区别？ 2. Trace
和 Run 分别代表什么？ 3. 如何定位 Agent 执行过程中最耗时的步骤？ 4.
为什么需要区分不同环境的 Agent Trace？ 5. 如何通过 LangSmith 定位 Prompt
导致的输出质量下降？

### 第12周：LangSmith Evaluation 与 Agent 测试

**理论学习** - Dataset Creation - Example / Run - Evaluators -
LLM-as-a-Judge - Rule-based Evaluation - Pairwise Evaluation -
Experiment Comparison - Regression Testing - Online Evaluation

**实战练习** 1. 为 AI 工作助手建立至少50条测试数据。 2. 建立预期输出。
3. 编写规则型 Evaluator。 4. 编写 LLM-as-a-Judge Evaluator。 5. 评估
Agent 任务完成率。 6. 评估 Tool Selection 准确率。 7. 对比两个 Prompt
版本。 8. 对比两个 Agent 架构版本。 9. 建立回归测试流程。

**练习项目的初始指标参考** \| 指标 \| 目标参考值 \| \|---\|---:\| \|
Task Success Rate \| ≥ 85% \| \| Tool Selection Accuracy \| ≥ 90% \| \|
RAG Faithfulness \| ≥ 85% \| \| Schema Validity \| ≥ 99% \| \| Critical
Safety Violation \| 0 \|

这些是练习项目的初始验收目标，不是行业统一标准，应根据任务和数据集调整。

**月度项目：AI Software Development Assistant**

设计以下 Agent： - Requirement Agent：分析需求。 - Architecture
Agent：设计系统架构。 - Coding Agent：生成代码。 - Review
Agent：代码审查。 - Testing Agent：生成测试用例。 - Supervisor
Agent：管理整个流程。

工作流程：

``` text
User Requirement
       |
       v
Supervisor Agent
       |
       v
Requirement Analysis
       |
       v
Architecture Design
       |
       v
Coding Agent
       |
       v
Code Review Agent
       |
       v
Testing Agent
       |
       v
Human Approval
       |
       v
Final Report
```

同时使用 LangSmith 完成： - Agent Tracing - Token Usage Tracking -
Prompt Version Management - Evaluation Dataset - Agent Performance
Evaluation - Regression Testing

**验收标准：** 至少完成10个真实软件开发任务的端到端测试，保留
Trace、评估结果和问题分析报告。

------------------------------------------------------------------------

## 第4个月：生产级 AI Agent 项目

**月度目标：** 将 Agent Demo 做成可运行、可维护、可扩展的应用。

### 第13周：AI Agent 系统架构设计

**理论学习** - Agent 系统整体架构 - FastAPI REST API - PostgreSQL -
PGVector - Redis - Async / Await - SSE / Token Streaming - JWT / API
Key - Multi-tenancy

**推荐技术栈** - Backend：Python + FastAPI - Agent
Framework：LangChain + LangGraph - Observability：LangSmith -
Database：PostgreSQL - Vector Search：PGVector - Cache：Redis -
Frontend：Angular - Deployment：Docker

**实战练习** 1. 使用 FastAPI 暴露 Agent API。 2. 实现多用户会话。 3.
实现 SSE 流式输出。 4. 实现用户认证。 5. 使用 PostgreSQL 保存业务数据。
6. 使用 PGVector 保存文档向量。 7. 使用 Redis 实现缓存。 8. 实现 Angular
Chat UI。

**练习题** 1. 为什么不建议把所有 Agent 状态都保存在内存中？ 2. SSE 和
WebSocket 有什么区别？ 3. 如何实现多租户的数据隔离？ 4. LangGraph
Checkpointer 应该如何选择存储后端？ 5. 如何避免不同用户读取到彼此的
Agent Memory？

### 第14周：可靠性、安全与性能优化

**理论学习** - Retry Strategy、Timeout - Fallback Model - Rate
Limiting - Idempotency - Error Recovery - Prompt Injection - Tool
Permission - PII Protection - Guardrails - Token Optimization - Cost
Optimization

**实战练习** 1. 为 LLM 请求增加超时和重试。 2. 为 Tool 增加异常处理。 3.
为 Agent 设置最大执行次数。 4. 为敏感 Tool 添加权限控制。 5. 防止 Prompt
Injection。 6. 实现 API 限流。 7. 实现模型调用失败后的备用策略。 8. 优化
Prompt 和上下文长度。 9. 记录每个用户的 Token 和成本。

**练习题** 1. 为什么 Agent 需要最大执行次数？ 2. 如何避免 Tool
被恶意调用？ 3. Prompt Injection 和普通 Prompt 有什么区别？ 4. Retry 和
Fallback 有什么区别？ 5. 如何控制 Agent 的运行成本？

### 第15周：测试、部署和生产监控

**理论学习** - Unit Testing - Integration Testing - Agent Evaluation -
Mock LLM - Docker - CI/CD - Production Monitoring - Logging - Alerting -
Observability

**实战练习** 1. 为每个 Node 编写单元测试。 2. 为整个 Graph
编写集成测试。 3. 使用 Mock Tool 测试异常场景。 4. 使用 LangSmith
Evaluation 测试 Agent。 5. 使用 Docker 构建部署镜像。 6. 建立 CI/CD
流程。 7. 部署到云服务器。 8. 配置 LangSmith 生产 Trace。 9.
建立异常和成本监控。

### 第16周：毕业项目------企业级 AI Agent 平台

**项目名称：Enterprise AI Knowledge & Workflow Agent Platform**

**核心功能** \| 模块 \| 功能 \| \|---\|---\| \| AI Chat \| 多轮对话 \|
\| Knowledge Base \| 文档上传、知识库管理 \| \| RAG \| 企业知识检索 \|
\| Tool Calling \| 调用企业业务 API \| \| Agent Workflow \|
多步骤任务编排 \| \| Multi-Agent \| 多 Agent 协作 \| \| Human Approval
\| 敏感操作人工审批 \| \| Memory \| 短期和长期记忆 \| \| Tracing \|
LangSmith 执行追踪 \| \| Evaluation \| 自动评估 \| \| Admin \|
用户、权限、成本管理 \|

**最终验收任务** 1. 上传至少20份不同类型的企业文档。 2.
完成30个以上知识库问答测试。 3. 完成10个以上工具调用场景。 4.
完成至少3种多步骤 Agent 工作流。 5. 完成一个 Multi-Agent 协作场景。 6.
完成人工审批和中断恢复。 7. 建立至少100条 Evaluation Dataset。 8. 使用
LangSmith 进行质量评估和版本对比。 9. 完成 Docker 部署。 10.
编写系统架构文档、API 文档和测试报告。

------------------------------------------------------------------------

# 四、逐周学习讲义：理论解释、跟做示例与课后答案

本章对应前面的第1—16周安排。每周按“先理解概念 → 跟做最小示例 → 自己改造 → 完成课后题”的顺序学习。代码示例以 Python 为主；模型、向量库和 LangChain 集成的 API 可能随版本变化，运行前请对照官方文档，并将密钥放入环境变量，不要写入代码或提交到 Git。

**建议每周节奏（10—15小时）：**

1. 理论与官方文档：2—3小时，先写下本周概念之间的关系。
2. 跟做示例：4—6小时，先按步骤完成，再关掉教程独立重写。
3. 课后练习：2—3小时，至少增加一个边界或失败场景。
4. 复盘：1小时，记录运行结果、错误、改进前后对比，并提交代码。

> **答案使用方式：** 先独立作答，再对照答案解释“为什么”。答案是检查理解的参考，不是唯一实现；涉及准确率、成本、延迟等指标时，应以自己的数据集测量。

## 逐周内容完整性检查

下表按原计划逐项核对，区分“代码已演示”和“需要自己扩展完成”，避免把最小示例误当成整周项目已经做完。

| 周次 | 原计划实战核心 | 本文示例覆盖 | 项目级验收时还要完成 |
|---|---|---|---|
| 1 | 模型 API、多轮、角色切换、Token | LangChain 模型调用、多轮、角色对照、历史裁剪、用量输出 | 原始 HTTP 对照和真实 Token 用量记录 |
| 2 | 邮件、JSON、Pydantic、天气 Tool、工具选择 | 结构化分类、JSON Parser、邮件草稿和模拟天气 Agent | 增加更多邮件类型及异常输出测试 |
| 3 | PDF/Word、切分、Embedding、Chroma、RAG、引用 | 本地真实 Embedding、PDF/DOCX 加载、切分、Chroma、回答与来源 | 运行 5 个以上真实检索问题，调 Chunk/Top-K |
| 4 | Hybrid、Metadata、Multi-query、Rerank、对照评估 | RRF、租户过滤、结构化重排和对照指标 | 接入并测量真实关键词/向量检索器与 Multi-query |
| 5 | Graph、State、三个节点、条件路由 | 三个节点和条件路由 | 补齐分支测试、未知类别及错误输入 |
| 6 | Search、Calculator、Database、File、REST、多工具 | 本地 FAQ/订单/文件/REST 模拟和计算器、多工具 Agent、步数限制 | 替换真实服务时增加认证、超时和权限测试 |
| 7 | SQLite、Thread、Interrupt、长期记忆、审批、恢复 | SQLite Checkpointer、中断审批恢复、Store 命名空间 | 使用持久化 Store，测试服务重启及授权 |
| 8 | 分析、拆任务、并行、汇总、报告、审批 | 并行汇总及可嵌入的审批节点 | 组合成完整 Graph，测试 10 个成功/失败/恢复场景 |
| 9 | Research、Analysis、Writer、Supervisor | 三个有输入输出的工作节点及顺序控制 | 将模拟节点替换成子 Agent 并比较成本/质量 |
| 10 | 判断是否检索、路由、相关性、改写、答案检查 | 有限重试、相关性阈值、来源和无依据拒答 | 加入多个真实知识库路由及答案质量评测 |
| 11 | Chain/Graph Trace、耗时、失败、Metadata、Prompt 版本 | 模型 Trace、Agent/Graph 配置及版本 Metadata | 在 LangSmith 中检查真实失败 Trace 和耗时差异 |
| 12 | 50 条 Dataset、规则/Judge、架构对比、回归 | Dataset/实验代码和规则评估示例 | 补足至少 50 条样例、Judge 校准及架构对照 |
| 13 | API、多用户、SSE、认证、Postgres、PGVector、Redis、Angular | FastAPI、API Key 教学认证、SSE、参数化 SQL、PGVector/Redis 读写和 Angular 示例 | 接入真实认证并完成多用户端到端验证 |
| 14 | 超时、重试、限流、注入防护、Fallback、成本 | 重试/幂等、超时/备用模型、限流和工具白名单 | 按业务验证模型异常类型、PII/注入防护和成本配额 |
| 15 | 单测、集成、Mock、评估、Docker、CI/CD、监控 | 单测、Dockerfile、CI Workflow 和监控指标 | 给业务 Graph 写集成/Mock 测试并配置实际告警 |
| 16 | 知识库、工具、工作流、多 Agent、审批、评估、部署 | 可运行的带来源/拒答 Graph 及 pytest | 将前 15 周模块集成为项目，逐项通过毕业验收清单 |

每周示例是能运行的学习起点，不代表已替学习者完成整周项目；表中的“项目级验收”是必须亲自补齐的工作。第三方服务需要凭证的部分标明了配置条件；不具备服务时先用本地模拟完成同一接口，再接真实服务。

**练习题核对结果：** 第1—16周的讲义均包含课后题及参考答案。原详细安排中练习题不完整的第4、8、15、16周，已在讲义补上问题和答案；其余周保留对应主题的问题并补充答案。先自行作答，再用答案核对理由。

## 第1周：LLM 基础与开发环境

### 概念讲解

- **LLM 与 Transformer：** LLM 根据上下文预测后续 Token；Transformer 的注意力机制使模型能利用上下文中的相关信息。它生成的是概率性文本，不是自动查证过的事实。
- **Token 与上下文窗口：** Token 是模型处理文本的单位，不等于一个英文单词或一个汉字。上下文窗口通常同时容纳输入、历史消息和输出；超限时可能报错或截断。
- **Temperature：** 调节生成分布的随机性。温度较低通常更稳定，较高通常更有变化；它不等于正确率开关。
- **消息角色：** System 提供行为约束，Human 表达当前需求，AI 是模型回复。角色有助于组织上下文，但不能替代权限校验。

### 跟做：模型调用与多轮对话

1. 创建虚拟环境并配置模型提供方，确认 API Key 只从环境变量读取。
2. 单独发送一条用户消息，打印回答、耗时和可用的 Token 用量信息。
3. 用消息列表保存两轮对话；每轮将用户消息和模型回答追加到历史。
4. 将 System Prompt 改成“简洁解释”和“初学者导师”两种，使用相同问题比较结果。
5. 测试长输入，记录输入长度、模型报错或截断行为；设置合理的历史长度上限。

**验收示例：** 用户先说“我在学 Python”，再问“给我一个练习”。第二轮回答应能利用第一轮上下文；切换角色后风格变化，但不应伪造 Token 统计。

### 课后题参考答案

1. **Token 和 Word 有何区别？** Word 是语言学词语单位；Token 是特定模型的编码单位，可能是完整单词、词的一部分、标点或字符。不同模型的分词规则不同。
2. **Temperature 有什么影响？** 它影响采样随机性和多样性；具体效果也受模型、Prompt、采样参数影响，不能据此保证事实正确。
3. **System 与 Human Message 有何区别？** 前者描述助手的总体行为和约束，后者提供用户当前输入。两者共同构成模型上下文。
4. **Chat Model 与直接 HTTP API 有何区别？** Chat Model 通常封装消息格式、提供方差异和统一调用接口；底层仍是网络请求。直接 API 更灵活，但需自行处理鉴权、错误、重试和响应格式。
5. **上下文超限怎么办？** 限制历史长度、摘要旧对话、检索相关历史、缩短文档片段或选择更大上下文模型；应避免直接丢掉完成任务所需信息。

## 第2周：LangChain 核心组件

### 概念讲解

- **Prompt Template** 把固定指令与运行时变量分开，减少字符串拼接错误；`MessagesPlaceholder` 可插入对话历史。
- **Runnable** 为 Prompt、模型、解析器等提供可组合的调用形式，便于串联、批处理和流式处理。
- **Structured Output** 用 Schema 描述输出字段与类型；解析后的数据仍需进行业务校验，不能因为有 Schema 就默认内容可信。
- **Tool Calling** 通常是模型提出带参数的工具调用请求，由应用校验并执行工具，再把结果交回模型；模型本身并不直接执行 Python 函数。

### 跟做：邮件分类与模拟天气工具

1. 用 Pydantic 定义 `category`、`priority`、`summary` 三个字段及其约束。
2. 将一封邮件传给模型，要求返回该结构；解析失败时明确报错并记录原始响应，别静默返回空值。
3. 定义一个只读的模拟天气函数，参数为城市名，返回固定样例数据。
4. 将工具名称、用途、参数说明提供给模型；测试“查天气”和“写邮件”两类输入。
5. 断言工具参数符合 Schema，并记录模型是否选对工具；不要在本练习中接入真实的写操作 API。

**验收示例：** “客户反映订单晚到，请写一封道歉邮件”应生成结构化草稿；“查询杭州天气”才触发天气工具。

### 课后题参考答案

1. **Runnable 解决什么问题？** 让不同处理步骤拥有一致的组合与调用接口，降低组件粘合成本；它不会自动保证每一步的业务逻辑正确。
2. **Structured Output 与普通 JSON Prompt 有何区别？** 前者通常借助模型结构化输出能力或解析器约束并校验形状；仅在 Prompt 中要求 JSON 仍可能得到无效 JSON。
3. **Tool Description 为什么重要？** 模型据此判断工具用途、适用条件和参数含义；含糊描述会造成误选或错误参数。
4. **模型如何判断是否调用工具？** 根据消息、工具说明和模型的工具调用能力提出调用建议；应用应验证调用并决定是否执行。
5. **如何避免 Schema 不合规？** 使用结构化输出、Pydantic 校验、处理解析错误并设计有限重试；字段通过校验后仍需业务规则检查。

## 第3周：RAG 基础

### 概念讲解

- **Embedding** 把文本映射为向量，使语义相近的文本在向量空间中较接近；它不是摘要，也不代表事实验证。
- **Chunking** 将长文档拆成可检索片段。Chunk 太小会丢失上下文，太大则易混入无关内容并占用上下文窗口；Overlap 可缓和边界切断。
- **检索与生成：** 用户问题也被向量化，检索器取回候选片段，再将片段和问题交给模型生成回答。索引、检索、生成应分别调试。
- **引用来源：** 每个片段应保留文档名、页码或段落等 Metadata，回答引用应链接到实际检索到的来源。

### 跟做：小型文档问答

1. 手工准备 3—5 篇短文，每篇写入唯一标题和来源信息。
2. 按段落切分，给每个片段保存 `source`、`page` 等 Metadata。
3. 选择一种 Embedding 和本地向量库，写入片段；先检索 5 个已知答案的问题。
4. 打印问题、检索片段、相似度分数和来源，先确认“找对了”，再接生成模型。
5. 指令模型只依据检索内容回答；找不到依据时返回“现有资料无法确定”，并返回引用。
6. 用同一批问题测试不同 Chunk Size 和 Top-K，记录差异。

**验收示例：** 对资料中不存在的“公司 2028 年收入是多少？”回答无法确定，不得凭空补数字。

### 课后题参考答案

1. **Embedding 的作用是什么？** 将文本编码成向量，支持语义相似度搜索；向量距离不是事实正确性分数。
2. **为什么需要 Chunking？** 将长文档转成适合索引和检索的单元，并控制提供给模型的上下文大小。
3. **Chunk Size 大或小的影响？** 过大可能噪声多、定位粗、成本高；过小可能上下文不完整、召回片段零散。应由数据和检索评估决定。
4. **相似度搜索与关键词搜索的区别？** 前者偏语义近似，能匹配不同措辞；后者偏字面词项匹配，精确术语和编号常更可靠。可以组合使用。
5. **RAG 能否消除幻觉？** 不能。它提供外部证据、改善依据可追溯性，但检索可能错误，模型也可能误读或超出证据作答。
6. **Top-K=3 和 10 有何权衡？** 3 通常上下文更短、噪声较少但可能漏证据；10 召回机会更多但可能带来噪声、延迟和成本。应在标注问题集上比较。

## 第4周：Advanced RAG 与知识库项目

### 概念讲解

- **Hybrid Search** 结合关键词和向量检索，适合同时处理语义表达和专有名词、编号等精确匹配。
- **Metadata Filter** 在检索前按租户、分类、日期等范围缩小候选集；过滤条件必须由可信的服务端身份和权限决定。
- **Reranking** 对初次检索的候选片段重新排序；通常提升前几条相关性，但会增加计算与延迟。
- **Query Transformation / Multi-query** 将问题改写或扩展为多个检索查询；需去重、合并结果并评估是否引入偏题内容。

### 跟做：比较基线 RAG 与优化 RAG

1. 建立不少于 30 条测试问题，并标注每题预期来源和可接受答案要点。
2. 记录基线 RAG 的检索结果、引用正确性、回答质量和耗时。
3. 每次只改一个因素：混合检索、过滤、Query Rewrite 或 Reranking。
4. 再跑相同问题集，比较命中率、引用准确性、耗时和 Token；保留负面结果。
5. 实现“无依据则拒答”和“回答句子关联来源”的最小版本。

**验收示例：** 输出一张优化前后对比表，能够指出某项优化对哪些问题有效、对哪些问题有副作用。

### 课后练习与参考答案

1. **为什么做 Hybrid Search？** 向量检索擅长语义近似，关键词检索擅长精确词项；合并可提高互补场景的召回，但需验证排序。
2. **什么时候改写 Query？** 原问题过短、含指代、术语不一致或首轮检索证据不足时；改写应保留用户意图。
3. **Reranker 的价值与代价？** 可更精细地排序初筛候选，改善上下文前列质量；会增加模型/服务调用和延迟。
4. **如何判断证据足够？** 用标注问题集的召回与相关性指标、分数阈值、来源覆盖和业务规则共同判断；不能只依赖一个相似度阈值。
5. **如何减少 RAG 幻觉？** 提升检索质量、限制回答依据、要求引用、支持拒答，并评测答案与来源的一致性；无法保证完全消除。

## 第5周：Agent 与 LangGraph 基础

### 概念讲解

- **Workflow** 是开发者预先确定的步骤；**Agent** 在给定权限和约束内，根据模型输出选择下一步。可预测业务优先考虑显式 Workflow。
- **State** 是跨节点共享的任务数据；Node 接收 State 并返回更新，Edge 决定下一节点。
- **Conditional Edge** 根据 State 中的明确字段分流。循环必须有退出条件或步数上限，避免无限运行。

### 跟做：问题分类 Graph

1. 定义 State：`question`、`category`、`answer`。
2. 建立分类 Node，将问题标记为 `tech`、`billing` 或 `other`。
3. 建立三个处理 Node，为每类返回不同结果。
4. 用条件边依据 `category` 路由到处理 Node，再结束流程。
5. 用 10 条固定样例检查路由结果，并测试空字符串和未知类别。

**流程示意：** `START → classify → (tech | billing | other) → answer → END`。

### 课后题参考答案

1. **LangChain Agent 与 LangGraph 的关系？** LangChain 提供模型、工具等应用组件；LangGraph 提供有状态图执行和控制流能力，可承载 Agent 与 Workflow。
2. **State 的作用？** 保存任务在节点间传递和累积的数据，是路由和恢复执行的重要依据。
3. **Node 与 Edge 的职责？** Node 执行业务步骤并更新状态；Edge 描述执行顺序或路由规则。
4. **何时用 Conditional Edge？** 后续步骤需要依据当前状态、用户选择或模型分类决定时。
5. **为何不把复杂业务全写进一个 Prompt？** 难测试、难限制副作用、难恢复，也不易观测具体失败环节；拆分后可验证每个步骤。

## 第6周：Tool Calling Agent

### 概念讲解

- **Agent Loop** 常见形态是模型提出动作、应用执行工具、结果回到模型，循环直到完成或达到限制。
- **工具边界是安全边界：** 参数要验证，用户身份要授权，读写能力要区分；工具描述清楚不等于安全控制。
- **错误处理** 应区分可重试错误、业务拒绝和不可恢复错误。对外部写操作还要考虑幂等键与重复提交。

### 跟做：安全的只读工具 Agent

1. 从计算器和本地 FAQ 搜索两个只读工具开始，先写普通 Python 函数并单元测试。
2. 为函数加类型、参数范围和明确的错误信息，再注册为工具。
3. 测试“直接回答”“调用单个工具”“连续调用两个工具”三种输入。
4. 配置最大迭代次数；超限时返回明确状态，不伪装成成功答案。
5. 构造工具异常并验证 Agent 能说明失败；不要在练习中开放任意文件读写或任意 URL 请求。

### 课后题参考答案

1. **Tool Calling 是模型直接执行函数吗？** 不是。模型生成调用请求，应用负责校验、授权、执行并返回结果。
2. **与普通函数调用有何区别？** 普通函数由程序确定调用；Tool Calling 的候选动作由模型按上下文选择，但最终执行仍由程序控制。
3. **如何处理工具失败？** 返回可理解且不泄露敏感信息的错误，分类决定有限重试或停止，并记录日志和 Trace。
4. **如何限制最大执行次数？** 设置 Agent 最大步数/迭代数、超时和预算，达到限制时明确结束并返回状态。
5. **如何防止重复调用？** 设计清晰完成条件、记录已执行动作，对有副作用操作使用幂等键，并限制循环次数。

## 第7周：Memory、Persistence 与 Human-in-the-loop

### 概念讲解

- **Checkpoint / Checkpointer** 保存某个线程的图状态，便于暂停后恢复；**Store** 更适合保存跨线程复用的长期数据。两者适用范围不同。
- **Thread ID** 用来区分可恢复的会话或执行线程，不应单独作为授权凭证。
- **Human-in-the-loop** 在发送邮件、付款、删改数据等高影响动作前暂停，向人展示待执行内容，收到明确批准后继续。

### 跟做：审批后再执行

1. 给状态增加 `thread_id`、`draft_action`、`approval_status`。
2. 图先生成待审批草稿，再进入人工中断点；尚未获批时不调用写操作。
3. 使用持久化 Checkpointer 保存状态；以同一 Thread ID 恢复，以不同 ID 验证数据隔离。
4. 分别测试批准、拒绝、超时和恢复；拒绝时应结束或返回修改步骤。
5. 长期记忆只存用户明确允许保存且必要的信息，支持查看、更新和删除。

### 课后题参考答案

1. **Checkpointer 和 Store 有何区别？** Checkpointer 面向某个图线程的状态快照与恢复；Store 面向跨线程读写的长期数据。具体能力以选用的后端和版本为准。
2. **Thread ID 的作用？** 找回某个会话/任务的状态，不代表用户有权访问它；服务端仍须验证所有权。
3. **为何需要 Persistence？** 进程重启、长任务、审批等待或工具暂时失败时可恢复，避免全部从头执行。
4. **哪些场景适合人工审批？** 财务交易、对外发送、权限变更、删除数据、重要发布等有现实副作用的操作。
5. **Interrupt 后如何恢复？** 持久化暂停时状态，以同一线程标识重新加载；将人工决定写回状态后继续执行，并防止重复副作用。

## 第8周：复杂 Workflow 与状态管理

### 概念讲解

- **Fan-out / Fan-in** 把独立子任务并行执行，再汇总结果；并发能缩短等待，但要控制并发数和外部服务配额。
- **Subgraph** 把可复用的子流程封装起来；输入输出边界清晰时更容易测试和维护。
- **Streaming** 逐步返回执行进展或内容；流式输出不代表任务已成功完成。
- **Time Travel / 恢复** 有助于检查历史状态、重放或从检查点继续；必须防止重放有副作用的动作。

### 跟做：并行研究汇总

1. 把一个主题拆成“定义、优缺点、案例”三个互不依赖的只读任务。
2. 并行执行各任务，分别记录结果、错误和来源。
3. 汇总节点检查是否收齐结果；缺失任务应标记失败或有限重试。
4. 生成报告后做格式校验；任何对外操作先进入审批节点。
5. 比较串行与并行耗时，测试单个子任务失败时的处理。

### 课后练习与参考答案

1. **并行执行适合什么任务？** 子任务彼此独立且可安全并发的工作；存在依赖的步骤仍应按顺序执行。
2. **Fan-in 汇总要处理什么？** 结果完整性、重复项、错误、来源归属和冲突；不应把失败的分支当成成功结果。
3. **为什么需要 Subgraph？** 将明确边界的可复用流程模块化，便于独立测试和降低主图复杂度。
4. **流式输出就是成功吗？** 不是。仍要处理终态、异常、取消和中断，并明确告知用户最终状态。
5. **如何避免恢复时重复副作用？** 将副作用与可重放计算分离，检查执行记录，使用幂等操作或幂等键，并在恢复前确认状态。

## 第9周：Multi-Agent Architecture

### 概念讲解

- 多 Agent 的价值在于职责隔离、上下文隔离或可并行分工，不是“Agent 越多越聪明”。
- **Supervisor** 负责分派和汇总；**Handoff** 将任务控制权交给另一 Agent；**Shared State** 需要定义字段所有权和冲突处理。
- 任务交接应带有结构化输入、预期输出、预算和停止条件；否则容易重复劳动、循环或成本失控。

### 跟做：研究—分析—写作

1. 先用单一流程实现 Research、Analysis、Writer 三个普通函数，固定输入输出 Schema。
2. 为每个步骤写独立测试和失败结果格式。
3. 再用 Supervisor 按顺序调用它们；只在确实独立的研究主题上试并行。
4. 限制交接次数、总模型调用数和总时长，记录每步产物。
5. 比较单 Agent 和多 Agent 的质量、延迟、Token 成本，写出是否值得采用的结论。

### 课后题参考答案

1. **单 Agent 与 Multi-Agent 适用场景？** 单 Agent 适合步骤简单、上下文共享且目标明确的任务；多 Agent 适合职责清晰、可并行或需要隔离上下文的任务。
2. **Supervisor 的优点？** 将路由和结果汇总集中管理，便于观察整体流程；同时也可能成为瓶颈或单点复杂度。
3. **如何共享 State？** 通过显式 Schema 传入必要字段，约定字段写入方、版本和冲突策略；避免共享任意可变数据。
4. **Multi-Agent 一定更好吗？** 不是，可能增加成本、延迟、协调错误和调试难度；应通过对照评测证明收益。
5. **如何避免无限交接？** 设置最大步数、允许的状态转换、总预算和终止条件，并检测重复任务或重复状态。

## 第10周：Agentic RAG

### 概念讲解

- 普通 RAG 通常是固定的检索—生成流程；Agentic RAG 可依据问题决定是否检索、使用哪个检索器或是否重试。
- 检索评估和答案评估是两个问题：找到相关文档不代表答案正确；答案流畅也不代表有证据。
- 改写与重试应有限制，且保存每轮查询和结果，避免循环检索及无法解释的成本增长。

### 跟做：自适应检索流程

1. 对输入做分类：常识/闲聊、知识库问题、需要多个知识库的问题。
2. 只对需要的请求检索，并根据分类路由到相应知识库。
3. 用固定规则或评估器检查候选片段相关性；不足时最多改写并检索一次。
4. 生成答案时要求引用证据；证据仍不足则拒答或说明缺失信息。
5. 统计每条请求的检索次数、引用正确率和回答质量。

### 课后题参考答案

1. **Agentic RAG 与普通 RAG 的最大区别？** 前者可根据中间结果动态决定检索策略或下一步，后者通常按预设固定管线执行。
2. **什么时候 Query Rewrite？** 原查询歧义、含指代、首轮未召回相关证据或需要拆分复合问题时；改写须保留原始意图。
3. **为什么需要 Reranking？** 初筛召回追求候选覆盖，Reranker 可更精细排序候选，优先把相关证据放进有限上下文。
4. **如何判断证据足够？** 使用带标注的评测集、相关性/召回指标、来源覆盖和业务规则；低分阈值要结合实际数据校准。
5. **如何减少幻觉？** 提升检索、要求引用、只依据上下文回答、允许拒答并对答案做事实一致性评估；不能承诺彻底消除。

## 第11周：LangSmith Tracing

### 概念讲解

- **Trace** 展示一次请求的整体执行过程；**Run** 是其中一个模型、工具或链步骤。层级结构能定位失败和耗时所在。
- 普通日志侧重应用事件；Tracing 还呈现组件调用关系、输入输出、错误和耗时。二者互补。
- Trace 可能包含用户输入或业务数据；应限制敏感数据、访问权限和保留期限，并区分开发与生产项目。

### 跟做：追踪并定位慢调用

1. 在本地开发环境创建独立项目并安全配置 LangSmith 凭证。
2. 对已有链或图开启追踪，运行正常、工具报错和慢响应三类样例。
3. 在 Trace Tree 中查看调用顺序、每个 Run 的延迟、模型用量和错误。
4. 增加不含个人敏感信息的环境、版本和场景 Metadata。
5. 对比修改前后的 Trace，确认改动缩短了哪个步骤，而不是只比较总耗时。

### 课后题参考答案

1. **Tracing 与普通日志的区别？** Tracing 提供关联的层级调用、时序和组件输入输出；日志记录任意应用事件。排障时通常需要两者。
2. **Trace 与 Run 是什么？** Trace 表示一次端到端执行；Run 表示其中的单个组件调用或步骤。
3. **如何定位最耗时步骤？** 查看 Trace 中各 Run 的耗时和嵌套关系，区分模型、工具、网络及排队时间。
4. **为什么区分环境？** 避免开发噪声混入生产监控，方便按版本和部署环境比较与排障。
5. **如何发现 Prompt 导致的质量下降？** 将 Trace 与对应 Prompt 版本、输入和评测结果关联，对比同一数据集上的实验，并抽查失败样例。

## 第12周：Evaluation 与 Agent 测试

### 概念讲解

- **Dataset** 是可重复评测的输入样例及参考信息；需要覆盖常见、边界、拒答和失败情形。
- **规则型 Evaluator** 可稳定检查字段、工具名、引用格式等；**LLM-as-a-Judge** 能评语义质量，但可能有偏差，应校准并抽样人工复核。
- 对照实验应固定数据、配置和运行条件；将评测作为回归门槛，而不是单看一个平均分。

### 跟做：小型回归评测

1. 从真实或合成场景整理 20 条起步样例，记录输入、参考要点和期望行为；之后扩展至原计划的 50 条以上。
2. 写确定性检查：Schema 是否有效、必需引用是否存在、工具路由是否正确。
3. 设计简短明确的 Judge 量表，例如正确性、依据充分度各 1—5 分，并保留理由。
4. 对 Prompt A/B 运行同一数据集，比较分项成绩、失败案例、耗时和成本。
5. 将关键安全用例设置为必须通过的门槛；不能被平均分掩盖。

### 课后题参考答案

1. **Dataset 应包含什么？** 输入、参考答案或评分要点、预期工具/引用等可验证信息，并覆盖正常与边界场景。
2. **规则型和 Judge 如何配合？** 规则检查客观约束，Judge 评估语义质量；用人工标注样本检查 Judge 的一致性。
3. **为什么做 Pairwise 比较？** 同一输入下直接比较两个版本，减少评分尺度漂移；仍需随机化顺序并记录偏好理由。
4. **什么是回归测试？** 新版本在固定基准集上重复评估，确认关键能力没有退化，同时检查改进是否带来成本或安全副作用。
5. **参考指标阈值如何设定？** 根据业务风险、标注集和基线确定；示例目标不是通用行业标准，安全关键失败可单独设为零容忍门槛。

## 第13周：系统架构、API 与数据层

### 概念讲解

- API 服务负责身份认证、请求校验、限流和调用编排；Agent 逻辑不应直接绕过服务端授权。
- PostgreSQL 适合业务数据和事务；向量扩展适合相似度检索；Redis 可用于缓存或短期协调，但不能未经设计就当作唯一持久化存储。
- SSE 适合服务端向客户端持续推送事件；WebSocket 适合双向实时通信。认证、断线恢复和流结束语义仍需设计。
- 多租户隔离必须贯穿数据库查询、检索过滤、缓存键和日志；不能只靠前端传入租户 ID。

### 跟做：最小 Agent API

1. 定义 `POST /chat` 输入模型：会话 ID、问题；输出模型：答案、状态、引用。
2. 在服务端从认证上下文取得用户和租户身份，验证会话所有权后再运行 Agent。
3. 为长请求增加超时和请求 ID；支持 SSE 时明确区分 `start`、`token`、`error`、`done` 事件。
4. 将业务数据写入数据库，为向量检索增加租户过滤；缓存键中包含租户和模型/Prompt 版本。
5. 编写跨租户访问测试，确认用户不能读取其他租户会话或文档。

### 课后题参考答案

1. **为何不把所有状态放内存？** 服务重启会丢失，多个实例间也难共享，难以支持恢复和横向扩展。
2. **SSE 与 WebSocket 的区别？** SSE 是基于 HTTP 的单向服务端推送；WebSocket 提供双向持续通信。选择取决于交互需求。
3. **如何实现多租户隔离？** 服务端身份绑定租户，查询始终带租户范围，数据库约束/行级策略辅助，并测试越权、缓存和向量检索边界。
4. **Checkpointer 后端怎么选？** 原型可用本地 SQLite；生产根据并发、持久性、运维能力、备份和水平扩展需求选择受支持的数据库后端。
5. **如何避免读取他人 Memory？** 先验证会话所有权，后端按用户/租户范围读取；不可相信客户端提供的 Thread ID 或缓存键。

## 第14周：可靠性、安全与性能优化

### 概念讲解

- **Retry** 用于可恢复的瞬时失败；应设置次数上限、退避和抖动。**Fallback** 是主方案不可用时切换到另一个可接受方案。
- **Idempotency** 确保重复请求不重复产生副作用；超时并不代表服务端没有执行成功。
- Prompt Injection 可将不可信内容伪装成指令。真正的防线是工具最小权限、参数校验、隔离不可信数据、审批和输出检查，而非仅靠提示词。
- 成本控制可从模型选择、上下文长度、缓存、最大步数和配额入手；所有优化都需要质量回归测试。

### 跟做：故障注入与权限测试

1. 为模型和工具配置连接/读取超时及有限重试；对不可重试的校验错误直接返回。
2. 模拟超时、限流、模型不可用和重复提交，确认状态与用户提示正确。
3. 给工具实现基于用户身份的允许列表；写操作采用人工审批及幂等键。
4. 在知识库放入“忽略系统要求”等恶意文本，确认它被当作数据而非权限指令。
5. 给每次运行设置最大步骤、Token/成本预算和用户限额；记录超限原因。

### 课后题参考答案

1. **为何设置最大执行次数？** 防止循环、成本失控和长时间占用资源，并提供确定的停止边界。
2. **如何避免工具被恶意调用？** 服务端授权、最小权限、参数校验、目标 allowlist、速率限制、审计和高影响操作审批。
3. **Prompt Injection 是什么？** 不可信输入试图影响模型改变指令优先级或泄露/执行不当操作；不能仅靠区分文本标签来完全防护。
4. **Retry 与 Fallback 的区别？** Retry 重试同一操作；Fallback 切换到备用模型、工具或简化路径，二者可以组合但应有预算。
5. **如何控制成本？** 限制输入和输出长度、模型调用与 Agent 步数，合理缓存、选择模型、设置配额并持续监控成本/质量。

## 第15周：测试、部署与生产监控

### 概念讲解

- 单元测试验证 Node、Tool 和纯函数；集成测试验证 Graph、数据库和模型边界；评测集验证概率性输出的质量。
- 使用 Mock 可稳定复现超时、错误和边界响应；关键路径还需真实服务的少量集成测试。
- Docker 使运行环境更一致；CI/CD 应至少执行静态检查、单元测试、关键集成/评测门槛和镜像构建。
- 生产观测关注错误率、延迟、Token/成本、安全事件和任务完成率，同时避免记录不必要的敏感数据。

### 跟做：质量门禁流水线

1. 为每个 Node 和 Tool 添加成功、无效输入及异常测试。
2. 使用 Fake/Mock Model 测试路由、重试和人工审批；再用少量真实模型测试端到端行为。
3. 编写 Dockerfile，运行镜像并验证健康检查、环境变量和无密钥启动失败提示。
4. 在 CI 中执行格式/类型检查、单元测试和固定评测集；关键安全用例失败则阻止发布。
5. 部署后检查日志、Trace、告警和成本仪表板；演练一次回滚。

### 课后练习与参考答案

1. **为什么不能只测最终答案？** 错误可能来自路由、工具、检索或状态更新；节点级测试更容易定位根因。
2. **Mock 与真实集成测试如何搭配？** Mock 覆盖确定性故障和分支；真实集成测试验证服务兼容性，数量控制在必要范围。
3. **CI 中应包含什么？** 代码质量检查、单元测试、关键集成测试、评测门槛和镜像构建；密钥通过安全配置注入。
4. **生产应监控什么？** 成功率、错误率、延迟分位数、Token/成本、工具失败、拒答/安全事件和用户反馈。
5. **发生回归怎么办？** 暂停或回滚发布，检查版本关联 Trace 与失败样例，修复后在固定数据集重新评测再逐步发布。

## 第16周：毕业项目交付

### 概念讲解

- 毕业项目的重点是端到端工程闭环：需求边界、数据权限、可观察性、可恢复性、评测和部署，而非堆叠最多的 Agent 模式。
- 先实现可靠的单租户最小闭环，再扩展用户、知识库、工具和工作流；每项扩展都应有验收测试。
- 项目文档要使他人能复现：环境变量说明、启动步骤、架构图、API 契约、测试报告和已知限制。

### 跟做：分阶段交付清单

1. **第1阶段：** 明确 3 个核心用户场景、非目标、数据来源和成功指标。
2. **第2阶段：** 完成一个端到端知识库问答流程，包含来源引用、无依据拒答和评测集。
3. **第3阶段：** 增加一个只读工具及一个需人工审批的写操作模拟；实现状态保存和恢复。
4. **第4阶段：** 增加身份/租户边界、Trace、错误处理、成本限制和自动化测试。
5. **第5阶段：** Docker 部署，运行验收清单；提交架构、API、评测和部署文档，列出未完成项。

### 课后练习与参考答案

1. **怎样定义项目成功？** 将目标转成可测指标，例如固定问题集的引用正确率、任务完成率、错误率、延迟和每任务成本，并设定业务认可的门槛。
2. **先做 Multi-Agent 还是单 Agent？** 先做满足场景的最简单闭环；只有评测显示职责隔离或并行带来明确收益时再拆分。
3. **毕业项目必备的安全检查？** 身份与租户隔离、工具最小权限、敏感操作审批、密钥管理、输入/输出校验和敏感信息处理。
4. **如何证明系统可恢复？** 演示中断后从持久化状态继续，且重复请求不会重复执行副作用；保存测试步骤和结果。
5. **交付文档至少包含什么？** 架构与数据流、安装配置、API、运行和部署步骤、测试/评测结果、监控方式、已知限制及后续计划。

------------------------------------------------------------------------

# 五、逐周代码跟做示例

下面每周提供一段可复制运行或改造的最小代码。建议在项目根目录按 `examples/week-01.py`、`examples/week-02.py` 的方式保存，再使用 `uv run python examples/week-01.py` 运行。需要模型 API 的示例沿用项目中的 `get_model()`，请先按项目现有配置设置 `.env`；不要把 API Key 写进代码。

**依赖准备：**

```bash
# 基础 LangChain / DeepSeek 示例使用项目当前依赖
uv sync

# 后续章节需要外部向量库或 Embedding 提供方时再安装
uv add langgraph
uv add langchain-chroma langchain-huggingface
uv add langsmith
uv add fastapi uvicorn
uv add pytest
```

> 各周示例是学习用的最小片段，不等同于生产实现。第3周先用假 Embedding 认识向量库接口，后面另有接入真实本地 Embedding 的 PDF/Word RAG 完整示例；只有真实 Embedding 的检索结果适合用于评估语义检索质量。遇到集成包 API 更新时，以对应官方文档为准。

## 第1周：发起模型请求并保留多轮历史

保存为 `examples/week-01.py`：

```python
from applied_ai_engineer_practice.model import get_model
from langchain_core.messages import HumanMessage, SystemMessage

model = get_model(temperature=0.2)
messages = [
    SystemMessage(content="你是一位循序渐进的 Python 入门导师。"),
    HumanMessage(content="我正在学习 Python，请记住这一点。"),
]

first = model.invoke(messages)
messages.append(first)
print("第一轮：", first.content)

messages.append(HumanMessage(content="给我一个 5 分钟能完成的练习。"))
second = model.invoke(messages)
messages.append(second)
print("第二轮：", second.content)
print("Token 用量：", second.usage_metadata or "当前模型未返回用量信息")
```

运行：`uv run python examples/week-01.py`。试着改写 System Message，再观察回答风格；如果模型没有返回用量字段，应记录为“提供方未提供”，不要自行猜测。

角色切换和限制历史可在同一文件中继续练习：

```python
from langchain_core.messages import HumanMessage, SystemMessage
from applied_ai_engineer_practice.model import get_model

model = get_model(temperature=0)
question = "用一个例子解释 Python 的字典。"
for role in ("请用一句话回答。", "请用初学者能懂的步骤回答。"):
    response = model.invoke(
        [SystemMessage(content=role), HumanMessage(content=question)]
    )
    print(f"\n角色设定：{role}\n回答：{response.content}")

# 保留最近 N 轮 Human/AI 消息，避免无限增长；保留 System Message。
history = messages[:1] + messages[-4:]
print(f"\n裁剪前 {len(messages)} 条，裁剪后 {len(history)} 条")
```

## 第2周：Prompt 模板与结构化输出

保存为 `examples/week-02.py`：

```python
from pydantic import BaseModel, Field
from langchain_core.prompts import ChatPromptTemplate
from applied_ai_engineer_practice.model import get_model


class EmailLabel(BaseModel):
    category: str = Field(description="邮件类别，例如投诉、咨询或感谢")
    priority: int = Field(ge=1, le=3, description="优先级，1 最高，3 最低")
    summary: str = Field(description="一句话摘要")


model = get_model(temperature=0)
prompt = ChatPromptTemplate.from_messages(
    [
        ("system", "你是邮件分拣助手，只根据邮件内容分类。"),
        ("human", "邮件内容：{email}"),
    ]
)

chain = prompt | model.with_structured_output(
    EmailLabel,
    method="function_calling",
)
result = chain.invoke(
    {"email": "我的订单延迟一周仍未送到，请尽快帮我处理。"}
)
print(result.model_dump())
```

这段代码复用当前项目的模型配置。若使用的模型不支持 `function_calling` 结构化输出，请查看该提供方的 LangChain 集成文档，或改用 `PydanticOutputParser` 并处理解析异常。

天气工具和邮件草稿可以在这个基础上练习：

```python
from langchain.agents import create_agent
from langchain.tools import tool
from applied_ai_engineer_practice.model import get_model


@tool
def mock_weather(city: str) -> str:
    """查询模拟天气；仅支持杭州和上海，不访问外部服务。"""
    forecasts = {"杭州": "晴，25°C", "上海": "多云，23°C"}
    if city not in forecasts:
        return f"没有 {city} 的模拟天气数据。"
    return forecasts[city]


agent = create_agent(
    model=get_model(temperature=0),
    tools=[mock_weather],
    system_prompt="查询天气时调用 mock_weather。写邮件时直接生成草稿，不调用天气工具。",
)
for request in ("查一下杭州天气", "给客户写一封订单延迟的道歉邮件"):
    result = agent.invoke({"messages": [{"role": "user", "content": request}]})
    print(request, "=>", result["messages"][-1].content)
```

再将邮件分类 Schema 用于提取另一封邮件，检查 JSON 字段和天气工具是否分别在对应场景触发；不要只检查最终文本，还要查看 `result["messages"]` 中的 Tool 调用记录。

若本周还要比较“自由文本 JSON”与结构化输出，可用 `JsonOutputParser` 单独解析：

```python
from langchain_core.output_parsers import JsonOutputParser
from langchain_core.prompts import ChatPromptTemplate
from applied_ai_engineer_practice.model import get_model

json_prompt = ChatPromptTemplate.from_messages(
    [
        ("system", "只输出合法 JSON，不要使用 Markdown 代码围栏。"),
        ("human", "从邮件中提取 sender、issue 两个字段：{email}"),
    ]
)
json_chain = json_prompt | get_model(temperature=0) | JsonOutputParser()
print(json_chain.invoke({"email": "王敏来信：订单晚到，请告知预计送达时间。"}))
```

分别测试缺字段、错误类型和无效 JSON，记录解析异常；需要固定字段时优先使用 Pydantic Schema。

## 第3周：建立最小 RAG 检索流程

本周代码使用内存向量库和假 Embedding，不需安装额外向量数据库适配包。等切换到真实 Embedding / Chroma 时再安装：

```bash
uv add langchain-chroma langchain-huggingface
```

保存为 `examples/week-03.py`：

```python
from langchain_core.documents import Document
from langchain_core.embeddings import FakeEmbeddings
from langchain_core.vectorstores import InMemoryVectorStore

documents = [
    Document(
        page_content="差旅报销需在出差结束后 30 天内提交。",
        metadata={"source": "员工手册", "page": 12},
    ),
    Document(
        page_content="年假申请应至少提前 5 个工作日提交。",
        metadata={"source": "员工手册", "page": 8},
    ),
    Document(
        page_content="设备故障请联系 IT 服务台。",
        metadata={"source": "IT 指南", "page": 3},
    ),
]

# FakeEmbeddings 仅用于验证写入和检索流程；不是语义模型。
store = InMemoryVectorStore(embedding=FakeEmbeddings(size=32))
store.add_documents(documents)

query = "年假要提前多久申请？"
for doc, score in store.similarity_search_with_score(query, k=2):
    print(f"score={score:.3f} source={doc.metadata['source']} "
          f"page={doc.metadata['page']}: {doc.page_content}")
```

先确认能够写入、查询并保留来源字段。下一步将 `FakeEmbeddings` 换成实际 Embedding 提供方，再使用已知问题检查检索片段；假向量结果不能用于判断语义检索效果。

下面是连接真实本地 Embedding、PDF/Word 加载、切分、向量检索、生成和引用的端到端版本。首次运行会下载 Embedding 模型；需要网络和足够磁盘空间。

```bash
uv add langchain-community langchain-text-splitters langchain-chroma langchain-huggingface sentence-transformers pypdf docx2txt
```

将文件放入 `data/`，保存为 `examples/week-03-rag.py`：

```python
from pathlib import Path
from langchain_chroma import Chroma
from langchain_community.document_loaders import Docx2txtLoader, PyPDFLoader
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_core.prompts import ChatPromptTemplate
from langchain_text_splitters import RecursiveCharacterTextSplitter
from applied_ai_engineer_practice.model import get_model

documents = []
for path in Path("data").rglob("*"):
    if path.suffix.lower() == ".pdf":
        documents.extend(PyPDFLoader(str(path)).load())
    elif path.suffix.lower() == ".docx":
        documents.extend(Docx2txtLoader(str(path)).load())
if not documents:
    raise RuntimeError("data/ 下没有可读取的 PDF 或 DOCX 文档")

chunks = RecursiveCharacterTextSplitter(
    chunk_size=700, chunk_overlap=100
).split_documents(documents)
embeddings = HuggingFaceEmbeddings(
    model_name="sentence-transformers/all-MiniLM-L6-v2"
)
store = Chroma.from_documents(
    chunks,
    embedding=embeddings,
    persist_directory=".chroma",
    ids=[
        f"{doc.metadata.get('source', 'doc')}:{doc.metadata.get('page', 0)}:{index}"
        for index, doc in enumerate(chunks)
    ],
)

question = "差旅报销需要在什么时候提交？"
matches = store.similarity_search(question, k=4)
if not matches:
    raise RuntimeError("检索没有返回任何片段")

context = "\n\n".join(
    (
        f"[{index}] source={doc.metadata.get('source', 'unknown')}, "
        f"page={doc.metadata.get('page', 'unknown')}\n{doc.page_content}"
    )
    for index, doc in enumerate(matches, start=1)
)
prompt = ChatPromptTemplate.from_messages(
    [
        ("system", "仅依据给定资料回答。资料不足时明确回答无法确定，并在答案中标注引用编号及来源。"),
        ("human", "问题：{question}\n资料：\n{context}"),
    ]
)
answer = (prompt | get_model(temperature=0)).invoke(
    {"question": question, "context": context}
)
print("回答：", answer.content)
print("检索来源：")
for doc in matches:
    print(doc.metadata.get("source"), "page=", doc.metadata.get("page"))
```

运行 `uv run python examples/week-03-rag.py`。把 `question` 改成文档没有提到的问题，确认回答会拒绝猜测；再改变 `chunk_size` 和 `k`，记录来源命中变化。

## 第4周：用 Reciprocal Rank Fusion 合并检索结果

保存为 `examples/week-04.py`：

```python
def reciprocal_rank_fusion(
    result_lists: list[list[str]], k: int = 60
) -> list[tuple[str, float]]:
    scores: dict[str, float] = {}
    for results in result_lists:
        for rank, document_id in enumerate(results, start=1):
            scores[document_id] = scores.get(document_id, 0.0) + 1 / (k + rank)
    return sorted(scores.items(), key=lambda item: item[1], reverse=True)


keyword_results = ["policy-12", "policy-03", "policy-08"]
vector_results = ["policy-08", "policy-12", "policy-20"]
print(reciprocal_rank_fusion([keyword_results, vector_results]))
```

把 `keyword_results`、`vector_results` 替换为两种真实检索器的文档 ID 排名。练习加入 Metadata Filter，并验证过滤范围来自可信的服务端条件，而不是用户可随意指定的租户 ID。

将结果合并后，可用结构化输出做一次小候选集重排：

```python
from pydantic import BaseModel, Field
from langchain_core.prompts import ChatPromptTemplate
from applied_ai_engineer_practice.model import get_model


class RankedCandidates(BaseModel):
    ranked_ids: list[str] = Field(description="按与问题相关性从高到低排列的候选 ID")


def rerank(query: str, candidates: dict[str, str]) -> list[str]:
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", "只按候选文本是否能回答问题排序，不得编造候选 ID。"),
            ("human", "问题：{query}\n候选：{candidates}"),
        ]
    )
    ranker = get_model(temperature=0).with_structured_output(RankedCandidates)
    result = (prompt | ranker).invoke(
        {"query": query, "candidates": candidates}
    )
    allowed = set(candidates)
    ranked = [candidate_id for candidate_id in result.ranked_ids if candidate_id in allowed]
    return ranked + [candidate_id for candidate_id in candidates if candidate_id not in ranked]


candidate_text = {
    "policy-12": "差旅报销需在出差结束后 30 天内提交。",
    "policy-03": "办公设备申请需要主管审批。",
    "policy-08": "年假需提前 5 个工作日申请。",
}
print(rerank("年假要提前多久申请？", candidate_text))
```

完整练习顺序：先分别取关键词和向量检索 Top-K，再按服务端授权范围过滤，使用 RRF 合并，最后只对少量候选调用重排模型。用固定的 30 个问题对比基线与优化方案的命中、引用准确率、延迟和成本。

Multi-query 可先用模型扩写查询，再分别检索并把候选 ID 交给上面的 RRF：

```python
from pydantic import BaseModel, Field
from langchain_core.prompts import ChatPromptTemplate
from applied_ai_engineer_practice.model import get_model


class QueryVariants(BaseModel):
    queries: list[str] = Field(description="保留原意的 3 种检索表达")


query_prompt = ChatPromptTemplate.from_messages(
    [
        ("system", "将问题改写成三个含义相同、关键词不同的检索查询。"),
        ("human", "{question}"),
    ]
)
query_chain = query_prompt | get_model(temperature=0).with_structured_output(QueryVariants)
variants = query_chain.invoke({"question": "年假要提前多久申请？"})
print(variants.queries)
```

Metadata Filter 的关键是租户范围来自服务端认证结果，而不是问题文本：

```python
def retrieve_for_tenant(vector_store, query: str, authenticated_tenant_id: str):
    return vector_store.similarity_search(
        query,
        k=8,
        filter={"tenant_id": authenticated_tenant_id},
    )
```

创建测试资料时分别写入两个 `tenant_id`，确认租户 A 查询时结果中不出现租户 B 文档；再测试 Hybrid Search 中的每一路检索都应用了相同过滤条件。

## 第5周：LangGraph State、Node 和条件路由

保存为 `examples/week-05.py`：

```python
from typing import Literal
from typing_extensions import TypedDict
from langgraph.graph import END, START, StateGraph


class State(TypedDict):
    question: str
    category: str
    answer: str


def classify(state: State) -> dict:
    question = state["question"].lower()
    category = "billing" if "账单" in question or "费用" in question else "general"
    return {"category": category}


def billing_answer(state: State) -> dict:
    return {"answer": "我会帮你查看账单问题。"}


def general_answer(state: State) -> dict:
    return {"answer": "请告诉我你需要哪方面的帮助。"}


def route(state: State) -> Literal["billing", "general"]:
    if state["category"] == "billing":
        return "billing"
    return "general"


builder = StateGraph(State)
builder.add_node("classify", classify)
builder.add_node("billing", billing_answer)
builder.add_node("general", general_answer)
builder.add_edge(START, "classify")
builder.add_conditional_edges("classify", route)
builder.add_edge("billing", END)
builder.add_edge("general", END)
graph = builder.compile()

print(graph.invoke({"question": "我想查询本月账单费用", "category": "", "answer": ""}))
```

尝试给 `route()` 增加 `other` 分支，并为每个分支各写一个输入输出断言。若类型检查器无法推断分类值，可在分类节点中做显式校验，而不是忽略无效类别。

## 第6周：用工具 Agent 调用只读工具

保存为 `examples/week-06.py`：

```python
from langchain.agents import create_agent
from langchain.tools import tool
from applied_ai_engineer_practice.model import get_model


@tool
def add_numbers(a: int, b: int) -> int:
    """将两个整数相加。"""
    return a + b


agent = create_agent(
    model=get_model(temperature=0),
    tools=[add_numbers],
    system_prompt="需要计算时使用工具；不要猜测计算结果。",
)

result = agent.invoke(
    {"messages": [{"role": "user", "content": "计算 128 加 347。"}]}
)
print(result["messages"][-1].content)
```

该示例需要支持工具调用的模型。修改输入为普通知识问题，观察是否无需调用工具即可回答；再给工具传入无效参数，确认应用能显式处理错误。

下面将 Search、Calculator、Database 和 File 都限制在本地模拟数据中；可先验证 Agent 选择与组合工具，再逐一替换为经过授权的真实实现：

```python
from langchain.agents import create_agent
from langchain.tools import tool
from langgraph.errors import GraphRecursionError
from applied_ai_engineer_practice.model import get_model

FAQ = {"退款周期": "退款通常在 5 个工作日内到账。"}
ORDERS = {"A-100": "已发货"}
LOCAL_FILES = {"readme.txt": "这是允许读取的公开样例文件。"}


@tool
def search_faq(query: str) -> str:
    """搜索本地 FAQ，只返回匹配的已登记条目。"""
    return next((value for key, value in FAQ.items() if key in query), "未找到")


@tool
def lookup_order(order_id: str) -> str:
    """只读查询样例订单状态。"""
    return ORDERS.get(order_id, "订单不存在")


@tool
def read_public_file(filename: str) -> str:
    """读取预先允许的公开样例文件，不接受任意路径。"""
    return LOCAL_FILES.get(filename, "文件不在允许列表中")


@tool
def add_numbers(a: int, b: int) -> int:
    """将两个整数相加。"""
    return a + b


@tool
def mock_rest_status(ticket_id: str) -> str:
    """模拟只读 REST API 响应；只允许查询预置工单。"""
    response = {"T-100": {"status": "open", "owner": "support"}}
    return str(response.get(ticket_id, {"error": "ticket not found"}))


agent = create_agent(
    model=get_model(temperature=0),
    tools=[search_faq, lookup_order, read_public_file, add_numbers, mock_rest_status],
    system_prompt="仅按用户请求调用必要工具；不得猜订单号或尝试读取其他文件。",
)
try:
    result = agent.invoke(
        {"messages": [{"role": "user", "content": "订单 A-100 状态是什么？"}]},
        config={"recursion_limit": 8},
    )
    print(result["messages"][-1].content)
except GraphRecursionError as error:
    print(f"Agent 达到最大执行步数，任务未完成：{error}")
```

REST API Tool 的跟做任务应使用本地 Mock HTTP 服务，并设置超时、允许的主机列表和只读方法；不要让模型任意指定 URL 或文件路径。工具执行异常应由应用捕获、记录并以失败状态返回，不能把失败伪装成成功数据。

## 第7周：按 Thread ID 保存并续接会话

保存为 `examples/week-07.py`。本例需另外安装 SQLite Checkpointer：

```python
from typing_extensions import TypedDict
from langgraph.checkpoint.sqlite import SqliteSaver
from langgraph.graph import END, START, StateGraph
from langgraph.types import Command, interrupt


class State(TypedDict):
    thread_id: str
    draft: str
    approved: bool
    result: str


def prepare(state: State) -> dict:
    return {"draft": f"待执行操作：{state['draft']}"}


def approval(state: State) -> dict:
    decision = interrupt({"draft": state["draft"], "question": "是否批准？"})
    return {"approved": bool(decision["approved"])}


def execute(state: State) -> dict:
    if not state["approved"]:
        return {"result": "用户拒绝，操作未执行。"}
    return {"result": f"已执行模拟操作：{state['draft']}"}


builder = StateGraph(State)
builder.add_node("prepare", prepare)
builder.add_node("approval", approval)
builder.add_node("execute", execute)
builder.add_edge(START, "prepare")
builder.add_edge("prepare", "approval")
builder.add_conditional_edges(
    "approval",
    lambda state: "execute" if state["approved"] else END,
    {"execute": "execute", END: END},
)
builder.add_edge("execute", END)

with SqliteSaver.from_conn_string("checkpoints.sqlite") as checkpointer:
    graph = builder.compile(checkpointer=checkpointer)
    config = {"configurable": {"thread_id": "demo-user-001"}}
    initial = {
        "thread_id": "demo-user-001",
        "draft": "发送审批后的通知",
        "approved": False,
        "result": "",
    }
    paused = graph.invoke(initial, config=config)
    print("审批暂停状态：", paused)
    resumed = graph.invoke(
        Command(resume={"approved": True}),
        config=config,
    )
    print("恢复后结果：", resumed["result"])
```

安装：`uv add langgraph-checkpoint-sqlite`。SQLite 文件会持久化线程状态；使用不同 `thread_id` 验证线程隔离。长期记忆应使用独立 Store，并在读写前按服务端认证身份做授权，不要把客户端传入的 Thread ID 当成身份凭证。SQL 示例只模拟执行，不执行真实副作用。

跨会话记忆使用独立命名空间，练习时可先用内存 Store 验证隔离：

```python
from langgraph.store.memory import InMemoryStore

store = InMemoryStore()
user_namespace = ("user-memory", "authenticated-user-001")
store.put(user_namespace, "preferred_language", {"value": "中文"})
memory = store.get(user_namespace, "preferred_language")
print(memory.value["value"] if memory else "没有已保存偏好")
```

生产环境要换成持久化 Store，并提供用户可查看、删除和更正记忆的流程；不能仅凭模型推断的个人信息自动写入长期记忆。

## 第8周：并行执行两个节点并汇总

保存为 `examples/week-08.py`：

```python
import operator
from typing import Annotated
from typing_extensions import TypedDict
from langgraph.graph import END, START, StateGraph


class State(TypedDict):
    topic: str
    findings: Annotated[list[str], operator.add]
    report: str


def find_pros(state: State) -> dict:
    return {"findings": [f"{state['topic']}：记录一个优点"]}


def find_cons(state: State) -> dict:
    return {"findings": [f"{state['topic']}：记录一个风险"]}


def summarize(state: State) -> dict:
    return {"report": "\n".join(state["findings"])}


builder = StateGraph(State)
builder.add_node("pros", find_pros)
builder.add_node("cons", find_cons)
builder.add_node("summarize", summarize)
builder.add_edge(START, "pros")
builder.add_edge(START, "cons")
builder.add_edge(["pros", "cons"], "summarize")
builder.add_edge("summarize", END)
graph = builder.compile()

result = graph.invoke({"topic": "向量检索", "findings": [], "report": ""})
print(result["report"])
```

`Annotated` 中的 reducer 用于合并并行节点对 `findings` 的更新。增加一个可能失败的节点，明确区分“结果为空”和“执行失败”。

将本周练习扩展为数据分析流程：先由规划节点产生任务清单，再并行分析各列，最后汇总；遇到数据写入或外发报告时，在对应节点前增加人工审批中断。提交时至少用 10 个场景验证成功、单分支失败、全失败和恢复执行。

审批节点应使用 Checkpointer 保存中断状态。下面给出完整的并行分析、汇总、暂停审批和恢复流程；需要第7周的 SQLite Checkpointer 依赖：

```python
import operator
from typing import Annotated
from typing_extensions import TypedDict
from langgraph.checkpoint.sqlite import SqliteSaver
from langgraph.graph import END, START, StateGraph
from langgraph.types import Command, interrupt


class AnalysisState(TypedDict):
    topic: str
    findings: Annotated[list[str], operator.add]
    report: str
    approved: bool


def pros(state: AnalysisState) -> dict:
    return {"findings": [f"{state['topic']} 的优点已分析。"]}


def cons(state: AnalysisState) -> dict:
    return {"findings": [f"{state['topic']} 的风险已分析。"]}


def summarize(state: AnalysisState) -> dict:
    return {"report": "\n".join(state["findings"])}


def approval_gate(state: AnalysisState) -> dict:
    decision = interrupt({"action": "导出报告", "report": state["report"]})
    return {"approved": bool(decision["approved"])}


builder = StateGraph(AnalysisState)
builder.add_node("pros", pros)
builder.add_node("cons", cons)
builder.add_node("summarize", summarize)
builder.add_node("approval", approval_gate)
builder.add_edge(START, "pros")
builder.add_edge(START, "cons")
builder.add_edge(["pros", "cons"], "summarize")
builder.add_edge("summarize", "approval")
builder.add_edge("approval", END)

with SqliteSaver.from_conn_string("analysis-checkpoints.sqlite") as saver:
    graph = builder.compile(checkpointer=saver)
    config = {"configurable": {"thread_id": "analysis-001"}}
    paused = graph.invoke(
        {"topic": "向量检索", "findings": [], "report": "", "approved": False},
        config=config,
    )
    print("审批暂停：", paused)
    resumed = graph.invoke(Command(resume={"approved": True}), config=config)
    print("审批后状态：", resumed["approved"])
```

练习将“批准”与“拒绝”分别运行，确认审批前没有真实副作用；再关闭进程后用相同数据库文件和 Thread ID 恢复，验证持久化行为。真实系统还需鉴权确认审批人的权限。

## 第9周：用 Supervisor 路由到不同专业节点

保存为 `examples/week-09.py`：

```python
from typing_extensions import TypedDict
from langgraph.graph import END, START, StateGraph


class State(TypedDict):
    task: str
    next_step: str
    research_notes: str
    analysis: str
    result: str


def supervisor(state: State) -> dict:
    # 先用确定性规则学习路由；后续可用模型选择受限的下一步。
    if "只写标题" in state["task"]:
        return {"next_step": "writer"}
    return {"next_step": "research"}


def research(state: State) -> dict:
    return {"research_notes": f"关于“{state['task']}”的研究资料摘要。"}


def analyze(state: State) -> dict:
    if not state["research_notes"]:
        raise ValueError("缺少研究资料，不能继续分析")
    return {"analysis": f"基于资料的分析：{state['research_notes']}"}


def writer(state: State) -> dict:
    return {"result": f"报告标题：{state['task']}\n\n{state['analysis']}"}


def route_from_supervisor(state: State) -> str:
    return state["next_step"]


builder = StateGraph(State)
builder.add_node("supervisor", supervisor)
builder.add_node("research", research)
builder.add_node("analysis", analyze)
builder.add_node("writer", writer)
builder.add_edge(START, "supervisor")
builder.add_conditional_edges(
    "supervisor",
    route_from_supervisor,
    {"research": "research", "writer": "writer"},
)
builder.add_edge("research", "analysis")
builder.add_edge("analysis", "writer")
builder.add_edge("writer", END)

# Supervisor 控制执行顺序；每个节点在真实项目中可替换为受限的子 Agent。
graph = builder.compile()
result = graph.invoke(
    {
        "task": "比较 RAG 与关键词搜索",
        "next_step": "",
        "research_notes": "",
        "analysis": "",
        "result": "",
    }
)
print(result["result"])
```

这里先用确定性 Supervisor 验证 Research → Analysis → Writer 的输入输出契约。尝试“只写标题”分支和缺少资料的异常；接入模型子 Agent 后，要求每个子 Agent 返回结构化产物，限制 Supervisor 的交接次数并检查结果完整性，再决定是否重做。

## 第10周：检索不足时有限改写

保存为 `examples/week-10.py`：

```python
from dataclasses import dataclass


@dataclass
class Evidence:
    text: str
    score: float
    source: str


def retrieve(query: str) -> list[Evidence]:
    # 用固定数据演示评分和有限重试；接入向量库后替换此函数。
    knowledge = {
        "年假申请": [Evidence("年假需提前 5 个工作日申请。", 0.92, "HR 手册 p.8")],
        "报销": [Evidence("差旅报销需在出差结束后 30 天内提交。", 0.91, "财务手册 p.12")],
    }
    return next((docs for key, docs in knowledge.items() if key in query), [])


def answer_question(question: str) -> str:
    query = question
    for attempt in range(2):  # 首次检索 + 最多一次改写检索，防止无限循环
        evidence = retrieve(query)
        relevant = [item for item in evidence if item.score >= 0.8]
        if relevant:
            item = relevant[0]
            return f"{item.text}（来源：{item.source}）"
        query = f"员工制度：{question}"
    else:
        return "现有知识库没有足够依据回答这个问题。"


print(answer_question("年假申请要提前多久？"))
print(answer_question("公司明年的营收目标是多少？"))
```

先观察有依据与无依据两种输出，再把模拟 `retrieve()` 替换为第3周的向量检索器。进一步把 `query` 分类到 HR/财务等不同知识库，并记录原问题、改写问题、检索分数、来源和重试次数。分数阈值只是示例，必须用标注集校准。

将证据送入生成步骤并要求逐条引用；生成后再单独检查来源是否存在：

```python
from langchain_core.prompts import ChatPromptTemplate
from applied_ai_engineer_practice.model import get_model

answer_prompt = ChatPromptTemplate.from_messages(
    [
        ("system", "只根据证据回答，并引用来源。证据不足时回答无法确定。"),
        ("human", "问题：{question}\n证据：{evidence}"),
    ]
)


def generate_grounded_answer(question: str, evidence: list[Evidence]) -> str:
    if not evidence:
        return "现有知识库没有足够依据回答这个问题。"
    context = "\n".join(
        f"{item.text}（来源：{item.source}）" for item in evidence
    )
    response = (answer_prompt | get_model(temperature=0)).invoke(
        {"question": question, "evidence": context}
    )
    return response.content
```

## 第11周：开启 LangSmith Tracing

先安装并设置凭证：

```bash
uv add langsmith
export LANGSMITH_TRACING=true
export LANGSMITH_API_KEY="在本地安全设置，不要提交到代码库"
export LANGSMITH_PROJECT="agent-learning-dev"
```

保存为 `examples/week-11.py`：

```python
from applied_ai_engineer_practice.model import get_model

model = get_model(temperature=0)
answer = model.with_config(
    run_name="week-11-simple-answer",
    tags=["learning", "week-11"],
    metadata={"environment": "development"},
).invoke("用一句话解释什么是 LangSmith Trace。")

print(answer.content)
```

在 LangSmith 项目中查看这次调用的 Trace。不要将密码、API Key 或不必要的个人信息放进 Trace 的输入、Metadata 或日志中。

Graph 和工具异常也应产生可定位的 Trace。可以把第5周的 `graph` 或第6周的 `agent` 加入下面的配置，再分别执行正常与错误输入：

```python
from langchain.agents import create_agent
from langchain.tools import tool
from applied_ai_engineer_practice.model import get_model


@tool
def lookup_demo_order(order_id: str) -> str:
    """查询演示订单。"""
    if order_id != "A-100":
        raise ValueError(f"订单不存在：{order_id}")
    return "已发货"


agent = create_agent(
    model=get_model(temperature=0),
    tools=[lookup_demo_order],
    system_prompt="查询订单必须调用 lookup_demo_order；工具错误时说明无法查询。",
)
run_config = {
    "run_name": "week-11-agent-run",
    "tags": ["learning", "week-11", "prompt-v1"],
    "metadata": {
        "environment": "development",
        "prompt_version": "v1",
        "scenario": "tool-error",
    },
}
result = agent.invoke(
    {"messages": [{"role": "user", "content": "查询不存在的订单 A-999。"}]},
    config=run_config,
)
print(result["messages"][-1].content)
```

不要把真实用户 ID 或业务敏感内容直接写进 Metadata；使用无敏感性的测试标识。Prompt 改版时更新 `prompt_version`，用同一组输入比较两个版本的 Trace 与评测结果。

Prompt 版本管理可从 LangSmith 拉取已发布 Prompt，而不是把生产 Prompt 散落在代码中：

```python
from langsmith import Client
from langchain_core.prompts import ChatPromptTemplate

client = Client()
prompt = ChatPromptTemplate.from_messages(
    [("system", "你是内部知识助手。"), ("human", "{question}")]
)
client.push_prompt("agent-learning-support-prompt", object=prompt)

published_prompt = client.pull_prompt("agent-learning-support-prompt")
print(published_prompt.invoke({"question": "如何申请年假？"}))
```

为版本加清晰描述，先在开发 Project 验证，再将选定版本发布；不要把用户私有数据写进 Prompt 模板。

## 第12周：用固定数据做规则评测

保存为 `examples/week-12.py`：

```python
examples = [
    {"expected_tool": "calculator", "actual_tool": "calculator", "answer": "475"},
    {"expected_tool": "search", "actual_tool": "calculator", "answer": "未找到资料"},
]

tool_correct = sum(
    row["expected_tool"] == row["actual_tool"] for row in examples
) / len(examples)
schema_valid = all(isinstance(row["answer"], str) for row in examples)

print(f"Tool selection accuracy: {tool_correct:.0%}")
print(f"Answer field is string for every example: {schema_valid}")

for index, row in enumerate(examples, start=1):
    if row["expected_tool"] != row["actual_tool"]:
        print(f"需要复盘的样例 #{index}: {row}")
```

把固定样例换成自己的 Dataset；此处只演示确定性指标。语义正确性需要参考答案/评分标准及人工抽样或经过校准的 Judge，不能用字符串类型检查替代。

LangSmith 数据集与实验可通过 SDK 运行（先设置 `LANGSMITH_API_KEY`、`LANGSMITH_TRACING=true`）：

```python
from langsmith import Client
from langsmith.evaluation import evaluate
from applied_ai_engineer_practice.model import get_model

client = Client()
dataset_name = "agent-learning-week-12"
dataset = client.create_dataset(dataset_name=dataset_name)
client.create_examples(
    dataset_id=dataset.id,
    inputs=[
        {"question": "年假要提前多久申请？"},
        {"question": "资料中没有的收入数据是多少？"},
    ],
    outputs=[
        {"expected": "5 个工作日"},
        {"expected": "无法确定"},
    ],
)

def target(inputs: dict) -> dict:
    response = get_model(temperature=0).invoke(inputs["question"])
    return {"answer": response.content}


def contains_reference(run, example) -> dict:
    answer = run.outputs["answer"]
    expected = example.outputs["expected"]
    return {
        "key": "contains_reference",
        "score": expected in answer,
    }


experiment = evaluate(
    target,
    data=dataset_name,
    evaluators=[contains_reference],
    experiment_prefix="prompt-v1",
)
print(experiment)
```

重复运行前先检查数据集是否已存在，避免每次练习都创建同名 Dataset 失败；将 `target` 改为两个 Prompt 或 Agent 版本，在同一 Dataset 上比较。该字符串评估器只作演示，真实 RAG 评估还要检查检索相关性、答案依据和拒答行为。

LLM-as-a-Judge 可输出可追踪的分数和理由；以下示例仍需用人工标注样本检查 Judge 是否偏袒冗长或措辞相似的答案：

```python
from pydantic import BaseModel, Field
from langchain_core.prompts import ChatPromptTemplate
from applied_ai_engineer_practice.model import get_model


class JudgeResult(BaseModel):
    score: int = Field(ge=1, le=5, description="答案正确且有依据的程度")
    reason: str = Field(description="引用标准说明评分理由")


judge_prompt = ChatPromptTemplate.from_messages(
    [
        (
            "system",
            "你是严格评审员。比较答案与参考要点，事实错误或无依据内容要扣分。",
        ),
        (
            "human",
            "问题：{question}\n参考要点：{expected}\n待评答案：{answer}",
        ),
    ]
)
judge = judge_prompt | get_model(temperature=0).with_structured_output(JudgeResult)
rating = judge.invoke(
    {
        "question": "年假要提前多久申请？",
        "expected": "提前 5 个工作日",
        "answer": "年假需提前 5 个工作日申请。",
    }
)
print(rating.model_dump())
```

原计划要求至少 50 条样例。把人工编写、去重并审阅的案例保存为 `data/evaluation.jsonl`，每行包含 `inputs` 和 `outputs`，然后导入并检查数量：

```python
import json
from pathlib import Path
from langsmith import Client

cases = [
    json.loads(line)
    for line in Path("data/evaluation.jsonl").read_text(encoding="utf-8").splitlines()
    if line.strip()
]
if len(cases) < 50:
    raise ValueError(f"评测集至少需要 50 条样例，当前只有 {len(cases)} 条")

client.create_examples(
    dataset_name="agent-learning-week-12-reviewed",
    inputs=[case["inputs"] for case in cases],
    outputs=[case["outputs"] for case in cases],
)
```

不要用复制同一条样例的方式凑够 50 条；案例应覆盖正常请求、边界输入、工具失败、拒答和安全场景。Judge 评估还要抽取一部分人工打分，确认模型评分与人工标准大致一致后再用于回归门槛。

## 第13周：用 FastAPI 暴露最小问答 API

先安装：`uv add fastapi uvicorn`。保存为 `examples/week_13.py`：

```python
from fastapi import FastAPI
from pydantic import BaseModel, Field

app = FastAPI()


class ChatRequest(BaseModel):
    question: str = Field(min_length=1, max_length=4000)


class ChatResponse(BaseModel):
    answer: str


@app.post("/chat", response_model=ChatResponse)
def chat(request: ChatRequest) -> ChatResponse:
    # 教学占位：将此处替换为 Agent 调用，并从认证上下文取得用户身份。
    return ChatResponse(answer=f"收到问题：{request.question}")
```

运行：`uv run uvicorn examples.week_13:app --reload`。这是无认证的本地教学示例，不可直接部署到公网；上线前应加入身份认证、授权、限流、请求超时和租户隔离。

将占位接口扩展成 API Key 保护的 SSE 流式接口（教学鉴权，不等同完整 JWT 身份系统）。在 `.env` 中配置 `DEMO_API_TOKEN`，并在服务启动时加载：

```python
import hmac
import os
from collections.abc import AsyncIterator
from fastapi import Depends, HTTPException
from fastapi.responses import StreamingResponse
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from dotenv import load_dotenv
from applied_ai_engineer_practice.model import get_model

load_dotenv()
security = HTTPBearer()


def require_token(
    credentials: HTTPAuthorizationCredentials = Depends(security),
) -> str:
    expected = os.getenv("DEMO_API_TOKEN")
    if not expected:
        raise HTTPException(status_code=503, detail="API token 未配置")
    if not hmac.compare_digest(credentials.credentials, expected):
        raise HTTPException(status_code=401, detail="无效的 API token")
    return "demo-user"


async def token_stream(question: str) -> AsyncIterator[str]:
    async for chunk in get_model(temperature=0).astream(question):
        content = chunk.content
        if isinstance(content, str) and content:
            yield f"event: token\ndata: {content}\n\n"
    yield "event: done\ndata: {}\n\n"


@app.get("/chat/stream")
async def chat_stream(
    question: str,
    user_id: str = Depends(require_token),
) -> StreamingResponse:
    return StreamingResponse(
        token_stream(question),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache"},
    )
```

多租户练习：数据库每条会话和文档记录都保存 `tenant_id`，服务端从认证上下文取得租户 ID，再将其用于 SQL、向量检索和 Redis Key。可以先按以下约束建表，然后写跨租户拒绝测试：

```sql
CREATE TABLE conversations (
    tenant_id TEXT NOT NULL,
    conversation_id TEXT NOT NULL,
    thread_id TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, conversation_id),
    UNIQUE (tenant_id, thread_id)
);

CREATE TABLE conversation_messages (
    tenant_id TEXT NOT NULL,
    conversation_id TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('human', 'assistant', 'tool')),
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    FOREIGN KEY (tenant_id, conversation_id)
        REFERENCES conversations (tenant_id, conversation_id)
);
```

缓存键示例：`f"{tenant_id}:{model_version}:{prompt_version}:{query_hash}"`。关系数据库应始终使用参数化 SQL：

```bash
uv add "psycopg[binary]" redis
```

```python
import os
import redis
import psycopg

cache = redis.Redis.from_url(os.environ["REDIS_URL"], decode_responses=True)


def load_conversation(tenant_id: str, conversation_id: str) -> list[tuple[str, str]]:
    with psycopg.connect(os.environ["DATABASE_URL"]) as connection:
        rows = connection.execute(
            """
            SELECT role, content
            FROM conversation_messages
            WHERE tenant_id = %s AND conversation_id = %s
            ORDER BY created_at
            """,
            (tenant_id, conversation_id),
        ).fetchall()
    return rows
```

PGVector 练习先运行 `CREATE EXTENSION IF NOT EXISTS vector;`，再用 LangChain 集成写入和检索：

```bash
uv add langchain-postgres langchain-huggingface sentence-transformers
```

```python
import os
from langchain_postgres import PGVector
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_core.documents import Document

embeddings = HuggingFaceEmbeddings(
    model_name="sentence-transformers/all-MiniLM-L6-v2"
)
vector_store = PGVector(
    embeddings=embeddings,
    collection_name="knowledge_chunks",
    connection=os.environ["DATABASE_URL"],
    use_jsonb=True,
)
vector_store.add_documents(
    [
        Document(
            page_content="年假需提前 5 个工作日申请。",
            metadata={"tenant_id": "tenant-a", "source": "员工手册", "page": 8},
        )
    ]
)
tenant_id = "tenant-a"  # 实际应用中必须来自认证后的服务端上下文
matches = vector_store.similarity_search(
    "年假如何申请？",
    k=4,
    filter={"tenant_id": tenant_id},
)
print([(doc.page_content, doc.metadata) for doc in matches])
```

使用 Redis 缓存只读回答，缓存键包含租户和 Prompt 版本：

```python
def cache_answer(tenant_id: str, prompt_version: str, query_hash: str, answer: str) -> None:
    key = f"answer:{tenant_id}:{prompt_version}:{query_hash}"
    cache.setex(key, 300, answer)


def get_cached_answer(tenant_id: str, prompt_version: str, query_hash: str) -> str | None:
    key = f"answer:{tenant_id}:{prompt_version}:{query_hash}"
    return cache.get(key)
```

向量表应保存 `tenant_id`、`source`、`page` 等 Metadata，并确保每种相似度检索都带租户过滤。Angular 前端可用 `fetch` 读取带 Authorization Header 的 SSE：

```typescript
async function streamAnswer(question: string, token: string): Promise<void> {
  const response = await fetch(
    `/chat/stream?question=${encodeURIComponent(question)}`,
    { headers: { Authorization: ["Bearer ", token].join("") } },
  );
  if (!response.ok || !response.body) {
    throw new Error(`聊天请求失败：${response.status}`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";
  while (true) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    let boundary = buffer.indexOf("\n\n");
    while (boundary >= 0) {
      const event = buffer.slice(0, boundary);
      buffer = buffer.slice(boundary + 2);
      if (event.startsWith("event: token")) {
        console.log(event.slice("event: token".length).trim());
      } else if (event.startsWith("event: done")) {
        return;
      }
      boundary = buffer.indexOf("\n\n");
    }
  }
}
```

将 `console.log` 改为 Angular 组件状态更新，并处理网络错误、组件销毁和取消请求。Token 应由正式登录流程签发并安全保存；该练习不构成完整认证方案。

## 第14周：超时重试边界与幂等键

保存为 `examples/week-14.py`：

```python
import time


class TemporaryServiceError(Exception):
    pass


def call_with_retry(operation, attempts: int = 3):
    for attempt in range(attempts):
        try:
            return operation()
        except TemporaryServiceError:
            if attempt == attempts - 1:
                raise
            time.sleep(0.1 * (2**attempt))


completed: dict[str, str] = {}


def send_once(idempotency_key: str, message: str) -> str:
    if idempotency_key in completed:
        return completed[idempotency_key]
    # 实际写操作应在数据库事务或外部服务端实现幂等，不能只依赖进程内字典。
    result = f"已处理消息：{message}"
    completed[idempotency_key] = result
    return result


print(send_once("request-123", "审批通过"))
print(send_once("request-123", "审批通过"))
```

运行后确认相同键不会重复执行示例动作。将 `operation` 替换为会抛出 `TemporaryServiceError` 的函数，观察有限重试；参数错误等永久失败不应捕获成可重试错误。

超时、备用方案、简单限流和工具白名单的最小示例：

```python
import asyncio
import os
import time
from collections import defaultdict
from applied_ai_engineer_practice.model import get_model


async def ask_with_fallback(question: str) -> str:
    primary = get_model(temperature=0)
    try:
        response = await asyncio.wait_for(
            primary.ainvoke(question),
            timeout=15,
        )
        return response.content
    except (TimeoutError, OSError):
        backup_name = os.getenv("BACKUP_MODEL")
        if not backup_name:
            raise RuntimeError("主模型失败，且未配置独立的 BACKUP_MODEL")
        backup = get_model(model=backup_name, temperature=0)
        response = await asyncio.wait_for(
            backup.ainvoke(f"请简短回答：{question}"),
            timeout=10,
        )
        return response.content


last_request: dict[str, float] = defaultdict(float)


def allow_request(user_id: str, interval_seconds: float = 1.0) -> bool:
    now = time.monotonic()
    if now - last_request[user_id] < interval_seconds:
        return False
    last_request[user_id] = now
    return True


ALLOWED_TOOLS = {"search_faq", "calculate"}


def authorize_tool(tool_name: str) -> None:
    if tool_name not in ALLOWED_TOOLS:
        raise PermissionError(f"不允许调用工具：{tool_name}")


def redact_common_pii(text: str) -> str:
    # 教学示例：生产环境应使用经过评估的 PII 检测方案。
    import re

    text = re.sub(r"[\w.+-]+@[\w.-]+\.[A-Za-z]{2,}", "[EMAIL]", text)
    return re.sub(r"\b1[3-9]\d{9}\b", "[PHONE]", text)


def cost_estimate(
    input_tokens: int,
    output_tokens: int,
    input_price_per_million: float,
    output_price_per_million: float,
) -> float:
    return (
        input_tokens * input_price_per_million
        + output_tokens * output_price_per_million
    ) / 1_000_000
```

此示例需要 `import os`，并通过 `BACKUP_MODEL` 配置经提供方确认可用且独立的模型；主备相同不能构成有效 Fallback。内存限流只适合单进程练习，多实例需 Redis 等共享存储。使用模型实际返回的 Token Usage 和当前供应商价格表填入成本计算；价格需版本化，不能硬编码成通用常量。Prompt Injection 练习应把检索内容当不可信数据、限制工具权限，并测试恶意文档不能改变授权规则；不要把“在 System Prompt 中写一句忽略注入”当作防护。

## 第15周：用 pytest 验证核心逻辑

安装：`uv add --dev pytest`。保存为 `examples/test_week_15.py`：

```python
def route_question(question: str) -> str:
    if not question.strip():
        raise ValueError("question must not be empty")
    if "账单" in question:
        return "billing"
    return "general"


def test_routes_billing_question():
    assert route_question("我想查看账单") == "billing"


def test_routes_general_question():
    assert route_question("你好") == "general"


def test_rejects_empty_question():
    try:
        route_question(" ")
    except ValueError as error:
        assert str(error) == "question must not be empty"
    else:
        raise AssertionError("Expected ValueError")
```

运行：`uv run pytest -q examples/test_week_15.py`。试着增加模型超时、工具异常和无权限的测试。真实项目中可改用 pytest 的 `pytest.raises`，并把待测函数放入应用模块。

在原有测试外，继续为真实 Graph 写集成测试：给测试构造临时 Checkpointer 和 Mock Tool，覆盖成功、工具抛错、审批拒绝、恢复执行及重复请求。CI 和 Docker 的最小起点如下：

`Dockerfile`：

```dockerfile
FROM python:3.15-slim
WORKDIR /app
COPY pyproject.toml uv.lock ./
RUN pip install --no-cache-dir uv
RUN uv sync --frozen --no-dev
COPY src ./src
COPY examples ./examples
CMD ["uv", "run", "--no-sync", "uvicorn", "examples.week_13:app", "--host", "0.0.0.0", "--port", "8000"]
```

`.github/workflows/ci.yml`：

```yaml
name: tests
on:
  push:
  pull_request:
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: astral-sh/setup-uv@v5
      - run: uv sync --frozen
      - run: uv run pytest -q
```

CI 不应在未安全配置密钥时调用真实模型；可用 Mock 跑稳定测试，再把少量真实模型评测放在单独的受控任务中。上线验收还要检查健康端点、超时告警、错误率、延迟分位数、Token/成本及 Trace 脱敏。

## 第16周：串起一个最小毕业项目 Graph

保存为 `examples/week_16.py`：

```python
from typing import Literal
from typing_extensions import TypedDict
from langgraph.graph import END, START, StateGraph


class State(TypedDict):
    question: str
    evidence: str
    source: str
    answer: str
    status: str


def retrieve(state: State) -> dict:
    # 教学用固定知识；毕业项目中替换为带租户过滤的真实检索器。
    if "年假" in state["question"]:
        return {
            "evidence": "年假需提前 5 个工作日申请。",
            "source": "员工手册 p.8",
        }
    return {"evidence": "", "source": ""}


def answer(state: State) -> dict:
    if not state["evidence"]:
        return {"answer": "现有资料无法确定。", "status": "needs_more_evidence"}
    return {
        "answer": f"{state['evidence']}（来源：{state['source']}）",
        "status": "completed",
    }


def route(state: State) -> Literal["answer"]:
    return "answer"


builder = StateGraph(State)
builder.add_node("retrieve", retrieve)
builder.add_node("answer", answer)
builder.add_edge(START, "retrieve")
builder.add_conditional_edges("retrieve", route)
builder.add_edge("answer", END)
app = builder.compile()


def initial_state(question: str) -> State:
    return {
        "question": question,
        "evidence": "",
        "source": "",
        "answer": "",
        "status": "started",
    }


if __name__ == "__main__":
    print(app.invoke(initial_state("年假要提前多久申请？")))
```

运行：`uv run python examples/week_16.py`。本例可运行，但只实现“固定资料检索—引用回答/无依据拒答”，不是完整的企业级毕业项目。将固定检索替换为第3周 RAG，将 Graph 接入第13周 API，并通过第7周的 Checkpointer 增加人工审批和恢复。

保存为 `tests/test_week_16.py`，测试这段 Graph 的可验证行为：

```python
from examples.week_16 import app, initial_state


def test_knowledge_answer_has_source():
    result = app.invoke(initial_state("年假要提前多久申请？"))
    assert result["status"] == "completed"
    assert "来源：员工手册 p.8" in result["answer"]


def test_unknown_question_is_not_guessed():
    result = app.invoke(initial_state("公司未公开的营收是多少？"))
    assert result["status"] == "needs_more_evidence"
    assert "无法确定" in result["answer"]
```

运行：`uv run pytest -q tests/test_week_16.py`。之后再单独给真实业务工具增加审批恢复、跨租户拒绝、重复请求幂等、故障恢复、评测回归和 Docker 健康检查测试。毕业项目验收表需逐项记录“通过/未通过/证据”，不能把尚未实现的模块算作完成。

------------------------------------------------------------------------

# 六、推荐学习资源

优先使用官方文档和官方教程，并在学习过程中核对当前稳定版本及 API 变更。

1.  **LangChain Documentation**\
    https://docs.langchain.com/oss/python/langchain/overview

2.  **LangGraph Documentation**\
    https://docs.langchain.com/oss/python/langgraph/overview

3.  **LangSmith Documentation**\
    https://docs.langchain.com/langsmith/overview

4.  **LangChain GitHub**\
    https://github.com/langchain-ai/langchain

5.  **LangGraph GitHub**\
    https://github.com/langchain-ai/langgraph

6.  **LangChain Academy**\
    https://academy.langchain.com/

## 开发环境建议

建议使用 Python、uv 和 Git 管理学习项目。

``` bash
mkdir ai-agent-learning
cd ai-agent-learning

uv init
uv venv

uv add langchain langgraph langsmith
uv add langchain-openai
uv add langchain-community
uv add langchain-text-splitters

uv add fastapi uvicorn
uv add pydantic python-dotenv
uv add pytest
```

建议提交 `uv.lock`，保证项目环境可复现。不要一开始安装所有
Integration，根据实际使用的模型、向量数据库和工具逐步安装。

------------------------------------------------------------------------

# 七、学习建议

## 1. 不要只学习 API，要理解 Agent 底层原理

例如学习 LangGraph 时，不要只记住 `add_node()`、`add_edge()` 和
`add_conditional_edges()`，还要理解：

- State 如何变化？
- Node 为什么这样设计？
- Graph 如何决定下一步？
- Agent 如何判断任务是否完成？
- 执行失败后如何恢复？

## 2. 建议时间分配

| 学习活动 | 时间占比 |
|---|---:|
| 理论学习 | 30% |
| 编码实战 | 60% |
| 复盘总结 | 10% |

不要花大量时间看视频、记笔记，却没有真正写代码。

## 3. 从第一周开始使用 GitHub

建议目录结构：

``` text
ai-agent-learning/
├── month-01-langchain/
│   ├── week-01-llm/
│   ├── week-02-core-components/
│   ├── week-03-rag/
│   └── week-04-advanced-rag/
├── month-02-langgraph/
│   ├── week-05-graph-basics/
│   ├── week-06-tools-agent/
│   ├── week-07-memory/
│   └── week-08-workflows/
├── month-03-advanced-agent/
│   ├── week-09-multi-agent/
│   ├── week-10-agentic-rag/
│   ├── week-11-langsmith/
│   └── week-12-evaluation/
└── month-04-production/
    ├── architecture/
    ├── backend/
    ├── frontend/
    ├── tests/
    └── deployment/
```

每周提交代码，并写 README 说明实现思路、技术难点和解决方案。

## 4. 重视 LangSmith Evaluation

传统软件测试验证输入、输出和函数行为；AI Agent 还需要评估：

- 回答是否准确。
- 是否调用正确的工具。
- 是否检索到相关内容。
- 是否遵循业务规则。
- 是否出现幻觉。
- 是否在合理成本和时间内完成任务。

传统软件工程与 Agent 工程的实践对应：

| 传统软件工程 | AI Agent 工程 |
|---|---|
| Unit Testing | Node / Tool Testing |
| Integration Testing | Agent Workflow Testing |
| Regression Testing | LLM Evaluation |
| Code Coverage | Evaluation Dataset Coverage |
| CI Quality Gate | AI Quality Gate |
| Application Logging | LangSmith Tracing |

注意：LLM 评估具有概率性，不能简单把传统代码覆盖率等同于 Agent
质量。应结合规则型评估、模型评审和人工抽样。

## 5. 不要过早陷入 Multi-Agent

-   一个设计良好的 Single Agent，可能比多个 Agent 更高效。
-   Multi-Agent 会增加 Token 成本和执行延迟。
-   Agent 之间的交接可能产生错误。
-   Debugging 难度也会增加。

建议先把 Single Agent + Tools + RAG + Workflow 做扎实，再学习
Multi-Agent。

## 6. 额外学习 MCP

Model Context Protocol（MCP）可以帮助 Agent
通过标准化协议访问外部工具和数据源。建议放在第3个月后半段学习，作为工具集成的扩展，不要挤占
LangGraph 和 LangSmith 的核心练习时间。

------------------------------------------------------------------------

# 八、16周进度检查表

-   [ ] 第1周：LLM 基础和模型调用
-   [ ] 第2周：Prompt、Runnable、Tools、Structured Output
-   [ ] 第3周：Embedding、Vector DB、基础 RAG
-   [ ] 第4周：Advanced RAG 和知识库项目
-   [ ] 第5周：Graph、State、Node、Edge
-   [ ] 第6周：Tool Calling Agent
-   [ ] 第7周：Memory、Persistence、Human-in-the-loop
-   [ ] 第8周：复杂 Workflow 和个人 AI 助手
-   [ ] 第9周：Multi-Agent Architecture
-   [ ] 第10周：Agentic RAG
-   [ ] 第11周：LangSmith Tracing
-   [ ] 第12周：Evaluation 和软件开发助手
-   [ ] 第13周：FastAPI、数据库和系统架构
-   [ ] 第14周：安全、可靠性和成本优化
-   [ ] 第15周：测试、Docker、CI/CD 和监控
-   [ ] 第16周：毕业项目完整交付

------------------------------------------------------------------------

# 九、4个月后的能力目标

完成课程后，应该能够独立完成：

1.  需求分析与技术选型。
2.  Agent Architecture Design。
3.  LangChain / LangGraph 开发。
4.  RAG 系统设计。
5.  Tool Integration。
6.  LangSmith Evaluation。
7.  Backend API 开发。
8.  测试和部署。
9.  生产环境监控与优化。

**最终目标：** 不只是掌握三个框架的 API，而是具备独立交付企业级 AI Agent
应用的工程能力。

建议采用陪练式学习：每周完成理论学习、代码实战、课后练习和代码
Review；每个月完成一个阶段项目，并记录架构决策、测试结果和改进过程。
