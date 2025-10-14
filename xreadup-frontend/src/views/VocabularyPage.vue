<template>
  <div class="vocabulary-container">
    <h2>我的生词本</h2>

    <!-- 统计信息和复习入口 -->
    <div class="stats">
      <div class="stats-wrapper">
        <!-- 左侧：文字信息和系统性学习功能 -->
        <div class="stats-content">
          <div class="stat-overview">
            <div class="stat-item">
              <h3>生词本词汇量</h3>
              <p>{{ stats.totalWords }}</p>
            </div>
            <div class="stat-item">
              <h3>今日新增</h3>
              <p>{{ stats.todayWords }}</p>
            </div>
            <div class="stat-item">
              <h3>待复习</h3>
              <p>{{ stats.reviewWords }}</p>
            </div>
          </div>

          <div class="learning-modes-group">
            <h4 class="learning-mode-title">系统性学习</h4>
            <div class="review-button-container">
              <TactileButton
                @click="startTodayReview"
                variant="primary"
                size="lg"
                :loading="isReviewLoading"
                :disabled="!userStore.isLoggedIn"
                class="learning-mode-button"
              >
                闪卡式批量复习
              </TactileButton>
              <TactileButton
                @click="startWordSpeedReview"
                variant="success"
                size="lg"
                :loading="isSpeedReviewLoading"
                :disabled="!userStore.isLoggedIn || speedReviewWordsCount === 0"
                class="learning-mode-button"
              >
                <template #icon>
                  <el-icon><Collection /></el-icon>
                </template>
                单词速刷
              </TactileButton>
              <TactileButton
                @click="startBatchDictation"
                variant="secondary"
                size="lg"
                :loading="isDictationLoading"
                :disabled="!userStore.isLoggedIn"
                class="learning-mode-button"
              >
                批量听写
              </TactileButton>
            </div>
            <div class="learning-guide">
              <p class="guide-text">根据艾宾浩斯记忆曲线，科学安排学习进度，提高记忆效率。</p>
              <TactileButton
                @click="showLearningModesGuide"
                variant="ghost"
                size="sm"
                class="guide-button"
              >
                <template #icon>
                <el-icon><help-filled /></el-icon>
                </template>
                了解学习模式
              </TactileButton>
            </div>
          </div>
        </div>

        <!-- 右侧：图表 -->
        <div class="chart-container">
          <div ref="statusChart" class="status-chart"></div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filters">
      <el-input
        v-model="searchQuery"
        placeholder="搜索单词..."
        prefix-icon="Search"
        style="width: 300px"
      />
      <el-select v-model="statusFilter" placeholder="状态筛选">
        <el-option label="全部" value="" />
        <el-option label="未复习" value="unreviewed" />
        <el-option label="已掌握" value="mastered" />
        <el-option label="复习中" value="reviewing" />
      </el-select>
    </div>



    <!-- 网格视图 -->
    <div v-if="viewMode === 'grid'" class="word-cards">
      <el-card
        v-for="word in paginatedWords"
        :key="word.id"
        class="word-card"
        :body-style="{ padding: '16px' }"
        :data-status="word.reviewStatus"
      >
        <!-- 状态指示区域 - 为所有状态提供直观的视觉提示 -->
        <div class="word-status">
          <!-- 复习中状态：显示状态指示器和进度条 -->
          <div class="review-progress" v-if="word.reviewStatus === 'reviewing' && !word.noLongerReview">
            <!-- 新的进度条实现 - 与对勾对齐 -->
            <div class="aligned-progress-container">
              <!-- 状态指示器和进度条标题 -->
              <div class="stat-header" style="display: flex; justify-content: space-between; margin-bottom: 8px; font-weight: bold; font-size: 12px; color: #606266;">
                <div style="display: flex; align-items: center;">
                  <div class="status-indicator reviewing" style="margin-right: 8px; margin-bottom: 0;">
                    <span class="status-icon">•</span>
                    <span>复习中</span>
                  </div>
                </div>
                <span>{{ word.nextReviewTime ? formatNextReviewTime(word.nextReviewTime) : '-' }}</span>
              </div>
              <!-- 新的进度条 -->
              <div class="new-progress-bar">
                <div
                  class="progress-fill"
                  :style="{
                    width: `calc(${(function() {
                      try {
                        if (word.nextReviewTime) {
                          const progress = calculateReviewProgress(word.nextReviewTime)
                          return isNaN(progress) ? 50 : Math.max(0, Math.min(100, progress))
                        }
                        return 50
                      } catch (e) {
                        console.log(`单词ID:${word.id}进度条计算错误:`, e)
                        return 50
                      }
                    })()}% - 16px`
                  }"
                ></div>
                <!-- 根据单词是否逾期显示不同的标记 -->
                <div
                  class="progress-checkmark"
                  :class="{ 'progress-overdue': word.nextReviewTime && formatNextReviewTime(word.nextReviewTime).includes('逾期') }"
                >
                  {{ word.nextReviewTime && formatNextReviewTime(word.nextReviewTime).includes('逾期') ? '!' : '✓' }}
                </div>
              </div>
            </div>
          </div>
          <!-- 复习中但已不再巩固状态：仅显示状态指示器 -->
          <div class="status-indicator reviewing" v-else-if="word.reviewStatus === 'reviewing' && word.noLongerReview">
            <span class="status-icon">•</span>
            <span>复习中</span>
          </div>

          <!-- 未复习状态：显示状态指示器 -->
          <div class="status-indicator unreviewed" v-else-if="word.reviewStatus === 'unreviewed'">
            <span class="status-icon">•</span>
            <span>未复习</span>
          </div>
          <!-- 已掌握状态：显示状态指示器和进度条 -->
          <div v-else-if="word.reviewStatus === 'mastered' && !word.noLongerReview" class="review-progress">
            <div class="aligned-progress-container">
              <!-- 状态指示器和进度条标题 -->
              <div class="stat-header" style="display: flex; justify-content: space-between; margin-bottom: 8px; font-weight: bold; font-size: 12px; color: #606266;">
                <div style="display: flex; align-items: center;">
                  <div class="status-indicator mastered" style="margin-right: 8px; margin-bottom: 0;">
                    <span class="status-icon">✓</span>
                    <span>已掌握·巩固中</span>
                  </div>
                </div>
                <span>{{ word.nextReviewTime ? formatNextReviewTime(word.nextReviewTime).replace('复习', '巩固') : '-' }}</span>
              </div>
              <!-- 进度条 -->
              <div class="new-progress-bar">
                <div
                  class="progress-fill"
                  :style="{
                    width: `calc(${(function() {
                      try {
                        if (word.nextReviewTime) {
                          const progress = calculateReviewProgress(word.nextReviewTime)
                          return isNaN(progress) ? 50 : Math.max(0, Math.min(100, progress))
                        }
                        return 50
                      } catch (e) {
                        console.log(`单词ID:${word.id}进度条计算错误:`, e)
                        return 50
                      }
                    })()}% - 16px`
                  }"
                ></div>
                <!-- 根据单词是否逾期显示不同的标记 -->
                <div
                  class="progress-checkmark"
                  :class="{ 'progress-overdue': word.nextReviewTime && formatNextReviewTime(word.nextReviewTime).includes('逾期') }"
                >
                  {{ word.nextReviewTime && formatNextReviewTime(word.nextReviewTime).includes('逾期') ? '!' : '✓' }}
                </div>
              </div>
            </div>
          </div>
          <!-- 已掌握且已不再巩固状态：显示融合状态指示器 -->
          <div v-else-if="word.reviewStatus === 'mastered' && word.noLongerReview">
            <div class="status-indicator mastered-no-review">
              <span class="status-icon">✓</span>
              <span>已掌握·不再巩固</span>
            </div>
          </div>
        </div>

        <!-- 单词内容 -->
        <div class="word-content">
          <div style="display: flex; align-items: center; gap: 10px;">
            <h3 class="word-text">{{ word.word }}</h3>
            <el-button
              @click="handleSpeakWord(word.word)"
              size="default"
              type="text"
              title="发音"
              class="pronunciation-button"
            >
              🔊
            </el-button>
          </div>
          <p class="word-phonetic" v-if="word.phonetic">{{ word.phonetic }}</p>
          <p class="word-meaning">{{ word.meaning }}</p>
          <p class="word-example" v-if="word.example">
            <strong>例句：</strong>{{ word.example }}
          </p>
          <p class="word-date" v-if="word.createdAt">添加时间：{{ formatCreatedTime(word.createdAt) }}</p>
        </div>

        <!-- 操作按钮 -->
        <div class="word-actions">
          <TactileButton
            v-if="word.reviewStatus === 'mastered' && !word.noLongerReview"
            @click="setWordAsNoLongerReview(word)"
            variant="warning"
            size="sm"
          >
            不巩固
          </TactileButton>
        </div>
        
        <!-- 删除按钮 - 右下角垃圾桶图标 -->
        <div class="word-delete-btn">
          <el-button
            @click="deleteWord(word)"
            type="danger"
            :icon="Delete"
            circle
            size="small"
            class="delete-icon-btn"
          />
        </div>
      </el-card>
    </div>

    <!-- 叠层视图 -->
    <div v-if="viewMode === 'stack'" class="word-stack-container">
      
      <!-- 左侧导航按钮 -->
      <div class="stack-nav-left">
        <div v-if="isSpeedReviewMode" class="speed-nav-buttons">
          <TactileButton 
            @click="previousSpeedReviewCard" 
            :disabled="currentSpeedReviewIndex === 0"
            variant="secondary" 
            size="sm"
            class="speed-nav-btn"
          >
            ← 上一张
          </TactileButton>
        </div>
        <el-button 
          v-else
          @click="previousStackCard" 
          :disabled="currentStackIndex === 0"
          class="stack-nav-btn"
          circle
        >
          <el-icon><ArrowLeft /></el-icon>
          </el-button>
      </div>
      
      <!-- 单词堆叠区域 -->
      <div class="word-stack">
        <div 
          v-for="(word, index) in visibleStackWords" 
          :key="word.id"
          class="word-card-stack"
          :style="getStackCardStyle(index)"
          @click="handleStackCardClick(index)"
        >
          <el-card
            class="word-card"
            :body-style="{ padding: '16px' }"
            :data-status="word.reviewStatus"
          >
            <!-- 状态指示区域 -->
            <div class="word-status">
              <!-- 复习中状态：显示状态指示器和进度条 -->
              <div class="review-progress" v-if="word.reviewStatus === 'reviewing' && !word.noLongerReview">
                <div class="aligned-progress-container">
                  <div class="stat-header" style="display: flex; justify-content: space-between; margin-bottom: 8px; font-weight: bold; font-size: 12px; color: #606266;">
                    <div style="display: flex; align-items: center;">
                      <div class="status-indicator reviewing" style="margin-right: 8px; margin-bottom: 0;">
                        <span class="status-icon">•</span>
                        <span>复习中</span>
                      </div>
                    </div>
                    <span>{{ word.nextReviewTime ? formatNextReviewTime(word.nextReviewTime) : '-' }}</span>
                  </div>
                  <div class="new-progress-bar">
                    <div
                      class="progress-fill"
                      :style="{
                        width: `calc(${(function() {
                          try {
                            if (word.nextReviewTime) {
                              const progress = calculateReviewProgress(word.nextReviewTime)
                              return isNaN(progress) ? 50 : Math.max(0, Math.min(100, progress))
                            }
                            return 50
                          } catch (e) {
                            console.log(`单词ID:${word.id}进度条计算错误:`, e)
                            return 50
                          }
                        })()}%)`
                      }"
                    ></div>
                    <div class="progress-checkmark">
                      {{ word.nextReviewTime && formatNextReviewTime(word.nextReviewTime).includes('逾期') ? '!' : '✓' }}
                    </div>
                  </div>
                </div>
              </div>
              <!-- 复习中但已不再巩固状态：仅显示状态指示器 -->
              <div class="status-indicator reviewing" v-else-if="word.reviewStatus === 'reviewing' && word.noLongerReview">
                <span class="status-icon">•</span>
                <span>复习中</span>
              </div>
              <!-- 未复习状态：显示状态指示器 -->
              <div class="status-indicator unreviewed" v-else-if="word.reviewStatus === 'unreviewed'">
                <span class="status-icon">•</span>
                <span>未复习</span>
              </div>
              <!-- 已掌握状态：显示状态指示器和进度条 -->
              <div v-else-if="word.reviewStatus === 'mastered' && !word.noLongerReview" class="review-progress">
                <div class="aligned-progress-container">
                  <div class="stat-header" style="display: flex; justify-content: space-between; margin-bottom: 8px; font-weight: bold; font-size: 12px; color: #606266;">
                    <div style="display: flex; align-items: center;">
                      <div class="status-indicator mastered" style="margin-right: 8px; margin-bottom: 0;">
                        <span class="status-icon">✓</span>
                        <span>已掌握·巩固中</span>
                      </div>
                    </div>
                    <span>{{ word.nextReviewTime ? formatNextReviewTime(word.nextReviewTime).replace('复习', '巩固') : '-' }}</span>
                  </div>
                  <div class="new-progress-bar">
                    <div
                      class="progress-fill"
                      :style="{
                        width: `calc(${(function() {
                          try {
                            if (word.nextReviewTime) {
                              const progress = calculateReviewProgress(word.nextReviewTime)
                              return isNaN(progress) ? 50 : Math.max(0, Math.min(100, progress))
                            }
                            return 50
                          } catch (e) {
                            console.log(`单词ID:${word.id}进度条计算错误:`, e)
                            return 50
                          }
                        })()}%)`
                      }"
                    ></div>
                    <div class="progress-checkmark">
                      {{ word.nextReviewTime && formatNextReviewTime(word.nextReviewTime).includes('逾期') ? '!' : '✓' }}
                    </div>
                  </div>
                </div>
              </div>
              <!-- 已掌握且已不再巩固状态：显示融合状态指示器 -->
              <div v-else-if="word.reviewStatus === 'mastered' && word.noLongerReview">
                <div class="status-indicator mastered-no-review">
                  <span class="status-icon">✓</span>
                  <span>已掌握·不再巩固</span>
                </div>
              </div>
            </div>

            <!-- 单词内容 -->
            <div class="word-content">
              <div style="display: flex; align-items: center; gap: 10px;">
                <h3 class="word-text">{{ word.word }}</h3>
          <el-button
                  @click="handleSpeakWord(word.word)"
                  size="default"
                  type="text"
                  title="发音"
                  class="pronunciation-button"
                >
                  🔊
          </el-button>
              </div>
              <p class="word-phonetic" v-if="word.phonetic">{{ word.phonetic }}</p>
              <p class="word-meaning">{{ word.meaning }}</p>
              <p class="word-example" v-if="word.example">
                <strong>例句：</strong>{{ word.example }}
              </p>
              <p class="word-date" v-if="word.createdAt">添加时间：{{ formatCreatedTime(word.createdAt) }}</p>
            </div>

            <!-- 操作按钮 -->
            <div class="word-actions">
              <TactileButton
            v-if="word.reviewStatus === 'mastered' && !word.noLongerReview"
            @click="setWordAsNoLongerReview(word)"
                variant="warning"
                size="sm"
              >
                不巩固
              </TactileButton>
            </div>
            
            <!-- 删除按钮 - 右下角垃圾桶图标 -->
            <div class="word-delete-btn">
              <el-button
                @click="deleteWord(word)"
                type="danger"
                :icon="Delete"
                circle
                size="small"
                class="delete-icon-btn"
              />
            </div>
      </el-card>
        </div>
    </div>

      <!-- 右侧导航按钮 -->
      <div class="stack-nav-right">
        <div v-if="isSpeedReviewMode" class="speed-nav-buttons">
          <TactileButton 
            @click="markSpeedReviewAsMastered" 
            variant="success" 
            size="sm"
            class="speed-nav-btn"
          >
            ✓ 已掌握
          </TactileButton>
          <TactileButton 
            @click="skipSpeedReviewWord" 
            variant="warning" 
            size="sm"
            class="speed-nav-btn"
          >
            ⏭ 跳过
          </TactileButton>
        </div>
        <el-button 
          v-else
          @click="nextStackCard" 
          :disabled="currentStackIndex >= filteredWords.length - 1"
          class="stack-nav-btn"
          circle
        >
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      
      <!-- 底部进度显示 -->
      <div class="stack-progress-bottom">
        <span class="stack-progress">{{ currentStackIndex + 1 }} / {{ filteredWords.length }}</span>
      </div>
    </div>

    <!-- 分页 - 只在网格视图显示 -->
    <div v-if="viewMode === 'grid'" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="filteredWords.length"
        layout="prev, pager, next"
        background
      />
    </div>

    <!-- 复习模式模态框 -->
    <el-dialog
      v-model="isReviewMode"
      title="今日复习"
      width="800px"
      top="20px"
      :show-close="false"
      :close-on-click-modal="false"
    >
      <div v-if="reviewWords.length > 0 && currentReviewWord" class="review-mode">
        <div class="review-progress-total">
          <span>复习进度：{{ currentReviewIndex + 1 }} / {{ reviewWords.length }}</span>
          <el-progress
            :percentage="reviewWords.length > 0 ? Math.round((currentReviewIndex / reviewWords.length) * 100) : 0"
            :stroke-width="8"
            show-text
          ></el-progress>
        </div>

        <div v-if="currentReviewWord" class="review-card">
          <!-- 艾宾浩斯进度条 -->
          <div v-if="currentReviewWord.nextReviewTime" class="word-review-progress">
            <el-progress
              :percentage="currentReviewWord && currentReviewWord.nextReviewTime ? calculateReviewProgress(currentReviewWord.nextReviewTime) : 0"
              :status="'success'"
              :stroke-width="4"
              :show-text="false"
              class="mini-progress"
            ></el-progress>
          </div>

          <div style="display: flex; align-items: center; justify-content: center; gap: 15px;">
            <h2 class="review-word">{{ currentReviewWord.word }}</h2>
            <el-button
              @click="handleSpeakWord(currentReviewWord.word)"
              size="large"
              type="text"
              title="发音"
              class="pronunciation-button"
              style="margin-top: 10px;"
            >
              🔊
            </el-button>
          </div>
          <p class="review-phonetic" v-if="currentReviewWord.pronunciation">
            {{ currentReviewWord.pronunciation }}
          </p>
          <div class="review-actions">
            <div v-if="!showDefinition">
              <TactileButton
                @click="showWordDefinition"
                variant="primary"
                size="lg"
                style="margin-bottom: 20px"
              >
                查看释义
              </TactileButton>
            </div>

            <div v-if="showDefinition" class="review-definition">
              <p>{{ currentReviewWord.definition }}</p>
              <p v-if="currentReviewWord.partOfSpeech"><strong>词性：</strong>{{ currentReviewWord.partOfSpeech }}</p>
              <div class="review-result-buttons">
                <TactileButton
                  @click="recordReviewResult(true)"
                  variant="success"
                  size="lg"
                  style="margin-right: 10px"
                >
                  <template #icon>
                    <el-icon><Check /></el-icon>
                  </template>
                  记住了
                </TactileButton>
                <TactileButton
                  @click="recordReviewResult(false)"
                  variant="danger"
                  size="lg"
                  style="margin-left: 10px"
                >
                  <template #icon>
                    <el-icon><Close /></el-icon>
                  </template>
                  没记住
                </TactileButton>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="review-loading">
          <el-loading v-model="isReviewLoading" text="加载复习内容中..." />
        </div>

        <div class="review-navigation">
          <TactileButton
            @click="exitReviewMode"
            variant="ghost"
            size="lg"
          >
            退出复习
          </TactileButton>
        </div>
      </div>

      <div v-else class="no-review-words">
        <div class="empty-state">
          <el-empty description="今日没有需要复习的单词" />
          <TactileButton @click="exitReviewMode" variant="primary" size="md" style="margin-top: 20px">
            返回生词本
          </TactileButton>
        </div>
      </div>
    </el-dialog>

    <!-- 听写模态框 -->
    <DictationModal
      ref="dictationModal"
      v-model:visible="showDictationModal"
      :word="currentDictationWord || undefined"
      @close="closeDictationModal"
      @finish="handleDictationModalFinish"
    />

    <!-- 批量听写模式模态框 -->
    <el-dialog
      v-model="isDictationMode"
      title="批量听写"
      width="800px"
      top="20px"
      :show-close="false"
      :close-on-click-modal="false"
    >
      <div v-if="batchDictationWords.length > 0" class="review-mode">
        <div class="review-progress-total">
          <span>听写进度：{{ currentDictationIndex + 1 }} / {{ batchDictationWords.length }}</span>
          <el-progress
            :percentage="batchDictationWords.length > 0 ? Math.round((currentDictationIndex / batchDictationWords.length) * 100) : 0"
            :stroke-width="8"
            show-text
          ></el-progress>
        </div>

        <div v-if="currentDictationIndex < batchDictationWords.length" class="dictation-card">
          <!-- 单词发音区 -->
          <div style="display: flex; align-items: center; justify-content: center; gap: 15px; margin-bottom: 20px;">
            <h2 class="review-word">
              {{ showDictationAnswer ? batchDictationWords[currentDictationIndex].word : '听发音并拼写' }}
            </h2>
            <el-button
              @click="handleSpeakWord(batchDictationWords[currentDictationIndex].word)"
              size="large"
              type="primary"
              title="发音"
              class="pronunciation-button"
              :loading="isDictationSpeaking"
            >
              🔊 听发音
            </el-button>
          </div>

          <!-- 用户输入区 -->
          <div v-if="!showDictationAnswer" class="dictation-input-section" style="margin: 30px 0;">
            <el-input
              v-model="dictationUserInput"
              placeholder="请输入单词拼写..."
              @keyup.enter="checkDictationAnswer"
              :maxlength="30"
              clearable
              ref="dictationInputRef"
              style="width: 300px; margin: 0 auto;"
              :disabled="isDictationChecking"
            >
              <template #append>
                <el-button @click="showDictationHint" type="text" size="small" :disabled="isDictationChecking || dictationShowHint">
                  提示
                </el-button>
              </template>
            </el-input>
          </div>

          <!-- 单词提示 -->
          <div v-if="dictationShowHint && !showDictationAnswer" class="dictation-hint" style="margin: 15px 0;">
            <p class="hint-text">
              {{ batchDictationWords[currentDictationIndex].word.substring(0, dictationHintLength) }}{{ '*'.repeat(batchDictationWords[currentDictationIndex].word.length - dictationHintLength) }}
            </p>
          </div>

          <!-- 答案显示和控制按钮 -->
          <div class="review-actions">
            <div v-if="!showDictationAnswer">
              <el-button
                @click="skipDictationWord"
                size="large"
                :disabled="isDictationChecking"
                style="margin-right: 10px;"
              >
                跳过
              </el-button>
              <el-button
                type="primary"
                @click="checkDictationAnswer"
                size="large"
                :loading="isDictationChecking"
                :disabled="!dictationUserInput.trim() || isDictationChecking"
                style="margin-left: 10px;"
              >
                提交
              </el-button>
            </div>

            <div v-if="showDictationAnswer" class="review-definition">
              <!-- 根据dictationFeedback显示结果信息 -->
              <p v-if="dictationFeedback?.type === 'success'" class="success-result" style="color: #67c23a; font-weight: bold;">
                拼写正确！
              </p>
              <p v-else-if="dictationFeedback?.type === 'error'" class="error-result" style="color: #f56c6c; font-weight: bold;">
                拼写错误
              </p>

              <p><strong>正确拼写：</strong>{{ batchDictationWords[currentDictationIndex].word }}</p>
              <p><strong>释义：</strong>{{ batchDictationWords[currentDictationIndex].meaning }}</p>

              <!-- 下一个按钮 - 答案显示时显示 -->
              <div style="margin-top: 20px; text-align: center;">
                <el-button
                  type="primary"
                  @click="() => recordDictationResult(dictationFeedback?.type === 'success')"
                  size="large"
                >
                  下一个
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="review-loading">
          <el-loading v-model="isDictationLoading" text="加载听写内容中..." />
        </div>

        <div class="review-navigation">
          <TactileButton
            @click="exitDictationMode"
            variant="ghost"
            size="lg"
          >
            退出听写
          </TactileButton>
        </div>
      </div>

      <div v-else class="no-review-words">
        <div class="empty-state">
          <el-empty description="今日没有需要听写的单词" />
          <TactileButton @click="exitDictationMode" variant="primary" size="md" style="margin-top: 20px">
            返回生词本
          </TactileButton>
        </div>
      </div>
    </el-dialog>


  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElLoading, ElDialog, ElInput } from 'element-plus'
import { Grid, Collection, ArrowLeft, ArrowRight, Delete } from '@element-plus/icons-vue'
import { vocabularyApi, learningApi, reportApi } from '@/utils/api'
import { useUserStore } from '@/stores/user'
import type { ReviewWordDto } from '@/types/report'
import type { WordItem } from '@/types/word'
import * as echarts from 'echarts'
import { tts } from '@/utils/tts'
import TactileButton from '@/components/common/TactileButton.vue'

const userStore = useUserStore()
const words = ref<WordItem[]>([])
const searchQuery = ref('')
const statusFilter = ref('')

// 视图模式
const viewMode = ref<'grid' | 'stack'>('grid')
const currentStackIndex = ref(0)
const stackSize = 4 // 叠层显示数量
const currentPage = ref(1)
const pageSize = ref(9)
const totalWords = ref(0)
const loading = ref(false)
const isReviewing = ref(false)
const reviewingWordId = ref<number | null>(null)
const stats = ref({
  totalWords: 0,
  todayWords: 0,
  reviewWords: 0,
})

// 复习模式相关状态
const isReviewMode = ref(false)
const reviewWords = ref<ReviewWordDto[]>([])
const currentReviewIndex = ref(0)
const showDefinition = ref(false)
const isReviewLoading = ref(false)

// 批量听写模式相关状态
const isDictationMode = ref(false)
const batchDictationWords = ref<WordItem[]>([])
const currentDictationIndex = ref(0)
const showDictationAnswer = ref(false)
const isDictationLoading = ref(false)
const dictationUserInput = ref('')
const dictationInputRef = ref<InstanceType<typeof ElInput> | null>(null)
const isDictationChecking = ref(false)
const isDictationSpeaking = ref(false)
const dictationFeedback = ref<{type: string, message: string, details?: string} | null>(null)
const dictationShowHint = ref(false)
const dictationHintLength = ref(1)

// 单词速刷模式相关状态
const isSpeedReviewMode = ref(false)
const isSpeedReviewLoading = ref(false)
const speedReviewWords = ref<WordItem[]>([])
const currentSpeedReviewIndex = ref(0)
const speedReviewStats = ref({
  total: 0,
  mastered: 0,
  skipped: 0,
  startTime: null as Date | null
})

const filteredWords = computed(() => {
  let result = words.value

  if (searchQuery.value) {
    result = result.filter(word =>
      word.word.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      word.meaning.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  if (statusFilter.value) {
    result = result.filter(word => word.reviewStatus === statusFilter.value)
  }

  return result
})

// 判断单词是否应该进入速刷 - 参考闪卡式复习逻辑
const shouldReviewWord = (word: WordItem) => {
  // 1. 未复习的单词必须复习
  if (word.reviewStatus === 'unreviewed') return true
  
  // 2. 已逾期必须复习
  if (word.reviewStatus === 'overdue') return true
  
  // 3. 复习中的单词 - 检查nextReviewTime
  if (word.reviewStatus === 'reviewing') {
    if (word.nextReviewTime) {
      return new Date(word.nextReviewTime) <= new Date()
    }
    return true // 如果没有nextReviewTime，默认需要复习
  }
  
  // 4. 已掌握的单词（可选巩固）- 检查nextReviewTime
  if (word.reviewStatus === 'mastered' && !word.noLongerReview) {
    if (word.nextReviewTime) {
      return new Date(word.nextReviewTime) <= new Date()
    }
    return false // 如果没有nextReviewTime，默认不需要复习
  }
  
  return false
}

// 获取需要速刷的单词
const speedReviewWordsCount = computed(() => {
  return filteredWords.value.filter(shouldReviewWord).length
})

// 分页后的数据
const paginatedWords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredWords.value.slice(start, end)
})

// 叠层视图数据 - 按状态排序：未复习 → 复习中 → 已掌握
const visibleStackWords = computed(() => {
  // 速刷模式使用速刷单词列表
  if (isSpeedReviewMode.value) {
    const start = currentSpeedReviewIndex.value
    const remainingWords = speedReviewWords.value.length - start
    const dynamicStackSize = Math.min(remainingWords, 8)
    const end = start + dynamicStackSize
    return speedReviewWords.value.slice(start, end)
  }
  
  // 普通模式按状态优先级排序
  const sortedWords = [...filteredWords.value].sort((a, b) => {
    const statusOrder = { 'unreviewed': 0, 'reviewing': 1, 'mastered': 2, 'overdue': 0 }
    return statusOrder[a.reviewStatus] - statusOrder[b.reviewStatus]
  })
  
  const start = currentStackIndex.value
  // 动态计算堆叠数量：根据实际单词数量，最多显示8张（包括当前张）
  const remainingWords = sortedWords.length - start
  const dynamicStackSize = Math.min(remainingWords, 8) // 最多显示8张，包括当前张
  const end = start + dynamicStackSize
  return sortedWords.slice(start, end)
})

// 叠层卡片样式
const getStackCardStyle = (index: number) => {
  const totalStackSize = visibleStackWords.value.length // 使用实际的堆叠数量
  
  if (index === 0) {
    // 当前卡片：完全可见，无偏移，确保在最上层
    return {
      transform: 'translateY(0px) translateX(0px)',
      zIndex: totalStackSize + 10, // 根据实际堆叠数量调整z-index
      opacity: 1,
      position: 'absolute' as const,
      top: 0,
      left: 0,
      right: 0,
      cursor: 'default',
      transition: 'all 0.3s ease'
    }
  }
  
  // 叠层卡片：左右展开更多张卡片，模拟真实卡片堆叠
  // 计算左右展开的卡片数量
  const leftCards = Math.min(Math.floor(totalStackSize / 2), 4) // 左侧最多4张
  const rightCards = Math.min(Math.floor((totalStackSize - 1) / 2), 4) // 右侧最多4张
  
  // 根据卡片位置计算偏移
  let horizontalOffset = 0
  if (index <= leftCards) {
    // 左侧卡片：向左偏移，偏移量递增
    horizontalOffset = -25 - (leftCards - index) * 20
  } else if (index >= totalStackSize - rightCards) {
    // 右侧卡片：向右偏移，偏移量递增
    horizontalOffset = 25 + (index - (totalStackSize - rightCards)) * 20
  } else {
    // 中间卡片：轻微交替偏移
    horizontalOffset = index % 2 === 1 ? -15 : 15
  }
  
  const verticalOffset = index * 12 // 增加垂直偏移，模拟更厚的卡片
  const zIndex = totalStackSize - index // 根据实际堆叠数量调整z-index
  
  // 根据偏移方向调整旋转角度
  const rotation = horizontalOffset > 0 ? 2.5 + (horizontalOffset - 25) * 0.05 : 
                   horizontalOffset < 0 ? -2.5 + (horizontalOffset + 25) * 0.05 : 
                   index % 2 === 1 ? 1.5 : -1.5
  
  // 优化阴影计算，避免张数过多时阴影过重
  const maxShadowOffset = 60 // 最大阴影偏移量
  const shadowOffset = Math.min(verticalOffset, maxShadowOffset)
  const shadowBlur = Math.min(verticalOffset * 1.2, 80) // 限制阴影模糊范围
  const shadowOpacity = Math.max(0.05, 0.15 - index * 0.02) // 随层级递减阴影透明度
  
  return {
    transform: `translateY(${verticalOffset}px) translateX(${horizontalOffset}px) rotate(${rotation}deg)`,
    zIndex: zIndex,
    opacity: 1, // 移除透明度，避免颜色晕染
    position: 'absolute' as const,
    top: 0,
    left: 0,
    right: 0,
    cursor: 'pointer',
    transition: 'all 0.3s ease',
    // 优化后的卡片厚度阴影，避免过重
    boxShadow: `0 ${shadowOffset}px ${shadowBlur}px rgba(0, 0, 0, ${shadowOpacity}), 0 ${shadowOffset * 0.3}px ${shadowOffset * 0.6}px rgba(0, 0, 0, ${shadowOpacity * 0.6})`
  }
}

// 叠层视图控制方法
const nextStackCard = () => {
  if (currentStackIndex.value < filteredWords.value.length - 1) {
    // 添加当前卡片向右消失的动画
    const currentCard = document.querySelector('.word-card-stack:first-child .word-card') as HTMLElement
    if (currentCard) {
      currentCard.style.transition = 'all 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94)'
      currentCard.style.transform = 'translateX(120%) rotate(20deg) scale(0.8)'
      currentCard.style.opacity = '0'
    }
    
    // 延迟切换卡片，让动画完成
    setTimeout(() => {
      currentStackIndex.value++
      // 重置动画状态
      setTimeout(() => {
        resetCardAnimation()
      }, 100)
    }, 300)
  }
}

const previousStackCard = () => {
  if (currentStackIndex.value > 0) {
    // 添加当前卡片向左消失的动画
    const currentCard = document.querySelector('.word-card-stack:first-child .word-card') as HTMLElement
    if (currentCard) {
      currentCard.style.transition = 'all 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94)'
      currentCard.style.transform = 'translateX(-120%) rotate(-20deg) scale(0.8)'
      currentCard.style.opacity = '0'
    }
    
    // 延迟切换卡片，让动画完成
    setTimeout(() => {
      currentStackIndex.value--
      // 重置动画状态
      setTimeout(() => {
        resetCardAnimation()
      }, 100)
    }, 300)
  }
}

const handleStackCardClick = (index: number) => {
  if (index > 0) {
    currentStackIndex.value += index
  }
}

// 重置卡片动画状态
const resetCardAnimation = () => {
  // 重置所有卡片的动画状态
  const cards = document.querySelectorAll('.word-card-stack .word-card')
  cards.forEach((card, index) => {
    const element = card as HTMLElement
    element.style.transition = ''
    element.style.transform = ''
    element.style.opacity = ''
    
    // 为新出现的卡片添加淡入动画
    if (index === 0) {
      element.style.opacity = '0'
      element.style.transform = 'translateY(20px) scale(0.95)'
      element.style.transition = 'all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94)'
      
      // 触发淡入动画
      setTimeout(() => {
        element.style.opacity = '1'
        element.style.transform = 'translateY(0) scale(1)'
      }, 50)
    }
  })
}

// 单词速刷相关方法
const startWordSpeedReview = async () => {
  try {
    isSpeedReviewLoading.value = true
    
    // 获取需要速刷的单词
    const wordsToReview = filteredWords.value.filter(shouldReviewWord)
    
    if (wordsToReview.length === 0) {
      ElMessage.info('暂无需要速刷的单词')
      return
    }
    
    // 初始化速刷状态
    speedReviewWords.value = wordsToReview
    currentSpeedReviewIndex.value = 0
    speedReviewStats.value = {
      total: wordsToReview.length,
      mastered: 0,
      skipped: 0,
      startTime: new Date()
    }
    
    // 切换到叠层视图
    viewMode.value = 'stack'
    currentStackIndex.value = 0
    isSpeedReviewMode.value = true
    
    // 自动滚动到堆叠视图
    await nextTick()
    const stackContainer = document.querySelector('.word-stack-container')
    if (stackContainer) {
      stackContainer.scrollIntoView({ 
        behavior: 'smooth', 
        block: 'start' 
      })
    }
    
    ElMessage.success(`开始单词速刷，共 ${wordsToReview.length} 个单词`)
    
  } catch (error) {
    ElMessage.error('启动速刷失败')
  } finally {
    isSpeedReviewLoading.value = false
  }
}

const markSpeedReviewAsMastered = async () => {
  const currentWord = speedReviewWords.value[currentSpeedReviewIndex.value]
  if (!currentWord) return
  
  const userId = userStore.userInfo?.id
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    // 更新单词状态为已掌握
    await vocabularyApi.reviewWord(String(userId), Number(currentWord.id), 'mastered')
    
    // 更新统计
    speedReviewStats.value.mastered++
    
    // 刷新单词列表
    await loadWords()
    
    // 切换到下一张
    nextSpeedReviewCard()
    
  } catch (error) {
    ElMessage.error('标记失败')
  }
}

const skipSpeedReviewWord = async () => {
  const currentWord = speedReviewWords.value[currentSpeedReviewIndex.value]
  if (!currentWord) return
  
  const userId = userStore.userInfo?.id
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    // 跳过单词，标记为学习中
    await vocabularyApi.reviewWord(String(userId), Number(currentWord.id), 'learning')
    
    // 更新统计
    speedReviewStats.value.skipped++
    
    // 刷新单词列表
    await loadWords()
    
    // 切换到下一张
    nextSpeedReviewCard()
    
  } catch (error) {
    ElMessage.error('跳过失败')
  }
}

const nextSpeedReviewCard = () => {
  if (currentSpeedReviewIndex.value < speedReviewWords.value.length - 1) {
    currentSpeedReviewIndex.value++
    currentStackIndex.value++
  } else {
    // 速刷完成
    finishSpeedReview()
  }
}

const previousSpeedReviewCard = () => {
  if (currentSpeedReviewIndex.value > 0) {
    currentSpeedReviewIndex.value--
    currentStackIndex.value--
  }
}

const finishSpeedReview = () => {
  const duration = speedReviewStats.value.startTime 
    ? Math.round((new Date().getTime() - speedReviewStats.value.startTime.getTime()) / 1000 / 60)
    : 0
  
  ElMessageBox.alert(
    `速刷完成！\n\n总单词数: ${speedReviewStats.value.total}\n已掌握: ${speedReviewStats.value.mastered}\n跳过: ${speedReviewStats.value.skipped}\n用时: ${duration} 分钟`,
    '速刷完成',
    {
      confirmButtonText: '确定',
      type: 'success'
    }
  )
  
  // 退出速刷模式
  isSpeedReviewMode.value = false
  viewMode.value = 'grid'
  currentStackIndex.value = 0
}

// 当前复习单词
const currentReviewWord = computed(() => {
  if (reviewWords.value.length > 0 && currentReviewIndex.value < reviewWords.value.length) {
    return reviewWords.value[currentReviewIndex.value]
  }
  return null
})

onMounted(() => {
  // 先初始化图表
  initStatusChart()
  // 再加载数据
  loadWords()
  loadStats()

  // 添加窗口大小变化时重新调整图表大小
  window.addEventListener('resize', handleResize)
})

const handleResize = () => {
  if (statusChartInstance) {
    statusChartInstance.resize()
  }
}

onUnmounted(() => {
  // 清理图表实例，防止内存泄漏
  if (statusChartInstance) {
    statusChartInstance.dispose()
    statusChartInstance = null
  }

  // 移除窗口大小变化事件监听
  window.removeEventListener('resize', handleResize)
})

// 图表相关
const statusChart = ref<HTMLElement>()
let statusChartInstance: echarts.ECharts | null = null

// 初始化状态分布图
const initStatusChart = () => {
  if (statusChart.value) {
    statusChartInstance = echarts.init(statusChart.value)

    // 初始化空图表
    statusChartInstance.setOption({
      title: {
        text: '单词状态分布',
        left: 'center',
        textStyle: {
          fontSize: 16
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 'right'
      },
      series: [{
        name: '单词状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10
        },
        label: {
          show: true,
          formatter: '{b}\n{c}个'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        data: [
          { value: 0, name: '未复习', itemStyle: { color: '#409eff', shadowBlur: 10, shadowColor: 'rgba(64, 158, 255, 0.5)' } },
          { value: 0, name: '复习中', itemStyle: { color: '#e6a23c', shadowBlur: 10, shadowColor: 'rgba(230, 162, 60, 0.5)' } },
          { value: 0, name: '已掌握', itemStyle: { color: '#67c23a', shadowBlur: 10, shadowColor: 'rgba(103, 194, 58, 0.5)' } }
        ]
      }]
    })
  }
}

// 更新状态分布图数据
const updateStatusChart = () => {
  // 加强状态检查
  if (!statusChartInstance) {
    // 如果图表实例不存在，尝试重新初始化
    initStatusChart()
    // 延迟一小段时间后再次尝试更新数据
    setTimeout(() => {
      if (statusChartInstance) {
        doUpdateStatusChart()
      }
    }, 100)
    return
  }

  doUpdateStatusChart()
}

// 实际执行图表数据更新的函数
const doUpdateStatusChart = () => {
  if (!statusChartInstance) return

  // 统计不同状态的单词数量
  const statusCounts = {
    unreviewed: words.value.filter(w => w.reviewStatus === 'unreviewed').length,
    reviewing: words.value.filter(w => w.reviewStatus === 'reviewing').length,
    mastered: words.value.filter(w => w.reviewStatus === 'mastered').length
  }

  // 准备图表数据
  const chartData = [
    {
      value: statusCounts.unreviewed,
      name: '未复习',
      itemStyle: {
        color: 'rgba(64, 158, 255, 0.8)',
        shadowBlur: 10,
        shadowColor: 'rgba(64, 158, 255, 0.3)'
      }
    },
    {
      value: statusCounts.reviewing,
      name: '复习中',
      itemStyle: {
        color: 'rgba(230, 162, 60, 0.8)',
        shadowBlur: 10,
        shadowColor: 'rgba(230, 162, 60, 0.3)'
      }
    },
    {
      value: statusCounts.mastered,
      name: '已掌握',
      itemStyle: {
        color: 'rgba(103, 194, 58, 0.8)',
        shadowBlur: 10,
        shadowColor: 'rgba(103, 194, 58, 0.3)'
      }
    }
  ]

  try {
    // 更新图表
    statusChartInstance.setOption({
      series: [{
        name: '单词状态',
        data: chartData
      }]
    })
  } catch (error) {
    // 如果更新失败，尝试重新初始化并更新
    setTimeout(() => {
      initStatusChart()
      if (statusChartInstance) {
        try {
          statusChartInstance.setOption({
            series: [{
              name: '单词状态',
              data: chartData
            }]
          })
        } catch (retryError) {
          // 静默失败，不输出错误
        }
      }
    }, 100)
  }
}

// 监听搜索和筛选条件变化，重置为第1页
watch([searchQuery, statusFilter], () => {
  currentPage.value = 1
})

const loadWords = async () => {
  loading.value = true
  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      words.value = []
      totalWords.value = 0
      ElMessage.warning('请先登录以查看生词本')
      return
    }

  // 使用实际API获取用户所有单词
    const response = await vocabularyApi.getUserWords(String(userId))
    if (response?.data?.length > 0) {
      // 从localStorage读取'不再巩固'状态数据
      let noLongerReviewMap: Record<string, {nextReviewTime: string}> = {}
      try {
        const storageKey = `no_longer_review_${userId}`
        const existingData = localStorage.getItem(storageKey)
        if (existingData) {
          noLongerReviewMap = JSON.parse(existingData)
        }
      } catch (error) {
        console.error('读取不再巩固状态失败:', error)
        // 静默失败，使用空对象
      }

      // 映射数据并处理进度条显示问题
      words.value = response.data.map((word: any) => {
        // API返回的是reviewStatus字段
        const backendStatus = word.reviewStatus;
        const frontendStatus = mapBackendStatusToFrontend(backendStatus);

        // 确保正确获取nextReviewAt字段值并预处理
        let nextReviewTime = word.nextReviewAt;
        let noLongerReview = word.noLongerReview || false;

        // 检查localStorage中是否有该单词的'不再巩固'状态
        const wordIdStr = String(word.id);
        if (noLongerReviewMap[wordIdStr]) {
          nextReviewTime = noLongerReviewMap[wordIdStr].nextReviewTime;
          noLongerReview = true;
        }

        // 预处理nextReviewTime：确保它是有效的日期字符串
        if (nextReviewTime && typeof nextReviewTime === 'string') {
          try {
            // 特别处理ISO 8601格式（如2025-09-19T09:53:40）
            if (nextReviewTime.includes('T')) {
              // 替换T为空格，使格式更易解析
              const cleanDateStr = nextReviewTime.replace('T', ' ')
                // 移除可能存在的毫秒部分和时区信息
                .split('.')[0].split('Z')[0].split('+')[0]
              
              // 验证清理后的日期是否有效
              const testDate = new Date(cleanDateStr)
              if (!isNaN(testDate.getTime())) {
                nextReviewTime = cleanDateStr
              }
            }
          } catch (error) {
            console.warn('预处理nextReviewTime失败:', error)
            // 保留原始值，让formatNextReviewTime函数后续处理
          }
        }

        // 计算是否需要复习（基于状态和时间）
        // 待复习定义：不晚于今日晚24点，包括未复习(unreviewed)、复习中(reviewing)和已掌握(mastered)的单词
        const needsReview = !noLongerReview && nextReviewTime &&
          new Date(nextReviewTime) <= new Date(new Date().setHours(23, 59, 59, 999));

        return {
          id: word.id || 0,
          word: word.word,
          meaning: word.meaning || word.translation,
          example: word.example || '',
          reviewStatus: frontendStatus,
          createdAt: word.addedAt || word.createdAt,
          phonetic: word.phonetic || '',
          nextReviewTime: nextReviewTime || undefined,
          reviewCount: word.reviewCount || 0,
          masteryLevel: mapStatusToMasteryLevel(word.reviewStatus),
          needsReview: needsReview,
          noLongerReview: noLongerReview
        }
      });

      totalWords.value = words.value.length

      // 数据加载完成后更新图表
      updateStatusChart()
    } else {
      words.value = []
      totalWords.value = 0
      ElMessage.info('您的生词本还是空的，快去添加单词吧！')

      // 清空图表
      if (statusChartInstance) {
        statusChartInstance.setOption({
          series: [{
            data: []
          }]
        })
      }
    }
  } catch (error) {
    ElMessage.error('获取生词列表失败，请稍后再试')
    words.value = []
    totalWords.value = 0
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      stats.value = {
        totalWords: 0,
        todayWords: 0,
        reviewWords: 0
      }
      return
    }

    // 并行获取所有需要的数据
    const [reviewResponse, todaySummaryResponse, statsResponse] = await Promise.all([
      // 获取今日复习词汇
      learningApi.getTodayReviewWords(String(userId)),
      // 获取今日小结（包含今日新增单词）
      learningApi.getTodaySummary(String(userId)),
      // 获取词汇统计（包含总单词数）
      vocabularyApi.getVocabularyStats(String(userId))
    ])

    // 首先尝试从API获取待复习单词数
    let reviewWordsCount = reviewResponse?.data?.length || 0

    // 如果API返回0个待复习单词，尝试从本地单词列表计算实际需要复习的单词数
    if (reviewWordsCount === 0 && words.value.length > 0) {
      const locallyNeedingReview = words.value.filter((word: WordItem) =>
        word.nextReviewTime &&
        new Date(word.nextReviewTime) <= new Date(new Date().setHours(23, 59, 59, 999))
      ).length

      // 如果本地计算有需要复习的单词，则使用本地计算结果
      if (locallyNeedingReview > 0) {
        reviewWordsCount = locallyNeedingReview
      }
    }

    // 更新统计数据
    stats.value = {
      totalWords: statsResponse?.data?.totalWords || 0,
      // 使用report-service返回的今日新增数据
      todayWords: todaySummaryResponse?.data?.dailyNewWords || 0,
      // 使用更准确的待复习单词数
      reviewWords: reviewWordsCount
    }
  } catch (error) {
    // 如果发生错误，尝试从本地单词列表计算待复习单词数作为备选
    if (words.value.length > 0) {
      const locallyNeedingReview = words.value.filter(word =>
        word.nextReviewTime &&
        new Date(word.nextReviewTime) <= new Date(new Date().setHours(23, 59, 59, 999))
      ).length

      if (locallyNeedingReview > 0) {
        stats.value.reviewWords = locallyNeedingReview
      }
    }
    // 保持原有数据，避免显示错误数据
  }
}

// 映射后端状态到前端显示状态
const mapBackendStatusToFrontend = (backendStatus: string): string => {
  const statusMap: Record<string, string> = {
    'mastered': 'mastered',
    'learning': 'reviewing',
    'new': 'unreviewed'
  }
  return statusMap[backendStatus] || 'unreviewed'
}

// 映射后端状态到masteryLevel（用于向后兼容）
const mapStatusToMasteryLevel = (status: string): number => {
  const levelMap: Record<string, number> = {
    'mastered': 2,
    'learning': 1,
    'new': 0
  }
  return levelMap[status] || 0
}

const reviewWord = async (word: WordItem) => {
  // 防止重复点击
  if (isReviewing.value) {
    return
  }

  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      ElMessage.warning('请先登录')
      return
    }

    // 设置复习状态
    isReviewing.value = true
    reviewingWordId.value = word.id

    // 使用ElMessageBox让用户确认是否记住了单词
    // 注意：ElMessageBox.confirm的返回值value是boolean类型
    const { value: isRemembered } = await ElMessageBox.confirm(
      `您记住了单词 "${word.word}" 吗？`,
      {
        title: '确认复习结果',
        confirmButtonText: '记住了',
        cancelButtonText: '没记住',
        type: 'warning'
      }
    )

    // 确保isRemembered是布尔值
    const isRememberedBool: boolean = !!isRemembered

    // 将布尔值映射为后端期望的状态字符串
    const reviewStatus = isRememberedBool ? 'mastered' : 'learning';
    // 调用实际API更新单词复习记录
    await vocabularyApi.reviewWord(String(userId), word.id, reviewStatus)

    // 保存当前页码，避免刷新后回到第一页
    const currentPageValue = currentPage.value

    // 重新从后端获取最新数据，确保状态实时更新
    await loadWords()

    // 恢复当前页码，保持用户体验一致性
    currentPage.value = currentPageValue

    ElMessage.success('复习成功！')
    // 刷新统计信息
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败，请稍后再试')
    }
  } finally {
    // 无论成功失败，都要重置复习状态
    isReviewing.value = false
    reviewingWordId.value = null
  }
}

const deleteWord = async (word: WordItem) => {
  try {
    await ElMessageBox.confirm('确定删除这个单词吗？', '提示', {
      type: 'warning'
    })

    const userId = userStore.userInfo?.id
    if (!userId) {
      ElMessage.warning('请先登录')
      return
    }

    // 调用实际API删除单词
    await vocabularyApi.deleteWord(String(userId), word.id)

    // 更新本地数据
    words.value = words.value.filter((w: WordItem) => w.id !== word.id)
    totalWords.value = words.value.length
    ElMessage.success('删除成功！')
    // 刷新统计信息
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败，请稍后再试')
    }
    // 用户取消操作不做处理
  }
}

// 计算复习进度百分比 - 增强健壮性
const calculateReviewProgress = (nextReviewTime: string): number => {
  // 安全检查：确保nextReviewTime存在且有效
  if (!nextReviewTime || typeof nextReviewTime !== 'string' || nextReviewTime.trim() === '') {
    return 50
  }

  try {
    const now = new Date().getTime()
    let nextReview

    // 处理不同格式的日期
    if (nextReviewTime.includes('/')) {
      // 处理YYYY/MM/DD HH:MM:SS格式
      const [datePart, timePart] = nextReviewTime.split(' ')
      const [year, month, day] = datePart.split('/').map(Number)
      const [hour, minute, second] = timePart?.split(':')?.map(Number) || [0, 0, 0]
      nextReview = new Date(year, month - 1, day, hour, minute, second).getTime()
    } else {
      // 处理ISO格式和其他标准日期格式
      nextReview = new Date(nextReviewTime).getTime()
    }

    // 检查日期是否有效
    if (isNaN(nextReview)) {
      return 50
    }

    const dayInMs = 24 * 60 * 60 * 1000
    const timeDiff = nextReview - now

    // 如果已经过了复习时间，进度条显示100%
    if (timeDiff <= 0) {
      return 100
    }

    // 计算剩余时间占一天的百分比
    const progress = Math.min(100, Math.max(0, (dayInMs - timeDiff) / dayInMs * 100))
    const result = Math.round(progress)

    // 最终检查：确保返回值是有效的数字
    return isNaN(result) ? 50 : result
  } catch (error) {
    console.error('计算复习进度出错:', error)
    return 50
  }
}

// 格式化下次复习时间 - 修复日期解析问题和负数时间显示
const formatNextReviewTime = (nextReviewTime: string): string => {
  // 空值检查
  if (!nextReviewTime || typeof nextReviewTime !== 'string' || nextReviewTime.trim() === '') {
    return '时间未设置'
  }

  const now = new Date()
  let reviewDate: Date

  try {
    // 尝试多种日期格式解析策略
    if (nextReviewTime.includes('/')) {
      // 处理YYYY/MM/DD HH:MM:SS格式
      const [datePart, timePart] = nextReviewTime.split(' ')
      const [year, month, day] = datePart.split('/').map(Number)
      const [hour, minute, second] = timePart?.split(':')?.map(Number) || [0, 0, 0]
      reviewDate = new Date(year, month - 1, day, hour, minute, second)
    } else if (nextReviewTime.includes('T')) {
      // 特别处理ISO 8601格式（如2025-09-19T09:53:40）
      try {
        // 首先尝试直接解析ISO格式
        reviewDate = new Date(nextReviewTime)
        
        // 如果解析失败或日期无效，尝试手动解析
        if (isNaN(reviewDate.getTime())) {
          // 替换T为空格，使格式更易解析
          const isoDateStr = nextReviewTime.replace('T', ' ')
          // 移除可能存在的毫秒部分和时区信息
          const cleanDateStr = isoDateStr.split('.')[0].split('Z')[0].split('+')[0]
          const [datePart, timePart] = cleanDateStr.split(' ')
          const [year, month, day] = datePart.split('-').map(Number)
          const [hour, minute, second] = timePart?.split(':')?.map(Number) || [0, 0, 0]
          reviewDate = new Date(year, month - 1, day, hour, minute, second)
        }
      } catch (e) {
        // 手动解析作为备选方案
        const isoDateStr = nextReviewTime.replace('T', ' ')
        const cleanDateStr = isoDateStr.split('.')[0].split('Z')[0].split('+')[0]
        const [datePart, timePart] = cleanDateStr.split(' ')
        const [year, month, day] = datePart.split('-').map(Number)
        const [hour, minute, second] = timePart?.split(':')?.map(Number) || [0, 0, 0]
        reviewDate = new Date(year, month - 1, day, hour, minute, second)
      }
    } else {
      // 尝试解析其他标准日期格式
      reviewDate = new Date(nextReviewTime)
    }

    // 再次检查日期有效性
    if (!reviewDate || isNaN(reviewDate.getTime())) {
      console.warn('无法解析日期:', nextReviewTime)
      return '时间格式无效'
    }

    const diffMs = reviewDate.getTime() - now.getTime()
    const diffHours = Math.ceil(diffMs / (1000 * 60 * 60))

    // 处理负时间情况（复习时间已过）
    if (diffHours < 0) {
      const absHours = Math.abs(diffHours)
      if (absHours < 24) {
        return `已逾期${absHours}小时`
      } else {
        const diffDays = Math.ceil(absHours / 24)
        return `已逾期${diffDays}天`
      }
    } else if (diffHours < 24) {
      return `${diffHours}小时后复习`
    } else {
      const diffDays = Math.ceil(diffHours / 24)
      return `${diffDays}天后复习`
    }
  } catch (error) {
    console.error('格式化复习时间出错:', error, '输入时间:', nextReviewTime)
    return '时间格式无效'
  }
}

// 格式化创建时间（添加时间）
const formatCreatedTime = (createdTime: string): string => {
  // 空值检查
  if (!createdTime || typeof createdTime !== 'string' || createdTime.trim() === '') {
    return '时间未设置'
  }

  let createdDate: Date

  try {
    // 特别处理ISO 8601格式（如2025-09-19T09:53:40）
    if (createdTime.includes('T')) {
      // 替换T为空格，使格式更易解析
      const cleanDateStr = createdTime.replace('T', ' ')
        // 移除可能存在的毫秒部分和时区信息
        .split('.')[0].split('Z')[0].split('+')[0]
      
      // 验证清理后的日期是否有效
      createdDate = new Date(cleanDateStr)
      if (isNaN(createdDate.getTime())) {
        // 如果解析失败，尝试手动解析
        const [datePart, timePart] = cleanDateStr.split(' ')
        const [year, month, day] = datePart.split('-').map(Number)
        const [hour, minute, second] = timePart?.split(':')?.map(Number) || [0, 0, 0]
        createdDate = new Date(year, month - 1, day, hour, minute, second)
      }
    } else {
      // 尝试直接解析其他格式
      createdDate = new Date(createdTime)
    }

    // 检查日期有效性
    if (!createdDate || isNaN(createdDate.getTime())) {
      console.warn('无法解析创建时间:', createdTime)
      return '时间格式无效'
    }

    // 格式化日期为：YYYY-MM-DD HH:MM:SS
    const year = createdDate.getFullYear()
    const month = String(createdDate.getMonth() + 1).padStart(2, '0')
    const day = String(createdDate.getDate()).padStart(2, '0')
    const hour = String(createdDate.getHours()).padStart(2, '0')
    const minute = String(createdDate.getMinutes()).padStart(2, '0')
    const second = String(createdDate.getSeconds()).padStart(2, '0')

    return `${year}-${month}-${day} ${hour}:${minute}:${second}`
  } catch (error) {
    console.error('格式化创建时间出错:', error, '输入时间:', createdTime)
    return '时间格式无效'
  }
}

// 获取复习状态对应的CSS类
const getReviewStatusClass = (status: string): string => {
  const statusMap: Record<string, string> = {
    'unreviewed': 'status-unreviewed',
    'mastered': 'status-mastered',
    'reviewing': 'status-reviewing'
  }
  return statusMap[status] || 'status-unreviewed'
}

// 显示学习模式引导
const showLearningModesGuide = () => {
  ElMessageBox.alert(
    `<div class="learning-guide-container" style="text-align: left; max-width: 600px; margin: 0 auto;">
        <div class="guide-header" style="text-align: center; margin-bottom: 20px;">
          <h3 style="color: #303133; font-size: 18px; margin: 0 0 8px 0;">学习模式介绍</h3>
          <div style="height: 3px; width: 60px; background: linear-gradient(90deg, #409eff, #67c23a); margin: 0 auto;"></div>
        </div>

        <div style="margin-bottom: 25px;">
          <h4 style="color: #303133; font-size: 15px; margin: 0 0 15px 0; padding-left: 10px; border-left: 4px solid #409eff;">批量学习模式</h4>

          <div class="guide-card" style="background-color: #f8fafc; border-radius: 10px; padding: 16px; margin-bottom: 15px; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);">
            <h5 style="color: #303133; font-size: 14px; margin: 0 0 8px 0;">📚 闪卡式批量复习</h5>
            <p style="color: #606266; line-height: 1.5; margin: 0; font-size: 13px;">以闪卡形式批量复习单词，根据艾宾浩斯记忆曲线，系统自动筛选今日需复习单词，适合系统性长期学习。</p>
          </div>

          <div class="guide-card" style="background-color: #f8fafc; border-radius: 10px; padding: 16px; margin-bottom: 15px; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);">
            <h5 style="color: #303133; font-size: 14px; margin: 0 0 8px 0;">📝 批量听写</h5>
            <p style="color: #606266; line-height: 1.5; margin: 0; font-size: 13px;">连续听写多个单词，模拟真实听力场景，适合系统性训练，提高学习效率。</p>
          </div>
        </div>

        <div style="margin-bottom: 20px;">
          <h4 style="color: #303133; font-size: 15px; margin: 0 0 15px 0; padding-left: 10px; border-left: 4px solid #67c23a;">单个单词学习模式</h4>

          <div class="guide-card" style="background-color: #f8fafc; border-radius: 10px; padding: 16px; margin-bottom: 15px; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);">
            <h5 style="color: #303133; font-size: 14px; margin: 0 0 8px 0;">🎧 单词听写</h5>
            <p style="color: #606266; line-height: 1.5; margin: 0; font-size: 13px;">在单词卡片上直接进行听写练习，强化单词拼写和发音记忆，快速巩固。</p>
          </div>

          <div class="guide-card" style="background-color: #f8fafc; border-radius: 10px; padding: 16px; margin-bottom: 15px; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);">
            <h5 style="color: #303133; font-size: 14px; margin: 0 0 8px 0;">⚡ 快速复习</h5>
            <p style="color: #606266; line-height: 1.5; margin: 0; font-size: 13px;">单独点击按钮进行快速复习，灵活控制学习进度，适合碎片化时间学习。</p>
          </div>
        </div>

        <div class="guide-tip" style="background-color: #e6f7ff; border-radius: 8px; padding: 12px; text-align: center;">
          <p style="color: #1890ff; font-size: 13px; margin: 0;">💡 小贴士：结合多种学习模式，可显著提高记忆效果！</p>
        </div>
      </div>`,
    '学习模式指南',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '我知道了',
      type: 'info',
      customClass: 'learning-guide-dialog'
    }
  ).catch(() => {
    // 处理取消操作，防止Uncaught (in promise) cancel错误
  })
}

// 开始今日复习
const startTodayReview = async () => {
  const userId = userStore.userInfo?.id
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  isReviewLoading.value = true
  try {
    // 调用learning API获取今日复习单词
    const response = await learningApi.getTodayReviewWords(String(userId))

    // 更健壮地处理数据，确保数据是数组并包含正确的字段
    let todayReviews = Array.isArray(response?.data) ? response.data : []

    // 如果API返回空数组，尝试从本地单词列表中找出需要复习的单词
    if (todayReviews.length === 0 && words.value.length > 0) {
      const localReviewWords = words.value.filter((word: WordItem) =>
        word.nextReviewTime &&
        new Date(word.nextReviewTime) <= new Date(new Date().setHours(23, 59, 59, 999))
      )

      // 如果本地有需要复习的单词
      if (localReviewWords.length > 0) {
        // 使用本地找到的单词作为复习内容
        todayReviews = localReviewWords
      } else {
        ElMessage.info('今日没有需要复习的单词')
        return
      }
    }

    // 规范化复习单词数据，确保每个单词都有必要的字段
    reviewWords.value = todayReviews.map((word: any) => ({
      id: word.id || word.wordId || 0,
      word: word.word || '',
      definition: word.meaning || word.definition || '暂无释义',
      partOfSpeech: word.partOfSpeech || '',
      pronunciation: word.phonetic || word.pronunciation || '',
      reviewCount: word.reviewCount || 0,
      nextReviewTime: word.nextReviewTime || word.dueDate || new Date().toISOString(),
      difficultyLevel: word.difficultyLevel || 1
    })).filter(word => word.word.trim() !== '') // 过滤掉空单词

    if (reviewWords.value.length === 0) {
      ElMessage.info('今日没有有效的复习单词')
    } else {
      currentReviewIndex.value = 0
      showDefinition.value = false
      isReviewMode.value = true
    }
  } catch (error) {
    // 发生错误时，也尝试从本地获取复习单词作为备选
    if (words.value.length > 0) {
      const localReviewWords = words.value.filter((word: WordItem) =>
        word.reviewStatus === 'reviewing' &&
        word.nextReviewTime &&
        new Date(word.nextReviewTime) <= new Date()
      )

      if (localReviewWords.length > 0) {
        // 使用本地找到的单词作为复习内容
        reviewWords.value = localReviewWords.map((word: WordItem) => ({
          id: word.id,
          word: word.word,
          definition: word.meaning,
          pronunciation: word.phonetic || '',
          reviewCount: word.reviewCount || 0,
          nextReviewTime: word.nextReviewTime || new Date().toISOString(),
          difficultyLevel: 1
        }))

        currentReviewIndex.value = 0
        showDefinition.value = false
        isReviewMode.value = true
      } else {
        ElMessage.error('获取复习内容失败，请稍后再试')
      }
    } else {
      ElMessage.error('获取复习内容失败，请稍后再试')
    }
  } finally {
    isReviewLoading.value = false
  }
}

// 显示单词释义
const showWordDefinition = () => {
  showDefinition.value = true;
}

// 记录复习结果
const recordReviewResult = async (isRemembered: boolean) => {
  if (!currentReviewWord.value) return

  const userId = userStore.userInfo?.id
  if (!userId) {
    // 使用setTimeout包装ElMessage以避免潜在的渲染冲突
    setTimeout(() => {
      ElMessage.warning('请先登录')
    }, 0)
    return
  }

  isReviewLoading.value = true
  try {
    // 将布尔值映射为后端期望的状态字符串
    const reviewStatus = isRemembered ? 'mastered' : 'learning';
    // 调用API记录复习结果
    await vocabularyApi.reviewWord(
      String(userId),
      currentReviewWord.value.id,
      reviewStatus
    )

    // 移动到下一个单词
    currentReviewIndex.value++
    showDefinition.value = false

    // 如果复习完所有单词，显示完成提示
    if (currentReviewIndex.value >= reviewWords.value.length) {
      // 使用setTimeout包装ElMessage以避免潜在的渲染冲突
      setTimeout(() => {
        ElMessage.success('今日复习已完成！')
      }, 0)
      // 刷新数据
      await loadWords()
      await loadStats()
      // 退出复习模式
      setTimeout(() => {
        exitReviewMode()
      }, 1500)
    }
  } catch (error) {
    // 使用setTimeout包装ElMessage以避免潜在的渲染冲突
    setTimeout(() => {
      ElMessage.error('操作失败，请稍后再试')
    }, 0)
  } finally {
    isReviewLoading.value = false
  }
}

// 不再巩固单词 - 调用后端API设置单词为不再巩固
const setWordAsNoLongerReview = async (word: WordItem) => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      ElMessage.warning('请先登录')
      return
    }

    // 使用ElMessageBox让用户确认是否将单词设置为不再巩固
    await ElMessageBox.confirm(
      `确定将单词 "${word.word}" 设置为不再巩固吗？设置后该单词将不会再进入复习流程，但仍会显示在单词列表中。`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 设置加载状态
    isReviewing.value = true
    reviewingWordId.value = word.id

    // 设置nextReviewTime为100年后，这样单词就不会再进入复习流程
    const farFutureDate = new Date()
    farFutureDate.setFullYear(farFutureDate.getFullYear() + 100)

    // 找到单词并修改其状态（前端即时更新）
    const wordIndex = words.value.findIndex((w: WordItem) => w.id === word.id)
    if (wordIndex !== -1) {
      words.value[wordIndex].nextReviewTime = farFutureDate.toISOString()
      words.value[wordIndex].noLongerReview = true
      // 移除不存在的属性赋值
      // words.value[wordIndex].needsReview = false
    }

    // 持久化存储'不再巩固'状态到localStorage - 直接实现功能
    try {
      const storageKey = `no_longer_review_${userId}`
      const existingData = localStorage.getItem(storageKey)
      const wordStatusMap: Record<string, {nextReviewTime: string}> = existingData ? JSON.parse(existingData) : {}
      wordStatusMap[String(word.id)] = { nextReviewTime: farFutureDate.toISOString() }
      localStorage.setItem(storageKey, JSON.stringify(wordStatusMap))
    } catch (error) {
      console.error('保存不再巩固状态失败:', error)
      // 静默失败，不影响主要功能
    }

    try {
      // 调用后端API设置单词为不再巩固
      const response = await reportApi.setWordAsNoLongerReview(word.id)
      console.log('setWordAsNoLongerReview response:', response)

      ElMessage.success('已设置为不再巩固')
      // 刷新单词列表以获取最新状态
      await loadWords()
      // 刷新统计信息
      loadStats()
    } catch (apiError) {
      console.error('后端API调用失败，但前端状态已更新:', apiError)
      // 即使API调用失败，前端状态已经更新，仍然显示成功消息
      ElMessage.success('已设置为不再巩固')
      // 不需要刷新单词列表，因为我们已经在前端更新了状态
      loadStats()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('设置单词为不再巩固失败:', error)
      ElMessage.error('设置失败，请重试')
    }
  } finally {
    // 重置复习状态
    isReviewing.value = false
    reviewingWordId.value = null
  }
}

// 退出复习模式
const exitReviewMode = () => {
  isReviewMode.value = false
  reviewWords.value = []
  currentReviewIndex.value = 0
  showDefinition.value = false
}

// 处理单词发音
const handleSpeakWord = (word: string) => {
  // 检查浏览器是否支持语音合成
  if (!tts.isSupported()) {
    ElMessage.warning('您的浏览器不支持语音合成功能')
    return
  }

  // 设置发音状态为加载中
  isDictationSpeaking.value = true

  try {
    // 调用TTS Manager的speakWord方法
    tts.speakWord(word).then(() => {
      // 发音完成后重置状态
      setTimeout(() => {
        isDictationSpeaking.value = false
      }, 500)
    }).catch((error) => {
      console.error('发音失败:', error)
      ElMessage.error('发音失败，请稍后再试')
      isDictationSpeaking.value = false
    })
  } catch (error) {
    console.error('发音失败:', error)
    ElMessage.error('发音失败，请稍后再试')
    isDictationSpeaking.value = false
  }
}

// 导入听写模态框组件
import DictationModal from '@/components/common/DictationModal.vue'
import {HelpFilled} from "@element-plus/icons-vue";

// 听写模态框相关状态
const dictationModal = ref<InstanceType<typeof DictationModal> | null>(null)
const showDictationModal = ref(false)
const currentDictationWord = ref<WordItem | null>(null)

// 处理单词听写
const handleDictateWord = (word: WordItem) => {
  console.log('Dictation button clicked, user tier:', userStore.userTier);
  // 检查用户是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录以使用听写功能')
    console.log('User not logged in, can\'t use dictation');
    return
  }

  // 检查用户是否为PRO会员或以上
  if (userStore.userTier !== 'pro' && userStore.userTier !== 'enterprise') {
    console.log('User is not premium, showing upgrade dialog');
    ElMessageBox.confirm(
      '听写功能是PRO会员专属特权，升级会员即可解锁全部学习功能！',
      '会员特权',
      {
        confirmButtonText: '立即升级',
        cancelButtonText: '暂不升级',
        type: 'info'
      }
    ).then(() => {
      // 跳转到会员中心页面
      window.location.href = '/subscription'
    }).catch(() => {
      // 用户取消
    })
    return
  }

  // 设置当前要听写的单词并显示模态框
  console.log('Setting dictation word:', word.word);
  currentDictationWord.value = word
  showDictationModal.value = true
  console.log('showDictationModal set to:', showDictationModal.value);

  // 强制下一个tick检查状态
  nextTick(() => {
    console.log('After nextTick - showDictationModal:', showDictationModal.value, 'currentDictationWord:', currentDictationWord.value?.word);
  })
}

// 关闭听写模态框
const closeDictationModal = () => {
  showDictationModal.value = false
  currentDictationWord.value = null
}

// 处理听写模态框完成事件（单个单词听写）
const handleDictationModalFinish = async (results: { word: WordItem; isCorrect: boolean }[]) => {
  if (!results || results.length === 0) return

  const userId = userStore.userInfo?.id
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  isDictationLoading.value = true
  try {
    // 获取第一个结果（单个单词听写模式下只有一个结果）
    const { word, isCorrect } = results[0]
    
    // 将布尔值映射为后端期望的状态字符串
    const reviewStatus = isCorrect ? 'mastered' : 'learning';
    
    // 调用API记录听写结果
    await vocabularyApi.reviewWord(
      String(userId),
      word.id,
      reviewStatus
    )

    // 立即刷新数据，更新单词状态
    await loadWords()
    await loadStats()

    // 显示操作成功的提示
    ElMessage.success(isCorrect ? '回答正确！单词状态已更新。' : '已记录听写结果。')
  } catch (error) {
    ElMessage.error('记录听写结果失败，请稍后再试')
  } finally {
    isDictationLoading.value = false
  }
}

// 开始批量听写
const startBatchDictation = async () => {
  const userId = userStore.userInfo?.id
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  // 检查用户是否为PRO会员或以上
  if (userStore.userTier !== 'pro' && userStore.userTier !== 'enterprise') {
    ElMessageBox.confirm(
      '听写功能是PRO会员专属特权，升级会员即可解锁全部学习功能！',
      '会员特权',
      {
        confirmButtonText: '立即升级',
        cancelButtonText: '暂不升级',
        type: 'info'
      }
    ).then(() => {
      // 跳转到会员中心页面
      window.location.href = '/subscription'
    }).catch(() => {
      // 用户取消
    })
    return
  }

  isDictationLoading.value = true
  try {
    // 调用learning API获取今日复习单词作为听写内容
    const response = await learningApi.getTodayReviewWords(String(userId))

    // 更健壮地处理数据
    let dictationWords = Array.isArray(response?.data) ? response.data : []

    // 如果API返回空数组，尝试从本地单词列表中找出需要复习的单词
    if (dictationWords.length === 0 && words.value.length > 0) {
      const localDictationWords = words.value.filter((word: WordItem) =>
        word.nextReviewTime &&
        new Date(word.nextReviewTime) <= new Date(new Date().setHours(23, 59, 59, 999))
      )

      // 如果本地有需要复习的单词
      if (localDictationWords.length > 0) {
        // 使用本地找到的单词作为听写内容
        dictationWords = localDictationWords
      } else {
        ElMessage.info('今日没有需要听写的单词')
        return
      }
    }

    // 规范化听写单词数据
    batchDictationWords.value = dictationWords.map((word: any) => ({
      id: word.id || word.wordId || 0,
      word: word.word || '',
      // 同时提供meaning和translation，以兼容不同组件的需求
      meaning: word.meaning || word.definition || '暂无释义',
      translation: word.meaning || word.definition || '暂无释义',
      phonetic: word.phonetic || '',
      example: word.example || '暂无例句',
      reviewStatus: word.reviewStatus || 'unreviewed',
      createdAt: word.createdAt || new Date().toISOString(),
      nextReviewTime: word.nextReviewTime || word.dueDate || new Date().toISOString(),
      // 必须提供noLongerReview属性，DictationModal.vue的WordItem接口要求
      noLongerReview: false
    })).filter((word: WordItem) => word.word.trim() !== '') // 过滤掉空单词

    if (batchDictationWords.value.length === 0) {
      ElMessage.info('今日没有有效的听写单词')
    } else {
      currentDictationIndex.value = 0
      showDictationAnswer.value = false
      isDictationMode.value = true

      // 自动发音第一个单词
      setTimeout(() => {
        // 再次检查数组是否非空且索引有效
        if (batchDictationWords.value.length > 0 && currentDictationIndex.value < batchDictationWords.value.length) {
          handleSpeakWord(batchDictationWords.value[currentDictationIndex.value].word)
        }
      }, 500)
    }
  } catch (error) {
    // 发生错误时，也尝试从本地获取听写单词作为备选
    if (words.value.length > 0) {
      const localDictationWords = words.value.filter((word: WordItem) =>
        word.reviewStatus === 'reviewing' &&
        word.nextReviewTime &&
        new Date(word.nextReviewTime) <= new Date()
      )

      if (localDictationWords.length > 0) {
        // 使用本地找到的单词作为听写内容
        batchDictationWords.value = localDictationWords

        currentDictationIndex.value = 0
        showDictationAnswer.value = false
        isDictationMode.value = true

        // 自动发音第一个单词
        setTimeout(() => {
          handleSpeakWord(batchDictationWords.value[currentDictationIndex.value].word)
        }, 500)
      } else {
        ElMessage.error('获取听写内容失败，请稍后再试')
      }
    } else {
      ElMessage.error('获取听写内容失败，请稍后再试')
    }
  } finally {
    isDictationLoading.value = false
  }
}

// 显示听写答案
const showDictationDefinition = () => {
  showDictationAnswer.value = true
}

// 记录听写结果
const recordDictationResult = async (isRemembered: boolean) => {
  if (currentDictationIndex.value >= batchDictationWords.value.length) return

  const userId = userStore.userInfo?.id
  if (!userId) {
    // 使用setTimeout包装ElMessage以避免潜在的渲染冲突
    setTimeout(() => {
      ElMessage.warning('请先登录')
    }, 0)
    return
  }

  isDictationLoading.value = true
  try {
    // 将布尔值映射为后端期望的状态字符串
    const reviewStatus = isRemembered ? 'mastered' : 'learning';
    // 调用API记录听写结果（使用复习API，因为逻辑相似）
    await vocabularyApi.reviewWord(
      String(userId),
      batchDictationWords.value[currentDictationIndex.value].id,
      reviewStatus
    )

    // 移动到下一个单词
    currentDictationIndex.value++
    showDictationAnswer.value = false
    dictationFeedback.value = null // 清除上一个单词的反馈信息
    dictationUserInput.value = '' // 清除用户输入
    dictationShowHint.value = false // 清除提示状态
    dictationHintLength.value = 1 // 重置提示长度

    // 立即刷新数据，更新单词状态
    // 无论是否完成所有单词，都刷新数据，确保状态及时更新
    await loadWords()
    await loadStats()

    // 如果听写完所有单词，显示完成提示
    if (currentDictationIndex.value >= batchDictationWords.value.length) {
      // 使用setTimeout包装ElMessage以避免潜在的渲染冲突
      setTimeout(() => {
        ElMessage.success('恭喜您完成了所有单词的听写！')
      }, 0)
      // 退出听写模式
      setTimeout(() => {
        exitDictationMode()
      }, 1500)
    } else {
      // 自动发音下一个单词
      setTimeout(() => {
        // 再次检查索引是否有效
        if (currentDictationIndex.value < batchDictationWords.value.length) {
          handleSpeakWord(batchDictationWords.value[currentDictationIndex.value].word)
        }
      }, 500)
    }
  } catch (error) {
    // 使用setTimeout包装ElMessage以避免潜在的渲染冲突
    setTimeout(() => {
      ElMessage.error('操作失败，请稍后再试')
    }, 0)
  } finally {
    isDictationLoading.value = false
  }
}

// 退出听写模式
const exitDictationMode = () => {
  // 首先设置isDictationMode为false触发模态框关闭动画
  isDictationMode.value = false

  // 使用setTimeout延迟重置其他状态，确保模态框关闭动画完成
  setTimeout(() => {
    batchDictationWords.value = []
    currentDictationIndex.value = 0
    showDictationAnswer.value = false
    dictationUserInput.value = ''
    dictationFeedback.value = null
    dictationShowHint.value = false
    dictationHintLength.value = 1
  }, 300)
}

// 检查听写答案
const checkDictationAnswer = async () => {
  if (currentDictationIndex.value >= batchDictationWords.value.length) return

  const currentWord = batchDictationWords.value[currentDictationIndex.value]
  const userInput = dictationUserInput.value.trim().toLowerCase()
  const correctAnswer = currentWord.word.toLowerCase()

  isDictationChecking.value = true

  try {
    // 移除加载状态延迟，直接检查答案

    if (userInput === correctAnswer) {
      // 答案正确
      dictationFeedback.value = {
        type: 'success',
        message: '拼写正确！',
        details: currentWord.meaning
      }
    } else {
      // 答案错误
      dictationFeedback.value = {
        type: 'error',
        message: '拼写错误',
        details: `正确拼写：${currentWord.word}`
      }
    }

    // 减少答案显示延迟，提高响应速度
    setTimeout(() => {
      showDictationAnswer.value = true
      isDictationChecking.value = false
      dictationUserInput.value = ''

      // 如果答案正确，自动跳转到下一个单词
      if (dictationFeedback.value?.type === 'success') {
        setTimeout(() => {
          recordDictationResult(true)
        }, 800) // 减少自动跳转延迟
      }
    }, 200)
  } catch (error) {
    ElMessage.error('检查答案失败，请稍后再试')
  } finally {
    if (!isDictationChecking.value) {
      isDictationChecking.value = false
    }
  }
}

// 跳过当前听写单词
const skipDictationWord = () => {
  if (currentDictationIndex.value >= batchDictationWords.value.length) return

  // 清空输入和反馈
  dictationUserInput.value = ''
  dictationFeedback.value = null
  dictationShowHint.value = false
  dictationHintLength.value = 1

  // 移动到下一个单词
  currentDictationIndex.value++
  showDictationAnswer.value = false

  // 如果还有单词，自动发音下一个；如果已完成所有单词，退出听写模式
  if (currentDictationIndex.value < batchDictationWords.value.length) {
    setTimeout(() => {
      // 再次检查索引是否有效
      if (currentDictationIndex.value < batchDictationWords.value.length) {
        handleSpeakWord(batchDictationWords.value[currentDictationIndex.value].word)
      }
    }, 500)
  } else {
    // 所有单词已处理完毕，退出听写模式
    setTimeout(() => {
      ElMessage.success('听写已完成！')
      exitDictationMode()
    }, 500)
  }
}

// 显示听写提示
const showDictationHint = () => {
  if (currentDictationIndex.value >= batchDictationWords.value.length) return

  const currentWord = batchDictationWords.value[currentDictationIndex.value]

  if (!dictationShowHint.value) {
    // 首次显示提示，显示前1个字符
    dictationHintLength.value = Math.min(1, currentWord.word.length)
    dictationShowHint.value = true
  } else {
    // 增加提示长度，但不超过单词长度的一半
    const maxHintLength = Math.floor(currentWord.word.length / 2)
    dictationHintLength.value = Math.min(dictationHintLength.value + 1, maxHintLength)
  }
}


</script>

<style scoped>
@import '@/assets/design-system.css';

.vocabulary-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-6);
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.vocabulary-container h2 {
  font-size: var(--text-4xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-8);
  text-align: center;
  position: relative;
}

.vocabulary-container h2::after {
  content: '';
  position: absolute;
  bottom: -var(--space-3);
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, var(--primary-500), var(--warm-orange));
  border-radius: var(--radius-sm);
}

.stats {
  margin-bottom: var(--space-12);
}

.stats-wrapper {
  display: flex;
  flex-direction: row;
  background-color: var(--bg-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-3xl);
  padding: var(--space-8);
  width: 100%;
  box-sizing: border-box;
  box-shadow: var(--shadow-lg);
  align-items: center;
  justify-content: space-between;
  gap: var(--space-12);
  min-height: 380px;
}

.stats-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
  align-items: flex-start;
  justify-content: center;
}

.stat-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-6);
  width: 100%;
  text-align: center;
}

.learning-modes-group {
  width: 100%;
  padding: var(--space-6);
  background-color: var(--bg-secondary);
  border: 2px solid var(--border-light);
  border-radius: var(--radius-2xl);
  box-sizing: border-box;
  box-shadow: var(--shadow-md);
}

.learning-mode-title {
  margin: 0 0 var(--space-5) 0;
  color: var(--text-primary);
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
}

.review-button-container {
  display: flex;
  gap: var(--space-5);
  margin-bottom: var(--space-4);
  justify-content: center;
  margin-top: 0;
}

.learning-mode-button {
  min-width: 160px;
  padding: var(--space-3) var(--space-6);
  border-radius: var(--radius-xl);
  font-size: var(--text-base);
  transition: all var(--transition-normal);
}

.learning-mode-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.learning-guide {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  flex-wrap: wrap;
}

.guide-text {
  margin: 0;
  max-width: 450px;
  line-height: var(--line-height-normal);
}

.guide-button {
  padding: var(--space-2) var(--space-5);
  border-radius: var(--radius-3xl);
  font-size: var(--text-sm);
  transition: all var(--transition-normal);
  white-space: nowrap;
}

.guide-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 169, 255, 0.3);
}

.chart-container {
  width: 420px;
  height: 320px;
  margin-left: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-chart {
  width: 100%;
  height: 100%;
}

.stat-item h3 {
  color: #606266;
  margin-bottom: 15px;
  font-size: 15px;
  font-weight: 500;
}

.stat-item p {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin: 0;
  transition: all 0.3s ease;
}

.stat-item:hover p {
  transform: scale(1.1);
  color: #67c23a;
}

/* 现代玻璃态筛选器 */
.filters {
  margin-bottom: 20px;
  display: flex;
  gap: 15px;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.1) 0%, 
    rgba(255, 255, 255, 0.05) 100%);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

/* 筛选器输入框样式 */
.filters .el-input {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.filters .el-input__wrapper {
  background: transparent;
  border: none;
  box-shadow: none;
  border-radius: 12px;
}

.filters .el-input__inner {
  background: transparent;
  color: var(--text-primary);
  border-radius: 12px;
}

.filters .el-input__inner::placeholder {
  color: var(--text-tertiary);
}

/* 筛选器下拉框样式 */
.filters .el-select {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.filters .el-select .el-input__wrapper {
  background: transparent;
  border: none;
  box-shadow: none;
  border-radius: 12px;
}

.filters .el-select .el-input__inner {
  background: transparent;
  color: var(--text-primary);
  border-radius: 12px;
}

.filters .el-select .el-input__inner::placeholder {
  color: var(--text-tertiary);
}

/* 单词卡片样式 */
.word-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}



/* 现代玻璃态单词卡片 - 接近文章卡片效果，保持三色配色和边缘效果 */
.word-card.el-card {
  position: relative;
  background: linear-gradient(135deg, var(--glass-white) 0%, rgba(255, 255, 255, 0.8) 100%) !important;
  border-radius: 20px !important;
  padding: 24px !important;
  margin-bottom: 20px !important;
  border: 2px solid transparent !important;
  background-clip: padding-box !important;
  backdrop-filter: blur(16px) !important;
  -webkit-backdrop-filter: blur(16px) !important;
  /* 移除默认阴影，由状态光晕接管 */
  box-shadow: none !important;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1) !important;
  overflow: hidden !important;
  cursor: pointer !important;
  display: flex !important;
  flex-direction: column !important;
  height: 100% !important;
}


/* 现代状态光晕 - 参考文章卡片设计，使用渐变边框 + 内发光 */
.word-card.el-card[data-status="unreviewed"] {
  border: 2px solid transparent !important;
  background: linear-gradient(135deg, var(--glass-white) 0%, rgba(255, 255, 255, 0.8) 100%) !important;
  background-clip: padding-box !important;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.3) !important;
  position: relative !important;
}

.word-card.el-card[data-status="unreviewed"]::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(64, 158, 255, 0.2) 0%, 
    rgba(64, 158, 255, 0.1) 50%, 
    rgba(100, 200, 255, 0.15) 100%);
  border-radius: 20px;
  opacity: 0.8;
  z-index: -1;
}

.word-card.el-card[data-status="reviewing"] {
  border: 2px solid transparent !important;
  background: linear-gradient(135deg, var(--glass-white) 0%, rgba(255, 255, 255, 0.8) 100%) !important;
  background-clip: padding-box !important;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.3) !important;
  position: relative !important;
}

.word-card.el-card[data-status="reviewing"]::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(255, 193, 7, 0.08) 0%, 
    rgba(255, 193, 7, 0.05) 50%, 
    rgba(255, 235, 59, 0.06) 100%);
  border-radius: 20px;
  opacity: 1;
  z-index: -1;
}

.word-card.el-card[data-status="mastered"] {
  border: 2px solid transparent !important;
  background: linear-gradient(135deg, var(--glass-white) 0%, rgba(255, 255, 255, 0.8) 100%) !important;
  background-clip: padding-box !important;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.3) !important;
  position: relative !important;
}

.word-card.el-card[data-status="mastered"]::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(103, 194, 58, 0.2) 0%, 
    rgba(103, 194, 58, 0.1) 50%, 
    rgba(120, 220, 100, 0.15) 100%);
  border-radius: 20px;
  opacity: 0.8;
  z-index: -1;
}

/* 现代进入动画：保持三色配色，增强玻璃态效果 */
@keyframes glow-in-blue {
  to { 
    box-shadow:
      0 8px 32px rgba(0, 0, 0, 0.1),
      0 2px 8px rgba(0, 0, 0, 0.05),
      0 0 12px 4px rgba(64, 158, 255, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.3) !important;
  }
}

@keyframes glow-in-orange {
  to { 
    box-shadow:
      0 8px 32px rgba(0, 0, 0, 0.1),
      0 2px 8px rgba(0, 0, 0, 0.05),
      0 0 12px 4px rgba(230, 162, 60, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.3) !important;
  }
}

@keyframes glow-in-green {
  to { 
    box-shadow: 
      0 8px 32px rgba(0, 0, 0, 0.1),
      0 2px 8px rgba(0, 0, 0, 0.05), 
      0 0 12px 4px rgba(103, 194, 58, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.3) !important;
  }
}


/* 现代悬停效果：参考文章卡片设计，增强渐变背景 */
.word-card.el-card:hover {
  transform: translateY(-8px) scale(1.02) !important;
  box-shadow:
    0 20px 40px rgba(0, 0, 0, 0.15),
    0 8px 16px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.3) !important;
}

.word-card.el-card:hover::before {
  opacity: 1;
}



/* 状态区域样式 */
.word-status {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.review-progress {
  flex: 1;
}

.aligned-progress-container {
  flex: 1;
}


/* 状态指示器：为所有状态提供直观的识别元素 - 现代胶囊样式 */
.status-indicator {
  display: inline-flex;
  align-items: center;
  margin-bottom: 0;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.status-indicator:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.25);
}

.status-indicator.unreviewed {
  color: white;
  background: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  box-shadow: 0 4px 12px rgba(0, 122, 255, 0.3);
}

.status-indicator.mastered {
  color: white;
  background: linear-gradient(135deg, #52D16A 0%, #4EDB6A 100%);
  box-shadow: 0 4px 12px rgba(82, 209, 106, 0.3);
}

.status-indicator.mastered-no-review {
  color: white;
  background: linear-gradient(135deg, #34C759 0%, #30D158 100%);
  box-shadow: 0 4px 12px rgba(52, 199, 89, 0.3);
}

.status-indicator.reviewing {
  color: white;
  background: linear-gradient(135deg, #FF9500 0%, #FFCC02 100%);
  box-shadow: 0 4px 12px rgba(255, 149, 0, 0.3);
}

.status-icon {
  margin-right: 6px;
  font-size: 14px;
}

/* 现代化进度条设计 */
.new-progress-bar {
  position: relative;
  height: 6px;
  background: linear-gradient(90deg, 
    rgba(0, 0, 0, 0.05) 0%, 
    rgba(0, 0, 0, 0.08) 100%);
  border-radius: 3px;
  overflow: visible;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.1);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #67c23a 0%, #e6a23c 100%);
  border-radius: 3px;
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(255, 255, 255, 0.3) 50%, 
    transparent 100%);
  border-radius: 3px;
  animation: shimmer 2s ease-in-out infinite;
}

.progress-checkmark {
  position: absolute;
  right: -2px;
  top: 50%;
  transform: translateY(-50%);
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  box-shadow: 
    0 3px 8px rgba(103, 194, 58, 0.4),
    0 1px 3px rgba(0, 0, 0, 0.2);
  border: 2px solid white;
  transition: all 0.3s ease;
}

/* 逾期状态的进度条标记 */
.progress-overdue {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%) !important;
  box-shadow: 
    0 3px 8px rgba(245, 108, 108, 0.4),
    0 1px 3px rgba(0, 0, 0, 0.2) !important;
}

/* 进度条光泽动画 */
@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* 进度条标签样式 - 降低视觉权重 */
.stat-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: normal; /* 降低字体粗细 */
  font-size: 11px; /* 稍微调小字体 */
  color: #909399; /* 使用浅灰色，降低视觉权重 */
}

/* 移除旧的进度条样式 */
.mini-progress {
  display: none;
}

/* 单词内容样式 */
.word-content {
  margin-top: 16px;
  padding: 0 4px;
}

/* 主要信息：单词本身 - 最大最醒目 */
.word-text {
  font-size: 28px;
  font-weight: 800;
  margin-bottom: 12px;
  letter-spacing: 0.5px;
  line-height: 1.2;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #2d3748 0%, #4a5568 50%, #455365 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 次要信息：音标 - 中等大小，辅助阅读 */
.word-phonetic {
  margin-bottom: 12px;
  font-style: italic;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.3px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #6b7280 0%, #9ca3af 50%, #d1d5db 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 核心信息：释义 - 重要但略小于单词 */
.word-meaning {
  margin-bottom: 12px;
  line-height: 1.5;
  font-size: 16px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
  background: linear-gradient(135deg, #374151 0%, #4b5563 50%, #6b7280 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 辅助信息：例句 - 较小字体，背景区分 */
.word-example {
  font-size: 14px;
  margin-bottom: 10px;
  line-height: 1.4;
  background: rgba(0, 0, 0, 0.03);
  padding: 8px 12px;
  border-radius: 6px;
  border-left: 3px solid var(--color-primary);
  font-weight: 500;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.05);
  background-image: linear-gradient(135deg, rgba(0, 0, 0, 0.03) 0%, rgba(0, 0, 0, 0.05) 100%);
  color: #6b7280;
}

/* 元信息：添加时间 - 最小字体，最淡颜色 */
.word-date {
  color: #c0c4cc;
  font-size: 11px;
  font-weight: 400;
}

/* 操作按钮样式 */
.word-actions {
  margin-top: 12px;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  align-items: center;
}

/* 删除按钮 - 右下角垃圾桶图标 */
.word-delete-btn {
  position: absolute;
  bottom: 12px;
  right: 12px;
  z-index: 10;
}

.delete-icon-btn {
  width: 28px !important;
  height: 28px !important;
  padding: 0 !important;
  background: rgba(245, 108, 108, 0.1) !important;
  border: 1px solid rgba(245, 108, 108, 0.3) !important;
  color: #f56c6c !important;
  transition: all 0.3s ease !important;
}

.delete-icon-btn:hover {
  background: rgba(245, 108, 108, 0.2) !important;
  border-color: #f56c6c !important;
  transform: scale(1.1) !important;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3) !important;
}

/* 视图切换按钮 */
.view-toggle {
  margin-bottom: 20px;
  text-align: center;
}

/* 叠层视图容器 */
.word-stack-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  position: relative;
  min-height: 500px;
}


/* 速刷导航按钮 */
.speed-nav-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.speed-nav-btn {
  min-width: 80px;
  font-size: 12px;
  padding: 8px 12px;
}

.word-stack {
  position: relative;
  width: 100%;
  max-width: 400px;
  height: 500px;
  margin-bottom: 20px;
  /* 添加透视效果，增强立体感 */
  perspective: 1000px;
}

.word-card-stack {
  width: 100%;
  height: 100%;
  /* 为每张卡片添加厚度感 */
  border-radius: 20px;
  overflow: hidden;
}

.word-card-stack:hover {
  /* 悬停时稍微抬起，增强交互感 */
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15) !important;
}

/* 当前卡片：完全不透明，完全遮挡下面的卡片 */
.word-card-stack:first-child .word-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.9) 100%) !important;
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(20px) !important;
  border: 2px solid rgba(255, 255, 255, 0.3) !important;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.4) !important;
}

/* 叠层卡片：保持玻璃感但降低透明度 */
.word-card-stack:not(:first-child) .word-card {
  border: 2px solid rgba(255, 255, 255, 0.2);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  /* 确保叠层卡片有完整的背景，避免透明 */
  background: linear-gradient(135deg, var(--glass-white) 0%, rgba(255, 255, 255, 0.9) 100%) !important;
}

/* 叠层卡片的特殊样式 */
.word-card-stack:not(:first-child) {
  /* 添加卡片边缘效果，模拟真实卡片 */
  border-radius: 20px;
  overflow: hidden;
  /* 确保不透明 */
  background: transparent;
}

/* 左右导航按钮 */
.stack-nav-left, .stack-nav-right {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 100;
}

.stack-nav-left {
  left: 20px;
}

.stack-nav-right {
  right: 20px;
}

.stack-nav-btn {
  width: 60px !important;
  height: 60px !important;
  background: linear-gradient(135deg, var(--glass-white) 0%, rgba(255, 255, 255, 0.9) 100%) !important;
  backdrop-filter: blur(16px) !important;
  -webkit-backdrop-filter: blur(16px) !important;
  border: 2px solid rgba(255, 255, 255, 0.3) !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15) !important;
  transition: all 0.3s ease !important;
}

.stack-nav-btn:hover {
  transform: scale(1.1) !important;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2) !important;
}

.stack-nav-btn:disabled {
  opacity: 0.5 !important;
  cursor: not-allowed !important;
  transform: none !important;
}

/* 底部进度显示 */
.stack-progress-bottom {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
}

.stack-progress {
  font-weight: 600;
  color: var(--text-primary);
  background: linear-gradient(135deg, var(--glass-white) 0%, rgba(255, 255, 255, 0.9) 100%);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  padding: 8px 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  min-width: 80px;
  text-align: center;
}

/* 分页样式 */
.pagination {
  margin-top: 20px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 听写模式样式 */
.dictation-card {
  text-align: center;
  padding: 40px 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.dictation-input-section {
  margin: 30px 0;
}

.dictation-feedback {
  margin: 20px 0;
}

.dictation-feedback.success {
  color: #67c23a;
}

.dictation-feedback.error {
  color: #f56c6c;
}

.dictation-hint {
  margin: 15px 0;
}

.hint-text {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

/* 复习模式样式 */
.review-mode {
  padding: 20px;
}

.review-progress-total {
  margin-bottom: 30px;
}

.review-card {
  text-align: center;
  padding: 40px 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.review-word {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #303133;
}

.review-phonetic {
  font-size: 18px;
  color: #909399;
  margin-bottom: 30px;
  font-style: italic;
}

.review-definition {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-top: 20px;
  text-align: center;
  line-height: 1.8;
  font-size: 20px;
}

.review-definition p {
  margin-bottom: 15px;
}

/* 复习模式中的单词进度条容器 */
.word-review-progress {
  margin-bottom: 20px;
  padding: 10px 0;
}

/* 艾宾浩斯进度条样式已在上方统一定义 */


.review-result-buttons {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.review-navigation {
  margin-top: 30px;
  text-align: center;
}

.no-review-words {
  padding: 60px 20px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .stats .el-card {
    flex-direction: column;
    gap: 20px;
  }

  .stats-content {
    grid-template-columns: 1fr 1fr 1fr;
  }

  .chart-container {
    width: 100%;
  }

  .word-cards {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  }
}

@media (max-width: 768px) {
  .stats .el-card {
    flex-direction: column;
  }

  .stats-content {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .filters {
    flex-direction: column;
    align-items: stretch;
  }

  .filters .el-input {
    width: 100% !important;
  }

  .vocabulary-container {
    padding: 10px;
  }

  .word-cards {
    grid-template-columns: 1fr;
  }

  .word-text {
    font-size: 24px;
  }
  
  .word-phonetic {
    font-size: 14px;
  }
  
  .word-meaning {
    font-size: 14px;
  }
  
  .word-example {
    font-size: 12px;
  }

  .review-word {
    font-size: 28px;
  }
}
</style>
