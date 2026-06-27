<template>
  <div class="dashboard-container">
    <div class="stats-row">
      <div class="stat-item">
        <div class="stat-body">
          <div class="stat-left">
            <div class="stat-value">{{ stats.examCount }}</div>
            <div class="stat-title">可参加考试</div>
          </div>
          <div class="stat-icon" style="background: #1890ff">
            <i class="el-icon-document" />
          </div>
        </div>
        <div class="stat-footer">系统中所有开放的考试</div>
      </div>

      <div class="stat-item">
        <div class="stat-body">
          <div class="stat-left">
            <div class="stat-value">{{ stats.myExamCount }}</div>
            <div class="stat-title">我的考试</div>
          </div>
          <div class="stat-icon" style="background: #52c41a">
            <i class="el-icon-edit-outline" />
          </div>
        </div>
        <div class="stat-footer">已参加的考试次数</div>
      </div>

      <div class="stat-item">
        <div class="stat-body">
          <div class="stat-left">
            <div class="stat-value">{{ stats.passedCount }}</div>
            <div class="stat-title">已通过</div>
          </div>
          <div class="stat-icon" style="background: #faad14">
            <i class="el-icon-success" />
          </div>
        </div>
        <div class="stat-footer">已通过及格线的考试</div>
      </div>

      <div class="stat-item">
        <div class="stat-body">
          <div class="stat-left">
            <div class="stat-value">{{ notPassedCount }}</div>
            <div class="stat-title">未通过</div>
          </div>
          <div class="stat-icon" style="background: #f5222d">
            <i class="el-icon-error" />
          </div>
        </div>
        <div class="stat-footer">还需努力的考试</div>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="panel-card">
          <div slot="header" class="panel-header">
            <i class="el-icon-s-data" style="margin-right: 8px; color: #1890ff" />
            <span>快捷入口</span>
          </div>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/my/exam/online')">
              <div class="action-icon" style="background: #e6f7ff">
                <i class="el-icon-video-play" style="color: #1890ff" />
              </div>
              <div class="action-info">
                <div class="action-name">在线考试</div>
                <div class="action-desc">参加正在开放的考试</div>
              </div>
              <i class="el-icon-arrow-right action-arrow" />
            </div>
            <div class="action-item" @click="$router.push('/my/exam/records')">
              <div class="action-icon" style="background: #f6ffed">
                <i class="el-icon-data-line" style="color: #52c41a" />
              </div>
              <div class="action-info">
                <div class="action-name">考试记录</div>
                <div class="action-desc">查看我的考试历史和成绩</div>
              </div>
              <i class="el-icon-arrow-right action-arrow" />
            </div>
            <div class="action-item" @click="$router.push('/my/book/list')">
              <div class="action-icon" style="background: #fff7e6">
                <i class="el-icon-collection" style="color: #faad14" />
              </div>
              <div class="action-info">
                <div class="action-name">错题本</div>
                <div class="action-desc">回顾错题，查漏补缺</div>
              </div>
              <i class="el-icon-arrow-right action-arrow" />
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="panel-card">
          <div slot="header" class="panel-header">
            <i class="el-icon-info" style="margin-right: 8px; color: #1890ff" />
            <span>考试提示</span>
          </div>
          <div class="tips-box">
            <div class="tips-list">
              <p>1. 请在规定时间内完成考试</p>
              <p>2. 考试过程中请勿关闭页面</p>
              <p>3. 超时系统将自动交卷</p>
              <p>4. 交卷后可查看成绩和解析</p>
              <p>5. 错题会自动加入错题本</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script>
import { post } from '@/utils/request'

export default {
  name: 'StudentDashboard',
  data() {
    return {
      stats: {
        examCount: 0,
        myExamCount: 0,
        passedCount: 0
      }
    }
  },
  computed: {
    notPassedCount() {
      return Math.max(0, this.stats.myExamCount - this.stats.passedCount)
    }
  },
  created() {
    this.loadStats()
  },
  methods: {
    async loadStats() {
      try {
        const res = await post('/exam/api/sys/dashboard/student-stats', {})
        if (res.code === 0) {
          this.stats = res.data
        }
      } catch (e) {
        console.log('load stats error')
      }
    }
  }
}
</script>
<style scoped>
.dashboard-container {
  padding: 0;
}
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}
.stat-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.stat-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 12px;
}
.stat-left {
  flex: 1;
}
.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1;
}
.stat-title {
  font-size: 14px;
  color: #999;
  margin-top: 8px;
}
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  flex-shrink: 0;
}
.stat-footer {
  border-top: 1px solid #f0f0f0;
  padding: 8px 24px;
  font-size: 12px;
  color: #bbb;
}
.panel-card {
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.panel-card >>> .el-card__header {
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
}
.panel-card >>> .el-card__body {
  padding: 0;
}
.panel-header {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
}
.quick-actions {
  padding: 8px 20px;
}
.action-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
}
.action-item:hover {
  background: #f5f7fa;
}
.action-item:last-child {
  margin-bottom: 0;
}
.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.action-info {
  flex: 1;
  margin-left: 14px;
}
.action-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
.action-desc {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
.action-arrow {
  color: #ccc;
  font-size: 14px;
}
.tips-box {
  padding: 16px 20px;
  background: linear-gradient(135deg, #f6f9fc 0%, #f0f5ff 100%);
}
.tips-list {
  font-size: 13px;
  color: #666;
  line-height: 2;
}
.tips-list p {
  margin: 0;
}
</style>

