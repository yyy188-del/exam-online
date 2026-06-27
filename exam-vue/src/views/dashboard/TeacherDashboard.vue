<template>
  <div class="dashboard-container">
    <div class="stats-row">
      <div class="stat-item">
        <div class="stat-body">
          <div class="stat-left">
            <div class="stat-value">{{ stats.examCount }}</div>
            <div class="stat-title">考试管理</div>
          </div>
          <div class="stat-icon" style="background: #1890ff">
            <i class="el-icon-document" />
          </div>
        </div>
        <div class="stat-footer">系统中的考试场次</div>
      </div>

      <div class="stat-item">
        <div class="stat-body">
          <div class="stat-left">
            <div class="stat-value">{{ stats.repoCount }}</div>
            <div class="stat-title">题库管理</div>
          </div>
          <div class="stat-icon" style="background: #52c41a">
            <i class="el-icon-collection" />
          </div>
        </div>
        <div class="stat-footer">试题分类题库</div>
      </div>

      <div class="stat-item">
        <div class="stat-body">
          <div class="stat-left">
            <div class="stat-value">{{ stats.quCount }}</div>
            <div class="stat-title">试题管理</div>
          </div>
          <div class="stat-icon" style="background: #faad14">
            <i class="el-icon-edit-outline" />
          </div>
        </div>
        <div class="stat-footer">题库中的试题总数</div>
      </div>

      <div class="stat-item">
        <div class="stat-body">
          <div class="stat-left">
            <div class="stat-value">{{ stats.userExamCount }}</div>
            <div class="stat-title">考生记录</div>
          </div>
          <div class="stat-icon" style="background: #722ed1">
            <i class="el-icon-user" />
          </div>
        </div>
        <div class="stat-footer">学生参加考试的总记录</div>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="panel-card">
          <div slot="header" class="panel-header">
            <i class="el-icon-s-data" style="margin-right: 8px; color: #1890ff" />
            <span>快捷操作</span>
          </div>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/exam/repo')">
              <div class="action-icon" style="background: #e6f7ff">
                <i class="el-icon-collection" style="color: #1890ff" />
              </div>
              <div class="action-info">
                <div class="action-name">题库管理</div>
                <div class="action-desc">创建和管理试题题库</div>
              </div>
              <i class="el-icon-arrow-right action-arrow" />
            </div>
            <div class="action-item" @click="$router.push('/exam/qu')">
              <div class="action-icon" style="background: #f6ffed">
                <i class="el-icon-edit-outline" style="color: #52c41a" />
              </div>
              <div class="action-info">
                <div class="action-name">试题管理</div>
                <div class="action-desc">添加、编辑、导入试题</div>
              </div>
              <i class="el-icon-arrow-right action-arrow" />
            </div>
            <div class="action-item" @click="$router.push('/exam/exam')">
              <div class="action-icon" style="background: #fff7e6">
                <i class="el-icon-document" style="color: #faad14" />
              </div>
              <div class="action-info">
                <div class="action-name">考试管理</div>
                <div class="action-desc">创建和安排考试</div>
              </div>
              <i class="el-icon-arrow-right action-arrow" />
            </div>
            <div class="action-item" @click="$router.push('/exam/repo/add')">
              <div class="action-icon" style="background: #f9f0ff">
                <i class="el-icon-plus" style="color: #722ed1" />
              </div>
              <div class="action-info">
                <div class="action-name">添加题库</div>
                <div class="action-desc">创建新的试题分类</div>
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
            <span>操作提示</span>
          </div>
          <div class="tips-box">
            <div class="tips-list">
              <p>1. 先创建题库，再添加试题</p>
              <p>2. 支持Excel批量导入试题</p>
              <p>3. 创建考试时关联题库即可</p>
              <p>4. 考试可设置开放时间和限时</p>
              <p>5. 在考试记录中查看学生成绩</p>
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
  name: 'TeacherDashboard',
  data() {
    return {
      stats: {
        examCount: 0,
        repoCount: 0,
        quCount: 0,
        paperCount: 0,
        userExamCount: 0
      }
    }
  },
  created() {
    this.loadStats()
  },
  methods: {
    async loadStats() {
      try {
        const res = await post('/exam/api/sys/dashboard/teacher-stats', {})
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

