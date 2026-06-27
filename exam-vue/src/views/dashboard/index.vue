<template>
  <div>
    <!-- ==================== 超管控制台 ==================== -->
    <div v-if="isAdmin" class="dashboard-container">
      <div class="stats-row">
        <div class="stat-item" v-for="card in adminCards" :key="card.key">
          <div class="stat-body">
            <div class="stat-left">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-title">{{ card.title }}</div>
            </div>
            <div class="stat-icon" :style="{ background: card.color }">
              <i :class="card.icon" />
            </div>
          </div>
        </div>
      </div>

      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card class="panel-card">
            <div slot="header" class="panel-header">
              <i class="el-icon-data-line" style="margin-right: 8px; color: #1890ff" />
              <span>快捷操作</span>
            </div>
            <div class="quick-actions">
              <div class="action-item" @click="go('/exam/qu/add')">
                <div class="action-icon" style="background: #e6f7ff">
                  <i class="el-icon-edit-outline" style="color: #1890ff" />
                </div>
                <div class="action-info">
                  <div class="action-name">添加试题</div>
                  <div class="action-desc">手动添加考试题目</div>
                </div>
                <i class="el-icon-arrow-right action-arrow" />
              </div>
              <div class="action-item" @click="go('/exam/exam/add')">
                <div class="action-icon" style="background: #fff7e6">
                  <i class="el-icon-document" style="color: #faad14" />
                </div>
                <div class="action-info">
                  <div class="action-name">创建考试</div>
                  <div class="action-desc">创建新的考试安排</div>
                </div>
                <i class="el-icon-arrow-right action-arrow" />
              </div>
              <div class="action-item" @click="go('/sys/user')">
                <div class="action-icon" style="background: #f6ffed">
                  <i class="el-icon-user" style="color: #52c41a" />
                </div>
                <div class="action-info">
                  <div class="action-name">用户管理</div>
                  <div class="action-desc">管理系统用户</div>
                </div>
                <i class="el-icon-arrow-right action-arrow" />
              </div>
              <div class="action-item" @click="go('/exam/repo/add')">
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
        <el-col :span="12">
          <el-card class="panel-card">
            <div slot="header" class="panel-header">
              <i class="el-icon-info" style="margin-right: 8px; color: #1890ff" />
              <span>操作提示</span>
            </div>
            <div class="tips-box">
              <div class="tips-list">
                <p>1. 先创建题库，再导入试题</p>
                <p>2. 创建考试时关联题库抽题</p>
                <p>3. 支持单选、多选、判断题</p>
                <p>4. 可对用户进行角色分配</p>
                <p>5. 系统配置可修改站点信息</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card class="panel-card">
            <div slot="header" class="panel-header">
              <i class="el-icon-upload2" style="margin-right: 8px; color: #1890ff" />
              <span>数据导入</span>
            </div>
            <div class="import-box">
              <div class="import-item">
                <span>试题导入</span>
                <el-button type="text" size="small" @click="downloadQuTemplate">下载模板</el-button>
                <el-upload
                  :show-file-list="false"
                  :http-request="uploadQu"
                  accept=".xlsx,.xls"
                  style="display: inline-block"
                >
                  <el-button type="text" size="small" style="color: #52c41a">上传导入</el-button>
                </el-upload>
              </div>
              <div class="import-item">
                <span>用户导入</span>
                <el-button type="text" size="small" @click="downloadUserTemplate">下载模板</el-button>
                <el-upload
                  :show-file-list="false"
                  :http-request="uploadUser"
                  accept=".xlsx,.xls"
                  style="display: inline-block"
                >
                  <el-button type="text" size="small" style="color: #52c41a">上传导入</el-button>
                </el-upload>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- ==================== 教师控制台 ==================== -->
    <div v-else-if="isTeacher" class="dashboard-container">
      <div class="stats-row">
        <div class="stat-item" v-for="card in teacherCards" :key="card.key">
          <div class="stat-body">
            <div class="stat-left">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-title">{{ card.title }}</div>
            </div>
            <div class="stat-icon" :style="{ background: card.color }">
              <i :class="card.icon" />
            </div>
          </div>
        </div>
      </div>

      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card class="panel-card">
            <div slot="header" class="panel-header">
              <i class="el-icon-data-line" style="margin-right: 8px; color: #1890ff" />
              <span>快捷操作</span>
            </div>
            <div class="quick-actions">
              <div class="action-item" @click="go('/exam/repo')">
                <div class="action-icon" style="background: #e6f7ff">
                  <i class="el-icon-collection" style="color: #1890ff" />
                </div>
                <div class="action-info">
                  <div class="action-name">题库管理</div>
                  <div class="action-desc">创建和管理试题题库</div>
                </div>
                <i class="el-icon-arrow-right action-arrow" />
              </div>
              <div class="action-item" @click="go('/exam/qu')">
                <div class="action-icon" style="background: #f6ffed">
                  <i class="el-icon-edit-outline" style="color: #52c41a" />
                </div>
                <div class="action-info">
                  <div class="action-name">试题管理</div>
                  <div class="action-desc">添加、编辑、导入试题</div>
                </div>
                <i class="el-icon-arrow-right action-arrow" />
              </div>
              <div class="action-item" @click="go('/exam/exam')">
                <div class="action-icon" style="background: #fff7e6">
                  <i class="el-icon-document" style="color: #faad14" />
                </div>
                <div class="action-info">
                  <div class="action-name">考试管理</div>
                  <div class="action-desc">创建和安排考试</div>
                </div>
                <i class="el-icon-arrow-right action-arrow" />
              </div>
              <div class="action-item" @click="go('/exam/repo/add')">
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
        <el-col :span="12">
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

      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card class="panel-card">
            <div slot="header" class="panel-header">
              <i class="el-icon-upload2" style="margin-right: 8px; color: #1890ff" />
              <span>试题导入</span>
            </div>
            <div class="import-box">
              <div class="import-item">
                <span>批量导入试题</span>
                <el-button type="text" size="small" @click="downloadQuTemplate">下载模板</el-button>
                <el-upload
                  :show-file-list="false"
                  :http-request="uploadQu"
                  accept=".xlsx,.xls"
                  style="display: inline-block"
                >
                  <el-button type="text" size="small" style="color: #52c41a">上传导入</el-button>
                </el-upload>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="panel-card">
            <div slot="header" class="panel-header">
              <i class="el-icon-user" style="margin-right: 8px; color: #52c41a" />
              <span>用户导入</span>
            </div>
            <div class="import-box">
              <div class="import-item">
                <span>批量导入用户</span>
                <el-button type="text" size="small" @click="downloadUserTemplate">下载模板</el-button>
                <el-upload
                  :show-file-list="false"
                  :http-request="uploadUser"
                  accept=".xlsx,.xls"
                  style="display: inline-block"
                >
                  <el-button type="text" size="small" style="color: #52c41a">上传导入</el-button>
                </el-upload>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- ==================== 学生控制台 ==================== -->
    <div v-else class="dashboard-container">
      <div class="stats-row">
        <div class="stat-item" v-for="card in studentCards" :key="card.key">
          <div class="stat-body">
            <div class="stat-left">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-title">{{ card.title }}</div>
            </div>
            <div class="stat-icon" :style="{ background: card.color }">
              <i :class="card.icon" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { post, upload } from '@/utils/request'
import { download } from '@/utils/request'
import config from '../../../package.json'

export default {
  name: 'Dashboard',
  data() {
    return {
      version: config.version,
      stats: {
        examCount: 0,
        repoCount: 0,
        userCount: 0,
        paperCount: 0
      }
    }
  },
  computed: {
    isAdmin() {
      const user = this.$store.state.user || {}
      const roles = user.roles || []
      return roles.includes('sa')
    },
    isTeacher() {
      const user = this.$store.state.user || {}
      const roles = user.roles || []
      return !roles.includes('sa') && roles.includes('teacher')
    },
    adminCards() {
      return [
        { key: 'exam', title: '考试总数', value: this.stats.examCount, icon: 'el-icon-document', color: '#13c2c2' },
        { key: 'repo', title: '题库总数', value: this.stats.repoCount, icon: 'el-icon-collection', color: '#1890ff' },
        { key: 'user', title: '用户总数', value: this.stats.userCount, icon: 'el-icon-user', color: '#52c41a' },
        { key: 'paper', title: '考试记录', value: this.stats.paperCount, icon: 'el-icon-data-line', color: '#faad14' }
      ]
    },
    teacherCards() {
      return [
        { key: 'repo', title: '我的题库', value: this.stats.repoCount, icon: 'el-icon-collection', color: '#1890ff' },
        { key: 'exam', title: '我的考试', value: this.stats.examCount, icon: 'el-icon-document', color: '#13c2c2' },
        { key: 'paper', title: '考试记录', value: this.stats.paperCount, icon: 'el-icon-data-line', color: '#faad14' },
        { key: 'userExam', title: '考生人次', value: this.stats.userExamCount, icon: 'el-icon-user', color: '#52c41a' }
      ]
    },
    studentCards() {
      return [
        { key: 'exam', title: '可考试卷', value: this.stats.examCount, icon: 'el-icon-document', color: '#13c2c2' },
        { key: 'myExam', title: '我的考试', value: this.stats.myExamCount, icon: 'el-icon-data-line', color: '#1890ff' },
        { key: 'passed', title: '已通过', value: this.stats.passedCount, icon: 'el-icon-circle-check', color: '#52c41a' }
      ]
    }
  },
  created() {
    this.loadStats()
  },
  methods: {
    go(path) {
      this.$router.push(path)
    },
    async loadStats() {
      try {
        const res = await post('/exam/api/sys/dashboard/stats', {})
        if (res.code === 0) {
          this.stats = res.data
        }
      } catch (e) {
        console.log('统计数据加载失败，使用默认值')
      }
    },
    downloadQuTemplate() {
      download('/exam/api/qu/qu/import/template', {}, '试题导入模板.xlsx')
    },
    async uploadQu(file) {
      try {
        const res = await upload('/exam/api/qu/qu/import', file.file)
        if (res.code === 0) {
          this.$message.success('试题导入成功')
          this.loadStats()
        } else {
          this.$message.error(res.msg || '导入失败')
        }
      } catch (e) {
        this.$message.error('导入失败，请检查文件格式')
      }
    },
    downloadUserTemplate() {
      download('/exam/api/sys/user/import/template', {}, '用户导入模板.xlsx')
    },
    async uploadUser(file) {
      try {
        const res = await upload('/exam/api/sys/user/import', file.file)
        if (res.code === 0) {
          this.$message.success(res.data || '用户导入成功')
          this.loadStats()
        } else {
          this.$message.error(res.msg || '导入失败')
        }
      } catch (e) {
        this.$message.error('导入失败，请检查文件格式')
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

.import-section {
  padding: 20px;
}

.import-desc {
  background: #f6f8fb;
  padding: 12px 16px;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  line-height: 1.8;
  margin-bottom: 16px;
}

.import-actions {
  margin-bottom: 24px;
}

.import-steps {
  margin-top: 8px;
  padding: 0 10px;
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
  margin: 0 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f6f9fc 0%, #f0f5ff 100%);
  border-radius: 8px;
  border: 1px solid #e8f0fe;
}

.tips-title {
  font-size: 14px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 12px;
}

.tips-list {
  font-size: 13px;
  color: #666;
  line-height: 2;
}
</style>
