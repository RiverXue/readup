// 测试HomeView.vue中未登录状态下的快捷入口数量
const fs = require('fs');
const path = require('path');

// 读取HomeView.vue文件
const homeViewPath = path.join(__dirname, 'xreadup-frontend', 'src', 'views', 'HomeView.vue');
const content = fs.readFileSync(homeViewPath, 'utf8');

// 查找未登录状态下的快捷入口部分
const unloggedActionsMatch = content.match(/\/\/ 如果未登录，显示基础引导[\s\S]*?return actions/);
if (unloggedActionsMatch) {
  const unloggedActions = unloggedActionsMatch[0];
  
  // 计算actions.push的数量
  const pushMatches = unloggedActions.match(/actions\.push\(/g);
  const pushCount = pushMatches ? pushMatches.length : 0;
  
  console.log('未登录状态下的快捷入口数量:', pushCount);
  
  // 提取所有标题
  const titleMatches = unloggedActions.match(/title:\s*'([^']+)'/g);
  if (titleMatches) {
    console.log('快捷入口标题:');
    titleMatches.forEach((match, index) => {
      const title = match.match(/'([^']+)'/)[1];
      console.log(`${index + 1}. ${title}`);
    });
  }
  
  if (pushCount === 4) {
    console.log('✅ 成功添加了第四个快捷入口！');
  } else {
    console.log('❌ 快捷入口数量不正确，期望4个，实际', pushCount, '个');
  }
} else {
  console.log('❌ 未找到未登录状态下的快捷入口代码');
}
