admin-service客户端接口与各服务实际实现的不匹配问题分析
通过检查各模块的代码，我发现了admin-service中多个客户端接口与实际服务实现之间存在的不匹配问题，这些问题可能是导致用户列表和文章列表API返回500错误的主要原因。

1. UserServiceClient (admin-service) 与 user-service 实现不匹配
问题: admin-service中的UserServiceClient定义了getUserList方法，但在user-service中找不到对应的实现。

admin-service中的定义: UserServiceClient.getUserList 定义了3个参数(page, pageSize, keyword)
user-service中的实际实现: UserController主要提供注册、登录、生词本等功能，没有提供管理用户列表的API。AdminUserController只提供管理员用户的管理功能。
2. ArticleServiceClient (admin-service) 与 article-service 实现不匹配
问题: ArticleServiceClient定义的getArticleList方法与article-service中的实际实现不匹配。

admin-service中的定义: ArticleServiceClient.getArticleList 定义了5个参数(page, pageSize, keyword, category, difficulty)
article-service中的实际实现: ArticleController提供的是exploreArticles方法，该方法接收ArticleQueryDTO参数，而非单独的参数。
3. SubscriptionServiceClient (admin-service) 与 user-service 实现不匹配
问题: SubscriptionServiceClient定义的接口与user-service中的SubscriptionController实现完全不匹配。

admin-service中的定义: 包含获取/创建/更新/删除订阅计划、获取用户订阅列表等功能，路径为/api/subscription/plans和/api/subscription/user
user-service中的实际实现: SubscriptionController提供的是用户订阅管理功能，如创建订阅、获取当前订阅、取消订阅等，路径为/api/subscription/create、/api/subscription/current/{userId}等。
4. AIServiceClient (admin-service) 与 ai-service 实现不匹配
问题: AIServiceClient定义的接口与ai-service中的UnifiedAiAnalysisController实现不匹配。

admin-service中的定义: 包含获取AI分析列表、详情、测验题目等功能，路径为/api/ai/analysis/list、/api/ai/quiz/{articleId}等
ai-service中的实际实现: UnifiedAiAnalysisController提供的是智能分析(/api/ai/smart/analyze)和学习助手(/api/ai/smart/assistant)功能，没有与AIServiceClient匹配的接口。
修复建议
1.
对于UserServiceClient:

修改AdminUserServiceImpl中的逻辑，不再调用不存在的接口
2.
对于ArticleServiceClient:

修改ArticleServiceClient中的getArticleList方法，使其与article-service中的exploreArticles方法参数匹配
或者在ArticleServiceImpl中添加参数转换逻辑，将单独参数转换为ArticleQueryDTO
3.
对于SubscriptionServiceClient和AIServiceClient:

重新设计这两个客户端接口，使其与实际服务实现匹配
或者在对应的服务中实现符合客户端定义的接口
4.
统一错误处理:

继续在AdminUserController和ArticleController中实现类似DashboardController的降级处理逻辑，确保即使服务调用失败也能返回默认数据
这些不匹配问题需要系统地修复，建议按照业务优先级逐个解决，以确保admin-service能够正确调用其他服务的API接口。