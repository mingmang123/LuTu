<template>
  <div class="portrait-guide-page">
    <div class="guide-container">
      <div class="guide-header">
        <div class="logo">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z" fill="currentColor"/>
          </svg>
        </div>
        <h1>定制您的旅行偏好</h1>
        <p>让我们了解您的旅行风格，为您推荐更合适的行程</p>
      </div>

      <div class="guide-content">
        <!-- 步骤指示器 -->
        <div class="step-indicator">
          <div v-for="(step, index) in steps" :key="index" 
               :class="['step', { active: currentStep === index, completed: currentStep > index }]">
            <div class="step-number">{{ index + 1 }}</div>
            <div class="step-label">{{ step.label }}</div>
          </div>
        </div>

        <!-- 步骤1: 旅行风格 -->
        <div v-if="currentStep === 0" class="step-content">
          <h2>您喜欢什么样的旅行？</h2>
          <p class="step-desc">选择最吸引您的旅行方式</p>
          <div class="options-grid">
            <div v-for="style in travelStyles" :key="style.value"
                 :class="['option-card', { selected: portrait.travelStyle === style.value }]"
                 @click="selectTravelStyle(style.value)">
              <div class="option-icon">{{ style.icon }}</div>
              <div class="option-title">{{ style.label }}</div>
              <div class="option-desc">{{ style.desc }}</div>
            </div>
          </div>
        </div>

        <!-- 步骤2: 预算档位 -->
        <div v-if="currentStep === 1" class="step-content">
          <h2>您的旅行预算倾向？</h2>
          <p class="step-desc">选择适合您的消费档位</p>
          <div class="options-grid">
            <div v-for="budget in budgetOptions" :key="budget.value"
                 :class="['option-card', { selected: portrait.budgetStyle === budget.value }]"
                 @click="selectBudgetStyle(budget.value)">
              <div class="option-icon">{{ budget.icon }}</div>
              <div class="option-title">{{ budget.label }}</div>
              <div class="option-desc">{{ budget.desc }}</div>
            </div>
          </div>
        </div>

        <!-- 步骤3: 旅行节奏 -->
        <div v-if="currentStep === 2" class="step-content">
          <h2>您偏好什么样的旅行节奏？</h2>
          <p class="step-desc">选择您喜欢的游玩方式</p>
          <div class="options-grid">
            <div v-for="rhythm in rhythmOptions" :key="rhythm.value"
                 :class="['option-card', { selected: portrait.travelRhythm === rhythm.value }]"
                 @click="selectTravelRhythm(rhythm.value)">
              <div class="option-icon">{{ rhythm.icon }}</div>
              <div class="option-title">{{ rhythm.label }}</div>
              <div class="option-desc">{{ rhythm.desc }}</div>
            </div>
          </div>
        </div>

        <!-- 步骤4: 同行人群 -->
        <div v-if="currentStep === 3" class="step-content">
          <h2>您通常和谁一起旅行？</h2>
          <p class="step-desc">选择您的同行伙伴（可在个人信息中修改）</p>
          <div class="options-grid">
            <div v-for="crowd in crowdOptions" :key="crowd.value"
                 :class="['option-card', { selected: portrait.crowdType === crowd.value }]"
                 @click="selectCrowdType(crowd.value)">
              <div class="option-icon">{{ crowd.icon }}</div>
              <div class="option-title">{{ crowd.label }}</div>
              <div class="option-desc">{{ crowd.desc }}</div>
            </div>
          </div>
        </div>

        <!-- 错误提示 -->
        <div v-if="error" class="error-message">
          <svg class="message-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" fill="currentColor"/>
          </svg>
          {{ error }}
        </div>
      </div>

      <!-- 按钮区域 -->
      <div class="guide-actions">
        <button v-if="currentStep > 0" class="btn-secondary" @click="prevStep">
          上一步
        </button>
        <button v-if="currentStep < steps.length - 1" 
                class="btn-primary" 
                :disabled="!canProceed"
                @click="nextStep">
          下一步
        </button>
        <button v-else 
                class="btn-primary" 
                :disabled="!canSubmit || submitting"
                @click="submitPortrait">
          {{ submitting ? '保存中...' : '完成' }}
        </button>
      </div>

      <!-- 跳过按钮 -->
      <div class="skip-section">
        <button class="btn-skip" @click="skipGuide">跳过，稍后设置</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { addUserPortrait } from '@/api/userPortrait'

const router = useRouter()

const currentStep = ref(0)
const error = ref('')
const submitting = ref(false)

const portrait = ref({
  travelStyle: null,
  budgetStyle: null,
  travelRhythm: null,
  crowdType: null
})

const steps = [
  { label: '旅行风格' },
  { label: '预算档位' },
  { label: '旅行节奏' },
  { label: '同行人群' }
]

const travelStyles = [
  { value: 1, label: '山水风光', icon: '⛰️', desc: '名山大川、自然风景' },
  { value: 2, label: '海滨度假', icon: '🏖️', desc: '阳光沙滩、海岛风情' },
  { value: 3, label: '古镇人文', icon: '🏮', desc: '历史古迹、文化体验' },
  { value: 4, label: '城市探索', icon: '🏙️', desc: '现代都市、繁华商圈' },
  { value: 5, label: '小众秘境', icon: '🌿', desc: '人少景美、独特体验' }
]

const budgetOptions = [
  { value: 1, label: '性价比优选', icon: '💰', desc: '经济实惠、物超所值' },
  { value: 2, label: '舒适平价', icon: '💰💰', desc: '品质与价格的平衡' },
  { value: 3, label: '高端奢享', icon: '💰💰💰', desc: '顶级体验、尊贵服务' }
]

const rhythmOptions = [
  { value: 1, label: '慢享休闲', icon: '😌', desc: '不赶时间、深度体验' },
  { value: 2, label: '短途打卡', icon: '📸', desc: '高效游览、精华景点' },
  { value: 3, label: '正常游玩', icon: '🎯', desc: '劳逸结合、张弛有度' }
]

const crowdOptions = [
  { value: 1, label: '独自旅行', icon: '🚶', desc: '一个人自由自在' },
  { value: 2, label: '情侣出游', icon: '💑', desc: '浪漫二人世界' },
  { value: 3, label: '家庭亲子', icon: '👨‍👩‍👧‍👦', desc: '带着孩子看世界' },
  { value: 4, label: '朋友结伴', icon: '👫', desc: '好友同行欢乐多' },
  { value: 5, label: '商务出差', icon: '💼', desc: '工作之余顺便游玩' }
]

const canProceed = computed(() => {
  switch (currentStep.value) {
    case 0: return portrait.value.travelStyle !== null
    case 1: return portrait.value.budgetStyle !== null
    case 2: return portrait.value.travelRhythm !== null
    case 3: return portrait.value.crowdType !== null
    default: return false
  }
})

const canSubmit = computed(() => {
  return portrait.value.travelStyle !== null &&
         portrait.value.budgetStyle !== null &&
         portrait.value.travelRhythm !== null &&
         portrait.value.crowdType !== null
})

const selectTravelStyle = (value) => {
  portrait.value.travelStyle = value
}

const selectBudgetStyle = (value) => {
  portrait.value.budgetStyle = value
}

const selectTravelRhythm = (value) => {
  portrait.value.travelRhythm = value
}

const selectCrowdType = (value) => {
  portrait.value.crowdType = value
}

const nextStep = () => {
  if (currentStep.value < steps.length - 1) {
    currentStep.value++
    error.value = ''
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
    error.value = ''
  }
}

const submitPortrait = async () => {
  submitting.value = true
  error.value = ''
  
  try {
    const res = await addUserPortrait(portrait.value)
    if (res.code === 200) {
      router.push('/dashboard')
    } else {
      error.value = res.msg || '保存失败，请重试'
    }
  } catch (err) {
    error.value = err.message || '网络错误，请重试'
  } finally {
    submitting.value = false
  }
}

const skipGuide = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.portrait-guide-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #f4a261 0%, #e76f51 30%, #2a9d8f 70%, #264653 100%);
  padding: 20px;
}

.guide-container {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 24px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.15), 0 10px 30px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 800px;
  position: relative;
  z-index: 1;
}

.guide-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #e9c46a 0%, #f4a261 50%, #e76f51 100%);
  border-radius: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 16px;
  box-shadow: 0 8px 24px rgba(231, 111, 81, 0.3);
}

.logo svg {
  width: 32px;
  height: 32px;
  color: white;
}

.guide-header h1 {
  color: #264653;
  font-size: 28px;
  margin-bottom: 8px;
  font-weight: 700;
}

.guide-header p {
  color: #6b7280;
  font-size: 14px;
}

/* 步骤指示器 */
.step-indicator {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-bottom: 32px;
  position: relative;
}

.step-indicator::before {
  content: '';
  position: absolute;
  top: 16px;
  left: 20%;
  right: 20%;
  height: 2px;
  background: #e5e7eb;
  z-index: 0;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  position: relative;
  z-index: 1;
}

.step-number {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #e5e7eb;
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
}

.step.active .step-number {
  background: linear-gradient(135deg, #e76f51 0%, #f4a261 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(231, 111, 81, 0.4);
}

.step.completed .step-number {
  background: #2a9d8f;
  color: white;
}

.step-label {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
}

.step.active .step-label {
  color: #e76f51;
}

.step.completed .step-label {
  color: #2a9d8f;
}

/* 步骤内容 */
.step-content {
  text-align: center;
}

.step-content h2 {
  color: #264653;
  font-size: 22px;
  margin-bottom: 8px;
  font-weight: 600;
}

.step-desc {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 24px;
}

.options-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}

.option-card {
  background: #f9fafb;
  border: 2px solid transparent;
  border-radius: 16px;
  padding: 24px 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
}

.option-card:hover {
  background: #f3f4f6;
  transform: translateY(-2px);
}

.option-card.selected {
  background: white;
  border-color: #e76f51;
  box-shadow: 0 8px 24px rgba(231, 111, 81, 0.2);
}

.option-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.option-title {
  color: #374151;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.option-desc {
  color: #9ca3af;
  font-size: 12px;
}

/* 错误消息 */
.error-message {
  margin-top: 20px;
  padding: 14px 16px;
  background: rgba(239, 68, 68, 0.08);
  color: #dc2626;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.message-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

/* 按钮区域 */
.guide-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

.btn-primary {
  padding: 14px 32px;
  background: linear-gradient(135deg, #2a9d8f 0%, #264653 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(42, 157, 143, 0.35);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(42, 157, 143, 0.45);
}

.btn-primary:disabled {
  background: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-secondary {
  padding: 14px 32px;
  background: white;
  color: #6b7280;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  border-color: #d1d5db;
  color: #4b5563;
}

.skip-section {
  text-align: center;
  margin-top: 20px;
}

.btn-skip {
  background: none;
  border: none;
  color: #9ca3af;
  font-size: 14px;
  cursor: pointer;
  text-decoration: underline;
  transition: color 0.2s ease;
}

.btn-skip:hover {
  color: #6b7280;
}

/* 响应式设计 */
@media (max-width: 640px) {
  .guide-container {
    padding: 24px 20px;
  }

  .guide-header h1 {
    font-size: 22px;
  }

  .step-indicator {
    gap: 20px;
  }

  .step-label {
    font-size: 11px;
  }

  .options-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .option-card {
    padding: 16px 12px;
  }

  .option-icon {
    font-size: 32px;
  }

  .option-title {
    font-size: 13px;
  }

  .guide-actions {
    flex-direction: column;
  }

  .btn-primary,
  .btn-secondary {
    width: 100%;
  }
}
</style>
