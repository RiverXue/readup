# 🔧 试用功能Price字段修复

## 🐛 问题描述
```
java.sql.SQLException: Field 'price' doesn't have a default value
```

## 🔍 问题原因
在创建试用订阅时，没有设置`price`字段，但数据库中的`price`字段是`NOT NULL`且没有默认值。

## ✅ 修复方案
在`SubscriptionServiceImpl.startTrial()`方法中，为试用订阅设置所有必需的字段：

### 修改内容：
```java
// 创建试用订阅（等同于PRO套餐）
Subscription trialSubscription = new Subscription();
trialSubscription.setUserId(userId);
trialSubscription.setPlanType("TRIAL");
trialSubscription.setPrice(BigDecimal.ZERO); // 试用免费
trialSubscription.setCurrency("CNY"); // 设置货币
trialSubscription.setStatus("ACTIVE");
trialSubscription.setStartDate(LocalDateTime.now());
trialSubscription.setEndDate(LocalDateTime.now().plusDays(7)); // 7天试用
trialSubscription.setPaymentMethod("TRIAL"); // 试用方式
trialSubscription.setAutoRenew(false); // 试用不自动续费
trialSubscription.setIsTrial(true);

// 设置PRO套餐的权限
trialSubscription.setMaxArticlesPerMonth(300);
trialSubscription.setMaxWordsPerArticle(5000);
trialSubscription.setAiFeaturesEnabled(true);
trialSubscription.setPrioritySupport(false);
```

## 📋 添加的字段：
- ✅ `price`: `BigDecimal.ZERO` (试用免费)
- ✅ `currency`: `"CNY"` (人民币)
- ✅ `paymentMethod`: `"TRIAL"` (试用方式)
- ✅ `autoRenew`: `false` (试用不自动续费)

## 🧪 验证结果
- ✅ 编译成功
- ✅ 无语法错误
- ✅ 所有必需字段都已设置

## 🚀 现在可以测试
试用功能现在应该可以正常工作了！请重新测试：

1. **重启后端服务**（如果正在运行）
2. **点击"立即试用"按钮**
3. **查看是否成功创建试用订阅**

现在应该不会再出现`Field 'price' doesn't have a default value`错误了！
