# 📝 支付对话框升级差价显示恢复

## 🎯 更新目标
在支付方式选择对话框中重新添加升级差价显示，确保用户在两个地方都能看到升级差价：
1. **订阅状态区域**的升级对话框 - 已有升级差价
2. **支付方式选择**的支付对话框 - 恢复升级差价显示

## ✅ 更新内容

### 1. **支付对话框HTML结构恢复**
**文件**: `xreadup-frontend/src/views/SubscriptionPage.vue`

**更新前**:
```vue
<div class="selected-plan">
  <h3>{{ selectedPlan?.name }}</h3>
  <p class="plan-price">¥{{ selectedPlan ? getPlanPrice(selectedPlan.type) : 0 }}/{{ selectedPlan?.duration }}</p>
</div>
```

**更新后**:
```vue
<div class="selected-plan">
  <h3>{{ selectedPlan?.name }}</h3>
  <div class="plan-price-section">
    <p class="plan-price">¥{{ selectedPlan ? getPlanPrice(selectedPlan.type) : 0 }}/{{ selectedPlan?.duration }}</p>
    <!-- 升级差价显示 -->
    <div v-if="shouldShowUpgradePrice(selectedPlan?.type)" class="upgrade-price-info">
      <span class="upgrade-price-label">升级差价：</span>
      <span class="upgrade-price-amount">¥{{ getUpgradePrice(selectedPlan?.type) }}</span>
    </div>
  </div>
</div>
```

### 2. **辅助方法恢复**
**重新添加了被删除的方法**:

```javascript
// 判断是否应该显示升级差价
const shouldShowUpgradePrice = (planType: string) => {
  // 如果是当前套餐，不显示升级差价
  if (isCurrentPlan(planType)) {
    return false
  }
  
  // 如果是免费用户，不显示升级差价（显示原价）
  if (!currentSubscription.value || currentSubscription.value.planType === 'FREE') {
    return false
  }
  
  // 如果有升级差价数据，显示升级差价
  return upgradePrices.value[planType] !== undefined
}

// 获取升级差价
const getUpgradePrice = (planType: string) => {
  if (upgradePrices.value[planType]) {
    return upgradePrices.value[planType].upgradePrice
  }
  return 0
}
```

### 3. **新增CSS样式**
**为支付对话框中的升级差价添加专门样式**:

```css
/* 支付对话框中的升级差价样式 */
.plan-price-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.upgrade-price-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.upgrade-price-label {
  color: var(--text-secondary);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
}

.upgrade-price-amount {
  color: var(--ios-blue);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-semibold);
}
```

## 🎯 显示效果

### **支付对话框中的升级差价显示**:
```
┌─────────────────────────────────┐
│ 选择支付方式                    │
├─────────────────────────────────┤
│ 专业会员                        │
│ ¥29/月                          │
│ ┌─────────────────────────────┐ │
│ │ 升级差价：¥15               │ │
│ └─────────────────────────────┘ │
│                                 │
│ 💳 银行卡支付                   │
│ 💚 微信支付                     │
│ 💙 支付宝支付                   │
│                                 │
│ [确认支付] [取消]               │
└─────────────────────────────────┘
```

### **升级对话框中的升级差价显示**:
```
┌─────────────────────────────────┐
│ 升级套餐                        │
├─────────────────────────────────┤
│ 专业会员                        │
│ ¥29/月                          │
│ 升级差价: ¥15/月 (剩余15天)     │
│                                 │
│ [确认升级] [取消]               │
└─────────────────────────────────┘
```

## 📋 功能特点

### **智能显示逻辑**:
- ✅ **当前套餐**: 不显示升级差价
- ✅ **免费用户**: 不显示升级差价（显示原价）
- ✅ **付费用户升级**: 显示升级差价
- ✅ **数据验证**: 只有存在升级差价数据时才显示

### **视觉设计**:
- ✅ **统一风格**: 与页面整体设计保持一致
- ✅ **清晰标识**: 使用"升级差价："标签
- ✅ **突出显示**: 使用蓝色突出价格金额
- ✅ **背景区分**: 使用浅色背景区分升级差价信息

## 🎉 更新完成

现在用户在两个地方都能看到升级差价信息：
1. **订阅状态区域**的升级对话框
2. **支付方式选择**的支付对话框

这确保了用户在整个升级流程中都能清楚地了解需要支付的升级差价！🎉
